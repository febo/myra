/*
 * Interval.java
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

package myra.rule.shell.command;

import static myra.Config.CONFIG;
import static myra.rule.shell.command.Load.DATASET;
import static myra.rule.shell.command.Load.INSTANCES;

import myra.classification.attribute.MDLSplit;
import myra.classification.rule.ClassificationRule;
import myra.classification.rule.Parser;
import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.datamining.IntervalBuilder;
import myra.datamining.Attribute.Condition;
import myra.datamining.Dataset.Instance;
import myra.rule.Rule;
import myra.rule.shell.Command;
import myra.rule.shell.Memory;

/**
 * Executes the discretisation process on the specified continuous attributes.
 * A dataset must be loaded prior to the execution of this command.
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @see IntervalBuilder
 * @see Load
 */
public class Interval implements Command {
    @Override
    public void execute(Memory memory, String... arguments) {
	CONFIG.set(IntervalBuilder.MINIMUM_CASES, 10);
	CONFIG.set(IntervalBuilder.MAXIMUM_LIMIT, 25);
	CONFIG.set(Rule.DEFAULT_RULE, ClassificationRule.class);

	Dataset dataset = memory.get(DATASET);
	Instance[] instances = memory.get(INSTANCES);
	Attribute attribute = dataset.findAttribute(arguments[0]);

	Rule rule = Rule.newInstance();

	if (arguments.length == 2) {
	    rule = Parser.parse(dataset, "IF " + arguments[1] + " THEN 0");
	}

	rule.apply(dataset, instances);

	IntervalBuilder interval = new MDLSplit();
	Condition[] conditions =
		interval.multiple(dataset, instances, attribute.getIndex());

	for (Condition c : conditions) {
	    System.out.println(String.format("[%.6f] %s",
					     c.entropy,
					     c.toString(dataset)));
	}

	Instance.mark(instances, Dataset.RULE_COVERED, Dataset.NOT_COVERED);
    }

    @Override
    public String name() {
	return "interval";
    }
    
    @Override
    public int size() {
	return 1;
    }
}