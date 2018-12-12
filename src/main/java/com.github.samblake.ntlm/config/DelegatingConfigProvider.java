package com.github.samblake.ntlm.config;

import com.github.samblake.ntlm.InitialisationException;

/**
 * An abstract class that delegate all it's requests to another config provider. On it's own it does
 * nothing useful but it can be used as a building block for other implementations.
 */
public abstract class DelegatingConfigProvider implements ConfigProvider {
	
	private ConfigProvider configProvider;
	
	public DelegatingConfigProvider(ConfigProvider configProvider) {
		this.configProvider = configProvider;
	}
	
	@Override
	public String getDomain() throws InitialisationException {
		return configProvider.getDomain();
	}
	
	@Override
	public String getDomainControllerIp() throws InitialisationException {
		return configProvider.getDomainControllerIp();
	}
	
	@Override
	public String getDomainControllerHostname() throws InitialisationException {
		return configProvider.getDomainControllerHostname();
	}
	
	@Override
	public String getServiceAccount() throws InitialisationException {
		return configProvider.getServiceAccount();
	}
	
	@Override
	public String getServicePassword() throws InitialisationException {
		return configProvider.getServicePassword();
	}
	
}