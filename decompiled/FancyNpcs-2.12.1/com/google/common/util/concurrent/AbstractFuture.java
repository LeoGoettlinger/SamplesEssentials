package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.internal.InternalFutureFailureAccess;
import com.google.common.util.concurrent.internal.InternalFutures;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.ForOverride;
import com.google.j2objc.annotations.ReflectionSupport;
import com.google.j2objc.annotations.ReflectionSupport.Level;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import sun.misc.Unsafe;

@ElementTypesAreNonnullByDefault
@GwtCompatible(
   emulated = true
)
@ReflectionSupport(Level.FULL)
public abstract class AbstractFuture<V> extends InternalFutureFailureAccess implements ListenableFuture<V> {
   static final boolean GENERATE_CANCELLATION_CAUSES;
   private static final Logger log;
   private static final long SPIN_THRESHOLD_NANOS = 1000L;
   private static final AbstractFuture.AtomicHelper ATOMIC_HELPER;
   private static final Object NULL;
   @CheckForNull
   private volatile Object value;
   @CheckForNull
   private volatile AbstractFuture.Listener listeners;
   @CheckForNull
   private volatile AbstractFuture.Waiter waiters;

   private void removeWaiter(AbstractFuture.Waiter node) {
      node.thread = null;

      label28:
      while (true) {
         AbstractFuture.Waiter pred = null;
         AbstractFuture.Waiter curr = this.waiters;
         if (curr == AbstractFuture.Waiter.TOMBSTONE) {
            return;
         }

         while (curr != null) {
            AbstractFuture.Waiter succ = curr.next;
            if (curr.thread != null) {
               pred = curr;
            } else if (pred != null) {
               pred.next = succ;
               if (pred.thread == null) {
                  continue label28;
               }
            } else if (!ATOMIC_HELPER.casWaiters(this, curr, succ)) {
               continue label28;
            }

            curr = succ;
         }

         return;
      }
   }

   protected AbstractFuture() {
   }

