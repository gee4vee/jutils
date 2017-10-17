/**
 * 
 */
package com.valencia.jutils.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Gabriel Valencia, <gee4vee@me.com>
 *
 */
public class StreamUtils {
    
    /**
     * Reads from the specified input stream until encountering the newline character.
     * 
     * @param inputStream The input stream from which to read.
     * 
     * @return The next line or <code>null</code> if the stream is empty. The returned string is encoded in UTF-8.
     * 
     * @throws IOException
     */
    public static String readLine(InputStream inputStream) throws IOException {
        String charsetName = "UTF-8";
        return readLine(inputStream, charsetName);
    }
    
    /**
     * Reads from the specified input stream until encountering the newline character.
     * 
     * @param inputStream The input stream from which to read.
     * @param charsetName The name of the charset to use when encoding the returned string.
     * 
     * @return The next line or <code>null</code> if the stream is empty. The returned string is encoded with the specified charset.
     * 
     * @throws IOException
     */
    public static String readLine(InputStream inputStream, String charsetName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int r;

        for (r = inputStream.read(); r != '\n' && r != -1; r = inputStream.read()) {
            baos.write(r);
        }

        if (r == -1 && baos.size() == 0) {
            return null;
        }

        String lines = baos.toString(charsetName);
        return lines;
    }

}
