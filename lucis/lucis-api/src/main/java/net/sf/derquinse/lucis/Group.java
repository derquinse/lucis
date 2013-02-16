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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Group for grouped queries.
 * @author Andres Rodriguez
 */
public class Group implements Serializable {
	private static final long serialVersionUID = 2852471668339545587L;
	/** Total hits of the query. */
	private int hits = 0;
	/** Nested groups. */
	private Map<String, Group> groups = null;

	public Group() {
	}

	public int getHits() {
		return hits;
	}

	public int addHit() {
		hits++;
		return hits;
	}

	public Set<String> getGroupNames() {
		if (groups == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(groups.keySet());
	}

	public Group getGroup(String name) {
		if (name == null) {
			throw new NullPointerException("Name argument is required");
		}
		if (groups == null) {
			groups = new HashMap<String, Group>();
		}
		Group g = groups.get(name);
		if (g == null) {
			g = new Group();
			groups.put(name, g);
		}
		return g;
	}
}
