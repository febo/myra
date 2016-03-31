/*
 * Accuracy.java
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

import myra.io.Dataset;

/**
 * Accuracy model evaluation measure.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Accuracy extends Measure {
    @Override
    public double evaluate(Dataset dataset, ClassificationModel model) {
	int[][] matrix = Measure.fill(dataset, model);

	int correct = 0;
	int total = 0;

	for (int i = 0; i < matrix.length; i++) {
	    for (int j = 0; j < matrix.length; j++) {
		if (i == j) {
		    correct += matrix[i][j];
		}

		total += matrix[i][j];
	    }
	}

	return correct / (double) total;
    }
}