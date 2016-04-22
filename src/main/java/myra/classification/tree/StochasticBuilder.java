/*
 * TreeBuilder.java
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

package myra.classification.tree;

import static myra.Config.CONFIG;
import static myra.datamining.Algorithm.RANDOM_GENERATOR;
import static myra.classification.tree.Graph.START_INDEX;
import static myra.classification.tree.Heuristic.DEFAULT_HEURISTIC;

import myra.Config.ConfigKey;
import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;

/**
 * This class is responsible to probabilistically create a decision tree using
 * the pheromone values.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class StochasticBuilder extends TreeBuilder {
    /**
     * The config key to indicate if the heuristic value is recomputed at each
     * level of the tree.
     */
    public final static ConfigKey<Boolean> DYNAMIC_HEURISTIC =
	    new ConfigKey<>();

    @Override
    protected Attribute select(Graph graph,
			       double[] heuristic,
			       Dataset dataset,
			       boolean[] used,
			       Instance[] instances,
			       InternalNode parent,
			       final int index) {
	if (CONFIG.get(DYNAMIC_HEURISTIC)) {
	    heuristic = CONFIG.get(DEFAULT_HEURISTIC).compute(dataset,
							      instances,
							      used);
	}

	double[] pheromone = graph.pheromone((parent == null) ? START_INDEX
		: InternalNode.encode(parent, parent.conditions[index]));

	double[] probabilities = new double[pheromone.length];
	double total = 0;

	for (int i = 0; i < pheromone.length; i++) {
	    if (!used[i]) {
		probabilities[i] = pheromone[i] * heuristic[i];
		total += probabilities[i];
	    } else {
		probabilities[i] = 0.0;
	    }
	}

	if (total == 0.0) {
	    // there are no compatible attributes, the creation process
	    // is stopped
	    return null;
	}

	// prepares the roulette by accumulating the probabilities
	double cumulative = 0.0;

	for (int i = 0; i < probabilities.length; i++) {
	    if (probabilities[i] > 0) {
		probabilities[i] = cumulative + (probabilities[i] / total);
		cumulative = probabilities[i];
	    }
	}

	for (int i = (probabilities.length - 1); i >= 0; i--) {
	    if (probabilities[i] > 0) {
		probabilities[i] = 1.0;
		break;
	    }
	}

	// roulette selection
	double slot = CONFIG.get(RANDOM_GENERATOR).nextDouble();
	int selected = -1;

	for (int i = 0; i < probabilities.length; i++) {
	    if (slot < probabilities[i]) {
		selected = i;
		break;
	    }
	}

	return (selected == -1) ? null : dataset.attributes()[selected];
    }
}