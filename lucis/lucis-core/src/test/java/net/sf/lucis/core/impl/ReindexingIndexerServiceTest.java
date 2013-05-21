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

import static net.sf.lucis.core.impl.DocumentSupport.document;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import net.sf.lucis.core.Adder;
import net.sf.lucis.core.Delays;
import net.sf.lucis.core.FullIndexer;
import net.sf.lucis.core.Queryable;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for ReindexingIndexerService.
 * @author Andres Rodriguez
 */
public class ReindexingIndexerServiceTest extends AbstractDirectoryTest {
	private String checkpoint = null;
	private ReindexingFSStore store;
	private DefaultReindexingWriter writer;
	private Queryable queryable;
	private ReindexingIndexerService<Object> service = null;

	@BeforeClass
	public void init() {
		store = new ReindexingFSStore(getIndexDir());
		assertNotNull(store);
		queryable = new DefaultQueryable(SingleSearcherProvider.of(store));
		assertNotNull(queryable);
		writer = new DefaultReindexingWriter();
	}

	private void check() {
		assertEquals(DocumentSupport.count(queryable), 1000);
		DocumentSupport.found(queryable, 353);
		DocumentSupport.notFound(queryable, 35300);
	}

	@Test
	public void create() {
		service = new ReindexingIndexerService<Object>(store, writer, new Indexer());
		service.setDelays(Delays.constant(10L));
		assertNotNull(service);
		service.start();
	}

	private long cp() {
		String s = store.getCheckpoint();
		if (s == null) {
			return 0;
		}
		return Long.parseLong(s);
	}

	@Test(dependsOnMethods = "create", timeOut = 60000L)
	public void test() throws InterruptedException {
		long cp = 0;
		do {
			Thread.sleep(50L);
			cp = cp();
			if (cp > 1) {
				check();
			}
		} while (cp < 20);
	}

	@Test(dependsOnMethods = "test")
	public void shutdown() {
		service.stop();
	}

	private class Indexer implements FullIndexer<Object> {
		private int version = 0;

		public Object index(Adder adder) throws InterruptedException {
			version++;
			checkpoint = Integer.toString(version);
			for (int i = 0; i < 1000; i++) {
				adder.add(document(i));
			}
			adder.setCheckpoint(checkpoint);
			return null;
		}
		
		public void afterCommit(Object payload) {
		}
	}
}
