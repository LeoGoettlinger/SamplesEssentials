package com.viaversion.viaversion.protocols.v1_21_9to1_21_11.storage;

public final class GameTimeStorage {
   private long gameTime;

   public long gameTime() {
      return this.gameTime;
   }

   public void setGameTime(long gameTime) {
      this.gameTime = gameTime;
   }

   public void incrementGameTime() {
      this.gameTime++;
   }
}
