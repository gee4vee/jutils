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
    
    public void validateArgs(String... args) throws Exception {
        Map<String, String> input = new HashMap<>();
        if (this.argType.equals(ArgType.KEY_VALUE_PAIR)) {
            for (String arg : args) {
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
    }
    
    public <I, O> void startInteraction(Function<String, String> callback) throws Exception {
        System.out.println("Welcome to " + this.getName());
        String input = "";
        while (true) {
            this.printAvailableInteractiveOptions();
            try {
                input = this.getInput();
            } catch (IOException e) {
                throw new Exception("An error occurred while getting input", e);
            }
            
            if (!INPUT_EXIT.equalsIgnoreCase(input)) {
                System.out.println("Exiting...");
                break;
            }
            
            if (this.argType.equals(ArgType.KEY_VALUE_PAIR) && !input.contains(EQUAL)) {
                System.out.println("\tInput is expected in key-value pair form, e.g. ArgName=ArgValue.");
                continue;
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
                System.out.println("\tUnexpected argument " + argName);
                continue;
            }
            
            String output = callback.apply(input);
            System.out.println(">> Output: " + output);
        }
    }
    
    private String getInput() throws IOException {
        System.out.println(">> ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = reader.readLine();
        return input;
    }

    private void printAvailableInteractiveOptions() {
        System.out.println("Please enter one of the available options:");
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
            }
            System.out.println(sb.toString());
        }
    }
    
}
