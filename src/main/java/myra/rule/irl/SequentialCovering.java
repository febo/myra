/*
 * SequentialCovering.java
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
import static myra.rule.Assignator.ASSIGNATOR;


import myra.Config.ConfigKey;
import myra.datamining.Dataset;
import myra.datamining.Model;
import myra.datamining.Dataset.Instance;
import myra.Scheduler;
import myra.rule.Graph;
import myra.rule.GraphFactory;
import myra.rule.Rule;
import myra.rule.RuleList;

/**
 * This class represents a trditional sequential covering strategy to create a
 * list of rules.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class SequentialCovering {
    /**
     * The config key for the number of uncovered instances.
     */
    public final static ConfigKey<Integer> UNCOVERED = new ConfigKey<Integer>();

    public Model train(Dataset dataset) {
	final int uncovered = CONFIG.get(UNCOVERED);
	Instance[] instances = Instance.newArray(dataset.size());
	Instance.markAll(instances, NOT_COVERED);

	Graph graph = (Graph)GraphFactory.newInstance(dataset);

	RuleList discovered = new RuleList();
	int available = dataset.size();

	Scheduler<Rule> scheduler = Scheduler.newInstance(1);

	while (available >= uncovered) {
	    FindRuleActivity activity =
		    new FindRuleActivity(graph, instances, dataset);

	    // discovers one rule using an ACO procedure

	    scheduler.setActivity(activity);
	    scheduler.run();

	    Rule best = activity.getBest();
	    best.apply(dataset, instances);

	    // adds the rule to the list
	    discovered.add(best);

	    // marks the instances covered by the current rule as
	    // COVERED, so they are not available for the next
	    // iterations
	    available = Dataset.markCovered(instances);
	}

	if (!discovered.hasDefault()) {
	    // adds a default rule to the list

	    if (available == 0) {
		Instance.markAll(instances, NOT_COVERED);
	    }

	    Rule rule = Rule.newInstance();
	    rule.apply(dataset, instances);
	    CONFIG.get(ASSIGNATOR).assign(dataset, rule, instances);
	    discovered.add(rule);
	}

	return discovered;
    }
}