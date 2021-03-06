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

package myra.datamining;

import myra.Cost;

/**
 * Base class for model evaluation measures.
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class Measure {
    /**
     * Returns the cost of the specified model.
     * 
     * @param dataset
     *            the current dataset.
     * @param model
     *            the model to evaluate.
     * 
     * @return the cost of the specified model.
     */
    public abstract Cost evaluate(Dataset dataset, Model model);
}