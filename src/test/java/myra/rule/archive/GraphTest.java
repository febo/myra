/*
 * GraphTest.java
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

package myra.rule.archive;

import static myra.Config.CONFIG;
import static myra.datamining.Algorithm.RANDOM_GENERATOR;

import java.io.InputStreamReader;
import java.util.Random;

import junit.framework.TestCase;
import myra.datamining.ARFFReader;
import myra.datamining.Dataset;
import myra.datamining.VariableArchive;

/**
 * @author Fernando Esteban Barril Otero
 */
public class GraphTest extends TestCase {
    private Dataset dataset;

    @Override
    protected void setUp() throws Exception {
	ARFFReader reader = new ARFFReader();
	dataset = reader.read(new InputStreamReader(getClass()
		.getResourceAsStream("/weather.arff")));
	
	CONFIG.set(VariableArchive.ARCHIVE_SIZE, 5);
	CONFIG.set(VariableArchive.PRECISION, 2.0);
	CONFIG.set(RANDOM_GENERATOR, new Random(System.currentTimeMillis()));
    }

    public void testVertices() {
	Graph graph = new Graph(dataset);
	
	// START, END and 4 predictor attributes
	assertEquals(6, graph.vertices().length);
	assertEquals(6, graph.size());
	
	Graph.Vertex vertex = graph.vertices()[2];
	assertNotNull(vertex.initial.sample());
    }
}