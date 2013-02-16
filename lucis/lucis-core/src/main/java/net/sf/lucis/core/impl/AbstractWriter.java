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

import static com.google.common.base.Preconditions.checkNotNull;
import net.derquinse.common.log.ContextLog;
import net.sf.lucis.core.Factory;
import net.sf.lucis.core.Loggers;

import org.apache.lucene.index.IndexWriterConfig;

import com.google.common.base.Supplier;

/**
 * Abstract writer implementation.
 * @author Andres Rodriguez.
 */
abstract class AbstractWriter extends AbstractNamed {
	/** Writer configuration. */
	private final Supplier<IndexWriterConfig> config;

	AbstractWriter() {
		this(Factory.get().writerConfigSupplier());
	}

	AbstractWriter(Supplier<IndexWriterConfig> config) {
		this.config = checkNotNull(config, "A writer configuration supplier must be provided");
	}

	@Override
	final ContextLog baseLog() {
		return Loggers.writer();
	}

	@Override
	final String contextFormat() {
		return "Writer[%s]";
	}

	/** Provides a fresh config. */
	final IndexWriterConfig config() {
		return checkNotNull(config.get(), "Null writer config provider");
	}

}
