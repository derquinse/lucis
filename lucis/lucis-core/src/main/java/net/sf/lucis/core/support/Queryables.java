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

import java.util.Collection;

import net.sf.lucis.core.DirectoryProvider;
import net.sf.lucis.core.Queryable;
import net.sf.lucis.core.impl.DefaultQueryable;
import net.sf.lucis.core.impl.ManagedMultiSearcherProvider;
import net.sf.lucis.core.impl.ManagedSingleSearcherProvider;
import net.sf.lucis.core.impl.MultiSearcherProvider;
import net.sf.lucis.core.impl.SingleSearcherProvider;

import org.apache.lucene.store.Directory;

import com.google.common.collect.ImmutableList;

/**
 * Queryable factory.
 * @author Andres Rodriguez
 * @author Emilio Escobar Reyero
 */
public final class Queryables {
	/** Not instantiable. */
	private Queryables() {
		throw new AssertionError();
	}

	public static Queryable simple(Directory directory) {
		return new DefaultQueryable(SingleSearcherProvider.of(directory));
	}

	public static Queryable simple(DirectoryProvider provider) {
		return new DefaultQueryable(SingleSearcherProvider.of(provider));
	}

	public static Queryable multi(Collection<DirectoryProvider> providers) {
		return new DefaultQueryable(MultiSearcherProvider.of(providers));
	}

	public static Queryable managed(DirectoryProvider provider) {
		return new DefaultQueryable(ManagedSingleSearcherProvider.of(provider));
	}

	public static Queryable managed(Iterable<DirectoryProvider> providers) {
		ImmutableList<DirectoryProvider> list = ImmutableList.copyOf(providers);
		if (list.size() == 1) {
			return managed(list.get(0));
		}
		return new DefaultQueryable(ManagedMultiSearcherProvider.of(list));
	}

}
