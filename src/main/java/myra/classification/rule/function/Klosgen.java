/*
 * Klosgen.java
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

package myra.classification.rule.function;

import static myra.Config.CONFIG;

import myra.Config.ConfigKey;
import myra.Cost.Maximise;
import myra.classification.rule.ClassificationRule;
import myra.data.Dataset;
import myra.data.Dataset.Instance;

/**
 * The <code>Klosgen</code> class represents a rule quality function that allows
 * to directly trade off the increase in precision and coverage.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Klosgen extends ClassificationRuleFunction {
    /**
     * The config key for the w parameter.
     */
    public static final ConfigKey<Double> W = new ConfigKey<Double>();

    static {
	// default w value
	// see F. Janssen and J. Furnkranz, "On the quest for optimal rule
	// learning heuristics", Machine Learning 78, pp. 343-379, 2010.
	CONFIG.set(W, 0.4323);
    }

    @Override
    public Maximise evaluate(Dataset dataset,
			     ClassificationRule rule,
			     Instance[] instances) {
	BinaryConfusionMatrix m = fill(rule);

	double total = m.TP + m.FP + m.FN + m.TN;
	double precision = m.TP / (m.TP + m.FP);

	return new Maximise(Math.pow((m.TP + m.FP) / total, CONFIG.get(W))
		* (precision - ((m.TP + m.FN) / total)));
    }
}