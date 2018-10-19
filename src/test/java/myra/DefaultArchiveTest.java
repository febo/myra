/*
 * DefaultArchiveTest.java
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

/**
 * <code>DefaultArchive</code> class test.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class DefaultArchiveTest extends TestCase {
    /**
     * Tests the addition of elements to the archive.
     */
    public void testAdd() {
	Archive<WInteger> archive = new Archive.DefaultArchive<>(5);

	for (int i = 0; i < 5; i++) {
	    archive.add(new WInteger(i));
	}

	assertEquals(5, archive.size());
	assertEquals(0, archive.lowest().intValue());
	assertEquals(4, archive.highest().intValue());

	archive.add(new WInteger(5));
	assertEquals(1, archive.lowest().intValue());

	assertFalse(archive.add(new WInteger(0)));
	assertEquals(5, archive.size());

	for (int i = 6; i < 10; i++) {
	    archive.add(new WInteger(i));
	}

	assertEquals(5, archive.size());
	assertEquals(5, archive.lowest().intValue());
	assertEquals(9, archive.highest().intValue());
    }

    public static class WInteger extends Number implements Weighable<WInteger> {
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Wrapped <code>Integer</code> object.
	 */
	private Integer value;

	/**
	 * The solution weight.
	 */
	private double weight;

	/**
	 * Default constructor.
	 * 
	 * @param value
	 *            the integer value.
	 */
	public WInteger(int value) {
	    this.value = Integer.valueOf(value);
	}

	@Override
	public int compareTo(WInteger o) {
	    return value.compareTo(o.value);
	}

	@Override
	public double getWeight() {
	    return weight;
	}

	@Override
	public void setWeight(double weight) {
	    this.weight = weight;
	}

	@Override
	public int intValue() {
	    return value.intValue();
	}

	@Override
	public long longValue() {
	    return value.longValue();
	}

	@Override
	public float floatValue() {
	    return value.floatValue();
	}

	@Override
	public double doubleValue() {
	    return value.doubleValue();
	}
    }
}