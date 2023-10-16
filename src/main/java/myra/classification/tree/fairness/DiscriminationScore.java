/*
 * DiscriminationScore.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2022 Fernando Esteban Barril Otero
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

package myra.classification.tree.fairness;

import myra.Config;
import myra.classification.tree.fairness.FairnessHeuristic.FairnessInfo;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;

public class DiscriminationScore implements Metric {
    @Override
    public double calculate(Dataset dataset,
                            Instance[] instances,
                            FairnessInfo info) {
        int outcome = dataset.getTarget()
                .indexOf(Config.CONFIG.get(FairnessHeuristic.POSITIVE_VALUE));

        double pGroup = info.distribution(FairnessInfo.UNFAVOURED)[outcome]
                / info.length(FairnessInfo.UNFAVOURED);

        double uGroup = info.distribution(FairnessInfo.FAVOURED)[outcome]
                / info.length(FairnessInfo.FAVOURED);

        return uGroup - pGroup;
    }
}