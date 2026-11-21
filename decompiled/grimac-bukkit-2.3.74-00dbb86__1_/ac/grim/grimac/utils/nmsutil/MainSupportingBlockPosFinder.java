package ac.grim.grimac.utils.nmsutil;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.MainSupportingBlockData;
import com.google.common.util.concurrent.AtomicDouble;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Generated;

public final class MainSupportingBlockPosFinder {
   public static MainSupportingBlockData findMainSupportingBlockPos(
      GrimPlayer player, MainSupportingBlockData lastSupportingBlock, Vector3d lastMovement, SimpleCollisionBox maxPose, boolean isOnGround
   ) {
      if (!isOnGround) {
         return MainSupportingBlockData.AIR_OFF_GROUND;
      } else {
         SimpleCollisionBox slightlyBelowPlayer = new SimpleCollisionBox(
            maxPose.minX, maxPose.minY - 1.0E-6, maxPose.minZ, maxPose.maxX, maxPose.minY, maxPose.maxZ
         );
         Vector3i supportingBlock = findSupportingBlock(player, slightlyBelowPlayer);
         if (supportingBlock != null || lastSupportingBlock.lastOnGroundAndNoBlock()) {
            return new MainSupportingBlockData(supportingBlock, true);
         } else if (lastMovement != null) {
            SimpleCollisionBox aabb2 = slightlyBelowPlayer.offset(-lastMovement.x, 0.0, -lastMovement.z);
            return new MainSupportingBlockData(findSupportingBlock(player, aabb2), true);
         } else {
            return MainSupportingBlockData.AIR_ON_GROUND;
         }
      }
   }

   @Nullable
   private static Vector3i findSupportingBlock(@NotNull GrimPlayer player, @NotNull SimpleCollisionBox searchBox) {
      Vector3d playerPos = new Vector3d(player.x, player.y, player.z);
      AtomicReference<Vector3i> bestBlockPos = new AtomicReference<>();
      AtomicDouble blockPosDistance = new AtomicDouble(Double.MAX_VALUE);
      Collisions.forEachCollisionBox(
         player,
         searchBox,
         (block, x, y, z) -> {
            Vector3d center = new Vector3d(x + 0.5, y + 0.5, z + 0.5);
            double distance = playerPos.distanceSquared(center);
            if (distance < blockPosDistance.get()
               || distance == blockPosDistance.get() && (bestBlockPos.get() == null || firstHasPriorityOverSecond(x, y, z, bestBlockPos.get()))) {
               bestBlockPos.set(new Vector3i(x, y, z));
               blockPosDistance.set(distance);
            }
         }
      );
      return bestBlockPos.get();
   }

   private static boolean firstHasPriorityOverSecond(int firstX, int firstY, int firstZ, @NotNull Vector3i second) {
      if (firstY < second.getY()) {
         return true;
      } else {
         double sumX = second.getX() - firstX;
         double sumY = second.getZ() - firstZ;
         double horizontalSumTotal = sumX + sumY;
         return horizontalSumTotal == 0.0 ? sumX < 0.0 : horizontalSumTotal < 0.0;
      }
   }

   @Generated
   private MainSupportingBlockPosFinder() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
