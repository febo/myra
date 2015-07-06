/*
 * SynchronizedArchiveTest.java
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

import junit.framework.TestCase;
import myra.Archive.SynchronizedArchive;

/**
 * <code>SynchronizedArchive</code> class test.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class SynchronizedArchiveTest extends TestCase {
    /**
     * Tests the addition of elements to the archive.
     */
    public void testAdd() {
	Archive<Integer> archive = new Archive.DefaultArchive<>(5);
	Archive<Integer> pool = new SynchronizedArchive<Integer>(archive);

	for (int i = 0; i < 5; i++) {
	    pool.add(new Integer(i));
	}

	assertEquals(5, archive.size());
	assertEquals(0, (int) archive.lowest());
	assertEquals(4, (int) archive.highest());
    }
}