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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import net.sf.derquinse.lucis.Item;
import net.sf.lucis.core.Batch;
import net.sf.lucis.core.DocMapper;
import net.sf.lucis.core.LucisQuery;
import net.sf.lucis.core.Queryable;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.testng.Assert;

import com.google.common.collect.Multimap;

/**
 * Document support methods for tests.
 * @author Emilio Escobar Reyero
 * @author Andres Rodriguez
 */
final class DocumentSupport {
	private DocumentSupport() {
		throw new AssertionError();
	}

	static final String ID = "LUCIS-ID";
	static final String ANALIZED = "ANALIZED";

	static final String value(int value) {
		return "value_" + value;
	}

	static class Node {
		private String id;
		private String analyzed;

		void test(String value) {
			assertEquals(id, value);
			assertEquals(analyzed, value);
		}

		void test(int value) {
			test(value(value));
		}

	}

	static final DocMapper<Node> MAPPER = new DocMapper<Node>() {
		public Node map(int id, float score, Document doc, Multimap<String, String> fragments) {
			final Node node = new Node();
			node.id = doc.get(ID);
			node.analyzed = doc.get(ANALIZED);
			return node;
		}
	};

	static Document document(int value) {
		final Document document = new Document();
		final String text = value(value);
		document.add(new Field(ID, text, Field.Store.YES, Field.Index.NOT_ANALYZED));
		document.add(new Field(ANALIZED, text, Field.Store.YES, Field.Index.NOT_ANALYZED));
		return document;
	}

	static Term termId(int value) {
		return new Term(ID, value(value));
	}

	static Item<Node> first(Queryable queryable, Query query) {
		return queryable.query(LucisQuery.first(query, MAPPER));
	}

	static int count(Queryable queryable) {
		return queryable.query(LucisQuery.count(new MatchAllDocsQuery(), null)).getTotalHits();
	}

	static void found(Queryable queryable, int value) {
		final Item<Node> item = first(queryable, new TermQuery(termId(value)));
		assertNotNull(item);
		final Node node = item.getItem();
		assertNotNull(node);
		node.test(value);
	}

	static void notFound(Queryable queryable, int value) {
		final Item<Node> item = first(queryable, new TermQuery(termId(value)));
		assertNotNull(item);
		final Node node = item.getItem();
		Assert.assertNull(node);
	}

	static Batch<Long> batch(int from, int to, long cp) throws InterruptedException {
		final Batch.Builder<Long> builder = Batch.builder();
		for (int i = from; i <= to; i++) {
			builder.add(document(i));
		}
		return builder.build(cp);
	}

}
