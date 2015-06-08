/*
 * Assignator.java
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

package myra.rule;

import myra.Config.ConfigKey;

/**
 * The <code>Assignator</code> should be implemented by classes that are
 * responsible of assigning the consequent of a rule.
 * 
 * @author Fernando Esteban Barril Otero
 */
public interface Assignator {
    /**
     * The config key for the default assignator instance.
     */
    public final static ConfigKey<Assignator> ASSIGNATOR =
	    new ConfigKey<Assignator>();

    /**
     * Assignes the consequent of the rule.
     * 
     * @param rule
     *            a rule.
     * 
     * @return the number of uncovered instances.
     * 
     * @see Rule#covered()
     * @see Rule#uncovered()
     */
    public int assign(Rule rule);
}