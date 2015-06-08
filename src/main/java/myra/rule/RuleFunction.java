/*
 * RuleFunction.java
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

import myra.Config.ConfigKey;

/**
 * Base class for all rule quality functions.
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class RuleFunction {
    /**
     * The config key for the default rule function instance.
     */
    public final static ConfigKey<RuleFunction> DEFAULT_FUNCTION =
	    new ConfigKey<RuleFunction>();

    /**
     * Evaluates the specified rule.
     * 
     * @param rule
     *            the rule to be evaluated.
     * 
     * @return the quality of the rule.
     */
    public abstract double evaluate(Rule rule);

    /**
     * Returns a confusion matrix based on the covered/uncovered instances
     * information of the rule.
     * 
     * @param rule
     *            a rule.
     * 
     * @return a confusion matrix based on the covered/uncovered instances
     *         information of the rule.
     */
    public BinaryConfusionMatrix fill(Rule rule) {
	BinaryConfusionMatrix m = new BinaryConfusionMatrix();
	int[] covered = rule.covered();
	int[] uncovered = rule.uncovered();

	for (int i = 0; i < covered.length; i++) {
	    if (i == rule.getConsequent()) {
		m.TP += covered[i];
		m.FN += uncovered[i];
	    } else {
		m.FP += covered[i];
		m.TN += uncovered[i];
	    }
	}

	return m;
    }

    /**
     * Struct-like class to represent a binary confusion matrix.
     * 
     * @author Fernando Esteban Barril Otero
     */
    public static class BinaryConfusionMatrix {
	/**
	 * The true-positive value.
	 */
	public double TP = 0;

	/**
	 * The false-positive value.
	 */
	public double FP = 0;

	/**
	 * The false-negative value.
	 */
	public double FN = 0;

	/**
	 * The true-negative value.
	 */
	public double TN = 0;
    }
}