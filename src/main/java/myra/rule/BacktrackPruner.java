/*
 * BacktrackPruner.java
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

package myra.rule;

import static myra.Config.CONFIG;
import static myra.rule.Assignator.ASSIGNATOR;

import myra.Cost;
import myra.data.Dataset;
import myra.data.Dataset.Instance;
import myra.rule.Rule.Term;

/**
 * This class represents the "threshold-aware" prune procedure which removes the
 * last term of the rule until the rule quality improves.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class BacktrackPruner extends Pruner {
    @Override
    public int prune(Dataset dataset,
		     Rule rule,
		     Instance[] instances,
		     RuleFunction function) {
	Assignator assignator = CONFIG.get(ASSIGNATOR);
	int available = assignator.assign(rule);

	Cost best = function.evaluate(rule);
	Term last = null;

	while (rule.size() > 1) {
	    last = rule.pop();
	    rule.apply(dataset, instances);
	    int pruned = assignator.assign(rule);

	    Cost current = function.evaluate(rule);

	    if (current.compareTo(best) > 0) {
		available = pruned;
		best = current;
	    } else {
		rule.push(last);
		rule.apply(dataset, instances);
		available = assignator.assign(rule);

		break;
	    }
	}

	rule.compact();
	rule.setQuality(best);

	return available;
    }
}