package ac.grim.grimac.utils.nmsutil;

import ac.grim.grimac.checks.impl.combat.Reach;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.utils.collisions.CollisionData;
import ac.grim.grimac.utils.collisions.HitboxData;
import ac.grim.grimac.utils.collisions.datatypes.CollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.NoCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.BlockHitData;
import ac.grim.grimac.utils.data.EntityHitData;
import ac.grim.grimac.utils.data.HitData;
import ac.grim.grimac.utils.data.Pair;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import ac.grim.grimac.utils.math.GrimMath;
import ac.grim.grimac.utils.math.Vector3dm;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import lombok.Generated;

public final class WorldRayTrace {
   public static BlockHitData getNearestBlockHitResult(
      GrimPlayer player, StateType heldItem, boolean sourcesHaveHitbox, boolean fluidPlacement, boolean itemUsePlacement
   ) {
      Vector3d startingPos = new Vector3d(player.x, player.y + player.getEyeHeight(), player.z);
      Vector3dm startingVec = new Vector3dm(startingPos.getX(), startingPos.getY(), startingPos.getZ());
      Ray trace = new Ray(player, startingPos.getX(), startingPos.getY(), startingPos.getZ(), player.yaw, player.pitch);
      double distance = itemUsePlacement && player.getClientVersion().isOlderThan(ClientVersion.V_1_20_5)
         ? 5.0
         : player.compensatedEntities.self.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
      Vector3d endVec = trace.getPointAtDistance(distance);
      Vector3d endPos = new Vector3d(endVec.getX(), endVec.getY(), endVec.getZ());
      return traverseBlocks(
         player,
         startingPos,
         endPos,
         (block, vector3i) -> {
            if (fluidPlacement
               && player.getClientVersion().isOlderThan(ClientVersion.V_1_13)
               && CollisionData.getData(block.getType())
                  .getMovementCollisionBox(player, player.getClientVersion(), block, vector3i.getX(), vector3i.getY(), vector3i.getZ())
                  .isNull()) {
               return null;
            } else {
               CollisionBox data = HitboxData.getBlockHitbox(
                  player, heldItem, player.getClientVersion(), block, false, vector3i.getX(), vector3i.getY(), vector3i.getZ()
               );
               List<SimpleCollisionBox> boxes = new ArrayList<>();
               data.downCast(boxes);
               double bestHitResult = Double.MAX_VALUE;
               Vector3dm bestHitLoc = null;
               BlockFace bestFace = null;

               for (SimpleCollisionBox box : boxes) {
                  Pair<Vector3d, BlockFace> intercept = ReachUtils.calculateIntercept(box, trace.origin(), trace.getPointAtDistance(distance));
                  if (intercept.first() != null) {
                     Vector3dm hitLoc = Vector3dm.from(intercept.first());
                     if (hitLoc.distanceSquared(startingVec) < bestHitResult) {
                        bestHitResult = hitLoc.distanceSquared(startingVec);
                        bestHitLoc = hitLoc;
                        bestFace = intercept.second();
                     }
                  }
               }

               if (bestHitLoc != null) {
                  return new BlockHitData(vector3i, bestHitLoc, bestFace, block);
               } else {
                  if (sourcesHaveHitbox
                     && (
                        player.compensatedWorld.isWaterSourceBlock(vector3i.getX(), vector3i.getY(), vector3i.getZ())
                           || player.compensatedWorld.getLavaFluidLevelAt(vector3i.getX(), vector3i.getY(), vector3i.getZ()) == 0.8888889F
                     )) {
                     double waterHeight = player.getClientVersion().isOlderThan(ClientVersion.V_1_13)
                        ? 1.0
                        : player.compensatedWorld.getFluidLevelAt(vector3i.getX(), vector3i.getY(), vector3i.getZ());
                     SimpleCollisionBox boxx = new SimpleCollisionBox(
                        vector3i.getX(), vector3i.getY(), vector3i.getZ(), vector3i.getX() + 1, vector3i.getY() + waterHeight, vector3i.getZ() + 1
                     );
                     Pair<Vector3d, BlockFace> intercept = ReachUtils.calculateIntercept(boxx, trace.origin(), trace.getPointAtDistance(distance));
                     if (intercept.first() != null) {
                        return new BlockHitData(vector3i, Vector3dm.from(intercept.first()), intercept.second(), block);
                     }
                  }

                  return null;
               }
            }
         }
      );
   }

