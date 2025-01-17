package com.google.cloud.bigquery.connector.common;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.cloud.bigquery.connector.common.UnboundedScheduledExecutorService.ScheduledFutureTask;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Tests for {@link UnboundedScheduledExecutorService}. */
@RunWith(JUnit4.class)
public class UnboundedScheduledExecutorServiceTest {
  private static final Logger LOG =
      LoggerFactory.getLogger(UnboundedScheduledExecutorServiceTest.class);

  private static final Runnable RUNNABLE =
      () -> {
        // no-op
      };
  private static final Callable<String> CALLABLE = () -> "A";

  private static final Callable<String> FAILING_CALLABLE =
      () -> {
        throw new Exception("Test");
      };

  @Test
  public void testScheduleMethodErrorChecking() throws Exception {
    FastNanoClockAndSleeper fastNanoClockAndSleeper = new FastNanoClockAndSleeper();
    UnboundedScheduledExecutorService executorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);
    UnboundedScheduledExecutorService shutdownExecutorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);
    shutdownExecutorService.shutdown();

    assertThrows(
        NullPointerException.class, () -> executorService.schedule((Runnable) null, 10, SECONDS));
    assertThrows(NullPointerException.class, () -> executorService.schedule(RUNNABLE, 10, null));
    assertThrows(
        RejectedExecutionException.class,
        () -> shutdownExecutorService.schedule(RUNNABLE, 10, SECONDS));

    assertThrows(
        NullPointerException.class,
        () -> executorService.schedule((Callable<String>) null, 10, SECONDS));
    assertThrows(NullPointerException.class, () -> executorService.schedule(CALLABLE, 10, null));
    assertThrows(
        RejectedExecutionException.class,
        () -> shutdownExecutorService.schedule(CALLABLE, 10, SECONDS));

    assertThrows(
        NullPointerException.class,
        () -> executorService.scheduleAtFixedRate(null, 10, 10, SECONDS));
    assertThrows(
        NullPointerException.class,
        () -> executorService.scheduleAtFixedRate(RUNNABLE, 10, 10, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> executorService.scheduleAtFixedRate(RUNNABLE, 10, -10, SECONDS));
    assertThrows(
        RejectedExecutionException.class,
        () -> shutdownExecutorService.scheduleAtFixedRate(RUNNABLE, 10, 10, SECONDS));

    assertThrows(
        NullPointerException.class,
        () -> executorService.scheduleWithFixedDelay((Runnable) null, 10, 10, SECONDS));
    assertThrows(
        NullPointerException.class,
        () -> executorService.scheduleWithFixedDelay(RUNNABLE, 10, 10, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> executorService.scheduleWithFixedDelay(RUNNABLE, 10, -10, SECONDS));
    assertThrows(
        RejectedExecutionException.class,
        () -> shutdownExecutorService.scheduleWithFixedDelay(RUNNABLE, 10, 10, SECONDS));

    assertThat(executorService.shutdownNow()).isEmpty();
    assertThat(executorService.shutdownNow()).isEmpty();
  }

  @Test
  public void testSubmitMethodErrorChecking() throws Exception {
    FastNanoClockAndSleeper fastNanoClockAndSleeper = new FastNanoClockAndSleeper();
    UnboundedScheduledExecutorService executorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);
    UnboundedScheduledExecutorService shutdownExecutorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);
    shutdownExecutorService.shutdown();

    assertThrows(NullPointerException.class, () -> executorService.submit(null, "result"));
    assertThrows(
        RejectedExecutionException.class, () -> shutdownExecutorService.submit(RUNNABLE, "result"));

    assertThrows(NullPointerException.class, () -> executorService.submit((Runnable) null));
    assertThrows(RejectedExecutionException.class, () -> shutdownExecutorService.submit(RUNNABLE));

    assertThrows(NullPointerException.class, () -> executorService.submit((Callable<String>) null));
    assertThrows(RejectedExecutionException.class, () -> shutdownExecutorService.submit(CALLABLE));

    assertThat(executorService.shutdownNow()).isEmpty();
    assertThat(executorService.shutdownNow()).isEmpty();
  }

  @Test
  public void testInvokeMethodErrorChecking() throws Exception {
    FastNanoClockAndSleeper fastNanoClockAndSleeper = new FastNanoClockAndSleeper();
    UnboundedScheduledExecutorService executorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);
    UnboundedScheduledExecutorService shutdownExecutorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);
    shutdownExecutorService.shutdown();

    assertThrows(NullPointerException.class, () -> executorService.invokeAll(null));
    assertThrows(
        NullPointerException.class, () -> executorService.invokeAll(Collections.singleton(null)));
    assertThrows(
        RejectedExecutionException.class,
        () -> shutdownExecutorService.invokeAll(Collections.singleton(CALLABLE)));

    assertThrows(NullPointerException.class, () -> executorService.invokeAll(null, 10, SECONDS));
    assertThrows(
        NullPointerException.class,
        () -> executorService.invokeAll(Collections.singleton(null), 10, SECONDS));
    assertThrows(
        NullPointerException.class,
        () -> executorService.invokeAll(Collections.singleton(CALLABLE), 10, null));
    assertThrows(
        RejectedExecutionException.class,
        () -> shutdownExecutorService.invokeAll(Collections.singleton(CALLABLE), 10, SECONDS));

    assertThrows(NullPointerException.class, () -> executorService.invokeAny(null));
    assertThrows(
        NullPointerException.class, () -> executorService.invokeAny(Collections.singleton(null)));
    assertThrows(
        IllegalArgumentException.class, () -> executorService.invokeAny(Collections.emptyList()));
    assertThrows(
        ExecutionException.class,
        () -> executorService.invokeAny(Arrays.asList(FAILING_CALLABLE, FAILING_CALLABLE)));
    assertThrows(
        RejectedExecutionException.class,
        () -> shutdownExecutorService.invokeAny(Collections.singleton(CALLABLE)));

    assertThrows(NullPointerException.class, () -> executorService.invokeAny(null, 10, SECONDS));
    assertThrows(
        NullPointerException.class,
        () -> executorService.invokeAny(Collections.singleton(null), 10, SECONDS));
    assertThrows(
        NullPointerException.class,
        () -> executorService.invokeAny(Collections.singleton(CALLABLE), 10, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> executorService.invokeAny(Collections.emptyList(), 10, SECONDS));
    assertThrows(
        ExecutionException.class,
        () ->
            executorService.invokeAny(
                Arrays.asList(FAILING_CALLABLE, FAILING_CALLABLE), 10, SECONDS));
    assertThrows(
        RejectedExecutionException.class,
        () -> shutdownExecutorService.invokeAny(Collections.singleton(CALLABLE), 10, SECONDS));

    assertThat(executorService.shutdownNow()).isEmpty();
    assertThat(executorService.shutdownNow()).isEmpty();
  }

  @Test
  public void testExecuteMethodErrorChecking() throws Exception {
    FastNanoClockAndSleeper fastNanoClockAndSleeper = new FastNanoClockAndSleeper();
    UnboundedScheduledExecutorService executorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);
    UnboundedScheduledExecutorService shutdownExecutorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);
    shutdownExecutorService.shutdown();

    assertThrows(NullPointerException.class, () -> executorService.execute(null));
    assertThrows(RejectedExecutionException.class, () -> shutdownExecutorService.execute(RUNNABLE));

    assertThat(executorService.shutdownNow()).isEmpty();
    assertThat(executorService.shutdownNow()).isEmpty();
  }

  @Test
  public void testAllMethodsReturnScheduledFutures() throws Exception {
    FastNanoClockAndSleeper fastNanoClockAndSleeper = new FastNanoClockAndSleeper();
    UnboundedScheduledExecutorService executorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);

    assertThat(executorService.submit(RUNNABLE)).isInstanceOf(ScheduledFutureTask.class);
    assertThat(executorService.submit(CALLABLE)).isInstanceOf(ScheduledFutureTask.class);
    assertThat(executorService.submit(RUNNABLE, "Answer")).isInstanceOf(ScheduledFutureTask.class);

    assertThat(
        executorService.schedule(RUNNABLE, 10, SECONDS)).isInstanceOf(ScheduledFutureTask.class);
    assertThat(
        executorService.schedule(CALLABLE, 10, SECONDS)).isInstanceOf(ScheduledFutureTask.class);
    assertThat(
        executorService.scheduleAtFixedRate(RUNNABLE, 10, 10, SECONDS)).
        isInstanceOf(ScheduledFutureTask.class);
    assertThat(
        executorService.scheduleWithFixedDelay(RUNNABLE, 10, 10, SECONDS)).
        isInstanceOf(ScheduledFutureTask.class);

    List<Future<String>> futures = executorService.invokeAll(Arrays.asList(CALLABLE, CALLABLE));

    assertThat(futures.size()).isEqualTo(2);
    assertThat(futures.get(0)).isInstanceOf(ScheduledFutureTask.class);
    assertThat(futures.get(1)).isInstanceOf(ScheduledFutureTask.class);

    List<Future<String>> futuresWithTimeout = executorService.invokeAll(Arrays.asList(CALLABLE, CALLABLE), 10, SECONDS);
    assertThat(futuresWithTimeout.size()).isEqualTo(2);
    assertThat(futuresWithTimeout.get(0)).isInstanceOf(ScheduledFutureTask.class);
    assertThat(futuresWithTimeout.get(1)).isInstanceOf(ScheduledFutureTask.class);

    executorService.shutdownNow();
  }

  @Test
  public void testShutdown() throws Exception {
    FastNanoClockAndSleeper fastNanoClockAndSleeper = new FastNanoClockAndSleeper();
    UnboundedScheduledExecutorService executorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);

    Runnable runnable1 = Mockito.mock(Runnable.class);
    Runnable runnable2 = Mockito.mock(Runnable.class);
    Runnable runnable3 = Mockito.mock(Runnable.class);
    Callable<?> callable1 = Mockito.mock(Callable.class);

    Future<?> rFuture1 = executorService.schedule(runnable1, 10, SECONDS);
    Future<?> cFuture1 = executorService.schedule(callable1, 10, SECONDS);
    Future<?> rFuture2 = executorService.scheduleAtFixedRate(runnable2, 10, 10, SECONDS);
    Future<?> rFuture3 = executorService.scheduleWithFixedDelay(runnable3, 10, 10, SECONDS);

    assertThat(
        executorService.shutdownNow()).containsExactly(
            (Runnable) rFuture1, (Runnable) rFuture2, (Runnable) rFuture3, (Runnable) cFuture1);
    verifyNoInteractions(runnable1, runnable2, runnable3, callable1);

    assertThat(executorService.isShutdown()).isTrue();
    assertThat(executorService.awaitTermination(10, SECONDS)).isTrue();
    assertThat(executorService.isTerminated()).isTrue();
  }

  @Test
  public void testExecute() throws Exception {
    FastNanoClockAndSleeper fastNanoClockAndSleeper = new FastNanoClockAndSleeper();
    UnboundedScheduledExecutorService executorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);

    AtomicInteger callCount = new AtomicInteger();
    CountDownLatch countDownLatch = new CountDownLatch(1);

    executorService.execute(
        () -> {
          callCount.incrementAndGet();
          countDownLatch.countDown();
        });

    countDownLatch.await();
    assertThat( callCount.get()).isEqualTo(1);
  }

  @Test
  public void testSubmit() throws Exception {
    List<AtomicInteger> callCounts = new ArrayList<>();
    List<ScheduledFutureTask<?>> futures = new ArrayList<>();

    FastNanoClockAndSleeper fastNanoClockAndSleeper = new FastNanoClockAndSleeper();
    UnboundedScheduledExecutorService executorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);

    callCounts.add(new AtomicInteger());
    futures.add(
        (ScheduledFutureTask<?>)
            executorService.submit(
                (Runnable) callCounts.get(callCounts.size() - 1)::incrementAndGet));
    callCounts.add(new AtomicInteger());
    futures.add(
        (ScheduledFutureTask<?>)
            executorService.submit(
                callCounts.get(callCounts.size() - 1)::incrementAndGet, "Result"));
    callCounts.add(new AtomicInteger());
    futures.add(
        (ScheduledFutureTask<?>)
            executorService.submit(callCounts.get(callCounts.size() - 1)::incrementAndGet));

    assertThat(futures.get(0).get()).isNull();
    assertThat(futures.get(1).get()).isEqualTo("Result");
    assertThat(futures.get(2).get()).isEqualTo(1);

    for (int i = 0; i < callCounts.size(); ++i) {
      assertThat(futures.get(i).isPeriodic()).isFalse();
      assertThat(callCounts.get(i).get()).isEqualTo(1);
    }
  }

  @Test
  public void testSchedule() throws Exception {
    List<AtomicInteger> callCounts = new ArrayList<>();
    List<ScheduledFutureTask<?>> futures = new ArrayList<>();

    FastNanoClockAndSleeper fastNanoClockAndSleeper = new FastNanoClockAndSleeper();
    UnboundedScheduledExecutorService executorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);

    callCounts.add(new AtomicInteger());
    futures.add(
        (ScheduledFutureTask<?>)
            executorService.schedule(
                (Runnable) callCounts.get(callCounts.size() - 1)::incrementAndGet,
                100,
                MILLISECONDS));
    callCounts.add(new AtomicInteger());
    futures.add(
        (ScheduledFutureTask<?>)
            executorService.schedule(
                callCounts.get(callCounts.size() - 1)::incrementAndGet, 100, MILLISECONDS));

    // No tasks should have been picked up
    wakeUpAndCheckTasks(executorService);
    for (int i = 0; i < callCounts.size(); ++i) {
      assertThat(callCounts.get(i).get()).isEqualTo(0);
    }

    // No tasks should have been picked up even if the time advances 99 seconds
    fastNanoClockAndSleeper.sleep(99);
    wakeUpAndCheckTasks(executorService);
    for (int i = 0; i < callCounts.size(); ++i) {
      assertThat(callCounts.get(i).get()).isEqualTo(0);
    }

    // All tasks should wake up and pick-up tasks
    fastNanoClockAndSleeper.sleep(1);
    wakeUpAndCheckTasks(executorService);

    assertThat(futures.get(0).get()).isNull();
    assertThat(futures.get(1).get()).isEqualTo(1);

    for (int i = 0; i < callCounts.size(); ++i) {
      assertThat(futures.get(i).isPeriodic()).isFalse();
      assertThat(callCounts.get(i).get()).isEqualTo(1);
    }

    assertThat(executorService.shutdownNow()).isEmpty();
  }

  @Test
  public void testSchedulePeriodicWithFixedDelay() throws Exception {
    FastNanoClockAndSleeper fastNanoClockAndSleeper = new FastNanoClockAndSleeper();
    UnboundedScheduledExecutorService executorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);

    AtomicInteger callCount = new AtomicInteger();
    CountDownLatch latch = new CountDownLatch(1);

    ScheduledFutureTask<?> future =
        (ScheduledFutureTask<?>)
            executorService.scheduleWithFixedDelay(
                () -> {
                  callCount.incrementAndGet();
                  latch.countDown();
                },
                100,
                50,
                MILLISECONDS);

    // No tasks should have been picked up
    wakeUpAndCheckTasks(executorService);
    // assertEquals(0, callCount.get());
    assertThat(callCount.get()).isEqualTo(0);

    // No tasks should have been picked up even if the time advances 99 seconds
    fastNanoClockAndSleeper.sleep(99);
    wakeUpAndCheckTasks(executorService);
    // assertEquals(0, callCount.get());
    assertThat(callCount.get()).isEqualTo(0);

    // We should have picked up the task 1 time, next task should be scheduled in 50 even though we
    // advanced to 109
    fastNanoClockAndSleeper.sleep(10);
    wakeUpAndCheckTasks(executorService);
    latch.await();
    // assertEquals(1, callCount.get());
    assertThat(callCount.get()).isEqualTo(1);

    for (; ; ) {
      synchronized (executorService.tasks) {
        ScheduledFutureTask<?> task = executorService.tasks.peek();
        if (task != null) {
          // assertEquals(50, task.getDelay(MILLISECONDS));
          assertThat(task.getDelay(MILLISECONDS)).isEqualTo(50);
          break;
        }
      }
      Thread.sleep(1);
    }

    // assertTrue(future.isPeriodic());
    assertThat(future.isPeriodic()).isTrue();
    // assertFalse(future.isDone());
    assertThat(future.isDone()).isFalse();

    future.cancel(true);
    // assertTrue(future.isCancelled());
    assertThat(future.isCancelled()).isTrue();
    // assertTrue(future.isDone());
    assertThat(future.isDone()).isTrue();

    // Cancelled tasks should not be returned during shutdown
    assertThat(executorService.shutdownNow()).isEmpty();
  }

  @Test
  public void testSchedulePeriodicWithFixedRate() throws Exception {
    FastNanoClockAndSleeper fastNanoClockAndSleeper = new FastNanoClockAndSleeper();
    UnboundedScheduledExecutorService executorService =
        new UnboundedScheduledExecutorService(fastNanoClockAndSleeper);

    AtomicInteger callCount = new AtomicInteger();
    CountDownLatch latch = new CountDownLatch(1);

    ScheduledFutureTask<?> future =
        (ScheduledFutureTask<?>)
            executorService.scheduleAtFixedRate(
                () -> {
                  callCount.incrementAndGet();
                  latch.countDown();
                },
                100,
                50,
                MILLISECONDS);

    // No tasks should have been picked up
    wakeUpAndCheckTasks(executorService);
    // assertEquals(0, callCount.get());
    assertThat(callCount.get()).isEqualTo(0);

    // No tasks should have been picked up even if the time advances 99 seconds
    fastNanoClockAndSleeper.sleep(99);
    wakeUpAndCheckTasks(executorService);
    // assertEquals(0, callCount.get());
    assertThat(callCount.get()).isEqualTo(0);

    // We should have picked up the task 1 time, next task should be scheduled in 41 since we
    // advanced to 109
    fastNanoClockAndSleeper.sleep(10);
    wakeUpAndCheckTasks(executorService);
    latch.await();
    // assertEquals(1, callCount.get());
    assertThat(callCount.get()).isEqualTo(1);

    for (; ; ) {
      synchronized (executorService.tasks) {
        ScheduledFutureTask<?> task = executorService.tasks.peek();
        if (task != null) {
          // assertEquals(41, task.getDelay(MILLISECONDS));
          assertThat(task.getDelay(MILLISECONDS)).isEqualTo(41);
          break;
        }
      }
      Thread.sleep(1);
    }

    // assertTrue(future.isPeriodic());
    assertThat(future.isPeriodic()).isTrue();
    // assertFalse(future.isDone());
    assertThat(future.isDone()).isFalse();

    future.cancel(true);
    // assertTrue(future.isCancelled());
    assertThat(future.isCancelled()).isTrue();
    // assertTrue(future.isDone());
    assertThat(future.isDone()).isTrue();

    // Cancelled tasks should not be returned during shutdown
    assertThat(executorService.shutdownNow()).isEmpty();
  }

  void wakeUpAndCheckTasks(UnboundedScheduledExecutorService executorService) throws Exception {
    synchronized (executorService.tasks) {
      executorService.tasks.notify();
    }
    Thread.sleep(100);
  }

  @Test
  @Ignore
  public void testThreadsAreAddedOnlyAsNeededWithContention() throws Exception {
    UnboundedScheduledExecutorService executorService = new UnboundedScheduledExecutorService();
    CountDownLatch start = new CountDownLatch(100);

    ThreadPoolExecutor executor =
        new ThreadPoolExecutor(100, 100, Long.MAX_VALUE, MILLISECONDS, new SynchronousQueue<>());
    // Schedule 100 threads that are going to be scheduling work non-stop but sequentially.
    for (int i = 0; i < 100; ++i) {
      executor.execute(
          () -> {
            start.countDown();
            try {
              start.await();
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            }
            for (int j = 0; j < 1000; ++j) {
              try {
                executorService
                    .submit(
                        () -> {
                          try {
                            Thread.sleep(1);
                          } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                          }
                        })
                    .get();
              } catch (InterruptedException | ExecutionException e) {
                // Ignore, happens on executor shutdown.
              }
            }
          });
    }

    executor.shutdown();
    executor.awaitTermination(3, MINUTES);

    int largestPool = executorService.threadPoolExecutor.getLargestPoolSize();
    LOG.info("Created {} threads to execute at most 100 parallel tasks", largestPool);
    // Ideally we would never create more than 100, however with contention it is still possible
    // some extra threads will be created.
    assertThat(largestPool <= 104).isTrue();
    executorService.shutdown();
  }
}
