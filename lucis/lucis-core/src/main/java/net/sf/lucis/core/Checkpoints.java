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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class Checkpoints {
	private Checkpoints() {
		throw new AssertionError();
	}

	public static final Checkpoint<Long> ofLong() {
		return LONG;
	}

	public static final Checkpoint<String> ofStringLine() {
		return UTF8LINE;
	}

	private static final String UTF8 = "UTF-8";

	private static final Checkpoint<String> UTF8LINE = new Checkpoint<String>() {
		public String read(InputStream input) throws IOException {
			final BufferedReader br = new BufferedReader(new InputStreamReader(input, UTF8));
			final String checkpoint = br.readLine();
			return checkpoint;
		}

		public void write(String checkpoint, OutputStream output) throws IOException {
			if (checkpoint != null) {
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(output, UTF8), true);
				pw.println(checkpoint);
			}
		}
	};

	private static final Checkpoint<Long> LONG = new Checkpoint<Long>() {
		public Long read(InputStream input) throws IOException {
			final String s = UTF8LINE.read(input);
			if (s != null) {
				return Long.parseLong(s);
			}
			return null;
		}

		public void write(Long checkpoint, OutputStream output) throws IOException {
			if (checkpoint != null) {
				UTF8LINE.write(checkpoint.toString(), output);
			}
		}
	};
}
