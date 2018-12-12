package com.github.samblake.ntlm.config;

import com.github.samblake.ntlm.InitialisationException;

/**
 * Obtains details of the Active Directory configuration. The service account must be a
 * <a href="https://msdn.microsoft.com/en-us/library/cc246064.aspx">computer account</a>.
 */
public interface ConfigProvider {
	
	/**
	 * The Active Directory domain. For example domain.local.
	 *
	 * @return The Active Directory domain
	 * @throws InitialisationException Thrown if no value has been set
	 */
	String getDomain() throws InitialisationException;
	
	/**
	 * The domain controller IP address. For example 10.10.0.1.
	 *
	 * @return The domain controller IP address
	 * @throws InitialisationException Thrown if no value has been set
	 */
	String getDomainControllerIp() throws InitialisationException;
	
	/**
	 * The simple (non-FQDN) hostname of the domain controller. For example adc01.
	 *
	 * @return The simple (non-FQDN) hostname of the domain controller
	 * @throws InitialisationException Thrown if no value has been set
	 */
	String getDomainControllerHostname() throws InitialisationException;
	
	/**
	 * The service account name for connection to the domain controller. For example COMPUTER@domain.com.
	 *
	 * @return The service account name for connection to the domain controller
	 * @throws InitialisationException Thrown if no value has been set
	 */
	String getServiceAccount() throws InitialisationException;
	
	/**
	 * Service account password.
	 *
	 * @return Service account password
	 * @throws InitialisationException Thrown if no value has been set
	 */
	String getServicePassword() throws InitialisationException;
	
}