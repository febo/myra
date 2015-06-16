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

package myra;

import java.io.InputStreamReader;

import junit.framework.TestCase;
import myra.util.ARFFReader;

/**
 * Dataset class test.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class DatasetTest extends TestCase {
    private Dataset dataset;

    /**
     * Loads the test dataset.
     */
    @Override
    protected void setUp() throws Exception {
	ARFFReader reader = new ARFFReader();
	dataset = reader.read(new InputStreamReader(
		getClass().getResourceAsStream("/weather.arff")));
    }

    /**
     * Tests the size of the dataset.
     */
    public void testSize() {
	assertEquals(14, dataset.size());
    }
    
    public void testDistribution() {
	assertEquals(9, dataset.distribution(0));
	assertEquals(5, dataset.distribution(1));
	
	dataset.remove(0);
	
	assertEquals(8, dataset.distribution(0));
	assertEquals(5, dataset.distribution(1));
    }
}