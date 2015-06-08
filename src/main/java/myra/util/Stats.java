/*
 * Stats.java
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

package myra.util;

/**
 * This class provides utility methods to compute C4.5 error-based measure.
 * 
 * @author Fernando Esteban Barril Otero
 */
public final class Stats {
    /**
     * The values used to compute the coefficient value.
     */
    private final static double values[] = { 0,
					     0.001,
					     0.005,
					     0.01,
					     0.05,
					     0.10,
					     0.20,
					     0.40,
					     1.00 };

    /**
     * The deviation used to compute the coefficient value.
     */
    private final static double deviation[] = { 4.0,
						3.09,
						2.58,
						2.33,
						1.65,
						1.28,
						0.84,
						0.25,
						0.00 };

    /**
     * The confidence limit.
     */
    private final static double CF = 0.25;

    /**
     * Coefficient value.
     */
    private static double coefficient = Double.NaN;

    static {
	// from C4.5's stats.c: "Compute and retain the coefficient value,
	// interpolating from the values in values and deviation"

	int i = 0;

	while (CF > values[i]) {
	    i++;
	}

	coefficient =
		deviation[i - 1] + (deviation[i] - deviation[i - 1])
			* (CF - values[i - 1]) / (values[i] - values[i - 1]);
	coefficient = coefficient * coefficient;
    }

    /**
     * Compute the additional errors if the error rate increases to the upper
     * limit of the confidence level.
     * 
     * @param total
     *            the total number of predictions.
     * @param errors
     *            the number of errors observed.
     * 
     * @return the estimate error rate value.
     */
    public static double errors(double total, double errors) {
	if (errors < 1E-6) {
	    return total * (1 - Math.exp(Math.log(CF) / total));
	} else if (errors < 0.9999) {
	    double v = total * (1 - Math.exp(Math.log(CF) / total));
	    return v + errors * (errors(total, 1.0) - v);
	} else if (errors + 0.5 >= total) {
	    return 0.67 * (total - errors);
	} else {
	    double pr =
		    (errors + 0.5 + coefficient / 2
			    + Math.sqrt(coefficient
				    * ((errors + 0.5)
					    * (1 - (errors + 0.5) / total)
					    + coefficient / 4)))
			    / (total + coefficient);

	    return (total * pr - errors);
	}
    }
}