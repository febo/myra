/*
 * Regressor.java
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

import myra.datamining.Algorithm;
import myra.datamining.Dataset;
import myra.datamining.Model;
import myra.util.Logger;

/**
 * Base class for implementing regression algorithms.
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class Regressor extends Algorithm {

    @Override
    protected void evaluate(Dataset dataset, Model model) {
        RRMSE rrmse = new RRMSE();
        double value = rrmse.evaluate(dataset, model).raw();

        Logger.log("RRMSE on training set: %f (%.4f)\n", value, value);

    }

    @Override
    protected void test(Dataset dataset, Model model) {
        RRMSE rrmse = new RRMSE();
        double value = rrmse.evaluate(dataset, model).raw();

        Logger.log("RRMSE on test set: %f (%.4f)\n", value, value);
    }
}