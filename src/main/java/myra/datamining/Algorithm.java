/*
 * Algorithm.java
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

package myra.datamining;

import static myra.Config.CONFIG;
import static myra.datamining.Attribute.Type.NOMINAL;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TreeMap;

import myra.Config.ConfigKey;
import myra.Option;
import myra.Option.BooleanOption;
import myra.util.ARFFReader;
import myra.util.Logger;

/**
 * Base class for implementing data mining algorithms.
 * 
 * @since 4.5
 * 
 * @author Fernando Esteban Barril Otero
 */
public abstract class Algorithm {
    /**
     * The config key for the test file.
     */
    public final static ConfigKey<String> TEST_FILE = new ConfigKey<>();

    /**
     * The config key for the training file.
     */
    public final static ConfigKey<String> TRAINING_FILE = new ConfigKey<>();

    /**
     * The config key for the random number generator.
     */
    public final static ConfigKey<Random> RANDOM_GENERATOR = new ConfigKey<>();

    /**
     * The config key for the random seed.
     */
    public final static ConfigKey<Long> RANDOM_SEED = new ConfigKey<>();

    /**
     * The width of the output console.
     */
    private final int CONSOLE_WIDTH = 80;

    /**
     * Returns the algorithm command-line options. The default implementation
     * includes options for {@link #TEST_FILE} and {@link #TRAINING_FILE}.
     * 
     * @return the algorithm command-line options.
     */
    protected Collection<Option<?>> options() {
	ArrayList<Option<?>> options = new ArrayList<Option<?>>();

	options.add(new Option<String>(TRAINING_FILE,
				       "f",
				       "Path of the training file"));

	options.add(new Option<String>(TEST_FILE,
				       "t",
				       "Path of the (optional) test file"));

	// random seed
	options.add(new Option<Long>(RANDOM_SEED,
				     "s",
				     "Random %s value (default current time)",
				     false,
				     "seed") {
	    @Override
	    public void set(String value) {
		Long seed = Long.parseLong(value);

		CONFIG.set(RANDOM_SEED, seed);
		CONFIG.set(RANDOM_GENERATOR, new Random(seed));
	    }
	});

	return options;
    }

    /**
     * Process the command-line arguments.
     * 
     * @param args
     *            the array representing the command-line arguments.
     * 
     * @return a map of parameter [key, value] pairs from the command-line.
     */
    protected Map<String, String> processCommandLine(String[] args) {
	Map<String, String> parameters =
		new LinkedHashMap<String, String>(args.length, 1.0f);
	String current = null;

	for (int i = 0; i < args.length; i++) {
	    if (args[i].startsWith("-")) {
		if (current != null) {
		    parameters.put(current.substring(1), null);
		    current = null;
		}

		current = args[i];
	    } else {
		if (current == null) {
		    throw new IllegalArgumentException("Missing switch option for value: "
			    + args[i]);
		}

		parameters.put(current.substring(1), args[i]);
		current = null;
	    }
	}

	if (current != null) {
	    parameters.put(current.substring(1), null);
	}

	// set options values

	for (Option<?> option : options()) {
	    if (parameters.containsKey(option.getModifier())) {
		option.set(parameters.get(option.getModifier()));
	    }
	}

	return parameters;
    }

