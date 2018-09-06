/*
 * ArchiveRuleFactory.java
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

package myra.rule.archive;

import static myra.Config.CONFIG;
import static myra.datamining.Algorithm.RANDOM_GENERATOR;
import static myra.rule.Graph.START_INDEX;

import myra.classification.rule.SinglePassPruner;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.rule.Graph.Entry;
import myra.rule.Rule;
import myra.rule.Rule.Term;
import myra.rule.archive.Graph.Vertex;
import myra.rule.pittsburgh.LevelRuleFactory;

/**
 * Rule factory that uses a archive to sample value for rules' terms. Rules
 * created by this factory usually will have to undergo pruning using the
 * {@link SinglePassPruner}.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class ArchiveRuleFactory extends LevelRuleFactory {
    @Override
    public Rule create(int level,
		       myra.rule.Graph graph,
		       Entry[] heuristic,
		       Dataset dataset,
		       Instance[] instances) {

	if (!Graph.class.isInstance(graph)) {
	    throw new IllegalArgumentException("Invalid graph class: "
		    + graph.getClass());
	}

	return this.create(level, (Graph) graph, heuristic, dataset, instances);
    }

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
     * 
     * @return a classification rule.
     */
    public Rule create(int level,
		       Graph graph,
		       Entry[] heuristic,
		       Dataset dataset,
		       Instance[] instances) {
	// the rule being created (empty at the start)
	Rule rule = Rule.newInstance(graph.size() / 2);
	int previous = START_INDEX;

	double[] pheromone = new double[graph.size()];
	boolean[] incompatible = new boolean[graph.size()];
	incompatible[START_INDEX] = true;

	while (true) {
	    double total = 0.0;
	    Entry[] neighbours = graph.matrix()[previous];

	    // calculates the probability of visiting vertex i by
	    // multiplying the pheromone and heuristic information (only
	    // compatible vertices are considered)
	    for (int i = 0; i < neighbours.length; i++) {
		if (!incompatible[i] && neighbours[i] != null) {
		    pheromone[i] =
			    neighbours[i].value(level) * heuristic[i].value(0);

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
	    int selected = Graph.END_INDEX;

	    for (int i = 0; i < pheromone.length; i++) {
		if (slot < pheromone[i]) {
		    selected = i;
		    break;
		}
	    }

	    if (selected == Graph.END_INDEX) {
		break;
	    }

	    Vertex vertex = graph.vertices()[selected];
	    Term term = new Term(selected, vertex.condition(level));
	    rule.push(term);

	    previous = selected;
	    // make the vertex unavailable
	    incompatible[selected] = true;
	}

	rule.compact();

	return rule;
    }
}