package com.viaversion.viaversion.protocols.v1_20_2to1_20_3.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_20_3 extends ProtocolStorablesBase {
   private final ScoreboardStorage scoreboard = new ScoreboardStorage();

   public ScoreboardStorage scoreboard() {
      return this.scoreboard;
   }
}
