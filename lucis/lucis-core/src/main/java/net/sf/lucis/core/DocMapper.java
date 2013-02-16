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

import org.apache.lucene.document.Document;

import com.google.common.collect.Multimap;

/**
 * Mapper from a Lucene document to a custom object.
 * @author Andres Rodriguez
 * @author Emilio Escobar Reyero
 * @param <T> Type of the custom object.
 */
public interface DocMapper<T> {
	/**
	 * @param id
	 * @param score
	 * @param doc
	 * @param fragments not null multimap of highlighter fragments
	 * @return
	 */
	T map(int id, float score, Document doc, Multimap<String, String> fragments);
}
