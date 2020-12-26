/*
 * Measure.java
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

package myra.classification;

import myra.Cost;
import myra.datamining.Dataset;
import myra.datamining.Model;

/**
 * Base class for model classification evaluation measures.
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class Measure extends myra.datamining.Measure {
    @Override
    public Cost evaluate(Dataset dataset, Model model) {
        return evaluate(dataset, (ClassificationModel) model);
    }

    /**
     * Returns the quality of the specified classification model.
     * 
     * @param dataset
     *            the current dataset.
     * @param model
     *            the classification model to evaluate.
     * 
     * @return the quality of the specified classification model.
     */
    public abstract Cost evaluate(Dataset dataset, ClassificationModel model);

    /**
     * Creates a confusion matrix for the specified list.
     * 
     * @param dataset
     *            the current dataset.
     * @param model
     *            the classification model.
     * 
     * @return a multi-dimensional array representing the confusion matrix.
     */
    public static int[][] fill(Dataset dataset, ClassificationModel model) {
        int[][] matrix = new int[dataset.classLength()][dataset.classLength()];

        for (int i = 0; i < dataset.size(); i++) {
            int actual = (int) dataset.value(i, dataset.classIndex());
            Label predicted = model.predict(dataset, i);

            matrix[actual][predicted.value()]++;
        }

        return matrix;
    }

    /**
     * Returns the total number of errors of the confusion matrix.
     * 
     * @param matrix
     *            the confusion matrix.
     * 
     * @return the total number of errors of the confusion matrix.
     */
    public static int errors(int[][] matrix) {
        int errors = 0;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if (i != j) {
                    errors += matrix[i][j];
                }
            }
        }

        return errors;
    }
}