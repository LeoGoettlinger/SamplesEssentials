package com.lishid.openinv.util.profile;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ProfileStore {
   void addProfile(@NotNull Profile var1);

   void setup() throws Exception;

   void shutdown() throws Exception;

   void tryImport() throws Exception;

   @Nullable
   Profile getProfileExact(@NotNull String var1);

   @Nullable
   Profile getProfileInexact(@NotNull String var1);

   static void warnMainThread(@NotNull Logger logger) {
      if (Bukkit.getServer().isPrimaryThread()) {
         Throwable throwable = new Throwable("Current stack trace");
         StackTraceElement[] stackTrace = throwable.getStackTrace();
         if (stackTrace.length >= 2) {
            StackTraceElement caller = stackTrace[1];
            logger.warning(() -> String.format("Call to %s#%s made on the main thread!", caller.getClassName(), caller.getMethodName()));
            logger.warning("This can cause the server to hang, potentially severely.");
            logger.log(Level.WARNING, "Current stack trace", throwable);
         }
      }
   }
}
