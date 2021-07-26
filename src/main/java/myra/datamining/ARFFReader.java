/*
 * ARFFReader.java
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
import static myra.datamining.Hierarchy.DELIMITER;
import static myra.datamining.Hierarchy.IGNORE;
import static myra.datamining.Hierarchy.IGNORE_LIST;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipInputStream;

import myra.datamining.Attribute.Type;

/**
 * Reads the dataset information from a ARFF file.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class ARFFReader {
    /**
     * Constant representing an attribute section.
     */
    private static final String ATTRIBUTE = "@attribute";

    /**
     * Constant representing the data section.
     */
    private static final String DATA = "@data";

    /**
     * Constant representing the relation section.
     */
    private static final String RELATION = "@relation";

    /**
     * Constant representing a continuous value attribute.
     */
    private static final String CONTINUOUS = "continuous";

    /**
     * Constant representing a numeric value attribute.
     */
    private static final String NUMERIC = "numeric";

    /**
     * Constant representing a real value attribute.
     */
    private static final String REAL = "real";

    /**
     * Constant representing the value separator.
     */
    private static final String SEPARATOR = ",";

    /**
     * Constant representing a hierarchical attribute.
     */
    private static final String HIERARCHICAL = "hierarchical";

    /**
     * Constant representing the hierarchical relations.
     */
    private static final String HIERARCHY_RELATION = "@class";

    /**
     * Reads the specified file.
     * 
     * @param filename
     *            the file name.
     * 
     * @return a <code>Dataset</code> instance contaning the contents of the
     *         file
     * 
     * @exception IOException
     *                if an I/O error occurs.
     */
    public Dataset read(String filename) throws IOException {
        String contentType = Files.probeContentType(Paths.get(filename));

        if (contentType != null && contentType.equals("application/zip")) {
            ZipInputStream zip =
                    new ZipInputStream(new FileInputStream(filename));
            // positions the stream at the first entry
            zip.getNextEntry();

            return read(new InputStreamReader(zip));
        }

        return read(new File(filename));
    }

    /**
     * Reads the specified file.
     * 
     * @param input
     *            the dataset file.
     * 
     * @return a <code>Dataset</code> instance contaning the contents of the
     *         file
     * 
     * @exception IOException
     *                if an I/O error occurs.
     */
    public Dataset read(File input) throws IOException {
        if (!input.exists()) {
            throw new IllegalArgumentException("Could not open file: "
                    + input.getAbsolutePath());
        }

        return read(new FileReader(input));
    }

    /**
     * Reads the specified input reader. The reader will be closed at the end of
     * the method.
     * 
     * @param input
     *            a reader.
     * 
     * @return a <code>Dataset</code> instance contaning the contents of the
     *         input reader.
     * 
     * @exception IOException
     *                if an I/O error occurs.
     */
    public Dataset read(Reader input) throws IOException {
        BufferedReader reader = new BufferedReader(input);
        Dataset dataset = new Dataset();
        String line = null;
        boolean dataSection = false;
        // hierarchy relations (if present)
        StringBuffer hierarchy = null;

        while ((line = reader.readLine()) != null) {
            String[] split = split(line);

            if (split.length > 0 && !isComment(split[0])) {
                split[0] = split[0].toLowerCase();

                // are we dealing with an attribute?
                if (split[0].startsWith(ATTRIBUTE)) {
                    if (split.length == 4 && split[2].equals(HIERARCHICAL)) {
                        processHierarchy(dataset, split);
                    } else if (split.length != 3) {
                        reader.close();
                        throw new IllegalArgumentException("Invalid attribute specification: "
                                + line);
                    } else {
                        processAttribute(dataset, split);
                    }
                } else if (split[0].startsWith(DATA)) {
                    // hierarchy relations specified, need to process them
                    if (hierarchy != null) {
                        split = new String[] { ATTRIBUTE,
                                               dataset.getTarget().getName(),
                                               HIERARCHICAL,
                                               hierarchy.toString() };
                        // removes the class attribute (it will be added
                        // together with the class hierarchy)
                        dataset.remove(dataset.getTarget());

                        processHierarchy(dataset, split);
                    }

                    dataSection = true;
                } else if (split[0].startsWith(RELATION) && split.length == 2) {
                    dataset.setName(split[1]);
                } else if (split[0].startsWith(HIERARCHY_RELATION)) {
                    if (hierarchy == null) {
                        hierarchy = new StringBuffer();
                    } else {
                        hierarchy.append(SEPARATOR);
                    }
                    hierarchy.append(processHierarchyRelation(split));
                }
                // we must be dealing with an instance
                else if (dataSection) {
                    processInstance(dataset, line);
                }
            }
        }

        reader.close();

        return dataset;
    }

    /**
     * Divides the input String into tokens, using a white space as delimiter.
     * 
     * @param line
     *            the String to be divided.
     * 
     * @return an array of String representing the tokens.
     */
    private String[] split(String line) {
        String[] words = new String[0];
        int index = 0;

        while (index < line.length()) {
            StringBuffer word = new StringBuffer();

            boolean copying = false;
            boolean quotes = false;
            boolean brackets = false;

            int i = index;

            for (; i < line.length(); i++) {
                char c = line.charAt(i);

                if (!copying && !Character.isWhitespace(c)) {
                    copying = true;
                }

                if (c == '"' || c == '\'') {
                    quotes ^= true;
                } else if (c == '{' || c == '}') {
                    brackets ^= true;
                }

                if (copying) {
                    if (Character.isWhitespace(c) && !quotes && !brackets) {
                        index = i + 1;
                        break;
                    }

                    word.append(c);

                    // if (!(c == '"' || c == '\''))
                    // {
                    // word.append(c);
                    // }
                }
            }

            if (i >= line.length()) {
                // we reached the end of the line, need to stop the while loop
                index = i;
            }

            if (word.length() > 0) {
                words = Arrays.copyOf(words, words.length + 1);
                words[words.length - 1] = word.toString();
            }
        }

        return words;
    }

    /**
     * Checks if the line is a comment.
     * 
     * @param line
     *            the line to be checked.
     * 
     * @return <code>true</code> if the line is a comment; <code>false</code>
     *         otherwise.
     */
    private boolean isComment(String line) {
        if (line.startsWith("%") || line.startsWith("#")) {
            return true;
        }

        return false;
    }

    /**
     * Parses an attribute.
     * 
     * @param dataset
     *            the dataset being read.
     * @param components
     *            the components representing the attribute.
     */
    private void processAttribute(Dataset dataset, String[] components) {
        if (components[2].startsWith("{")) {
            // it is a nominal attribute
            Attribute attribute = new Attribute(Type.NOMINAL, components[1]);
            StringBuffer value = new StringBuffer();

            for (int i = 0; i < components[2].length(); i++) {
                if (components[2].charAt(i) == ',') {
                    attribute.add(trim(value.toString()));
                    value.delete(0, value.length());
                } else if (components[2].charAt(i) == '}') {
                    attribute.add(trim(value.toString()));
                    dataset.add(attribute);
                    break;
                } else if (components[2].charAt(i) != '{') {
                    value.append(components[2].charAt(i));
                }
            }
        } else {
            components[2] = components[2].toLowerCase();

            if (components[2].startsWith(CONTINUOUS)
                    || components[2].startsWith(NUMERIC)
                    || components[2].startsWith(REAL)) {
                // it is a continuous attribute
                dataset.add(new Attribute(Type.CONTINUOUS, components[1]));
            } else {
                throw new IllegalArgumentException("Unsupported attribute: "
                        + components[1]);
            }
        }
    }

    /**
     * Parses an instance and adds it to the current dataset.
     * 
     * @param dataset
     *            the dataset being read.
     * @param line
     *            the instance information.
     */
    private void processInstance(Dataset dataset, String line) {
        StringTokenizer tokens = new StringTokenizer(line, SEPARATOR);
        String[] values = new String[tokens.countTokens()];
        int index = 0;

        while (tokens.hasMoreTokens()) {
            String value = trim(tokens.nextToken());
            values[index] = value;
            index++;
        }

        dataset.add(values);
    }

    private void processHierarchy(Dataset dataset, String[] components) {
        String[] values = components[3].split(",");
        boolean unique = true;

        // (1) determines if label values are unique or not

        for (int i = 0; i < values.length && unique; i++) {
            String label = trim(values[i]);
            String[] nodes = label.split(DELIMITER);

            for (int j = 0; j < (nodes.length - 1); j++) {
                if (nodes[j].equals(nodes[j + 1])) {
                    // labels in the hierarchy are not unique (e.g., 01/01)
                    // should use complete labels to identify nodes
                    unique = false;
                    break;
                }
            }
        }

        // (2) process the label values

        Hierarchy hierarchy = new Hierarchy();
        Set<String> labels = new LinkedHashSet<String>();

        for (int i = 0; i < values.length; i++) {
            String label = trim(values[i]);
            int split = label.lastIndexOf(DELIMITER);

            if (split != -1) {
                String[] nodes = new String[2];
                nodes[0] = trim(label.substring(0, split));
                nodes[1] = trim(unique ? label.substring(split + 1) : label);

                if (hierarchy.isEmpty()) {
                    // adds the parent node to the hierarchy
                    hierarchy.add(nodes[0]);
                }

                if (hierarchy.get(nodes[1]) == null) {
                    // we checked if the node exists or not, since in DAG
                    // hierarchies nodes can have multiple parents
                    hierarchy.add(nodes[1]);
                }

                // creates the parent-child link
                hierarchy.link(nodes[0], nodes[1]);

                labels.add(nodes[0]);
                labels.add(nodes[1]);
            } else {
                if (hierarchy.isEmpty()) {
                    hierarchy.add("root");
                    labels.add("root");
                }

                hierarchy.add(label);
                hierarchy.link("root", label);
                labels.add(label);
            }
        }

        hierarchy.validate(labels);

        // (3) adds a nominal attribute to represent the hierarchy

        Attribute attribute = new Attribute(Type.NOMINAL, components[1]);

        for (String label : labels) {
            attribute.add(label);
        }

        dataset.add(attribute);
        dataset.setHierarchy(hierarchy);

        // (4) determines the labels from the hierarchy to ignore (usually the
        // root label)

        boolean[] flags = new boolean[dataset.getTarget().size()];
        Arrays.fill(flags, false);

        if (CONFIG.isPresent(IGNORE_LIST)) {
            Set<String> ignore = Collections.emptySet();
            String list = CONFIG.get(IGNORE_LIST);

            if (list == null) {
                ignore = new HashSet<>();
            } else {
                ignore = new HashSet<>(Arrays.asList(Hierarchy.SEPARATOR));
            }
            // the root label is always ignore
            ignore.add(dataset.getHierarchy().root().getLabel());

            String[] classLabels = dataset.getTarget().values();

            for (int i = 0; i < classLabels.length; i++) {
                flags[i] = ignore.contains(classLabels[i]);
            }
        }

        CONFIG.set(IGNORE, flags);
    }

    /**
     * Parses an attribute.
     * 
     * @param dataset
     *            the dataset being read.
     * @param components
     *            the components representing the attribute.
     */
    private String processHierarchyRelation(String[] components) {
        StringBuffer relations = new StringBuffer();

        if (components.length < 3) {
            // root label
            relations.append("root");
            relations.append(DELIMITER);
            relations.append(trim(components[1]));
        } else if (components[2].startsWith("{")) {
            StringBuffer values = new StringBuffer();
            for (int i = 0; i < components[2].length(); i++) {
                if (components[2].charAt(i) == ','
                        || components[2].charAt(i) == '}') {
                    if (relations.length() > 0) {
                        relations.append(SEPARATOR);
                    }
                    relations.append(trim(values.toString()));
                    relations.append(DELIMITER);
                    relations.append(trim(components[1]));
                    // clears the previous value
                    values.delete(0, values.length());
                } else if (components[2].charAt(i) != '{') {
                    values.append(components[2].charAt(i));
                }
            }
        }

        return relations.toString();
    }

    /**
     * Removes spaces from the beginning and end of the string. This method will
     * also remove single quotes usually created by WEKA discretisation process.
     * 
     * @param value
     *            the string to trim.
     * 
     * @return a string without spaces at the beginning and end.
     */
    private String trim(String value) {
        value = value.replace("'\\'", "\"").replace("\\''", "\"");
        return value.trim();
    }
}