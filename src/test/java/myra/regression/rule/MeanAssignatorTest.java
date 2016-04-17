/*
 * MeanAssignatorTest.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2016 Fernando Esteban Barril Otero
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

package myra.regression.rule;

import java.io.InputStreamReader;

import junit.framework.TestCase;
import myra.data.Dataset;
import myra.data.Dataset.Instance;
import myra.util.ARFFReader;

/**
 * <code>MeanAssignatorTest</code> class test.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class MeanAssignatorTest extends TestCase {
    public void testAssign() {
	try {
	    ARFFReader reader = new ARFFReader();

	    Dataset dataset = reader.read(new InputStreamReader(getClass()
		    .getResourceAsStream("/temperature.arff")));

	    RegressionRule rule = new RegressionRule();
	    Instance[] instances = Instance.newArray(dataset.size());
	    rule.apply(dataset, instances);
	    
	    MeanAssignator assignator = new MeanAssignator();
	    assignator.assign(dataset, rule, instances);
	    
	    assertEquals(73.5714, rule.getConsequent().value(), 0.00005);
	    
	} catch (Exception e) {
	    fail(e.toString());
	}
    }
}