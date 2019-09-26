/*
 * MeanAssignator.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2016 Fernando Esteban Barril Otero
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

package myra.regression.rule;

import static myra.datamining.Dataset.COVERED;
import static myra.datamining.Dataset.RULE_COVERED;

import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.regression.Real;
import myra.rule.Assignator;
import myra.rule.Rule;

/**
 * A <code>MeanAssignator</code> that assign the mean value observed on the
 * covered instances of a rule.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class MeanAssignator implements Assignator {
    @Override
    public int assign(Dataset dataset, Rule rule, Instance[] instances) {
	double total = 0;
	int count = 0;
	int available = 0;

	for (int i = 0; i < instances.length; i++) {
	   if (instances[i].flag != COVERED) {
		available++;

		if (instances[i].flag == RULE_COVERED) {
		    total += dataset.value(i, dataset.classIndex());
		    count++;
		}
	    }
	}

	rule.setConsequent(new Real(total / count));

	return available - count;
    }
}