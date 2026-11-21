package com.sk89q.worldedit.command.util;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.Timer;
import javax.annotation.Nullable;

public class FutureProgressListener implements Runnable {
   private static final Timer timer = new Timer();
   private static final int MESSAGE_DELAY = 1000;
   private static final int MESSAGE_PERIOD = 10000;
   private final MessageTimerTask task;

   @Deprecated
   public FutureProgressListener(Actor sender, String message) {
      this(sender, TextComponent.of(message));
   }

   public FutureProgressListener(Actor sender, Component message) {
      this(sender, message, null);
   }

   public FutureProgressListener(Actor sender, Component message, @Nullable Component workingMessage) {
      Preconditions.checkNotNull(sender);
      Preconditions.checkNotNull(message);
      this.task = new MessageTimerTask(sender, message, workingMessage);
      timer.scheduleAtFixedRate(this.task, 1000L, 10000L);
   }

   @Override
   public void run() {
      this.task.cancel();
   }

   @Deprecated
   public static void addProgressListener(ListenableFuture<?> future, Actor sender, String message) {
      addProgressListener(future, sender, TextComponent.of(message));
   }

   public static void addProgressListener(ListenableFuture<?> future, Actor sender, Component message) {
      future.addListener(new FutureProgressListener(sender, message), MoreExecutors.directExecutor());
   }

   public static void addProgressListener(ListenableFuture<?> future, Actor sender, Component message, Component workingMessage) {
      future.addListener(new FutureProgressListener(sender, message, workingMessage), MoreExecutors.directExecutor());
   }
}
