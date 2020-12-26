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
import static myra.datamining.Dataset.NOT_COVERED;
import static myra.datamining.Dataset.RULE_COVERED;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.irl.PheromonePolicy.DEFAULT_POLICY;
import static myra.rule.irl.RuleFactory.DEFAULT_FACTORY;

import myra.Archive;
import myra.IterativeActivity;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.rule.Graph;
import myra.rule.Graph.Entry;
import myra.rule.Rule;

/**
 * The <code>FindRuleActivity</code> is responsible for evolving a single rule
 * using an ACO-based procedure.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class FindRuleActivity extends IterativeActivity<Rule> {
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
     * Creates a new <code>FindRuleActivity</code> object.
     * 
     * @param graph
     *            the construction graph.
     * @param instances
     *            the instances to be used.
     * @param training
     *            the current dataset.
     */
    public FindRuleActivity(Graph graph,
                            Instance[] instances,
                            Dataset training) {
        this.graph = graph;
        this.instances = instances;
        this.dataset = training;
    }

    @Override
    public Rule create() {
        // the instances array will be modified by the create and prune,
        // so we need to work on a copy to avoid concurrency problems
        Instance[] clone = Instance.copyOf(instances);

        Rule rule = CONFIG.get(DEFAULT_FACTORY)
                .create(graph, heuristic, dataset, clone);

        CONFIG.get(DEFAULT_PRUNER).prune(dataset, rule, clone);
        rule.setQuality(CONFIG.get(DEFAULT_FUNCTION)
                .evaluate(dataset, rule, clone));

        return rule;
    }

    @Override
    public void initialise() {
        super.initialise();

        policy = CONFIG.get(DEFAULT_POLICY);
        policy.initialise(graph);

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
        return super.terminate() || stagnation > CONFIG.get(STAGNATION);
    }

    @Override
    public void update(Archive<Rule> archive) {
        super.update(archive);
        policy.update(graph, archive.highest());
    }
}