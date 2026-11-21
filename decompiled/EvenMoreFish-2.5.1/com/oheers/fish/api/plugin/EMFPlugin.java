package com.oheers.fish.api.plugin;

import com.oheers.fish.api.economy.selling.SoldFish;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class EMFPlugin extends JavaPlugin {
   private static EMFPlugin instance;

   protected EMFPlugin() {
      if (instance != null) {
         throw new UnsupportedOperationException("EMFPlugin has already been assigned!");
      } else {
         instance = this;
      }
   }

   @NonNull
   public static EMFPlugin getInstance() {
      if (instance == null) {
         throw new IllegalStateException("EMFPlugin not found. This should not happen!");
      } else {
         return instance;
      }
   }

   public void debug(String message) {
      this.debug(Level.INFO, message);
   }

   public void debug(String message, Throwable e) {
      this.debug(Level.INFO, message, e);
   }

   public void debug(Level level, String message) {
      if (this.isDebugSession()) {
         log(level, "DEBUG %s".formatted(message));
      }
   }

   public void debug(Level level, String message, Throwable e) {
      if (this.isDebugSession()) {
         log(level, "DEBUG %s".formatted(message), e);
      }
   }

   public abstract boolean isDebugSession();

   public abstract void reload(@Nullable CommandSender var1);

   public abstract boolean isRunningOnFolia();

   private static void log(Level level, String message) {
      log(level, message, null);
   }

   private static void log(Level level, String message, Throwable throwable) {
      StackTraceElement[] stack = Thread.currentThread().getStackTrace();
      String caller = "UnknownSource";
      if (stack.length > 3) {
         StackTraceElement element = stack[3];
         caller = element.getClassName() + "#" + element.getMethodName() + ":" + element.getLineNumber();
      }

      if (throwable == null) {
         getInstance().getLogger().log(level, "[" + caller + "] " + message);
      } else {
         getInstance().getLogger().log(level, "[" + caller + "] " + message, throwable);
      }
   }

   @ApiStatus.Internal
   public abstract void sendMessage(@NonNull String var1, @NonNull Player var2);

   @ApiStatus.Internal
   public abstract void logSoldFish(@NonNull SoldFish var1);

   @ApiStatus.Internal
   public abstract void sendSoldMessage(double var1, int var3, @NonNull Player var4);
}
