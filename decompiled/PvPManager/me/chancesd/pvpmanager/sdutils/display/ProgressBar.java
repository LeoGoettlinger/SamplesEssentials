package me.chancesd.pvpmanager.sdutils.display;

import com.google.common.base.Strings;
import me.chancesd.pvpmanager.sdutils.utils.Utils;

public class ProgressBar {
   private final int totalBars;
   private final String symbol;
   private final String originalMessage;
   private String message;
   private long goal;
   private long progress;
   private boolean dirty = true;

   public ProgressBar(String message, int totalBars, long goal, String symbol) {
      this.originalMessage = message;
      this.totalBars = totalBars;
      this.goal = goal;
      this.symbol = symbol;
   }

   public ProgressBar(String message, int totalBars, long goal, String symbol, long millisecondsPassed) {
      this(message, totalBars, goal, symbol);
      this.setProgress(millisecondsPassed);
      this.calculate();
   }

   public ProgressBar setProgress(long progress) {
      if (this.progress == progress) {
         return this;
      } else {
         this.progress = progress;
         this.dirty = true;
         return this;
      }
   }

   public ProgressBar setGoal(long goal) {
      if (this.goal == goal) {
         return this;
      } else {
         this.goal = goal;
         this.dirty = true;
         return this;
      }
   }

   public ProgressBar calculate() {
      if (!this.dirty) {
         return this;
      } else {
         double percent = (double)this.progress / this.goal;
         int progressBars = (int)(this.totalBars * percent);
         this.message = this.originalMessage
            .replace("<barsLeft>", Strings.repeat(this.symbol, this.totalBars - progressBars))
            .replace("<barsPassed>", Strings.repeat(this.symbol, progressBars))
            .replace("<time>", Double.toString(Utils.roundTo1Decimal((this.goal - this.progress) / 1000.0)));
         this.dirty = false;
         return this;
      }
   }

   public long getProgress() {
      return this.progress;
   }

   public String getMessage() {
      return this.message;
   }

   public int getTotalBars() {
      return this.totalBars;
   }

   public String getSymbol() {
      return this.symbol;
   }

   public long getGoal() {
      return this.goal;
   }

   @Override
   public String toString() {
      return this.message;
   }
}
