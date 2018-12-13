# NTLMv2 Filter

A J2EE filter that provides NTLMv2 authentication. This can be used for implementing Active Directory based auto login 
functionality. It's based on (and uses) the [NTLMv2 Authentication](http://ntlmv2auth.sourceforge.net/) library which 
in turn is based on the [Liferay](https://www.liferay.com/) implementation.

The filter has been modified to allow different caching and configuration implementations as well as allow selective 
filtering.

## Providers

The caching implementations provided are:

 * Ehcache
 * Cache2k
 
The configuration implementations provided are:

 * Filter parameters
 * System/Environmental properties
 
However, it's very simple to implement your own.

## Lisence

This code is distributed under the LGPL license.
