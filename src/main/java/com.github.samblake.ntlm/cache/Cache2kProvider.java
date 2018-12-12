package com.github.samblake.ntlm.cache;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import javax.servlet.FilterConfig;

import static java.lang.Integer.parseInt;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Provides caching via <a href="https://cache2k.org/">cache2k</a>.
 */
public class Cache2kProvider implements CacheProvider {
	
	// Cache2K doesn't like us storing arrays as values but it doesn't hate it either
	// See https://cache2k.org/docs/1.0/user-guide.html#value-type
	private Cache<String, Object> cache;
	
	@Override
	public void init(FilterConfig filterConfig) {
		String cacheExpiryParam = filterConfig.getInitParameter("cache-expiry");
		int cacheExpiry = cacheExpiryParam == null ? 60 : parseInt(cacheExpiryParam);
		
		String cacheCapacityParam = filterConfig.getInitParameter("cache-capacity");
		int cacheCapacity = cacheCapacityParam == null ? 100 : parseInt(cacheCapacityParam);
		
		cache = new Cache2kBuilder<String, Object>() {}
				.name("ntlmChallengeCache")
				.expireAfterWrite(cacheExpiry, SECONDS)
				.entryCapacity(cacheCapacity)
				.build();
	}
	
	@Override
	public void put(String remoteAddress, byte[] serverChallenge) {
		cache.put(remoteAddress, serverChallenge);
	}
	
	@Override
	public void remove(String remoteAddress) {
		cache.remove(remoteAddress);
	}
	
	@Override
	public byte[] get(String remoteAddr) {
		return (byte[])cache.get(remoteAddr);
	}
	
	@Override
	public void destroy() {
		cache.close();
	}
	
}