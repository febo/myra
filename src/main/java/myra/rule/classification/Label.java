/*
 * Label.java
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

package myra.rule.classification;

import myra.Attribute;
import myra.Dataset;
import myra.Prediction;

/**
 * The <code>Label</code> represents the predicted value of a classification
 * algorithm.
 * 
 * @author Fernando Esteban Barril Otero
 */
public final class Label implements Prediction {
    /**
     * The class value index represented by the label.
     */
    private int index;

    /**
     * Default constructor.
     */
    public Label() {
	this(Dataset.MISSING_VALUE_INDEX);
    }

    /**
     * Default constructor.
     * 
     * @param index
     *            the class value index.
     */
    public Label(int index) {
	this.index = index;
    }

    /**
     * Returns the class value index represented by the label.
     * 
     * @return the class value index represented by the label.
     */
    public int value() {
	return index;
    }

    @Override
    public String toString(Attribute target) {
	return target.value(index);
    }
}