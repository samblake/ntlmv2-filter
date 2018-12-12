package com.github.samblake.ntlm.cache;

import com.github.samblake.ntlm.InitialisationException;

import javax.servlet.FilterConfig;

/**
 * Provides caching of the server Challenge.
 */
public interface CacheProvider {
	
	/**
	 * Initialises the cache.
	 *
	 * @param filterConfig The filter config
	 * @throws InitialisationException Thrown if the cache cannot be initialised
	 */
	void init(FilterConfig filterConfig) throws InitialisationException;
	
	/**
	 * Stored the server challenge in the cache.
	 *
	 * @param remoteAddress The remote IP address of the server challenge was created for
	 * @param serverChallenge The server challenge to store
	 */
	void put(String remoteAddress, byte[] serverChallenge);
	
	/**
	 * Removes the server challenge from the cache.
	 *
	 * @param remoteAddress The remote IP address to remove the server challenge of
	 */
	void remove(String remoteAddress);
	
	/**
	 * Gets the server challenge associated with the remote address.
	 *
	 * @param remoteAddress The remote IP address to remove the server challenge for
	 * @return The server challenge or null if there is no value stored in the cache
	 */
	byte[] get(String remoteAddress);
	
	/**
	 * Destroys the cache and frees up associated resources.
	 */
	void destroy();
	
}