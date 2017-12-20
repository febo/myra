/*
 * ObjectFactory.java
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

package myra.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Utility class to create class instances.
 * 
 * @since 4.5
 * 
 * @author Fernando Esteban Barril Otero
 */
public final class ObjectFactory {
    /**
     * Creates a new object instance of the specified class.
     * 
     * @param <T>
     *            the generic type of the class.
     * @param c
     *            the class of the object to be created.
     * 
     * @return an object of the specified class.
     * 
     * @throws IllegalArgumentException
     *             if there is any error instantiating an object of the
     *             specified class.
     */
    public static <T> T create(Class<T> c) {
	try {
	    return c.getDeclaredConstructor().newInstance();
	} catch (Exception e) {
	    throw new IllegalArgumentException(c.getName()
		    + " could not be instantiated", e);
	}
    }

    /**
     * Creates a new object instance of the specified class.
     * 
     * @param <T>
     *            the generic type of the class.
     * @param c
     *            the class of the object to be created.
     * @param types
     *            the constructor parameter's class types.
     * @param parameters
     *            the constructor parameter's values.
     * 
     * @return an object of the specified class.
     * 
     * @throws IllegalArgumentException
     *             if there is any error instantiating an object of the
     *             specified class.
     */
    public static <T> T create(Class<T> c,
			       Class<?>[] types,
			       Object[] parameters) {
	try {
	    Constructor<T> constructor = c.getConstructor(types);
	    return constructor.newInstance(parameters);
	} catch (NoSuchMethodException e) {
	    throw new IllegalArgumentException("Constructor for '" + c.getName()
		    + "' could not be found", e);
	} catch (InstantiationException e) {
	    throw new IllegalArgumentException(c.getName()
		    + "could not be instantiated", e);
	} catch (InvocationTargetException e) {
	    throw new IllegalArgumentException(c.getName()
		    + "could not be instantiated", e);
	} catch (IllegalAccessException e) {
	    throw new IllegalArgumentException(c.getName()
		    + "could not be instantiated", e);
	} catch (NullPointerException e) {
	    throw new IllegalArgumentException("Class name not specified: null",
					       e);
	}
    }
}