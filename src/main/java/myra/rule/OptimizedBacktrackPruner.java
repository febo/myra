/*
 * OptimizedBacktrackPruner.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2017 Fernando Esteban Barril Otero
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

package myra.rule;

import static myra.Config.CONFIG;
import static myra.rule.Assignator.ASSIGNATOR;

import myra.Cost;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.rule.Rule.Term;

/**
 * @author amh58
 *
 */
public class OptimizedBacktrackPruner extends Pruner {
    @Override
    public int prune(Dataset dataset,
		     Rule rule,
		     Instance[] instances,
		     RuleFunction function) {
	Assignator assignator = CONFIG.get(ASSIGNATOR);
	int available = assignator.assign(dataset, rule, instances);

	Cost best = function.evaluate(dataset, rule, instances);
	Term last = null;

	while (rule.size() > 1) {
	    last = rule.pop();
	    
	    int pruned = assignator.assign(dataset, rule, instances);

	    Cost current = function.evaluate(dataset, rule, instances);

	    if (current.compareTo(best) >= 0) {
		available = pruned;
		best = current;
	    } else {
		rule.push(last);
		rule.apply(dataset, instances);
		available = assignator.assign(dataset, rule, instances);

		break;
	    }
	}

	rule.compact();
	rule.setQuality(best);

	return available;
    }
}