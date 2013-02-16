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

import java.io.IOException;

import net.sf.lucis.core.Factory;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/**
 * Empty Directory.
 * @author Andres Rodriguez
 */
final class EmptyDirectory {
	/** Not instantiable. */
	private EmptyDirectory() {
		throw new AssertionError();
	}

	private static volatile Directory directory = null;

	public static Directory get() throws IOException {
		if (directory != null) {
			return directory;
		}
		create();
		return directory;
	}

	private static synchronized void create() throws IOException {
		if (directory != null) {
			return;
		}
		final RAMDirectory ram = new RAMDirectory();
		IndexWriter w = new IndexWriter(ram, Factory.get().writerConfig());
		w.commit();
		w.close();
		directory = ram;
	}
}
