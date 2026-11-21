package com.viaversion.viabackwards.protocol.v26_3to26_2.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ProtocolStorables26_3 extends ProtocolStorablesBase {
   private Integer currentTeleportId;
   private int brewingStandContainerId = -1;

   @Nullable
   public Integer currentTeleportId() {
      return this.currentTeleportId;
   }

   public void setCurrentTeleportId(@Nullable Integer currentTeleportId) {
      this.currentTeleportId = currentTeleportId;
   }

   public int brewingStandContainerId() {
      return this.brewingStandContainerId;
   }

   public void setBrewingStandContainerId(int brewingStandContainerId) {
      this.brewingStandContainerId = brewingStandContainerId;
   }
}
