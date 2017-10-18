package com.valencia.jutils.test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Utilities for writing test code.
 * 
 * @author Gabriel Valencia, <gee4vee@me.com>
 */
public class TestUtils {
    
    public static Random rand = new Random();
    
    /**
     * Used to respond to various states of a JUnit test. This field must be copied to a test class that will be executed (or one of the 
     * parent classes in its class hierarchy) in order for it to be executed by JUnit. The various methods can be modified as needed to 
     * react to the different states of a test.
     */
    @Rule
    public TestRule junitTestHandler = new TestWatcher() {

        @Override
        protected void starting(Description description) {
            System.out.println("Starting test: " + description.getMethodName());
            super.starting(description);
        }

        @Override
        protected void failed(Throwable e, Description description) {
            System.out.println("FAILED test: " + description.getMethodName() + " with error: " + e.getMessage());
            super.failed(e, description);
        }

        @Override
        protected void succeeded(Description description) {
            System.out.println("SUCCEEDED test: " + description.getMethodName());
            super.succeeded(description);
        }

        @Override
        protected void finished(Description description) {
            System.out.println("Stopping test: " + description.getMethodName());
            super.finished(description);
        }
    };
    
    /**
     * Writes random data to the specified file. The buffer size used is geared towards writing a large amount of test data.
     * 
     * @param path The file where data will be written.
     * @param length The maximum amount of data to write.
     * 
     * @throws IOException
     */
    public static void writeRandomTestData(File path, long length) throws IOException {
        int chunkSize = 1024 * 128;
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path))) {
            long totalWritten = 0;
            Random rand = new Random();
            while (totalWritten < length) {
                byte[] chunk;
                if (length < chunkSize) {
                    chunk = new byte[(int) length];
                } else {
                    chunk = new byte[chunkSize];
                }
                rand.nextBytes(chunk);
                bos.write(chunk);
                totalWritten += chunk.length;
            }
        }
    }

}
