/*
 * EdgePheromonePolicy.java
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

import static myra.rule.Graph.START_INDEX;
import myra.rule.Graph;
import myra.rule.Rule;
import myra.rule.Graph.Entry;
import myra.rule.Rule.Term;

/**
 * This class is responsible for maintaining the pheromone values of the
 * construction graph. Pheromones are stored in each edge.
 * 
 * <p>
 * Evaporation is simulated by normalising pheromone values after an update.
 * Values that do not increase during the update will decrease as a result of
 * the normalisation.
 * </p>
 * 
 * @author Fernando Esteban Barril Otero
 */
public final class EdgePheromonePolicy implements PheromonePolicy {
    /**
     * Initialises the pheromone values of the specified graph.
     * 
     * @param graph
     *            the construction graph to be initialised.
     */
    public void initialise(Graph graph) {
	Entry[][] matrix = graph.matrix();

	for (int i = 0; i < matrix.length; i++) {
	    int count = 0;

	    for (int j = 0; j < matrix[i].length; j++) {
		if (matrix[i][j] != null) {
		    count++;
		}
	    }

	    double initial = 1.0 / count;

	    for (int j = 0; j < matrix[i].length; j++) {
		if (matrix[i][j] != null) {
		    matrix[i][j] = new Entry(initial, initial);
		}
	    }
	}
    }

    /**
     * Updates the pheromone values, increasing the pheromone according to the
     * <code>rule</code> quality. Evaporation is also performed by normalising
     * the pheromone values.
     * 
     * @param graph
     *            the construction graph.
     * @param rule
     *            the rule to guide the update.
     */
    public void update(Graph graph, Rule rule) {
	Term[] terms = rule.terms();
	Entry[][] matrix = graph.matrix();

	final double q = rule.getQuality();
	int from = START_INDEX;

	for (int i = 0; i < terms.length; i++) {
	    Entry entry = matrix[from][terms[i].index()];
	    double value = entry.value(0);
	    entry.set(0, value + (value * q));

	    from = terms[i].index();
	}

	// normalises the pheromone values (it has the effect of
	// evaporation for edges that have not being updated)

	for (int i = 0; i < matrix.length; i++) {
	    double total = 0.0;

	    for (int j = 0; j < matrix[i].length; j++) {
		if (matrix[i][j] != null) {
		    total += matrix[i][j].value(0);
		}
	    }

	    for (int j = 0; j < matrix[i].length; j++) {
		if (matrix[i][j] != null) {
		    double value = matrix[i][j].value(0);
		    matrix[i][j].set(0, value / total);
		}
	    }
	}
    }
}