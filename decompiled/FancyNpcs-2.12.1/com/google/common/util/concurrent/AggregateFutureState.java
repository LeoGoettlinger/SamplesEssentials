package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.Sets;
import com.google.j2objc.annotations.ReflectionSupport;
import com.google.j2objc.annotations.ReflectionSupport.Level;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible(
   emulated = true
)
@ReflectionSupport(Level.FULL)
abstract class AggregateFutureState<OutputT> extends AbstractFuture.TrustedFuture<OutputT> {
   @CheckForNull
   private volatile Set<Throwable> seenExceptions = null;
   private volatile int remaining;
   private static final AggregateFutureState.AtomicHelper ATOMIC_HELPER;
   private static final Logger log = Logger.getLogger(AggregateFutureState.class.getName());

   AggregateFutureState(int remainingFutures) {
      this.remaining = remainingFutures;
   }

   final Set<Throwable> getOrInitSeenExceptions() {
      Set<Throwable> seenExceptionsLocal = this.seenExceptions;
      if (seenExceptionsLocal == null) {
         seenExceptionsLocal = Sets.newConcurrentHashSet();
         this.addInitialException(seenExceptionsLocal);
         ATOMIC_HELPER.compareAndSetSeenExceptions(this, null, seenExceptionsLocal);
         seenExceptionsLocal = Objects.requireNonNull(this.seenExceptions);
      }

      return seenExceptionsLocal;
   }

   abstract void addInitialException(Set<Throwable> seen);

   final int decrementRemainingAndGet() {
      return ATOMIC_HELPER.decrementAndGetRemainingCount(this);
   }

   final void clearSeenExceptions() {
      this.seenExceptions = null;
   }

   static {
      Throwable thrownReflectionFailure = null;

      AggregateFutureState.AtomicHelper helper;
      try {
         helper = new AggregateFutureState.SafeAtomicHelper(
            AtomicReferenceFieldUpdater.newUpdater(AggregateFutureState.class, Set.class, "seenExceptions"),
            AtomicIntegerFieldUpdater.newUpdater(AggregateFutureState.class, "remaining")
         );
      } catch (Error | RuntimeException var3) {
         thrownReflectionFailure = var3;
         helper = new AggregateFutureState.SynchronizedAtomicHelper();
      }

      ATOMIC_HELPER = helper;
      if (thrownReflectionFailure != null) {
         log.log(java.util.logging.Level.SEVERE, "SafeAtomicHelper is broken!", thrownReflectionFailure);
      }
   }

   private abstract static class AtomicHelper {
      private AtomicHelper() {
      }

      abstract void compareAndSetSeenExceptions(AggregateFutureState<?> state, @CheckForNull Set<Throwable> expect, Set<Throwable> update);

      abstract int decrementAndGetRemainingCount(AggregateFutureState<?> state);
   }

   private static final class SafeAtomicHelper extends AggregateFutureState.AtomicHelper {
      final AtomicReferenceFieldUpdater<AggregateFutureState<?>, Set<Throwable>> seenExceptionsUpdater;
      final AtomicIntegerFieldUpdater<AggregateFutureState<?>> remainingCountUpdater;

      SafeAtomicHelper(AtomicReferenceFieldUpdater seenExceptionsUpdater, AtomicIntegerFieldUpdater remainingCountUpdater) {
         this.seenExceptionsUpdater = seenExceptionsUpdater;
         this.remainingCountUpdater = remainingCountUpdater;
      }

      @Override
      void compareAndSetSeenExceptions(AggregateFutureState<?> state, @CheckForNull Set<Throwable> expect, Set<Throwable> update) {
         this.seenExceptionsUpdater.compareAndSet(state, expect, update);
      }

      @Override
      int decrementAndGetRemainingCount(AggregateFutureState<?> state) {
         return this.remainingCountUpdater.decrementAndGet(state);
      }
   }

   private static final class SynchronizedAtomicHelper extends AggregateFutureState.AtomicHelper {
      private SynchronizedAtomicHelper() {
      }

      @Override
      void compareAndSetSeenExceptions(AggregateFutureState<?> state, @CheckForNull Set<Throwable> expect, Set<Throwable> update) {
         synchronized (state) {
            if (state.seenExceptions == expect) {
               state.seenExceptions = update;
            }
         }
      }

      @Override
      int decrementAndGetRemainingCount(AggregateFutureState<?> state) {
         synchronized (state) {
            return --state.remaining;
         }
      }
   }
}
