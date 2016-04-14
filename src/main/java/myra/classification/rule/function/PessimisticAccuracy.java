/*
 * PessimisticAccuracy.java
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

import myra.Cost.Maximise;
import myra.classification.rule.ClassificationRule;
import myra.util.Stats;

/**
 * The <code>PessimisticAccuracy</code> class represents a rule quality function
 * based on the C4.5 error-based metric.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class PessimisticAccuracy extends ClassificationRuleFunction {
    @Override
    public Maximise evaluate(ClassificationRule rule) {
	BinaryConfusionMatrix m = fill(rule);

	double total = m.TP + m.FP;
	double error = (m.FP + Stats.errors(total, m.FP)) / total;

	return new Maximise(1.0 - error);
    }
}