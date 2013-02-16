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

import java.util.concurrent.ScheduledExecutorService;

import net.sf.lucis.core.Batch;
import net.sf.lucis.core.Delays;
import net.sf.lucis.core.IndexStatus;
import net.sf.lucis.core.Indexer;
import net.sf.lucis.core.IndexerService;
import net.sf.lucis.core.Store;
import net.sf.lucis.core.Writer;

/**
 * Lucene-based index manager.
 * @author Andres Rodriguez
 * @param <T> Checkpoint type.
 */
public class DefaultIndexerService<T> extends AbstractIndexService implements IndexerService {
	/** Writer. */
	private final Writer writer;
	/** Index store. */
	private final Store<T> store;
	/** Indexer. */
	private final Indexer<T> indexer;
	/** Delays. */
	private volatile Delays delays = Delays.constant(1000);

	public DefaultIndexerService(Store<T> store, Writer writer, Indexer<T> indexer,
			ScheduledExecutorService externalExecutor, boolean pasive) {
		super(externalExecutor, pasive);
		this.store = store;
		this.writer = writer;
		this.indexer = indexer;
	}

	public DefaultIndexerService(Store<T> store, Writer writer, Indexer<T> indexer) {
		this(store, writer, indexer, null, false);
	}

	/* CONFIGURABLE PROPERTIES */

	public void setDelays(Delays delays) {
		this.delays = delays;
	}

	/* END CONFIGURABLE PROPERTIES. */

	Runnable newTask() {
		return new Task();
	}

	private final class Task extends AbstractTask {
		public void run() {
			final T checkpoint;
			try {
				checkpoint = store.getCheckpoint();
			} catch (Exception e) {
				log().error(e, "Error obtaining checkpoint");
				schedule(IndexStatus.ERROR, delays.getError());
				return;
			}
			final Batch<T> batch;
			try {
				batch = indexer.index(checkpoint);
			} catch (InterruptedException e) {
				interrupted();
				return;
			} catch (RuntimeException e) {
				log().error(e, "Unable to obtain batch");
				schedule(delays.getError());
				return;
			}
			try {
				if (batch == null) {
					log().trace("Empty batch. Nothing to do.");
					schedule(delays.getIdle());
				} else {
					IndexStatus status = writer.write(store, batch);
					if (status == null) {
						log().trace("Writer had nothing to do.");
						schedule(delays.getIdle());
					} else {
						log().trace("Writing complete.");
						schedule(status, delays.getNormal());
					}
				}
			} catch (InterruptedException e) {
				interrupted();
				return;
			} catch (RuntimeException e) {
				log().error(e, "Unable to write batch");
				schedule(delays.getError());
			}
		}
	}

}
