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

import net.sf.derquinse.lucis.SearchException;
import net.sf.lucis.core.LucisSearcher;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;

/**
 * Default LucisSearcher implementation.
 * @author Andres Rodriguez
 */
public final class DefaultLucisSearcher implements LucisSearcher {
	/** Index reader. */
	private final IndexReader reader;
	/** Index searcher. */
	private final IndexSearcher searcher;

	DefaultLucisSearcher(IndexReader reader) {
		this.reader = Preconditions.checkNotNull(reader, "The index reader must be provided");
		this.searcher = new IndexSearcher(reader);
	}

	public final Document doc(int i) {
		try {
			return searcher.doc(i);
		} catch (CorruptIndexException e) {
			throw new SearchException(e);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.lucis.core.LucisSearcher#rewrite(org.apache.lucene.search.Query)
	 */
	public final Query rewrite(Query query) {
		try {
			return searcher.rewrite(query);
		} catch (CorruptIndexException e) {
			throw new SearchException(e);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	public final void search(Query query, Filter filter, Collector results) {
		try {
			searcher.search(query, filter, results);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	public final TopDocs search(Query query, Filter filter, int n) {
		try {
			return searcher.search(query, filter, n);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	public final TopFieldDocs search(Query query, Filter filter, int n, Sort sort) {
		try {
			return searcher.search(query, filter, n, sort);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}

	public void close() {
		try {
			Closeables.close(searcher, true);
			Closeables.close(reader, true);
		} catch(IOException e) {
			// TODO
		}
	}
}
