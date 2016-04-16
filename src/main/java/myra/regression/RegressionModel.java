/*
 * RegressionModel.java
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

import myra.data.Dataset;
import myra.data.Model;

/**
 * Wrapper class for classification models. This class follows the Adapter
 * design pattern to provide type safety for models that actually represent
 * classification models.
 * 
 * @since 4.5
 * 
 * @author Fernando Esteban Barril Otero
 */
public final class RegressionModel implements Model {
    /**
     * The wrapped (classification) model.
     */
    private Model model;

    /**
     * Default constructor.
     * 
     * @param model
     *            the wrapped model. Note that it is expected that the model
     *            {@link Model#predict(Dataset, int)} method returns a
     *            <code>Real</code> object.
     */
    public RegressionModel(Model model) {
	this.model = model;
    }

    /**
     * @throws ClassCastException
     *             if the wrapped model does not represent a regression model.
     */
    @Override
    public Real predict(Dataset dataset, int instance) {
	return (Real) model.predict(dataset, instance);
    }

    @Override
    public String toString(Dataset dataset) {
	return model.toString(dataset);
    }
}