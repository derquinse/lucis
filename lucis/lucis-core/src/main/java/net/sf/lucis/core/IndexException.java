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

public class IndexException extends RuntimeException {
	/** Serial UID. */
	private static final long serialVersionUID = -6302456405801205080L;
	/** Index status. */
	private final IndexStatus status;

	public IndexException(final IndexStatus status) {
		super();
		this.status = status;
	}

	public IndexException(final IndexStatus status, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	public IndexException(final IndexStatus status, Throwable cause) {
		super(cause);
		this.status = status;
	}

	public IndexStatus getStatus() {
		return status;
	}
}