/*
 * RuleSet.java
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

import static myra.Config.CONFIG;
import static myra.datamining.Dataset.NOT_COVERED;

import myra.Config.ConfigKey;
import myra.classification.Label;
import myra.classification.rule.ClassificationRule;
import myra.classification.rule.unordered.ConflictResolution;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.datamining.Prediction;

/**
 * This class represent an unordered list of rules. While the rules are
 * maintained in an (ordered) array, their order is not taken into account when
 * classifying an instance.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class RuleSet extends RuleList {
    /**
     * The config key for the default conflict resolution strategy.
     */
    public static final ConfigKey<ConflictResolution> CONFLICT_RESOLUTION =
	    new ConfigKey<ConflictResolution>();

    @Override
    public void apply(Dataset dataset) {
	Instance[] instances = Instance.newArray(dataset.size());
	Instance.markAll(instances, NOT_COVERED);

	for (int i = 0; i < rules.length; i++) {
	    if (rules[i].isEnabled()) {
		rules[i].apply(dataset, instances);
	    }
	}
    }

    @Override
    public Prediction predict(Dataset dataset, int instance) {
	boolean[] fired = new boolean[rules.length];
	int defaultRule = -1;

	for (int i = 0; i < rules.length; i++) {
	    if (rules[i].isEmpty()) {
		defaultRule = i;
	    } else if (rules[i].isEnabled()
		    && rules[i].covers(dataset, instance)) {
		fired[i] = true;
	    }
	}

	// we assume that we are dealing with classification rules, which
	// should be the case; there is nothing we can do if this is not
	// the case, apart from raising an exception
	Label predicted = CONFIG.get(CONFLICT_RESOLUTION)
		.resolve(dataset, (ClassificationRule[]) rules, fired);

	if (predicted == null) {
	    predicted = (Label) rules[defaultRule].getConsequent();
	}

	return predicted;
    }

    @Override
    public String export(Dataset dataset) {
	StringBuffer buffer = new StringBuffer();
	buffer.append(super.export(dataset));
	buffer.append(String.format("%n"));

	for (int i = 0; i < rules.length; i++) {
	    ClassificationRule c = (ClassificationRule) rules[i];
	    buffer.append(String.format("%n("));

	    for (int j = 0; j < c.covered().length; j++) {
		if (j > 0) {
		    buffer.append(",");
		}

		buffer.append(c.covered()[j]);
	    }

	    buffer.append(")");
	}

	return buffer.toString();
    }
}