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

import static net.sf.lucis.core.impl.DocumentSupport.batch;
import static net.sf.lucis.core.impl.DocumentSupport.termId;
import static org.testng.Assert.assertEquals;
import net.sf.lucis.core.Batch;
import net.sf.lucis.core.IndexStatus;
import net.sf.lucis.core.Queryable;
import net.sf.lucis.core.Store;
import net.sf.lucis.core.Writer;
import net.sf.lucis.core.support.Queryables;

import org.testng.annotations.Test;

/**
 * Base tests for stores.
 * @author Emilio Escobar Reyero
 * @author Andres Rodriguez
 */
public abstract class AbstractStoreTest {
	public AbstractStoreTest() {
	}

	private static final Writer writer = new DefaultWriter();

	static <T> IndexStatus write(Store<T> store, Batch<T> batch) throws InterruptedException {
		return writer.write(store, batch);
	}

	abstract Store<Long> createStore();

	private Store<Long> store;
	private Queryable queryable;

	@Test
	public void store() {
		this.store = createStore();
	}

	@Test(dependsOnMethods = "store")
	public void queryable() {
		this.queryable = Queryables.simple(store);
		notFound(-300);
	}

	@Test(dependsOnMethods = "queryable")
	public void add1() throws InterruptedException {
		write(store, batch(1, 100, 1L));
		assertEquals(store.getCheckpoint(), Long.valueOf(1L));
	}

	private void found(int value) {
		DocumentSupport.found(queryable, value);
	}

	private void notFound(int value) {
		DocumentSupport.notFound(queryable, value);
	}

	@Test(dependsOnMethods = { "add1" })
	public void query1() throws Exception {
		found(5);
		notFound(500);
	}

	private Batch<Long> delete(int value, long cp) throws InterruptedException {
		final Batch.Builder<Long> builder = Batch.builder();
		return builder.delete(termId(value)).build(cp);
	}

	@Test(dependsOnMethods = "query1")
	public void delete1() throws InterruptedException {
		write(store, delete(10, 2L));
		assertEquals(store.getCheckpoint(), Long.valueOf(2L));
	}

	@Test(dependsOnMethods = "delete1")
	public void query2() throws Exception {
		found(5);
		notFound(10);
	}

}
