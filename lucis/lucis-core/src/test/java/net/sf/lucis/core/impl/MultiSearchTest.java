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

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import net.sf.derquinse.lucis.Item;
import net.sf.derquinse.lucis.Result;
import net.sf.lucis.core.Batch;
import net.sf.lucis.core.Checkpoints;
import net.sf.lucis.core.DirectoryProvider;
import net.sf.lucis.core.DocMapper;
import net.sf.lucis.core.LucisQuery;
import net.sf.lucis.core.Queryable;
import net.sf.lucis.core.Store;
import net.sf.lucis.core.Writer;
import net.sf.lucis.core.support.Queryables;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Multimap;

public class MultiSearchTest {
	static final String ID = "LUCIS-ID";
	static final String ID2 = "ID2";
	static final String ANALIZED = "ANALIZED";
	static final String STR = "STR";

	String idxDir;
	Store<Long> ram;
	Store<Long> fs;
	Store<Long> ram2;

	private Queryable queryableRAM;
	private Queryable queryableFS;
	private Queryable queryableRAM2;
	private Queryable queryableMULTI;

	private static final Writer writerRAM = new DefaultWriter();
	private static final Writer writerFS = new DefaultWriter();

	private static final Writer writerRAM2 = new DefaultWriter();

	@BeforeClass
	public void before() {
		final String tempDir = System.getProperty("java.io.tmpdir");
		// idxDir = tempDir + File.separator + "lucis-test";
		idxDir = tempDir + File.separator + "lucis-test-" + UUID.randomUUID();
		File dir = new File(idxDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	@Test
	public void createStores() {
		ram = new RAMStore<Long>();
		ram2 = new RAMStore<Long>();
		fs = new FSStore<Long>(Checkpoints.ofLong(), idxDir);

	}

	@Test(dependsOnMethods = "createStores")
	public void addRam() throws InterruptedException {
		writerRAM.write(ram, add(1, 100, 1L, "RAM"));
		assertEquals(ram.getCheckpoint(), Long.valueOf(1L));
	}

	@Test(dependsOnMethods = "createStores")
	public void addRam2() throws InterruptedException {
		writerRAM2.write(ram2, add2(1, 100, 1L, "RAM"));
		assertEquals(ram2.getCheckpoint(), Long.valueOf(1L));
	}

	@Test(dependsOnMethods = "createStores")
	public void addFs() throws InterruptedException {
		writerFS.write(fs, add(1, 100, 1L, "FILE"));
		assertEquals(fs.getCheckpoint(), Long.valueOf(1L));
	}

	@Test(dependsOnMethods = "createStores")
	public void queryables() {
		this.queryableRAM = Queryables.simple(ram);
		this.queryableRAM2 = Queryables.simple(ram2);
		this.queryableFS = Queryables.simple(fs);
		Collection<DirectoryProvider> providers = new HashSet<DirectoryProvider>();
		providers.add(ram2);
		providers.add(ram);
		providers.add(fs);
		this.queryableMULTI = Queryables.multi(providers);
	}

	@Test(dependsOnMethods = { "addFs", "addRam", "addRam2", "queryables" })
	public void findAll() {
		found(queryableRAM, 5, "RAM");
		found(queryableFS, 5, "FILE");
		found(queryableMULTI, 5, "RAM");
		found(queryableMULTI, 5, "FILE");
	}

	@Test(dependsOnMethods = { "addFs", "addRam", "addRam2" })
	public void findAll2() {
		// found(queryableRAM, 5, "RAM");
		// //found(queryableFS, 5, "FILE");
		// //found(queryableRAM2, 5, "RAM");
		// found(queryableMULTI, 5, "RAM");
		// //found(queryableMULTI, 5, "FILE");
		// found2(queryableRAM2, 5, "RAM");
		// found2(queryableMULTI, 5, "RAM");
		// //found2(queryableMULTI, 5, "FILE");
	}

	@Test(dependsOnMethods = "findAll")
	public void deleteFs() throws InterruptedException {
		writerFS.write(fs, delete(5, 2L));
		assertEquals(fs.getCheckpoint(), Long.valueOf(2L));
	}

	@Test(dependsOnMethods = { "addFs", "addRam", "addRam2" })
	public void countMultiId() {
		// Result res = count(queryableMULTI, new TermQuery(termId(5)));
		// assertNotNull(res);
		// Assert.assertTrue(res.getTotalHits() > 1);
	}

	@Test(dependsOnMethods = { "addFs", "addRam", "addRam2" })
	public void countRam() {
		// Result res = count(queryableRAM, new TermQuery(new Term(STR, "RAM")));
		// assertNotNull(res);
		// Assert.assertTrue(res.getTotalHits() > 1);
	}

	@Test(dependsOnMethods = { "addFs", "addRam", "addRam2" })
	public void countMulti() {
		// Result res = count(queryableMULTI, new TermQuery(new Term(STR, "RAM")));
		// assertNotNull(res);
		// Assert.assertTrue(res.getTotalHits() > 1);
	}

	@Test(dependsOnMethods = "deleteFs")
	public void findOnMulti() {
		// found(queryableRAM, 5);
		// notFound(queryableFS, 5);
		// found(queryableMULTI, 5);
	}

	private void found(final Queryable queryable, int value, String str) {

		BooleanClause clauseId = new BooleanClause(new TermQuery(new Term(ID, value(value))), Occur.MUST);
		BooleanClause clauseStr = new BooleanClause(new TermQuery(new Term(STR, str)), Occur.MUST);
		BooleanQuery query = new BooleanQuery();
		query.add(clauseId);
		query.add(clauseStr);

		final Item<Node> item = first(queryable, query);
		assertNotNull(item);
		final Node node = item.getItem();
		assertNotNull(node);
		node.test(value);
	}

	private void found2(final Queryable queryable, int value, String str) {

		BooleanClause clauseId = new BooleanClause(new TermQuery(new Term(ID2, value(value))), Occur.MUST);
		BooleanClause clauseStr = new BooleanClause(new TermQuery(new Term(STR, str)), Occur.MUST);
		BooleanQuery query = new BooleanQuery();
		query.add(clauseId);
		query.add(clauseStr);

		final Item<Node> item = first(queryable, query);
		assertNotNull(item);
		final Node node = item.getItem();
		assertNotNull(node);
		node.test(value);
	}

	private void found(final Queryable queryable, int value) {
		final Item<Node> item = first(queryable, new TermQuery(termId(value)));
		assertNotNull(item);
		final Node node = item.getItem();
		assertNotNull(node);
		node.test(value);
	}

	private void notFound(final Queryable queryable, int value) {
		final Item<Node> item = first(queryable, new TermQuery(termId(value)));
		assertNotNull(item);
		final Node node = item.getItem();
		Assert.assertNull(node);
	}

	private Item<Node> first(final Queryable queryable, final Query query) {
		return queryable.query(LucisQuery.first(query, MAPPER));
	}

	private Result count(final Queryable queryable, final Query query) {
		return queryable.query(LucisQuery.count(query, null));
	}

	private Batch<Long> delete(int value, long cp) throws InterruptedException {
		final Batch.Builder<Long> builder = Batch.builder();
		return builder.delete(termId(value)).build(cp);
	}

	static Term termId(int value) {
		return new Term(ID, value(value));
	}

	private Batch<Long> add2(int from, int to, long cp, String str) throws InterruptedException {
		final Batch.Builder<Long> builder = Batch.builder();
		for (int i = from; i <= to; i++) {
			builder.add(document2(i, str));
		}
		return builder.build(cp);
	}

	private Batch<Long> add(int from, int to, long cp, String str) throws InterruptedException {
		final Batch.Builder<Long> builder = Batch.builder();
		for (int i = from; i <= to; i++) {
			builder.add(document(i, str));
		}
		return builder.build(cp);
	}

	static Document document(int value, String str) {
		final Document document = new Document();
		final String text = value(value);
		document.add(new Field(ID, text, Field.Store.YES, Field.Index.NOT_ANALYZED));
		document.add(new Field(ANALIZED, text, Field.Store.YES, Field.Index.ANALYZED));
		document.add(new Field(STR, str, Field.Store.YES, Field.Index.NOT_ANALYZED));
		return document;
	}

	static Document document2(int value, String str) {
		final Document document = new Document();
		final String text = value(value);
		// document.add(new Field(ID, text, Field.Store.YES, Field.Index.NOT_ANALYZED));
		document.add(new Field(ANALIZED, text, Field.Store.YES, Field.Index.ANALYZED));
		document.add(new Field(STR, str, Field.Store.YES, Field.Index.NOT_ANALYZED));
		document.add(new Field(ID2, text, Field.Store.YES, Field.Index.NOT_ANALYZED));
		return document;
	}

	static final String value(int value) {
		return "value" + value;
	}

	private static class Node {
		private String id;
		private String analyzed;
		private String str;

		private void test(String value, String str) {
			assertEquals(id, value);
			assertEquals(analyzed, value);
			assertEquals(this.str, str);
		}

		private void test(int value, String str) {
			test(value(value), str);
		}

		private void test(String value) {
			assertEquals(id, value);
			assertEquals(analyzed, value);
			// assertEquals(this.str, str);
		}

		private void test(int value) {
			test(value(value));
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof Node)) {
				return false;
			}

			Node o = (Node) obj;

			return id.equals(o.id) && analyzed.equals(o.analyzed) && str.equals(o.str);
		}

	}

	private static final DocMapper<Node> MAPPER = new DocMapper<Node>() {
		public Node map(int id, float score, Document doc, Multimap<String, String> fragments) {
			final Node node = new Node();

			node.id = doc.get(ID);
			if (node.id == null) {
				node.id = doc.get(ID2);
			}
			node.analyzed = doc.get(ANALIZED);
			node.str = doc.get(STR);

			return node;
		}
	};
}
