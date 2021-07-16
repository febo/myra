/*
 * hFMeasure.java
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

package myra.classification.hierarchical;

import static myra.Config.CONFIG;
import static myra.datamining.Hierarchy.IGNORE;

import myra.Config.ConfigKey;
import myra.Cost;
import myra.Cost.Maximise;
import myra.classification.ClassificationModel;
import myra.datamining.Dataset;
import myra.datamining.Hierarchy;
import myra.rule.ListMeasure;
import myra.rule.RuleList;

/**
 * This class calculates the Hierarchical hF-measure using the micro-average of
 * the Hierarchical Precision and Recall.
 * 
 * <p>
 * <b>Note:</b> this class will ignore the labels specified by
 * {@link Hierarchy.IGNORE} when calculating the hF measure.
 * </p>
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @since 5.0
 */
public class hFmeasure extends HierarchicalMeasure implements ListMeasure {
    /**
     * The config key for the weighing factor between recall and precision.
     */
    public final static ConfigKey<Double> BETA = new ConfigKey<>();

    /**
     * The default factor, which weighs recall the same as precision.
     */
    public final static double DEFAULT_BETA = 1.0;

    @Override
    public Cost evaluate(Dataset dataset, ClassificationModel model) {
        // indexes of the labels to ignore
        boolean[] ignore = CONFIG.get(IGNORE);
        // number of correct prediction
        int correct = 0;
        // total number of predictions
        int predicted = 0;
        // total number of labels present
        int actual = 0;

        for (int i = 0; i < dataset.size(); i++) {
            boolean[] tLabel = dataset.label(i).active();
            boolean[] pLabel = model.predict(dataset, i).active();

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

        // calculates the hierarchical measure

        double hP = correct / (double) predicted;
        double hR = correct / (double) actual;
        double b = Math.pow(CONFIG.get(BETA), 2);
        double hF = ((b + 1) * hP * hR) / ((b * hP) + hR);

        return new Maximise(hF);
    }

    @Override
    public Cost evaluate(Dataset dataset, RuleList list) {
        return evaluate(dataset, new ClassificationModel(list));
    }
}