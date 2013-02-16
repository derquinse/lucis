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

import com.google.common.base.Supplier;

public final class DefaultQueryable extends AbstractQueryable {
	private final Supplier<LucisSearcher> provider;

	/** Log. */
	private Logger logger = Logger.getLogger(getClass().getName());

	public DefaultQueryable(final Supplier<LucisSearcher> provider) {
		this.provider = provider;
	}

	/* CONFIGURABLE PROPERTIES */

	public void setLogName(final String name) {
		this.logger = Logger.getLogger(name);
	}

	/* END CONFIGURABLE PROPERTIES. */

	public <T> T query(LucisQuery<T> inquiry) {
		return doQuery(logger, provider, inquiry);
	}

	public <T> T query(ComplexQuery<T> inquiry) {
		return doQuery(logger, provider, inquiry);
	}
}
