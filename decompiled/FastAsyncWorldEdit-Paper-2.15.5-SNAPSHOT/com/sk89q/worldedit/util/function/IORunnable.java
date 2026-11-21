package com.sk89q.worldedit.util.function;

import java.io.IOException;
import java.io.UncheckedIOException;

@FunctionalInterface
public interface IORunnable {
   static Runnable unchecked(IORunnable runnable) {
      return () -> {
         try {
            runnable.run();
         } catch (IOException var2) {
            throw new UncheckedIOException(var2);
         }
      };
   }

   void run() throws IOException;
}
