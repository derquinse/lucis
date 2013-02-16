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

import static net.sf.lucis.core.Interruption.throwIfInterrupted;

import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Class representing the set of operations to be performed in order to take the index to the given
 * checkpoint. Objects of this class are built using a batch builder.
 * @author Andres Rodriguez
 * @param <T> Checkpoint type.
 */
public final class Batch<T> {
	/** Map Entry to term function. */
	private static final Function<Entry<String, String>, Term> TERM_BUILDER = new Function<Entry<String, String>, Term>() {
		public Term apply(Entry<String, String> entry) {
			return new Term(entry.getKey(), entry.getValue());
		}
	};

	/** The final checkpoint. */
	private final T checkpoint;
	/** Addtions. */
	private final ImmutableList<Addition> additions;
	/** Deletions. */
	private final ImmutableMultimap<String, String> deletions;
	/** Whether the index must be recreated before applying the batch. */
	private final boolean recreate;

	private Batch(final T checkpoint, final Builder<T> builder) {
		this.checkpoint = checkpoint;
		this.additions = ImmutableList.copyOf(builder.additions);
		this.deletions = builder.deletions.build();
		this.recreate = builder.recreate;
	}

	public boolean isEmpty() {
		return additions.isEmpty() && deletions.isEmpty();
	}

	public T getCheckpoint() {
		return checkpoint;
	}

	public ImmutableList<Addition> getAdditions() {
		return additions;
	}

	public Iterable<Term> getDeletions() {
		return Iterables.transform(deletions.entries(), TERM_BUILDER);
	}

	public boolean isRecreate() {
		return recreate;
	}

	public static <T> Builder<T> builder() {
		return new Builder<T>();
	}

	public static class Builder<T> {
		private final List<Addition> additions = Lists.newLinkedList();
		private final ImmutableMultimap.Builder<String, String> deletions = ImmutableMultimap.builder();
		/** Whether the index must be recreated before applying the batch. */
		private boolean recreate = false;

		private Builder() {

		}

		/**
		 * States that the index must be recreated before applying the batch.
		 */
		public Builder<T> recreate() throws InterruptedException {
			throwIfInterrupted();
			recreate = true;
			return this;
		}

		public Builder<T> add(final Document document) throws InterruptedException {
			throwIfInterrupted();
			if (document != null) {
				additions.add(new Addition(document));
			}
			return this;
		}

		public Builder<T> add(final Document document, final Analyzer analyzer) throws InterruptedException {
			throwIfInterrupted();
			if (document != null) {
				additions.add(new Addition(document, analyzer));
			}
			return this;
		}

		public Builder<T> delete(final String field, final String text) throws InterruptedException {
			throwIfInterrupted();
			if (field != null && text != null) {
				deletions.put(field, text);
			}
			return this;
		}

		public Builder<T> delete(final Term term) throws InterruptedException {
			if (term != null) {
				return delete(term.field(), term.text());
			}
			return this;
		}

		public Builder<T> update(final Document document, final String field, final String text)
				throws InterruptedException {
			if (field != null && text != null) {
				return delete(field, text).add(document);
			}
			return this;
		}

		public Builder<T> update(final Document document, final Term term) throws InterruptedException {
			if (term != null) {
				return update(document, term.field(), term.text());
			}
			return this;
		}

		public Batch<T> build(final T checkpoint) throws InterruptedException {
			throwIfInterrupted();
			if (checkpoint == null) {
				return null;
			}
			return new Batch<T>(checkpoint, this);
		}

	}

	public static final class Addition {
		private final Document document;
		private final Analyzer analyzer;

		private Addition(final Document document, final Analyzer analyzer) {
			this.document = document;
			this.analyzer = analyzer;
		}

		private Addition(final Document document) {
			this(document, null);
		}

		public Document getDocument() {
			return document;
		}

		public Analyzer getAnalyzer() {
			return analyzer;
		}
	}
}
