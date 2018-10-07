/*
 * GraphFactory.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2018 Fernando Esteban Barril Otero
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

package myra.rule;

import static myra.Config.CONFIG;

import myra.Config.ConfigKey;
import myra.datamining.Dataset;
import myra.util.ObjectFactory;


/**
 * @author amh58
 */
public abstract class GraphFactory {
    /**
     * The config key for the default graph factory instance.
     */
    public final static ConfigKey<Class<? extends GraphFactory>> DEFAULT_GRAPH =
	    new ConfigKey<>();
    
    /**
     * Returns a new <code>Rule</code> object using the default class
     * implementation.
     * @param dataset 
     * 
     * @return a new <code>Rule</code> object.
     * 
     * @see #DEFAULT_RULE
     */
    public static GraphFactory newInstance(Dataset dataset) {
	return ObjectFactory.create(CONFIG.get(DEFAULT_GRAPH),
				    new Class<?>[] { Dataset.class },
				    new Object[] { dataset });
    }

}
