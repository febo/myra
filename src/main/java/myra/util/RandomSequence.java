/*
 * RandomSequence.java
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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class represents a random sequence of numbers, which are read from a
 * file.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class RandomSequence
	extends Random {
    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = -9159623808799675090L;

    /**
     * The array of random numbers.
     */
    private Double[] numbers;

    /**
     * The current value index.
     */
    private int current;

    /**
     * Default constructor.
     * 
     * @param file
     *            the file from where the random number list is read.
     */
    public RandomSequence(String file) {
	BufferedReader reader = null;

	try {
	    ArrayList<Double> list = new ArrayList<Double>();
	    reader = new BufferedReader(new FileReader(file));
	    String line = null;

	    while ((line = reader.readLine()) != null) {
		list.add(Double.parseDouble(line));
	    }

	    numbers = list.toArray(new Double[0]);
	    current = 0;
	} catch (IOException e) {
	    throw new RuntimeException(e);
	} finally {
	    try {
		if (reader != null) {
		    reader.close();
		}
	    } catch (IOException e) {
		throw new RuntimeException(e);
	    }
	}
    }

    @Override
    public double nextDouble() {
	return numbers[current++];
    }
}