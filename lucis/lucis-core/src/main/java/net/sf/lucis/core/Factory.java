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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.util.Version;

import com.google.common.base.Supplier;

/**
 * Support class the default values related to the lucene version for this lucis version.
 * @author Andres Rodriguez
 */
public final class Factory {
	/** Not instantiable. */
	private Factory() {
	}

	/** Instance. */
	private static final Factory INSTANCE = new Factory();
	/** Supplier. */
	private static final IndexWriterConfigSupplier SUPPLIER = new IndexWriterConfigSupplier();

	/** Returns the instance. */
	public static Factory get() {
		return INSTANCE;
	}

	/** Returns the used lucene version. */
	public Version version() {
		return Version.LUCENE_36;
	}

	/** Returns the default standard analyzer. */
	public Analyzer standardAnalyzer() {
		return new StandardAnalyzer(version());
	}

	/** Returns the default writer configuration. */
	public IndexWriterConfig writerConfig() {
		return new IndexWriterConfig(version(), standardAnalyzer());
	}

	/** Returns the default writer configuration supplier. */
	public Supplier<IndexWriterConfig> writerConfigSupplier() {
		return SUPPLIER;
	}

	/** Default IndexWriterConfig provider. */
	private static final class IndexWriterConfigSupplier implements Supplier<IndexWriterConfig> {
		IndexWriterConfigSupplier() {
		}

		public IndexWriterConfig get() {
			return Factory.get().writerConfig();
		}

		@Override
		public String toString() {
			return "Default IndexWriterConfig provider";
		}
	}

}
