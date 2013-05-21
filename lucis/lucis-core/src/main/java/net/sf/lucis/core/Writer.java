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
 * A writer is the object that applies the operations into a store.
 * @author Andres Rodriguez.
 */
public interface Writer {
	/**
	 * Applies a batch of operations.
	 * @param <T> Checkpoint type.
	 * @param <P> Payload type.
	 * @param store Destination store.
	 * @param batch Batch to apply.
	 * @return The final index status or {@code null} if the batch is {@code null} or the final
	 *         checkpoint is equals to the initial one.
	 * @throws InterruptedException if the current task has been interrupted.
	 */
	<T, P> IndexStatus write(Store<T> store, Batch<T, P> batch) throws InterruptedException;
}
