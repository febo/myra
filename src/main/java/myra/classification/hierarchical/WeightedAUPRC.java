/*
 * WeightedAUPRC.java
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

package myra.classification.hierarchical;

import static myra.Config.CONFIG;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import myra.Cost;
import myra.Cost.Maximise;
import myra.classification.ClassificationModel;
import myra.datamining.Dataset;
import myra.datamining.Hierarchy;

/**
 * This class represents a weighted precision-recall evaluation measure,
 * consisting on calculating the weighted average over the area under the
 * precision-recall curve of each individual class label of the hierarchy.
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @see AUPRC
 * 
 * @since 5.0
 */
public class WeightedAUPRC extends AUPRC {
    /**
     * The weighting type.
     */
    private Type type;

    /**
     * Default constructor.
     */
    public WeightedAUPRC() {
        this(Type.CLASS);
    }

    /**
     * Creates a new <code>WeightedAUPRC</code> with the specified weighting
     * type.
     * 
     * @param type
     *            the weighting type.
     */
    public WeightedAUPRC(Type type) {
        this.type = type;
    }

    @Override
    public Cost evaluate(Dataset dataset, ClassificationModel model) {
        // finds the indexes of the labels to ignore
        Set<String> ignore = Collections.emptySet();

        if (CONFIG.isPresent(IGNORE_LIST)) {
            String list = CONFIG.get(IGNORE_LIST);

            if (list == null) {
                ignore = new HashSet<>();
            } else {
                ignore = new HashSet<>(Arrays
                        .asList(list.split(Hierarchy.SEPARATOR)));
            }
            // the root label is always ignore
            ignore.add(dataset.getHierarchy().root().getLabel());
        }

        final int size = dataset.size();
        final double[] frequency = dataset.getHierarchy().distribution();

        String[] labels = dataset.getTarget().values();
        double total = 0.0;
        int labelSize = 0;

        for (int i = 0; i < frequency.length; i++) {
            if (!ignore.contains(labels[i]) && frequency[i] > 0) {
                total += frequency[i];
                labelSize++;
            }
        }

        // the weight of each individual class label
        double[] weight = new double[dataset.getTarget().size()];
        Arrays.fill(weight, 0.0);

        for (int i = 0; i < labels.length; i++) {
            if (!ignore.contains(labels[i]) && frequency[i] > 0) {
                weight[i] = (Type.CLASS == type) ? 1.0 / labelSize
                        : frequency[i] / total;
            }
        }

        // array with each individual class label (prediction, actual) pair
        double[][] values = new double[size][2];
        double average = 0.0;

        for (int i = 0; i < weight.length; i++) {
            if (weight[i] > 0) {
                for (int j = 0; j < size; j++) {
                    values[j][0] = model.predict(dataset, j).probability(i);
                    values[j][1] = dataset.label(j).active(i) ? 1 : 0;
                }

                average += (weight[i] * area(values));
            }
        }

        return new Maximise(average);
    }

    /**
     * Enum of valid weighting types.
     * 
     * @author Fernando Esteban Barril Otero
     */
    public static enum Type {
        /**
         * Weighting based on frequency of examples.
         */
        FREQUENCY,
        /**
         * Weighting based on the number of class labels.
         */
        CLASS;
    }
}