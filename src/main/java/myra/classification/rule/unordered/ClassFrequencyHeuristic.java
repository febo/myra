/*
 * ClassFrequencyHeuristic.java
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

package myra.classification.rule.unordered;

import static myra.Config.CONFIG;
import static myra.datamining.Attribute.Type.CONTINUOUS;
import static myra.datamining.Attribute.Type.NOMINAL;
import static myra.datamining.Dataset.RULE_COVERED;
import static myra.datamining.IntervalBuilder.DEFAULT_BUILDER;
import static myra.rule.Graph.START_INDEX;

import java.util.Arrays;

import myra.classification.rule.unordered.attribute.ClassAwareSplit;
import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset.Instance;
import myra.rule.Graph;
import myra.rule.Graph.Entry;
import myra.rule.Heuristic;

/**
 * This class is responsible to calculate the class-specific heuristic
 * information based on the frequency of each term (vertex).
 * 
 * @author Fernando Esteban Barril Otero
 */
public class ClassFrequencyHeuristic implements Heuristic {
    @Override
    public Entry[] compute(Graph graph, Dataset dataset, Instance[] instances) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Entry[] compute(Graph graph,
			   Dataset dataset,
			   Instance[] instances,
			   boolean[] used) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Entry[] compute(Graph graph,
			   Dataset dataset,
			   Instance[] instances,
			   int target) {
	return compute(graph, dataset, instances, new boolean[0], target);
    }

    @Override
    public Entry[] compute(Graph graph,
			   Dataset dataset,
			   Instance[] instances,
			   boolean[] used,
			   int target) {
	ClassAwareSplit builder = null;

	try {
	    builder = (ClassAwareSplit) CONFIG.get(DEFAULT_BUILDER);
	} catch (ClassCastException e) {
	    throw new RuntimeException("ClassAwareSplit instance expected", e);
	}

	Entry[] heuristic = Entry.initialise(new Entry[graph.size()]);
	heuristic[START_INDEX].set(0, 0.0);

	boolean[] available = new boolean[dataset.attributes().length];
	Arrays.fill(available, true);

	// determines the available attributes

	for (int i = 0; i < used.length; i++) {
	    if (used[i]) {
		heuristic[i].setAll(0.0);

		int index = graph.vertices()[i].attribute;

		if (index != -1) {
		    available[graph.vertices()[i].attribute] = false;
		}
	    }
	}

	for (int i = 0; i < dataset.attributes().length; i++) {
	    if (available[i]) {
		Attribute attribute = dataset.attributes()[i];

		// nominal attributes
		if (attribute.getType() == NOMINAL
			&& i != dataset.classIndex()) {
		    // (1) calculates the class distribution for each value of
		    // the
		    // attribute

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

		    // (2) calculates the class-specific frequency of each
		    // term (attribute-condition)

		    for (int j = 0; j < attribute.size(); j++) {
			int vertex = graph.indexOf(attribute.getIndex(), j);

			if (counter[j] > 0) {
			    // only computes the value for the target

			    int index = j * dataset.classLength();
			    double value = 0;

			    if (terms[index + target] > 0) {
				value = terms[index + target]
					/ (double) (counter[j]);
			    }

			    heuristic[vertex].set(target, value);
			} else {
			    heuristic[vertex].set(target, 0.0);
			}
		    }
		}
		// continuous attributes
		else if (attribute.getType() == CONTINUOUS) {
		    Condition condition = builder.single(dataset,
							 instances,
							 attribute.getIndex(),
							 target);

		    double value = 0.0;

		    if (condition != null) {
			// index 1 is the target (positive) value
			value = condition.frequency[target] / condition.length;
		    }

		    int vertex = graph.indexOf(attribute.getIndex(), -1);
		    heuristic[vertex].set(target, value);
		}
	    }
	}

	return heuristic;
    }
}