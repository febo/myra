/*
 * FindRuleListActivity.java
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

package myra.rule.pittsburgh;

import static myra.Config.CONFIG;
import static myra.Dataset.NOT_COVERED;
import static myra.Dataset.RULE_COVERED;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.ListMeasure.DEFAULT_MEASURE;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import myra.Config.ConfigKey;
import myra.Dataset;
import myra.Dataset.Instance;
import myra.IterativeActivity;
import myra.rule.Graph;
import myra.rule.Graph.Entry;
import myra.rule.Rule;
import myra.rule.RuleList;

public class FindRuleListActivity extends IterativeActivity<RuleList> {
    /**
     * The config key for the convergence test. If the same rule is created over
     * <code>CONVERGENCE</code> iterations, the creation process is considered
     * stagnant.
     */
    public final static ConfigKey<Integer> CONVERGENCE =
	    new ConfigKey<Integer>();

    /**
     * The config key for the percentage of uncovered instances allowed.
     */
    public static final ConfigKey<Double> UNCOVERED = new ConfigKey<Double>();

    /**
     * The current dataset.
     */
    private Dataset dataset;

    /**
     * The construction graph.
     */
    private Graph graph;

    /**
     * The ACO pheromone policy.
     */
    private LevelPheromonePolicy policy;

    /**
     * The convergence termination criteria counter.
     */
    private int convergence;

    /**
     * The convergence termination criteria counter.
     */
    private boolean reset;

    /**
     * The best-so-far list of rules.
     */
    private RuleList best;

    /**
     * The (initial) heuristic of the dataset. This value is not modified after
     * the {@link #initialise()} method.
     */
    private Entry[] INITIAL_HEURISTIC;

    /**
     * Creates a new <code>FindRuleListActivity</code> object.
     * 
     * @param graph
     *            the construction graph.
     * @param dataset
     *            the current dataset.
     */
    public FindRuleListActivity(Graph graph, Dataset dataset) {
	this.graph = graph;
	this.dataset = dataset;
    }

    /**
     * Returns the best-so-far list of rules.
     * 
     * @return the best-so-far list of rules.
     */
    public RuleList getBest() {
	return best;
    }

    @Override
    public RuleList create() {
	Instance[] instances = Instance.newArray(dataset.size());
	Instance.markAll(instances, NOT_COVERED);
	Entry[] heuristic = Entry.deepClone(INITIAL_HEURISTIC);

	RuleList list = new RuleList();
	list.setIteration(iteration);

	int available = dataset.size();
	int uncovered = (int) ((dataset.size() * CONFIG.get(UNCOVERED)) + 0.5);

	while (available >= uncovered) {
	    if (list.size() > 0) {
		// the heuristic procedure only takes into account
		// the instances covered by a rule, so we prepare an
		// instance array where each NOT_COVERED value is
		// replaced by a RULE_COVERED value

		Instance.mark(instances, NOT_COVERED, RULE_COVERED);

		heuristic = CONFIG.get(DEFAULT_HEURISTIC)
			.compute(graph, dataset, instances);

		Instance.mark(instances, RULE_COVERED, NOT_COVERED);
	    }

	    // creates a rule for the current level

	    Rule rule = LevelRuleFactory
		    .create(list.size(), graph, heuristic, dataset, instances);

	    available =
		    CONFIG.get(DEFAULT_PRUNER).prune(dataset, rule, instances);

	    list.add(rule);

	    if (rule.size() == 0) {
		break;
	    }

	    // marks the instances covered by the current rule as
	    // COVERED, so they are not available for the next
	    // iterations
	    Dataset.markCovered(instances);
	}

	if (!list.hasDefault()) {
	    if (available == 0) {
		Instance.markAll(instances, NOT_COVERED);
	    }

	    Rule rule = new Rule();
	    rule.apply(dataset, instances);
	    CONFIG.get(ASSIGNATOR).assign(rule);
	    list.add(rule);
	}

	list.setQuality(CONFIG.get(DEFAULT_MEASURE).evaluate(dataset, list));

	return list;
    }

    @Override
    public void initialise() {
	super.initialise();

	policy = new LevelPheromonePolicy();
	policy.initialise(graph);

	convergence = 0;
	reset = true;

	// (initial) heuristic of the whole dataset

	Instance[] instances = Instance.newArray(dataset.size());
	Instance.markAll(instances, RULE_COVERED);

	INITIAL_HEURISTIC = CONFIG.get(DEFAULT_HEURISTIC)
		.compute(graph, dataset, instances);
    }

    @Override
    public boolean terminate() {
	if (convergence > CONFIG.get(CONVERGENCE)) {
	    if (reset) {
		policy.initialise(graph);
		convergence = 0;
		reset = false;
	    } else {
		return true;
	    }
	}

	return super.terminate();
    }

    @Override
    public void update(RuleList candidate) {
	policy.update(graph, candidate);

	// updates the global best
	if (best == null || candidate.compareTo(best) > 0) {
	    best = candidate;
	    convergence = 0;
	} else if (candidate.compareTo(best) == 0) {
	    convergence++;
	}
    }
}