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
 * Inferface for full indexer objects.
 * @author Andres Rodriguez
 * @param <P> Payload type.
 */
public interface FullIndexer<P> extends BaseIndexer<P> {
	/**
	 * Create the index.
	 * @param adder Index adder.
	 * @throws IndexException if an error occurs.
	 * @throws InterruptedException if the current task has been interrupted.
	 */
	P index(Adder adder) throws InterruptedException;
}
