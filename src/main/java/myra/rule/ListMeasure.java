/*
 * ListMeasure.java
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

package myra.rule;

import myra.Cost;
import myra.Config.ConfigKey;
import myra.datamining.Dataset;

/**
 * This class is the base class for all list quality measures.
 * 
 * @author Fernando Esteban Barril Otero
 */
public interface ListMeasure {
    /**
     *
     * Config key for the default list measure instance.
     */
    public static final ConfigKey<ListMeasure> DEFAULT_MEASURE =
            new ConfigKey<ListMeasure>();

    /**
     * Returns the quality of the specified list of rules.
     * 
     * @param dataset
     *            the current dataset.
     * @param list
     *            the list of rules.
     * 
     * @return the quality of the specified list of rules.
     */
    public Cost evaluate(Dataset dataset, RuleList list);
}