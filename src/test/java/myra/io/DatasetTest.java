/*
 * DatasetTest.java
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

package myra.io;

import java.io.InputStreamReader;

import junit.framework.TestCase;
import myra.datamining.ARFFReader;
import myra.datamining.Dataset;

/**
 * <code>Dataset</code> class test.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class DatasetTest extends TestCase {
    /**
     * Classification dataset.
     */
    private Dataset cDataset;

    /**
     * Regression dataset.
     */
    private Dataset rDataset;

    /**
     * Loads the test dataset.
     */
    @Override
    protected void setUp() throws Exception {
	ARFFReader reader = new ARFFReader();

	cDataset = reader.read(new InputStreamReader(getClass()
		.getResourceAsStream("/weather.arff")));

	rDataset = reader.read(new InputStreamReader(getClass()
		.getResourceAsStream("/temperature.arff")));
    }

    /**
     * Tests the size of the dataset.
     */
    public void testSize() {
	assertEquals(14, cDataset.size());
	assertEquals(14, rDataset.size());
    }

    /**
     * Tests the class distribution of the dataset.
     */
    public void testDistribution() {
	assertEquals(9, cDataset.distribution(0));
	assertEquals(5, cDataset.distribution(1));

	cDataset.remove(0);

	assertEquals(8, cDataset.distribution(0));
	assertEquals(5, cDataset.distribution(1));
    }
    
    public void testClassLength() {
	assertEquals(2,  cDataset.classLength());
	
	try {
	    rDataset.classLength();
	    fail("Non-nominal class attribute");
	} catch (UnsupportedOperationException e) {
	    // we are expecting an exception
	}
    }
}