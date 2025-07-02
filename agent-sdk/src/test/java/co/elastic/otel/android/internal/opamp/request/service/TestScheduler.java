/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.otel.android.internal.opamp.request.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class TestScheduler {
  private final List<Task> tasks = new ArrayList<>();
  private final ScheduledExecutorService service = createTestScheduleExecutorService();

  public ScheduledExecutorService getMockService() {
    return service;
  }

  public List<Task> getScheduledTasks() {
    return Collections.unmodifiableList(tasks);
  }

  public void clearTasks() {
    tasks.clear();
  }

  private ScheduledExecutorService createTestScheduleExecutorService() {
    ScheduledExecutorService service = mock();

    lenient()
        .doAnswer(
            invocation -> {
              Runnable runnable = invocation.getArgument(0);
              runnable.run();
              return null;
            })
        .when(service)
        .execute(any());

    lenient()
        .when(service.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
        .thenAnswer(
            invocation -> {
              Task task = new Task(invocation.getArgument(0), invocation.getArgument(1));

              tasks.add(task);

              return task;
            });

    return service;
  }

  public class Task implements ScheduledFuture<Object> {
    private final Runnable runnable;
    private final Duration delay;

    public void run() {
      get();
    }

    private Task(Runnable runnable, long timeNanos) {
      this.runnable = runnable;
      this.delay = Duration.ofNanos(timeNanos);
    }

    public Duration getDelay() {
      return delay;
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return tasks.remove(this);
    }

    @Override
    public boolean isCancelled() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object get() {
      tasks.remove(this);
      runnable.run();
      return null;
    }

    @Override
    public Object get(long timeout, @NotNull TimeUnit unit) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
      throw new UnsupportedOperationException();
    }
  }
}
