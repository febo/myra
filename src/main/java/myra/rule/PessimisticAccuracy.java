/*
 * PessimisticAccuracy.java
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

import myra.Dataset;
import myra.util.Stats;

/**
 * Measure based on C4.5 error estimation.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class PessimisticAccuracy implements ListMeasure {
    @Override
    public double evaluate(Dataset dataset, RuleList list) {
	if (list.size() == 0) {
	    return 0.0;
	}

	double[] coverage = new double[list.size()];
	double[] errors = new double[list.size()];

	// coverage and errors of each rule

	for (int i = 0; i < coverage.length; i++) {
	    for (int j = 0; j < dataset.classLength(); j++) {
		coverage[i] += list.rules()[i].covered()[j];

		if (j != list.rules()[i].getConsequent()) {
		    errors[i] += list.rules()[i].covered()[j];
		}
	    }
	}

	// predicted errors of the list (sum of the estimated errors
	// of each rule)

	double predicted = 0;

	for (int i = 0; i < coverage.length; i++) {
	    predicted += (errors[i] + Stats.errors(coverage[i], errors[i]));
	}

	return 1.0 - (predicted / (double) dataset.size());
    }
}