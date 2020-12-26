/*
 * ClassAwareSplit.java
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

package myra.classification.rule.unordered.attribute;

import myra.datamining.Dataset;
import myra.datamining.IntervalBuilder;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset.Instance;

/**
 * The <code>ClassAwareSplit</code> interface specifies the methods to handle
 * continuous attributes discretisation when the target class value is known.
 * 
 * @since 4.5
 * 
 * @author Fernando Esteban Barril Otero
 *
 * @see IntervalBuilder
 */
public interface ClassAwareSplit {
    /**
     * Returns attribute conditions representing the discrete interval for the
     * specified attribute.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param attribute
     *            the index of the continuous attribute.
     * @param target
     *            the target class value.
     * 
     * @return attribute conditions representing discrete intervals for the
     *         specified attribute
     */
    public Condition[] multiple(Dataset dataset,
                                Instance[] instances,
                                int attribute,
                                int target);

    /**
     * Returns an attribute condition representing a single discrete interval
     * for the specified attribute.
     * 
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param attribute
     *            the index of the continuous attribute.
     * @param target
     *            the target class value.
     * 
     * @return an attribute condition representing a discrete interval for the
     *         specified attribute
     */
    public Condition single(Dataset dataset,
                            Instance[] instances,
                            int attribute,
                            int target);
}