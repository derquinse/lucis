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
 * Grouped search result.
 * @author Andres Rodriguez
 */
public class GroupResult extends Result {
	private static final long serialVersionUID = 3517663190273307742L;
	/** Group. */
	private final Group group;

	public GroupResult(final Group group, final float maxScore, final long time) {
		super(group.getHits(), maxScore, time);
		this.group = group;
	}

	GroupResult() {
		super(0, 0.0f, 0L);
		this.group = new Group();
	}

	public Group getGroup() {
		return group;
	}
}
