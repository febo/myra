/*
 * Config.java
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

import java.util.HashMap;

/**
 * The <code>Config</code> holds all configuration parameters for the execution
 * of an algorithm.
 * 
 * @author Fernando Esteban Barril Otero
 */
public final class Config {
    /**
     * The singleton instance.
     */
    public static final Config CONFIG = new Config();

    /**
     * Mapping of <code>ConfigKey</code> object and values.
     */
    private HashMap<ConfigKey<?>, Object> mapping = new HashMap<>();

    /**
     * Private constructor.
     */
    private Config() {
    }

    /**
     * Sets the specified value to the <code>ConfigKey</code> object.
     * 
     * @param <T>
     *            the type of the key.
     * @param key
     *            the <code>ConfigKey</code> object.
     * @param value
     *            the value to set.
     */
    public <T> void set(ConfigKey<T> key, T value) {
	mapping.put(key, value);
    }

    /**
     * Returns the value associated with the specified <code>ConfigKey</code>
     * object.
     * 
     * @param <T>
     *            the type of the key.
     * @param key
     *            the <code>ConfigKey</code> object.
     * 
     * @return the value associated with the specified <code>ConfigKey</code>
     *         object.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(ConfigKey<T> key) {
	T value = (T) mapping.get(key);

	if (value == null) {
	    throw new IllegalStateException("ConfigKey " + key
		    + " has not been set.");
	}

	return value;
    }

    /**
     * Returns <code>true</code> if the specified <code>ConfigKey</code> object
     * has been set.
     * 
     * @param <T>
     *            the type of the key.
     * @param key
     *            the <code>ConfigKey</code> object.
     * 
     * @return <code>true</code> if the specified <code>ConfigKey</code> object
     *         has been set; <code>false</code> otherwise.
     */
    public <T> boolean isPresent(ConfigKey<T> key) {
	return mapping.containsKey(key);
    }

    /**
     * Struct-like class to represent a configuration key.
     * 
     * @param <T>
     *            The type of objects that can be associated with the key.
     */
    public static final class ConfigKey<T> {
    }
}