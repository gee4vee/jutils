/**
 * 
 */
package com.valencia.jutils.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.valencia.jutils.console.ConsoleArg.InputType;

/**
 * @author Gabriel Valencia, <gee4vee@me.com>
 *
 */
public class ConsoleApp {
    
    public static final String EQUAL = "=";

    public static final String SINGLE_SPACE = " ";

    public static final String ARG_PREFIX = "--";
    
    public static final String NEWLINE = System.lineSeparator();
    
    public static final String INPUT_EXIT = "exit";
    
    public static final String INPUT_HELP = "?";
    
    public static enum ArgType {
        /**
         * Example: --host hostName
         */
        SPACE_SEPARATED,
        
        /**
         * Example: host=hostName
         */
        KEY_VALUE_PAIR,
        ;
    }
    
    private final String name;
    private final String version;
    private final List<ConsoleArg> args = new ArrayList<>();
    private final ArgType argType;
    private String description;

    /**
     * 
     */
    public ConsoleApp(String name, String version, ArgType type) {
        this.name = name.trim();
        this.version = version.trim();
        this.argType = type;
    }
    
    public ConsoleApp(String name, String version) {
        this(name, version, ArgType.KEY_VALUE_PAIR);
    }
    
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ConsoleApp setDescription(String description) {
        this.description = description.trim();
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ArgType getArgType() {
        return argType;
    }

    public List<ConsoleArg> getArgs() {
        return Collections.unmodifiableList(this.args);
    }
    
    public ConsoleApp addArg(ConsoleArg arg) {
        this.args.add(arg);
        return this;
    }
    
    public ConsoleApp addArg(String name, InputType inputType, String description) {
        ConsoleArg arg = new ConsoleArg(name, inputType);
        arg.setDescription(description);
        this.args.add(arg);
        return this;
    }
    
    public ConsoleApp addArg(String name, InputType inputType, String description, boolean isRequired) {
        ConsoleArg arg = new ConsoleArg(name, inputType, isRequired);
        arg.setDescription(description);
        this.args.add(arg);
        return this;
    }
    
    public ConsoleApp addArg(String name, InputType inputType, String description, boolean isMultivalued, boolean isInteractive) {
        ConsoleArg arg = new ConsoleArg(name, inputType, isMultivalued, isInteractive);
        arg.setDescription(description);
        this.args.add(arg);
        return this;
    }
    
    public ConsoleApp addArg(String name, InputType inputType, String description, boolean isRequired, boolean isMultivalued, boolean isInteractive) {
        ConsoleArg arg = new ConsoleArg(name, inputType, isMultivalued, isInteractive, isRequired);
        arg.setDescription(description);
        this.args.add(arg);
        return this;
    }
    
    public List<ConsoleArg> getRequiredArgs() {
        List<ConsoleArg> req = new ArrayList<>();
        for (ConsoleArg arg : this.args) {
            if (arg.isRequired()) {
                req.add(arg);
            }
        }
        
        return Collections.unmodifiableList(req);
    }
    
    public Set<String> getRequiredArgNames() {
        Set<String> req = new HashSet<>();
        for (ConsoleArg arg : this.args) {
            if (arg.isRequired()) {
                req.add(arg.getName());
            }
        }
        
        return Collections.unmodifiableSet(req);
    }
    
    public List<ConsoleArg> getInteractiveArgs() {
        List<ConsoleArg> req = new ArrayList<>();
        for (ConsoleArg arg : this.args) {
            if (arg.isInteractive()) {
                req.add(arg);
            }
        }
        
        return Collections.unmodifiableList(req);
    }
    
    public ConsoleArg getArg(String name) {
        for (ConsoleArg arg : this.args) {
            if (arg.getName().equals(name)) {
                return arg;
            }
        }
        
        return null;
    }
    
    public String getUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name).append(SINGLE_SPACE).append("v").append(this.version);
        if (this.description != null && !this.description.trim().isEmpty()) {
            sb.append(SINGLE_SPACE).append(" - ").append(this.description);
        }
        sb.append(NEWLINE);
        
