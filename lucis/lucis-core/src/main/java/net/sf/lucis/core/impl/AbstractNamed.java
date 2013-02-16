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

import net.derquinse.common.log.ContextLog;

/**
 * Abstract class for named objects with a context log.
 * @author Andres Rodriguez.
 */
abstract class AbstractNamed {
	/** Object name. */
	private String name = null;
	/** Logger. */
	private ContextLog log;

	AbstractNamed() {
	}

	abstract String contextFormat();

	abstract ContextLog baseLog();

	private void loadLog() {
		this.log = baseLog().to(String.format(contextFormat(), getName()));
	}

	final ContextLog log() {
		if (log == null) {
			loadLog();
		}
		return log;
	}

	/** Returns the name of the object or the class name if no one was provided. */
	public final String getName() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		return name;
	}

	/** Sets the object name. */
	public final void setName(String name) {
		this.name = name;
		loadLog();
	}
}
