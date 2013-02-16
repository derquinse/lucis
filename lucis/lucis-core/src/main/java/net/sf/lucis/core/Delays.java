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

public final class Delays {
	private final long normal;
	private final long idle;
	private final long error;

	private Delays(long normal, long idle, long error) {
		this.normal = normal;
		this.idle = idle;
		this.error = error;
	}

	public static Delays constant(long delay) {
		return new Delays(delay, delay, delay);
	}

	public static Delays of(long normal, long idle, long error) {
		return new Delays(normal, idle, error);
	}

	public static Delays normalAndError(long normal, long error) {
		return new Delays(normal, normal, error);
	}

	public long getNormal() {
		return normal;
	}

	public long getIdle() {
		return idle;
	}

	public long getError() {
		return error;
	}
}
