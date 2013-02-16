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

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;

/**
 * Document adder for full indexers. Implementations are not thread-safe.
 * @author Andres Rodriguez
 */
@NotThreadSafe
public interface Adder {
	/**
	 * Returns the current checkpoint.
	 * @return The current checkpoint or {@code null} if no checkpoint is available.
	 * @throws InterruptedException if the current task has been interrupted.
	 */
	String getCheckpoint() throws InterruptedException;

	/**
	 * Sets the current checkpoint.
	 * @param checkpoint New checkpoint.
	 * @throws InterruptedException if the current task has been interrupted.
	 */
	void setCheckpoint(@Nullable String checkpoint) throws InterruptedException;

	/**
	 * Notifies the adder that the current run should be skipped.
	 * @throws InterruptedException if the current task has been interrupted.
	 */
	void skip() throws InterruptedException;

	/**
	 * Adds a document to the index.
	 * @param document Document to add.
	 * @throws IndexException if an error occurs.
	 * @throws InterruptedException if the current task has been interrupted.
	 */
	void add(Document document) throws InterruptedException;

	/**
	 * Adds a document to the index.
	 * @param document Document to add.
	 * @param analyzer Analzer to use.
	 * @throws IndexException if an error occurs.
	 * @throws InterruptedException if the current task has been interrupted.
	 */
	void add(Document document, Analyzer analyzer) throws InterruptedException;
}