    /**
     * Entry point for the execution. This is a template method for the
     * subclasses. It calls the following methods:
     * 
     * <ul>
     * <li>{@link #train(Dataset)}</li>
     * <li>{@link #evaluate(Dataset, Model)}</li>
     * <li>{@link #test(Dataset, Model)} (if a test file has been provided)</li>
     * </ul>
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public void run(String[] args) throws Exception {
	// sets property defaults
	defaults();
	// reads command-line arguments
	Map<String, String> parameters = processCommandLine(args);

	if (CONFIG.isPresent(TRAINING_FILE)) {
	    ARFFReader reader = new ARFFReader();
	    Dataset dataset = reader.read(CONFIG.get(TRAINING_FILE));

	    logRuntime(dataset, parameters);

	    long start = System.nanoTime();

	    Model model = train(dataset);

	    Logger.log("=== Discovered Model ===%n%n");
	    Logger.log(model.toString(dataset));
	    Logger.log("%n");

	    double elapsed = (System.nanoTime() - start) / Math.pow(10, 9);

	    evaluate(dataset, model);

	    // if a test file is provided, evaluates the model on the test
	    // data and logs the confusion matrix

	    if (CONFIG.isPresent(TEST_FILE)) {
		dataset = reader.read(CONFIG.get(TEST_FILE));

		Logger.log("%n=== Evaluation on test set ===%n%n");

		test(dataset, model);
	    }

	    Logger.log("%nRunning time (seconds): %.2f%n", elapsed);
	    Logger.close();
	} else {
	    usage();
	}
    }

    /**
     * Trains the algorithm for the current dataset.
     * 
     * @param dataset
     *            the current dataset.
     * 
     * @return the model created.
     */
    protected abstract Model train(Dataset dataset);

    /**
     * Evaluates the model on the training data.
     * 
     * @param dataset
     *            the current dataset.
     * @param model
     *            the model.
     */
    protected abstract void evaluate(Dataset dataset, Model model);

    /**
     * Applies the model to the test data.
     * 
     * @param dataset
     *            the test data.
     * @param model
     *            the model.
     */
    protected abstract void test(Dataset dataset, Model model);

    /**
     * Returns the algorithm description.
     * 
     * @return the algorithm description.
     */
    protected abstract String description();

    /**
     * Sets the default property values. The default implementation sets the
     * random seed to be the current time.
     */
    protected void defaults() {
	Long seed = System.currentTimeMillis();

	CONFIG.set(RANDOM_SEED, seed);
	CONFIG.set(RANDOM_GENERATOR, new Random(seed));
    }

    /**
     * Prints the usage information. This consists in listing all the options
     * available.
     */
    protected void usage() {
	TreeMap<String, Option<?>> map =
		new TreeMap<String, Option<?>>(new Comparator<String>() {
		    @Override
		    public int compare(String o1, String o2) {
			if (o1.startsWith("--")) {
			    if (o2.startsWith("--")) {
				return o1.compareTo(o2);
			    } else {
				return 1;
			    }
			} else if (o2.startsWith("--")) {
			    return -1;
			}

			return o1.compareTo(o2);
		    }
		});
	int longest = 0;

	// sorts options by modified

	for (Option<?> option : options()) {
	    if (option.getKey() != TEST_FILE
		    && option.getKey() != TRAINING_FILE) {
		String modifier = null;

		if (option.hasArgument()) {
		    modifier = String
			    .format("-%s",
				    option.getModifier() + String
					    .format(" <%s>    ",
						    option.getArgument()));
		} else {
		    modifier = String.format("-%s    ", option.getModifier());
		}

		map.put(modifier, option);
		longest = Math.max(longest, modifier.length());
	    }
	}

	StringBuffer buffer = new StringBuffer();

	// prints options' details

	for (String modifier : map.keySet()) {
	    buffer.append(String.format("  %-" + longest + "s", modifier));

	    String[] description = map.get(modifier).toString().split(" ");
	    int available = CONSOLE_WIDTH - (longest + 2);

	    for (String s : description) {
		if (s.length() > available) {
		    buffer.append(String.format("%n  %-" + longest + "s", " "));
		    available = CONSOLE_WIDTH - (longest + 2);
		}

		buffer.append(String.format("%s ", s));
		available -= (s.length() + 1);
	    }

	    buffer.append(String.format("%n%n"));
	}

	Logger.log("Usage: %s -f %s [-t %s] [options]%n%n",
		   getClass().getSimpleName(),
		   "<arff_training_file>",
		   "<arff_test_file>");

	try {
	    Properties messages = new Properties();
	    messages.load(getClass()
		    .getResourceAsStream("/myra-help.properties"));
	    String help = messages.getProperty("usage.message");

	    int available = CONSOLE_WIDTH;

	    for (String s : help.split(" ")) {
		if (s.length() > available) {
		    Logger.log("%n");
		    available = CONSOLE_WIDTH;
		}

		Logger.log("%s ", s);
		available -= (s.length() + 1);
	    }
	} catch (Exception e) {
	    // quietly ignored, the only effect is that the usage
	    // message is not going to be printed
	}

	Logger.log("%n%n%s%n%n", "The following options are available:");
	Logger.log("%s", buffer);
    }

