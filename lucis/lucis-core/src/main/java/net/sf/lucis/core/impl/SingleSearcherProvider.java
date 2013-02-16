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

import net.sf.derquinse.lucis.IndexNotAvailableException;
import net.sf.lucis.core.DirectoryProvider;
import net.sf.lucis.core.LucisSearcher;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

public abstract class SingleSearcherProvider implements Supplier<LucisSearcher> {
	public static SingleSearcherProvider of(final Directory directory) {
		Preconditions.checkNotNull(directory);
		return new SingleSearcherProvider() {
			@Override
			Directory directory() {
				return directory;
			}
		};
	}

	public static SingleSearcherProvider of(final DirectoryProvider provider) {
		Preconditions.checkNotNull(provider);
		return new SingleSearcherProvider() {
			@Override
			Directory directory() {
				return provider.getDirectory();
			}
		};
	}

	private SingleSearcherProvider() {
	}

	abstract Directory directory();

	public LucisSearcher get() {
		try {
			Directory d = directory();
			if (!IndexReader.indexExists(d)) {
				d = EmptyDirectory.get();
			}
			final IndexReader reader = IndexReader.open(d);
			return new DefaultLucisSearcher(reader);
		} catch (Exception e) {
			throw new IndexNotAvailableException(e);
		}
	}
}
