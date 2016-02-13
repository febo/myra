/*
 * FixedClassRuleFactory.java
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

package myra.rule.pittsburgh.unordered;

import static myra.Classifier.RANDOM_GENERATOR;
import static myra.Config.CONFIG;
import static myra.interval.IntervalBuilder.DEFAULT_BUILDER;
import static myra.rule.Graph.START_INDEX;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Heuristic.DYNAMIC_HEURISTIC;

import myra.Attribute.Condition;
import myra.Dataset;
import myra.Dataset.Instance;
import myra.interval.IntervalBuilder;
import myra.rule.Graph;
import myra.rule.Graph.Entry;
import myra.rule.Graph.Vertex;
import myra.rule.Rule;
import myra.rule.Rule.Term;

/**
 * This class is responsible for creating classification rules.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class FixedClassRuleFactory {
    /**
     * Creates a classification rule. Note that this method does not determine
     * the consequent of the rule.
     * 
     * @param level
     *            the id (sequence) of the rule.
     * @param graph
     *            the construction graph.
     * @param heuristic
     *            the heuristic values.
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flag.
     * @param target
     *            the index of the target class value.
     * 
     * @return a classification rule.
     */
    public static Rule create(final int level,
			      Graph graph,
			      Entry[] heuristic,
			      Dataset dataset,
			      Instance[] instances,
			      final int target) {
	IntervalBuilder builder = CONFIG.get(DEFAULT_BUILDER);

	// the rule must cover at least MINIMUM_CASES
	final int minimum = CONFIG.get(IntervalBuilder.MINIMUM_CASES);
	Term last = null;

	// the rule being created (empty at the start)
	Rule rule = new Rule(graph.size() / 2);
	rule.setConsequent(target);
	int ruleCovered = rule.apply(dataset, instances);
	int previous = START_INDEX;

	double[] pheromone = new double[graph.size()];
	boolean[] incompatible = new boolean[graph.size()];
	incompatible[START_INDEX] = true;

	// the rule creation process starts with an empty rule and adds new
	// terms to the antecedent while the number of covered cases is greater
	// than the minimum allowed and the diversity of the covered instances
	// is greater than 1
	while (ruleCovered > minimum && rule.diversity() > 1) {
	    int selected = -1;

	    while (selected == -1) {
		double total = 0.0;
		Entry[] neighbours = graph.matrix()[previous];
		// the number of nominal neighbours
		int nominal = 0;

		// calculates the probability of visiting vertex i by
		// multipliying the pheromone and heuristic information (only
		// compatible vertices are considered)
		for (int i = 0; i < neighbours.length; i++) {
		    if (!incompatible[i] && neighbours[i] != null) {
			pheromone[i] = neighbours[i].value(level)
				* heuristic[i].value(target);

			total += pheromone[i];

			if (graph.vertices()[i].condition != null) {
			    nominal++;
			}
		    } else {
			pheromone[i] = 0.0;
		    }
		}

		if (total == 0.0) {
		    // there are no compatible vertices, the creation process
		    // is stopped
		    break;
		} else if (nominal == 0 && ruleCovered < (minimum * 2)) {
		    // if no nominal neighbour is available and the number of
		    // covered instances is smaller that 2 times the minimum,
		    // the creation is stopped since the dynamic discretisation
		    // won't be able to create intervals
		    break;
		}

		// prepares the roulette by accumulation the probabilities,
		// from 0 to 1
		double cumulative = 0.0;

		for (int i = 0; i < pheromone.length; i++) {
		    if (pheromone[i] > 0) {
			pheromone[i] = cumulative + (pheromone[i] / total);
			cumulative = pheromone[i];
		    }
		}

		for (int i = (pheromone.length - 1); i >= 0; i--) {
		    if (pheromone[i] > 0) {
			pheromone[i] = 1.0;
			break;
		    }
		}

		// roulette selection
		double slot = CONFIG.get(RANDOM_GENERATOR).nextDouble();

		for (int i = 0; i < pheromone.length; i++) {
		    if (slot < pheromone[i]) {
			selected = i;
			break;
		    }
		}

		Vertex vertex = graph.vertices()[selected];
		Condition condition = vertex.condition;

		if (vertex.condition == null) {
		    // continuous vertices do not have a condition,
		    // discretisation is required
		    condition =
			    builder.single(dataset,
					   instances,
					   vertex.attribute,
					   target);
		}

		if (vertex.condition == null && condition == null) {
		    // the discretisation may not be able to produce an
		    // interval for the selected attribute
		    incompatible[selected] = true;
		    selected = -1;
		} else {
		    last = new Term(selected, condition);
		    rule.push(last);

		    Instance[] clone = Instance.copyOf(instances);
		    int currentCovered = rule.apply(dataset, clone);
		    int targetCovered = rule.covered()[target];

		    // a term is only added to the rule if it makes the rule
		    // cover a different number of instances, satisfying the
		    // the minimum limit and covering at least one instance
		    // of the target class
		    if (ruleCovered != currentCovered
			    && currentCovered >= minimum && targetCovered > 0) {
			for (int i = 0; i < graph.size(); i++) {
			    if (!incompatible[i] && graph
				    .vertices()[i].attribute == vertex.attribute) {
				incompatible[i] = true;
			    }
			}

			// copy the coverend instances information to the
			// original instances array
			System.arraycopy(clone, 0, instances, 0, clone.length);

			previous = selected;
			ruleCovered = currentCovered;
			last = null;

			if (CONFIG.get(DYNAMIC_HEURISTIC)) {
			    heuristic = CONFIG.get(DEFAULT_HEURISTIC)
				    .compute(graph,
					     dataset,
					     instances,
					     incompatible,
					     target);
			}
		    } else {
			// removed the last added term and marks the selected
			// vertex as incompatible
			rule.pop();
			incompatible[selected] = true;
			selected = -1;
		    }
		}
	    }

	    if (selected == -1) {
		// no vertex could be selected
		break;
	    }
	}

	rule.compact();

	if (last != null) {
	    // determines the coverage information, since a term was added
	    // to the rule and later removed
	    rule.apply(dataset, instances);
	}

	return rule;
    }
}
