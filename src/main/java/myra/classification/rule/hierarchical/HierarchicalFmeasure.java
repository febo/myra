/*
 * HierarchicalFmeasure.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2021 Fernando Esteban Barril Otero
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

package myra.classification.rule.hierarchical;

import static myra.Config.CONFIG;
import static myra.classification.hierarchical.HierarchicalFmeasure.BETA;
import static myra.datamining.Dataset.RULE_COVERED;
import static myra.datamining.Hierarchy.IGNORE;

import myra.Cost;
import myra.Cost.Maximise;
import myra.classification.rule.ClassificationRule;
import myra.classification.rule.function.ClassificationRuleFunction;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.datamining.Hierarchy;

/**
 * The <code>hFmeasure</code> class represents a hierarchical rule quality
 * function based on the hierarchical F-measure.
 * 
 * <p>
 * <b>Note:</b> this class will ignore the labels specified by
 * {@link Hierarchy#IGNORE} when calculating the hierarchical F-measure.
 * </p>
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @since 5.0
 */
public class HierarchicalFmeasure extends ClassificationRuleFunction {
    @Override
    public Cost evaluate(Dataset dataset,
                         ClassificationRule rule,
                         Instance[] instances) {

        // indexes of the labels to ignore
        boolean[] ignore = CONFIG.get(IGNORE);
        int correct = 0;
        int predicted = 0;
        int actual = 0;

        boolean[] pLabel = rule.getConsequent().active();

        for (int i = 0; i < instances.length; i++) {
            if (instances[i].flag == RULE_COVERED) {
                boolean[] tLabel = dataset.label(i).active();

                for (int j = 0; j < ignore.length; j++) {
                    if (!ignore[j]) {
                        if (tLabel[j]) {
                            actual++;
                        }
                        if (pLabel[j]) {
                            predicted++;
                        }
                        if (tLabel[j] && pLabel[j]) {
                            correct++;
                        }
                    }
                }

            }
        }

        double hP = correct / (double) predicted;
        double hR = correct / (double) actual;
        double b = Math.pow(CONFIG.get(BETA), 2);
        double hF = ((b + 1) * hP * hR) / ((b * hP) + hR);

        return new Maximise(hF);
    }
}