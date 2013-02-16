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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import net.derquinse.common.io.DurableFiles;
import net.sf.lucis.core.Factory;
import net.sf.lucis.core.ReindexingStore;
import net.sf.lucis.core.StoreException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * File system-based reindexing store.
 * @author Andres Rodriguez
 */
public class ReindexingFSStore extends AbstractStore implements ReindexingStore {
	/** Name of the control file. */
	private static final String STATUS_FILE = "status.ctl";
	/** Name of the control file. */
	private static final String CHECKPOINT_FILE = "checkpoint.ctl";
	/** Initial state. */
	private static final State STATE = new State(Status.DONE01);
	/** Relative path to first copy. */
	private static final String COPY01 = "copy01";
	/** Relative path to second copy. */
	private static final String COPY02 = "copy02";
	/** Main directory. */
	private final File file;
	/** Status file. */
	private final File statusFile;
	/** Status file. */
	private final File checkpointFile;

	/** Status. */
	enum Status {
		NULL, DONE01, DONE02
	}

	/** First copy. */
	private final Copy copy01;
	/** Second copy. */
	private final Copy copy02;
	/** Version. */
	private final AtomicReference<State> state;

	private static Status readStatus(File file) {
		if (!file.exists()) {
			return Status.NULL;
		}
		try {
			String str = Files.toString(file, Charsets.UTF_8);
			if (str == null || str.length() == 0) {
				return Status.NULL;
			}
			str = str.trim();
			if (str.length() == 0) {
				return Status.NULL;
			}
			try {
				return Enum.valueOf(Status.class, str);
			} catch (IllegalArgumentException e) {
				return Status.NULL;
			}
		} catch (FileNotFoundException fnfe) {
			// Nothing.
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return Status.NULL;
	}

	private static File file(File base, String name, String description) {
		final File f = new File(base, name);
		checkArgument(!f.isDirectory(), "%s cannot be a directory", description);
		return f;
	}

	public ReindexingFSStore(final String indexDir) {
		try {
			this.file = new File(indexDir);
			checkArgument(file.exists() && file.isDirectory(), "Invalid index directory");
			this.statusFile = file(file, STATUS_FILE, "Status file");
			this.checkpointFile = file(file, CHECKPOINT_FILE, "Checkpoint file");
			this.copy01 = new Copy(file, COPY01);
			this.copy02 = new Copy(file, COPY02);
			final Status status = readStatus(this.statusFile);
			if (Status.NULL.equals(status)) {
				IndexWriter w = new IndexWriter(copy01.directory, Factory.get().writerConfig());
				w.commit();
				w.close();
				state = new AtomicReference<State>(STATE);
				writeStatus();
				writeCheckpoint(null);
			} else {
				state = new AtomicReference<State>(new State(status));
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	Status getStatus() {
		return state.get().status;
	}

	public Directory getDirectory() {
		switch (getStatus()) {
		case NULL:
			return null;
		case DONE01:
			return copy01.directory;
		case DONE02:
			return copy02.directory;
		}
		throw new AssertionError();
	}

	public Directory getDestinationDirectory() {
		switch (getStatus()) {
		case DONE01:
			return copy02.directory;
		default:
			return copy01.directory;
		}
	}

	public String getCheckpoint() throws StoreException {
		if (!checkpointFile.exists()) {
			return null;
		}
		try {
			return Files.toString(checkpointFile, Charsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}

	public void reindexed(String checkpoint) {
		try {
			state.set(state.get().increment());
			writeStatus();
			writeCheckpoint(checkpoint);
		} finally {
			changed();
		}
	}

	private void writeStatus() {
		try {
			DurableFiles.write(getStatus().name(), statusFile, Charsets.UTF_8);
		} catch (IOException e) {
		}
	}

	private void writeCheckpoint(String checkpoint) {
		try {
			if (checkpoint == null) {
				if (checkpointFile.exists()) {
					checkpointFile.delete();
				}
			} else {
				DurableFiles.write(checkpoint, checkpointFile, Charsets.UTF_8);
			}
		} catch (IOException e) {
		}
	}

	public Object getVersion() {
		return state.get().version;
	}

	private static final class Copy {
		private final File file;
		private final Directory directory;

		Copy(File base, String path) throws IOException {
			this.file = new File(base, path);
			checkArgument(!file.exists() || (file.exists() && file.isDirectory()), "Invalid index copy directory");
			if (!file.exists()) {
				file.mkdir();
			}
			this.directory = FSDirectory.open(this.file);
		}
	}

	private static final class State {
		private final Status status;
		private final int version;

		State(Status status, int version) {
			this.status = status;
			this.version = version;
		}

		State(Status status) {
			this(status, 0);
		}

		State increment() {
			Status ns = status == Status.DONE01 ? Status.DONE02 : Status.DONE01;
			return new State(ns, version + 1);
		}
	}

}
