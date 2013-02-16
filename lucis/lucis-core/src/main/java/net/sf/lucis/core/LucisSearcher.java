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

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;

/**
 * Interface for lucis searchers.
 * @author Andres Rodriguez
 */
public interface LucisSearcher {
	void search(Query query, Filter filter, Collector results);

	TopDocs search(Query query, Filter filter, int n);

	TopFieldDocs search(Query query, Filter filter, int n, Sort sort);

	/**
	 * Rewrite a query into primitive queries.
	 * @param query Query to rewrite.
	 * @return The rewritten query.
	 */
	Query rewrite(Query query);

	Document doc(int i);

	void close();
}