    /**
     * Logs the runtime information.
     * 
     * @param dataset
     *            the training dataset.
     * @param parameters
     *            the command-line parameters.
     */
    protected void logRuntime(Dataset dataset, Map<String, String> parameters) {
	String description = description() + " " + version();
	Logger.log("%s", description);

	DateFormat formatter = new SimpleDateFormat("EEE MMM dd yyyy");
	String timestamp = formatter.format(new Date());
	Logger.log("%" + (80 - description.length()) + "s%n", timestamp);

	for (int i = 0; i < description.length(); i++) {
	    Logger.log("_");
	}

	formatter = new SimpleDateFormat("HH:mm:ss");
	timestamp = formatter.format(new Date());
	Logger.log("%" + (80 - description.length()) + "s%n", timestamp);

	if (CONFIG.isPresent(TRAINING_FILE)) {
	    Logger.log("%nTraining file: %s", CONFIG.get(TRAINING_FILE));
	}

	if (CONFIG.isPresent(TEST_FILE)) {
	    Logger.log("%nTest file: %s", CONFIG.get(TEST_FILE));
	}

	parameters.remove("f"); // training file
	parameters.remove("t"); // test file

	if (!parameters.isEmpty()) {
	    Logger.log("%nOptions:");

	    for (String option : parameters.keySet()) {
		String value = parameters.get(option);
		Logger.log(" -%s %s",
			   option,
			   (value == null ? "" : parameters.get(option)));
	    }
	}

	// default option values
	Logger.log("%n%n[Runtime default values]%n");

	for (Option<?> option : options()) {
	    if (!parameters.containsKey(option.getModifier())
		    && CONFIG.isPresent(option.getKey())
		    && !option.getModifier().equals("f")
		    && !option.getModifier().equals("t")) {

		if (option instanceof BooleanOption
			&& !CONFIG.get(((BooleanOption) option).getKey())) {
		    // we only print the informantion for BooleanOptions that
		    // have default values set to true
		} else {
		    Logger.log("\t-%s %s%n",
			       option.getModifier(),
			       option.value());
		}
	    }
	}

	Logger.log("%n");
	Logger.log("Relation: %s%n", dataset.getName());
	Logger.log("Instances: %d%n", dataset.size());
	Logger.log("Attributes: %d%n", (dataset.attributes().length - 1));

	if (dataset.getTarget().getType() == NOMINAL) {
	    Logger.log("Classes: %d%n", dataset.classLength());
	}

	if (CONFIG.isPresent(RANDOM_SEED)) {
	    Logger.log("Random seed: %d%n", CONFIG.get(RANDOM_SEED));
	}

	Logger.log("%n");
    }

    /**
     * Return the current implementation version. This will only work when the
     * code is running from a jar file.
     * 
     * @return the implementaion version or an empty string when no version is
     *         available.
     */
    public static String version() {
	try {
	    Properties properties = new Properties();
	    properties.load(Algorithm.class
		    .getResourceAsStream("/myra-git.properties"));

	    if (properties.containsKey("git.commit.id.describe")) {
		return String
			.format("[build %s]",
				properties
					.getProperty("git.commit.id.describe"));

	    }
	} catch (Exception e) {
	    // silently ingored
	}

	return "[NO-GIT]";
    }
}