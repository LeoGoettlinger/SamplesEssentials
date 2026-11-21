package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

enum DisabledFuture implements Future<Void> {
   INSTANCE;

   static Future<? extends Object> instance() {
      return INSTANCE;
   }

   @Override
   public boolean isDone() {
      return true;
   }

   @Override
   public boolean isCancelled() {
      return false;
   }

   @Override
   public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
   }

   @Nullable
   public Void get(long timeout, TimeUnit unit) {
      return null;
   }

   @Nullable
   public Void get() {
      return null;
   }
}
