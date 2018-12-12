package com.github.samblake.ntlm;

/**
 * An exception that is thrown if the NTLM filter cannot be initialised.
 */
public class InitialisationException extends Exception {
	
	public InitialisationException(String message) {
		super(message);
	}
	
	public InitialisationException(String message, Exception exception) {
		super(message, exception);
	}
	
}