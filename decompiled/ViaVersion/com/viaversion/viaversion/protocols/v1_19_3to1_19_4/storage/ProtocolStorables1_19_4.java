package com.viaversion.viaversion.protocols.v1_19_3to1_19_4.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;

public final class ProtocolStorables1_19_4 extends ProtocolStorablesBase {
   private PlayerVehicleTracker playerVehicleTracker = new PlayerVehicleTracker();

   public PlayerVehicleTracker playerVehicleTracker() {
      return this.playerVehicleTracker;
   }

   public void setPlayerVehicleTracker(PlayerVehicleTracker playerVehicleTracker) {
      this.playerVehicleTracker = playerVehicleTracker;
   }
}
