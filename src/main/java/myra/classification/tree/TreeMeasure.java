/*
 * TreeMeasure.java
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

package myra.classification.tree;

import myra.Cost;
import myra.datamining.Dataset;

/**
 * Base class for tree quality measures.
 * 
 * @author Fernando Esteban Barril Otero
 */
public enum TreeMeasure {
    /**
     * Accuracy tree measure.
     */
    ACCURACY {
        @Override
        public Cost evaluate(Dataset dataset, Tree tree) {
            int[][] matrix = fill(dataset, tree);
            int hits = 0;

            for (int i = 0; i < matrix.length; i++) {
                hits += matrix[i][i];
            }

            return new Cost.Maximise(hits / (double) dataset.size());
        }
    },
    /**
     * A pessimistic accuracy measure. It estimates the number of errors given
     * the error rate.
     */
    PESSIMISTIC {
        @Override
        public Cost evaluate(Dataset dataset, Tree tree) {
            double error = TreeStats.estimated(tree.getRoot());
            int total = dataset.size();

            return new Cost.Maximise((total - error) / total);
        }
    };

    /**
     * Returns the quality of the decision tree.
     * 
     * @param dataset
     *            the current dataset.
     * @param tree
     *            the decision tree.
     * 
     * @return the quality of the decision tree.
     */
    public abstract Cost evaluate(Dataset dataset, Tree tree);

    /**
     * Returns a multi-class confusion matrix for the specified tree.
     * 
     * @param dataset
     *            the current dataset.
     * @param tree
     *            the decison tree.
     * 
     * @return a multi-class confusion matrix for the specified tree.
     */
    public static int[][] fill(Dataset dataset, Tree tree) {
        int[][] matrix = new int[dataset.classLength()][dataset.classLength()];

        for (int i = 0; i < dataset.size(); i++) {
            int actual = (int) dataset.value(i, dataset.classIndex());
            int predicted = tree.predict(dataset, i).value();

            matrix[actual][predicted]++;
        }

        return matrix;
    }
}