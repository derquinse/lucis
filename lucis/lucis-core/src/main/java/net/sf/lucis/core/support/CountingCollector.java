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
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

/**
 * A hit collector that simply counts the number of hits.
 * @author Andres Rodriguez
 */
public class CountingCollector extends Collector {
	private Scorer scorer;
	private float maxScore;
	private int count;

	/** Constructor. */
	public CountingCollector() {
		reset();
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#collect(int)
	 */
	@Override
	public void collect(int doc) throws IOException {
		count++;
		if (scorer != null) {
			maxScore = Math.max(maxScore, scorer.score());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#acceptsDocsOutOfOrder()
	 */
	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#setNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.lucene.search.Collector#setScorer(org.apache.lucene.search.Scorer)
	 */
	@Override
	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}

	/** Resets the collector. */
	public void reset() {
		maxScore = Float.MIN_VALUE;
		count = 0;
	}

	/**
	 * Returns the document count.
	 * @return The document count
	 */
	public final int getCount() {
		return count;
	}

	/**
	 * Returns the maximum collected score.
	 * @return The maximum collected score.
	 */
	public final float getMaxScore() {
		return maxScore;
	}
}
