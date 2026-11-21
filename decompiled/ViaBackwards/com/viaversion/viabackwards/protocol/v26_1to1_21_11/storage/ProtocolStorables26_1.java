package com.viaversion.viabackwards.protocol.v26_1to1_21_11.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables26_1 extends ProtocolStorablesBase {
   private final DayTimeStorage dayTimeStorage = new DayTimeStorage();
   private int gameMode = -1;

   public DayTimeStorage dayTimeStorage() {
      return this.dayTimeStorage;
   }

   public int gameMode() {
      return this.gameMode;
   }

   public void setGameMode(int gameMode) {
      this.gameMode = gameMode;
   }
}
