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

import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import net.derquinse.common.log.ContextLog;
import net.sf.derquinsej.concurrent.ActiveObjectStatus;
import net.sf.derquinsej.concurrent.ActiveObjectSupport;
import net.sf.lucis.core.IndexStatus;
import net.sf.lucis.core.Loggers;

/**
 * Abstract base class for indexing services.
 * @author Andres Rodriguez
 */
public abstract class AbstractIndexService extends AbstractNamed {
	/** Index status. */
	private volatile IndexStatus status = IndexStatus.OK;
	/** External executor. */
	private final ScheduledExecutorService externalExecutor;
	/** Executor. */
	private volatile ScheduledExecutorService executor;
	/** Active object support. */
	private final ActiveObjectSupport support;
	/** Whether the service is pasive. */
	private final boolean pasive;

	/**
	 * Constructor.
	 * @param externalExecutor External executor.
	 * @param pasive Whether the service is pasive.
	 */
	AbstractIndexService(@Nullable ScheduledExecutorService externalExecutor, boolean pasive) {
		this.externalExecutor = externalExecutor;
		final Runnable onStart = new Runnable() {
			public void run() {
				initExecutor();
				schedule(0L);
			}
		};
		final Runnable onStop = new Runnable() {
			public void run() {
				shutdown();
			}
		};
		this.support = new ActiveObjectSupport(onStart, null, onStop);
		this.pasive = pasive;
	}

	@Override
	final ContextLog baseLog() {
		return Loggers.index();
	}

	@Override
	final String contextFormat() {
		return "Index[%s]";
	}

	/**
	 * Initializes the executor service to use.
	 * @throws IllegalStateException if the executor is already initialized.
	 */
	final void initExecutor() {
		checkState(executor == null, "Executor already initialized.");
		if (externalExecutor != null) {
			executor = externalExecutor;
		} else {
			executor = Executors.newSingleThreadScheduledExecutor();
		}
	}

	abstract Runnable newTask();

	/**
	 * Schedules a new task.
	 * @param delay Delay in milliseconds.
	 */
	final void schedule(long delay) {
		final ScheduledExecutorService ses = executor;
		if (ses != null) {
			ses.schedule(newTask(), delay, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Sets the index status and schedules a new task.
	 * @param status New index status.
	 * @param delay Delay in milliseconds.
	 */
	final void schedule(IndexStatus status, long delay) {
		this.status = status;
		schedule(delay);
	}

	/**
	 * Stop using the executor. If using an internal executor, shut it down.
	 */
	private void shutdown() {
		if (externalExecutor == null) {
			final ScheduledExecutorService ses = executor;
			if (ses != null) {
				executor = null;
				ses.shutdownNow();
			}
		}
		executor = null;
	}

	/* PUBLIC API */

	public IndexStatus getIndexStatus() {
		return status;
	}

	public final ActiveObjectStatus getStatus() {
		return support.getStatus();
	}

	public final void start() {
		if (!pasive) {
			support.start();
		}
	}

	public final void stop() {
		support.stop();
	}

	/* END PUBLIC API */

	abstract class AbstractTask implements Runnable {
		final void interrupted() {
			log().warn("Task interruption requested. Restoring status and exiting");
			Thread.currentThread().interrupt();
		}
	}

}
