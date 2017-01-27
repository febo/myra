/*
 * AttributeArchiveTest.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2017 Fernando Esteban Barril Otero
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
import static myra.datamining.Algorithm.RANDOM_GENERATOR;

import java.util.Random;

import junit.framework.TestCase;
import myra.datamining.Attribute.Condition;
import myra.datamining.AttributeArchive.Nominal;

/**
 * @author Fernando Esteban Barril Otero
 */
public class AttributeArchiveTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
	super.setUp();

	CONFIG.set(AttributeArchive.ARCHIVE_SIZE, 5);
	CONFIG.set(AttributeArchive.Q, AttributeArchive.DEFAULT_Q);
	CONFIG.set(RANDOM_GENERATOR, new Random());
    }

    public void testNominalSampling() {
	Nominal archive = new Nominal(0, 3);

	for (int i = 0; i < 10; i++) {
        	Condition condition = archive.sample();
        	assertNotNull(condition);
        	condition.quality = CONFIG.get(RANDOM_GENERATOR).nextDouble();
        	
        	archive.add(condition);
	}

	
	// archive is complete now
	
	Condition condition = archive.sample();
	assertNotNull(condition);
    }
}