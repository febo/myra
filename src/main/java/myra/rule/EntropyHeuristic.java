/*
 * EntropyHeuristic.java
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

import static myra.Attribute.Type.CONTINUOUS;
import static myra.Attribute.Type.NOMINAL;
import static myra.Dataset.RULE_COVERED;
import static myra.rule.Graph.START_INDEX;

import java.util.Arrays;

import myra.Attribute;
import myra.Dataset;
import myra.Attribute.Condition;
import myra.Dataset.Instance;
import myra.interval.IntervalBuilder;
import myra.rule.Graph.Entry;

/**
 * This class is responsible to calculate the heuristic information based on the
 * entropy for each term (vertex).
 * 
 * @author Fernando Esteban Barril Otero
 */
public class EntropyHeuristic implements Heuristic {
    /**
     * The zero-equivalent double value.
     */
    private static final double ZERO = 1E-15;

    @Override
    public Entry[] compute(Graph graph, Dataset dataset, Instance[] instances) {
	return compute(graph, dataset, instances, new boolean[0]);
    }

    @Override
    public Entry[] compute(Graph graph,
			   Dataset dataset,
			   Instance[] instances,
			   boolean[] used) {
	final double log_k = Math.log(dataset.classLength()) / Math.log(2.0);
	Entry[] heuristic = Entry.initialise(new Entry[graph.size()]);

	if (graph.vertices[START_INDEX].attribute == -1) {
	    // we are dealing with a virtual starting vertex
	    heuristic[START_INDEX].set(0, 0.0);
	}

	boolean[] available = new boolean[dataset.attributes().length];
	Arrays.fill(available, true);

	// determines the available attributes

	for (int i = 0; i < used.length; i++) {
	    if (used[i]) {
		heuristic[i].set(0, 0.0);

		int index = graph.vertices()[i].attribute;

		if (index != -1) {
		    available[index] = false;
		}
	    }
	}

	// computes the heuristic of the vertices which the attribute is
	// available

	for (int i = 0; i < available.length; i++) {
	    if (available[i]) {
		Attribute attribute = dataset.attributes()[i];

		// nominal attributes
		if (attribute.getType() == NOMINAL
			&& i != dataset.classIndex()) {
		    // (1) calculates the class distribution for each value of
		    // the attribute

		    int[] terms =
			    new int[attribute.size() * dataset.classLength()];
		    int[] counter = new int[attribute.size()];

		    for (int j = 0; j < dataset.size(); j++) {
			if (instances[j].flag == RULE_COVERED) {
			    double v = dataset.value(j, attribute.getIndex());

			    if (v != Dataset.MISSING_VALUE_INDEX) {
				terms[(int) ((v * dataset.classLength())
					+ dataset
						.value(j,
						       dataset.classIndex()))]++;
				counter[(int) v]++;
			    }
			}
		    }

		    // (2) calculates the entropy of each term (attribute,
		    // value)

		    for (int j = 0; j < attribute.size(); j++) {
			int vertex = graph.indexOf(attribute.getIndex(), j);

			if (counter[j] > 0) {
			    int index = j * dataset.classLength();
			    double entropy = 0.0;

			    for (int k = 0; k < dataset.classLength(); k++) {
				if (terms[index + k] > 0) {
				    double p = terms[index + k]
					    / (double) counter[j];
				    entropy -=
					    (p * (Math.log(p) / Math.log(2.0)));
				}
			    }

			    double value = log_k - entropy;
			    heuristic[vertex].set(0,
						  (value < ZERO) ? 0.0 : value);
			} else {
			    heuristic[vertex].set(0, 0.0);
			}
		    }
		}
		// continuous attributes
		else if (attribute.getType() == CONTINUOUS) {
		    Condition condition = IntervalBuilder.singleton()
			    .single(dataset, instances, attribute.getIndex());
		    double value = 0.0;

		    if (condition != null) {
			value = log_k - condition.entropy;
		    }

		    int vertex = graph.indexOf(attribute.getIndex(), -1);
		    heuristic[vertex].set(0, (value < ZERO) ? 0.0 : value);
		}
	    }
	}

	return heuristic;
    }

    @Override
    public Entry[] compute(Graph graph,
			   Dataset dataset,
			   Instance[] instances,
			   int target) {
	return compute(graph, dataset, instances, new boolean[0], target);
    }

    /**
     * This detault implementation ignores the <code>target</code> parameter.
     */
    @Override
    public Entry[] compute(Graph graph,
			   Dataset dataset,
			   Instance[] instances,
			   boolean[] used,
			   int target) {
	return compute(graph, dataset, instances, used);
    }
}