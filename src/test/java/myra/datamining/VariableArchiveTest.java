/*
 * AttributeArchiveTest.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2018 Fernando Esteban Barril Otero
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

import static myra.Archive.ARCHIVE_SIZE;
import static myra.Archive.DEFAULT_Q;
import static myra.Archive.Q;
import static myra.Config.CONFIG;
import static myra.datamining.Algorithm.RANDOM_GENERATOR;
import static myra.datamining.VariableArchive.CONVERGENCE_SPEED;
import static myra.datamining.VariableArchive.DEFAULT_CONVERGENCE_SPEED;
import static myra.datamining.VariableArchive.DEFAULT_PRECISION;
import static myra.datamining.VariableArchive.PRECISION;

import java.util.Random;

import junit.framework.TestCase;

/**
 * @author Fernando Esteban Barril Otero
 */
public class VariableArchiveTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
	super.setUp();

	CONFIG.set(ARCHIVE_SIZE, 5);
	CONFIG.set(Q, DEFAULT_Q);
	CONFIG.set(CONVERGENCE_SPEED, DEFAULT_CONVERGENCE_SPEED);
	CONFIG.set(PRECISION, DEFAULT_PRECISION);
	CONFIG.set(RANDOM_GENERATOR, new Random());
    }

    public void testCategoricalSampling() {
	VariableArchive.Categorical archive = new VariableArchive.Categorical(3);

	for (int i = 0; i < CONFIG.get(ARCHIVE_SIZE); i++) {
	    Integer value = archive.sample();
	    assertNotNull(value);
	    double quality = CONFIG.get(RANDOM_GENERATOR).nextDouble();

	    archive.add(value, quality);
	}

	archive.update();

	// archive is complete now

	Integer value = archive.sample();
	assertNotNull(value);
    }

    public void testContinuousSampling() {
	VariableArchive.Continuous archive = new VariableArchive.Continuous(0, 10);

	for (int i = 0; i < CONFIG.get(ARCHIVE_SIZE); i++) {
	    Double value = archive.sample();
	    assertTrue(value < 10.0);
	    double quality = CONFIG.get(RANDOM_GENERATOR).nextDouble();

	    archive.add(value, quality);
	}

	archive.update();

	// archive is complete now

	for (int i = 0; i < CONFIG.get(ARCHIVE_SIZE); i++) {
	    archive.sample();
	}
    }
}