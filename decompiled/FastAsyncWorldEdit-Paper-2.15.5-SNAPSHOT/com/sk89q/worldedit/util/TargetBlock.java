package com.sk89q.worldedit.util;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.SolidBlockMask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import javax.annotation.Nullable;

public class TargetBlock {
   private final Extent world;
   private int maxDistance;
   private double checkDistance;
   private double curDistance;
   private BlockVector3 targetPos = BlockVector3.ZERO;
   private Vector3 targetPosDouble = Vector3.ZERO;
   private BlockVector3 prevPos = BlockVector3.ZERO;
   private Vector3 offset = Vector3.ZERO;
   private Mask stopMask;
   private Mask solidMask;

   public TargetBlock(Player player) {
      this.world = player.getWorld();
      this.setValues(player.getLocation().toVector(), player.getLocation().getYaw(), player.getLocation().getPitch(), 300, 1.65, 0.2);
      this.stopMask = new ExistingBlockMask(this.world);
      this.solidMask = new SolidBlockMask(this.world);
   }

   public TargetBlock(Player player, int maxDistance, double checkDistance) {
      this(player, player.getWorld(), maxDistance, checkDistance);
   }

   public TargetBlock(Player player, Extent extent, int maxDistance, double checkDistance) {
      this.world = player.getWorld();
      this.setValues(player.getLocation().toVector(), player.getLocation().getYaw(), player.getLocation().getPitch(), maxDistance, 1.65, checkDistance);
      this.stopMask = new ExistingBlockMask(this.world);
      this.solidMask = new SolidBlockMask(this.world);
   }

   public void setStopMask(@Nullable Mask stopMask) {
      if (stopMask == null) {
         this.stopMask = new ExistingBlockMask(this.world);
      } else {
         this.stopMask = stopMask;
      }
   }

   public void setSolidMask(@Nullable Mask solidMask) {
      if (solidMask == null) {
         this.solidMask = new SolidBlockMask(this.world);
      } else {
         this.solidMask = solidMask;
      }
   }

   private void setValues(Vector3 loc, double rotationX, double rotationY, int maxDistance, double viewHeight, double checkDistance) {
      this.maxDistance = maxDistance;
      this.checkDistance = checkDistance;
      this.curDistance = 0.0;
      rotationX = (rotationX + 90.0) % 360.0;
      rotationY *= -1.0;
      double h = checkDistance * Math.cos(Math.toRadians(rotationY));
      this.offset = Vector3.at(
         h * Math.cos(Math.toRadians(rotationX)), checkDistance * Math.sin(Math.toRadians(rotationY)), h * Math.sin(Math.toRadians(rotationX))
      );
      this.targetPosDouble = loc.add(0.0, viewHeight, 0.0);
      this.targetPos = this.targetPosDouble.toBlockPoint();
      this.prevPos = this.targetPos;
   }

   public Location getAnyTargetBlock() {
      Location lastBlock = null;

      while (this.getNextBlock() != null && !this.stopMask.test(this.targetPos)) {
         lastBlock = this.getCurrentBlock();
         if (lastBlock.getBlockY() < this.world.getMinY() || lastBlock.getBlockY() > this.world.getMaxY()) {
            return null;
         }
      }

      Location currentBlock = this.getCurrentBlock();
      return currentBlock != null ? currentBlock : lastBlock;
   }

   public Location getTargetBlock() {
      while (this.getNextBlock() != null && !this.stopMask.test(this.targetPos)) {
      }

      return this.getCurrentBlock();
   }

   public Location getSolidTargetBlock() {
      while (this.getNextBlock() != null && !this.solidMask.test(this.targetPos)) {
      }

      return this.getCurrentBlock();
   }

   public Location getNextBlock() {
      this.prevPos = this.targetPos;

      do {
         this.curDistance = this.curDistance + this.checkDistance;
         this.targetPosDouble = this.offset.add(this.targetPosDouble.x(), this.targetPosDouble.y(), this.targetPosDouble.z());
         this.targetPos = this.targetPosDouble.toBlockPoint();
      } while (
         this.curDistance <= this.maxDistance
            && this.targetPos.x() == this.prevPos.x()
            && this.targetPos.y() == this.prevPos.y()
            && this.targetPos.z() == this.prevPos.z()
      );

      return this.curDistance > this.maxDistance ? null : new Location(this.world, this.targetPos.toVector3());
   }

   public Location getCurrentBlock() {
      return this.curDistance > this.maxDistance ? null : new Location(this.world, this.targetPos.toVector3());
   }

   public Location getPreviousBlock() {
      return new Location(this.world, this.prevPos.toVector3());
   }

   public Location getAnyTargetBlockFace() {
      this.getAnyTargetBlock();
      Location current = this.getCurrentBlock();
      return current != null
         ? current.setDirection(current.toVector().subtract(this.getPreviousBlock().toVector()))
         : new Location(this.world, this.targetPos.toVector3(), Float.NaN, Float.NaN);
   }

   public Location getTargetBlockFace() {
      this.getTargetBlock();
      return this.getCurrentBlock() == null
         ? null
         : this.getCurrentBlock().setDirection(this.getCurrentBlock().toVector().subtract(this.getPreviousBlock().toVector()));
   }
}
