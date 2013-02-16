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

/**
 * Result representing a single item.
 * @author Andres Rodriguez
 */
public class Item<T> extends Result {
	private static final long serialVersionUID = 3033574198812373505L;
	/** Empty item. */
	private static final Item<Object> EMPTY = new Item<Object>(0, 0.0f, 0L, null);

	/** Item returned by the query. */
	private final T item;

	/**
	 * Returns the empty item.
	 * @param <T> Item type.
	 * @return The empty item.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Item<T> empty() {
		return (Item<T>) EMPTY;
	}

	public Item(final int totalHits, final float maxScore, final long time, T item) {
		super(totalHits, maxScore, time);
		this.item = item;
	}

	public T getItem() {
		return item;
	}
}
