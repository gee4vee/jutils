/**
 * 
 */
package com.valencia.jutils.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import com.valencia.jutils.string.RandomString;

/**
 * A wrapper around an input stream to allow for repeated reads from the beginning. The data from the wrapped stream is saved in temporary 
 * storage.
 * 
 * @author Gabriel Valencia, gee4vee@me.com
 */
public class RepeatableInputStream extends InputStream {
    
    private final InputStream is;
    private File tempFile;
    private OutputStream tempOS;
    private InputStream tempIS;
    private boolean restarted = false;
    
    
    /**
     * Returns a new repeatable input stream for the specified stream.
     * 
     * @throws IOException 
     */
    public RepeatableInputStream(InputStream is) throws IOException {
        this.is = new BufferedInputStream(is);
        this.tempFile = File.createTempFile(RepeatableInputStream.class.getSimpleName() + "_" + new RandomString(6), ".dat");
        this.tempFile.deleteOnExit();
        this.tempOS = new BufferedOutputStream(new FileOutputStream(this.tempFile));
    }
    
    /**
     * Restarts this stream so that it can read the contents of the wrapped stream from the beginning.
     * 
     * @throws IOException
     */
    public void restart() throws IOException {
        int wrappedData = this.is.read();
        while (wrappedData != -1) {
            // the wrapped stream still has data. consume it all into our temp storage.
            this.tempOS.write(wrappedData);
            wrappedData = this.is.read();
        }
        this.tempOS.close();
        
        if (this.tempIS != null) {
            this.tempIS.close();
        }
        this.tempIS = new BufferedInputStream(new FileInputStream(this.tempFile));
        this.restarted = true;
    }

    @Override
    public int read() throws IOException {
        if (this.restarted) {
            return this.tempIS.read();
        }
        
        // save the wrapped stream data to our temp storage until we consume the stream.
        int wrappedData = this.is.read();
        if (wrappedData != -1) {
            this.tempOS.write(wrappedData);
        } else {
            this.tempOS.close();
        }
        return wrappedData;
    }
    
    @Override
    public void close() throws IOException {
        this.is.close();
        this.tempIS.close();
        this.tempOS.close();
    }
    
    public static void main(String[] args) throws Exception {
        File tempFile = File.createTempFile("TestFile", ".dat");
        tempFile.deleteOnExit();
        byte[] chunk = new byte[1024*20];
        Random rand = new Random();
        rand.nextBytes(chunk);
        
        ByteArrayInputStream baos = new ByteArrayInputStream(chunk);
        RepeatableInputStream ris = new RepeatableInputStream(baos);
        try {
            for (int j = 0; j < 10; j++) {
                byte[] chunk2 = new byte[chunk.length];
                int numRead = ris.read(chunk2);
                if (numRead < chunk.length) {
                    throw new Exception("unexpected num read: " + numRead);
                }
                for (int i = 0; i < chunk.length; i++) {
                    if (chunk[i] != chunk2[i]) {
                        throw new Exception("Unexpected reread bytes");
                    }
                }
                ris.restart();
            }
        } finally {
            ris.close();
        }
    }

}
