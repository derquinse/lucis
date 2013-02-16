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
package net.sf.derquinse.lucis;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Result representing a page of search results. Results are 0-indexed.
 * @author Andres Rodriguez
 */
public class Page<T> extends Result implements Iterable<T> {
	private static final long serialVersionUID = -2166985025861839904L;
	/** Empty page. */
	private static final Page<Object> EMPTY = new Page<Object>(0, 0.0f, 0L, 0, Collections.emptyList());
	/** First result. */
	private final int firstResult;
	/** Items returned by the query. */
	private final List<T> items;

	/**
	 * Returns the empty page.
	 * @param <T> Item type.
	 * @return The empty apge.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Page<T> empty() {
		return (Page<T>) EMPTY;
	}

	public Page(final int totalHits, final float maxScore, final long time, final int firstResult, final List<T> items) {
		super(totalHits, maxScore, time);
		this.firstResult = firstResult;
		if (items == null) {
			this.items = Collections.emptyList();
		} else {
			this.items = items;
		}
	}

	public int getFirstResult() {
		return firstResult;
	}

	public int getLastResult() {
		return firstResult + items.size() - 1;
	}

	public int size() {
		return items.size();
	}

	public List<T> getItems() {
		return items;
	}

	public T get(int index) {
		return items.get(firstResult + index);
	}

	public Iterator<T> iterator() {
		return items.iterator();
	}
}
