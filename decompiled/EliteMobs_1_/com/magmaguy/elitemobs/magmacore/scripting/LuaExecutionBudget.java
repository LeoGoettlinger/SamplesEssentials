package com.magmaguy.elitemobs.magmacore.scripting;

import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaClosure;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaError;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaFunction;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.DebugLib;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class LuaExecutionBudget {
   static final long MAX_CPU_MILLIS = 50L;
   static final long MAX_FALLBACK_ELAPSED_MILLIS = 250L;
   static final long MAX_INSTRUCTIONS = 250000L;
   private static final long CLOCK_CHECK_MASK = 1023L;
   private static final LuaExecutionBudget.ExecutionClock ELAPSED_FALLBACK_CLOCK = new LuaExecutionBudget.ElapsedExecutionClock();
   private static final LuaExecutionBudget.ExecutionClock SYSTEM_CLOCK = createSystemClock();
   private static final ThreadLocal<LuaExecutionBudget.Budget> ACTIVE_BUDGET = new ThreadLocal<>();

   private LuaExecutionBudget() {
   }

   public static <T> LuaExecutionBudget.Measurement<T> measure(Supplier<T> call, long maxCpuNanos, long maxInstructions) {
      if (maxCpuNanos <= 0L || maxCpuNanos > TimeUnit.MILLISECONDS.toNanos(50L) || maxInstructions <= 0L || maxInstructions > 250000L) {
         throw new IllegalArgumentException("Lua allowance exceeds the engine ceiling");
      } else if (ACTIVE_BUDGET.get() != null) {
         throw new IllegalStateException("A measured Lua invocation cannot reenter its host");
      } else {
         LuaExecutionBudget.Budget budget = new LuaExecutionBudget.Budget(
            new LuaExecutionBudget.LimitedExecutionClock(SYSTEM_CLOCK, maxCpuNanos, maxCpuNanos * 5L), maxInstructions
         );
         budget.enforceElapsedCeiling = true;
         ACTIVE_BUDGET.set(budget);
         T value = null;
         RuntimeException failure = null;

         try {
            value = call.get();
         } catch (RuntimeException var19) {
            failure = var19;
         } finally {
            ACTIVE_BUDGET.remove();
         }

         try {
            if (budget.instructions > budget.instructionLimit) {
               throw new LuaExecutionBudget.RunawayLimitExceeded("Lua instruction budget exceeded");
            }

            checkTimeBudget(budget);
         } catch (LuaExecutionBudget.RunawayLimitExceeded var18) {
            failure = var18;
         }

         long elapsed = Math.max(0L, budget.clock.nanoTime() - budget.clockStartNanos);
         long wallCharge = (Math.max(0L, System.nanoTime() - budget.elapsedStartNanos) + 4L) / 5L;
         long charged = budget.clock.kind() == LuaExecutionBudget.ClockKind.THREAD_CPU_TIME ? Math.max(elapsed, wallCharge) : wallCharge;
         boolean exhausted = failure instanceof LuaExecutionBudget.RunawayLimitExceeded;
         return new LuaExecutionBudget.Measurement<>(failure == null ? value : null, failure, charged, budget.instructions, exhausted);
      }
   }

   static <T> T run(LuaExecutionBudget.BudgetedCall<T> call) {
      return run(call, SYSTEM_CLOCK);
   }

   static <T> T run(LuaExecutionBudget.BudgetedCall<T> call, LuaExecutionBudget.ExecutionClock clock) {
      return run(call, clock, 250000L);
   }

   static <T> T run(LuaExecutionBudget.BudgetedCall<T> call, long maxCpuNanos, long maxInstructions) {
      if (maxCpuNanos > 0L && maxInstructions > 0L) {
         long fallbackNanos = maxCpuNanos > 1844674407370955161L ? Long.MAX_VALUE : maxCpuNanos * 5L;
         return run(call, new LuaExecutionBudget.LimitedExecutionClock(SYSTEM_CLOCK, maxCpuNanos, fallbackNanos), maxInstructions);
      } else {
         throw new IllegalArgumentException("Lua execution limits must be positive");
      }
   }

   private static <T> T run(LuaExecutionBudget.BudgetedCall<T> call, LuaExecutionBudget.ExecutionClock clock, long maxInstructions) {
      LuaExecutionBudget.Budget existing = ACTIVE_BUDGET.get();
      if (existing != null) {
         return call.call();
      } else {
         LuaExecutionBudget.Budget budget = new LuaExecutionBudget.Budget(clock, maxInstructions);
         ACTIVE_BUDGET.set(budget);

         Object var7;
         try {
            T result = call.call();
            checkTimeBudget(budget);
            var7 = result;
         } finally {
            ACTIVE_BUDGET.remove();
         }

         return (T)var7;
      }
   }

   static DebugLib debugLibrary() {
      return new LuaExecutionBudget.BudgetDebugLib();
   }

   private static void onInstruction() {
      LuaExecutionBudget.Budget budget = ACTIVE_BUDGET.get();
      if (budget != null) {
         long instructions = ++budget.instructions;
         if (instructions > budget.instructionLimit) {
            throw new LuaExecutionBudget.RunawayLimitExceeded("Lua instruction budget exceeded (" + budget.instructionLimit + " instruction limit)");
         } else {
            if ((instructions & 1023L) == 0L) {
               checkTimeBudget(budget);
            }
         }
      }
   }

   private static void checkTimeBudget(LuaExecutionBudget.Budget budget) {
      if (budget.enforceElapsedCeiling && System.nanoTime() - budget.elapsedStartNanos > budget.fallbackClock.limitNanos()) {
         throw new LuaExecutionBudget.RunawayLimitExceeded("Lua elapsed-time safety budget exceeded");
      } else {
         long nowNanos = budget.clock.nanoTime();
         if (nowNanos < 0L) {
            budget.useElapsedFallback();
            nowNanos = budget.clock.nanoTime();
         }

         if (nowNanos - budget.clockStartNanos > budget.clock.limitNanos()) {
            if (budget.clock.kind() == LuaExecutionBudget.ClockKind.THREAD_CPU_TIME) {
               throw new LuaExecutionBudget.RunawayLimitExceeded(
                  "Lua CPU-time budget exceeded (" + formatMillis(budget.clock.limitNanos()) + "ms current-thread CPU limit)"
               );
            } else {
               throw new LuaExecutionBudget.RunawayLimitExceeded(
                  "Lua elapsed-time fallback budget exceeded (" + formatMillis(budget.clock.limitNanos()) + "ms fallback; current-thread CPU time unavailable)"
               );
            }
         }
      }
   }

   private static String formatMillis(long nanos) {
      long wholeMillis = TimeUnit.NANOSECONDS.toMillis(nanos);
      return TimeUnit.MILLISECONDS.toNanos(wholeMillis) == nanos ? Long.toString(wholeMillis) : Double.toString(nanos / 1000000.0);
   }

   private static LuaExecutionBudget.ExecutionClock createSystemClock() {
      ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

      try {
         if (threadBean.isCurrentThreadCpuTimeSupported()) {
            if (!threadBean.isThreadCpuTimeEnabled()) {
               threadBean.setThreadCpuTimeEnabled(true);
            }

            if (threadBean.isThreadCpuTimeEnabled() && threadBean.getCurrentThreadCpuTime() >= 0L) {
               return new LuaExecutionBudget.ThreadCpuExecutionClock(threadBean);
            }
         }
      } catch (UnsupportedOperationException | SecurityException var2) {
      }

      return ELAPSED_FALLBACK_CLOCK;
   }

   private static final class Budget {
      private LuaExecutionBudget.ExecutionClock clock;
      private final LuaExecutionBudget.ExecutionClock fallbackClock;
      private long clockStartNanos;
      private final long elapsedStartNanos = System.nanoTime();
      private final long instructionLimit;
      private long instructions;
      private boolean enforceElapsedCeiling;

      private Budget(LuaExecutionBudget.ExecutionClock requestedClock, long instructionLimit) {
         this.clock = requestedClock;
         this.fallbackClock = (LuaExecutionBudget.ExecutionClock)(requestedClock instanceof LuaExecutionBudget.LimitedExecutionClock limited
            ? new LuaExecutionBudget.FixedElapsedExecutionClock(limited.elapsedLimitNanos)
            : LuaExecutionBudget.ELAPSED_FALLBACK_CLOCK);
         this.instructionLimit = instructionLimit;
         this.clockStartNanos = this.clock.nanoTime();
         if (this.clockStartNanos < 0L) {
            this.useElapsedFallback();
         }
      }

      private void useElapsedFallback() {
         this.clock = this.fallbackClock;
         this.clockStartNanos = this.elapsedStartNanos;
      }
   }

   private static final class BudgetDebugLib extends DebugLib {
      @Override
      public void onInstruction(int pc, Varargs v, int top) {
         LuaExecutionBudget.onInstruction();
      }

      @Override
      public void onCall(LuaFunction function) {
      }

      @Override
      public void onCall(LuaClosure function, Varargs varargs, LuaValue[] stack) {
      }

      @Override
      public void onReturn() {
      }
   }

   @FunctionalInterface
   interface BudgetedCall<T> {
      T call();
   }

   static enum ClockKind {
      THREAD_CPU_TIME,
      ELAPSED_TIME_FALLBACK;
   }

   private static final class ElapsedExecutionClock implements LuaExecutionBudget.ExecutionClock {
      @Override
      public long nanoTime() {
         return System.nanoTime();
      }

      @Override
      public long limitNanos() {
         return TimeUnit.MILLISECONDS.toNanos(250L);
      }

      @Override
      public LuaExecutionBudget.ClockKind kind() {
         return LuaExecutionBudget.ClockKind.ELAPSED_TIME_FALLBACK;
      }
   }

   interface ExecutionClock {
      long nanoTime();

      long limitNanos();

      LuaExecutionBudget.ClockKind kind();
   }

   private static final class FixedElapsedExecutionClock implements LuaExecutionBudget.ExecutionClock {
      private final long limitNanos;

      private FixedElapsedExecutionClock(long limitNanos) {
         this.limitNanos = limitNanos;
      }

      @Override
      public long nanoTime() {
         return System.nanoTime();
      }

      @Override
      public long limitNanos() {
         return this.limitNanos;
      }

      @Override
      public LuaExecutionBudget.ClockKind kind() {
         return LuaExecutionBudget.ClockKind.ELAPSED_TIME_FALLBACK;
      }
   }

   private static final class LimitedExecutionClock implements LuaExecutionBudget.ExecutionClock {
      private final LuaExecutionBudget.ExecutionClock delegate;
      private final long cpuLimitNanos;
      private final long elapsedLimitNanos;

      private LimitedExecutionClock(LuaExecutionBudget.ExecutionClock delegate, long cpuLimitNanos, long elapsedLimitNanos) {
         this.delegate = delegate;
         this.cpuLimitNanos = cpuLimitNanos;
         this.elapsedLimitNanos = elapsedLimitNanos;
      }

      @Override
      public long nanoTime() {
         return this.delegate.nanoTime();
      }

      @Override
      public long limitNanos() {
         return this.kind() == LuaExecutionBudget.ClockKind.THREAD_CPU_TIME ? this.cpuLimitNanos : this.elapsedLimitNanos;
      }

      @Override
      public LuaExecutionBudget.ClockKind kind() {
         return this.delegate.kind();
      }
   }

   public record Measurement<T>(T value, RuntimeException failure, long chargedNanos, long instructions, boolean exhausted) {
   }

   static final class RunawayLimitExceeded extends LuaError {
      private RunawayLimitExceeded(String message) {
         super(message);
      }
   }

   private static final class ThreadCpuExecutionClock implements LuaExecutionBudget.ExecutionClock {
      private final ThreadMXBean threadBean;

      private ThreadCpuExecutionClock(ThreadMXBean threadBean) {
         this.threadBean = threadBean;
      }

      @Override
      public long nanoTime() {
         return this.threadBean.getCurrentThreadCpuTime();
      }

      @Override
      public long limitNanos() {
         return TimeUnit.MILLISECONDS.toNanos(50L);
      }

      @Override
      public LuaExecutionBudget.ClockKind kind() {
         return LuaExecutionBudget.ClockKind.THREAD_CPU_TIME;
      }
   }
}
