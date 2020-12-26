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

package myra.rule;

import static java.lang.Double.NaN;

import java.io.InputStreamReader;

import junit.framework.TestCase;
import myra.datamining.ARFFReader;
import myra.datamining.Dataset;
import myra.rule.Graph.Entry;

/**
 * @author Fernando Esteban Barril Otero
 */
public class GraphTest extends TestCase {
    /**
     * The graph for the test.
     */
    private Graph graph;

    @Override
    protected void setUp() throws Exception {
        ARFFReader reader = new ARFFReader();
        Dataset dataset = reader.read(new InputStreamReader(getClass()
                .getResourceAsStream("/weather.arff")));

        graph = new Graph(dataset);
    }

    public void testMatrix() {
        Entry[][] matrix = graph.matrix();

        assertEquals(0, matrix[0][1].size());

        assertEquals(NaN, matrix[0][1].value(0));
        assertEquals(NaN, matrix[0][1].value(1));
        assertEquals(NaN, matrix[0][1].value(2));
        assertEquals(NaN, matrix[0][1].value(3));

        matrix[0][1].setInitial(10.0);
        assertEquals(10.0, matrix[0][1].value(0));
        assertEquals(10.0, matrix[0][1].value(1));
        assertEquals(10.0, matrix[0][1].value(2));
        assertEquals(10.0, matrix[0][1].value(3));

        matrix[0][1].set(2, 5.0);
        assertEquals(10.0, matrix[0][1].value(0));
        assertEquals(10.0, matrix[0][1].value(1));
        assertEquals(5.0, matrix[0][1].value(2));
        assertEquals(10.0, matrix[0][1].value(3));

        matrix[0][1].set(1, 1.0);
        assertEquals(10.0, matrix[0][1].value(0));
        assertEquals(1.0, matrix[0][1].value(1));
        assertEquals(5.0, matrix[0][1].value(2));
        assertEquals(10.0, matrix[0][1].value(3));
    }
}