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

import net.derquinse.common.log.ContextLog;

/**
 * Port@l core logging channels.
 * @author Andres Rodriguez
 */
public final class Loggers {
	/** Not instantiable. */
	private Loggers() {
		throw new AssertionError();
	}

	private static final String STORE = "net.sf.lucis.store";
	private static final String WRITER = "net.sf.lucis.writer";
	private static final String INDEX = "net.sf.lucis.index";

	public static ContextLog store() {
		return ContextLog.of(STORE);
	}

	public static ContextLog writer() {
		return ContextLog.of(WRITER);
	}

	public static ContextLog index() {
		return ContextLog.of(INDEX);
	}

}
