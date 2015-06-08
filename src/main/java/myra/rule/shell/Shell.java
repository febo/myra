/*
 * Shell.java
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

package myra.rule.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import myra.rule.shell.Memory.Location;
import myra.rule.shell.command.Cover;
import myra.rule.shell.command.Domain;
import myra.rule.shell.command.Echo;
import myra.rule.shell.command.Help;
import myra.rule.shell.command.Interval;
import myra.rule.shell.command.Load;
import myra.rule.shell.command.Quit;
import myra.rule.shell.command.Remove;
import myra.rule.shell.command.Reset;
import myra.rule.shell.command.Version;

public class Shell {
    /**
     * Memory location for the shell.
     */
    public static final Location<Shell> SHELL = new Location<Shell>();

    private Map<String, Command> commands = new HashMap<String, Command>();

    public static void main(String[] args) {
	Shell shell = new Shell();
	shell.add(new Echo());
	shell.add(new Cover());
	shell.add(new Domain());
	shell.add(new Help());
	shell.add(new Interval());
	shell.add(new Load());
	shell.add(new Quit());
	shell.add(new Remove());
	shell.add(new Reset());
	shell.add(new Version());

	try {
	    System.out.println("MYRA Interactive Shell");
	    System.out.println("----------------------");
	    System.out.println("Type 'help' for a list of commands.");
	    System.out.println();

	    shell.run();
	} catch (IOException e) {
	    System.out.println(String.format("An %s occurred. Aborting shell.",
					     e.getClass().getName()));
	    System.exit(1);
	}
    }

    public void add(Command command) {
	if (commands.containsKey(command.name())) {
	    throw new IllegalArgumentException("Duplicated command: "
		    + command.name());
	}

	commands.put(command.name(), command);
    }

    public Command get(String name) {
	return commands.get(name);
    }

    /**
     * Returns the list of commands of the shell.
     * 
     * @return the list of commands of the shell.
     */
    public Collection<String> list() {
	return commands.keySet();
    }

    public void run() throws IOException {
	BufferedReader reader =
		new BufferedReader(new InputStreamReader(System.in));
	System.out.print("> ");

	Memory memory = new Memory();
	memory.put(SHELL, this);
	String line = null;

	while ((line = reader.readLine()) != null) {
	    String[] input = parse(line);
	    // process the command
	    Command command = commands.get(input[0]);

	    if (command == null) {
		System.out.println("Command not found: " + input[0]);
	    } else {
		String[] arguments = new String[input.length - 1];

		if (arguments.length > 0) {
		    System.arraycopy(input, 1, arguments, 0, arguments.length);
		}

		command.execute(memory, arguments);
	    }

	    System.out.print("> ");
	}
    }

    /**
     * Divides the input String into tokens, using a white space as delimiter.
     * 
     * @param line
     *            the String to be divided.
     * 
     * @return an array of String representing the tokens.
     */
    private String[] parse(String line) {
	String[] words = new String[0];
	int index = 0;

	while (index < line.length()) {
	    StringBuffer word = new StringBuffer();

	    boolean copying = false;
	    boolean quotes = false;

	    int i = index;

	    for (; i < line.length(); i++) {
		char c = line.charAt(i);

		if (!copying && !Character.isWhitespace(c)) {
		    copying = true;
		}

		if (c == '"' || c == '\'') {
		    quotes ^= true;
		}

		if (copying) {
		    if (Character.isWhitespace(c) && !quotes) {
			index = i + 1;
			break;
		    }

		    word.append(c);
		}
	    }

	    if (i >= line.length()) {
		// we reached the end of the line, need to stop the while loop
		index = i;
	    }

	    if (word.length() > 0) {
		words = Arrays.copyOf(words, words.length + 1);

		if (word.charAt(0) == '"' || word.charAt(0) == '\'') {
		    word.deleteCharAt(0);
		}

		int length = word.length();

		if (word.charAt(length - 1) == '"'
			|| word.charAt(length - 1) == '\'') {
		    word.deleteCharAt(length - 1);
		}

		words[words.length - 1] = word.toString();
	    }
	}

	return words;
    }
}