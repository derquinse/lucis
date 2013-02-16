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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.derquinse.common.io.DurableFiles;
import net.sf.lucis.core.Checkpoint;
import net.sf.lucis.core.Store;
import net.sf.lucis.core.StoreException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.google.common.io.ByteStreams;

/**
 * File system-based store.
 * @author Andres Rodriguez
 * @param <T> Checkpoint type.
 */
public class FSStore<T> extends AbstractStore implements Store<T> {
	/** Name of the control file. */
	private static final String CONTROL_FILE = "checkpoint.ctl";
	/** Directory that contains de index. */
	private final File file;
	/** Control file. */
	private final File control;
	/** Index store. */
	private final FSDirectory directory;
	/** Checkpoint serializer. */
	private final Checkpoint<T> serializer;

	public FSStore(final Checkpoint<T> serializer, final String indexDir) {
		checkNotNull(serializer, "A checkpoint serializer must be provided.");
		this.serializer = serializer;
		try {
			this.file = new File(indexDir);
			checkArgument(file.exists() && file.isDirectory(), "Invalid index directory");
			this.control = new File(file, CONTROL_FILE);
			checkArgument(!control.isDirectory(), "Control file cannot be a directory");
			this.directory = FSDirectory.open(file);
		} catch (IOException e) {
			throw new StoreException(e);
		}
	}

	public Directory getDirectory() {
		return directory;
	}

	private void close(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException ioe) {
		}
	}

	public T getCheckpoint() {
		if (!control.exists()) {
			return null;
		}
		try {
			final FileInputStream stream = new FileInputStream(control);
			try {
				return serializer.read(stream);
			} finally {
				close(stream);
			}
		} catch (FileNotFoundException fnfe) {
			// Nothing.
		} catch (IOException e) {
			throw new StoreException(e);
		}
		return null;
	}

	public void setCheckpoint(T checkpoint) {
		byte[] data;
		try {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
			serializer.write(checkpoint, bos);
			data = bos.toByteArray();
			DurableFiles.copy(ByteStreams.newInputStreamSupplier(data), control);
		} catch (IOException e) {
			throw new StoreException(e);
		} finally {
			changed();
		}
	}

	public Object getVersion() {
		try {
			return getCheckpoint();
		} catch (StoreException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return "FSStore[" + file.getAbsolutePath() + "]";
	}

}
