package com.github.samblake.ntlm.cache;

import com.github.samblake.ntlm.InitialisationException;
import com.github.samblake.ntlm.AbstractNtlmFilter;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterConfig;

/**
 * Provides caching via <a href="https://www.ehcache.org/">Ehcache</a>.
 */
public class EhCacheProvider implements CacheProvider {
	private static Logger log = LoggerFactory.getLogger(AbstractNtlmFilter.class);
	
	private static final String CACHE_NAME = "ntlmChallengeCache";
	
	private Cache cache;
	
	@Override
	public void init(FilterConfig filterConfig) throws InitialisationException {
		try {
			CacheManager cacheManager = CacheManager.create(this.getClass().getResourceAsStream("/ehcache.xml"));
			cache = cacheManager.getCache(CACHE_NAME);
			
			if (log.isTraceEnabled()) {
				log.trace("NTLM challenge cache initialized");
			}
		}
		catch (CacheException e) {
			throw new InitialisationException("Failed to initialize cache", e);
		}
	}
	
	@Override
	public synchronized void put(String remoteAddress, byte[] serverChallenge) {
		if (log.isTraceEnabled()) {
			log.trace("Cache server challenge for: " + remoteAddress);
		}
		
		cache.put(new Element(remoteAddress, serverChallenge));
	}
	
	@Override
	public synchronized void remove(String remoteAddress) {
		cache.remove(remoteAddress);
	}
	
	@Override
	public synchronized byte[] get(String remoteAddress) {
		try {
			if (log.isTraceEnabled()) {
				log.trace("Get cached server challenge for: " + remoteAddress);
			}
			
			Element challengeHolder = cache.get(remoteAddress);
			return (byte[])challengeHolder.getObjectValue();
		}
		catch (CacheException e) {
			if (log.isWarnEnabled()) {
				log.warn("No challenge found in cache for client: " + remoteAddress);
			}
			
			return null;
		}
	}
	
	@Override
	public void destroy() {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Removing cache");
			}
			
			CacheManager singletonManager = CacheManager.create();
			singletonManager.removeCache(CACHE_NAME);
			singletonManager.shutdown();
		}
		catch (Exception e) {
			// ignore
		}
	}
	
}