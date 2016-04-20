/*
 * RRMSE.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2016 Fernando Esteban Barril Otero
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

package myra.regression;

import myra.Cost;
import myra.Cost.Minimise;
import myra.data.Dataset;

/**
 * @author Fernando Esteban Barril Otero
 *
 */
public class RRMSE extends Measure {
    @Override
    public Cost evaluate(Dataset dataset, RegressionModel model) {
	double mean = dataset.mean();
	double lRMSE = 0;
	double lDefault = 0;

	for (int i = 0; i < dataset.size(); i++) {
	    double actual = dataset.value(i, dataset.classIndex());
	    double predicted = model.predict(dataset, i).value();

	    lRMSE += Math.pow(actual - predicted, 2);
	    lDefault += Math.pow(actual - mean, 2);
	}

	double RRMSE = Math.sqrt(lRMSE / dataset.size())
		/ Math.sqrt(lDefault / dataset.size());

	return new Minimise(RRMSE);
    }
}