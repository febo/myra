/*
 * PessimisticAccuracy.java
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

package myra.classification.rule;

import myra.Cost;
import myra.Cost.Maximise;
import myra.datamining.Dataset;
import myra.rule.ListMeasure;
import myra.rule.RuleList;
import myra.util.Stats;

/**
 * Measure based on C4.5 error estimation.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class PessimisticAccuracy implements ListMeasure {
    @Override
    public Cost evaluate(Dataset dataset, RuleList list) {
        if (list.size() == 0) {
            return new Maximise();
        }

        // updates the coverage of each rule
        list.apply(dataset);

        // we assume that we are dealing with classification rules, which
        // should be the case; there is nothing we can do if this is not
        // the case, apart from raising an exception
        ClassificationRule[] rules = (ClassificationRule[]) list.rules();

        double[] coverage = new double[list.size()];
        double[] errors = new double[list.size()];

        // coverage and errors of each rule

        for (int i = 0; i < coverage.length; i++) {
            for (int j = 0; j < dataset.classLength(); j++) {
                coverage[i] += rules[i].covered()[j];

                if (j != rules[i].getConsequent().value()) {
                    errors[i] += rules[i].covered()[j];
                }
            }
        }

        // predicted errors of the list (sum of the estimated errors
        // of each rule)

        double predicted = 0;

        for (int i = 0; i < coverage.length; i++) {
            predicted += (errors[i] + Stats.errors(coverage[i], errors[i]));
        }

        return new Maximise(1.0 - (predicted / (double) dataset.size()));
    }
}