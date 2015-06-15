
package myra.rule.shell.command;

import static myra.rule.shell.command.Load.DATASET;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import myra.Attribute;
import myra.Dataset;
import myra.rule.shell.Command;
import myra.rule.shell.Memory;

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
	    System.out.print("Domain for attribute " + attribute.getName() + ": ");
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