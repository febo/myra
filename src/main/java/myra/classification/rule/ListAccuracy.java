/*
 * ListAccuracy.java
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

package myra.classification.rule;

import myra.Cost;
import myra.classification.ClassificationModel;
import myra.datamining.Dataset;
import myra.rule.ListMeasure;
import myra.rule.RuleList;

/**
 * Accuracy list measure implementation.
 * 
 * @since 4.5
 * 
 * @author Fernando Esteban Barril Otero
 */
public class ListAccuracy extends myra.classification.Accuracy
        implements ListMeasure {
    @Override
    public Cost evaluate(Dataset dataset, RuleList list) {
        return evaluate(dataset, new ClassificationModel(list));
    }
}