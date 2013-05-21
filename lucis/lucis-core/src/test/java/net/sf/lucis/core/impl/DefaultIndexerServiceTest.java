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
package net.sf.lucis.core.impl;

import static org.testng.Assert.assertNotNull;
import net.sf.lucis.core.Batch;
import net.sf.lucis.core.Delays;
import net.sf.lucis.core.Indexer;
import net.sf.lucis.core.Queryable;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for DefaultIndexerService.
 * @author Andres Rodriguez
 */
public class DefaultIndexerServiceTest {
	private RAMStore<Long> store;
	private DefaultWriter writer;
	private Queryable queryable;
	private volatile int position = 0;
	private DefaultIndexerService<Long, Object> service = null;

	@BeforeClass
	public void init() {
		store = new RAMStore<Long>();
		queryable = new DefaultQueryable(SingleSearcherProvider.of(store));
		writer = new DefaultWriter();
	}

	private void check() {
		Long cpObj = store.getCheckpoint();
		int cp = cpObj != null ? cpObj.intValue() : 0;
		if (cp > 0) {
			DocumentSupport.found(queryable, cp - 20);
			DocumentSupport.notFound(queryable, cp + 10000);
		}
	}

	@Test
	public void create() {
		service = new DefaultIndexerService<Long, Object>(store, writer, new TestIndexer());
		service.setDelays(Delays.constant(10L));
		assertNotNull(service);
		service.start();
	}

	@Test(dependsOnMethods = "create")
	public void test() throws InterruptedException {
		for (int i = 0; i < 50; i++) {
			Thread.sleep(50L);
			check();
		}
	}

	@Test(dependsOnMethods = "test")
	public void shutdown() {
		service.stop();
	}

	private class TestIndexer implements Indexer<Long, Object> {
		public Batch<Long, Object> index(Long checkpoint) throws InterruptedException {
			int start = position;
			position += 1000;
			int end = position - 1;
			return DocumentSupport.batch(start, end, end);
		}

		public void afterCommit(Object payload) {
		}
	}
}
