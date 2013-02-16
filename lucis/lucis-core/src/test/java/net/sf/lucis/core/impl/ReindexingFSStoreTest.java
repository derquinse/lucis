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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.util.UUID;

import net.sf.lucis.core.impl.ReindexingFSStore.Status;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.testng.annotations.Test;

public class ReindexingFSStoreTest extends AbstractDirectoryTest {
	private Directory first;
	private Directory second;
	private String checkpoint = null;
	private ReindexingFSStore store;

	private void create() {
		store = new ReindexingFSStore(getIndexDir());
	}

	private File file(Directory d) {
		return ((FSDirectory)d).getFile();
	}

	private void assertFile(Directory actual, Directory expected) {
		assertEquals(file(actual), file(expected));
	}
	
	private void check(Status status, Directory read, Directory write) {
		assertNotNull(store);
		Directory d1 = store.getDirectory();
		Directory d2 = store.getDestinationDirectory();
		assertNotNull(d1);
		assertNotNull(d2);
		assertEquals(store.getStatus(), status);
		if (read != null) {
			assertFile(d1, read);
		}
		if (write != null) {
			assertFile(d2, write);
		}
		assertEquals(store.getCheckpoint(), checkpoint);
	}

	private void reindex() {
		store.reindexed(checkpoint);
		assertEquals(store.getCheckpoint(), checkpoint);
	}

	@Test
	public void createStoreTest() {
		create();
		check(Status.DONE01, null, null);
		first = store.getDirectory();
		second = store.getDestinationDirectory();
	}

	@Test(dependsOnMethods = "createStoreTest")
	public void recreateStoreTest() {
		create();
		check(Status.DONE01, first, second);
	}

	@Test(dependsOnMethods = "recreateStoreTest")
	public void reindexTest() {
		reindex();
		check(Status.DONE02, second, first);
		checkpoint = UUID.randomUUID().toString();
		reindex();
		check(Status.DONE01, first, second);
	}

	@Test(dependsOnMethods = "reindexTest")
	public void recreateAndReindexTest() {
		create();
		check(Status.DONE01, first, second);
		checkpoint = null;
		reindex();
		check(Status.DONE02, second, first);
		checkpoint = UUID.randomUUID().toString();
		reindex();
		check(Status.DONE01, first, second);
	}
}
