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
 * A store is the combination of a Lucene directory and a checkpoint.
 * @author Andres Rodriguez
 * @param <T> Checkpoint type.
 */
public interface Store<T> extends DirectoryProvider {
	/**
	 * Returns the current checkpoint.
	 * @return The current checkpoint.
	 */
	T getCheckpoint() throws StoreException;

	/**
	 * Sets the current checkpoint.
	 * @param checkpoint Checkpoint to set. Must not be null.
	 * @throws Exception
	 */
	void setCheckpoint(T checkpoint) throws StoreException;
}
