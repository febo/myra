/*
 * Entropy.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2019 Fernando Esteban Barril Otero
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

package myra.classification.rule.function;

import myra.Cost.Maximise;
import myra.classification.rule.ClassificationRule;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;

/**
 * @author Fernando Esteban Barril Otero
 */
public class Entropy extends ClassificationRuleFunction {

    @Override
    public Maximise evaluate(Dataset dataset,
			     ClassificationRule rule,
			     Instance[] instances) {
	int[] covered = rule.covered();
	int total = 0;

	// calculate the total number of covered instances

	for (int i = 0; i < covered.length; i++) {
	    total += covered[i];
	}

	// calculate the entropy

	double entropy = 0.0;

	for (int i = 0; i < dataset.classLength(); i++) {
	    if (covered[i] > 0) {
		double p = covered[i] / (double) total;
		entropy += (p * (Math.log(p) / Math.log(2.0)));
	    }
	}

	return new Maximise(-entropy);
    }

}
