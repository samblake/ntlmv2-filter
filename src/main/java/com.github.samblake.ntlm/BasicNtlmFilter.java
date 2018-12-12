package com.github.samblake.ntlm;

import com.github.samblake.ntlm.cache.Cache2kProvider;
import com.github.samblake.ntlm.cache.CacheProvider;
import com.github.samblake.ntlm.config.ConfigProvider;
import com.github.samblake.ntlm.config.FilterConfigProvider;

import javax.servlet.FilterConfig;

/**
 * A concrete NTLM authentication filter that uses Cache 2K and the filter config.
 */
public class BasicNtlmFilter extends AbstractNtlmFilter {
	
	@Override
	protected CacheProvider createCacheProvider(FilterConfig filterConfig) {
		return new Cache2kProvider();
	}
	
	@Override
	protected ConfigProvider createConfigProvider(FilterConfig filterConfig) {
		return new FilterConfigProvider(filterConfig);
	}
	
}