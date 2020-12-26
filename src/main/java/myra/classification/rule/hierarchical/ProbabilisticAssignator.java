/*
 * ProbabilisticAssignator.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2020 Fernando Esteban Barril Otero
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

import static myra.datamining.Dataset.COVERED;
import static myra.datamining.Dataset.NOT_COVERED;
import static myra.datamining.Dataset.RULE_COVERED;

import myra.classification.Label;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.rule.Assignator;
import myra.rule.Rule;

/**
 * @author Fernando Esteban Barril Otero
 */
public class ProbabilisticAssignator implements Assignator {
    @Override
    public int assign(Dataset dataset, Rule rule, Instance[] instances) {
        double[] frequencies = new double[dataset.getHierarchy().size()];
        double size = 0.0;
        double available = 0.0;

        for (int i = 0; i < instances.length; i++) {
            if (instances[i].flag != COVERED) {
                if (instances[i].flag == RULE_COVERED) {
                    boolean[] active = dataset.label(i).active();

                    for (int j = 0; j < active.length; j++) {
                        frequencies[j] += active[j] ? instances[i].weight : 0;
                    }

                    size += instances[i].weight;
                } else if (instances[i].flag == NOT_COVERED) {
                    available += instances[i].weight;
                }
            }
        }

        for (int i = 0; i < frequencies.length; i++) {
            frequencies[i] /= size;
        }

        rule.setConsequent(Label.toLabel(dataset.getTarget(), frequencies));

        return (int) Math.ceil(available);
    }
}