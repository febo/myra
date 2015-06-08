/*
 * BoundarySplit.java
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
import myra.Attribute.Condition;

/**
 * This class creates discrete intervals based on the entropy measure, similar
 * to the C4.5 algorithm. The interval returned is the one that has the lowest
 * entropy and, at the same time, provides the higher entropy gain in relation
 * to the distribution of values. This implementation only consider boundary
 * threshold values (values occuring between different class values).
 * 
 * @author Fernando Esteban Barril Otero
 */
public class BoundarySplit extends C45Split {
    @Override
    protected Condition[] create(Pair[] candidates,
				 int start,
				 int end,
				 double[] frequency,
				 double size,
				 double minimum) {
	// calculates the entropy of the distribution

	double entropy = 0.0;

	for (int i = 0; i < frequency.length; i++) {
	    if (frequency[i] > 0) {
		double p = frequency[i] / size;
		entropy -= (p * (Math.log(p) / Math.log(2.0)));
	    }
	}

	// determines the best threshold value

	double gain = 0.0;

	// 0: lower interval condition
	// 1: upper interval condition
	Condition[] conditions = new Condition[2];

	for (int i = 0; i < conditions.length; i++) {
	    conditions[i] = new Condition();
	    conditions[i].relation = 0;
	    conditions[i].frequency = new double[frequency.length];
	}

	double[] intervalFrequency = new double[frequency.length];
	double intervalSize = 0;
	double total = size;
	int tries = 0;
	boolean[] evaluated = new boolean[candidates.length];

	for (int i = (start + 1); i < end; i++) {
	    double weight = candidates[i - 1].weight;

	    intervalSize += weight;
	    intervalFrequency[(int) candidates[i - 1].classValue] += weight;

	    size -= weight;
	    frequency[(int) candidates[i - 1].classValue] -= weight;

	    if (candidates[i - 1].classValue != candidates[i].classValue) {
		if (candidates[i - 1].value == candidates[i].value) {
		    // skip backwards

		    double[] lowerFrequency = new double[frequency.length];
		    System.arraycopy(intervalFrequency,
				     0,
				     lowerFrequency,
				     0,
				     lowerFrequency.length);
		    double lowerSize = intervalSize;

		    double[] upperFrequency = new double[frequency.length];
		    System.arraycopy(frequency,
				     0,
				     upperFrequency,
				     0,
				     upperFrequency.length);
		    double upperSize = size;

		    int threshold = i;

		    while ((threshold > 1)
			    && (candidates[threshold
				    - 1].value == candidates[threshold].value)) {
			weight = candidates[threshold - 1].weight;
			int c = (int) candidates[threshold - 1].classValue;

			lowerSize -= weight;
			lowerFrequency[c] -= weight;

			upperSize += weight;
			upperFrequency[c] += weight;

			threshold--;
		    }

		    if (!evaluated[threshold - 1]
			    && (lowerSize + PRECISION_10 >= minimum)
			    && (upperSize + PRECISION_10 >= minimum)) {
			evaluated[threshold - 1] = true;
			tries++;

			gain = entropy(candidates,
				       threshold,
				       entropy,
				       gain,
				       total,
				       conditions,
				       lowerFrequency,
				       lowerSize,
				       upperFrequency,
				       upperSize);
		    }

		    // skip forward

		    System.arraycopy(intervalFrequency,
				     0,
				     lowerFrequency,
				     0,
				     lowerFrequency.length);
		    lowerSize = intervalSize;

		    System.arraycopy(frequency,
				     0,
				     upperFrequency,
				     0,
				     upperFrequency.length);
		    upperSize = size;

		    threshold = i;

		    while ((threshold < candidates.length)
			    && (candidates[threshold
				    - 1].value == candidates[threshold].value)) {
			threshold++;
			weight = candidates[threshold - 1].weight;
			int c = (int) candidates[threshold - 1].classValue;

			lowerSize += weight;
			lowerFrequency[c] += weight;

			upperSize -= weight;
			upperFrequency[c] -= weight;
		    }

		    if (!evaluated[threshold - 1]
			    && (lowerSize + PRECISION_10 >= minimum)
			    && (upperSize + PRECISION_10 >= minimum)) {
			evaluated[threshold - 1] = true;
			tries++;

			gain = entropy(candidates,
				       threshold,
				       entropy,
				       gain,
				       total,
				       conditions,
				       lowerFrequency,
				       lowerSize,
				       upperFrequency,
				       upperSize);
		    }
		}
		// the boundary point falls between two different values
		else if (!evaluated[i - 1]
			&& (intervalSize + PRECISION_10 >= minimum)
			&& (size + PRECISION_10 >= minimum)) {
		    evaluated[i - 1] = true;
		    tries++;

		    gain = entropy(candidates,
				   i,
				   entropy,
				   gain,
				   total,
				   conditions,
				   intervalFrequency,
				   intervalSize,
				   frequency,
				   size);
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

    /**
     * Computes the entropy of the intervals. This method updates the conditions
     * array if any of the intervals has a higher gain than the current best.
     * 
     * @param candidates
     *            the cadidate values.
     * @param index
     *            the index of the threshold value.
     * @param entropy
     *            the entropy of the entire distribution.
     * @param gain
     *            the current best gain value.
     * @param total
     *            the total length of the distribution.
     * @param conditions
     *            the discrete conditions.
     * @param lowerFrequency
     *            the lower interval frequency.
     * @param lowerSize
     *            the lower interval size.
     * @param upperFrequency
     *            the upper interval frequency.
     * @param upperSize
     *            the upper interval size.
     * 
     * @return the gain value of the interval.
     */
    protected double entropy(Pair[] candidates,
			     int index,
			     double entropy,
			     double gain,
			     double total,
			     Condition[] conditions,
			     double[] lowerFrequency,
			     double lowerSize,
			     double[] upperFrequency,
			     double upperSize) {
	// compute the entropy of the intervals

	double lowerEntropy = 0.0;
	double upperEntropy = 0.0;

	int lowerDiversity = 0;
	int upperDiversity = 0;

	for (int j = 0; j < lowerFrequency.length; j++) {
	    if (upperFrequency[j] > 0) {
		double p = upperFrequency[j] / upperSize;
		upperEntropy -= (p * (Math.log(p) / Math.log(2.0)));
		upperDiversity++;
	    }

	    if (lowerFrequency[j] > 0) {
		double p = lowerFrequency[j] / lowerSize;
		lowerEntropy -= (p * (Math.log(p) / Math.log(2.0)));
		lowerDiversity++;
	    }
	}

	// determines the gain of the split

	double intervalGain =
		entropy - ((lowerSize / total) * lowerEntropy)
			- ((upperSize / total) * upperEntropy);

	if ((intervalGain - gain) > PRECISION_15) {
	    gain = intervalGain;

	    conditions[0].length = lowerSize;
	    conditions[0].relation = LESS_THAN_OR_EQUAL_TO;
	    conditions[0].entropy = lowerEntropy;
	    conditions[0].diversity = lowerDiversity;
	    conditions[0].index = index - 1;
	    conditions[0].threshold[0] = candidates[index - 1].value;
	    conditions[0].value[0] =
		    (candidates[index - 1].value + candidates[index].value)
			    / 2.0;
	    // copies the distribution frequency
	    System.arraycopy(lowerFrequency,
			     0,
			     conditions[0].frequency,
			     0,
			     lowerFrequency.length);

	    conditions[1].length = upperSize;
	    conditions[1].relation = GREATER_THAN;
	    conditions[1].entropy = upperEntropy;
	    conditions[1].diversity = upperDiversity;
	    conditions[1].index = index - 1;
	    conditions[1].threshold[0] = candidates[index - 1].value;
	    conditions[1].value[0] =
		    (candidates[index - 1].value + candidates[index].value)
			    / 2.0;
	    // copies the distribution frequency
	    System.arraycopy(upperFrequency,
			     0,
			     conditions[1].frequency,
			     0,
			     upperFrequency.length);
	}

	return gain;
    }
}