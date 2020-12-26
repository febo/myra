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
import static myra.datamining.Dataset.NOT_COVERED;
import static myra.datamining.Dataset.RULE_COVERED;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.ListMeasure.DEFAULT_MEASURE;
import static myra.rule.ListPruner.DEFAULT_LIST_PRUNER;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.pittsburgh.FindRuleListActivity.UNCOVERED;

import myra.Archive;
import myra.Config.ConfigKey;
import myra.IterativeActivity;
import myra.classification.Label;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.rule.Graph;
import myra.rule.Graph.Entry;
import myra.rule.Rule;
import myra.rule.RuleList;
import myra.rule.RuleSet;

/**
 * The <code>FindRuleSetActivity</code> is responsible for evolving a complete
 * list of (disjunctive) rules using an ACO-based procedure. This activity
 * implements the sequential covering procedure described in Tom Mitchell's
 * Machine Learning book.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class FindRuleSetActivity extends IterativeActivity<RuleList> {
    /**
     * The config key to indicate the use of unordered rules.
     */
    public static final ConfigKey<Boolean> UNORDERED = new ConfigKey<Boolean>();

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
     * The ACO rule factory.
     */
    private LevelRuleFactory factory;

    /**
     * The convergence termination criteria counter.
     */
    private boolean reset;

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
    public FindRuleSetActivity(Graph graph, Dataset dataset) {
        this(graph, dataset, new LevelRuleFactory());
    }

    /**
     * Creates a new <code>FindRuleListActivity</code> object.
     * 
     * @param graph
     *            the construction graph.
     * @param dataset
     *            the current dataset.
     * @param factory
     *            the rule factory.
     */
    public FindRuleSetActivity(Graph graph,
                               Dataset dataset,
                               LevelRuleFactory factory) {
        this(graph, dataset, factory, new LevelPheromonePolicy());
    }

    /**
     * Creates a new <code>FindRuleListActivity</code> object.
     * 
     * @param graph
     *            the construction graph.
     * @param dataset
     *            the current dataset.
     * @param factory
     *            the rule factory.
     * @param policy
     *            the pheromone policy.
     */
    public FindRuleSetActivity(Graph graph,
                               Dataset dataset,
                               LevelRuleFactory factory,
                               LevelPheromonePolicy policy) {
        this.graph = graph;
        this.dataset = dataset;
        this.factory = factory;
        this.policy = policy;
    }

    @Override
    public RuleSet create() {
        Instance[] instances = Instance.newArray(dataset.size());
        Instance.markAll(instances, NOT_COVERED);
        Entry[] heuristic = Entry.deepClone(INITIAL_HEURISTIC);

        RuleSet set = new RuleSet();
        set.setIteration(iteration);

        int available = dataset.size();
        int uncovered = (int) ((dataset.size() * CONFIG.get(UNCOVERED)) + 0.5);

        while (available >= uncovered) {
            if (set.size() > 0) {
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

            Rule rule = factory
                    .create(set.size(), graph, heuristic, dataset, instances);

            available =
                    CONFIG.get(DEFAULT_PRUNER).prune(dataset, rule, instances);

            set.add(rule);

            if (rule.size() == 0) {
                break;
            }

            // marks the instances correctly covered by the current rule as
            // COVERED, so they are not available for the next iterations
            Dataset.markCorrect(dataset,
                                instances,
                                ((Label) rule.getConsequent()).value());
        }

        if (!set.hasDefault()) {
            if (available == 0) {
                Instance.markAll(instances, NOT_COVERED);
            }

            Rule rule = Rule.newInstance();
            rule.apply(dataset, instances);
            CONFIG.get(ASSIGNATOR).assign(dataset, rule, instances);
            set.add(rule);
        }

        // global pruning
        CONFIG.get(DEFAULT_LIST_PRUNER).prune(dataset, set);
        // evaluates the list
        set.setQuality(CONFIG.get(DEFAULT_MEASURE).evaluate(dataset, set));

        return set;
    }

    @Override
    public void initialise() {
        super.initialise();
        policy.initialise(graph);

        reset = true;

        // (initial) heuristic of the whole dataset

        Instance[] instances = Instance.newArray(dataset.size());
        Instance.markAll(instances, RULE_COVERED);

        INITIAL_HEURISTIC = CONFIG.get(DEFAULT_HEURISTIC)
                .compute(graph, dataset, instances);
    }

    @Override
    public boolean terminate() {
        if (stagnation > CONFIG.get(STAGNATION)) {
            if (reset) {
                policy.initialise(graph);
                stagnation = 0;
                reset = false;
            } else {
                return true;
            }
        }

        return super.terminate();
    }

    @Override
    public void update(Archive<RuleList> archive) {
        super.update(archive);
        policy.update(graph, archive.highest());
    }
}