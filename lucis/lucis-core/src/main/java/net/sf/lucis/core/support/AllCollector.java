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
package net.sf.lucis.core.support;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.OpenBitSet;

/**
 * A hit collector that collects the document index of all the hits.
 * @author Andres Rodriguez
 */
public final class AllCollector extends CountingCollector {
	private OpenBitSet bits;
	private int docBase;

	/**
	 * Constructor.
	 */
	public AllCollector() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#setNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		this.docBase = docBase;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.lucene.search.HitCollector#collect(int, float)
	 */
	@Override
	public void collect(int doc) throws IOException {
		super.collect(doc);
		bits.set(doc + docBase);
	}

	/** Resets the collector. */
	public void reset() {
		super.reset();
		bits = new OpenBitSet();
	}

	public OpenBitSet getBits() {
		return bits;
	}
}
