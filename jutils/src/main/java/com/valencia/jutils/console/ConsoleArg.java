/**
 * 
 */
package com.valencia.jutils.console;

import java.util.List;

/**
 * Represents an argument for a console application.
 * 
 * @author Gabriel Valencia, <gee4vee@me.com>
 */
public class ConsoleArg {
    
    public static final String DEFAULT_MV_DELIM = ",";
    
    public static enum InputType {
        STRING,
        NUMBER,
        BOOLEAN,
        
        /**
         * Used for arguments that don't take in any value, e.g. togglers.
         */
        NONE,
        ;
    }
    
    private final String name;
    private String description;
    private final boolean multivalued;
    private final InputType inputType;
    private final boolean isInteractive;
    private final boolean isRequired;
    private String multiValuedDelimiter = DEFAULT_MV_DELIM;
    private Object value;
    private boolean valueSet = false;

    /**
     * 
     */
    public ConsoleArg(String name, InputType type, boolean multivalued, boolean interactive, boolean required) {
        this.name = name;
        this.inputType = type;
        this.multivalued = multivalued;
        this.isInteractive = interactive;
        this.isRequired = required;
    }
    
    public ConsoleArg(String name) {
        this(name, InputType.STRING, false, false, false);
    }
    
    public ConsoleArg(String name, InputType type) {
        this(name, type, false, false, false);
    }
    
    public ConsoleArg(String name, InputType type, boolean required) {
        this(name, type, required, false, required);
    }
    
    public ConsoleArg(String name, InputType type, boolean multivaled, boolean interactive) {
        this(name, type, multivaled, interactive, false);
    }
    
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ConsoleArg setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isMultivalued() {
        return multivalued;
    }

    public InputType getInputType() {
        return inputType;
    }

    public boolean isInteractive() {
        return isInteractive;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public String getMultiValuedDelimiter() {
        return multiValuedDelimiter;
    }

    public ConsoleArg setMultiValuedDelimiter(String multiValuedDelimiter) {
        this.multiValuedDelimiter = multiValuedDelimiter;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public ConsoleArg setValue(Object value) {
        this.value = value;
        this.valueSet = true;
        return this;
    }
    
    public ConsoleArg clearValue() {
        this.value = null;
        this.valueSet = false;
        return this;
    }

    public boolean validateInput(Object input, String mvDelim) {
        if (input == null && this.isRequired) {
            return false;
        }
        
        boolean result;
        if (this.multivalued) {
            if (input instanceof List<?>) {
                result = true;
            } else if (isString(input) && input.toString().contains(mvDelim)) {
                result = true;
            } else {
                result = false;
            }
        }
        
        if (this.multivalued && isString(input) && input.toString().contains(mvDelim)) {
            input = input.toString().split(mvDelim)[0]; // get the first in the list to check data type.
        }
        switch (this.inputType) {
        case STRING:
            if (isString(input)) {
                result = true;
            } else {
                result = false;
            }
            break;
            
        case NUMBER:
            if (isNumber(input)) {
                result = true;
            } else {
                result = false;
            }
            break;
            
        case BOOLEAN:
            if (isBoolean(input)) {
                result = true;
            } else {
                result = false;
            }
            break;
            
        default:
            result = true;
        }
        
        return result;
    }

    public static boolean isBoolean(Object input) {
        return (input instanceof Boolean) || 
                (isString(input) && ((Boolean.FALSE.toString().equalsIgnoreCase(input.toString()))
                                                || (Boolean.TRUE.toString().equalsIgnoreCase(input.toString())))) ||
                (isString(input) && ("yes".equalsIgnoreCase(input.toString()) || "no".equalsIgnoreCase(input.toString())));
    }

    public static boolean isString(Object input) {
        return input instanceof String;
    }

    public static boolean isNumber(Object input) {
        if (isString(input)) {
            try {
                Long.parseLong(input.toString());
                return true;
            } catch (NumberFormatException e) {
                try {
                    Double.parseDouble(input.toString());
                    return true;
                } catch (NumberFormatException e2) {
                    return false;
                }
            }
        }
        
        return (input instanceof Integer) || (input instanceof Long) || (input instanceof Float) 
                || (input instanceof Short) || (input instanceof Double);
    }

}
