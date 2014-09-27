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

import java.util.List;
import java.util.concurrent.TimeUnit;

import net.sf.derquinse.lucis.Group;
import net.sf.derquinse.lucis.GroupResult;
import net.sf.derquinse.lucis.Item;
import net.sf.derquinse.lucis.Page;
import net.sf.derquinse.lucis.Result;
import net.sf.lucis.core.Highlight.HighlightedQuery;
import net.sf.lucis.core.support.AllCollector;
import net.sf.lucis.core.support.CountingCollector;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.OpenBitSet;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

/**
 * Abstract implementation for a search service.
 * @author Andres Rodriguez
 */
public abstract class LucisQuery<T> {
	private LucisQuery() {
	}

	TopDocs getTopDocs(LucisSearcher searcher, Query query, Filter filter, Sort sort, int hits) {
		final TopDocs docs;
		if (sort == null) {
			docs = searcher.search(query, filter, hits);
		} else {
			docs = searcher.search(query, filter, hits, sort);
		}
		return docs;
	}

	public abstract T perform(LucisSearcher searcher);

	public static <T> LucisQuery<Item<T>> first(final Query query, final Filter filter, final Sort sort,
			final DocMapper<T> mapper, final Highlight highlight) {
		return new LucisQuery<Item<T>>() {
			public Item<T> perform(final LucisSearcher searcher) {
				Stopwatch w = Stopwatch.createStarted();
				Query rewritten = searcher.rewrite(query);
				TopDocs docs = getTopDocs(searcher, query, filter, sort, 1);
				if (docs.totalHits > 0) {
					ScoreDoc sd = docs.scoreDocs[0];
					Document doc = searcher.doc(sd.doc);
					HighlightedQuery highlighted = MoreObjects.firstNonNull(highlight, Highlight.no()).highlight(rewritten);
					float score = sd.score;
					T item = mapper.map(sd.doc, score, doc, highlighted.getFragments(doc));
					return new Item<T>(docs.totalHits, score, w.elapsed(TimeUnit.MILLISECONDS), item);
				} else {
					return new Item<T>(docs.totalHits, 0.0f, w.elapsed(TimeUnit.MILLISECONDS), null);
				}
			}
		};
	}

	public static <T> LucisQuery<Item<T>> first(final Query query, final Filter filter, final Sort sort,
			final DocMapper<T> mapper) {
		return first(query, filter, sort, mapper, Highlight.no());
	}

	public static <T> LucisQuery<Item<T>> first(final Query query, final Sort sort, final DocMapper<T> mapper) {
		return first(query, null, sort, mapper, Highlight.no());
	}

	public static <T> LucisQuery<Item<T>> first(final Query query, final Filter filter, final DocMapper<T> mapper) {
		return first(query, filter, null, mapper, Highlight.no());
	}

	public static <T> LucisQuery<Item<T>> first(final Query query, final DocMapper<T> mapper) {
		return first(query, null, null, mapper, Highlight.no());
	}

	public static <T> LucisQuery<Page<T>> page(final Query query, final Filter filter, final Sort sort,
			final DocMapper<T> mapper, final int first, final int pageSize, final Highlight highlight) {
		return new LucisQuery<Page<T>>() {
			public Page<T> perform(LucisSearcher searcher) {
				Stopwatch w = Stopwatch.createStarted();
				int total = first + pageSize;
				Query rewritten = searcher.rewrite(query);
				TopDocs docs = getTopDocs(searcher, rewritten, filter, sort, total);
				if (docs.totalHits > 0) {
					int n = Math.min(total, docs.scoreDocs.length);
					float score = docs.getMaxScore();
					if (n > first) {
						final List<T> items = Lists.newArrayListWithCapacity(n - first);
						HighlightedQuery highlighted = MoreObjects.firstNonNull(highlight, Highlight.no()).highlight(rewritten);
						for (int i = first; i < n; i++) {
							ScoreDoc sd = docs.scoreDocs[i];
							Document doc = searcher.doc(sd.doc);
							T item = mapper.map(sd.doc, score, doc, highlighted.getFragments(doc));
							items.add(item);
						}
						return new Page<T>(docs.totalHits, score, w.elapsed(TimeUnit.MILLISECONDS), first, items);
					} else {
						return new Page<T>(docs.totalHits, score, w.elapsed(TimeUnit.MILLISECONDS), first, null);
					}
				} else {
					return new Page<T>(docs.totalHits, 0.0f, w.elapsed(TimeUnit.MILLISECONDS), first, null);
				}
			}
		};
	}

	public static <T> LucisQuery<Page<T>> page(final Query query, final Filter filter, final Sort sort,
			final DocMapper<T> mapper, final int first, final int pageSize) {
		return page(query, filter, sort, mapper, first, pageSize, Highlight.no());
	}

	public static <T> LucisQuery<Page<T>> page(final Query query, final Sort sort, final DocMapper<T> mapper,
			final int first, final int pageSize) {
		return page(query, null, sort, mapper, first, pageSize, Highlight.no());
	}

	public static <T> LucisQuery<Page<T>> page(final Query query, final Filter filter, final DocMapper<T> mapper,
			final int first, final int pageSize) {
		return page(query, filter, null, mapper, first, pageSize, Highlight.no());
	}

	public static <T> LucisQuery<Page<T>> page(final Query query, final DocMapper<T> mapper, final int first,
			final int pageSize) {
		return page(query, null, null, mapper, first, pageSize, Highlight.no());
	}

	public static LucisQuery<Result> count(final Query query, final Filter filter) {
		return new LucisQuery<Result>() {
			@Override
			public Result perform(LucisSearcher searcher) {
				final long t0 = System.currentTimeMillis();
				final CountingCollector collector = new CountingCollector();
				searcher.search(query, filter, collector);
				final long t1 = System.currentTimeMillis();
				return new Result(collector.getCount(), collector.getMaxScore(), t1 - t0);
			}
		};
	}

	public static LucisQuery<GroupResult> group(final Query query, final Filter filter, final List<String> fields) {
		return new LucisQuery<GroupResult>() {
			@Override
			public GroupResult perform(LucisSearcher searcher) {
				final long t0 = System.currentTimeMillis();
				final AllCollector collector = new AllCollector();
				searcher.search(query, filter, collector);
				final Group g = new Group();
				final OpenBitSet bits = collector.getBits();
				for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
					g.addHit();
					final Document d = searcher.doc(i);
					if (fields != null && !fields.isEmpty()) {
						add(g, d, 0);
					}
				}
				final long t1 = System.currentTimeMillis();
				return new GroupResult(g, collector.getMaxScore(), t1 - t0);
			}

			private void add(Group g, Document d, int i) {
				if (i < fields.size()) {
					final String f = fields.get(i);
					if (f == null) {
						return;
					}
					i++;
					String[] values = d.getValues(f);
					for (String value : values) {
						Group next = g.getGroup(value);
						next.addHit();
						add(next, d, i);
					}
				}
			}
		};
	}
}
