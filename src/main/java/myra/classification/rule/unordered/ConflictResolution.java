/*
 * ConflictResolution.java
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

import myra.classification.Label;
import myra.classification.rule.ClassificationRule;
import myra.datamining.Dataset;

/**
 * The <code>ConflictResolution</code> enum represent different conflict
 * resolution strategies used to make the final prediction when a unordered list
 * of rules is used. In an unordered list of rules, multiple rules predicting
 * different class values can cover an instance.
 * 
 * @author Fernando Esteban Barril Otero
 */
public enum ConflictResolution {
    /**
     * The frequent-class resolution strategy. It consist in adding up the class
     * frequency of the rules that cover the instance and then predict the
     * majority class of this sum.
     */
    FREQUENT_CLASS() {
	@Override
	public Label resolve(Dataset dataset,
			     ClassificationRule[] rules,
			     boolean[] active) {
	    int[] frequency = new int[dataset.classLength()];

	    for (int i = 0; i < active.length; i++) {
		if (active[i]) {
		    for (int j = 0; j < frequency.length; j++) {
			frequency[j] += rules[i].covered()[j];
		    }
		}
	    }

	    // finds the majority class

	    int majority = -1;

	    for (int i = 0; i < frequency.length; i++) {
		if (frequency[i] > 0 && (majority == -1
			|| frequency[i] > frequency[majority])) {
		    majority = i;
		}
	    }

	    return new Label(majority);
	}
    },
    /**
     * The confidence resolution strategy. It consist in using the most accurate
     * rule that covers the instance.
     */
    CONFIDENCE() {
	@Override
	public Label resolve(Dataset dataset,
			     ClassificationRule[] rules,
			     boolean[] active) {
	    int predicted = -1;
	    double best = 0.0;

	    for (int i = 0; i < active.length; i++) {
		if (active[i]) {
		    int[] frequency = rules[i].covered();
		    int total = 0;

		    for (int j = 0; j < frequency.length; j++) {
			total += frequency[j];
		    }

		    double laplace =
			    (frequency[rules[i].getConsequent().value()] + 1)
				    / (double) (total + dataset.classLength());

		    if (laplace > best) {
			predicted = rules[i].getConsequent().value();
			best = laplace;
		    }
		}
	    }

	    return new Label(predicted);
	}
    },
    /**
     * The confidence resolution strategy. It consist in using the most accurate
     * rule that covers the instance.
     */
    WEIGHTED_FREQUENCY() {
	@Override
	public Label resolve(Dataset dataset,
			     ClassificationRule[] rules,
			     boolean[] active) {
	    double[] frequency = new double[dataset.classLength()];

	    for (int i = 0; i < active.length; i++) {
		if (active[i]) {
		    int[] covered = rules[i].covered();
		    int total = 0;

		    for (int j = 0; j < covered.length; j++) {
			total += covered[j];
		    }

		    double laplace =
			    (covered[rules[i].getConsequent().value()] + 1)
				    / (double) (total + dataset.classLength());

		    for (int j = 0; j < covered.length; j++) {
			frequency[j] += (laplace * covered[j]);
		    }
		}
	    }

	    int majority = -1;

	    for (int i = 0; i < frequency.length; i++) {
		if (frequency[i] > 0 && (majority == -1
			|| frequency[i] > frequency[majority])) {
		    majority = i;
		}
	    }

	    return new Label(majority);
	}
    };

    /**
     * Returns a class value prediction based on the rules that covers the
     * instance.
     * 
     * @param dataset
     *            the current dataset.
     * @param rules
     *            the set of rules.
     * @param active
     *            indicates the rules that are covering the instance (
     *            <code>true</code>=covers).
     * 
     * @return a class value prediction based on the rules that covers the
     *         instance.
     */
    public abstract Label resolve(Dataset dataset,
				  ClassificationRule[] rules,
				  boolean[] active);
}