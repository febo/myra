/*
 * EdgeArchivePhermonePolice.java
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

package myra.rule.irl;

import static myra.rule.Graph.START_INDEX;

import myra.rule.archive.Graph;
import myra.rule.archive.Graph.Vertex;
import myra.rule.Rule;
import myra.rule.Graph.Entry;
import myra.rule.Rule.Term;

/**
 * @author amh58
 */
public class EdgeArchivePhermonePolicy implements PheromonePolicy {


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

	final double q = rule.getQuality().raw();
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
	
	//update archive in the graph
	
	for (int j = 0; j < rule.size(); j++) {
		Term term = rule.terms()[j];
		Vertex vertex = graph.vertices()[term.index()];
		vertex.update(0, term.condition(), q);
	   }
    }

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

    /* (non-Javadoc)
     * @see myra.rule.irl.PheromonePolicy#update(myra.rule.Graph, myra.rule.Rule)
     */
    @Override
    public void update(myra.rule.Graph graph, Rule rule) {
	if (!Graph.class.isInstance(graph)) {
	    throw new IllegalArgumentException("Invalid graph class: "
		    + graph.getClass());
	}
	
	 this.update((Graph) graph,rule);
    }


    /* (non-Javadoc)
     * @see myra.rule.irl.PheromonePolicy#initialise(myra.rule.Graph)
     */
    @Override
    public void initialise(myra.rule.Graph graph) {
	if (!Graph.class.isInstance(graph)) {
	    throw new IllegalArgumentException("Invalid graph class: "
		    + graph.getClass());
	}
	
	 this.initialise((Graph) graph);
    }
}