/*
 * Copyright (C) the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.derquinse.lucis;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import net.derquinse.common.test.HessianSerializabilityTests;
import net.derquinse.common.test.SerializabilityTests;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Simple test.
 * @author Emilio Escobar Reyero
 */
public class PageTest {

	Page<String> result;

	final int totalHits = 102;
	final float maxScore = 1;
	final long time = 320;
	final int firstResult = 0;
	final int size = 10;

	@BeforeMethod
	public void before() {
		List<String> items = createList(size);
		result = new Page<String>(totalHits, maxScore, time, firstResult, items);
	}

	private List<String> createList(int tam) {
		List<String> items = new ArrayList<String>(tam);

		for (int i = 0; i < tam; i++) {
			items.add("identificador" + i);
		}

		return items;
	}

	@Test
	public void data() {
		int localTotalHits = result.getTotalHits();
		int localFirstResult = result.getFirstResult();
		int localSize = result.size();

		assertEquals(localTotalHits, totalHits);
		assertEquals(localFirstResult, firstResult);
		assertEquals(localSize, size);
		String firstItem = result.get(localFirstResult);
		Assert.assertEquals(firstItem, "identificador0");
	}
	
	@Test
	public void serializability() {
		SerializabilityTests.check(result, false);
		HessianSerializabilityTests.both(result, false);
	}
	

}
