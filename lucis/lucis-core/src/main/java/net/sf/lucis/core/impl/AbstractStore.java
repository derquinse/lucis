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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.concurrent.GuardedBy;

import net.derquinse.common.log.ContextLog;
import net.sf.lucis.core.DirectoryProvider;
import net.sf.lucis.core.Loggers;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;

import com.google.common.base.Stopwatch;
import com.google.common.io.Closeables;

/**
 * Abstract store implementation.
 * @author Andres Rodriguez.
 */
abstract class AbstractStore extends AbstractNamed implements DirectoryProvider {
	/** Maximum time without reader checking. */
	private static final long MAX_HOLD_MS = 5000L;
	/** Sequence number. */
	private final AtomicLong sequence = new AtomicLong();
	/** Internal lock. */
	private final Lock lock = new ReentrantLock();
	/** Stopwatch . */
	@GuardedBy("lock")
	private final Stopwatch watch = Stopwatch.createUnstarted();
	/** Last sequence. */
	@GuardedBy("lock")
	private long lastSequence = Long.MIN_VALUE;
	/** Last directory. */
	@GuardedBy("lock")
	private Directory lastDirectory = null;
	/** Whether the last reader is managed. */
	@GuardedBy("lock")
	private boolean lastManaged = false;
	/** Last reader. */
	@GuardedBy("lock")
	private IndexReader reader;

	AbstractStore() {
	}

	@Override
	final ContextLog baseLog() {
		return Loggers.store();
	}

	@Override
	final String contextFormat() {
		return "Store[%s]";
	}

	/** Called when the index has changed. */
	final void changed() {
		sequence.incrementAndGet();
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.lucis.core.DirectoryProvider#getManagedReader()
	 */
	public final IndexReader getManagedReader() throws IOException {
		lock.lock();
		boolean ok = false;
		try {
			if (!lastManaged) {
				newReader();
			} else if (lastSequence != sequence.get() || watch.elapsed(TimeUnit.MILLISECONDS) > MAX_HOLD_MS) {
				reopen();
			} else {
				reader.incRef();
			}
			ok = true;
			return reader;
		} finally {
			if (!ok) {
				shutdown();
			}
			lock.unlock();
		}
	}

	private void setManagedReader(IndexReader r) {
		r.incRef();
		reader = r;
		lastManaged = true;
		watch.reset().start();
	}

	private void newReader() throws IOException {
		lastDirectory = getDirectory();
		if (lastDirectory == null || !IndexReader.indexExists(lastDirectory)) {
			log().warn("Directory [%s] does not exist. Using empty one.", lastDirectory);
			newEmpty();
		} else {
			try {
				setManagedReader(IndexReader.open(lastDirectory));
			} catch (IOException e) {
				log().error(e, "Unable to open directory [%s]. Using empty one.", lastDirectory);
				newEmpty();
			}
		}
	}

	private void newEmpty() throws IOException {
		lastManaged = false;
		lastDirectory = EmptyDirectory.get();
		lastSequence = Long.MIN_VALUE;
		reader = IndexReader.open(lastDirectory);
		watch.reset();
	}

	private void shutdown() {
		try {
			Closeables.close(reader, true);
			reader = null;
			lastManaged = false;
			watch.reset();
		} catch (IOException e) {
			// TODO: Log
		}
	}

	private void reopen() throws IOException {
		Directory newDirectory = getDirectory();
		if (lastDirectory != newDirectory) {
			shutdown();
			newReader();
		} else {
			try {
				IndexReader r = IndexReader.openIfChanged(reader);
				if (r != null) {
					shutdown();
					setManagedReader(r);
				} else {
					setManagedReader(reader);
				}
			} catch (IOException e) {
				log().error(e, "Unable to reopen directory [%s]. Using empty one.", lastDirectory);
				newEmpty();
			}
		}
	}

}
