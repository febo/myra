/*
 * MajorityAssignator.java
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

import static myra.Classifier.RANDOM_GENERATOR;
import static myra.Config.CONFIG;

import java.util.Arrays;

/**
 * An <code>Assignator</code> that assign the majority class observed on the
 * covered instances of a rule.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class MajorityAssignator implements Assignator {
    @Override
    public int assign(Rule rule) {
	int[] covered = rule.covered();
	int[] uncovered = rule.uncovered();

	boolean[] candidates = new boolean[covered.length];
	int size = 0;

	int available = 0;
	int majority = -1;

	for (int i = 0; i < covered.length; i++) {
	    if (majority == -1 || covered[majority] < covered[i]) {
		Arrays.fill(candidates, false);

		majority = i;
		candidates[majority] = true;
		size = 1;
	    } else if (covered[majority] == covered[i]) {
		candidates[i] = true;
		size++;
	    }

	    available += uncovered[i];
	}

	if (size == 1) {
	    rule.setConsequent(majority);
	} else {
	    if (rule.getConsequent() == -1
		    || !candidates[rule.getConsequent()]) {
		double[] probabilities = new double[covered.length];
		double accumulated = 0.0;
		int last = 0;

		for (int i = 0; i < probabilities.length; i++) {
		    if (candidates[i]) {
			probabilities[i] = accumulated + (1.0 / (double) size);
			accumulated = probabilities[i];
			last = i;
		    } else {
			probabilities[i] = 0;
		    }
		}

		probabilities[last] = 1.0;

		double slot = CONFIG.get(RANDOM_GENERATOR).nextDouble();

		for (int i = 0; i < probabilities.length; i++) {
		    if (slot < probabilities[i]) {
			majority = i;
			break;
		    }
		}

		rule.setConsequent(majority);
	    }
	}

	return available;
    }
}