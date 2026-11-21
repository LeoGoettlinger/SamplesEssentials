package com.viaversion.viaversion.protocols.v1_20_5to1_21.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_21 extends ProtocolStorablesBase {
   private final EfficiencyAttributeStorage efficiencyAttributes = new EfficiencyAttributeStorage();
   private final PlayerPositionStorage playerPosition = new PlayerPositionStorage();

   public EfficiencyAttributeStorage efficiencyAttributes() {
      return this.efficiencyAttributes;
   }

   public PlayerPositionStorage playerPosition() {
      return this.playerPosition;
   }
}
