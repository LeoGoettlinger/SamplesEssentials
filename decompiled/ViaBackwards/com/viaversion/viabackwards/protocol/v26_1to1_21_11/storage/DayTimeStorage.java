package com.viaversion.viabackwards.protocol.v26_1to1_21_11.storage;

public final class DayTimeStorage {
   private long gameTime;
   private long dayTime;
   private boolean advanceTime;

   public long gameTime() {
      return this.gameTime;
   }

   public void setGameTime(long gameTime) {
      this.gameTime = gameTime;
   }

   public long setGameTimeAndUpdateDayTime(long gameTime) {
      if (this.advanceTime) {
         this.dayTime = this.dayTime + (gameTime - this.gameTime);
      }

      this.gameTime = gameTime;
      return this.dayTime;
   }

   public long dayTime() {
      return this.dayTime;
   }

   public void setDayTime(long dayTime) {
      this.dayTime = dayTime;
   }

   public boolean advanceTime() {
      return this.advanceTime;
   }

   public void setAdvanceTime(boolean advanceTime) {
      this.advanceTime = advanceTime;
   }
}
