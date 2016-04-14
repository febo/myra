/*
 * CostTest.java
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

package myra;

import junit.framework.TestCase;
import myra.Cost.Minimise;
import myra.Cost.Maximise;

/**
 * <code>Cost</code> class test.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class CostTest extends TestCase {
    /**
     * Tests the <code>Error</code> class.
     */
    public void testError() {
	Minimise c1 = new Minimise(0.1);
	Minimise c2 = new Minimise(0.2);
	assertTrue(c1.compareTo(c2) > 0);
	assertTrue(c2.compareTo(c1) < 0);
	
	Minimise c3 = new Minimise(0.2);
	assertTrue(c2.compareTo(c3) == 0);
    }
    
    /**
     * Tests the <code>Success</code> class.
     */
    public void testSuccess() {
	Maximise c1 = new Maximise(0.1);
	Maximise c2 = new Maximise(0.2);
	assertTrue(c1.compareTo(c2) < 0);
	assertTrue(c2.compareTo(c1) > 0);
	
	Maximise c3 = new Maximise(0.2);
	assertTrue(c2.compareTo(c3) == 0);
    }  
}