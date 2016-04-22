/*
 * RegressionRule.java
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
import static myra.datamining.Dataset.NOT_COVERED;
import static myra.datamining.Dataset.RULE_COVERED;

import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.datamining.Prediction;
import myra.regression.Real;
import myra.rule.Rule;

/**
 * @author Fernando Esteban Barril Otero
 *
 */
public class RegressionRule extends Rule {
    /**
     * The predicted real value.
     */
    private Real consequent;

    /**
     * Creates a new <code>RegressionRule</code>.
     */
    public RegressionRule() {
	this(0);
    }

    /**
     * Creates a new <code>RegressionRule</code> with the specified capacity.
     * 
     * @param capacity
     *            the allocated size of the rule.
     */
    public RegressionRule(int capacity) {
	super(capacity);
    }

    @Override
    public int apply(Dataset dataset, Instance[] instances) {
	int total = 0;

	for (int i = 0; i < dataset.size(); i++) {
	    if (instances[i].flag != COVERED) {
		if (covers(dataset, i)) {
		    total++;
		    instances[i].flag = RULE_COVERED;
		} else {
		    instances[i].flag = NOT_COVERED;
		}
	    }
	}

	return total;
    }

    @Override
    public void setConsequent(Prediction prediction) {
	if (!(prediction instanceof Real)) {
	    throw new IllegalArgumentException("Invalid predicted value: "
		    + prediction);
	}

	consequent = (Real) prediction;
    }

    @Override
    public Real getConsequent() {
	return consequent;
    }

    /**
     * Returns always <code>true</code>.
     */
    @Override
    public boolean isDiverse() {
	return true;
    }
}