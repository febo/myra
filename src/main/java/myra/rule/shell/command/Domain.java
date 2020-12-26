/*
 * Domain.java
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import myra.datamining.Attribute;
import myra.datamining.Dataset;
import myra.rule.shell.Command;
import myra.rule.shell.Memory;

/**
 * Displays the domain values of an attribute. A dataset needs to be loaded
 * prior to executing this command.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Domain implements Command {
    @Override
    public void execute(Memory memory, String... arguments) {
        Dataset dataset = memory.get(DATASET);
        Attribute attribute = dataset.findAttribute(arguments[0]);

        if (attribute.getType() == Attribute.Type.CONTINUOUS) {
            ArrayList<Double> values = new ArrayList<Double>();

            for (int i = 0; i < dataset.size(); i++) {
                double v = dataset.value(i, attribute.getIndex());

                if (!Double.isNaN(v)) {
                    values.add(v);
                }
            }

            Collections.sort(values);

            System.out.print("Domain for attribute " + attribute.getName());
            System.out.println(": [" + values.get(0) + ", "
                    + values.get(values.size() - 1) + "]");
        } else if (attribute.getType() == Attribute.Type.NOMINAL) {
            System.out.print("Domain for attribute " + attribute.getName()
                    + ": ");
            System.out.println(Arrays.toString(attribute.values()));
        }
    }

    @Override
    public String name() {
        return "domain";
    }

    @Override
    public int size() {
        return 1;
    }
}