/*
 * LaplaceSplit.java
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

package myra.interval;

import static myra.Attribute.GREATER_THAN;
import static myra.Attribute.LESS_THAN_OR_EQUAL_TO;
import static myra.Dataset.RULE_COVERED;
import static myra.interval.C45Split.DELTA;
import static myra.interval.C45Split.PRECISION_10;
import static myra.interval.C45Split.PRECISION_15;

import java.util.Arrays;

import myra.Attribute.Condition;
import myra.Dataset;
import myra.Dataset.Instance;

/**
 * This class creates discrete intervals based on the Laplace accuracy of the
 * intervals.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class LaplaceSplit extends IntervalBuilder {
    @Override
    public Condition[] multiple(Dataset dataset,
				Instance[] instances,
				int attribute) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Condition single(Dataset dataset,
			    Instance[] instances,
			    int attribute) {
	throw new UnsupportedOperationException();
    }

    /**
     * Returns attribute conditions representing the discrete intervals for the
     * specified attribute that have provide the higher Laplace accuracy.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param attribute
     *            the index of the continuous attribute.
     * 
     * @return attribute conditions representing discrete intervals for the
     *         specified attribute.
     */
    public Condition[] multiple(Dataset dataset,
				Instance[] instances,
				int attribute,
				int target) {
	// (1) creates the pairing (value,class) for the uncovered examples

	Pair[] candidates = new Pair[dataset.size()];
	double[] frequency = new double[dataset.classLength()];

	int index = 0;
	double size = 0;

	for (int i = 0; i < dataset.size(); i++) {
	    // the dynamc discretisation only considers the instances covered
	    // by the current rule
	    if (instances[i].flag == RULE_COVERED) {
		double v = dataset.value(i, attribute);

		if (!Double.isNaN(v)) {
		    Pair pair = new Pair();
		    pair.value = v;
		    pair.classValue = dataset.value(i, dataset.classIndex());
		    pair.weight = instances[i].weight;
		    candidates[index] = pair;

		    frequency[(int) pair.classValue] += pair.weight;
		    size += pair.weight;

		    index++;
		}
	    }
	}

	if (index == 0) {
	    // there are no candidate threshold values
	    return null;
	}

	candidates = Arrays.copyOf(candidates, index);
	Arrays.sort(candidates);

	// (2) determines the best threshold value

	final double minimum = IntervalBuilder.minimumCases(dataset, size);
	double accuracy = 0.0;

	// 0: lower interval condition
	// 1: upper interval condition
	Condition[] conditions = new Condition[2];

	for (int i = 0; i < conditions.length; i++) {
	    conditions[i] = new Condition();
	    conditions[i].attribute = attribute;
	    conditions[i].relation = 0;
	    conditions[i].frequency = new double[dataset.classLength()];
	    conditions[i].length = 0;
	}

	double[] intervalFrequency = new double[dataset.classLength()];
	double intervalSize = 0;

	for (int i = 1; i < candidates.length; i++) {
	    double weight = candidates[i - 1].weight;

	    intervalSize += weight;
	    intervalFrequency[(int) candidates[i - 1].classValue] += weight;

	    size -= weight;
	    frequency[(int) candidates[i - 1].classValue] -= weight;

	    // + 1E-5 as it is used by C4.5
	    if (candidates[i - 1].value + DELTA < candidates[i].value) {
		// CORRECTION is used to avoid imprecisions in the double
		// number representation
		if ((intervalSize + PRECISION_10 >= minimum)
			&& (size + PRECISION_10 >= minimum)) {
		    // compute the accuracy of the intervals

		    double lower = (intervalFrequency[target] > 0)
			    ? (intervalFrequency[target] + 1)
				    / (intervalSize + dataset.classLength())
			    : 0.0;

		    double upper =
			    (frequency[target] > 0)
				    ? (frequency[target] + 1)
					    / (size + dataset.classLength())
				    : 0.0;

		    double intervalAccuracy = Math.max(lower, upper);

		    if (((intervalAccuracy - accuracy) > PRECISION_15)
			    || (lower > 0.0 && lower == accuracy
				    && intervalSize > conditions[0].length)
			    || (upper > 0.0 && upper == accuracy
				    && size > conditions[1].length)) {
			accuracy = intervalAccuracy;

			conditions[0].length = intervalSize;
			conditions[0].relation = LESS_THAN_OR_EQUAL_TO;
			conditions[0].entropy = lower;
			conditions[0].threshold[0] = candidates[i - 1].value;
			conditions[0].value[0] =
				(candidates[i - 1].value + candidates[i].value)
					/ 2.0;
			System.arraycopy(intervalFrequency,
					 0,
					 conditions[0].frequency,
					 0,
					 dataset.classLength());

			conditions[1].length = size;
			conditions[1].relation = GREATER_THAN;
			conditions[1].entropy = upper;
			conditions[1].threshold[0] = candidates[i - 1].value;
			conditions[1].value[0] =
				(candidates[i - 1].value + candidates[i].value)
					/ 2.0;
			System.arraycopy(frequency,
					 0,
					 conditions[1].frequency,
					 0,
					 dataset.classLength());
		    }
		}
	    }
	}

	if (conditions[0].relation == 0) {
	    // a condition could not be created
	    return null;
	}

	return conditions;
    }

    /**
     * Returns an attribute condition representing the discrete interval for the
     * specified attribute that has the highest Laplace accuracy.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param attribute
     *            the index of the continuous attribute.
     * 
     * @return an attribute condition representing a discrete interval for the
     *         specified attribute.
     */
    public Condition single(Dataset dataset,
			    Instance[] instances,
			    int attribute,
			    int target) {
	Condition[] conditions =
		multiple(dataset, instances, attribute, target);

	if (conditions != null) {
	    int index = -1;

	    if (conditions[0].entropy > conditions[1].entropy) {
		index = 0;
	    } else if (conditions[0].entropy < conditions[1].entropy) {
		index = 1;
	    } else {
		if (conditions[0].frequency[target] > conditions[1].frequency[target]) {
		    index = 0;
		} else
		    if (conditions[0].frequency[target] < conditions[1].frequency[target]) {
		    index = 1;
		} else {
		    index = (conditions[1].length > conditions[0].length) ? 1
			    : 0;
		}
	    }

	    return conditions[index];
	}

	// this only happens if we are not able to create any condition
	return null;
    }
}