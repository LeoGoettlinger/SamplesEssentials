package ac.grim.grimac.predictionengine.blockeffects;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.GameMode;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.PotentSulfurState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.utils.collisions.CollisionData;
import ac.grim.grimac.utils.collisions.datatypes.CollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import ac.grim.grimac.utils.math.GrimMath;
import ac.grim.grimac.utils.math.Vector3dm;
import ac.grim.grimac.utils.nmsutil.EntityTypeTags;
import ac.grim.grimac.utils.nmsutil.Materials;
import java.util.List;
import lombok.Generated;

public final class PotentSulfurGeyser {
   private static final boolean HAS_GEYSERS = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_26_2);

   public static void launchEntityTicker(GrimPlayer player, Vector3dm clientVelocity, boolean checkFallDistanceAccumulation) {
      if (!player.getClientVersion().isOlderThan(ClientVersion.V_26_2) && HAS_GEYSERS) {
         PacketEntity launchedEntity = player.compensatedEntities.self.getRiding();
         if (launchedEntity == null) {
            launchedEntity = player.compensatedEntities.self;
         } else if (launchedEntity.getRiding() != null) {
            return;
         }

         if (!launchedEntity.isDead
            && player.gamemode != GameMode.SPECTATOR
            && (launchedEntity != player.compensatedEntities.self || !player.isFlying)
            && !EntityTypeTags.NOT_AFFECTED_BY_GEYSERS.anyOf(launchedEntity.getType())) {
            launchFromPotentSulfurGeysers(player, clientVelocity, checkFallDistanceAccumulation);
         }
      }
   }

   private static void launchFromPotentSulfurGeysers(GrimPlayer player, Vector3dm clientVelocity, boolean checkFallDistanceAccumulation) {
      SimpleCollisionBox box = player.boundingBox;
      int minX = GrimMath.floor(box.minX);
      int maxX = GrimMath.floor(box.maxX);
      int minY = Math.max(player.compensatedWorld.getMinHeight(), GrimMath.floor(box.minY) - 25);
      int maxY = Math.min(player.compensatedWorld.getMaxHeight() - 1, GrimMath.floor(box.maxY) - 1);
      int minZ = GrimMath.floor(box.minZ);
      int maxZ = GrimMath.floor(box.maxZ);
      if (!player.compensatedWorld.areChunksUnloadedAt(minX, minY, minZ, maxX, maxY, maxZ)) {
         List<CompensatedGeysers.GeyserBlockEntity> tickerPositions = player.compensatedGeysers.getTickersInOrder(player, minX, minY, minZ, maxX, maxY, maxZ);
         if (tickerPositions != null && !tickerPositions.isEmpty()) {
            for (CompensatedGeysers.GeyserBlockEntity geyser : tickerPositions) {
               int x = geyser.x();
               int y = geyser.y();
               int z = geyser.z();
               WrappedBlockState block = player.compensatedWorld.getBlock(x, y, z);
               if (isActivePotentSulfur(block)) {
                  Vector3i sourceBlock = findNoxiousGasSourceBlock(player, x, y, z);
                  if (sourceBlock != null) {
                     int waterBlocks = sourceBlock.y - y - 1;
                     int geyserForceHeight = getUnobstructedBlockCount(player, x, y + 1, z, waterBlocks);
                     if (geyserForceHeight > 0 && box.isIntersected(new SimpleCollisionBox(x, y + 1, z).expandMax(0.0, geyserForceHeight - 1.0, 0.0))) {
                        if (checkFallDistanceAccumulation) {
                           checkFallDistanceAccumulation(player, clientVelocity);
                        }

                        if (clientVelocity.getY() < 0.3F + waterBlocks * 0.1F) {
                           clientVelocity.add(0.0, 0.2F, 0.0);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static Vector3i findNoxiousGasSourceBlock(GrimPlayer player, int x, int y, int z) {
      int maxY = y + 5;

      for (int currentY = y + 1; currentY <= maxY; currentY++) {
         WrappedBlockState state = player.compensatedWorld.getBlock(x, currentY, z);
         boolean isWaterSource = Materials.isWaterSource(player.getClientVersion(), state);
         if (!isWaterSource || state.getType() != StateTypes.WATER && !isGeyserPassableBlock(player, state, x, currentY, z, y)) {
            if (state.getType().isAir() || isGeyserPassableBlock(player, state, x, currentY, z, y)) {
               return new Vector3i(x, currentY, z);
            }
            break;
         }
      }

      return null;
   }

   private static int getUnobstructedBlockCount(GrimPlayer player, int x, int y, int z, int waterBlocks) {
      int geyserForceHeight = 6 * waterBlocks;
      int geyserPositionY = y - 1;

      for (int i = 0; i < geyserForceHeight; i++) {
         WrappedBlockState state = player.compensatedWorld.getBlock(x, y + i, z);
         if (!isGeyserPassableBlock(player, state, x, y + i, z, geyserPositionY)) {
            return i;
         }
      }

      return geyserForceHeight;
   }

   private static boolean isGeyserPassableBlock(GrimPlayer player, WrappedBlockState state, int x, int y, int z, int geyserPositionY) {
      if (state.getType().isAir() || state.getType() == StateTypes.WATER || state.getType() == StateTypes.LAVA || state.getType() == StateTypes.POWDER_SNOW) {
         return true;
      } else if (state.getType() == StateTypes.SCAFFOLDING) {
         return geyserPositionY > y + 1.0 - 1.0E-5 ? false : state.getDistance() == 0 || !state.isBottom() || geyserPositionY <= y - 1.0E-5;
      } else if (!state.getType().isSolid()) {
         return true;
      } else {
         CollisionBox collisionBox = CollisionData.getData(state.getType()).getMovementCollisionBox(player, player.getClientVersion(), state, x, y, z);
         return collisionBox.isNull();
      }
   }

   private static boolean isActivePotentSulfur(WrappedBlockState state) {
      if (state.getType() != StateTypes.POTENT_SULFUR) {
         return false;
      } else {
         PotentSulfurState potentSulfurState = state.getPotentSulfurState();
         return potentSulfurState == PotentSulfurState.ERUPTING || potentSulfurState == PotentSulfurState.CONTINUOUS;
      }
   }

   private static void checkFallDistanceAccumulation(GrimPlayer player, Vector3dm clientVelocity) {
      if (clientVelocity.getY() > -0.5 && player.fallDistance > 1.0) {
         player.fallDistance = 1.0;
      }
   }

   @Generated
   private PotentSulfurGeyser() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
