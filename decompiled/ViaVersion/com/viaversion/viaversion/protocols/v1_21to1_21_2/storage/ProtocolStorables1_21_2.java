package com.viaversion.viaversion.protocols.v1_21to1_21_2.storage;

import com.viaversion.viaversion.connection.ProtocolStorablesBase;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ProtocolStorables1_21_2 extends ProtocolStorablesBase {
   private final BundleStateTracker bundleStateTracker = new BundleStateTracker();
   private final TeleportAckCancelStorage teleportAckCancelStorage = new TeleportAckCancelStorage();
   private GroundFlagTracker groundFlagTracker = new GroundFlagTracker();
   private LastExplosionPowerStorage lastExplosionPowerStorage;
   private PlayerPositionStorage playerPositionStorage;
   private ChunkLoadTracker chunkLoadTracker;
   private ClientVehicleStorage clientVehicleStorage;

   public BundleStateTracker bundleStateTracker() {
      return this.bundleStateTracker;
   }

   public GroundFlagTracker groundFlagTracker() {
      return this.groundFlagTracker;
   }

   public void setGroundFlagTracker(GroundFlagTracker groundFlagTracker) {
      this.groundFlagTracker = groundFlagTracker;
   }

   public TeleportAckCancelStorage teleportAckCancelStorage() {
      return this.teleportAckCancelStorage;
   }

   @Nullable
   public LastExplosionPowerStorage lastExplosionPowerStorage() {
      return this.lastExplosionPowerStorage;
   }

   public void setLastExplosionPowerStorage(LastExplosionPowerStorage lastExplosionPowerStorage) {
      this.lastExplosionPowerStorage = lastExplosionPowerStorage;
   }

   @Nullable
   public PlayerPositionStorage playerPositionStorage() {
      return this.playerPositionStorage;
   }

   public void setPlayerPositionStorage(PlayerPositionStorage playerPositionStorage) {
      this.playerPositionStorage = playerPositionStorage;
   }

   @Nullable
   public ChunkLoadTracker chunkLoadTracker() {
      return this.chunkLoadTracker;
   }

   public void setChunkLoadTracker(ChunkLoadTracker chunkLoadTracker) {
      this.chunkLoadTracker = chunkLoadTracker;
   }

   @Nullable
   public ClientVehicleStorage clientVehicleStorage() {
      return this.clientVehicleStorage;
   }

   public void setClientVehicleStorage(@Nullable ClientVehicleStorage clientVehicleStorage) {
      this.clientVehicleStorage = clientVehicleStorage;
   }
}
