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

import java.io.File;
import java.util.UUID;

/**
 * Temporary index directory support.
 * @author Emilio Escobar Reyero
 * @author Andres Rodriguez
 */
final class DirectorySupport {
	private DirectorySupport() {
		throw new AssertionError();
	}

	static String create() {
		final String tempDir = System.getProperty("java.io.tmpdir");
		String indexDir = tempDir + File.separator + "lucis-test-" + UUID.randomUUID();
		File dir = new File(indexDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return indexDir;
	}

	static void delete(String indexDir) {
		final File dir = new File(indexDir);
		final File files[] = dir.listFiles();
		for (File f : files) {
			f.deleteOnExit();
		}
		dir.deleteOnExit();
	}

}
