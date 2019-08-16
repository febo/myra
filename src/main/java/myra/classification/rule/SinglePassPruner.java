/*
 * SinglePassPruner.java
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

package myra.classification.rule;

import static myra.Config.CONFIG;
import static myra.datamining.Dataset.COVERED;
import static myra.datamining.IntervalBuilder.MINIMUM_CASES;
import static myra.rule.Assignator.ASSIGNATOR;

import myra.Cost;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.rule.Assignator;
import myra.rule.Pruner;
import myra.rule.Rule;
import myra.rule.Rule.Term;
import myra.rule.RuleFunction;

/**
 * This class represents a pruning procedure that evaluates all terms at once
 * (single pass over the dataset) and removes terms, from the end to the start
 * of the rule, as long as the quality improves.
 * <p>
 * Note that the rule might be empty at the end of the pruning, meaning that no
 * term covered the minimum number of instances required.
 * </p>
 * 
 * @author Fernando Esteban Barril Otero
 */
public class SinglePassPruner extends Pruner {
    @Override
    public int prune(Dataset dataset,
		     Rule rule,
		     Instance[] instances,
		     RuleFunction function) {
	Term[] terms = rule.terms();
	Coverage[] coverage = new Coverage[terms.length];

	for (int j = 0; j < coverage.length; j++) {
	    coverage[j] = new Coverage(dataset.classLength());
	}

	// (1) determines the coverage of each term

	int start = 0;

	while (start < coverage.length) {
	    for (int i = 0; i < dataset.size(); i++) {
		// only considers instances not covered
		if (instances[i].flag != COVERED) {
		    int c = (int) dataset.value(i, dataset.classIndex());

		    for (int j = start; j < terms.length; j++) {
			if (terms[j].isEnabeld()) {
			    Condition condition = terms[j].condition();
			    double v = dataset.value(i, condition.attribute);

			    if (condition.satisfies(v)) {
				coverage[j].covered[c]++;
			    } else {
				coverage[j].uncovered[c]++;
				// stops checking the remaining terms
				break;
			    }
			}
		    }
		}
	    }

	    // checks that the first term of the rule cover the minimum
	    // number of cases, otherwise disables it and repeat the
	    // coverage of the rule
	    if (coverage[start].total() < CONFIG.get(MINIMUM_CASES)) {
		terms[start].setEnabeld(false);
		start++;
		// reset coverage for all terms
		for (int j = 0; j < coverage.length; j++) {
		    coverage[j] = new Coverage(dataset.classLength());
		}
	    } else {
		// when the rule covers the minimum number of cases, stop the
		// coverage test
		break;
	    }
	}

	// (2) determines the quality of each term

	Assignator assignator = CONFIG.get(ASSIGNATOR);
	ClassificationRule cRule = (ClassificationRule) rule;

	int selected = -1;
	Cost best = null;

	for (int i = start; i < coverage.length; i++) {
	    // the rule must cover a minimum number of cases, therefore
	    // only terms that cover more than the limit are considered
	    if (coverage[i].total() >= CONFIG.get(MINIMUM_CASES)) {
		cRule.covered(coverage[i].covered);
		cRule.uncovered(coverage[i].uncovered);
		assignator.assign(dataset, rule, instances);

		Cost current = function.evaluate(dataset, rule, instances);

		if (best == null || current.compareTo(best) >= 0) {
		    selected = i;
		    best = current;
		}
	    } else {
		// stops the procedure, since any remaining term will not
		// cover the minimum number
		break;
	    }
	}

	// (3) disables and removes unused terms

	for (int i = selected + 1; i < terms.length; i++) {
	    terms[i].setEnabeld(false);
	}

	rule.setQuality(best);
	rule.compact();
	rule.apply(dataset, instances);

	return assignator.assign(dataset, rule, instances);
    }

    /**
     * Class to store the coverage information of a term.
     * 
     * @author Fernando Esteban Barril Otero
     */
    private static class Coverage {
	/**
	 * Covered instances information.
	 */
	int[] covered;

	/**
	 * Uncovered instances information.
	 */
	int[] uncovered;

	/**
	 * Default constructor.
	 * 
	 * @param length
	 *            the number of classes.
	 */
	Coverage(int length) {
	    covered = new int[length];
	    uncovered = new int[length];
	}

	/**
	 * Returns the total number of covered examples.
	 * 
	 * @return the total number of covered examples.
	 */
	int total() {
	    int total = 0;

	    for (int i = 0; i < covered.length; i++) {
		total += covered[i];
	    }

	    return total;
	}
    }
}