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

import static com.google.common.base.Preconditions.checkNotNull;
import net.sf.lucis.core.Store;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/**
 * RAM-based store.
 * @author Andres Rodriguez
 * @param <T> Checkpoint type.
 */
public class RAMStore<T> extends AbstractStore implements Store<T> {
	/** Index store. */
	private final RAMDirectory directory;
	/** Checkpoint. */
	private T checkpoint;

	public RAMStore(final Store<T> store) throws Exception {
		checkNotNull(store, "A source store must be provided.");
		this.checkpoint = store.getCheckpoint();
		this.directory = new RAMDirectory(store.getDirectory());
	}

	public RAMStore() {
		this.directory = new RAMDirectory();
		this.checkpoint = null;
	}

	public Directory getDirectory() {
		return directory;
	}

	public T getCheckpoint() {
		return checkpoint;
	}

	public Object getVersion() {
		return checkpoint;
	}

	public void setCheckpoint(T checkpoint) {
		checkNotNull(checkpoint, "A checkpoint must be provided.");
		this.checkpoint = checkpoint;
		changed();
	};
}
