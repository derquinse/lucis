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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Base class for tests with an index directory.
 * @author Andres Rodriguez
 */
public abstract class AbstractDirectoryTest {
	private String indexDir;

	@BeforeClass
	public void createDirectory() {
		indexDir = DirectorySupport.create();
	}

	@AfterClass
	public void deleteDirectory() {
		DirectorySupport.delete(indexDir);
	}

	protected final String getIndexDir() {
		return indexDir;
	}

}
