package com.github.samblake.ntlm.config;

import com.github.samblake.ntlm.InitialisationException;

/**
 * Obtains configuration details from the
 * <a href="https://www.oracle.com/technetwork/java/filters-137243.html#70248">filter configuration</a>.
 */
public class SystemConfigProvider implements ConfigProvider {
	
	@Override
	public String getDomain() throws InitialisationException {
		return getProperty("ntlm.domain");
	}
	
	@Override
	public String getDomainControllerIp() throws InitialisationException {
		return getProperty("ntlm.dc.ip");
	}
	
	@Override
	public String getDomainControllerHostname() throws InitialisationException {
		return getProperty("ntlm.dc.hostname");
	}
	
	@Override
	public String getServiceAccount() throws InitialisationException {
		return getProperty("ntlm.account");
	}
	
	@Override
	public String getServicePassword() throws InitialisationException {
		return getProperty("ntlm.password");
	}
	
	private String getProperty(String property) throws InitialisationException {
		String value = System.getProperty(property, System.getenv(property));
		
		if (value == null || value.trim().isEmpty()) {
			throw new InitialisationException("No value set for " + property);
		}
		
		return value;
	}
	
}