        sb.append("Available options:").append(NEWLINE);
        for (ConsoleArg arg : this.args) {
            if (arg.isInteractive()) {
                continue;
            }
            sb.append("\t");
            if (this.argType.equals(ArgType.KEY_VALUE_PAIR)) {
                sb.append(arg.getName());
            } else {
                sb.append(ARG_PREFIX).append(arg.getName()).append(SINGLE_SPACE);
            }
            InputType inputType = arg.getInputType();
            if (!InputType.NONE.equals(inputType)) {
                sb.append(EQUAL).append(inputType);
                if (arg.isMultivalued()) {
                    String mvDelim = arg.getMultiValuedDelimiter();
                    sb.append(mvDelim).append(inputType).append(mvDelim).append("...");
                }
            }
            
            if (arg.isRequired()) {
                sb.append(" (required): ");
            } else {
                sb.append(" (optional): ");
            }
            sb.append(arg.getDescription()).append(NEWLINE);
        }
        
        return sb.toString();
    }
    
    public boolean expectsArg(String name) {
        for (ConsoleArg arg : args) {
            if (arg.getName().equals(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    public void printUsage(PrintStream s) {
        s.println(this.getUsage());
    }
    
    public void printUsage(PrintWriter w) {
        w.println(this.getUsage());
    }
    
    public void printUsage() {
        this.printUsage(System.out);
    }
    
    /**
     * Validates the specified input arguments against the arguments specified in this app. If any input is invalid, an exception 
     * will be thrown. If all inputs are valid, the input values will be set on the corresponding <code>ConsoleArg</code>. The 
     * input values can then be retrieved with one of the <code>getArg*Value</code> methods.
     * 
     * @param args The input argument values.
     * 
     * @throws Exception If any of the input values is invalid.
     */
    public void validateArgs(String... args) throws Exception {
        Map<String, String> input = new HashMap<>();
        if (this.argType.equals(ArgType.KEY_VALUE_PAIR)) {
            for (String arg : args) {
            	if (INPUT_HELP.equalsIgnoreCase(arg)) {
            		System.out.println(getUsage());
            		continue;
            	}
            	
                if (!arg.contains(EQUAL)) {
                    throw new Exception("Argument '" + arg + "' must be in key-value form.");
                }
                
                String[] split = arg.split(EQUAL);
                String argName = split[0];
                if (!this.expectsArg(argName)) {
                    throw new Exception("Unexpected argument: " + argName);
                }
                String argValue;
                if (split.length == 1) {
                    argValue = "";
                    input.put(argName, argValue);
                } else {
                    argValue = split[1];
                    input.put(argName, argValue);
                }
                
                ConsoleArg appArg = this.getArg(argName);
                if (appArg == null) {
                    throw new Exception("Unexpected argument: " + argName);
                }
                boolean result = appArg.validateInput(argValue, appArg.getMultiValuedDelimiter());
                if (!result) {
                    throw new Exception("Value for arg '" + argName + "' is incorrect: " + argValue);
                }
            }
            
        } else {
            for (int i = 0; i < args.length; i+=2) {
                String argName = args[i];
            	if (INPUT_HELP.equalsIgnoreCase(argName)) {
            		System.out.println(getUsage());
            		continue;
            	}
            	
                if (!this.expectsArg(argName)) {
                    throw new Exception("Unexpected argument: " + argName);
                }
                
                ConsoleArg appArg = this.getArg(argName);
                if (appArg == null) {
                    throw new Exception("Unexpected argument: " + argName);
                }
                String argValue;
                if (i+1 < args.length) {
                    argValue = args[i+1];
                } else {
                    if (appArg.isRequired()) {
                        throw new Exception("Argument '" + argName + "' is required but was not specified.");
                    } else {
                        argValue = "";
                    }
                }
                input.put(argName, argValue);
                boolean result = appArg.validateInput(argValue, appArg.getMultiValuedDelimiter());
                if (!result) {
                    throw new Exception("Value for arg '" + argName + "' is incorrect: " + argValue);
                }
            }
        }
        
        Set<String> requiredArgNames = this.getRequiredArgNames();
        if (!input.keySet().containsAll(requiredArgNames)) {
            throw new Exception("At least one required argument was not specified. Required args: " + requiredArgNames);
        }
        
        for (ConsoleArg arg : this.args) {
            String argName = arg.getName();
            if (input.containsKey(argName)) {
                String value = input.get(argName);
                arg.setValue(value);
            }
        }
    }
    
    /**
     * Returns the value that was set for the argument with the specified name, or <code>null</code> if the value has not been set or 
     * an argument with the specified name cannot be found.
     *  
     * @param argName The name of the argument.
     * 
     * @return the value that was set for the argument with the specified name, or <code>null</code> if the value has not been set or 
     * an argument with the specified name cannot be found.
     */
    public Object getArgValue(String argName) {
        return this.getArgValue(argName, null);
    }
    
    public Object getArgValue(String argName, String defaultValue) {
        ConsoleArg arg = this.getArg(argName);
        if (arg != null && arg.isValueSet()) {
           return arg.getValue(); 
        }
        
        return defaultValue;
    }
    
    /**
     * Returns the value that was set for the argument with the specified name as an <code>Integer</code>.
     * 
     * @param argName The name of the argument.
     * @param defaultValue A default value to return if the specified argument does not have a value set.
     * 
     * @return the value that was set for the argument with the specified name, or <code>null</code> if the value has not been set or 
     * an argument with the specified name cannot be found.
     * 
     * @throws IllegalArgumentException If the value for the specified argument cannot be retrieved as an <code>Integer</code>.
     */
    public Integer getArgIntValue(String argName, Integer defaultValue) throws IllegalArgumentException {
        Object value = this.getArgValue(argName);
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Integer) {
            return ((Integer)value);
        }
        
        if (value instanceof String) {
            return Integer.parseInt(value.toString());
        }
        
        throw new IllegalArgumentException("Cannot get integer value for argument " + argName);
    }
    
    /**
     * Returns the value that was set for the argument with the specified name as a <code>Long</code>.
     * 
     * @param argName The name of the argument.
     * @param defaultValue A default value to return if the specified argument does not have a value set.
     * 
     * @return the value that was set for the argument with the specified name, or <code>null</code> if the value has not been set or 
     * an argument with the specified name cannot be found.
     * 
     * @throws IllegalArgumentException If the value for the specified argument cannot be retrieved as a <code>Long</code>.
     */
    public Long getArgLongValue(String argName, Long defaultValue) throws IllegalArgumentException {
        Object value = this.getArgValue(argName);
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Long || value instanceof Integer) {
            return ((Long)value);
        }
        
        if (value instanceof String) {
            return Long.parseLong(value.toString());
        }
        
        throw new IllegalArgumentException("Cannot get long value for argument " + argName);
    }
    
    /**
     * Returns the value that was set for the argument with the specified name as a <code>Double</code>.
     * 
     * @param argName The name of the argument.
     * @param defaultValue A default value to return if the specified argument does not have a value set.
     * 
     * @return the value that was set for the argument with the specified name, or <code>null</code> if the value has not been set or 
     * an argument with the specified name cannot be found.
     * 
     * @throws IllegalArgumentException If the value for the specified argument cannot be retrieved as a <code>Double</code>.
     */
    public Double getArgDoubleValue(String argName, Double defaultValue) throws IllegalArgumentException {
        Object value = this.getArgValue(argName);
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Double) {
            return ((Double)value);
        }
        
        if (value instanceof String) {
            return Double.parseDouble(value.toString());
        }
        
        throw new IllegalArgumentException("Cannot get double value for argument " + argName);
    }
    
    /**
     * Returns the value that was set for the argument with the specified name as a <code>Boolean</code>.
     * 
     * @param argName The name of the argument.
     * @param defaultValue A default value to return if the specified argument does not have a value set.
     * 
     * @return the value that was set for the argument with the specified name, or <code>null</code> if the value has not been set or 
     * an argument with the specified name cannot be found.
     * 
     * @throws IllegalArgumentException If the value for the specified argument cannot be retrieved as a <code>Boolean</code>.
     */
    public Boolean getArgBooleanValue(String argName, Boolean defaultValue) throws IllegalArgumentException {
        Object value = this.getArgValue(argName);
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Boolean) {
            return ((Boolean)value);
        }
        
        if (value instanceof String) {
            return Boolean.parseBoolean(value.toString());
        }
        
        throw new IllegalArgumentException("Cannot get boolean value for argument " + argName);
    }
    
    /**
     * Clears the value that was set for all arguments in this app.
     */
    public void clearArgValues() {
        for (ConsoleArg arg : this.args) {
            arg.clearValue();
        }
    }
    
    /**
     * Used in a <code>Map</code> passed to {@link #startInteraction(Map)} to specify an input handler that will be used 
     * for all possible inputs.
     */
    public static final String KEY_INTERACTION_CALLBACKS_ALL = "InteractionCallbacksAll";
    
    public static Map<String, Function<String, String>> getSingleInputHandlerMap(Function<String, String> handler) {
    	Map<String, Function<String, String>> map = new HashMap<>();
    	map.put(KEY_INTERACTION_CALLBACKS_ALL, handler);
    	return map;
    }
    
    /**
     * Starts the interactive console loop.
     * 
     * @param inputHandlers A map of input handler functions that will be used to handle the various inputs chosen by the user. 
     * The key is one of the possible inputs arguments. The value is a function that will take the input and produce a 
     * String result. Use the key {@link #KEY_INTERACTION_CALLBACKS_ALL} to specify a single input handler function for all 
     * possible inputs. If this key is used, any other entries in the map are ignored.
     * 
     * @throws Exception
     */
    public <I, O> void startInteraction(Map<String, Function<String, String>> inputHandlers) throws Exception {
        System.out.println("Welcome to " + this.getName() + " " + this.getVersion());
        String input = "";
        boolean firstTime = true;
        while (true) {
            this.printAvailableInteractiveOptions(firstTime);
            firstTime = false;
            try {
                input = this.getInput();
            } catch (IOException e) {
                throw new Exception("An error occurred while getting input", e);
            }
            
            if (INPUT_HELP.equalsIgnoreCase(input)) {
            	firstTime = true;
            	continue;
            }
            
            if (INPUT_EXIT.equalsIgnoreCase(input)) {
                System.out.println("Goodbye!");
                break;
            }
            
            String argName;
            if (input.contains(EQUAL)) {
                String[] split = input.split(EQUAL);
                argName = split[0];
            } else {
                argName = input;
            }
            ConsoleArg arg = this.getArg(argName);
            if (arg == null) {
                System.out.println("\tUnexpected input " + argName);
                continue;
            }
            
            if (!InputType.NONE.equals(arg.getInputType()) && this.argType.equals(ArgType.KEY_VALUE_PAIR) && !input.contains(EQUAL)) {
                System.out.println("\tInput is expected in key-value pair form, e.g. ArgName=ArgValue.");
                continue;
            }
            
            if (inputHandlers == null) {
            	System.out.println();
            	continue;
            }
            
            Function<String, String> callback;
            if (inputHandlers.containsKey(KEY_INTERACTION_CALLBACKS_ALL)) {
            	callback = inputHandlers.get(KEY_INTERACTION_CALLBACKS_ALL);
            } else {
            	String key;
            	if (input.contains(EQUAL)) {
            		key = input.split(EQUAL)[0];
            	} else {
            		key = input;
            	}
            	callback = inputHandlers.get(key);
            }
            
            if (callback == null) {
            	System.out.println("No input handler for " + input);
            } else {
                String output = callback.apply(input);
                System.out.println(">> Output: " + output);
            }
            System.out.println();
        }
    }
    
    private String getInput() throws IOException {
        System.out.print(">> ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = reader.readLine();
        return input;
    }

    private void printAvailableInteractiveOptions(boolean printAllOptions) {
        System.out.println("Please enter one of the available options, " 
        					+ INPUT_HELP + " to display the available options, or " 
    						+ INPUT_EXIT + " to exit immediately:");
        if (printAllOptions) {
            List<ConsoleArg> interactiveArgs = this.getInteractiveArgs();
            for (ConsoleArg arg : interactiveArgs) {
                StringBuilder sb = new StringBuilder();
                sb.append("\t").append(arg.getName());
                InputType inputType = arg.getInputType();
                if (!InputType.NONE.equals(inputType)) {
                    if (this.argType.equals(ArgType.KEY_VALUE_PAIR)) {
                        sb.append(EQUAL);
                    } else {
                        sb.append(SINGLE_SPACE);
                    }
                    sb.append(arg.getInputType());
                    if (arg.isMultivalued()) {
                        String mvDelim = arg.getMultiValuedDelimiter();
                        sb.append(mvDelim).append(inputType).append(mvDelim).append("...");
                    }
                }
                sb.append(" - ").append(arg.getDescription());
                System.out.println(sb.toString());
            }
        }
    }
    
}
