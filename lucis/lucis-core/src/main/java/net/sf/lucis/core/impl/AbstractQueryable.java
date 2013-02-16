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

import java.util.logging.Logger;

import net.sf.lucis.core.ComplexQuery;
import net.sf.lucis.core.LucisQuery;
import net.sf.lucis.core.LucisSearcher;
import net.sf.lucis.core.Queryable;

import com.google.common.base.Supplier;

public abstract class AbstractQueryable implements Queryable {

	public AbstractQueryable() {
	}

	final <T> T doQuery(final Logger logger, final LucisSearcher searcher, final LucisQuery<T> query) {
		return query.perform(searcher);
	}

	final <T> T doQuery(final Logger logger, final Supplier<LucisSearcher> provider, final LucisQuery<T> query) {
		final LucisSearcher searcher = provider.get();
		try {
			return doQuery(logger, searcher, query);
		} finally {
			searcher.close();
		}
	}

	final <T> T doQuery(final Logger logger, final LucisSearcher searcher, ComplexQuery<T> query) {
		final Queryable queryable = new AbstractQueryable() {
			public <S> S query(LucisQuery<S> inquiry) {
				return doQuery(logger, searcher, inquiry);
			}

			public <S> S query(ComplexQuery<S> inquiry) {
				return doQuery(logger, searcher, inquiry);
			}
		};
		return query.inquire(queryable);
	}

	final <T> T doQuery(final Logger logger, final Supplier<LucisSearcher> provider, final ComplexQuery<T> query) {
		final LucisSearcher searcher = provider.get();
		try {
			return doQuery(logger, searcher, query);
		} finally {
			searcher.close();
		}
	}
}
