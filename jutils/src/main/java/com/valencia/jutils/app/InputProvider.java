/**
 * 
 */
package com.valencia.jutils.app;

import java.io.File;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides an abstraction for retrieving user input. Useful when an application can retrieve input in various forms, e.g. console and GUI.
 * 
 * @author Gabriel Valencia, <gee4vee@me.com>
 */
public abstract class InputProvider {
    
    protected static Logger logger = LogManager.getLogger(InputProvider.class);
    
    public static final String[] VALID_TRUE_INPUT = new String[] {"true", "yes", "y"};
    
    public static final String INPUT_CANCEL = "cancel";
    public static final String INPUT_YES = "yes";
    
    protected String id;
    
    public InputProvider() {
        this.id = InputProvider.class.getName();
    }
    
    /**
     * Returns the ID of this input provider. This should be unique across all providers in a single JVM.
     */
    public String getId() {
        return this.id;
    }
    
    /**
     * Returns the next input from the user as a string. This method may block until the user provides input. For GUI-based input 
     * providers, this method can return {@link #INPUT_CANCEL} to signify that the user wishes to cancel the command invocation.
     * 
     * @param contextInfo Contextual information about the request that an input provider can use.
     * 
     * @throws Exception
     */
    public String getInput(String contextInfo) throws Exception {
        return this.getInput(null);
    }
    
    /**
     * Returns the next input from the user as a string. This method may block until the user provides input. For GUI-based input 
     * providers, this method can return {@link #INPUT_CANCEL} to signify that the user wishes to cancel the command invocation.
     * 
     * @param contextInfo Contextual information about the request that an input provider can use.
     * @param message A message to present to the user when requesting input. Can be <code>null</code>.
     * 
     * @throws Exception
     */
    public abstract String getInput(String contextInfo, String message) throws Exception;
    
    /**
     * Returns the next input from the user as a string. This method may block until the user provides input. For GUI-based input 
     * providers, this method can return {@link #INPUT_CANCEL} to signify that the user wishes to cancel the command invocation.
     * 
     * @param contextInfo Contextual information about the request that an input provider can use.
     * @param message A message to present to the user when requesting input. Can be <code>null</code>.
     * @param properties An optional set of properties that can be used to pass additional information to the provider.
     * 
     * @throws Exception
     */
    public abstract String getInput(String contextInfo, String message, Map<String, Object> properties) throws Exception;
    
    /**
     * Returns the next input from the user as a boolean. This method may block until the user provides input.
     * 
     * @param contextInfo Contextual information about the request that an input provider can use.
     * 
     * @throws Exception
     */
    public boolean getBooleanInput(String contextInfo) throws Exception {
        return this.getBooleanInput(contextInfo, null);
    }
    
    /**
     * Returns the next input from the user as a boolean. This method may block until the user provides input. The user's input 
     * is considered <b>true</b> if it matches any of the string specified in {@link #VALID_TRUE_INPUT}, ignoring case.
     * 
     * @param message A message to present to the user when requesting input.
     * 
     * @throws Exception
     */
    public boolean getBooleanInput(String contextInfo, String message) throws Exception {
        return this.getBooleanInput(contextInfo, message, null);
    }
    
    /**
     * Returns the next input from the user as a boolean. This method may block until the user provides input. The user's input 
     * is considered <b>true</b> if it matches any of the string specified in {@link #VALID_TRUE_INPUT}, ignoring case.
     * 
     * @param message A message to present to the user when requesting input.
     * @param properties An optional set of properties that can be used to pass additional information to the provider.
     * 
     * @throws Exception
     */
    public boolean getBooleanInput(String contextInfo, String message, Map<String, Object> properties) throws Exception {
        String input = this.getInput(contextInfo, message, properties);
        return isTrue(input);
    }

    public static boolean isTrue(String input) {
        for (String trueStr : VALID_TRUE_INPUT) {
            if (trueStr.equalsIgnoreCase(input)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns the next input from the user as a <code>File</code>, or <code>null</code> if the user canceled input. This method 
     * may block until the user provides input.
     * 
     * @param message A message to present to the user when requesting input.
     * 
     * @throws Exception
     */
    public File getFileInput(String contextInfo, String message) throws Exception {
        return this.getFileInput(contextInfo, message, null);
    }
    
    /**
     * Returns the next input from the user as a <code>File</code>, or <code>null</code> if the user canceled input. This method 
     * may block until the user provides input.
     * 
     * @param message A message to present to the user when requesting input.
     * @param properties An optional set of properties that can be used to pass additional information to the provider.
     * 
     * @throws Exception
     */
    public File getFileInput(String contextInfo, String message, Map<String, Object> properties) throws Exception {
        String input = this.getInput(contextInfo, message);
        if (INPUT_CANCEL.equals(input)) {
            return null;
        }
        
        return new File(input);
    }

}
