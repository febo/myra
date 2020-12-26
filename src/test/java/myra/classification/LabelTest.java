/*
 * LabelTest.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2020 Fernando Esteban Barril Otero
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

package myra.classification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * <code>LabelTest</code> class test.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class LabelTest {
    @Test
    public void testRemove() {
        Label label = new Label(10, 9);
        assertEquals(10, label.active().length);
        assertEquals(10, label.probabilities().length);
        assertTrue(label.cardinality() == 1);
        assertTrue(label.active(9));

        label.remove(0, 8);

        assertEquals(8, label.active().length);
        assertEquals(8, label.probabilities().length);
        assertTrue(label.cardinality() == 1);
        assertTrue(label.active(7));

        label = new Label(5, 0);
        assertTrue(label.active(0));

        label.remove(1, 2, 3, 4);
        assertEquals(1, label.probabilities().length);
        assertTrue(label.active(0));

        label = new Label(5, 4);
        assertTrue(label.active(4));

        label.remove(0, 1, 2, 3);
        assertEquals(1, label.probabilities().length);
        assertTrue(label.active(0));
    }
}