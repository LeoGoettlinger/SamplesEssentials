package org.jdbi.v3.core.internal.exceptions;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jdbi.v3.core.internal.UtilityClassException;

public class Unchecked {
   private Unchecked() {
      throw new UtilityClassException();
   }

   public static <T> Consumer<T> consumer(CheckedConsumer<T> checkedConsumer) {
      return x -> {
         try {
            checkedConsumer.accept(x);
         } catch (Throwable var3) {
            throw Sneaky.throwAnyway(var3);
         }
      };
   }

   public static Runnable runnable(Unchecked.CheckedRunnable checkedRunnable) {
      return () -> {
         try {
            checkedRunnable.run();
         } catch (Throwable var2) {
            throw Sneaky.throwAnyway(var2);
         }
      };
   }

   public static <T> Unchecked.SneakyCallable<T> callable(CheckedCallable<T> checkedCallable) {
      return () -> {
         try {
            return checkedCallable.call();
         } catch (Throwable var2) {
            throw Sneaky.throwAnyway(var2);
         }
      };
   }

   public static <T> Supplier<T> supplier(CheckedSupplier<T> checkedSupplier) {
      return () -> {
         try {
            return checkedSupplier.get();
         } catch (Throwable var2) {
            throw Sneaky.throwAnyway(var2);
         }
      };
   }

   public static <X, T> Function<X, T> function(CheckedFunction<X, T> checkedFunction) {
      return x -> {
         try {
            return checkedFunction.apply(x);
         } catch (Throwable var3) {
            throw Sneaky.throwAnyway(var3);
         }
      };
   }

   public static <X, Y, T> BiFunction<X, Y, T> biFunction(CheckedBiFunction<X, Y, T> checkedBiFunction) {
      return (x, y) -> {
         try {
            return checkedBiFunction.apply(x, y);
         } catch (Throwable var4) {
            throw Sneaky.throwAnyway(var4);
         }
      };
   }

   public static <X, Y> BiConsumer<X, Y> biConsumer(CheckedBiConsumer<X, Y> checkedBiConsumer) {
      return (x, y) -> {
         try {
            checkedBiConsumer.accept(x, y);
         } catch (Throwable var4) {
            throw Sneaky.throwAnyway(var4);
         }
      };
   }

   @FunctionalInterface
   public interface CheckedRunnable {
      void run() throws Exception;
   }

   @FunctionalInterface
   public interface SneakyCallable<T> extends Callable<T> {
      @Override
      T call();
   }
}
