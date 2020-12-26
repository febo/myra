/*
 * Option.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2015 Fernando Esteban Barril Otero
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package myra;

import static myra.Config.CONFIG;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import myra.Config.ConfigKey;

/**
 * The <code>Option</code> class represents a command-line switch.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Option<T> {
    /**
     * The <code>ConfigKey</code> represented by this option.
     */
    protected ConfigKey<T> key;

    /**
     * The switch modifier. This usually is a single character.
     */
    protected String modifier;

    /**
     * The description of this option.
     */
    protected String description;

    /**
     * Indicates whether this options has a default value.
     */
    protected boolean hasDefault;

    /**
     * The argument name (if available).
     */
    protected String argument;

    /**
     * Mapping of option values.
     */
    private Map<String, T> alternatives = new TreeMap<String, T>();

    /**
     * Creates a new <code>Option</code> object.
     * 
     * @param key
     *            the key of the option.
     * @param modifier
     *            the modifier of the option.
     * @param description
     *            the description of the option.
     */
    public Option(ConfigKey<T> key, String modifier, String description) {
        this(key, modifier, description, true);
    }

    /**
     * Creates a new <code>Option</code> object.
     * 
     * @param key
     *            the key of the option.
     * @param modifier
     *            the modifier of the option.
     * @param description
     *            the description of the option.
     * @param hasDefault
     *            indication whether this option has a default value.
     */
    public Option(ConfigKey<T> key,
                  String modifier,
                  String description,
                  boolean hasDefault) {
        this(key, modifier, description, hasDefault, null);
    }

    /**
     * Creates a new <code>Option</code> object.
     * 
     * @param key
     *            the key of the option.
     * @param modifier
     *            the modifier of the option.
     * @param description
     *            the description of the option.
     * @param hasDefault
     *            indication whether this option has a default value.
     * @param argument
     *            the name of the option's argument.
     */
    public Option(ConfigKey<T> key,
                  String modifier,
                  String description,
                  boolean hasDefault,
                  String argument) {
        this.key = key;
        this.modifier = modifier;
        this.description = description;
        this.hasDefault = hasDefault;
        this.argument = argument;
    }

    /**
     * Adds a mapping between the specifiec option value and object.
     * 
     * @param value
     *            the option value.
     * @param object
     *            the corresponding configuration object.
     */
    public void add(String value, T object) {
        alternatives.put(value, object);
    }

    /**
     * Sets the value of the configuration represented by this option. The value
     * can be <code>null</code> for configurations that do not require a value
     * (e.g., presence/absence of the option).
     * 
     * @param value
     *            the value to set.
     */
    @SuppressWarnings("unchecked")
    public void set(String value) {
        if (alternatives.isEmpty()) {
            CONFIG.set((ConfigKey<String>) key, value);
        } else {
            CONFIG.set(key, alternatives.get(value));
        }
    }

    /**
     * Returns the value set for this option.
     * 
     * @return the value set for this option.
     */
    public String value() {
        if (hasArgument()) {
            T value = CONFIG.get(key);

            if (alternatives.isEmpty()) {
                return value.toString();
            }

            for (Map.Entry<String, T> entry : alternatives.entrySet()) {
                if (value == entry.getValue()) {
                    return entry.getKey();
                }
            }

            throw new IllegalArgumentException("Value not set for option: "
                    + modifier);
        } else {
            return new String();
        }
    }

    /**
     * Returns <code>true</code> if this option has a default value.
     * 
     * @return <code>true</code> if this option has a default value;
     *         <code>false</code> otherwise.
     */
    public boolean hasDefault() {
        return hasDefault;
    }

    /**
     * Returns <code>true</code> if this option has an argument value.
     * 
     * @return <code>true</code> if this option has an argument value;
     *         <code>false</code> otherwise.
     */
    public boolean hasArgument() {
        return (argument != null);
    }

    /**
     * Returns the <code>ConfigKey</code> object represented by this option.
     * 
     * @return the <code>ConfigKey</code> object represented by this option.
     */
    public ConfigKey<T> getKey() {
        return key;
    }

    /**
     * Returns the argument of this option.
     * 
     * @return the argument of this option.
     */
    public String getArgument() {
        return argument;
    }

    /**
     * Returns the switch modifier.
     * 
     * @return the switch modifier.
     */
    public String getModifier() {
        return modifier;
    }

    /**
     * Returns the description of this option.
     * 
     * @return the description of this option.
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer();

        if (argument == null) {
            out.append(description);
        } else {
            out.append(String.format(description, argument));
        }

        if (!alternatives.isEmpty()) {
            out.append(" [");

            for (String value : alternatives.keySet()) {
                out.append(value);
                out.append(" | ");
            }

            out.delete(out.length() - 3, out.length());
            out.append("]");
        }

        if (hasDefault() && CONFIG.isPresent(key)) {
            if (alternatives.isEmpty()) {
                out.append(String.format(" (default %s)", CONFIG.get(key)));
            } else {
                for (Entry<String, T> entry : alternatives.entrySet()) {
                    if (entry.getValue() instanceof Boolean) {
                        if (entry.getValue().equals(CONFIG.get(key))) {
                            out.append(String.format(" (default %s)",
                                                     entry.getKey()));
                        }
                    }
                    // default value for an object option should be the
                    // same instance
                    else if (entry.getValue() == CONFIG.get(key)) {
                        out.append(String.format(" (default %s)",
                                                 entry.getKey()));
                    }
                }
            }
        }

        return out.toString();
    }

    /**
     * <code>Option</code> for integer parameters.
     */
    public static class IntegerOption extends Option<Integer> {
        /**
         * Creates an <code>IntegerOption</code>.
         * 
         * @param key
         *            the key of the option.
         * @param modifier
         *            the modifier of the option.
         * @param description
         *            the description of the option.
         * @param argument
         *            the name of the option's argument.
         */
        public IntegerOption(ConfigKey<Integer> key,
                             String modifier,
                             String description,
                             String argument) {
            super(key, modifier, description, true, argument);
        }

        @Override
        public void set(String value) {
            try {
                CONFIG.set(key, Integer.parseInt(value));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Expected an integer value: "
                        + value, e);
            }
        }
    }

    /**
     * <code>Option</code> for double parameters.
     */
    public static class DoubleOption extends Option<Double> {
        /**
         * Creates a <code>DoubleOption</code>.
         * 
         * @param key
         *            the key of the option.
         * @param modifier
         *            the modifier of the option.
         * @param description
         *            the description of the option.
         * @param argument
         *            the name of the option's argument.
         */
        public DoubleOption(ConfigKey<Double> key,
                            String modifier,
                            String description,
                            String argument) {
            super(key, modifier, description, true, argument);
        }

        @Override
        public void set(String value) {
            try {
                CONFIG.set(key, Double.parseDouble(value));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Expected a double value: "
                        + value, e);
            }
        }
    }

    /**
     * <code>Option</code> for boolean parameters.
     */
    public static class BooleanOption extends Option<Boolean> {
        /**
         * Creates an <code>BooleanOption</code>.
         * 
         * @param key
         *            the key of the option.
         * @param modifier
         *            the modifier of the option.
         * @param description
         *            the description of the option.
         */
        public BooleanOption(ConfigKey<Boolean> key,
                             String modifier,
                             String description) {
            super(key, modifier, description, false);
        }

        @Override
        public void set(String value) {
            // the presence of the option means that its value
            // should be set
            CONFIG.set(key, Boolean.TRUE);
        }
    }
}