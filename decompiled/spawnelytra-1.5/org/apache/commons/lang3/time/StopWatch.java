package org.apache.commons.lang3.time;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.commons.lang3.function.FailableSupplier;

public class StopWatch {
   private static final long NANO_2_MILLIS = 1000000L;
   private final String message;
   private StopWatch.State runningState = StopWatch.State.UNSTARTED;
   private StopWatch.SplitState splitState = StopWatch.SplitState.UNSPLIT;
   private long startTimeNanos;
   private Instant startInstant;
   private Instant stopInstant;
   private long stopTimeNanos;

   public static StopWatch create() {
      return new StopWatch();
   }

   public static StopWatch createStarted() {
      StopWatch sw = new StopWatch();
      sw.start();
      return sw;
   }

   public StopWatch() {
      this(null);
   }

   public StopWatch(String message) {
      this.message = message;
   }

   public String formatSplitTime() {
      return DurationFormatUtils.formatDurationHMS(this.getSplitDuration().toMillis());
   }

   public String formatTime() {
      return DurationFormatUtils.formatDurationHMS(this.getTime());
   }

   public <T> T get(Supplier<T> supplier) {
      this.startResume();

      Object var2;
      try {
         var2 = supplier.get();
      } finally {
         this.suspend();
      }

      return (T)var2;
   }

   public Duration getDuration() {
      return Duration.ofNanos(this.getNanoTime());
   }

   public String getMessage() {
      return this.message;
   }

   public long getNanoTime() {
      switch (this.runningState) {
         case RUNNING:
            return System.nanoTime() - this.startTimeNanos;
         case STOPPED:
         case SUSPENDED:
            return this.stopTimeNanos - this.startTimeNanos;
         case UNSTARTED:
            return 0L;
         default:
            throw new IllegalStateException("Illegal running state has occurred.");
      }
   }

   public Duration getSplitDuration() {
      return Duration.ofNanos(this.getSplitNanoTime());
   }

   public long getSplitNanoTime() {
      if (this.splitState != StopWatch.SplitState.SPLIT) {
         throw new IllegalStateException("Stopwatch must be split to get the split time.");
      } else {
         return this.stopTimeNanos - this.startTimeNanos;
      }
   }

   @Deprecated
   public long getSplitTime() {
      return this.nanosToMillis(this.getSplitNanoTime());
   }

   public Instant getStartInstant() {
      if (this.runningState == StopWatch.State.UNSTARTED) {
         throw new IllegalStateException("Stopwatch has not been started");
      } else {
         return this.startInstant;
      }
   }

   @Deprecated
   public long getStartTime() {
      return this.getStartInstant().toEpochMilli();
   }

   public Instant getStopInstant() {
      if (this.runningState == StopWatch.State.UNSTARTED) {
         throw new IllegalStateException("Stopwatch has not been started");
      } else {
         return this.stopInstant;
      }
   }

   @Deprecated
   public long getStopTime() {
      return this.getStopInstant().toEpochMilli();
   }

   public <T, E extends Throwable> T getT(FailableSupplier<T, E> supplier) throws Throwable {
      this.startResume();

      Object var2;
      try {
         var2 = supplier.get();
      } finally {
         this.suspend();
      }

      return (T)var2;
   }

   public long getTime() {
      return this.nanosToMillis(this.getNanoTime());
   }

   public long getTime(TimeUnit timeUnit) {
      return timeUnit.convert(this.getNanoTime(), TimeUnit.NANOSECONDS);
   }

   public boolean isStarted() {
      return this.runningState.isStarted();
   }

   public boolean isStopped() {
      return this.runningState.isStopped();
   }

   public boolean isSuspended() {
      return this.runningState.isSuspended();
   }

   private long nanosToMillis(long nanos) {
      return nanos / 1000000L;
   }

   public void reset() {
      this.runningState = StopWatch.State.UNSTARTED;
      this.splitState = StopWatch.SplitState.UNSPLIT;
   }

