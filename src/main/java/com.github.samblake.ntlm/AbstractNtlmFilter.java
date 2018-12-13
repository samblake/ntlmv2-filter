package com.github.samblake.ntlm;

import com.github.samblake.ntlm.cache.CacheProvider;
import com.github.samblake.ntlm.config.ConfigProvider;
import jcifs.util.Base64;
import org.ntlmv2.filter.NtlmV2HttpRequestWrapper;
import org.ntlmv2.liferay.NtlmManager;
import org.ntlmv2.liferay.NtlmUserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.ntlmv2.liferay.util.HttpHeaders.WWW_AUTHENTICATE;

/**
 * Performs NTLMv2 authentication. If the users is successfully authenticated the username will be
 * available via {@link HttpServletRequest#getRemoteUser()} and {@link HttpServletRequest#getUserPrincipal()}.
 */
public abstract class AbstractNtlmFilter implements Filter {
	private static Logger log = LoggerFactory.getLogger(AbstractNtlmFilter.class);
	
	public static final String NTLM_USER_ACCOUNT = "ntlmUserAccount";
	
	/** Random number generator for challenge creation. */
	private SecureRandom secureRandom = new SecureRandom();
	
	/** Stores reference to the singleton cache manager. */
	private CacheProvider cacheProvider;
	
	/** NtlmManager instance. */
	private NtlmManager ntlmManager;
	
	private boolean enabled;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.info("Initializing NTLMv2 filter");
		
		ConfigProvider configProvider = createConfigProvider(filterConfig);
		enabled = configProvider.isEnabled();
		if (enabled) {
			try {
				cacheProvider = createCacheProvider(filterConfig);
				cacheProvider.init(filterConfig);
				
				ntlmManager = createNtlmManager(configProvider);
			}
			catch (InitialisationException e) {
				throw new ServletException("NTLM filter initialization failed", e);
			}
			
			log.info("NTLMv2 filter initialized");
		}
		else {
			log.info("NTLMv2 filter disabled");
		}
	}
	
	private NtlmManager createNtlmManager(ConfigProvider configProvider) throws InitialisationException {
		String domain = configProvider.getDomain();
		log.info("Windows domain: " + domain);
		
		String domainControllerIp = configProvider.getDomainControllerIp();
		log.info("Domain controller IP address: " + domainControllerIp);
		
		String domainControllerHostname = configProvider.getDomainControllerHostname();
		log.info("Domain controller hostname: " + domainControllerHostname);
		
		String serviceAccount = configProvider.getServiceAccount();
		log.info("Computer account name: " + serviceAccount);
		
		String servicePassword = configProvider.getServicePassword();
		
		return new NtlmManager(domain, domainControllerIp,
				domainControllerHostname, serviceAccount, servicePassword);
	}
	
	/**
	 * Obtains the {@link CacheProvider} to be used by the filter.
	 */
	protected abstract CacheProvider createCacheProvider(FilterConfig filterConfig);
	
	/**
	 * Obtains the {@link ConfigProvider} to be used by the filter.
	 */
	protected abstract ConfigProvider createConfigProvider(FilterConfig filterConfig);
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain filterChain) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		
		if (shouldHandleRequest(request)) {
			handleRequest(filterChain, request, response);
		}
		else {
			filterChain.doFilter(req, response);
		}
	}
	
	/**
	 * If this method returns true the filter will process the request, if it returns false the next filter in
	 * the chain will be called.
	 */
	protected boolean shouldHandleRequest(HttpServletRequest request) {
		return enabled;
	}
	
	private void handleRequest(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		// Type 1 NTLM requests from browser can (and should) always immediately
		// be replied to with an Type 2 NTLM response, no matter whether we're
		// yet logging in or whether it is much later in the session.
		
		if (log.isDebugEnabled()) {
			log.debug("Processing NTLM request");
		}
		
		HttpSession session = request.getSession(false);
		
		String authorization = request.getHeader("Authorization");
		if (authorization != null && authorization.startsWith("NTLM")) {
			
			byte[] src = Base64.decode(authorization.substring(5));
			if (src[8] == 1) {
				if (log.isDebugEnabled()) {
					log.debug("Creating server challenge");
				}
				
				byte[] serverChallenge = new byte[8];
				secureRandom.nextBytes(serverChallenge);
				
				byte[] challengeMessage = ntlmManager.negotiate(src, serverChallenge);
				cacheProvider.put(request.getRemoteAddr(), serverChallenge);
				
				sendAuthenticateResponse(response, "NTLM " + Base64.encode(challengeMessage));
				return;
			}
			
			byte[] serverChallenge = cacheProvider.get(request.getRemoteAddr());
			if (serverChallenge == null) {
				if (log.isDebugEnabled()) {
					log.debug("Start NTLM login");
				}
				
				sendAuthenticateResponse(response);
				return;
			}
			
			NtlmUserAccount ntlmUserAccount = null;
			try {
				if (log.isDebugEnabled()) {
					log.debug("Attempting authenticating");
				}
				
				ntlmUserAccount = ntlmManager.authenticate(src, serverChallenge);
				
				log.info("Authentication was successful");
				session = request.getSession(true);
				session.setAttribute(NTLM_USER_ACCOUNT, ntlmUserAccount);
			}
			catch (Exception e) {
				log.error("NTLM authentication failed", e);
				handleAuthenticationFailure(request, response);
				return;
			}
			finally {
				cacheProvider.remove(request.getRemoteAddr());
			}
			
			if (ntlmUserAccount == null) {
				// No NTLM user in session yet, or authentication failed
				sendAuthenticateResponse(response);
				return;
			}
			
			if (log.isDebugEnabled()) {
				log.debug("NTLM remote user " + ntlmUserAccount.getUserName());
			}
		}
		
		// Check if NTLM user account has already been stored in session
		NtlmUserAccount ntlmUserAccount = null;
		if (session != null) {
			ntlmUserAccount = (NtlmUserAccount)session.getAttribute(NTLM_USER_ACCOUNT);
		}
		
		HttpServletRequest filteredReq = request;
		if (ntlmUserAccount == null) {
			if (log.isDebugEnabled()) {
				log.debug("Begin authentication");
			}
			
			sendAuthenticateResponse(response);
			return;
		}
		
		if (log.isDebugEnabled()) {
			log.debug("NTLM user in session: " + ntlmUserAccount.getUserName());
		}
		
		if (!(request instanceof NtlmV2HttpRequestWrapper)) {
			// Wrap original request only once
			filteredReq = new NtlmV2HttpRequestWrapper(request, ntlmUserAccount.getUserName());
		}
		
		filterChain.doFilter(filteredReq, response);
	}
	
	protected void handleAuthenticationFailure(HttpServletRequest request, HttpServletResponse response) throws IOException {
		sendAuthenticateResponse(response);
	}
	
	private void sendAuthenticateResponse(HttpServletResponse response) throws IOException {
		sendAuthenticateResponse(response, "NTLM");
	}
	
	private void sendAuthenticateResponse(HttpServletResponse response, String authValue) throws IOException {
		response.setContentLength(0);
		response.setHeader(WWW_AUTHENTICATE, authValue);
		response.setStatus(SC_UNAUTHORIZED);
		response.flushBuffer();
	}
	
	@Override
	public void destroy() {
		cacheProvider.destroy();
	}
	
}