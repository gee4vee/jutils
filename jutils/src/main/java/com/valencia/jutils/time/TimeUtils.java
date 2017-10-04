/**
 * 
 */
package com.valencia.jutils.time;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Time-related utilities.
 * 
 * @author Gabriel Valencia, gee4vee@me.com
 */
public class TimeUtils {

    /**
     * Converts the specified unit to a matching <code>TimeUnit</code> if possible. Only those supported by <code>TimeUnit</code> can be 
     * converted. All others will return <code>null</code>.
     * 
     * @param tu The unit to convert.
     */
    public static TimeUnit toTimeUnit(ChronoUnit tu) {
        switch (tu) {
        case DAYS:
            return TimeUnit.DAYS;
            
        case HOURS:
            return TimeUnit.HOURS;
            
        case MICROS:
            return TimeUnit.MICROSECONDS;
            
        case MILLIS:
            return TimeUnit.MILLISECONDS;
            
        case MINUTES:
            return TimeUnit.MINUTES;
            
        case NANOS:
            return TimeUnit.NANOSECONDS;
            
        case SECONDS:
            return TimeUnit.SECONDS;
            
        default:
            return null;
        }
    }

}
