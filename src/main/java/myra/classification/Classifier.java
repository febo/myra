/*
 * Classifier.java
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

import myra.datamining.Algorithm;
import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.Model;
import myra.util.Logger;

/**
 * Base class for implementing classification algorithms.
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class Classifier extends Algorithm {
    @Override
    protected void evaluate(Dataset dataset, Model model) {
	Accuracy measure = new Accuracy();
	double accuracy = measure.evaluate(dataset, model).raw();
	Logger.log("Classification accuracy on training set: %f (%3.2f%%)\n",
		   accuracy,
		   accuracy * 100);
    }

    @Override
    protected void test(Dataset dataset, Model model) {
	Accuracy measure = new Accuracy();
	double accuracy = measure.evaluate(dataset, model).raw();
	Logger.log("Classification accuracy on test set: %f (%3.2f%%)%n",
		   accuracy,
		   accuracy * 100);

	int[][] matrix = Measure.fill(dataset, (ClassificationModel) model);

	Logger.log("Correctly classified instances: %d (%3.2f%%)%n",
		   dataset.size() - Measure.errors(matrix),
		   ((dataset.size() - Measure.errors(matrix))
			   / (double) dataset.size()) * 100);

	Logger.log("Incorrectly classified instances: %d (%3.2f%%)\n",
		   Measure.errors(matrix),
		   (Measure.errors(matrix) / (double) dataset.size()) * 100);

	logConfusionMatrix(dataset, matrix);
    }

    /**
     * Logs the confusion matrix.
     * 
     * @param dataset
     *            the current dataset.
     * @param matrix
     *            the confusion matrix.
     */
    protected void logConfusionMatrix(Dataset dataset, int[][] matrix) {
	Logger.log("%n>>> Confusion matrix:%n%n");

	int minimum = Integer.toString(dataset.size()).length();
	int[] width = new int[matrix.length];
	Attribute[] attributes = dataset.attributes();

	for (int i = 0; i < attributes[dataset.classIndex()].size(); i++) {
	    width[i] = attributes[dataset.classIndex()].value(i).length();

	    if (width[i] < minimum) {
		width[i] = minimum;
	    }

	    Logger.log("%" + width[i] + "s ",
		       attributes[dataset.classIndex()].value(i));
	}

	Logger.log("  <-- classified as \n");

	for (int i = 0; i < matrix.length; i++) {
	    for (int j = 0; j < matrix.length; j++) {
		Logger.log("%" + width[j] + "d ", matrix[i][j]);
	    }

	    Logger.log("  %s%n", attributes[dataset.classIndex()].value(i));
	}
    }
}