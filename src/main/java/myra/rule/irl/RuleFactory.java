/*
 * RuleFactory.java
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
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.rule.Graph;
import myra.rule.Graph.Entry;
import myra.rule.Rule;

/**
 * The <code>RuleFactory</code> represents the rule construction procedure.
 * 
 * @author Fernando Esteban Barril Otero
 */
public interface RuleFactory {
    /**
     * The config key for the default rule factory instance.
     */
    public final static ConfigKey<RuleFactory> DEFAULT_FACTORY =
	    new ConfigKey<RuleFactory>();

    /**
     * Creates a classification rule. Note that this method does not need to set
     * the consequent of the rule.
     * 
     * @param graph
     *            the construction graph.
     * @param heuristic
     *            the heuristic values of graph's vertices.
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flag.
     * 
     * @return a classification rule.
     */
    public Rule create(Graph graph,
		       Entry[] heuristic,
		       Dataset dataset,
		       Instance[] instances);
}