/*
 * Parser.java
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

package myra.classification.rule;

import static myra.Config.CONFIG;
import static myra.classification.rule.unordered.ConflictResolution.FREQUENT_CLASS;
import static myra.data.Attribute.ANY_OF;
import static myra.data.Attribute.EQUAL_TO;
import static myra.data.Attribute.GREATER_THAN;
import static myra.data.Attribute.GREATER_THAN_OR_EQUAL_TO;
import static myra.data.Attribute.IN_RANGE;
import static myra.data.Attribute.LESS_THAN;
import static myra.data.Attribute.LESS_THAN_OR_EQUAL_TO;
import static myra.data.Attribute.Type.NOMINAL;
import static myra.data.Dataset.RULE_COVERED;
import static myra.rule.RuleSet.CONFLICT_RESOLUTION;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import myra.Cost;
import myra.classification.Label;
import myra.data.Attribute;
import myra.data.Dataset;
import myra.data.Attribute.Condition;
import myra.data.Dataset.Instance;
import myra.rule.PredictionExplanationSize;
import myra.rule.Rule;
import myra.rule.RuleList;
import myra.rule.RuleSet;
import myra.util.ARFFReader;

/**
 * Utility to parse a list of rule from different algorihtms' output.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class Parser {
    /**
     * Start of a rule token.
     */
    private static final String BEGIN = "IF";

    /**
     * End of a rule token.
     */
    private static final String END = "THEN";

    /**
     * Antecedent term of a rule token.
     */
    private static final String CONDITION = "AND";

    /**
     * Parses a rule.
     * 
     * @param dataset
     *            the dataset to get the attribute informatino from.
     * @param text
     *            the rule text.
     * 
     * @return a <code>Rule</code> instance.
     */
    public static Rule parse(Dataset dataset, String text) {
	StringTokenizer tokens = new StringTokenizer(text);
	boolean consequent = false;

	ClassificationRule rule = null;
	ArrayList<String> slices = null;

	while (tokens.hasMoreTokens()) {
	    String token = tokens.nextToken();

	    if (consequent) {
		Attribute target = dataset.attributes()[dataset.classIndex()];

		for (int i = 0; i < target.values().length; i++) {
		    if (target.value(i).equalsIgnoreCase(token)) {
			rule.setConsequent(new Label(i));
			break;
		    }
		}

		break;
	    }
	    if (token.equals(BEGIN)) {
		rule = new ClassificationRule();
		slices = new ArrayList<String>();
	    } else if (token.equals(END) || token.equalsIgnoreCase(CONDITION)) {
		if (slices.size() == 3) {
		    Attribute attribute = null;
		    String name = slices.get(0).trim();

		    for (Attribute att : dataset.attributes()) {
			if (att.getName().equalsIgnoreCase(name)) {
			    attribute = att;
			    break;
			}
		    }

		    Condition condition = new Condition();
		    condition.attribute = attribute.getIndex();

		    String operator = slices.get(1).trim();

		    if (attribute.getType() == NOMINAL) {
			if ("=".equals(operator)) {
			    condition.relation = EQUAL_TO;
			    String value = slices.get(2).trim();

			    for (int i =
				    0; i < attribute.values().length; i++) {
				if (attribute.value(i).equals(value)) {
				    condition.value[0] = i;
				    break;
				}
			    }
			} else if ("IN".equals(operator)) {
			    String slice = slices.get(2).trim();
			    String[] values =
				    slice.replaceAll("\\{|\\}", "").split(",");

			    condition.relation = ANY_OF;
			    condition.value = new double[values.length];

			    for (int i = 0; i < values.length; i++) {
				condition.value[i] =
					attribute.indexOf(values[i]);
			    }
			}
		    } else {
			condition.relation = toOperator(operator);
			condition.value[0] =
				Double.parseDouble(slices.get(2).trim());
		    }

		    slices.clear();
		    rule.add(-1, condition);
		} else if (slices.size() == 5) {
		    Condition condition = new Condition();
		    condition.attribute = dataset
			    .findAttribute(slices.get(2).trim()).getIndex();
		    condition.value[0] =
			    Double.parseDouble(slices.get(0).trim());
		    condition.value[1] =
			    Double.parseDouble(slices.get(4).trim());

		    condition.relation = IN_RANGE;

		    slices.clear();
		    rule.add(-1, condition);
		} else if (slices.size() != 0) {
		    throw new RuntimeException("Invalid rule input");
		}

		consequent = token.equals(END);
	    } else if ("<empty>".equals(token.trim())) {
		// it is ok, we reached the default rule
	    } else {
		slices.add(token);
	    }
	}

	return rule;
    }

    /**
     * Returns the operator specified by the text.
     * 
     * @param text
     *            the textual representation of the operator.
     * 
     * @return the operator specified by the text.
     */
    private static short toOperator(String text) {
	if ("<=".equals(text)) {
	    return LESS_THAN_OR_EQUAL_TO;
	} else if (">".equals(text)) {
	    return GREATER_THAN;
	} else if ("<".equals(text)) {
	    return LESS_THAN;
	} else if (">=".equals(text)) {
	    return GREATER_THAN_OR_EQUAL_TO;
	} else if ("=".equals(text)) {
	    return EQUAL_TO;
	}

	throw new IllegalArgumentException("Invalid attribute condition: "
		+ text);
    }

    /**
     * Calculates the standard deviation of the values.
     * 
     * @param values
     *            the output values.
     * 
     * @return the standard deviation of the values as a string.
     */
    private static String stdev(ArrayList<Double> values) {
	double mean = 0;

	for (int i = 0; i < values.size(); i++) {
	    mean += values.get(i);
	}

	mean /= ((double) values.size());
	double std = 0;

	for (int i = 0; i < values.size(); i++) {
	    std += Math.pow(values.get(i) - mean, 2);
	}

	std /= (((double) values.size()) - 1);
	std /= ((double) values.size());
	std = Math.sqrt(std);

	if (Double.isNaN(std) || Double.isInfinite(std)) {
	    std = 0.0;
	}

	return String.format("%.3f +/- %.3f", mean, std);
    }

    /**
     * Parses a list of rules.
     * 
     * @param dataset
     *            the dataset to get the attribute informatino from.
     * @param rules
     *            the textual representation of the list of rules.
     * 
     * @return a list of rules.
     */
    public static RuleList parseList(Dataset dataset, ArrayList<String> rules) {
	RuleList list = new RuleList();
	Instance[] instances = Instance.newArray(dataset.size());

	for (String text : rules) {
	    Rule rule = parse(dataset, text);
	    rule.apply(dataset, instances);
	    Dataset.markCovered(instances);

	    list.add(rule);
	}

	return list;
    }

    /**
     * Parses a set of rules.
     * 
     * @param dataset
     *            the dataset to get the attribute informatino from.
     * @param rules
     *            the textual representation of the set of rules.
     * 
     * @return a set of rules.
     */
    public static RuleSet parseSet(Dataset dataset, ArrayList<String> rules) {
	RuleSet set = new RuleSet();

	for (String text : rules) {
	    Rule rule = parse(dataset, text);
	    set.add(rule);
	}

	set.apply(dataset);

	return set;
    }

    /**
     * Calculates the predictino-explanation size value of the specified list.
     * 
     * @param dataset
     *            the dataset to get the instances from.
     * @param list
     *            the list of rules.
     * 
     * @return the predictino-explanation size value of the specified list.
     * 
     * @see PredictionExplanationSize
     */
    public static Cost average(Dataset dataset, RuleList list) {
	PredictionExplanationSize measure = new PredictionExplanationSize();

	return measure.evaluate(dataset, list);
    }

    /**
     * C45rules output file parser.
     */
    public static class C45rules {
	/**
	 * Entry point to parse rules from a C45rules output file.
	 * 
	 * @param args
	 *            command-line arguments (args[0] = datasets folder, args[1]
	 *            = results folder).
	 * 
	 * @throws Exception
	 *             in case of any error.
	 */
	public static void main(String[] args) throws Exception {
	    File datasets = new File(args[0]);
	    File results = new File(args[1]);

	    for (File ds : datasets.listFiles(new FileFilter() {
		public boolean accept(File pathname) {
		    return pathname.isDirectory();
		}
	    })) {
		final String pattern = ds.getName();

		for (File output : results.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
			return name.contains(pattern);
		    }
		})) {
		    parse(output.getAbsolutePath(), ds.getAbsolutePath());
		}
	    }
	}

	/**
	 * Parses an output file.
	 * 
	 * @param output
	 *            the output file path.
	 * @param dataset
	 *            the dataset file path.
	 * 
	 * @throws Exception
	 *             in case of any error (typically I/O error).
	 */
	private static void parse(String output, String dataset)
		throws Exception {
	    ARFFReader reader = new ARFFReader();

	    BufferedReader file = new BufferedReader(new FileReader(output));
	    String line = null;

	    boolean isRule = false;
	    boolean isList = false;

	    ArrayList<String> rules = new ArrayList<String>();
	    StringBuffer buffer = new StringBuffer();
	    String steam = null;

	    ArrayList<Double> values = new ArrayList<Double>();

	    while ((line = file.readLine()) != null) {
		if (line.contains("File stem")) {
		    String[] tokens =
			    line.substring(line.indexOf("<"), line.indexOf(">"))
				    .trim().split("/");

		    steam = tokens[tokens.length - 1];
		} else if (line.startsWith("Final rules from tree")) {
		    isList = true;
		} else if (isList && line.startsWith("Rule")) {
		    isRule = true;
		} else if (line.contains("->  class")) {
		    buffer.append(" THEN ");
		    buffer.append(line
			    .substring(line.indexOf("->  class")
				    + "->  class".length(), line.indexOf("["))
			    .trim());

		    isRule = false;
		    rules.add(buffer.toString());
		    buffer.delete(0, buffer.length());
		} else if (isRule) {
		    if (buffer.length() == 0) {
			buffer.append("IF ");
		    } else {
			buffer.append(" AND ");
		    }

		    buffer.append(line.trim());
		} else if (isList && line.contains("Default")) {
		    buffer.append("IF <empty> THEN ");
		    buffer.append(line.substring(line.indexOf(":") + 1).trim());
		    rules.add(buffer.toString());
		    buffer.delete(0, buffer.length());

		    Dataset training =
			    reader.read(dataset + "/" + steam + ".arff");
		    Dataset test = reader.read(dataset + "/"
			    + steam.replace("TR", "TS") + ".arff");

		    // we have a complete list
		    RuleList list = Parser.parseList(training, rules);

		    values.add(Parser.average(test, list).raw());

		    isList = false;
		    rules.clear();
		}
	    }

	    file.close();

	    System.out.println(stdev(values));
	}
    }

    /**
     * PART output file parser.
     */
    public static class PART {
	/**
	 * Entry point to parse rules from a PART output file.
	 * 
	 * @param args
	 *            command-line arguments (args[0] = datasets folder, args[1]
	 *            = results folder).
	 * 
	 * @throws Exception
	 *             in case of any error.
	 */
	public static void main(String[] args) throws Exception {
	    File datasets = new File(args[0]);
	    File results = new File(args[1]);

	    for (File ds : datasets.listFiles(new FileFilter() {
		public boolean accept(File pathname) {
		    return pathname.isDirectory();
		}
	    })) {
		final String pattern = ds.getName();

		for (File output : results.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
			return name.contains(pattern);
		    }
		})) {
		    parse(output.getAbsolutePath(), ds.getAbsolutePath());
		}
	    }
	}

	/**
	 * Parses an output file.
	 * 
	 * @param output
	 *            the output file path.
	 * @param dataset
	 *            the dataset file path.
	 * 
	 * @throws Exception
	 *             in case of any error (typically I/O error).
	 */
	private static void parse(String output, String dataset)
		throws Exception {
	    ARFFReader reader = new ARFFReader();

	    BufferedReader file = new BufferedReader(new FileReader(output));
	    String line = null;

	    boolean isRule = false;
	    boolean isList = false;

	    ArrayList<String> rules = new ArrayList<String>();
	    StringBuffer buffer = new StringBuffer();
	    String steam = null;

	    ArrayList<Double> values = new ArrayList<Double>();

	    while ((line = file.readLine()) != null) {
		if (line.contains("Relation: ")) {
		    steam = line.substring(line.indexOf("Relation: ")
			    + "Relation: ".length()).trim();

		} else if (line.startsWith("=== Discovered Model ===")) {
		    isList = true;
		} else if (isList && line.trim().length() == 0) {
		    isRule = true;
		} else if (isList && line.contains(": ")) {
		    String[] tokens = line.split(": ");

		    if (tokens[0].trim().length() == 0) {
			// default rule
			buffer.append("IF <empty>");
		    } else {
			if (buffer.length() == 0) {
			    buffer.append(BEGIN);
			}

			buffer.append(" " + tokens[0].trim());
		    }

		    buffer.append(" THEN ");
		    buffer.append(tokens[1].substring(0, tokens[1].indexOf("("))
			    .trim());

		    isRule = false;
		    rules.add(buffer.toString());
		    buffer.delete(0, buffer.length());

		    if (tokens[0].trim().length() == 0) {
			isList = false;
		    }
		} else if (line.contains("Number of Rules")) {
		    Dataset training =
			    reader.read(dataset + "/" + steam + ".arff");
		    Dataset test = reader.read(dataset + "/"
			    + steam.replace("TR", "TS") + ".arff");

		    // we have a complete list
		    RuleList list = Parser.parseList(training, rules);

		    values.add(Parser.average(test, list).raw());

		    isList = false;
		    isRule = false;

		    rules.clear();
		} else if (isRule && line.contains("AND")) {
		    if (buffer.length() == 0) {
			buffer.append(BEGIN);
		    }

		    buffer.append(" " + line.trim());
		}
	    }

	    file.close();

	    System.out.println(stdev(values));
	}
    }

    /**
     * JRip output file parser.
     */
    public static class JRip {
	/**
	 * Entry point to parse rules from a JRip output file.
	 * 
	 * @param args
	 *            command-line arguments (args[0] = datasets folder, args[1]
	 *            = results folder).
	 * 
	 * @throws Exception
	 *             in case of any error.
	 */
	public static void main(String[] args) throws Exception {
	    File datasets = new File(args[0]);
	    File results = new File(args[1]);

	    for (File ds : datasets.listFiles(new FileFilter() {
		public boolean accept(File pathname) {
		    return pathname.isDirectory();
		}
	    })) {
		final String pattern = ds.getName();

		for (File output : results.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
			return name.contains(pattern);
		    }
		})) {
		    parse(output.getAbsolutePath(), ds.getAbsolutePath());
		}
	    }
	}

	/**
	 * Parses an output file.
	 * 
	 * @param output
	 *            the output file path.
	 * @param dataset
	 *            the dataset file path.
	 * 
	 * @throws Exception
	 *             in case of any error (typically I/O error).
	 */
	private static void parse(String output, String dataset)
		throws Exception {
	    ARFFReader reader = new ARFFReader();

	    BufferedReader file = new BufferedReader(new FileReader(output));
	    String line = null;
	    boolean isList = false;

	    ArrayList<String> rules = new ArrayList<String>();
	    StringBuffer buffer = new StringBuffer();
	    String steam = null;

	    ArrayList<Double> values = new ArrayList<Double>();

	    while ((line = file.readLine()) != null) {
		if (line.contains("Relation: ")) {
		    steam = line.substring(line.indexOf("Relation: ")
			    + "Relation: ".length()).trim();

		} else if (line.startsWith("=== Discovered Model ===")) {
		    isList = true;
		} else if (isList && line.contains("=>")) {
		    String[] tokens = line.split("=>");

		    if (tokens[0].trim().length() == 0) {
			// default rule
			buffer.append("IF <empty>");
		    } else {
			tokens[0] = tokens[0].replaceAll("\\(", "");
			tokens[0] = tokens[0].replaceAll("\\)", "");

			buffer.append("IF ");
			buffer.append(tokens[0].trim());
		    }

		    buffer.append(" THEN ");
		    buffer.append(tokens[1]
			    .substring(tokens[1].indexOf('=') + 1,
				       tokens[1].indexOf("("))
			    .trim());

		    rules.add(buffer.toString());
		    buffer.delete(0, buffer.length());
		} else if (line.contains("Number of Rules")) {
		    Dataset training =
			    reader.read(dataset + "/" + steam + ".arff");
		    Dataset test = reader.read(dataset + "/"
			    + steam.replace("TR", "TS") + ".arff");

		    // we have a complete list
		    RuleList list = Parser.parseList(training, rules);

		    values.add(Parser.average(test, list).raw());

		    isList = false;
		    rules.clear();
		}
	    }

	    file.close();

	    System.out.println(stdev(values));
	}
    }

    /**
     * CN2 output file parser.
     */
    public static class CN2 {
	/**
	 * Entry point to parse rules from a CN2 output file.
	 * 
	 * @param args
	 *            command-line arguments (args[0] = datasets folder, args[1]
	 *            = results folder).
	 * 
	 * @throws Exception
	 *             in case of any error.
	 */
	public static void main(String[] args) throws Exception {
	    File datasets = new File(args[0]);
	    File results = new File(args[1]);

	    for (File ds : datasets.listFiles(new FileFilter() {
		public boolean accept(File pathname) {
		    return pathname.isDirectory();
		}
	    })) {
		final String pattern = ds.getName();

		for (File output : results.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
			return name.contains(pattern);
		    }
		})) {
		    parse(output.getAbsolutePath(), ds.getAbsolutePath());
		}
	    }
	}

	/**
	 * Parses an output file.
	 * 
	 * @param output
	 *            the output file path.
	 * @param dataset
	 *            the dataset file path.
	 * 
	 * @throws Exception
	 *             in case of any error (typically I/O error).
	 */
	private static void parse(String output, String dataset)
		throws Exception {
	    ARFFReader reader = new ARFFReader();

	    BufferedReader file = new BufferedReader(new FileReader(output));
	    String line = null;

	    boolean isRule = false;

	    ArrayList<String> rules = new ArrayList<String>();
	    StringBuffer buffer = new StringBuffer();
	    String steam = null;

	    ArrayList<Double> values = new ArrayList<Double>();

	    while ((line = file.readLine()) != null) {
		if (line.contains("Reading file")) {
		    steam = line.substring(line.indexOf("...") + "...".length())
			    .trim().replace(".att", ".arff");
		    steam = steam.substring(steam.indexOf('/') + 1);

		} else if (line.startsWith("=== Discovered Model ===")) {
		    isRule = true;
		} else if (line.contains("(DEFAULT)")) {
		    buffer.append("IF <empty> THEN ");
		    buffer.append(line.substring(line.indexOf('\"') + 1,
						 line.lastIndexOf('\"')));
		    rules.add(buffer.toString());

		    Dataset training = reader.read(dataset + "/" + steam);
		    Dataset test = reader
			    .read(dataset + "/" + steam.replace("TR", "TS"));

		    // we have a complete list
		    RuleSet set = Parser.parseSet(training, rules);
		    CONFIG.set(CONFLICT_RESOLUTION, FREQUENT_CLASS);

		    values.add(Parser.average(test, set).raw());

		    isRule = false;
		    rules.clear();
		} else if (isRule) {
		    line = line.trim();

		    if (line.startsWith("THEN")) {
			buffer.append(" THEN ");
			buffer.append(line.substring(line.indexOf('\"') + 1,
						     line.lastIndexOf('\"')));

			rules.add(buffer.toString().trim());
			buffer.delete(0, buffer.length());
		    } else {
			buffer.append(" " + line.trim().replaceAll("\"", ""));
		    }
		}
	    }

	    file.close();

	    System.out.println(stdev(values));
	}
    }

    /**
     * <i>c</i>Ant-Miner (and its variations) output file parser.
     */
    public static class cAntMiner {
	/**
	 * Entry point to parse rules from a <i>c</i>Ant-Miner (and its
	 * extensions) output file.
	 * 
	 * @param args
	 *            command-line arguments (args[0] = datasets folder, args[1]
	 *            = results folder).
	 * 
	 * @throws Exception
	 *             in case of any error.
	 */
	public static void main(String[] args) throws Exception {
	    File datasets = new File(args[0]);
	    File results = new File(args[1]);

	    for (File ds : datasets.listFiles(new FileFilter() {
		public boolean accept(File pathname) {
		    return pathname.isDirectory();
		}
	    })) {
		final String pattern = ds.getName();
		ArrayList<Double> values = new ArrayList<Double>();

		for (File output : results.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
			return name.contains(pattern);
		    }
		})) {
		    values.add(parse(output.getAbsolutePath(),
				     ds.getAbsolutePath()));
		}

		System.out.println(stdev(values));
	    }
	}

	/**
	 * Parses an output file.
	 * 
	 * @param output
	 *            the output file path.
	 * @param dataset
	 *            the dataset file path.
	 * 
	 * @throws Exception
	 *             in case of any error (typically I/O error).
	 */
	private static double parse(String output, String dataset)
		throws Exception {
	    ARFFReader reader = new ARFFReader();

	    BufferedReader file = new BufferedReader(new FileReader(output));
	    String line = null;

	    boolean isList = false;

	    ArrayList<String> rules = new ArrayList<String>();
	    String steam = null;

	    // we are using 10-fold cross validation
	    double total = 0;
	    double count = 0;

	    while ((line = file.readLine()) != null) {
		if (line.contains("Relation:")) {
		    steam = line.substring(line.indexOf("Relation: ")
			    + "Relation: ".length()).trim();
		} else if (line.startsWith("=== Discovered Model ===")) {
		    isList = true;
		} else if (isList && line.startsWith(BEGIN)) {
		    rules.add(line.substring(0, line.indexOf('(')).trim());
		}

		if (isList && line.contains("<empty>")) {
		    Dataset training =
			    reader.read(dataset + "/" + steam + ".arff");
		    Dataset test = reader.read(dataset + "/"
			    + steam.replace("TR", "TS") + ".arff");

		    // we have a complete list
		    RuleList list = Parser.parseList(training, rules);

		    total += Parser.average(test, list).raw();
		    count++;

		    isList = false;
		    rules.clear();
		}
	    }

	    file.close();

	    return (total / count);
	}
    }

    /**
     * Unordered <i>c</i>Ant-Miner (and its variations) output file parser.
     */
    public static class UcAntMiner {
	/**
	 * Entry point to parse rules from a Unordered <i>c</i>Ant-Miner (and
	 * its variations) output file.
	 * 
	 * @param args
	 *            command-line arguments (args[0] = datasets folder, args[1]
	 *            = results folder).
	 * 
	 * @throws Exception
	 *             in case of any error.
	 */
	public static void main(String[] args) throws Exception {
	    File datasets = new File(args[0]);
	    File results = new File(args[1]);

	    for (File ds : datasets.listFiles(new FileFilter() {
		public boolean accept(File pathname) {
		    return pathname.isDirectory();
		}
	    })) {
		final String pattern = ds.getName();
		ArrayList<Double> values = new ArrayList<Double>();

		for (File output : results.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
			return name.contains(pattern);
		    }
		})) {
		    values.add(parse(output.getAbsolutePath(),
				     ds.getAbsolutePath()));
		}

		System.out.println(stdev(values));
	    }
	}

	/**
	 * Parses an output file.
	 * 
	 * @param output
	 *            the output file path.
	 * @param dataset
	 *            the dataset file path.
	 * 
	 * @throws Exception
	 *             in case of any error (typically I/O error).
	 */
	private static double parse(String output, String dataset)
		throws Exception {
	    ARFFReader reader = new ARFFReader();

	    BufferedReader file = new BufferedReader(new FileReader(output));
	    String line = null;

	    boolean isList = false;

	    ArrayList<String> rules = new ArrayList<String>();
	    String steam = null;

	    // we are using 10-fold cross validation
	    double total = 0;
	    double count = 0;

	    while ((line = file.readLine()) != null) {
		if (line.contains("Relation:")) {
		    steam = line.substring(line.indexOf("Relation: ")
			    + "Relation: ".length()).trim();
		} else if (line.startsWith("=== Discovered Model ===")) {
		    isList = true;
		} else if (isList && line.startsWith(BEGIN)) {
		    rules.add(line.substring(0, line.indexOf('(')).trim());
		}

		if (isList && line.contains("<empty>")) {
		    Dataset training =
			    reader.read(dataset + "/" + steam + ".arff");
		    Dataset test = reader.read(dataset + "/"
			    + steam.replace("TR", "TS") + ".arff");

		    // we have a complete list
		    RuleSet set = Parser.parseSet(training, rules);
		    CONFIG.set(CONFLICT_RESOLUTION, FREQUENT_CLASS);
		    // CONFIG.set(CONFLICT_RESOLUTION,
		    // ConflictResolution.CONFIDENCE);

		    total += Parser.average(test, set).raw();
		    count++;

		    isList = false;
		    rules.clear();
		}
	    }

	    file.close();

	    return total / count;
	}
    }

    /**
     * PSO/ACO2 output file parser.
     */
    public static class PSOACO2 {
	/**
	 * Entry point to parse rules from a PSO/ACO2 output file.
	 * 
	 * @param args
	 *            command-line arguments (args[0] = datasets folder, args[1]
	 *            = results folder).
	 * 
	 * @throws Exception
	 *             in case of any error.
	 */
	public static void main(String[] args) throws Exception {
	    File datasets = new File(args[0]);
	    File results = new File(args[1]);

	    for (File ds : datasets.listFiles(new FileFilter() {
		public boolean accept(File pathname) {
		    return pathname.isDirectory();
		}
	    })) {
		final String pattern = ds.getName();
		ArrayList<Double> values = new ArrayList<Double>();

		for (File output : results.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
			return name.contains(pattern);
		    }
		})) {
		    values.add(parse(output.getAbsolutePath(),
				     ds.getAbsolutePath()));
		}

		System.out.println(stdev(values));
	    }
	}

	/**
	 * Parses an output file.
	 * 
	 * @param output
	 *            the output file path.
	 * @param dataset
	 *            the dataset file path.
	 * 
	 * @throws Exception
	 *             in case of any error (typically I/O error).
	 */
	private static double parse(String output, String dataset)
		throws Exception {
	    ARFFReader reader = new ARFFReader();

	    BufferedReader file = new BufferedReader(new FileReader(output));
	    String line = null;

	    boolean isList = false;

	    ArrayList<String> rules = new ArrayList<String>();
	    String steam = null;

	    // we are using 10-fold cross validation
	    double total = 0;
	    double count = 0;

	    while ((line = file.readLine()) != null) {
		if (line.contains("Training file:")) {
		    steam = line.substring(
					   line.indexOf("Training file: ")
						   + "Training file: ".length(),
					   line.lastIndexOf('('))
			    .trim();

		    isList = true;
		} else if (isList && line.startsWith(BEGIN)) {
		    rules.add(line.trim());
		}

		if (isList && line.contains("number of rules")) {
		    Dataset training = reader.read(dataset + "/" + steam);
		    Dataset test = reader
			    .read(dataset + "/" + steam.replace("TR", "TS"));

		    // we have a complete list
		    RuleList list = Parser.parseList(training, rules);

		    if (!list.hasDefault()) {
			Instance[] instances =
				Instance.newArray(training.size());
			Instance.markAll(instances, RULE_COVERED);

			int v = training.findMajority(instances, RULE_COVERED);
			Rule rule = new ClassificationRule();
			rule.setConsequent(new Label(v));
			list.add(rule);
		    }

		    total += Parser.average(test, list).raw();
		    count++;

		    isList = false;
		    rules.clear();
		}
	    }

	    file.close();

	    return (total / count);
	}
    }

    /**
     * BioHEL output file parser.
     */
    public static class BioHEL {
	/**
	 * Entry point to parse rules from a BioHEL output file.
	 * 
	 * @param args
	 *            command-line arguments (args[0] = datasets folder, args[1]
	 *            = results folder).
	 * 
	 * @throws Exception
	 *             in case of any error.
	 */
	public static void main(String[] args) throws Exception {
	    File datasets = new File(args[0]);
	    File results = new File(args[1]);

	    for (File ds : datasets.listFiles(new FileFilter() {
		public boolean accept(File pathname) {
		    return pathname.isDirectory();
		}
	    })) {
		final String pattern = ds.getName();
		ArrayList<Double> values = new ArrayList<Double>();

		for (File output : results.listFiles(new FilenameFilter() {
		    @Override
		    public boolean accept(File dir, String name) {
			return name.contains(pattern);
		    }
		})) {
		    values.add(parse(output.getAbsolutePath(),
				     ds.getAbsolutePath()));
		}

		System.out.println(stdev(values));
	    }
	}

	/**
	 * Parses an output file.
	 * 
	 * @param output
	 *            the output file path.
	 * @param dataset
	 *            the dataset file path.
	 * 
	 * @throws Exception
	 *             in case of any error (typically I/O error).
	 */
	private static double parse(String output, String dataset)
		throws Exception {
	    ARFFReader reader = new ARFFReader();

	    BufferedReader file = new BufferedReader(new FileReader(output));
	    String line = null;

	    boolean isList = false;

	    ArrayList<String> rules = new ArrayList<String>();
	    String steam = null;

	    // we are using 10-fold cross validation
	    double total = 0;
	    double count = 0;

	    while ((line = file.readLine()) != null) {
		if (line.startsWith("=====================")) {
		    steam = line
			    .substring(line.indexOf("===================== ")
				    + "===================== ".length())
			    .trim();
		} else if (line.startsWith("Phenotype:")) {
		    isList = true;
		} else if (isList && line.contains(":Att")) {
		    StringBuffer rule = new StringBuffer();
		    rule.append("IF ");

		    String[] tokens = line.split("\\|");

		    for (String token : tokens) {
			int start = token.indexOf("Att");

			if (start != -1) {
			    if (rule.length() > "IF ".length()) {
				rule.append("AND ");
			    }

			    int end = token.indexOf(" is ");

			    // continuous attributes
			    if (token.contains("[")) {
				String value =
					token.substring(token.indexOf("[") + 1,
							token.indexOf("]"));

				String operator =
					value.split("\\-?\\d[.\\d]*")[0];

				if (operator.length() > 0) {
				    value = value
					    .substring(value.indexOf(operator)
						    + operator.length());

				    rule.append(token
					    .substring(start + "Att".length(),
						       end)
					    .trim());

				    rule.append(" ");
				    rule.append(operator);
				    rule.append(" ");
				    rule.append(value);
				    rule.append(" ");
				}
				// in range
				else {
				    String[] values = value.split(",");

				    rule.append(values[0]);
				    rule.append(" <= ");

				    rule.append(token
					    .substring(start + "Att".length(),
						       end)
					    .trim());

				    rule.append(" < ");
				    rule.append(values[1]);
				    rule.append(" ");
				}
			    }
			    // nominal attributes
			    else {
				rule.append(token
					.substring(start + "Att".length(), end)
					.trim());

				String value =
					token.substring(end + " is ".length())
						.trim();
				String[] values = value.split(",");

				// multi-valued
				if (values.length > 1) {
				    rule.append(" IN {");
				    rule.append(value);
				    rule.append("} ");
				}
				// single-valued
				else {
				    rule.append(" = ");
				    rule.append(value);
				    rule.append(" ");
				}
			    }
			} else {
			    rule.append("THEN ");

			    rule.append(token);
			}
		    }

		    rules.add(rule.toString());
		} else if (isList && line.contains("Default rule")) {
		    String consequent =
			    line.substring(line.indexOf("->") + "->".length());
		    rules.add("IF <empty> THEN " + consequent);

		    Dataset training = reader.read(dataset + "/" + steam);
		    Dataset test = reader
			    .read(dataset + "/" + steam.replace("TR", "TS"));

		    // we have a complete list
		    RuleList list = Parser.parseList(training, rules);

		    if (!list.hasDefault()) {
			Instance[] instances =
				Instance.newArray(training.size());
			Instance.markAll(instances, RULE_COVERED);

			int v = training.findMajority(instances, RULE_COVERED);
			Rule rule = new ClassificationRule();
			rule.setConsequent(new Label(v));
			list.add(rule);
		    }

		    total += Parser.average(test, list).raw();
		    count++;

		    isList = false;
		    rules.clear();
		}
	    }

	    file.close();

	    return (total / count);
	}
    }
}