/*
 * PheromonePolicy.java
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

package myra.rule.irl;

import myra.Config.ConfigKey;
import myra.rule.Graph;
import myra.rule.Rule;

/**
 * The <code>PheromonePolicy</code> represents the procedures used to control
 * pheromone values during the search. The basic operations are:
 * 
 * <ul>
 * <li>initialisation</li>
 * <li>update (including evaporation)</li>
 * </ul>
 * 
 * @author Fernando Esteban Barril Otero
 */
public interface PheromonePolicy {
    /**
     * The config key for the default pheromone policy instance.
     */
    public static final ConfigKey<PheromonePolicy> DEFAULT_POLICY =
            new ConfigKey<PheromonePolicy>();

    /**
     * Initialises the pheromone values of the specified graph.
     * 
     * @param graph
     *            the construction graph to be initialised.
     */
    public void initialise(Graph graph);

    /**
     * Updates the pheromone values, increasing the pheromone according to the
     * <code>rule</code> quality.
     * 
     * @param graph
     *            the construction graph.
     * @param rule
     *            the rule to guide the update.
     */
    public void update(Graph graph, Rule rule);
}