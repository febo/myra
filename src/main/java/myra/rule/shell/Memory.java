/*
 * Memory.java
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

package myra.rule.shell;

import java.util.HashMap;

/**
 * This class represents the shell's memory. Commands can use this to store
 * values to be used by other commands.
 * 
 * @see Shell
 * @see Command
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Memory extends HashMap<Object, Object> {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -9170083061162953550L;

    /**
     * Retrieves an object from the memory.
     * 
     * @param <T>
     *            the type of the location.
     * @param location
     *            the object's location.
     * 
     * @return an object from the memory; <code>null</code> if no object is
     *         found under the spefied <code>location</code>.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Location<T> location) {
        return (T) super.get(location);
    }

    /**
     * Struct-like class to represent a memory location.
     * 
     * @author Fernando Esteban Barril Otero
     *
     * @param <T>
     *            The type of objects that can be associated with the key.
     */
    public static final class Location<T> {
    }
}