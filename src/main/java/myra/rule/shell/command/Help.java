/*
 * Help.java
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

import static myra.rule.shell.Shell.SHELL;

import java.util.TreeSet;

import myra.rule.shell.Command;
import myra.rule.shell.Memory;
import myra.rule.shell.Shell;

/**
 * Prints the list of commands available in the shell.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Help implements Command {
    @Override
    public void execute(Memory memory, String... arguments) {
        Shell shell = memory.get(SHELL);

        if (arguments.length == 0) {
            TreeSet<String> list = new TreeSet<String>(shell.list());

            System.out.println("The following commands are available:");
            System.out.println();

            for (String command : list) {
                System.out.println(command);
            }
        }
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public int size() {
        return 0;
    }
}