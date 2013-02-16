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

import static com.google.common.base.Preconditions.checkState;
import static net.sf.lucis.core.Interruption.throwIfInterrupted;

import java.util.concurrent.Callable;

import net.sf.lucis.core.Adder;
import net.sf.lucis.core.FullIndexer;
import net.sf.lucis.core.IndexException;
import net.sf.lucis.core.IndexStatus;
import net.sf.lucis.core.ReindexingStore;
import net.sf.lucis.core.ReindexingWriter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;

import com.google.common.base.Supplier;

/**
 * Default reindexing writer.
 * @author Andres Rodriguez.
 */
public final class DefaultReindexingWriter extends AbstractWriter implements ReindexingWriter {
	public DefaultReindexingWriter() {
	}

	public DefaultReindexingWriter(Supplier<IndexWriterConfig> config) {
		super(config);
	}

	public void reindex(ReindexingStore store, FullIndexer indexer) throws InterruptedException {
		final AdderImpl adder = new AdderImpl(store.getDestinationDirectory(), store.getCheckpoint());
		final boolean indexed;
		try {
			indexer.index(adder);
			indexed = adder.done();
			adder.commit();
		} finally {
			adder.close();
		}
		if (indexed) {
			store.reindexed(adder.getCheckpoint());
		}
	}

	/**
	 * Default adder implementation. Not thread safe.
	 * @author Andres Rodriguez.
	 */
	private class AdderImpl implements Adder {
		private final Directory directory;
		private final IndexWriterConfig config;
		private IndexWriter writer = null;
		private IndexStatus status = IndexStatus.OK;
		private String checkpoint;
		private boolean skipped = false;
		private boolean done = false;

		private AdderImpl(final Directory directory, String checkpoint) {
			this.directory = directory;
			this.checkpoint = checkpoint;
			this.config = config().setOpenMode(OpenMode.CREATE);
		}

		void on() throws InterruptedException {
			throwIfInterrupted();
			checkState(!done, "Adder already closed");
			if (writer == null) {
				final Callable<IndexWriter> callable = new Callable<IndexWriter>() {
					public IndexWriter call() throws Exception {
						return new IndexWriter(directory, config);
					}
				};
				writer = MayFail.run(callable);
			}
		}

		public void add(Document document) throws InterruptedException {
			add(document, config.getAnalyzer());
		}

		public void add(final Document document, final Analyzer analyzer) throws InterruptedException {
			throwIfInterrupted();
			if (skipped) {
				return;
			}
			try {
				on();
				final Callable<Object> callable = new Callable<Object>() {
					public Object call() throws Exception {
						writer.addDocument(document, analyzer);
						return null;
					}
				};
				MayFail.run(callable);
			} catch (IndexException e) {
				status = e.getStatus();
				throw e;
			}
		}

		/**
		 * Called after the indexing is done.
		 * @return If the indexing has been performed.
		 */
		private boolean done() throws InterruptedException {
			if (writer == null && !skipped) {
				on();
			}
			done = true;
			return !skipped;
		}

		private void commit() throws InterruptedException {
			if (writer == null) {
				return;
			}
			final Callable<Object> callable = new Callable<Object>() {
				public Object call() throws Exception {
					try {
						if (status == IndexStatus.OK) {
							writer.commit();
							throwIfInterrupted();
						} else {
							writer.rollback();
						}
					} finally {
						close();
					}
					return null;
				}
			};
			MayFail.run(callable);
		}

		private void close() throws InterruptedException {
			if (writer == null) {
				return;
			}
			final Callable<Object> callable = new Callable<Object>() {
				public Object call() throws Exception {
					writer.close();
					writer = null;
					return null;
				}
			};
			MayFail.run(callable);
		}

		public String getCheckpoint() throws InterruptedException {
			throwIfInterrupted();
			return checkpoint;
		}

		public void setCheckpoint(String checkpoint) throws InterruptedException {
			throwIfInterrupted();
			this.checkpoint = checkpoint;
		}

		public void skip() throws InterruptedException {
			throwIfInterrupted();
			this.skipped = true;
		}

	}

}
