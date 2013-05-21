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
package net.sf.lucis.core;

/**
 * A reindexing writer is the object that writes the documents into a reindexing store.
 * @author Andres Rodriguez.
 */
public interface ReindexingWriter {
	/**
	 * Reindex.
	 * @param <P> Payload type.
	 * @param store Destination store.
	 * @param indexer Indexer.
	 * @throws IndexException if an error occurs.
	 * @throws InterruptedException if the current task has been interrupted.
	 */
	<P> P reindex(ReindexingStore store, FullIndexer<P> indexer) throws InterruptedException;
}
