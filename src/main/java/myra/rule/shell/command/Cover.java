/*
 * Cover.java
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

import static myra.rule.shell.command.Load.DATASET;
import static myra.rule.shell.command.Load.INSTANCES;

import myra.classification.rule.Parser;
import myra.data.Dataset;
import myra.data.Dataset.Instance;
import myra.rule.Rule;
import myra.rule.shell.Command;
import myra.rule.shell.Memory;

/**
 * Command that cover instances using the specified rule. A dataset needs to be
 * loaded prior to executing this command.
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @see Load
 */
public class Cover implements Command {
    @Override
    public void execute(Memory memory, String... arguments) {
	Dataset dataset = memory.get(DATASET);
	Instance[] instances = memory.get(INSTANCES);

	if (dataset == null || instances == null) {
	    System.out.println("No dataset loaded.");
	} else {
	    Rule rule = Parser.parse(dataset, arguments[0]);
	    rule.apply(dataset, instances);
	    int available = Dataset.markCovered(instances);

	    System.out.println(rule.toString(dataset));
	    System.out.println("Available instances: " + available);
	}
    }

    @Override
    public String name() {
	return "cover";
    }
    
    @Override
    public int size() {
        return 1;
    }
}