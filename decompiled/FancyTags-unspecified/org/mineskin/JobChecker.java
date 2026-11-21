package org.mineskin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.mineskin.data.JobInfo;
import org.mineskin.data.JobReference;
import org.mineskin.data.JobStatus;
import org.mineskin.exception.MineskinException;
import org.mineskin.options.IJobCheckOptions;
import org.mineskin.request.backoff.RequestInterval;

public class JobChecker {
   private final MineSkinClient client;
   private JobInfo jobInfo;
   private final ScheduledExecutorService executor;
   private CompletableFuture<JobReference> future;
   private final AtomicInteger attempts = new AtomicInteger(0);
   private final int maxAttempts;
   private final int initialDelay;
   private final RequestInterval interval;
   private final TimeUnit timeUnit;
   private final boolean useEta;

   public JobChecker(MineSkinClient client, JobInfo jobInfo, IJobCheckOptions options) {
      this.client = client;
      this.jobInfo = jobInfo;
      this.executor = options.scheduler();
      this.maxAttempts = options.maxAttempts();
      this.initialDelay = options.initialDelayMillis();
      this.interval = options.interval();
      this.timeUnit = TimeUnit.MILLISECONDS;
      this.useEta = options.useEta();
   }

   @Deprecated
   public JobChecker(MineSkinClient client, JobInfo jobInfo, ScheduledExecutorService executor, int maxAttempts, int initialDelaySeconds, int intervalSeconds) {
      this(client, jobInfo, executor, maxAttempts, initialDelaySeconds, intervalSeconds, TimeUnit.SECONDS);
   }

   @Deprecated
   public JobChecker(
      MineSkinClient client, JobInfo jobInfo, ScheduledExecutorService executor, int maxAttempts, int initialDelay, int interval, TimeUnit timeUnit
   ) {
      this(client, jobInfo, executor, maxAttempts, initialDelay, interval, timeUnit, false);
   }

   @Deprecated
   public JobChecker(
      MineSkinClient client,
      JobInfo jobInfo,
      ScheduledExecutorService executor,
      int maxAttempts,
      int initialDelay,
      int interval,
      TimeUnit timeUnit,
      boolean useEta
   ) {
      this.client = client;
      this.jobInfo = jobInfo;
      this.executor = executor;
      this.maxAttempts = maxAttempts;
      this.initialDelay = (int)timeUnit.toMillis(initialDelay);
      this.interval = RequestInterval.constant((int)timeUnit.toMillis(interval));
      this.timeUnit = timeUnit;
      this.useEta = useEta;
   }

   public CompletableFuture<JobReference> check() {
      this.future = new CompletableFuture<>();
      if (this.useEta && this.jobInfo.eta() > 1L) {
         long delay = this.jobInfo.eta() - System.currentTimeMillis();
         if (delay > 0L) {
            this.client.getLogger().log(Level.FINER, "Scheduling first job check in {0}ms based on ETA", delay);
            this.executor.schedule(this::checkJob, delay, TimeUnit.MILLISECONDS);
            return this.future;
         }
      }

      this.executor.schedule(this::checkJob, (long)this.initialDelay, this.timeUnit);
      return this.future;
   }

   private void checkJob() {
      int attempt = this.attempts.incrementAndGet();
      if (attempt > this.maxAttempts) {
         this.future.completeExceptionally(new MineskinException("Max attempts reached").withBreadcrumb(this.jobInfo.getBreadcrumb()));
      } else {
         this.client.queue().get(this.jobInfo).thenAccept(response -> {
            JobInfo info = response.getBody();
            if (info != null) {
               this.jobInfo = info;
            }

            if (this.jobInfo.status() != JobStatus.COMPLETED && this.jobInfo.status() != JobStatus.FAILED) {
               this.executor.schedule(this::checkJob, (long)this.interval.getInterval(attempt), this.timeUnit);
            } else {
               this.future.complete(response);
            }
         }).exceptionally(throwable -> {
            this.future.completeExceptionally(throwable);
            return null;
         });
      }
   }
}
