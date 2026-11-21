package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@J2ktIncompatible
@GwtIncompatible
public final class SimpleTimeLimiter implements TimeLimiter {
   private final ExecutorService executor;

   private SimpleTimeLimiter(ExecutorService executor) {
      this.executor = Preconditions.checkNotNull(executor);
   }

   public static SimpleTimeLimiter create(ExecutorService executor) {
      return new SimpleTimeLimiter(executor);
   }

   @Override
   public <T> T newProxy(T target, Class<T> interfaceType, long timeoutDuration, TimeUnit timeoutUnit) {
      Preconditions.checkNotNull(target);
      Preconditions.checkNotNull(interfaceType);
      Preconditions.checkNotNull(timeoutUnit);
      checkPositiveTimeout(timeoutDuration);
      Preconditions.checkArgument(interfaceType.isInterface(), "interfaceType must be an interface type");
      final Set<Method> interruptibleMethods = findInterruptibleMethods(interfaceType);
      InvocationHandler handler = new InvocationHandler() {
         @CheckForNull
         @Override
         public Object invoke(Object obj, Method method, @CheckForNull Object[] args) throws Throwable {
            Callable<Object> callable = () -> {
               try {
                  return method.invoke(target, args);
               } catch (InvocationTargetException var4x) {
                  throw SimpleTimeLimiter.throwCause(var4x, false);
               }
            };
            return SimpleTimeLimiter.this.callWithTimeout(callable, timeoutDuration, timeoutUnit, interruptibleMethods.contains(method));
         }
      };
      return newProxy(interfaceType, handler);
   }

   private static <T> T newProxy(Class<T> interfaceType, InvocationHandler handler) {
      Object object = Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class[]{interfaceType}, handler);
      return interfaceType.cast(object);
   }

   @ParametricNullness
   private <T> T callWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit, boolean amInterruptible) throws Exception {
      Preconditions.checkNotNull(callable);
      Preconditions.checkNotNull(timeoutUnit);
      checkPositiveTimeout(timeoutDuration);
      Future<T> future = this.executor.submit(callable);

      try {
         return amInterruptible ? future.get(timeoutDuration, timeoutUnit) : Uninterruptibles.getUninterruptibly(future, timeoutDuration, timeoutUnit);
      } catch (InterruptedException var8) {
         future.cancel(true);
         throw var8;
      } catch (ExecutionException var9) {
         throw throwCause(var9, true);
      } catch (TimeoutException var10) {
         future.cancel(true);
         throw new UncheckedTimeoutException(var10);
      }
   }

   @ParametricNullness
   @CanIgnoreReturnValue
   @Override
   public <T> T callWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException, InterruptedException, ExecutionException {
      Preconditions.checkNotNull(callable);
      Preconditions.checkNotNull(timeoutUnit);
      checkPositiveTimeout(timeoutDuration);
      Future<T> future = this.executor.submit(callable);

      try {
         return future.get(timeoutDuration, timeoutUnit);
      } catch (TimeoutException | InterruptedException var7) {
         future.cancel(true);
         throw var7;
      } catch (ExecutionException var8) {
         this.wrapAndThrowExecutionExceptionOrError(var8.getCause());
         throw new AssertionError();
      }
   }

   @ParametricNullness
   @CanIgnoreReturnValue
   @Override
   public <T> T callUninterruptiblyWithTimeout(Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException, ExecutionException {
      Preconditions.checkNotNull(callable);
      Preconditions.checkNotNull(timeoutUnit);
      checkPositiveTimeout(timeoutDuration);
      Future<T> future = this.executor.submit(callable);

      try {
         return Uninterruptibles.getUninterruptibly(future, timeoutDuration, timeoutUnit);
      } catch (TimeoutException var7) {
         future.cancel(true);
         throw var7;
      } catch (ExecutionException var8) {
         this.wrapAndThrowExecutionExceptionOrError(var8.getCause());
         throw new AssertionError();
      }
   }

   @Override
   public void runWithTimeout(Runnable runnable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException, InterruptedException {
      Preconditions.checkNotNull(runnable);
      Preconditions.checkNotNull(timeoutUnit);
      checkPositiveTimeout(timeoutDuration);
      Future<?> future = this.executor.submit(runnable);

      try {
         future.get(timeoutDuration, timeoutUnit);
      } catch (TimeoutException | InterruptedException var7) {
         future.cancel(true);
         throw var7;
      } catch (ExecutionException var8) {
         this.wrapAndThrowRuntimeExecutionExceptionOrError(var8.getCause());
         throw new AssertionError();
      }
   }

   @Override
   public void runUninterruptiblyWithTimeout(Runnable runnable, long timeoutDuration, TimeUnit timeoutUnit) throws TimeoutException {
      Preconditions.checkNotNull(runnable);
      Preconditions.checkNotNull(timeoutUnit);
      checkPositiveTimeout(timeoutDuration);
      Future<?> future = this.executor.submit(runnable);

      try {
         Uninterruptibles.getUninterruptibly(future, timeoutDuration, timeoutUnit);
      } catch (TimeoutException var7) {
         future.cancel(true);
         throw var7;
      } catch (ExecutionException var8) {
         this.wrapAndThrowRuntimeExecutionExceptionOrError(var8.getCause());
         throw new AssertionError();
      }
   }

   private static Exception throwCause(Exception e, boolean combineStackTraces) throws Exception {
      Throwable cause = e.getCause();
      if (cause == null) {
         throw e;
      } else {
         if (combineStackTraces) {
            StackTraceElement[] combined = ObjectArrays.concat(cause.getStackTrace(), e.getStackTrace(), StackTraceElement.class);
            cause.setStackTrace(combined);
         }

         if (cause instanceof Exception) {
            throw (Exception)cause;
         } else if (cause instanceof Error) {
            throw (Error)cause;
         } else {
            throw e;
         }
      }
   }

   private static Set<Method> findInterruptibleMethods(Class<?> interfaceType) {
      Set<Method> set = Sets.newHashSet();

      for (Method m : interfaceType.getMethods()) {
         if (declaresInterruptedEx(m)) {
            set.add(m);
         }
      }

      return set;
   }

   private static boolean declaresInterruptedEx(Method method) {
      for (Class<?> exType : method.getExceptionTypes()) {
         if (exType == InterruptedException.class) {
            return true;
         }
      }

      return false;
   }

   private void wrapAndThrowExecutionExceptionOrError(Throwable cause) throws ExecutionException {
      if (cause instanceof Error) {
         throw new ExecutionError((Error)cause);
      } else if (cause instanceof RuntimeException) {
         throw new UncheckedExecutionException(cause);
      } else {
         throw new ExecutionException(cause);
      }
   }

   private void wrapAndThrowRuntimeExecutionExceptionOrError(Throwable cause) {
      if (cause instanceof Error) {
         throw new ExecutionError((Error)cause);
      } else {
         throw new UncheckedExecutionException(cause);
      }
   }

   private static void checkPositiveTimeout(long timeoutDuration) {
      Preconditions.checkArgument(timeoutDuration > 0L, "timeout must be positive: %s", timeoutDuration);
   }
}
