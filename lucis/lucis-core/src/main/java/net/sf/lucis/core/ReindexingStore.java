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

import org.apache.lucene.store.Directory;

/**
 * A directory provider for full reindexings.
 * @author Andres Rodriguez
 */
public interface ReindexingStore extends DirectoryProvider {
	/**
	 * Returns the current checkpoint.
	 * @return The current checkpoint or {@code null} if no checkpoint is available.
	 */
	String getCheckpoint() throws StoreException;

	/**
	 * Returns the directory in which the new index must be created.
	 * @return The destitation directory.
	 * @throws IndexException if an error occurs.
	 */
	Directory getDestinationDirectory();

	/**
	 * Called when a reindexing process has been completed.
	 * @param checkpoint New index checkpoint.
	 */
	void reindexed(@Nullable String checkpoint);
}
