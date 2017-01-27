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

package myra.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.StringTokenizer;

import myra.datamining.Attribute;
import myra.datamining.Dataset;
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

	while ((line = reader.readLine()) != null) {
	    String[] split = split(line);

	    if (split.length > 0 && !isComment(split[0])) {
		split[0] = split[0].toLowerCase();

		// are we dealing with an attribute?
		if (split[0].startsWith(ATTRIBUTE)) {
		    if (split.length != 3) {
			reader.close();
			throw new IllegalArgumentException("Invalid attribute specification: "
				+ line);
		    }

		    processAttribute(dataset, split);
		} else if (split[0].startsWith(DATA)) {
		    dataSection = true;
		} else if (split[0].startsWith(RELATION) && split.length == 2) {
		    dataset.setName(split[1]);
		}
		// we must be dealing with an instance
		else if (dataSection) {
		    processInstance(dataset, line);
		}
	    }
	}

	reader.close();
	
	setMaxMinValue(dataset);
	
	return dataset;
    }

    /**
     * updates the attributes in the dataset to find the max and min values of continuous attributes
     * 
	 * @param dataset
	 */
	private void setMaxMinValue(Dataset dataset) {
		
		Attribute[] attributes = dataset.attributes();
		
		
		for(int i=0; i < attributes.length; i ++)
		{
			if(attributes[i].getType() == Type.CONTINUOUS){
				double min = Double.MAX_VALUE;
				double max = Double.MIN_VALUE;
				for(int j=0;j < dataset.size();j++){
					if(min > dataset.value(j, i))
						min = dataset.value(j, i);
					
					if(max < dataset.value(j, i))
						max = dataset.value(j, i);
				}
				dataset.getAttribute(i).setMax(max);
				dataset.getAttribute(i).setMin(min);
				
			}
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
	StringTokenizer tokens = new StringTokenizer(line, ",");
	String[] values = new String[tokens.countTokens()];
	int index = 0;

	while (tokens.hasMoreTokens()) {
	    String value = trim(tokens.nextToken());
	    // value.replaceAll("'|\"", "");
	    values[index] = value;
	    index++;
	}

	dataset.add(values);
    }
    
    /**
     * Removes spaces from the beginning and end of the string. This method
     * will also remove single quotes usually created by WEKA discretisation
     * process.
     * 
     * @param value the string to trim.
     * 
     * @return a string without spaces at the beginning and end.
     */
    private String trim(String value) {
	value = value.replace("'\\'", "\"").replace("\\''", "\"");
	return value.trim();
    }
}