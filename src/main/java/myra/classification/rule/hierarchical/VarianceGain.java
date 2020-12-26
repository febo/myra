/*
 * VarianceGain.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2019 Fernando Esteban Barril Otero
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

import static myra.datamining.Dataset.NOT_COVERED;
import static myra.datamining.Dataset.RULE_COVERED;

import myra.Cost;
import myra.Cost.Maximise;
import myra.classification.rule.ClassificationRule;
import myra.classification.rule.function.ClassificationRuleFunction;
import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.datamining.Hierarchy;

/**
 * @author Fernando Esteban Barril Otero
 */
public class VarianceGain extends ClassificationRuleFunction {
    @Override
    public Cost evaluate(Dataset dataset,
                         ClassificationRule rule,
                         Instance[] instances) {
        // (1) determines the weight of the labels

        Hierarchy hierarchy = dataset.getHierarchy();

        if (hierarchy == null) {
            throw new IllegalArgumentException("Hierarchical problem expected.");
        }

        double[] weight = new double[hierarchy.size()];
        Attribute target = dataset.getTarget();

        for (int i = 0; i < target.values().length; i++) {
            weight[i] = hierarchy.get(target.value(i)).getWeight();
        }

        // (2) accumulates the frequencies

        double[] tAverage = new double[dataset.getHierarchy().size()];
        double tSize = 0;

        double[] cAverage = new double[dataset.getHierarchy().size()];
        double cSize = 0;

        double[] uAverage = new double[dataset.getHierarchy().size()];
        double uSize = 0;

        for (int i = 0; i < instances.length; i++) {
            boolean[] active = dataset.label(i).active();

            if (instances[i].flag == RULE_COVERED) {
                for (int j = 0; j < active.length; j++) {
                    double value = active[j] ? instances[i].weight : 0;
                    cAverage[j] += value;
                    // overall frequency
                    tAverage[j] += value;
                }

                cSize += instances[i].weight;
                // overall size
                tSize += instances[i].weight;
            } else if (instances[i].flag == NOT_COVERED) {
                for (int j = 0; j < active.length; j++) {
                    double value = active[j] ? instances[i].weight : 0;
                    uAverage[j] += value;
                    // overall frequency
                    tAverage[j] += value;
                }

                uSize += instances[i].weight;
                // overall size
                tSize += instances[i].weight;
            }
        }

        for (int i = 0; i < tAverage.length; i++) {
            tAverage[i] /= tSize;
            cAverage[i] /= cSize;
            uAverage[i] /= uSize;
        }

        // (3) calculates the variance gain

        double tVariance = 0.0;
        double cVariance = 0.0;
        double uVariance = 0.0;

        for (int i = 0; i < instances.length; i++) {
            boolean[] active = dataset.label(i).active();

            if (instances[i].flag == RULE_COVERED) {
                double tDistance = 0.0;
                double cDistance = 0.0;

                for (int j = 0; j < active.length; j++) {
                    double tValue =
                            (active[j] ? instances[i].weight : 0) - tAverage[j];
                    tDistance = weight[j] * (tValue * tValue);

                    double cValue =
                            (active[j] ? instances[i].weight : 0) - cAverage[j];
                    cDistance = weight[j] * (cValue * cValue);
                }

                tVariance += Math.sqrt(tDistance);
                cVariance += Math.sqrt(cDistance);
            } else if (instances[i].flag == NOT_COVERED) {
                double tDistance = 0.0;
                double uDistance = 0.0;

                for (int j = 0; j < active.length; j++) {
                    double tValue =
                            (active[j] ? instances[i].weight : 0) - tAverage[j];
                    tDistance = weight[j] * (tValue * tValue);

                    double uValue =
                            (active[j] ? instances[i].weight : 0) - uAverage[j];
                    uDistance = weight[j] * (uValue * uValue);
                }

                tVariance += Math.sqrt(tDistance);
                uVariance += Math.sqrt(uDistance);
            }
        }

        tVariance /= tSize;
        cVariance /= cSize;
        uVariance /= uSize;

        return new Maximise(tVariance - ((cSize / tSize) * cVariance)
                - ((uSize / tSize) * uVariance));
    }
}