   public static BlockHitData traverseBlocks(GrimPlayer player, Vector3d start, Vector3d end, BiFunction<WrappedBlockState, Vector3i, BlockHitData> predicate) {
      double endX = GrimMath.lerp(-1.0E-7, end.x, start.x);
      double endY = GrimMath.lerp(-1.0E-7, end.y, start.y);
      double endZ = GrimMath.lerp(-1.0E-7, end.z, start.z);
      double startX = GrimMath.lerp(-1.0E-7, start.x, end.x);
      double startY = GrimMath.lerp(-1.0E-7, start.y, end.y);
      double startZ = GrimMath.lerp(-1.0E-7, start.z, end.z);
      int floorStartX = GrimMath.floor(startX);
      int floorStartY = GrimMath.floor(startY);
      int floorStartZ = GrimMath.floor(startZ);
      if (start.equals(end)) {
         return null;
      } else {
         WrappedBlockState state = player.compensatedWorld.getBlock(floorStartX, floorStartY, floorStartZ);
         BlockHitData apply = predicate.apply(state, new Vector3i(floorStartX, floorStartY, floorStartZ));
         if (apply != null) {
            return apply;
         } else {
            double xDiff = endX - startX;
            double yDiff = endY - startY;
            double zDiff = endZ - startZ;
            double xSign = Math.signum(xDiff);
            double ySign = Math.signum(yDiff);
            double zSign = Math.signum(zDiff);
            double posXInverse = xSign == 0.0 ? Double.MAX_VALUE : xSign / xDiff;
            double posYInverse = ySign == 0.0 ? Double.MAX_VALUE : ySign / yDiff;
            double posZInverse = zSign == 0.0 ? Double.MAX_VALUE : zSign / zDiff;
            double d12 = posXInverse * (xSign > 0.0 ? 1.0 - GrimMath.frac(startX) : GrimMath.frac(startX));
            double d13 = posYInverse * (ySign > 0.0 ? 1.0 - GrimMath.frac(startY) : GrimMath.frac(startY));
            double d14 = posZInverse * (zSign > 0.0 ? 1.0 - GrimMath.frac(startZ) : GrimMath.frac(startZ));

            while (d12 <= 1.0 || d13 <= 1.0 || d14 <= 1.0) {
               if (d12 < d13) {
                  if (d12 < d14) {
                     floorStartX = (int)(floorStartX + xSign);
                     d12 += posXInverse;
                  } else {
                     floorStartZ = (int)(floorStartZ + zSign);
                     d14 += posZInverse;
                  }
               } else if (d13 < d14) {
                  floorStartY = (int)(floorStartY + ySign);
                  d13 += posYInverse;
               } else {
                  floorStartZ = (int)(floorStartZ + zSign);
                  d14 += posZInverse;
               }

               state = player.compensatedWorld.getBlock(floorStartX, floorStartY, floorStartZ);
               apply = predicate.apply(state, new Vector3i(floorStartX, floorStartY, floorStartZ));
               if (apply != null) {
                  return apply;
               }
            }

            return null;
         }
      }
   }

   @Nullable
   public static HitData getNearestHitResult(GrimPlayer player, PacketEntity targetEntity, Vector3dm eyePos, Vector3dm lookVec) {
      double maxAttackDistance = player.compensatedEntities.self.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
      double maxBlockDistance = player.compensatedEntities.self.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
      Vector3d startingPos = new Vector3d(eyePos.getX(), eyePos.getY(), eyePos.getZ());
      Vector3dm startingVec = new Vector3dm(startingPos.getX(), startingPos.getY(), startingPos.getZ());
      Ray trace = new Ray(eyePos, lookVec);
      Vector3d endVec = trace.getPointAtDistance(maxBlockDistance);
      Vector3d endPos = new Vector3d(endVec.getX(), endVec.getY(), endVec.getZ());
      BlockHitData blockHitData = getTraverseResult(player, null, startingPos, startingVec, trace, endPos, false, true, maxBlockDistance, true);
      Vector3dm closestHitVec = null;
      PacketEntity closestEntity = null;
      double closestDistanceSquared = blockHitData != null
         ? blockHitData.getBlockHitLocation().distanceSquared(startingVec)
         : maxAttackDistance * maxAttackDistance;

      for (PacketEntity entity : player.compensatedEntities.entityMap.values().stream().filter(PacketEntity::canHit).toList()) {
         SimpleCollisionBox box = null;
         if (entity.equals(targetEntity)) {
            box = entity.getPossibleCollisionBoxes();
            box.expand(player.checkManager.getPacketCheck(Reach.class).threshold);
            if (!player.packetStateData.didLastLastMovementIncludePosition || player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
               box.expand(player.getMovementThreshold());
            }

            if (ReachUtils.isVecInside(box, startingPos)) {
               return new EntityHitData(entity, eyePos);
            }
         } else {
            CollisionBox b = entity.getMinimumPossibleCollisionBoxes();
            if (b instanceof NoCollisionBox) {
               continue;
            }

            box = (SimpleCollisionBox)b;
            box.expand(-player.checkManager.getPacketCheck(Reach.class).threshold);
            if (!player.packetStateData.didLastLastMovementIncludePosition || player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
               box.expand(-player.getMovementThreshold());
            }
         }

         if (player.getClientVersion().isOlderThan(ClientVersion.V_1_9)) {
            box.expand(0.1F);
         }

         Pair<Vector3d, BlockFace> intercept = ReachUtils.calculateIntercept(box, trace.origin(), trace.getPointAtDistance(Math.sqrt(closestDistanceSquared)));
         if (intercept.first() != null) {
            Vector3dm hitLoc = Vector3dm.from(intercept.first());
            double distSquared = hitLoc.distanceSquared(startingVec);
            if (distSquared < closestDistanceSquared) {
               closestDistanceSquared = distSquared;
               closestHitVec = hitLoc;
               closestEntity = entity;
            }
         }
      }

      return (HitData)(closestEntity == null ? blockHitData : new EntityHitData(closestEntity, closestHitVec));
   }

