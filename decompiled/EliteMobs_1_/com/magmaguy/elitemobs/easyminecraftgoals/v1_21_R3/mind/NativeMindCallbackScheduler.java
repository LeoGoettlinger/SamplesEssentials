package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R3.mind;

import com.magmaguy.elitemobs.magmacore.ai.MindExecutionPolicy;
import com.magmaguy.elitemobs.magmacore.ai.MindFailure;
import com.magmaguy.elitemobs.magmacore.ai.MindRunawayException;
import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

final class NativeMindCallbackScheduler {
   private final MindExecutionPolicy policy;
   private final NativeMindServerBudget serverBudget;
   private final NativeMindCallbackScheduler.FailureReporter failureReporter;
   private final LongSupplier nanoTime;
   private int serverTick;
   private long entitySpentNanos;
   private int callbackCount;
   private boolean callbackSliceExhausted;
   private boolean tickStarted;

   NativeMindCallbackScheduler(MindExecutionPolicy policy, NativeMindServerBudget serverBudget, NativeMindCallbackScheduler.FailureReporter failureReporter) {
      this(policy, serverBudget, failureReporter, System::nanoTime);
   }

   NativeMindCallbackScheduler(
      MindExecutionPolicy policy, NativeMindServerBudget serverBudget, NativeMindCallbackScheduler.FailureReporter failureReporter, LongSupplier nanoTime
   ) {
      this.policy = Objects.requireNonNull(policy, "policy");
      this.serverBudget = Objects.requireNonNull(serverBudget, "serverBudget");
      this.failureReporter = Objects.requireNonNull(failureReporter, "failureReporter");
      this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
   }

   void beginTick(int currentServerTick) {
      this.serverTick = currentServerTick;
      this.entitySpentNanos = 0L;
      this.callbackCount = 0;
      this.callbackSliceExhausted = false;
      this.tickStarted = true;
   }

   <T> NativeMindCallbackScheduler.CallbackResult<T> invoke(String callbackIdentifier, Supplier<T> callback) {
      if (!this.tickStarted) {
         throw new IllegalStateException("Mind callback scheduler tick has not started");
      } else {
         Objects.requireNonNull(callbackIdentifier, "callbackIdentifier");
         Objects.requireNonNull(callback, "callback");
         if (!this.callbackSliceExhausted
            && this.callbackCount < this.policy.maxCallbacksPerEntityTick()
            && this.entitySpentNanos < this.policy.maxEntityNanosPerTick()
            && this.serverBudget.permits(this.serverTick, this.policy.maxServerNanosPerTick())) {
            this.callbackCount++;
            long started = this.nanoTime.getAsLong();

            try {
               T result = callback.get();
               this.record(this.elapsedSince(started));
               return NativeMindCallbackScheduler.CallbackResult.success(result);
            } catch (MindRunawayException var6) {
               this.record(this.elapsedSince(started));
               this.failureReporter.report(callbackIdentifier, MindFailure.Kind.RUNAWAY_LIMIT_EXCEEDED, var6);
               return NativeMindCallbackScheduler.CallbackResult.failed();
            } catch (RuntimeException var7) {
               this.record(this.elapsedSince(started));
               this.failureReporter.report(callbackIdentifier, MindFailure.Kind.CALLBACK_EXCEPTION, var7);
               return NativeMindCallbackScheduler.CallbackResult.failed();
            }
         } else {
            return NativeMindCallbackScheduler.CallbackResult.skipped();
         }
      }
   }

   private void record(long elapsedNanos) {
      long next = this.entitySpentNanos + elapsedNanos;
      this.entitySpentNanos = next < this.entitySpentNanos ? Long.MAX_VALUE : next;
      this.serverBudget.record(this.serverTick, elapsedNanos);
      if (elapsedNanos > this.policy.maxCallbackNanos()) {
         this.callbackSliceExhausted = true;
      }
   }

   private long elapsedSince(long started) {
      return Math.max(0L, this.nanoTime.getAsLong() - started);
   }

   record CallbackResult<T>(NativeMindCallbackScheduler.CallbackResult.State state, T value) {
      static <T> NativeMindCallbackScheduler.CallbackResult<T> success(T value) {
         return new NativeMindCallbackScheduler.CallbackResult<>(NativeMindCallbackScheduler.CallbackResult.State.SUCCESS, value);
      }

      static <T> NativeMindCallbackScheduler.CallbackResult<T> skipped() {
         return new NativeMindCallbackScheduler.CallbackResult<>(NativeMindCallbackScheduler.CallbackResult.State.SKIPPED, null);
      }

      static <T> NativeMindCallbackScheduler.CallbackResult<T> failed() {
         return new NativeMindCallbackScheduler.CallbackResult<>(NativeMindCallbackScheduler.CallbackResult.State.FAILED, null);
      }

      boolean successful() {
         return this.state == NativeMindCallbackScheduler.CallbackResult.State.SUCCESS;
      }

      boolean wasSkipped() {
         return this.state == NativeMindCallbackScheduler.CallbackResult.State.SKIPPED;
      }

      static enum State {
         SUCCESS,
         SKIPPED,
         FAILED;
      }
   }

   @FunctionalInterface
   interface FailureReporter {
      void report(String var1, MindFailure.Kind var2, Throwable var3);
   }
}
