/*
 * Fmeasure.java
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

/**
 * The <code>Fmeasure</code> class represents a rule quality function based on
 * the Precision and Recall measures.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Fmeasure extends ClassificationRuleFunction {
    /**
     * The config key for the <i>beta</i> parameter.
     */
    public static final ConfigKey<Double> BETA = new ConfigKey<Double>();

    static {
	// default beta value
	// see F. Janssen and J. Furnkranz, "On the quest for optimal rule
	// learning heuristics", Machine Learning 78, pp. 343-379, 2010.
	CONFIG.set(BETA, 0.5);
    }

    @Override
    public Maximise evaluate(ClassificationRule rule) {
	BinaryConfusionMatrix m = fill(rule);
	final double beta = CONFIG.get(BETA);

	double precision = m.TP / (m.TP + m.FP);
	double recall = m.TP / (m.TP + m.FN);

	return new Maximise((1 + (beta * beta)) * ((precision * recall)
		/ (((beta * beta) * precision) + recall)));
    }
}