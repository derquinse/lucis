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

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;

import net.sf.lucis.core.Delays;
import net.sf.lucis.core.FullIndexer;
import net.sf.lucis.core.IndexException;
import net.sf.lucis.core.IndexStatus;
import net.sf.lucis.core.IndexerService;
import net.sf.lucis.core.ReindexingStore;
import net.sf.lucis.core.ReindexingWriter;

/**
 * Default reindexing service.
 * @author Andres Rodriguez
 * @param <P> Payload type.
 */
public class ReindexingIndexerService<P> extends AbstractIndexService implements IndexerService {
	/** Writer. */
	private final ReindexingWriter writer;
	/** Index store. */
	private final ReindexingStore store;
	/** Indexer. */
	private final FullIndexer<P> indexer;
	/** Delays. */
	private volatile Delays delays = Delays.constant(600000);

	public ReindexingIndexerService(ReindexingStore store, ReindexingWriter writer, FullIndexer<P> indexer,
			ScheduledExecutorService externalExecutor, boolean pasive) {
		super(externalExecutor, pasive);
		this.store = checkNotNull(store);
		this.writer = checkNotNull(writer);
		this.indexer = checkNotNull(indexer);
	}

	public ReindexingIndexerService(ReindexingStore store, ReindexingWriter writer, FullIndexer<P> indexer) {
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
			final Callable<P> callable = new Callable<P>() {
				public P call() throws Exception {
					return writer.reindex(store, indexer);
				}
			};
			try {
				P payload = MayFail.run(callable);
				log().trace("Reindexing complete. Calling post commit hook");
				if (payload != null) {
					postCommit(payload);
				}
				log().trace("Post commit hook completed.");
				schedule(IndexStatus.OK, delays.getNormal());
			} catch (IndexException e) {
				log().error(e, "Index exception while reindexing");
				schedule(e.getStatus(), delays.getError());
			} catch (InterruptedException e) {
				interrupted();
				return;
			} catch (Exception ex) {
				log().error(ex, "Exception while reindexing");
				schedule(IndexStatus.ERROR, delays.getError());
			}
		}

		private void postCommit(P payload) {
			try {
				indexer.afterCommit(payload);
			} catch (Throwable t) {
				log().error(t, "Error processing post commit hook");
			}
		}

	}

}
