/*
 * FindRuleActivity.java
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

import static myra.Config.CONFIG;
import static myra.Dataset.NOT_COVERED;
import static myra.Dataset.RULE_COVERED;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.irl.PheromonePolicy.DEFAULT_POLICY;
import static myra.rule.irl.RuleFactory.DEFAULT_FACTORY;
import myra.Config.ConfigKey;
import myra.Dataset;
import myra.Dataset.Instance;
import myra.IterativeActivity;
import myra.rule.Graph;
import myra.rule.Graph.Entry;
import myra.rule.Rule;

/**
 * The <code>EvolverActivity</code> is responsible for evolving a single rule
 * using an ACO-based procedure.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class FindRuleActivity extends IterativeActivity<Rule> {
    /**
     * The config key for the convergence test. If the same rule is created over
     * <code>CONVERGENCE</code> iterations, the creation process is considered
     * stagnant.
     */
    public final static ConfigKey<Integer> CONVERGENCE =
	    new ConfigKey<Integer>();

    /**
     * Instance flag array indicating the instances to be used during the
     * construction procedure.
     */
    private Instance[] instances;

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
    private PheromonePolicy policy;

    /**
     * The heuristic values for the graph's vertices.
     */
    private Entry[] heuristic;

    /**
     * The convergence termination criteria counter.
     */
    private int convergence;

    /**
     * The best-so-far rule.
     */
    private Rule best;

    /**
     * Creates a new <code>FindRuleActivity</code> object.
     * 
     * @param graph
     *            the construction graph.
     * @param instances
     *            the instances to be used.
     * @param training
     *            the current dataset.
     */
    public FindRuleActivity(Graph graph, Instance[] instances,
	    Dataset training) {
	this.graph = graph;
	this.instances = instances;
	this.dataset = training;
    }

    /**
     * Returns the best-so-far rule.
     * 
     * @return the best-so-far rule.
     */
    public Rule getBest() {
	return best;
    }

    @Override
    public Rule create() {
	Rule rule = CONFIG.get(DEFAULT_FACTORY)
		.create(graph, heuristic, dataset, instances);

	CONFIG.get(DEFAULT_PRUNER).prune(dataset, rule, instances);
	rule.setQuality(CONFIG.get(DEFAULT_FUNCTION).evaluate(rule));

	return rule;
    }

    @Override
    public void initialise() {
	super.initialise();

	policy = CONFIG.get(DEFAULT_POLICY);
	policy.initialise(graph);

	convergence = 0;

	// the heuristic procedure only takes into account
	// the instances covered by a rule, so we prepare an
	// instance array where each NOT_COVERED value is
	// replaced by a RULE_COVERED value

	Instance[] clone = Instance.copyOf(instances);
	Instance.mark(clone, NOT_COVERED, RULE_COVERED);

	heuristic =
		CONFIG.get(DEFAULT_HEURISTIC).compute(graph, dataset, clone);
    }

    @Override
    public boolean terminate() {
	return super.terminate() || convergence > CONFIG.get(CONVERGENCE);
    }

    @Override
    public void update(Rule candidate) {
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