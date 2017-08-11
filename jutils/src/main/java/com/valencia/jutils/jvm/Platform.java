package com.valencia.jutils.jvm;
/**
 * 
 */

/**
 * Used for introspecting the current OS platform.
 * 
 * @author gee4vee@me.com
 */
public enum Platform {
	
	WINDOWS, LINUX, MAC_OS, AIX,
	;
	
	public static Platform get() {
		String os = System.getProperty("os.name");
		
		if (os.toLowerCase().contains("win")) {
			return Platform.WINDOWS;
		}
		
		if (os.equals("Mac OS X")) {
			return Platform.MAC_OS;
		}
		
		return Platform.LINUX;
	}

}