   @NotNull
   public static Pair<Double, HitData> didRayTraceHit(
      GrimPlayer player, PacketEntity targetEntity, List<Pair<Vector3dm, Double>> possibleLookVecsAndEyeHeights, double x, double y, double z
   ) {
      HitData firstObstruction = null;
      double firstObstructionDistanceSq = 0.0;

      for (Pair<Vector3dm, Double> vectorDoublePair : possibleLookVecsAndEyeHeights) {
         Vector3dm lookVec = vectorDoublePair.first();
         double eye = vectorDoublePair.second();
         Vector3dm eyes = new Vector3dm(x, y + eye, z);
         HitData hitResult = getNearestHitResult(player, targetEntity, eyes, lookVec);
         if (hitResult instanceof EntityHitData && ((EntityHitData)hitResult).getEntity().equals(targetEntity)) {
            double distanceSquared = eyes.distanceSquared(hitResult.getBlockHitLocation());
            return new Pair<>(distanceSquared, hitResult);
         }

         if (hitResult != null && firstObstruction == null) {
            firstObstruction = hitResult;
            firstObstructionDistanceSq = eyes.distanceSquared(hitResult.getBlockHitLocation());
         }
      }

      assert firstObstruction != null;

      return new Pair<>(firstObstructionDistanceSq, firstObstruction);
   }

   private static BlockHitData getTraverseResult(
      GrimPlayer player,
      @Nullable StateType heldItem,
      Vector3d startingPos,
      Vector3dm startingVec,
      Ray trace,
      Vector3d endPos,
      boolean sourcesHaveHitbox,
      boolean checkInside,
      double knownDistance,
      boolean shrinkBlocks
   ) {
      return traverseBlocks(
         player,
         startingPos,
         endPos,
         (block, vector3i) -> {
            CollisionBox data = HitboxData.getBlockHitbox(
               player, heldItem, player.getClientVersion(), block, false, vector3i.getX(), vector3i.getY(), vector3i.getZ()
            );
            SimpleCollisionBox[] boxes = new SimpleCollisionBox[15];
            int size = data.downCast(boxes);
            double bestHitResult = Double.MAX_VALUE;
            Vector3dm bestHitLoc = null;
            BlockFace bestFace = null;

            for (int i = 0; i < size; i++) {
               if (shrinkBlocks) {
                  boxes[i].expand(-player.getMovementThreshold());
               }

               Pair<Vector3d, BlockFace> intercept = ReachUtils.calculateIntercept(boxes[i], trace.origin(), trace.getPointAtDistance(knownDistance));
               if (intercept.first() != null) {
                  Vector3dm hitLoc = Vector3dm.from(intercept.first());
                  if (checkInside && ReachUtils.isVecInside(boxes[i], trace.origin())) {
                     return null;
                  }

                  if (hitLoc.distanceSquared(startingVec) < bestHitResult) {
                     bestHitResult = hitLoc.distanceSquared(startingVec);
                     bestHitLoc = hitLoc;
                     bestFace = intercept.second();
                  }
               }
            }

            if (bestHitLoc != null) {
               return new BlockHitData(vector3i, bestHitLoc, bestFace, block);
            } else {
               if (sourcesHaveHitbox
                  && (
                     player.compensatedWorld.isWaterSourceBlock(vector3i.getX(), vector3i.getY(), vector3i.getZ())
                        || player.compensatedWorld.getLavaFluidLevelAt(vector3i.getX(), vector3i.getY(), vector3i.getZ()) == 0.8888889F
                  )) {
                  double waterHeight = player.compensatedWorld.getFluidLevelAt(vector3i.getX(), vector3i.getY(), vector3i.getZ());
                  SimpleCollisionBox box = new SimpleCollisionBox(
                     vector3i.getX(), vector3i.getY(), vector3i.getZ(), vector3i.getX() + 1, vector3i.getY() + waterHeight, vector3i.getZ() + 1
                  );
                  Pair<Vector3d, BlockFace> intercept = ReachUtils.calculateIntercept(box, trace.origin(), trace.getPointAtDistance(knownDistance));
                  if (intercept.first() != null) {
                     return new BlockHitData(vector3i, Vector3dm.from(intercept.first()), intercept.second(), block);
                  }
               }

               return null;
            }
         }
      );
   }

   @Generated
   private WorldRayTrace() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
