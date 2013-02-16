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
import net.sf.lucis.core.FullIndexer;
import net.sf.lucis.core.Queryable;
import net.sf.lucis.core.impl.ReindexingFSStore.Status;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Objects;

public class DefaultReindexingWriterTest extends AbstractDirectoryTest {
	private String checkpoint = null;
	private ReindexingFSStore store;
	private DefaultReindexingWriter writer;
	private Queryable queryable;
	private int version = 0;

	@BeforeClass
	public void init() {
		create();
		writer = new DefaultReindexingWriter();
	}

	private void create() {
		store = new ReindexingFSStore(getIndexDir());
		assertNotNull(store);
		queryable = new DefaultQueryable(SingleSearcherProvider.of(store));
		assertNotNull(queryable);
	}

	private void check(Status status, boolean empty) {
		assertEquals(store.getStatus(), status);
		if (!empty) {
			DocumentSupport.found(queryable, 353);
			DocumentSupport.notFound(queryable, 35300);
		}
		assertEquals(store.getCheckpoint(), checkpoint);
	}

	private void reindex() throws InterruptedException {
		writer.reindex(store, new Indexer());
		assertEquals(store.getCheckpoint(), checkpoint);
	}

	@Test
	public void test() throws InterruptedException {
		check(Status.DONE01, true);
		reindex();
		check(Status.DONE02, false);
		reindex();
		check(Status.DONE01, false);
		version--;
		reindex();
		check(Status.DONE01, false);
		reindex();
		check(Status.DONE02, false);
		reindex();
		check(Status.DONE01, false);
	}

	@Test(dependsOnMethods = "test")
	public void recreate() throws InterruptedException {
		create();
		check(Status.DONE01, false);
		reindex();
		check(Status.DONE02, false);
		reindex();
		check(Status.DONE01, false);
		version--;
		reindex();
		check(Status.DONE01, false);
		reindex();
		check(Status.DONE02, false);
	}

	private class Indexer implements FullIndexer {
		public void index(Adder adder) throws InterruptedException {
			version++;
			checkpoint = Integer.toString(version);
			if (Objects.equal(checkpoint, adder.getCheckpoint())) {
				adder.skip();
				return;
			}
			for (int i = 0; i < 1000; i++) {
				adder.add(document(i));
			}
			adder.setCheckpoint(checkpoint);
		}
	}
}