   @ParametricNullness
   @CanIgnoreReturnValue
   @Override
   public V get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
      long timeoutNanos = unit.toNanos(timeout);
      long remainingNanos = timeoutNanos;
      if (Thread.interrupted()) {
         throw new InterruptedException();
      } else {
         Object localValue = this.value;
         if (localValue != null & !(localValue instanceof AbstractFuture.SetFuture)) {
            return this.getDoneValue(localValue);
         } else {
            long endNanos = timeoutNanos > 0L ? System.nanoTime() + timeoutNanos : 0L;
            if (timeoutNanos >= 1000L) {
               AbstractFuture.Waiter oldHead = this.waiters;
               if (oldHead == AbstractFuture.Waiter.TOMBSTONE) {
                  return this.getDoneValue(Objects.requireNonNull(this.value));
               }

               AbstractFuture.Waiter node = new AbstractFuture.Waiter();

               while (true) {
                  node.setNext(oldHead);
                  if (ATOMIC_HELPER.casWaiters(this, oldHead, node)) {
                     do {
                        OverflowAvoidingLockSupport.parkNanos(this, remainingNanos);
                        if (Thread.interrupted()) {
                           this.removeWaiter(node);
                           throw new InterruptedException();
                        }

                        localValue = this.value;
                        if (localValue != null & !(localValue instanceof AbstractFuture.SetFuture)) {
                           return this.getDoneValue(localValue);
                        }

                        remainingNanos = endNanos - System.nanoTime();
                     } while (remainingNanos >= 1000L);

                     this.removeWaiter(node);
                     break;
                  }

                  oldHead = this.waiters;
                  if (oldHead == AbstractFuture.Waiter.TOMBSTONE) {
                     return this.getDoneValue(Objects.requireNonNull(this.value));
                  }
               }
            }

            while (remainingNanos > 0L) {
               localValue = this.value;
               if (localValue != null & !(localValue instanceof AbstractFuture.SetFuture)) {
                  return this.getDoneValue(localValue);
               }

               if (Thread.interrupted()) {
                  throw new InterruptedException();
               }

               remainingNanos = endNanos - System.nanoTime();
            }

            String futureToString = this.toString();
            String unitString = unit.toString().toLowerCase(Locale.ROOT);
            String message = "Waited " + timeout + " " + unit.toString().toLowerCase(Locale.ROOT);
            if (remainingNanos + 1000L < 0L) {
               message = message + " (plus ";
               long overWaitNanos = -remainingNanos;
               long overWaitUnits = unit.convert(overWaitNanos, TimeUnit.NANOSECONDS);
               long overWaitLeftoverNanos = overWaitNanos - unit.toNanos(overWaitUnits);
               boolean shouldShowExtraNanos = overWaitUnits == 0L || overWaitLeftoverNanos > 1000L;
               if (overWaitUnits > 0L) {
                  message = message + overWaitUnits + " " + unitString;
                  if (shouldShowExtraNanos) {
                     message = message + ",";
                  }

                  message = message + " ";
               }

               if (shouldShowExtraNanos) {
                  message = message + overWaitLeftoverNanos + " nanoseconds ";
               }

               message = message + "delay)";
            }

            if (this.isDone()) {
               throw new TimeoutException(message + " but future completed as timeout expired");
            } else {
               throw new TimeoutException(message + " for " + futureToString);
            }
         }
      }
   }

   @ParametricNullness
   @CanIgnoreReturnValue
   @Override
   public V get() throws InterruptedException, ExecutionException {
      if (Thread.interrupted()) {
         throw new InterruptedException();
      } else {
         Object localValue = this.value;
         if (localValue != null & !(localValue instanceof AbstractFuture.SetFuture)) {
            return this.getDoneValue(localValue);
         } else {
            AbstractFuture.Waiter oldHead = this.waiters;
            if (oldHead != AbstractFuture.Waiter.TOMBSTONE) {
               AbstractFuture.Waiter node = new AbstractFuture.Waiter();

               do {
                  node.setNext(oldHead);
                  if (ATOMIC_HELPER.casWaiters(this, oldHead, node)) {
                     do {
                        LockSupport.park(this);
                        if (Thread.interrupted()) {
                           this.removeWaiter(node);
                           throw new InterruptedException();
                        }

                        localValue = this.value;
                     } while (!(localValue != null & !(localValue instanceof AbstractFuture.SetFuture)));

                     return this.getDoneValue(localValue);
                  }

                  oldHead = this.waiters;
               } while (oldHead != AbstractFuture.Waiter.TOMBSTONE);
            }

            return this.getDoneValue(Objects.requireNonNull(this.value));
         }
      }
   }

   @ParametricNullness
   private V getDoneValue(Object obj) throws ExecutionException {
      if (obj instanceof AbstractFuture.Cancellation) {
         throw cancellationExceptionWithCause("Task was cancelled.", ((AbstractFuture.Cancellation)obj).cause);
      } else if (obj instanceof AbstractFuture.Failure) {
         throw new ExecutionException(((AbstractFuture.Failure)obj).exception);
      } else {
         return (V)(obj == NULL ? NullnessCasts.uncheckedNull() : obj);
      }
   }

   @Override
   public boolean isDone() {
      Object localValue = this.value;
      return localValue != null & !(localValue instanceof AbstractFuture.SetFuture);
   }

   @Override
   public boolean isCancelled() {
      Object localValue = this.value;
      return localValue instanceof AbstractFuture.Cancellation;
   }

   @CanIgnoreReturnValue
   @Override
   public boolean cancel(boolean mayInterruptIfRunning) {
      Object localValue = this.value;
      boolean rValue = false;
      if (localValue == null | localValue instanceof AbstractFuture.SetFuture) {
         Object valueToSet = GENERATE_CANCELLATION_CAUSES
            ? new AbstractFuture.Cancellation(mayInterruptIfRunning, new CancellationException("Future.cancel() was called."))
            : Objects.requireNonNull(
               mayInterruptIfRunning ? AbstractFuture.Cancellation.CAUSELESS_INTERRUPTED : AbstractFuture.Cancellation.CAUSELESS_CANCELLED
            );
         AbstractFuture<?> abstractFuture = this;

         while (true) {
            while (!ATOMIC_HELPER.casValue(abstractFuture, localValue, valueToSet)) {
               localValue = abstractFuture.value;
               if (!(localValue instanceof AbstractFuture.SetFuture)) {
                  return rValue;
               }
            }

            rValue = true;
            complete(abstractFuture, mayInterruptIfRunning);
            if (!(localValue instanceof AbstractFuture.SetFuture)) {
               break;
            }

            ListenableFuture<?> futureToPropagateTo = ((AbstractFuture.SetFuture)localValue).future;
            if (!(futureToPropagateTo instanceof AbstractFuture.Trusted)) {
               futureToPropagateTo.cancel(mayInterruptIfRunning);
               break;
            }

            AbstractFuture<?> trusted = (AbstractFuture<?>)futureToPropagateTo;
            localValue = trusted.value;
            if (!(localValue == null | localValue instanceof AbstractFuture.SetFuture)) {
               break;
            }

            abstractFuture = trusted;
         }
      }

      return rValue;
   }

   protected void interruptTask() {
   }

   protected final boolean wasInterrupted() {
      Object localValue = this.value;
      return localValue instanceof AbstractFuture.Cancellation && ((AbstractFuture.Cancellation)localValue).wasInterrupted;
   }

   @Override
   public void addListener(Runnable listener, Executor executor) {
      Preconditions.checkNotNull(listener, "Runnable was null.");
      Preconditions.checkNotNull(executor, "Executor was null.");
      if (!this.isDone()) {
         AbstractFuture.Listener oldHead = this.listeners;
         if (oldHead != AbstractFuture.Listener.TOMBSTONE) {
            AbstractFuture.Listener newNode = new AbstractFuture.Listener(listener, executor);

            do {
               newNode.next = oldHead;
               if (ATOMIC_HELPER.casListeners(this, oldHead, newNode)) {
                  return;
               }

               oldHead = this.listeners;
            } while (oldHead != AbstractFuture.Listener.TOMBSTONE);
         }
      }

      executeListener(listener, executor);
   }

   @CanIgnoreReturnValue
   protected boolean set(@ParametricNullness V value) {
      Object valueToSet = value == null ? NULL : value;
      if (ATOMIC_HELPER.casValue(this, null, valueToSet)) {
         complete(this, false);
         return true;
      } else {
         return false;
      }
   }

   @CanIgnoreReturnValue
   protected boolean setException(Throwable throwable) {
      Object valueToSet = new AbstractFuture.Failure(Preconditions.checkNotNull(throwable));
      if (ATOMIC_HELPER.casValue(this, null, valueToSet)) {
         complete(this, false);
         return true;
      } else {
         return false;
      }
   }

   @CanIgnoreReturnValue
   protected boolean setFuture(ListenableFuture<? extends V> future) {
      Preconditions.checkNotNull(future);
      Object localValue = this.value;
      if (localValue == null) {
         if (future.isDone()) {
            Object value = getFutureValue(future);
            if (ATOMIC_HELPER.casValue(this, null, value)) {
               complete(this, false);
               return true;
            }

            return false;
         }

         AbstractFuture.SetFuture<V> valueToSet = new AbstractFuture.SetFuture<>(this, future);
         if (ATOMIC_HELPER.casValue(this, null, valueToSet)) {
            try {
               future.addListener(valueToSet, DirectExecutor.INSTANCE);
            } catch (Error | RuntimeException var8) {
               Throwable t = var8;

               AbstractFuture.Failure failure;
               try {
                  failure = new AbstractFuture.Failure(t);
               } catch (Error | RuntimeException var7) {
                  failure = AbstractFuture.Failure.FALLBACK_INSTANCE;
               }

               boolean oomMostLikely = ATOMIC_HELPER.casValue(this, valueToSet, failure);
            }

            return true;
         }

         localValue = this.value;
      }

      if (localValue instanceof AbstractFuture.Cancellation) {
         future.cancel(((AbstractFuture.Cancellation)localValue).wasInterrupted);
      }

      return false;
   }

   private static Object getFutureValue(ListenableFuture<?> future) {
      if (future instanceof AbstractFuture.Trusted) {
         Object v = ((AbstractFuture)future).value;
         if (v instanceof AbstractFuture.Cancellation) {
            AbstractFuture.Cancellation c = (AbstractFuture.Cancellation)v;
            if (c.wasInterrupted) {
               v = c.cause != null ? new AbstractFuture.Cancellation(false, c.cause) : AbstractFuture.Cancellation.CAUSELESS_CANCELLED;
            }
         }

         return Objects.requireNonNull(v);
      } else {
         if (future instanceof InternalFutureFailureAccess) {
            Throwable throwable = InternalFutures.tryInternalFastPathGetFailure((InternalFutureFailureAccess)future);
            if (throwable != null) {
               return new AbstractFuture.Failure(throwable);
            }
         }

         boolean wasCancelled = future.isCancelled();
         if (!GENERATE_CANCELLATION_CAUSES & wasCancelled) {
            return Objects.requireNonNull(AbstractFuture.Cancellation.CAUSELESS_CANCELLED);
         } else {
            try {
               Object v = getUninterruptibly((Future<V>)future);
               if (wasCancelled) {
                  return new AbstractFuture.Cancellation(
                     false, new IllegalArgumentException("get() did not throw CancellationException, despite reporting isCancelled() == true: " + future)
                  );
               } else {
                  return v == null ? NULL : v;
               }
            } catch (ExecutionException var3) {
               return wasCancelled
                  ? new AbstractFuture.Cancellation(
                     false, new IllegalArgumentException("get() did not throw CancellationException, despite reporting isCancelled() == true: " + future, var3)
                  )
                  : new AbstractFuture.Failure(var3.getCause());
            } catch (CancellationException var4) {
               return !wasCancelled
                  ? new AbstractFuture.Failure(
                     new IllegalArgumentException("get() threw CancellationException, despite reporting isCancelled() == false: " + future, var4)
                  )
                  : new AbstractFuture.Cancellation(false, var4);
            } catch (Error | RuntimeException var5) {
               return new AbstractFuture.Failure(var5);
            }
         }
      }
   }

   @ParametricNullness
   private static <V> V getUninterruptibly(Future<V> future) throws ExecutionException {
      boolean interrupted = false;

      try {
         while (true) {
            try {
               return future.get();
            } catch (InterruptedException var6) {
               interrupted = true;
            }
         }
      } finally {
         if (interrupted) {
            Thread.currentThread().interrupt();
         }
      }
   }

   private static void complete(AbstractFuture<?> param, boolean callInterruptTask) {
      AbstractFuture<?> future = param;
      AbstractFuture.Listener next = null;

      label27:
      while (true) {
         future.releaseWaiters();
         if (callInterruptTask) {
            future.interruptTask();
            callInterruptTask = false;
         }

         future.afterDone();
         next = future.clearListeners(next);
         AbstractFuture<?> var8 = null;

         while (next != null) {
            AbstractFuture.Listener curr = next;
            next = next.next;
            Runnable task = Objects.requireNonNull(curr.task);
            if (task instanceof AbstractFuture.SetFuture) {
               AbstractFuture.SetFuture<?> setFuture = (AbstractFuture.SetFuture<?>)task;
               future = setFuture.owner;
               if (future.value == setFuture) {
                  Object valueToSet = getFutureValue(setFuture.future);
                  if (ATOMIC_HELPER.casValue(future, setFuture, valueToSet)) {
                     continue label27;
                  }
               }
            } else {
               executeListener(task, Objects.requireNonNull(curr.executor));
            }
         }

         return;
      }
   }

   @ForOverride
   protected void afterDone() {
   }

   @CheckForNull
   @Override
   protected final Throwable tryInternalFastPathGetFailure() {
      if (this instanceof AbstractFuture.Trusted) {
         Object obj = this.value;
         if (obj instanceof AbstractFuture.Failure) {
            return ((AbstractFuture.Failure)obj).exception;
         }
      }

      return null;
   }

   final void maybePropagateCancellationTo(@CheckForNull Future<?> related) {
      if (related != null & this.isCancelled()) {
         related.cancel(this.wasInterrupted());
      }
   }

   private void releaseWaiters() {
      AbstractFuture.Waiter head = ATOMIC_HELPER.gasWaiters(this, AbstractFuture.Waiter.TOMBSTONE);

      for (AbstractFuture.Waiter currentWaiter = head; currentWaiter != null; currentWaiter = currentWaiter.next) {
         currentWaiter.unpark();
      }
   }

   @CheckForNull
   private AbstractFuture.Listener clearListeners(@CheckForNull AbstractFuture.Listener onto) {
      AbstractFuture.Listener head = ATOMIC_HELPER.gasListeners(this, AbstractFuture.Listener.TOMBSTONE);
      AbstractFuture.Listener reversedList = onto;

      while (head != null) {
         AbstractFuture.Listener tmp = head;
         head = head.next;
         tmp.next = reversedList;
         reversedList = tmp;
      }

      return reversedList;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      if (this.getClass().getName().startsWith("com.google.common.util.concurrent.")) {
         builder.append(this.getClass().getSimpleName());
      } else {
         builder.append(this.getClass().getName());
      }

      builder.append('@').append(Integer.toHexString(System.identityHashCode(this))).append("[status=");
      if (this.isCancelled()) {
         builder.append("CANCELLED");
      } else if (this.isDone()) {
         this.addDoneString(builder);
      } else {
         this.addPendingString(builder);
      }

      return builder.append("]").toString();
   }

   @CheckForNull
   protected String pendingToString() {
      return this instanceof ScheduledFuture ? "remaining delay=[" + ((ScheduledFuture)this).getDelay(TimeUnit.MILLISECONDS) + " ms]" : null;
   }

   private void addPendingString(StringBuilder builder) {
      int truncateLength = builder.length();
      builder.append("PENDING");
      Object localValue = this.value;
      if (localValue instanceof AbstractFuture.SetFuture) {
         builder.append(", setFuture=[");
         this.appendUserObject(builder, ((AbstractFuture.SetFuture)localValue).future);
         builder.append("]");
      } else {
         String pendingDescription;
         try {
            pendingDescription = Strings.emptyToNull(this.pendingToString());
         } catch (StackOverflowError | RuntimeException var6) {
            pendingDescription = "Exception thrown from implementation: " + var6.getClass();
         }

         if (pendingDescription != null) {
            builder.append(", info=[").append(pendingDescription).append("]");
         }
      }

      if (this.isDone()) {
         builder.delete(truncateLength, builder.length());
         this.addDoneString(builder);
      }
   }

   private void addDoneString(StringBuilder builder) {
      try {
         V value = getUninterruptibly(this);
         builder.append("SUCCESS, result=[");
         this.appendResultObject(builder, value);
         builder.append("]");
      } catch (ExecutionException var3) {
         builder.append("FAILURE, cause=[").append(var3.getCause()).append("]");
      } catch (CancellationException var4) {
         builder.append("CANCELLED");
      } catch (RuntimeException var5) {
         builder.append("UNKNOWN, cause=[").append(var5.getClass()).append(" thrown from get()]");
      }
   }

   private void appendResultObject(StringBuilder builder, @CheckForNull Object o) {
      if (o == null) {
         builder.append("null");
      } else if (o == this) {
         builder.append("this future");
      } else {
         builder.append(o.getClass().getName()).append("@").append(Integer.toHexString(System.identityHashCode(o)));
      }
   }

   private void appendUserObject(StringBuilder builder, @CheckForNull Object o) {
      try {
         if (o == this) {
            builder.append("this future");
         } else {
            builder.append(o);
         }
      } catch (StackOverflowError | RuntimeException var4) {
         builder.append("Exception thrown from implementation: ").append(var4.getClass());
      }
   }

   private static void executeListener(Runnable runnable, Executor executor) {
      try {
         executor.execute(runnable);
      } catch (RuntimeException var3) {
         log.log(java.util.logging.Level.SEVERE, "RuntimeException while executing runnable " + runnable + " with executor " + executor, (Throwable)var3);
      }
   }

   private static CancellationException cancellationExceptionWithCause(String message, @CheckForNull Throwable cause) {
      CancellationException exception = new CancellationException(message);
      exception.initCause(cause);
      return exception;
   }

   static {
      boolean generateCancellationCauses;
      try {
         generateCancellationCauses = Boolean.parseBoolean(System.getProperty("guava.concurrent.generate_cancellation_cause", "false"));
      } catch (SecurityException var7) {
         generateCancellationCauses = false;
      }

      GENERATE_CANCELLATION_CAUSES = generateCancellationCauses;
      log = Logger.getLogger(AbstractFuture.class.getName());
      Throwable thrownUnsafeFailure = null;
      Throwable thrownAtomicReferenceFieldUpdaterFailure = null;

      try {
         helper = new AbstractFuture.UnsafeAtomicHelper();
      } catch (Error | RuntimeException var6) {
         thrownUnsafeFailure = var6;

         try {
            helper = new AbstractFuture.SafeAtomicHelper(
               AtomicReferenceFieldUpdater.newUpdater(AbstractFuture.Waiter.class, Thread.class, "thread"),
               AtomicReferenceFieldUpdater.newUpdater(AbstractFuture.Waiter.class, AbstractFuture.Waiter.class, "next"),
               AtomicReferenceFieldUpdater.newUpdater(AbstractFuture.class, AbstractFuture.Waiter.class, "waiters"),
               AtomicReferenceFieldUpdater.newUpdater(AbstractFuture.class, AbstractFuture.Listener.class, "listeners"),
               AtomicReferenceFieldUpdater.newUpdater(AbstractFuture.class, Object.class, "value")
            );
         } catch (Error | RuntimeException var5) {
            thrownAtomicReferenceFieldUpdaterFailure = var5;
            helper = new AbstractFuture.SynchronizedHelper();
         }
      }

      ATOMIC_HELPER = helper;
      Class<?> ensureLoaded = LockSupport.class;
      if (thrownAtomicReferenceFieldUpdaterFailure != null) {
         log.log(java.util.logging.Level.SEVERE, "UnsafeAtomicHelper is broken!", thrownUnsafeFailure);
         log.log(java.util.logging.Level.SEVERE, "SafeAtomicHelper is broken!", thrownAtomicReferenceFieldUpdaterFailure);
      }

      NULL = new Object();
   }

   private abstract static class AtomicHelper {
      private AtomicHelper() {
      }

      abstract void putThread(AbstractFuture.Waiter waiter, Thread newValue);

      abstract void putNext(AbstractFuture.Waiter waiter, @CheckForNull AbstractFuture.Waiter newValue);

      abstract boolean casWaiters(AbstractFuture<?> future, @CheckForNull AbstractFuture.Waiter expect, @CheckForNull AbstractFuture.Waiter update);

      abstract boolean casListeners(AbstractFuture<?> future, @CheckForNull AbstractFuture.Listener expect, AbstractFuture.Listener update);

      abstract AbstractFuture.Waiter gasWaiters(AbstractFuture<?> future, AbstractFuture.Waiter update);

      abstract AbstractFuture.Listener gasListeners(AbstractFuture<?> future, AbstractFuture.Listener update);

      abstract boolean casValue(AbstractFuture<?> future, @CheckForNull Object expect, Object update);
   }

   private static final class Cancellation {
      @CheckForNull
      static final AbstractFuture.Cancellation CAUSELESS_INTERRUPTED;
      @CheckForNull
      static final AbstractFuture.Cancellation CAUSELESS_CANCELLED;
      final boolean wasInterrupted;
      @CheckForNull
      final Throwable cause;

      Cancellation(boolean wasInterrupted, @CheckForNull Throwable cause) {
         this.wasInterrupted = wasInterrupted;
         this.cause = cause;
      }

      static {
         if (AbstractFuture.GENERATE_CANCELLATION_CAUSES) {
            CAUSELESS_CANCELLED = null;
            CAUSELESS_INTERRUPTED = null;
         } else {
            CAUSELESS_CANCELLED = new AbstractFuture.Cancellation(false, null);
            CAUSELESS_INTERRUPTED = new AbstractFuture.Cancellation(true, null);
         }
      }
   }

   private static final class Failure {
      static final AbstractFuture.Failure FALLBACK_INSTANCE = new AbstractFuture.Failure(new Throwable("Failure occurred while trying to finish a future.") {
         @Override
         public synchronized Throwable fillInStackTrace() {
            return this;
         }
      });
      final Throwable exception;

      Failure(Throwable exception) {
         this.exception = Preconditions.checkNotNull(exception);
      }
   }

   private static final class Listener {
      static final AbstractFuture.Listener TOMBSTONE = new AbstractFuture.Listener();
      @CheckForNull
      final Runnable task;
      @CheckForNull
      final Executor executor;
      @CheckForNull
      AbstractFuture.Listener next;

      Listener(Runnable task, Executor executor) {
         this.task = task;
         this.executor = executor;
      }

      Listener() {
         this.task = null;
         this.executor = null;
      }
   }

   private static final class SafeAtomicHelper extends AbstractFuture.AtomicHelper {
      final AtomicReferenceFieldUpdater<AbstractFuture.Waiter, Thread> waiterThreadUpdater;
      final AtomicReferenceFieldUpdater<AbstractFuture.Waiter, AbstractFuture.Waiter> waiterNextUpdater;
      final AtomicReferenceFieldUpdater<AbstractFuture, AbstractFuture.Waiter> waitersUpdater;
      final AtomicReferenceFieldUpdater<AbstractFuture, AbstractFuture.Listener> listenersUpdater;
      final AtomicReferenceFieldUpdater<AbstractFuture, Object> valueUpdater;

      SafeAtomicHelper(
         AtomicReferenceFieldUpdater<AbstractFuture.Waiter, Thread> waiterThreadUpdater,
         AtomicReferenceFieldUpdater<AbstractFuture.Waiter, AbstractFuture.Waiter> waiterNextUpdater,
         AtomicReferenceFieldUpdater<AbstractFuture, AbstractFuture.Waiter> waitersUpdater,
         AtomicReferenceFieldUpdater<AbstractFuture, AbstractFuture.Listener> listenersUpdater,
         AtomicReferenceFieldUpdater<AbstractFuture, Object> valueUpdater
      ) {
         this.waiterThreadUpdater = waiterThreadUpdater;
         this.waiterNextUpdater = waiterNextUpdater;
         this.waitersUpdater = waitersUpdater;
         this.listenersUpdater = listenersUpdater;
         this.valueUpdater = valueUpdater;
      }

      @Override
      void putThread(AbstractFuture.Waiter waiter, Thread newValue) {
         this.waiterThreadUpdater.lazySet(waiter, newValue);
      }

      @Override
      void putNext(AbstractFuture.Waiter waiter, @CheckForNull AbstractFuture.Waiter newValue) {
         this.waiterNextUpdater.lazySet(waiter, newValue);
      }

      @Override
      boolean casWaiters(AbstractFuture<?> future, @CheckForNull AbstractFuture.Waiter expect, @CheckForNull AbstractFuture.Waiter update) {
         return this.waitersUpdater.compareAndSet(future, expect, update);
      }

      @Override
      boolean casListeners(AbstractFuture<?> future, @CheckForNull AbstractFuture.Listener expect, AbstractFuture.Listener update) {
         return this.listenersUpdater.compareAndSet(future, expect, update);
      }

      @Override
      AbstractFuture.Listener gasListeners(AbstractFuture<?> future, AbstractFuture.Listener update) {
         return this.listenersUpdater.getAndSet(future, update);
      }

      @Override
      AbstractFuture.Waiter gasWaiters(AbstractFuture<?> future, AbstractFuture.Waiter update) {
         return this.waitersUpdater.getAndSet(future, update);
      }

      @Override
      boolean casValue(AbstractFuture<?> future, @CheckForNull Object expect, Object update) {
         return this.valueUpdater.compareAndSet(future, expect, update);
      }
   }

   private static final class SetFuture<V> implements Runnable {
      final AbstractFuture<V> owner;
      final ListenableFuture<? extends V> future;

      SetFuture(AbstractFuture<V> owner, ListenableFuture<? extends V> future) {
         this.owner = owner;
         this.future = future;
      }

      @Override
      public void run() {
         if (this.owner.value == this) {
            Object valueToSet = AbstractFuture.getFutureValue(this.future);
            if (AbstractFuture.ATOMIC_HELPER.casValue(this.owner, this, valueToSet)) {
               AbstractFuture.complete(this.owner, false);
            }
         }
      }
   }

   private static final class SynchronizedHelper extends AbstractFuture.AtomicHelper {
      private SynchronizedHelper() {
      }

      @Override
      void putThread(AbstractFuture.Waiter waiter, Thread newValue) {
         waiter.thread = newValue;
      }

      @Override
      void putNext(AbstractFuture.Waiter waiter, @CheckForNull AbstractFuture.Waiter newValue) {
         waiter.next = newValue;
      }

      @Override
      boolean casWaiters(AbstractFuture<?> future, @CheckForNull AbstractFuture.Waiter expect, @CheckForNull AbstractFuture.Waiter update) {
         synchronized (future) {
            if (future.waiters == expect) {
               future.waiters = update;
               return true;
            } else {
               return false;
            }
         }
      }

      @Override
      boolean casListeners(AbstractFuture<?> future, @CheckForNull AbstractFuture.Listener expect, AbstractFuture.Listener update) {
         synchronized (future) {
            if (future.listeners == expect) {
               future.listeners = update;
               return true;
            } else {
               return false;
            }
         }
      }

      @Override
      AbstractFuture.Listener gasListeners(AbstractFuture<?> future, AbstractFuture.Listener update) {
         synchronized (future) {
            AbstractFuture.Listener old = future.listeners;
            if (old != update) {
               future.listeners = update;
            }

            return old;
         }
      }

      @Override
      AbstractFuture.Waiter gasWaiters(AbstractFuture<?> future, AbstractFuture.Waiter update) {
         synchronized (future) {
            AbstractFuture.Waiter old = future.waiters;
            if (old != update) {
               future.waiters = update;
            }

            return old;
         }
      }

      @Override
      boolean casValue(AbstractFuture<?> future, @CheckForNull Object expect, Object update) {
         synchronized (future) {
            if (future.value == expect) {
               future.value = update;
               return true;
            } else {
               return false;
            }
         }
      }
   }

   interface Trusted<V> extends ListenableFuture<V> {
   }

   abstract static class TrustedFuture<V> extends AbstractFuture<V> implements AbstractFuture.Trusted<V> {
      @ParametricNullness
      @CanIgnoreReturnValue
      @Override
      public final V get() throws InterruptedException, ExecutionException {
         return super.get();
      }

      @ParametricNullness
      @CanIgnoreReturnValue
      @Override
      public final V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
         return super.get(timeout, unit);
      }

      @Override
      public final boolean isDone() {
         return super.isDone();
      }

      @Override
      public final boolean isCancelled() {
         return super.isCancelled();
      }

      @Override
      public final void addListener(Runnable listener, Executor executor) {
         super.addListener(listener, executor);
      }

      @CanIgnoreReturnValue
      @Override
      public final boolean cancel(boolean mayInterruptIfRunning) {
         return super.cancel(mayInterruptIfRunning);
      }
   }

   private static final class UnsafeAtomicHelper extends AbstractFuture.AtomicHelper {
      static final Unsafe UNSAFE;
      static final long LISTENERS_OFFSET;
      static final long WAITERS_OFFSET;
      static final long VALUE_OFFSET;
      static final long WAITER_THREAD_OFFSET;
      static final long WAITER_NEXT_OFFSET;

      private UnsafeAtomicHelper() {
      }

      @Override
      void putThread(AbstractFuture.Waiter waiter, Thread newValue) {
         UNSAFE.putObject(waiter, WAITER_THREAD_OFFSET, newValue);
      }

      @Override
      void putNext(AbstractFuture.Waiter waiter, @CheckForNull AbstractFuture.Waiter newValue) {
         UNSAFE.putObject(waiter, WAITER_NEXT_OFFSET, newValue);
      }

      @Override
      boolean casWaiters(AbstractFuture<?> future, @CheckForNull AbstractFuture.Waiter expect, @CheckForNull AbstractFuture.Waiter update) {
         return UNSAFE.compareAndSwapObject(future, WAITERS_OFFSET, expect, update);
      }

      @Override
      boolean casListeners(AbstractFuture<?> future, @CheckForNull AbstractFuture.Listener expect, AbstractFuture.Listener update) {
         return UNSAFE.compareAndSwapObject(future, LISTENERS_OFFSET, expect, update);
      }

      @Override
      AbstractFuture.Listener gasListeners(AbstractFuture<?> future, AbstractFuture.Listener update) {
         return (AbstractFuture.Listener)UNSAFE.getAndSetObject(future, LISTENERS_OFFSET, update);
      }

      @Override
      AbstractFuture.Waiter gasWaiters(AbstractFuture<?> future, AbstractFuture.Waiter update) {
         return (AbstractFuture.Waiter)UNSAFE.getAndSetObject(future, WAITERS_OFFSET, update);
      }

      @Override
      boolean casValue(AbstractFuture<?> future, @CheckForNull Object expect, Object update) {
         return UNSAFE.compareAndSwapObject(future, VALUE_OFFSET, expect, update);
      }

      static {
         Unsafe unsafe = null;

         try {
            unsafe = Unsafe.getUnsafe();
         } catch (SecurityException var6) {
            try {
               unsafe = AccessController.doPrivileged(new PrivilegedExceptionAction<Unsafe>() {
                  public Unsafe run() throws Exception {
                     Class<Unsafe> k = Unsafe.class;

                     for (Field f : k.getDeclaredFields()) {
                        f.setAccessible(true);
                        Object x = f.get(null);
                        if (k.isInstance(x)) {
                           return k.cast(x);
                        }
                     }

                     throw new NoSuchFieldError("the Unsafe");
                  }
               });
            } catch (PrivilegedActionException var5) {
               throw new RuntimeException("Could not initialize intrinsics", var5.getCause());
            }
         }

         try {
            Class<?> abstractFuture = AbstractFuture.class;
            WAITERS_OFFSET = unsafe.objectFieldOffset(abstractFuture.getDeclaredField("waiters"));
            LISTENERS_OFFSET = unsafe.objectFieldOffset(abstractFuture.getDeclaredField("listeners"));
            VALUE_OFFSET = unsafe.objectFieldOffset(abstractFuture.getDeclaredField("value"));
            WAITER_THREAD_OFFSET = unsafe.objectFieldOffset(AbstractFuture.Waiter.class.getDeclaredField("thread"));
            WAITER_NEXT_OFFSET = unsafe.objectFieldOffset(AbstractFuture.Waiter.class.getDeclaredField("next"));
            UNSAFE = unsafe;
         } catch (NoSuchFieldException var3) {
            throw new RuntimeException(var3);
         } catch (RuntimeException var4) {
            throw var4;
         }
      }
   }

   private static final class Waiter {
      static final AbstractFuture.Waiter TOMBSTONE = new AbstractFuture.Waiter(false);
      @CheckForNull
      volatile Thread thread;
      @CheckForNull
      volatile AbstractFuture.Waiter next;

      Waiter(boolean unused) {
      }

      Waiter() {
         AbstractFuture.ATOMIC_HELPER.putThread(this, Thread.currentThread());
      }

      void setNext(@CheckForNull AbstractFuture.Waiter next) {
         AbstractFuture.ATOMIC_HELPER.putNext(this, next);
      }

      void unpark() {
         Thread w = this.thread;
         if (w != null) {
            this.thread = null;
            LockSupport.unpark(w);
         }
      }
   }
}
