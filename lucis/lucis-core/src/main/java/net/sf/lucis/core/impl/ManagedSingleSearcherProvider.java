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
import net.sf.derquinse.lucis.IndexNotAvailableException;
import net.sf.lucis.core.DirectoryProvider;
import net.sf.lucis.core.LucisSearcher;

import com.google.common.base.Supplier;

public final class ManagedSingleSearcherProvider implements Supplier<LucisSearcher> {
	private final DirectoryProvider provider;

	public static ManagedSingleSearcherProvider of(DirectoryProvider provider) {
		return new ManagedSingleSearcherProvider(provider);
	}

	private ManagedSingleSearcherProvider(DirectoryProvider provider) {
		this.provider = checkNotNull(provider, "The directory provider is mandatory.");
	}

	public LucisSearcher get() {
		try {
			return new DefaultLucisSearcher(provider.getManagedReader());
		} catch (Exception e) {
			throw new IndexNotAvailableException(e);
		}
	}

}
