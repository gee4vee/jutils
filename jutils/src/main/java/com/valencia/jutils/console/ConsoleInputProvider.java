/**
 * 
 */
package com.valencia.jutils.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.valencia.jutils.app.InputProvider;

/**
 * An input provider that receives user input from the console.
 * 
 * @author Gabriel Valencia, <gee4vee@me.com>
 */
public class ConsoleInputProvider extends InputProvider {

    public ConsoleInputProvider() {
    }

    @Override
    public String getInput(String contextInfo, String message) throws Exception {
        try {
            return this.grabInput(message + " (enter " + INPUT_CANCEL + " to cancel) ");
        } catch (IOException e) {
            logger.warn("Unable to get user input from console", e);
            return null;
        }
    }
    
    @Override
    public boolean getBooleanInput(String contextInfo, String message) throws Exception {
        message += " (y/n/cancel): ";
        try {
            String input = this.grabInput(message);
            return isTrue(input);
        } catch (IOException e) {
            logger.warn("Unable to get user input from console", e);
            return false;
        }
    }

    private String grabInput(String message) throws IOException {
        System.out.print(message);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = reader.readLine();
        return input;
    }

}
