/**
 * 
 */
package com.valencia.jutils.console;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.valencia.jutils.console.ConsoleArg.InputType;

/**
 * @author Gabriel Valencia, <gee4vee@me.com>
 *
 */
public class ConsoleApp {
    
    public static final String SINGLE_SPACE = " ";

    public static final String ARG_PREFIX = "--";
    
    public static final String NEWLINE = System.lineSeparator();
    
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
                sb.append(arg.getName()).append("=");
            } else {
                sb.append(ARG_PREFIX).append(arg.getName()).append(SINGLE_SPACE);
            }
            InputType inputType = arg.getInputType();
            sb.append(inputType);
            if (arg.isMultivalued()) {
                String mvDelim = arg.getMultiValuedDelimiter();
                sb.append(mvDelim).append(inputType).append(mvDelim).append("...");
            }
            
            if (arg.isRequired()) {
                sb.append(" (required)");
            } else {
                sb.append(" (optional)");
            }
            sb.append(NEWLINE);
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
                if (!arg.contains("=")) {
                    throw new Exception("Arguments must be in key-value form.");
                }
                
                String[] split = arg.split("=");
                String argName = split[0];
                if (!this.expectsArg(argName)) {
                    throw new Exception("Unexpected argument: " + argName);
                }
                String argValue = split[1];
                input.put(argName, argValue);
                
                ConsoleArg appArg = this.getArg(argName);
                if (appArg == null) {
                    throw new Exception("Unexpected argument: " + argName);
                }
                appArg.validateInput(argValue, appArg.getMultiValuedDelimiter());
            }
            
        } else {
            
        }
        
        Set<String> requiredArgNames = this.getRequiredArgNames();
        if (!input.keySet().containsAll(requiredArgNames)) {
            throw new Exception("At least one required argument was not specified. Required args: " + requiredArgNames);
        }
    }
    
}
