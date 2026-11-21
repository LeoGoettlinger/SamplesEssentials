package com.sk89q.worldedit.extension.platform;

import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.task.AsyncNotifyKeyedQueue;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractNonPlayerActor implements Actor {
   private final ConcurrentHashMap<String, Object> meta = new ConcurrentHashMap<>();
   private final AtomicInteger runningCount = new AtomicInteger();
   private final AsyncNotifyKeyedQueue asyncNotifyQueue = new AsyncNotifyKeyedQueue((thread, throwable) -> {
      while (throwable.getCause() != null) {
         throwable = throwable.getCause();
      }

      if (throwable instanceof WorldEditException) {
         this.printError(TextComponent.of(throwable.getLocalizedMessage()));
      } else {
         FaweException fe = FaweException.get(throwable);
         if (fe != null) {
            this.printError(fe.getComponent());
         } else {
            throwable.printStackTrace();
         }
      }
   }, this::getUniqueId);

   @Override
   public boolean canDestroyBedrock() {
      return true;
   }

   @Override
   public boolean isPlayer() {
      return false;
   }

   @Override
   public File openFileOpenDialog(String[] extensions) {
      return null;
   }

   @Override
   public File openFileSaveDialog(String[] extensions) {
      return null;
   }

   @Override
   public void dispatchCUIEvent(CUIEvent event) {
   }

   @Override
   public Map<String, Object> getRawMeta() {
      return this.meta;
   }

   @Override
   public boolean runAction(Runnable ifFree, boolean checkFree, boolean async) {
      if (checkFree && this.runningCount.get() != 0) {
         return false;
      } else {
         Runnable wrapped = () -> {
            try {
               this.runningCount.addAndGet(1);
               ifFree.run();
            } finally {
               this.runningCount.decrementAndGet();
            }
         };
         if (async) {
            this.asyncNotifyQueue.run(wrapped);
         } else {
            TaskManager.taskManager().taskNow(wrapped, false);
         }

         return true;
      }
   }
}
