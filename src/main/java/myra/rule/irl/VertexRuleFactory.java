/*
 * VertexRuleFactory.java
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

import static myra.Classifier.RANDOM_GENERATOR;
import static myra.Config.CONFIG;
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
 * This class is responsible for creating classification rules. This is an
 * implementation of Ant-Miner's rule creation procedure, where the pheromones
 * are stored on the vertices of the constructions graph.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class VertexRuleFactory implements RuleFactory {
    /**
     * Creates a classification rule. Note that this method does not determine
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
		       Instance[] instances) {
	// the rule must cover at least MINIMUM_CASES
	final int minimum = CONFIG.get(IntervalBuilder.MINIMUM_CASES);
	Term last = null;

	// the rule being created (empty at the start)
	Rule rule = new Rule(graph.size() / 2);
	int ruleCovered = rule.apply(dataset, instances);

	double[] pheromone = new double[graph.size()];
	boolean[] incompatible = new boolean[graph.size()];
	incompatible[START_INDEX] = true;

	Entry[][] matrix = graph.matrix();

	// the rule creation process starts with an empty rule and adds new
	// terms to the antecedent while the number of covered cases is greater
	// than the minimum allowed and the diversity of the covered instances
	// is greater than 1
	while (ruleCovered > minimum && rule.diversity() > 1) {
	    int selected = -1;

	    while (selected == -1) {
		double total = 0.0;
		// calculates the probability of visiting vertex i by
		// multipliying the pheromone and heuristic information (only
		// compatible vertices are considered)
		for (int i = 0; i < matrix.length; i++) {
		    if (!incompatible[i]) {
			// always using index 0 since pheromones are stored
			// on the vertices
			pheromone[i] =
				matrix[i][0].value(0) * heuristic[i].value(0);

			total += pheromone[i];
		    } else {
			pheromone[i] = 0.0;
		    }
		}

		if (total == 0.0) {
		    // there are no compatible vertices, the creation process
		    // is stopped
		    break;
		}

		double cumulative = 0.0;
		int limit = 0;

		// roulette selection
		double slot = CONFIG.get(RANDOM_GENERATOR).nextDouble();

		for (int i = 0; i < pheromone.length; i++) {
		    if (pheromone[i] > 0) {
			pheromone[i] = cumulative + (pheromone[i] / total);

			if (slot < pheromone[i]) {
			    selected = i;
			    break;
			}

			cumulative = pheromone[i];
			limit = i;
		    }
		}

		if (selected == -1) {
		    // we might not be able to select the last index due
		    // to numeric imprecision

		    selected = limit;
		}

		Vertex vertex = graph.vertices()[selected];
		Condition condition = vertex.condition;

		if (vertex.condition == null) {
		    // continuous vertices do not have a condition,
		    // discretisation is required
		    condition = IntervalBuilder.singleton()
			    .single(dataset, instances, vertex.attribute);
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

		    // a term is only added to the rule if it makes the rule
		    // cover a different number of instances, satisfying the
		    // the minimum limit
		    if (rule.isEmpty() || (ruleCovered != currentCovered
			    && currentCovered >= minimum)) {
			for (int i = 0; i < graph.size(); i++) {
			    if (!incompatible[i] && graph
				    .vertices()[i].attribute == vertex.attribute) {
				incompatible[i] = true;
			    }
			}

			// copy the coverend instances information to the
			// original instances array
			System.arraycopy(clone, 0, instances, 0, clone.length);

			ruleCovered = currentCovered;
			last = null;

			// recompute the heuristic infortation if we are
			// using the dynamic heuristic
			if (CONFIG.get(DYNAMIC_HEURISTIC)) {
			    heuristic = CONFIG.get(DEFAULT_HEURISTIC)
				    .compute(graph,
					     dataset,
					     instances,
					     incompatible);
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