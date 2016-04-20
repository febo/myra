/*
 * StandardDeviationSplit.java
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

package myra.regression.attribute;

import static myra.data.Attribute.GREATER_THAN;
import static myra.data.Attribute.LESS_THAN_OR_EQUAL_TO;
import static myra.data.Dataset.RULE_COVERED;

import java.util.Arrays;

import myra.data.Attribute.Condition;
import myra.data.Dataset;
import myra.data.Dataset.Instance;
import myra.data.IntervalBuilder;

/**
 * This class creates discrete intervals based on the standard deviation of the
 * target values in each interval, similarly to the M5 algorithm. The interval
 * returned is the one that has the lowest standard deviation and, at the same
 * time, provides the higher gain in relation to the distribution of values.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class StandardDeviationSplit extends IntervalBuilder {
    @Override
    public Condition[] multiple(Dataset dataset,
				Instance[] instances,
				int attribute) {
	Pair[] candidates = new Pair[dataset.size()];
	Condition[] distribution = new Condition[2];

	for (int i = 0; i < distribution.length; i++) {
	    distribution[i] = new Condition();
	    distribution[i].length = 0;
	    // 0: mean
	    // 1: variance
	    distribution[i].frequency = new double[2];
	}

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

		    distribution[1].frequency[0] += pair.value;
		    distribution[1].frequency[1] += (pair.value * pair.value);
		    distribution[1].length += pair.weight;

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

	Condition[] conditions = create(candidates,
					0,
					candidates.length,
					distribution,
					IntervalBuilder.minimumCases(dataset,
								     size));

	if (conditions == null) {
	    // no interval was created
	    return null;
	} else {
	    for (Condition c : conditions) {
		c.attribute = attribute;
	    }

	    return conditions;
	}
    }

    @Override
    public Condition single(Dataset dataset,
			    Instance[] instances,
			    int attribute) {
	Condition[] conditions = multiple(dataset, instances, attribute);
	Condition best = null;

	if (conditions != null && conditions.length > 0) {
	    for (Condition c : conditions) {
		if ((best == null) || (c.entropy < best.entropy)
			|| (c.entropy == best.entropy
				&& c.length > best.length)) {
		    best = c;
		}
	    }
	}

	return best;
    }

    /**
     * Returns attribute conditions representing the discrete intervals for the
     * specified attribute that have provide the higher gain in relation to the
     * standard deviation of the target values.
     * 
     * @param candidates
     *            the array os candidate values.
     * @param start
     *            the start index on the candidates array.
     * @param end
     *            the end index on the candidates array.
     * @param distribution
     *            the distribution of values.
     * @param minimum
     *            the minimum interval size allowed.
     * 
     * @return attribute conditions representing discrete intervals for the
     *         specified attribute.
     */
    protected Condition[] create(Pair[] candidates,
				 int start,
				 int end,
				 Condition[] distribution,
				 double minimum) {
	double mean = distribution[1].frequency[0] / distribution[1].length;
	double variance = distribution[1].frequency[1] / distribution[1].length;
	// calculates the standard deviation of the distribution
	final double SD = variance - (mean * mean);

	// determines the best threshold value
	double gain = 0.0;
	int tries = 0;

	// 0: lower interval condition
	// 1: upper interval condition
	Condition[] conditions = new Condition[2];

	for (int i = 0; i < conditions.length; i++) {
	    conditions[i] = new Condition();
	    conditions[i].relation = 0;
	    conditions[i].frequency = new double[2];
	}

	for (int i = (start + 1); i < end; i++) {
	    double weight = candidates[i - 1].weight;
	    double value = candidates[i - 1].value;

	    distribution[0].length += weight;
	    distribution[0].frequency[0] += value;
	    distribution[0].frequency[1] += (value * value);

	    distribution[1].length -= weight;
	    distribution[1].frequency[0] -= value;
	    distribution[1].frequency[1] -= (value * value);

	    if (candidates[i - 1].value < candidates[i].value) {
		if ((distribution[0].length >= minimum)
			&& (distribution[1].length >= minimum)) {
		    tries++;

		    // compute the SD of the intervals
		    double total = 0;
		    double intervalSD = 0;

		    for (int j = 0; j < distribution.length; j++) {
			mean = distribution[j].frequency[0]
				/ distribution[j].length;
			variance = distribution[j].frequency[1]
				/ distribution[j].length;
			// standard deviation of the sample
			double sd = ((variance - (mean * mean))
				/ distribution[j].length)
				/ (distribution[j].length - 1);
			distribution[j].entropy = sd;

			intervalSD += (distribution[j].length * sd);
			total += distribution[j].length;
		    }

		    // determines the gain of the split
		    double intervalGain = SD - (intervalSD / total);

		    if (intervalGain > gain) {
			gain = intervalGain;

			conditions[0].length = distribution[0].length;
			conditions[0].relation = LESS_THAN_OR_EQUAL_TO;
			// standard deviation of the condition
			conditions[0].entropy = (distribution[0].length / total)
				* distribution[0].entropy;
			conditions[0].index = i - 1;
			conditions[0].threshold[0] = candidates[i - 1].value;
			conditions[0].value[0] =
				(candidates[i - 1].value + candidates[i].value)
					/ 2.0;
			// copies the distribution frequency
			System.arraycopy(distribution[0].frequency,
					 0,
					 conditions[0].frequency,
					 0,
					 distribution[0].frequency.length);

			conditions[1].length = distribution[1].length;
			conditions[1].relation = GREATER_THAN;
			// standard deviation of the condition
			conditions[1].entropy = (distribution[1].length / total)
				* distribution[1].entropy;
			conditions[1].index = i - 1;
			conditions[1].threshold[0] = candidates[i - 1].value;
			conditions[1].value[0] =
				(candidates[i - 1].value + candidates[i].value)
					/ 2.0;
			// copies the distribution frequency
			System.arraycopy(distribution[1].frequency,
					 0,
					 conditions[1].frequency,
					 0,
					 distribution[1].frequency.length);
		    }
		}
	    }
	}

	if (conditions[0].relation == 0) {
	    // a condition could not be created
	    return null;
	}

	conditions[0].tries = tries;
	conditions[1].tries = tries;

	return conditions;
    }
}