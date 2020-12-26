/*
 * Remove.java
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
import static myra.rule.shell.command.Load.DATASET;
import static myra.rule.shell.command.Load.INSTANCES;

import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.rule.shell.Command;
import myra.rule.shell.Memory;

/**
 * Removes instances from the loaded dataset.
 * 
 * @author Fernando Esteban Barril Otero
 * 
 * @see Load
 */
public class Remove implements Command {
    @Override
    public void execute(Memory memory, String... arguments) {
        Dataset dataset = memory.get(DATASET);
        Instance[] instances = memory.get(INSTANCES);

        if (dataset == null || instances == null) {
            System.out.println("Dataset not loaded.");
        } else {
            int[] indexes = new int[arguments.length];

            for (int i = 0; i < arguments.length; i++) {
                indexes[i] = Integer.parseInt(arguments[i]);
            }

            System.out
                    .println("Available instances: " + dataset.remove(indexes));

            instances = Instance.newArray(dataset.size());
            Instance.markAll(instances, NOT_COVERED);
            memory.put(INSTANCES, instances);
        }
    }

    @Override
    public String name() {
        return "remove";
    }

    @Override
    public int size() {
        return 1;
    }
}