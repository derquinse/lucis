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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import net.sf.lucis.core.Factory;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.testng.annotations.Test;

import com.google.common.io.Closeables;

/**
 * Tests for index recreation in Lucene
 * @author Andres Rodriguez
 */
public class RecreateTest {
	public RecreateTest() {
	}

	private void write(IndexWriter w, int a, int b) throws Exception {
		for (int i = a; i <= b; i++) {
			w.addDocument(DocumentSupport.document(i));
		}
	}

	public boolean found(IndexSearcher s, int i) throws Exception {
		Query q = new TermQuery(DocumentSupport.termId(i));
		return s.search(q, null, 1).totalHits > 0;
	}

	private IndexReader reopen(IndexReader r) throws Exception {
		IndexReader other = IndexReader.openIfChanged(r);
		if (other == null) {
			return r;
		} else {
			Closeables.closeQuietly(r);
			return other;
		}
	}

	private void doTest(Directory d) throws Exception {
		IndexWriter w = new IndexWriter(d, Factory.get().writerConfig().setOpenMode(OpenMode.CREATE_OR_APPEND));
		write(w, 1, 100);
		w.close();
		IndexReader r1 = IndexReader.open(d);
		IndexSearcher s1 = new IndexSearcher(r1);
		assertTrue(found(s1, 1));
		assertTrue(found(s1, 100));
		assertFalse(found(s1, 200));
		w = new IndexWriter(d, Factory.get().writerConfig());
		write(w, 101, 200);
		assertTrue(found(s1, 1));
		assertTrue(found(s1, 100));
		assertFalse(found(s1, 200));
		assertFalse(found(s1, 300));
		s1.close();
		s1 = new IndexSearcher(r1);
		assertTrue(found(s1, 1));
		assertTrue(found(s1, 100));
		assertFalse(found(s1, 200));
		assertFalse(found(s1, 300));
		r1 = reopen(r1);
		s1.close();
		s1 = new IndexSearcher(r1);
		assertTrue(found(s1, 1));
		assertTrue(found(s1, 100));
		assertFalse(found(s1, 200));
		assertFalse(found(s1, 300));
		w.close();
		assertTrue(found(s1, 1));
		assertTrue(found(s1, 100));
		assertFalse(found(s1, 200));
		assertFalse(found(s1, 300));
		s1.close();
		s1 = new IndexSearcher(r1);
		assertTrue(found(s1, 1));
		assertTrue(found(s1, 100));
		assertFalse(found(s1, 200));
		assertFalse(found(s1, 300));
		r1 = reopen(r1);
		s1.close();
		s1 = new IndexSearcher(r1);
		assertTrue(found(s1, 1));
		assertTrue(found(s1, 100));
		assertTrue(found(s1, 200));
		assertFalse(found(s1, 300));
		w = new IndexWriter(d, Factory.get().writerConfig().setOpenMode(OpenMode.CREATE_OR_APPEND));
		write(w, 1000, 2000);
		assertTrue(found(s1, 1));
		assertTrue(found(s1, 100));
		assertTrue(found(s1, 200));
		assertFalse(found(s1, 300));
		assertFalse(found(s1, 1500));
		r1 = reopen(r1);
		s1.close();
		s1 = new IndexSearcher(r1);
		// assertTrue(found(s1, 1));
		// assertTrue(found(s1, 100));
		// assertTrue(found(s1, 200));
		// assertFalse(found(s1, 300));
		// assertFalse(found(s1, 1500));
		s1.close();
	}

	@Test
	public void ram() throws Exception {
		doTest(new RAMDirectory());
	}

}
