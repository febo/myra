/*
 * Model.java
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

/**
 * Marker interface for classification models.
 * 
 * @author Fernando Esteban Barril Otero
 */
public interface Model {
    /**
     * Returns the predicted class value of the specified instance.
     * 
     * @param dataset
     *            the current dataset.
     * @param instance
     *            the instance index.
     * 
     * @return the predicted class value of the specified instance.
     */
    public Prediction predict(Dataset dataset, int instance);

    /**
     * Returns the string representation of the model. The dataset might be used
     * to retrieve the attributes' metadata.
     * 
     * @param dataset
     *            the current dataset.
     * 
     * @return the string representation of the model.
     */
    public String toString(Dataset dataset);

    /**
     * Returns the string representation of the model suitable to export to a
     * file. The dataset might be used to retrieve the attributes' metadata.
     * 
     * @param dataset
     *            the current dataset.
     * 
     * @return the string representation of the model.
     */
    public String export(Dataset dataset);
}