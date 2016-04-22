/*
 * Load.java
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

import static myra.datamining.Dataset.NOT_COVERED;

import java.io.IOException;

import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.rule.shell.Command;
import myra.rule.shell.Memory;
import myra.rule.shell.Memory.Location;
import myra.util.ARFFReader;

/**
 * Loads a dataset into the shell's memory.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Load implements Command {
    /**
     * Memory location for the dataset.
     */
    public static final Location<Dataset> DATASET = new Location<Dataset>();

    /**
     * Memory location for the instances array.
     */
    public static final Location<Instance[]> INSTANCES =
	    new Location<Instance[]>();

    @Override
    public void execute(Memory memory, String... arguments) {
	try {
	    ARFFReader reader = new ARFFReader();
	    Dataset dataset = reader.read(arguments[0]);
	    memory.put(DATASET, dataset);

	    Instance[] instances = Instance.newArray(dataset.size());
	    Instance.markAll(instances, NOT_COVERED);
	    memory.put(INSTANCES, instances);

	    System.out.println("Available instances: " + dataset.size());
	} catch (IOException e) {
	    System.out.println("Could not load file: " + arguments[0]);
	}
    }

    @Override
    public String name() {
	return "load";
    }
    
    @Override
    public int size() {
	return 1;
    }
}