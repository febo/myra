/*
 * MinimalSplit.java
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

package myra.classification.attribute;

import static myra.data.Attribute.GREATER_THAN;
import static myra.data.Attribute.LESS_THAN_OR_EQUAL_TO;

import myra.data.Attribute.Condition;

/**
 * This class creates discrete intervals based on the entropy measure. The
 * interval returned is the one that has the lowest entropy.
 * 
 * <p>
 * <b>Note:</b> This implementation differ from {@link C45Split} in the sense
 * that it tried to minimize the entropy on a single interval, instead of the
 * entropy gain in relation to the distribution of values. Therefore, it is not
 * suited in cases where multiple intervals are needed.
 * </p>
 * 
 * @author Fernando Esteban Barril Otero
 */
public class MinimalSplit extends C45Split {
    @Override
    protected Condition[] create(Pair[] candidates,
				 int start,
				 int end,
				 double[] frequency,
				 double size,
				 double minimum) {
	// 0: entropy
	// 1: length
	double[] current = { Double.MAX_VALUE, 0.0 };

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
	int tries = 0;

	for (int i = (start + 1); i < end; i++) {
	    double weight = candidates[i - 1].weight;

	    intervalSize += weight;
	    intervalFrequency[(int) candidates[i - 1].classValue] += weight;

	    size -= weight;
	    frequency[(int) candidates[i - 1].classValue] -= weight;

	    if (candidates[i - 1].value + DELTA < candidates[i].value) {
		if ((intervalSize >= minimum) || (size >= minimum)
			&& (intervalSize > 0) && (size > 0)) {
		    tries++;

		    // compute the entropy of the intervals

		    double lowerEntropy = 0.0;
		    double upperEntropy = 0.0;
		    int lowerDiversity = 0;
		    int upperDiversity = 0;

		    for (int j = 0; j < frequency.length; j++) {
			if (frequency[j] > 0) {
			    double p = frequency[j] / size;
			    upperEntropy -= (p * log2(p));
			    upperDiversity++;
			}

			if (intervalFrequency[j] > 0) {
			    double p = intervalFrequency[j] / intervalSize;
			    lowerEntropy -= (p * log2(p));
			    lowerDiversity++;
			}
		    }

		    boolean updated = false;

		    if ((current[0] > lowerEntropy
			    || (current[0] == lowerEntropy
				    && current[1] < intervalSize))
			    && intervalSize >= minimum) {
			updated = true;

			current[0] = lowerEntropy;
			current[1] = intervalSize;
		    }

		    if ((current[0] > upperEntropy
			    || (current[0] == upperEntropy
				    && current[1] < size))
			    && size >= minimum) {
			updated = true;

			current[0] = upperEntropy;
			current[1] = size;
		    }

		    if (updated) {
			conditions[0].length = intervalSize;
			conditions[0].relation = LESS_THAN_OR_EQUAL_TO;
			conditions[0].entropy = lowerEntropy;
			conditions[0].diversity = lowerDiversity;
			conditions[0].index = i - 1;
			conditions[0].threshold[0] = candidates[i - 1].value;
			conditions[0].value[0] =
				(candidates[i - 1].value + candidates[i].value)
					/ 2.0;
			// copies the distribution frequency
			System.arraycopy(intervalFrequency,
					 0,
					 conditions[0].frequency,
					 0,
					 intervalFrequency.length);

			conditions[1].length = size;
			conditions[1].relation = GREATER_THAN;
			conditions[1].entropy = upperEntropy;
			conditions[1].diversity = upperDiversity;
			conditions[1].index = i - 1;
			conditions[1].threshold[0] = candidates[i - 1].value;
			conditions[1].value[0] =
				(candidates[i - 1].value + candidates[i].value)
					/ 2.0;
			// copies the distribution frequency
			System.arraycopy(frequency,
					 0,
					 conditions[1].frequency,
					 0,
					 frequency.length);
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