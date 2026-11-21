package com.fastasyncworldedit.core.util.progress;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.util.StringMan;
import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import java.util.function.BiConsumer;

public class DefaultProgressTracker implements BiConsumer<DefaultProgressTracker.ProgressType, Integer> {
   private final Player player;
   private final long start;
   private int delay = Settings.QUEUE.PROGRESS.DELAY;
   private int interval = Settings.QUEUE.PROGRESS.INTERVAL;
   private int totalQueue = 0;
   private int amountQueue = 0;
   private int amountDispatch = 0;
   private final int lastSize = 0;
   private boolean done = false;
   private long lastTick = 0L;

   public DefaultProgressTracker(Player player) {
      this.start = System.currentTimeMillis();
      this.player = player;
   }

   public void setInterval(int interval) {
      this.interval = interval;
   }

   public void setDelay(int delay) {
      this.delay = delay;
   }

   public int getInterval() {
      return this.interval;
   }

   public int getDelay() {
      return this.delay;
   }

   public Player getPlayer() {
      return this.player;
   }

   public void accept(DefaultProgressTracker.ProgressType type, Integer amount) {
      switch (type) {
         case DISPATCH:
            this.amountDispatch++;
            this.amountQueue = amount;
            break;
         case QUEUE:
            this.totalQueue++;
            this.amountQueue = amount;
            break;
         case DONE:
            if (this.totalQueue > 64 && !this.done) {
               this.done = true;
               this.done();
            }

            return;
      }

      long now = System.currentTimeMillis();
      if (now - this.start > this.delay) {
         long currentTick = now / 50L;
         if (currentTick > this.lastTick + this.interval) {
            this.lastTick = currentTick;
            this.send();
         }
      }
   }

   private void done() {
      TaskManager.taskManager().task(this::doneTask);
   }

   private void send() {
      TaskManager.taskManager().task(this::sendTask);
   }

   public void doneTask() {
      this.sendTile(TextComponent.empty(), Caption.of("fawe.progress.progress.finished"));
   }

   public void sendTask() {
      String queue = StringMan.padRight(String.valueOf(this.totalQueue), 3);
      String dispatch = StringMan.padLeft(String.valueOf(this.amountDispatch), 3);
      int total = this.amountDispatch != 0 ? this.amountDispatch : this.amountQueue;
      int speed = total != 0 ? (int)(total / Math.max((System.currentTimeMillis() - this.start) / 1000.0, 1.0)) : 0;
      String speedStr = StringMan.padRight(String.valueOf(speed), 3);
      String percent = StringMan.padRight(String.valueOf(this.amountDispatch != 0 ? this.amountDispatch * 100 / this.totalQueue : 0), 3);
      int remaining = speed != 0 ? this.amountQueue / speed : -1;
      this.sendTile(
         TextComponent.empty(),
         Caption.of("fawe.progress.progress.message", queue, dispatch, percent, StringMan.padLeft(speed + "", 3), StringMan.padLeft(remaining + "", 3))
      );
   }

   public void sendTile(Component title, Component sub) {
      this.player.sendTitle(title, sub);
   }

   public static enum ProgressType {
      DISPATCH,
      QUEUE,
      DONE;
   }
}
