/*
 * RuleClassifier.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2017 Fernando Esteban Barril Otero
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

import myra.classification.ClassificationModel;
import myra.classification.Classifier;
import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.Model;
import myra.rule.Rule;
import myra.rule.RuleList;
import myra.util.Logger;

/**
 * Base class for implementing rule-based classification algorithms.
 * 
 * @author Fernnado Esteban Barril Otero
 */
public abstract class RuleClassifier extends Classifier {

    @Override
    protected void evaluate(Dataset dataset, Model model) {
	super.evaluate(dataset, model);

	if (((ClassificationModel) model).raw() instanceof RuleList) {
	    logRules(dataset, (RuleList) ((ClassificationModel) model).raw());
	}
    }

    /**
     * Logs information about the classification rules.
     * 
     * @param dataset
     *            the current dataset.
     * @param list
     *            the <code>RuleList</code> model.
     */
    protected void logRules(Dataset dataset, RuleList list) {
	Logger.log("%n>>> Rule coverage:%n%n");

	Rule[] rules = list.rules();
	int position = Integer.toString(rules.length).length();
	Logger.log("%" + position + "s ", "#");

	int minimum = Integer.toString(dataset.size()).length();
	int[] width = new int[dataset.classLength()];
	Attribute[] attributes = dataset.attributes();

	for (int i = 0; i < attributes[dataset.classIndex()].size(); i++) {
	    width[i] = attributes[dataset.classIndex()].value(i).length();

	    if (width[i] < minimum) {
		width[i] = minimum;
	    }

	    Logger.log("%" + width[i] + "s ",
		       attributes[dataset.classIndex()].value(i));
	}

	Logger.log(" <-- class %n");

	for (int i = 0; i < rules.length; i++) {
	    Logger.log("%" + position + "d ", i + 1);
	    ClassificationRule c = (ClassificationRule) rules[i];

	    for (int j = 0; j < width.length; j++) {
		Logger.log("%" + width[j] + "d ", c.covered()[j]);
	    }

	    if (rules[i].isEmpty()) {
		Logger.log(" (default rule)");
	    }

	    Logger.log("%n");
	}
    }
}