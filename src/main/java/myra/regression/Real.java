/*
 * Real.java
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

import myra.datamining.Attribute;
import myra.datamining.Prediction;

/**
 * The <code>Real</code> class represents the predicted value of a regression
 * algorithm.
 * 
 * @since 4.5
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Real implements Prediction {
    /**
     * The real value representing the prediction.
     */
    private double value;

    /**
     * Default construction.
     */
    public Real() {
        this(Double.NaN);
    }

    /**
     * Creates a new <code>Real</code> prediction object.
     * 
     * @param value
     *            the value representing the prediction.
     */
    public Real(double value) {
        this.value = value;
    }

    /**
     * Returns the predicted (real) value.
     * 
     * @return the predicted (real) value.
     */
    public double value() {
        return value;
    }

    @Override
    public String toString(Attribute target) {
        return String.format("%.2f", value);
    }
}