   public void resume() {
      if (this.runningState != StopWatch.State.SUSPENDED) {
         throw new IllegalStateException("Stopwatch must be suspended to resume.");
      } else {
         this.startTimeNanos = this.startTimeNanos + (System.nanoTime() - this.stopTimeNanos);
         this.runningState = StopWatch.State.RUNNING;
      }
   }

   public void run(Runnable runnable) {
      this.startResume();

      try {
         runnable.run();
      } finally {
         this.suspend();
      }
   }

   public <E extends Throwable> void runT(FailableRunnable<E> runnable) throws Throwable {
      this.startResume();

      try {
         runnable.run();
      } finally {
         this.suspend();
      }
   }

   public void split() {
      if (this.runningState != StopWatch.State.RUNNING) {
         throw new IllegalStateException("Stopwatch is not running.");
      } else {
         this.stopTimeNanos = System.nanoTime();
         this.splitState = StopWatch.SplitState.SPLIT;
      }
   }

   public void start() {
      if (this.runningState == StopWatch.State.STOPPED) {
         throw new IllegalStateException("Stopwatch must be reset before being restarted.");
      } else if (this.runningState != StopWatch.State.UNSTARTED) {
         throw new IllegalStateException("Stopwatch already started.");
      } else {
         this.startTimeNanos = System.nanoTime();
         this.startInstant = Instant.now();
         this.runningState = StopWatch.State.RUNNING;
      }
   }

   private void startResume() {
      if (this.isStopped()) {
         this.start();
      } else if (this.isSuspended()) {
         this.resume();
      }
   }

   public void stop() {
      if (this.runningState != StopWatch.State.RUNNING && this.runningState != StopWatch.State.SUSPENDED) {
         throw new IllegalStateException("Stopwatch is not running.");
      } else {
         if (this.runningState == StopWatch.State.RUNNING) {
            this.stopTimeNanos = System.nanoTime();
            this.stopInstant = Instant.now();
         }

         this.runningState = StopWatch.State.STOPPED;
      }
   }

   public void suspend() {
      if (this.runningState != StopWatch.State.RUNNING) {
         throw new IllegalStateException("Stopwatch must be running to suspend.");
      } else {
         this.stopTimeNanos = System.nanoTime();
         this.stopInstant = Instant.now();
         this.runningState = StopWatch.State.SUSPENDED;
      }
   }

   public String toSplitString() {
      String msgStr = Objects.toString(this.message, "");
      String formattedTime = this.formatSplitTime();
      return msgStr.isEmpty() ? formattedTime : msgStr + " " + formattedTime;
   }

   @Override
   public String toString() {
      String msgStr = Objects.toString(this.message, "");
      String formattedTime = this.formatTime();
      return msgStr.isEmpty() ? formattedTime : msgStr + " " + formattedTime;
   }

   public void unsplit() {
      if (this.splitState != StopWatch.SplitState.SPLIT) {
         throw new IllegalStateException("Stopwatch has not been split.");
      } else {
         this.splitState = StopWatch.SplitState.UNSPLIT;
      }
   }

   private static enum SplitState {
      SPLIT,
      UNSPLIT;
   }

   private static enum State {
      RUNNING {
         @Override
         boolean isStarted() {
            return true;
         }

         @Override
         boolean isStopped() {
            return false;
         }

         @Override
         boolean isSuspended() {
            return false;
         }
      },
      STOPPED {
         @Override
         boolean isStarted() {
            return false;
         }

         @Override
         boolean isStopped() {
            return true;
         }

         @Override
         boolean isSuspended() {
            return false;
         }
      },
      SUSPENDED {
         @Override
         boolean isStarted() {
            return true;
         }

         @Override
         boolean isStopped() {
            return false;
         }

         @Override
         boolean isSuspended() {
            return true;
         }
      },
      UNSTARTED {
         @Override
         boolean isStarted() {
            return false;
         }

         @Override
         boolean isStopped() {
            return true;
         }

         @Override
         boolean isSuspended() {
            return false;
         }
      };

      private State() {
      }

      abstract boolean isStarted();

      abstract boolean isStopped();

      abstract boolean isSuspended();
   }
}
