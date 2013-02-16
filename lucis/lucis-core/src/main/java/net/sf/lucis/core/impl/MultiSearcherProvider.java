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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import net.sf.derquinse.lucis.IndexNotAvailableException;
import net.sf.lucis.core.DirectoryProvider;
import net.sf.lucis.core.LucisSearcher;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.store.Directory;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * MultiSearcher simple implementation.
 * @author Andres Rodriguez
 */
public final class MultiSearcherProvider implements Supplier<LucisSearcher> {
	private final ImmutableList<DirectoryProvider> providers;

	public static MultiSearcherProvider of(Iterable<DirectoryProvider> providers) {
		return new MultiSearcherProvider(providers);
	}

	private MultiSearcherProvider(Iterable<DirectoryProvider> providers) {
		this.providers = ImmutableList.copyOf(checkNotNull(providers, "The directory providers are mandatory."));
	}

	public LucisSearcher get() {
		try {
			List<IndexReader> readers = Lists.newArrayListWithCapacity(providers.size());
			for (DirectoryProvider p : providers) {
				Directory d = p.getDirectory();
				if (d != null && IndexReader.indexExists(d)) {
					readers.add(IndexReader.open(d));
				}
			}
			if (readers.isEmpty()) {
				final IndexReader reader = IndexReader.open(EmptyDirectory.get());
				readers.add(reader);
			}
			return new DefaultLucisSearcher(new MultiReader(readers.toArray(new IndexReader[readers.size()])));
		} catch (Exception e) {
			throw new IndexNotAvailableException(e);
		}
	}

}
