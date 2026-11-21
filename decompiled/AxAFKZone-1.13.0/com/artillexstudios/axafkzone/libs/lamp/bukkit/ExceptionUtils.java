package com.artillexstudios.axafkzone.libs.lamp.bukkit;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public final class ExceptionUtils {
   @Nullable
   public static <T> T catching(Supplier<T> supplier, Function<Exception, T> function) {
      try {
         return supplier.get();
      } catch (Exception var3) {
         return function.apply(var3);
      }
   }

   @Nullable
   public static <T> T orElse(ThrowingSupplier<T> supplier, ThrowingSupplier<T> other, Function<Exception, T> function) {
      try {
         return supplier.get();
      } catch (Exception var6) {
         try {
            return other.get();
         } catch (Exception var5) {
            return function.apply(var5);
         }
      }
   }

   public static void catching(Runnable runnable, Consumer<Exception> consumer) {
      try {
         runnable.run();
      } catch (Exception var3) {
         consumer.accept(var3);
      }
   }
}
