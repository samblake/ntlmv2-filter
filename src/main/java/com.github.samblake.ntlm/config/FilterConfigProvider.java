package com.github.samblake.ntlm.config;

import com.github.samblake.ntlm.InitialisationException;

import javax.servlet.FilterConfig;

/**
 * Obtains configuration details from the
 * <a href="https://www.oracle.com/technetwork/java/filters-137243.html#70248">filter configuration</a>.
 */
public class FilterConfigProvider implements ConfigProvider {
	private final FilterConfig filterConfig;
	
	public FilterConfigProvider(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
	}
	
	@Override
	public boolean isEnabled() {
		String value = filterConfig.getInitParameter("ntlm-enabled");
		return Boolean.parseBoolean(value);
	}
	
	@Override
	public String getDomain() {
		return filterConfig.getInitParameter("ntlm-domain");
	}
	
	@Override
	public String getDomainControllerIp() throws InitialisationException {
		return getParameter("ntlm-dc-ip");
	}
	
	@Override
	public String getDomainControllerHostname() throws InitialisationException {
		return getParameter("ntlm-dc-hostname");
	}
	
	@Override
	public String getServiceAccount() throws InitialisationException {
		return getParameter("ntlm-account");
	}
	
	@Override
	public String getServicePassword() throws InitialisationException {
		return getParameter("ntlm-password");
	}
	
	private String getParameter(String property) throws InitialisationException {
		String value = filterConfig.getInitParameter(property);
		
		if (value == null || value.trim().isEmpty()) {
			throw new InitialisationException("No value set for " + property);
		}
		
		return value;
	}
	
}