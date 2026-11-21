package com.sk89q.worldedit.util.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Function;

@FunctionalInterface
public interface IOFunction<T, R> {
   static <T, R> Function<T, R> unchecked(IOFunction<T, R> function) {
      return param -> {
         try {
            return function.apply(param);
         } catch (IOException var3) {
            throw new UncheckedIOException(var3);
         }
      };
   }

   R apply(T param) throws IOException;
}
