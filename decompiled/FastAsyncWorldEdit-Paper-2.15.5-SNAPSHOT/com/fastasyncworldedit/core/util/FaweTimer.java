package com.fastasyncworldedit.core.util;

import java.util.Arrays;

public class FaweTimer implements Runnable {
   private final double[] history = new double[]{
      20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0
   };
   private int historyIndex;
   private long lastPoll = System.currentTimeMillis();
   private long tickStart = System.currentTimeMillis();
   private final long tickInterval = 5L;
   private long tick;
   private long tickMod;
   private long lastGetTPSTick;
   private double lastGetTPSValue = 20.0;
   private long skip = 0L;
   private long skipTick = 0L;

   @Override
   public void run() {
      this.tickStart = System.currentTimeMillis();
      this.tick++;
      if (++this.tickMod == 5L) {
         this.tickMod = 0L;
         long timeSpent = this.tickStart - this.lastPoll;
         if (timeSpent == 0L) {
            timeSpent = 1L;
         }

         double millisPer20Interval = 5000.0;
         double tps = millisPer20Interval / timeSpent;
         this.history[this.historyIndex++] = tps;
         if (this.historyIndex >= this.history.length) {
            this.historyIndex = 0;
         }

         this.lastPoll = this.tickStart;
      }
   }

   public double getTPS() {
      if (this.tick < this.lastGetTPSTick + 5L) {
         return this.lastGetTPSValue;
      } else {
         double total = Arrays.stream(this.history).sum();
         this.lastGetTPSValue = total / this.history.length;
         this.lastGetTPSTick = this.tick;
         return this.lastGetTPSValue;
      }
   }

   public long getTick() {
      return this.tick;
   }

   public long getTickMillis() {
      return System.currentTimeMillis() - this.tickStart;
   }

   public long getTickStart() {
      return this.tickStart;
   }

   public boolean isAbove(double tps) {
      if (tps <= 0.0) {
         return true;
      } else if (this.skip > 0L) {
         if (this.skipTick != this.tick) {
            this.skip--;
            this.skipTick = this.tick;
            return true;
         } else {
            return false;
         }
      } else {
         if (this.getTickMillis() > 100L || this.getTPS() < tps) {
            this.skip = 10L;
            this.skipTick = this.tick;
         }

         return true;
      }
   }

   private boolean runIfAbove(Runnable run, double tps) {
      if (this.isAbove(tps)) {
         run.run();
         return true;
      } else {
         return false;
      }
   }
}
