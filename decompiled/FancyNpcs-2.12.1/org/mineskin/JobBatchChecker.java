package org.mineskin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.mineskin.data.JobInfo;
import org.mineskin.data.JobReference;
import org.mineskin.data.JobStatus;
import org.mineskin.exception.MineskinException;
import org.mineskin.options.IJobCheckOptions;
import org.mineskin.response.JobListResponse;

public class JobBatchChecker {
   public static final int BATCH_THRESHOLD = 4;
   private final MineSkinClient client;
   private final IJobCheckOptions options;
   private final Runnable onJobFailure;
   private final Map<String, JobBatchChecker.Pending> pending = new ConcurrentHashMap<>();
   private ScheduledFuture<?> scheduledCheck;

   public JobBatchChecker(MineSkinClient client, IJobCheckOptions options) {
      this(client, options, null);
   }

   public JobBatchChecker(MineSkinClient client, IJobCheckOptions options, Runnable onJobFailure) {
      this.client = client;
      this.options = options;
      this.onJobFailure = onJobFailure;
   }

   public CompletableFuture<JobReference> register(JobInfo jobInfo) {
      long firstDelayMillis;
      if (this.options.useEta() && jobInfo.eta() > 1L) {
         long etaDelay = jobInfo.eta() - System.currentTimeMillis();
         firstDelayMillis = etaDelay > 0L ? etaDelay : this.options.initialDelayMillis();
      } else {
         firstDelayMillis = this.options.initialDelayMillis();
      }

      long nextCheckAt = System.currentTimeMillis() + firstDelayMillis;
      JobBatchChecker.Pending p = new JobBatchChecker.Pending(jobInfo, nextCheckAt);
      JobBatchChecker.Pending prev = this.pending.putIfAbsent(jobInfo.id(), p);
      if (prev != null) {
         return prev.future;
      } else {
         this.client.getLogger().log(Level.FINER, "Registered job {0} for batch checking (first check in {1}ms)", new Object[]{jobInfo.id(), firstDelayMillis});
         this.rescheduleIfNeeded();
         return p.future;
      }
   }

   private synchronized void rescheduleIfNeeded() {
      if (this.scheduledCheck != null) {
         this.scheduledCheck.cancel(false);
         this.scheduledCheck = null;
      }

      if (!this.pending.isEmpty()) {
         long soonest = Long.MAX_VALUE;

         for (JobBatchChecker.Pending p : this.pending.values()) {
            if (p.nextCheckAt < soonest) {
               soonest = p.nextCheckAt;
            }
         }

         long delay = Math.max(0L, soonest - System.currentTimeMillis());
         this.scheduledCheck = this.options.scheduler().schedule(this::tick, delay, TimeUnit.MILLISECONDS);
      }
   }

   private void tick() {
      long now = System.currentTimeMillis();
      List<JobBatchChecker.Pending> due = new ArrayList<>();

      for (JobBatchChecker.Pending p : this.pending.values()) {
         if (p.nextCheckAt <= now) {
            int attempt = p.attempts.incrementAndGet();
            if (attempt > this.options.maxAttempts()) {
               p.future.completeExceptionally(new MineskinException("Max attempts reached").withBreadcrumb(p.jobInfo.getBreadcrumb()));
               this.pending.remove(p.jobInfo.id());
            } else {
               p.nextCheckAt = now + this.options.interval().getInterval(attempt);
               due.add(p);
            }
         }
      }

      if (due.isEmpty()) {
         this.rescheduleIfNeeded();
      } else {
         if (this.pending.size() > 4) {
            this.checkViaList(due);
         } else {
            this.checkIndividually(due);
         }
      }
   }

   private void checkViaList(List<JobBatchChecker.Pending> due) {
      this.client.getLogger().log(Level.FINER, "Batch-checking {0} pending jobs via list endpoint", this.pending.size());
      this.client.queue().list().whenComplete((response, throwable) -> {
         try {
            if (throwable == null) {
               Map<String, JobInfo> byId = indexById(response);
               long now = System.currentTimeMillis();

               for (JobBatchChecker.Pending p : new ArrayList<>(this.pending.values())) {
                  JobInfo updated = byId.get(p.jobInfo.id());
                  if (updated != null) {
                     p.jobInfo = updated;
                  } else if (!due.contains(p)) {
                     continue;
                  }

                  this.handleStatus(p, now);
               }

               return;
            }

            for (JobBatchChecker.Pending p : due) {
               p.future.completeExceptionally(throwable);
               this.pending.remove(p.jobInfo.id());
            }
         } finally {
            this.rescheduleIfNeeded();
         }
      });
   }

   private void checkIndividually(List<JobBatchChecker.Pending> due) {
      AtomicInteger remaining = new AtomicInteger(due.size());

      for (JobBatchChecker.Pending p : due) {
         this.client.queue().get(p.jobInfo).whenComplete((response, throwable) -> {
            try {
               if (throwable == null) {
                  JobInfo info = response.getBody();
                  if (info != null) {
                     p.jobInfo = info;
                  }

                  JobStatus status = p.jobInfo.status();
                  if (status == JobStatus.FAILED || status == JobStatus.COMPLETED && p.jobInfo.result().isPresent()) {
                     p.future.complete(response);
                     this.pending.remove(p.jobInfo.id());
                     if (status == JobStatus.FAILED && this.onJobFailure != null) {
                        this.onJobFailure.run();
                     }

                     return;
                  }

                  p.nextCheckAt = System.currentTimeMillis() + this.options.interval().getInterval(p.attempts.get());
                  return;
               }

               p.future.completeExceptionally(throwable);
               this.pending.remove(p.jobInfo.id());
            } finally {
               if (remaining.decrementAndGet() == 0) {
                  this.rescheduleIfNeeded();
               }
            }
         });
      }
   }

   private void handleStatus(JobBatchChecker.Pending p, long now) {
      JobStatus status = p.jobInfo.status();
      if (status == JobStatus.FAILED || status == JobStatus.COMPLETED && p.jobInfo.result().isPresent()) {
         this.pending.remove(p.jobInfo.id());
         if (status == JobStatus.FAILED && this.onJobFailure != null) {
            this.onJobFailure.run();
         }

         this.fetchFullAndComplete(p);
      } else {
         p.nextCheckAt = now + this.options.interval().getInterval(p.attempts.get());
      }
   }

   private void fetchFullAndComplete(JobBatchChecker.Pending p) {
      this.client.queue().get(p.jobInfo).whenComplete((response, throwable) -> {
         if (throwable != null) {
            p.future.completeExceptionally(throwable);
         } else {
            p.future.complete(response);
         }
      });
   }

   private static Map<String, JobInfo> indexById(JobListResponse response) {
      List<JobInfo> jobs = response.getJobs();
      if (jobs != null && !jobs.isEmpty()) {
         Map<String, JobInfo> byId = new HashMap<>(jobs.size());

         for (JobInfo j : jobs) {
            if (j.id() != null) {
               byId.put(j.id(), j);
            }
         }

         return byId;
      } else {
         return Map.of();
      }
   }

   private static final class Pending {
      volatile JobInfo jobInfo;
      final CompletableFuture<JobReference> future = new CompletableFuture<>();
      final AtomicInteger attempts = new AtomicInteger(0);
      volatile long nextCheckAt;

      Pending(JobInfo jobInfo, long nextCheckAt) {
         this.jobInfo = jobInfo;
         this.nextCheckAt = nextCheckAt;
      }
   }
}
