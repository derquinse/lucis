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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * Highlight configuration.
 * @author Emilio Escobar Reyero
 * @author Andres Rodriguez
 */
public abstract class Highlight {
	/** Fields filter. */
	private static final Predicate<Entry<String, Integer>> FILTER = new Predicate<Entry<String, Integer>>() {
		public boolean apply(Entry<String, Integer> input) {
			String k = input.getKey();
			Integer v = input.getValue();
			return k != null && v != null && v >= 0;
		};
	};

	/** No highlighting. */
	private static final No NO = new No();
	/** Unhighlighted query . */
	private static final HighlightedQuery UNHIGHLIGHTED = new HighlightedQuery();

	private static ImmutableMap<String, Integer> filter(Map<String, Integer> fields) {
		if (fields == null || fields.isEmpty()) {
			return ImmutableMap.of();
		}
		return ImmutableMap.copyOf(Maps.filterEntries(fields, FILTER));
	}

	public static Highlight of(Analyzer analyzer, Formatter formatter, Map<String, Integer> fields) {
		final ImmutableMap<String, Integer> f = filter(fields);
		if (f.isEmpty()) {
			return no();
		}
		checkNotNull(analyzer);
		checkNotNull(formatter);
		return new Some(analyzer, formatter, f);
	}

	public static Highlight of(Map<String, Integer> fields) {
		final ImmutableMap<String, Integer> f = filter(fields);
		if (f.isEmpty()) {
			return no();
		}
		return new Some(Factory.get().standardAnalyzer(), new SimpleHTMLFormatter(), f);
	}

	public static Highlight no() {
		return NO;
	}

	/** Constructor. */
	Highlight() {
	}

	/**
	 * Highlight a query with this configuration.
	 * @param query Query to highlight.
	 * @return The highligthed query.
	 */
	public abstract HighlightedQuery highlight(Query query);

	private static class No extends Highlight {
		No() {
		}

		@Override
		public HighlightedQuery highlight(Query query) {
			return UNHIGHLIGHTED;
		}
	}

	private static class Some extends Highlight {
		private final ImmutableMap<String, Integer> fields;
		private final Analyzer analyzer;
		private final Formatter formatter;

		Some(Analyzer analyzer, Formatter formatter, ImmutableMap<String, Integer> fields) {
			this.analyzer = analyzer;
			this.formatter = formatter;
			this.fields = fields;
		}

		@Override
		public HighlightedQuery highlight(Query query) {
			return new HQuery(query);
		}

		private final class HQuery extends HighlightedQuery {
			/** Highlighter. */
			private final Highlighter highlighter;

			private HQuery(Query query) {
				checkNotNull(query, "The rewritten query to highlight must be provided");
				this.highlighter = new Highlighter(formatter, new QueryScorer(query));
			}

			@Override
			public Multimap<String, String> getFragments(Document doc) {
				final Multimap<String, String> fragments = ArrayListMultimap.create();
				for (Map.Entry<String, Integer> entry : fields.entrySet()) {
					final String field = entry.getKey();
					final Integer maxNumFragments = entry.getValue();
					final String text = doc.get(field);
					if (text != null) {
						try {
							highlighter.setTextFragmenter(maxNumFragments > 0 ? new SimpleFragmenter() : new NullFragmenter());
							String[] fr = highlighter.getBestFragments(analyzer, field, text, maxNumFragments);
							if (fr != null && fr.length > 0) {
								fragments.putAll(field, Arrays.asList(fr));
							}
						} catch (IOException e) {
						} catch (InvalidTokenOffsetsException e) {
						}
					}
				}
				// TODO: fix exceptions.
				return fragments;
			}

		}

	}

	public static class HighlightedQuery {
		HighlightedQuery() {
		}

		public Multimap<String, String> getFragments(Document doc) {
			return ImmutableListMultimap.of();
		}
	}

}
