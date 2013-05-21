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

import static net.sf.lucis.core.Interruption.throwIfInterrupted;

import java.io.IOException;

import net.sf.lucis.core.Batch;
import net.sf.lucis.core.Batch.Addition;
import net.sf.lucis.core.IndexStatus;
import net.sf.lucis.core.Store;
import net.sf.lucis.core.Writer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

/**
 * Default writer implementation.
 * @author Andres Rodriguez.
 */
public final class DefaultWriter extends AbstractWriter implements Writer {
	public DefaultWriter() {
	}

	public DefaultWriter(Supplier<IndexWriterConfig> config) {
		super(config);
	}

	public <T, P> IndexStatus write(Store<T> store, Batch<T, P> batch) throws InterruptedException {
		Preconditions.checkNotNull(store, "A destination store must be provided.");
		if (batch == null) {
			return null;
		}
		try {
			final IndexWriterConfig config = config();
			final T oldCP = store.getCheckpoint();
			final T newCP = batch.getCheckpoint();
			if (Objects.equal(oldCP, newCP)) {
				return null;
			}
			throwIfInterrupted();
			if (!batch.isEmpty()) {
				final Analyzer analyzer = config.getAnalyzer();
				// Check whether the index must be created
				final Directory directory = store.getDirectory();
				config.setOpenMode(batch.isRecreate() ? OpenMode.CREATE : OpenMode.CREATE_OR_APPEND);
				final IndexWriter writer = new IndexWriter(directory, config);
				boolean ok = false;
				try {
					// Deletions
					if (!batch.isRecreate()) {
						for (Term term : batch.getDeletions()) {
							throwIfInterrupted();
							writer.deleteDocuments(term);
						}
					}
					// Additions
					for (Addition addition : batch.getAdditions()) {
						throwIfInterrupted();
						final Analyzer aa = addition.getAnalyzer();
						writer.addDocument(addition.getDocument(), aa != null ? aa : analyzer);
					}
					// Commit
					throwIfInterrupted();
					writer.commit();
					ok = true;
					// No optimize until policy is defined.
					// writer.optimize();
				} finally {
					if (!ok) {
						rollback(writer);
					}
					writer.close();
				}
			}
			store.setCheckpoint(newCP);
			return IndexStatus.OK;
		} catch (InterruptedException ie) {
			throw ie;
		} catch (LockObtainFailedException le) {
			log().error(le, "Unable to lock index");
			return IndexStatus.LOCKED;
		} catch (CorruptIndexException ce) {
			log().error(ce, "Corrupt index");
			return IndexStatus.CORRUPT;
		} catch (IOException ioe) {
			log().error(ioe, "I/O Error while writing");
			return IndexStatus.IOERROR;
		} catch (Exception e) {
			log().error(e, "Exception while writing");
			return IndexStatus.ERROR;
		}
	}

	private void rollback(IndexWriter writer) {
		try {
			writer.rollback();
		} catch (Exception e) {
		}
	}
}
