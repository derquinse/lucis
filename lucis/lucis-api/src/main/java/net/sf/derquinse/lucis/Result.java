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

import java.io.Serializable;

/**
 * Base class for search results.
 * @author Andres Rodriguez
 */
public class Result implements Serializable {
	private static final long serialVersionUID = 87563292090748315L;
	/** Total hits of the query. */
	private final int totalHits;
	/** Maximum score. */
	private final float maxScore;
	/** Time taken by the query. */
	private final long time;

	public Result(final int totalHits, final float maxScore, final long time) {
		this.totalHits = totalHits;
		this.maxScore = maxScore;
		this.time = time;
	}

	public float getMaxScore() {
		return maxScore;
	}

	public long getTime() {
		return time;
	}

	public int getTotalHits() {
		return totalHits;
	}
}
