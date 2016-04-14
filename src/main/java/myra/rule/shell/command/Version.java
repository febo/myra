/*
 * Version.java
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

import myra.classification.Classifier;
import myra.rule.shell.Command;
import myra.rule.shell.Memory;

/**
 * Prints the shell version.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Version implements Command {
    @Override
    public void execute(Memory memory, String... arguments) {
	System.out.println("Shell " + Classifier.version());
    }

    @Override
    public String name() {
	return "version";
    }

    @Override
    public int size() {
	return 0;
    }
}