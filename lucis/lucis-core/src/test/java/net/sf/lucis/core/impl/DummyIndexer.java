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

import java.util.List;
import java.util.concurrent.BlockingQueue;

import net.sf.lucis.core.Batch;
import net.sf.lucis.core.Batch.Builder;
import net.sf.lucis.core.Factory;
import net.sf.lucis.core.Indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

/**
 * Dummy sample just for tests
 * @author Emilio Escobar Reyero
 * @param <T>
 */
public class DummyIndexer<T> implements Indexer<Long> {

	private final Analyzer analyzer = Factory.get().standardAnalyzer();
	private BlockingQueue<List<String>> tareas;

	public DummyIndexer(BlockingQueue<List<String>> tareas) {
		this.tareas = tareas;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public Batch<Long> index(Long checkpoint) throws InterruptedException {

		Long actual = checkpoint != null ? checkpoint : 0;
		Batch<Long> batch;

		try {
			List<String> lote = tareas.take();
			batch = generateBatch(actual, lote);

		} catch (InterruptedException e) {
			e.printStackTrace();
			batch = emptyBatch(actual);
		}

		return batch;
	}

	private Batch<Long> emptyBatch(Long checkpoint) throws InterruptedException {
		Builder<Long> builder = Batch.builder();
		return builder.build(checkpoint);
	}

	private Batch<Long> generateBatch(Long checkpoint, List<String> lote) throws InterruptedException {
		Builder<Long> builder = Batch.builder();

		for (String t : lote) {
			String partes[] = t.split("!");

			if ("ADD".equals(partes[0])) {
				builder.add(createDocument(partes[1]));
			} else if ("DEL".equals(partes[0])) {
				builder.delete("idd", "identificador" + partes[1]);
			} else {

			}

		}

		Long next = checkpoint + lote.size();

		return builder.build(next);
	}

	private Document createDocument(String id) {
		Document document = new Document();

		document.add(new Field("id", "identificador" + String.valueOf(id), Store.YES, Index.ANALYZED));
		document.add(new Field("idd", "identificador" + String.valueOf(id), Store.YES, Index.NOT_ANALYZED));

		return document;
	}
}
