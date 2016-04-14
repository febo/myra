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

package myra.classification.rule.unordered;

import static myra.Config.CONFIG;
import static myra.data.Dataset.NOT_COVERED;
import static myra.data.Dataset.RULE_COVERED;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.ListMeasure.DEFAULT_MEASURE;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.pittsburgh.FindRuleListActivity.UNCOVERED;

import myra.Archive;
import myra.Config.ConfigKey;
import myra.classification.Label;
import myra.classification.rule.ClassificationRule;
import myra.classification.rule.MajorityAssignator;
import myra.classification.rule.function.FunctionSelector;
import myra.data.Dataset;
import myra.data.Dataset.Instance;
import myra.IterativeActivity;
import myra.rule.Graph;
import myra.rule.Rule;
import myra.rule.RuleFunction;
import myra.rule.RuleList;
import myra.rule.RuleSet;
import myra.rule.Graph.Entry;
import myra.rule.pittsburgh.LevelPheromonePolicy;

public class FindRuleSetActivity extends IterativeActivity<RuleList> {
    /**
     * The config key to indicate if the dynamic function selector should be
     * used or not.
     */
    public final static ConfigKey<Boolean> DYNAMIC_FUNCTION = new ConfigKey<>();

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
     * The rule quality function selector.
     */
    private FunctionSelector selector;

    /**
     * The convergence termination criteria counter.
     */
    private boolean reset;

    /**
     * Creates a new <code>FindRuleListActivity</code> object.
     * 
     * @param graph
     *            the construction graph.
     * @param training
     *            the current dataset.
     */
    public FindRuleSetActivity(Graph graph, Dataset training) {
	this.graph = graph;
	this.dataset = training;
    }

    @Override
    public RuleSet create() {
	Instance[] instances = Instance.newArray(dataset.size());
	RuleSet ruleSet = new RuleSet();
	ruleSet.setIteration(iteration);

	// creates rules for each class

	for (int j = 0; j < dataset.classLength(); j++) {
	    Instance.markAll(instances, NOT_COVERED);

	    final int total = dataset.size(j);
	    int available = total;

	    int uncovered = (int) ((available * CONFIG.get(UNCOVERED)) + 0.5);

	    while (available > 0 && available >= uncovered) {
		// the heuristic only takes into account the instances
		// covered by a rule
		Instance.mark(instances, NOT_COVERED, RULE_COVERED);
		Entry[] heuristic = CONFIG.get(DEFAULT_HEURISTIC)
			.compute(graph, dataset, instances, j);
		Instance.mark(instances, RULE_COVERED, NOT_COVERED);

		ClassificationRule rule =
			FixedClassRuleFactory.create(ruleSet.size(),
						     graph,
						     heuristic,
						     dataset,
						     instances,
						     new Label(j));

		RuleFunction function = null;

		if (CONFIG.get(DYNAMIC_FUNCTION)) {
		    rule.setFunction(selector.select(ruleSet.size()));
		    function = selector.get(rule.getFunction());
		} else {
		    function = CONFIG.get(DEFAULT_FUNCTION);
		}

		available = CONFIG.get(DEFAULT_PRUNER)
			.prune(dataset, rule, instances, function);

		if (rule.size() == 0) {
		    break;
		} else {
		    ruleSet.add(rule);
		}

		// marks the instances correctly covered by the current
		// rule as COVERED, so they are not available for the
		// next iterations

		Dataset.markCorrect(dataset,
				    instances,
				    rule.getConsequent().value());
	    }
	}

	// creates a default rule predicting the majority class

	Instance.markAll(instances, NOT_COVERED);
	Rule rule = Rule.newInstance();
	rule.apply(dataset, instances);
	new MajorityAssignator().assign(rule);

	ruleSet.add(rule);
	ruleSet.apply(dataset);

	ruleSet.setQuality(CONFIG.get(DEFAULT_MEASURE).evaluate(dataset,
								ruleSet));

	return ruleSet;
    }

    @Override
    public void initialise() {
	super.initialise();

	policy = new LevelPheromonePolicy();
	policy.initialise(graph);
	selector = new FunctionSelector();

	reset = true;
    }

    @Override
    public boolean terminate() {
	if (stagnation > CONFIG.get(STAGNATION)) {
	    if (reset) {
		policy.initialise(graph);
		selector = new FunctionSelector();

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

	RuleSet candidate = (RuleSet) archive.highest();
	policy.update(graph, candidate);

	if (CONFIG.get(DYNAMIC_FUNCTION)) {
	    selector.update(candidate, policy);
	}
    }

    @Override
    public RuleSet getBest() {
	// TODO Auto-generated method stub
	return (RuleSet) super.getBest();
    }
}