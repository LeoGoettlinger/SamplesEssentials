package com.viaversion.viabackwards.protocol.v1_21_9to1_21_7.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.storage.BundleStateTracker;

public final class ProtocolStorables1_21_9 extends ProtocolStorablesBase {
   private final PlayerRotationStorage playerRotationStorage = new PlayerRotationStorage();
   private final DimensionScaleStorage dimensionScaleStorage = new DimensionScaleStorage();
   private final BundleStateTracker bundleStateTracker = new BundleStateTracker();

   public PlayerRotationStorage playerRotationStorage() {
      return this.playerRotationStorage;
   }

   public DimensionScaleStorage dimensionScaleStorage() {
      return this.dimensionScaleStorage;
   }

   public BundleStateTracker bundleStateTracker() {
      return this.bundleStateTracker;
   }
}
