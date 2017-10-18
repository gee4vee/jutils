/**
 * 
 */
package com.valencia.jutils.app;

/**
 * Useful numerical constants and conversion methods.
 * 
 * @author Gabriel Valencia, <gee4vee@me.com>
 */
public class NumberConstants {
    
    /**
     * Number of bytes in a kilobyte.
     */
    public static final long KB_BYTES = 1024;

    /**
     * Number of bytes in a megabyte.
     */
    public static final long MB_BYTES = 1024 * 1024;
    
    /**
     * Number of bytes in a gigabyte.
     */
    public static final long GB_BYTES = MB_BYTES * 1024;
    
    /**
     * Number of bytes in a terabyte.
     */
    public static final long TB_BYTES = GB_BYTES * 1024;
    
    /**
     * Number of bytes in a petabyte.
     */
    public static final long PB_BYTES = TB_BYTES * 1024;
    
    /**
     * Number of bytes in an exabyte.
     */
    public static final long EB_BYTES = PB_BYTES * 1024;
    
    public static float toKB(long bytes) {
        return ((float)bytes)/KB_BYTES;
    }
    
    public static float toMB(long bytes) {
        return ((float)bytes)/MB_BYTES;
    }
    
    public static float toGB(long bytes) {
        return ((float)bytes)/GB_BYTES;
    }
    
    public static float toTB(long bytes) {
        return ((float)bytes)/TB_BYTES;
    }
    
    public static float toPB(long bytes) {
        return ((float)bytes)/PB_BYTES;
    }
    
    public static float toEB(long bytes) {
        return ((float)bytes)/EB_BYTES;
    }
    
    /**
     * Number of milliseconds in one second.
     */
    public static final long SEC_MS = 1000L;
    
    /**
     * Number of milliseconds in one minute.
     */
    public static final long MIN_MS = SEC_MS * 60;
    
    /**
     * Number of milliseconds in one hour.
     */
    public static final long HOUR_MS = MIN_MS * 60;
    
    /**
     * Number of milliseconds in one day.
     */
    public static final long DAY_MS = HOUR_MS * 24;
    
    /**
     * Number of milliseconds in one week.
     */
    public static final long WEEK_MS = DAY_MS * 7;
    
    public static float toSeconds(long ms) {
        return ((float)ms)/SEC_MS;
    }
    
    public static float toMinutes(long ms) {
        return ((float)ms)/MIN_MS;
    }
    
    public static float toHours(long ms) {
        return ((float)ms)/HOUR_MS;
    }
    
    public static float toDays(long ms) {
        return ((float)ms)/DAY_MS;
    }
    
    public static float toWeeks(long ms) {
        return ((float)ms)/WEEK_MS;
    }
}
