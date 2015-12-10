/*
 * MEstimate.java
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

package myra.rule.function;

import static myra.Config.CONFIG;

import myra.Config.ConfigKey;
import myra.rule.Rule;
import myra.rule.RuleFunction;

/**
 * The <code>MEstimate</code> class represents a rule quality function that
 * presumes a rule covers {@link MEstimate#M} training examples a priori.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class MEstimate extends RuleFunction {
    /**
     * The config key for the <i>m</i> parameter.
     */
    public static final ConfigKey<Double> M = new ConfigKey<Double>();

    static {
	// default m value
	// see F. Janssen and J. Furnkranz, "On the quest for optimal rule
	// learning heuristics", Machine Learning 78, pp. 343-379, 2010.
	CONFIG.set(M, 22.466);
    }

    @Override
    public double evaluate(Rule rule) {
	BinaryConfusionMatrix m = fill(rule);

	final double mValue = CONFIG.get(M);
	double totalN = m.FP + m.TN;
	double totalP = m.TP + m.FN;

	return (m.TP + (mValue * (totalP / (totalN + totalP))))
		/ (m.TP + m.FP + mValue);
    }
}