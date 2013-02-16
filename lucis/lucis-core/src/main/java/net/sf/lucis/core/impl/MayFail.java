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

import java.io.IOException;
import java.util.concurrent.Callable;

import net.sf.lucis.core.IndexException;
import net.sf.lucis.core.IndexStatus;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

/**
 * A writer is the object that applies the operations into a store.
 * @author Andres Rodriguez.
 * @param <T> Result type.
 */
public abstract class MayFail<T> {
	private MayFail() {
	}

	public abstract IndexStatus getStatus();

	public T getResult() {
		throw new UnsupportedOperationException();
	}

	public IndexException getException() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Runs a callable translating the exceptions, except {@link InterruptedException}.
	 * @param <V> Result type.
	 * @param callabe Callable to run.
	 * @return The callable result.
	 * @throws IndexException If an error occurs.
	 * @throws InterruptedException if the current task has been interrupted.
	 */
	public static <V> V run(Callable<V> callabe) throws InterruptedException {
		try {
			return callabe.call();
		} catch(InterruptedException ie) {
			throw ie;
		} catch (LockObtainFailedException le) {
			throw new IndexException(IndexStatus.LOCKED, le);
		} catch (CorruptIndexException ce) {
			throw new IndexException(IndexStatus.CORRUPT, ce);
		} catch (IOException ioe) {
			throw new IndexException(IndexStatus.IOERROR, ioe);
		} catch (Exception e) {
			throw new IndexException(IndexStatus.ERROR, e);
		}
	}

	/**
	 * Runs a possibly failed result from the execution of a callable.
	 * @param <V> Result type.
	 * @param callabe Callable to run.
	 * @return The possible failed callable result.
	 * @throws InterruptedException if the current task has been interrupted.
	 */
	public static <V> MayFail<V> of(Callable<V> callabe) throws InterruptedException {
		try {
			final V result = run(callabe);
			return new MayFail<V>() {
				@Override
				public IndexStatus getStatus() {
					return IndexStatus.OK;
				}

				@Override
				public V getResult() {
					return result;
				}
			};
		} catch (final IndexException e) {
			return new MayFail<V>() {
				@Override
				public IndexStatus getStatus() {
					return e.getStatus();
				}

				@Override
				public IndexException getException() {
					return e;
				}
			};
		}
	}

}
