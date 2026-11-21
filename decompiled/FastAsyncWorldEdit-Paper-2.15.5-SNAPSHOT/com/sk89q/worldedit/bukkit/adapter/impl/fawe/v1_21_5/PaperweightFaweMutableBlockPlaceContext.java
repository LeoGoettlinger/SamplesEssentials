package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v1_21_5;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PaperweightFaweMutableBlockPlaceContext extends BlockPlaceContext {
   private static final BlockHitResult DEFAULT_BLOCK_HIT = new BlockHitResult(
      new Vec3(2.147483647E9, 2.147483647E9, 2.147483647E9), Direction.NORTH, new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE), false
   );
   private final ServerLevel level;
   private BlockHitResult hitResult = null;
   private Direction direction = null;
   private BlockPos relativePos;

   public PaperweightFaweMutableBlockPlaceContext(ServerLevel level) {
      super(level, null, null, null, DEFAULT_BLOCK_HIT);
      this.level = level;
      this.replaceClicked = false;
   }

   public PaperweightFaweMutableBlockPlaceContext withSetting(BlockHitResult hitResult, Direction direction) {
      this.hitResult = hitResult;
      this.direction = direction;
      this.relativePos = hitResult.getBlockPos().relative(hitResult.getDirection());
      return this;
   }

   @Nonnull
   public BlockPos getClickedPos() {
      return this.relativePos;
   }

   @Nonnull
   public Direction getClickedFace() {
      return this.hitResult.getDirection();
   }

   @Nonnull
   public Vec3 getClickLocation() {
      return this.hitResult.getLocation();
   }

   public boolean isInside() {
      return this.hitResult.isInside();
   }

   public ItemStack getItemInHand() {
      return ItemStack.EMPTY;
   }

   @Nullable
   public Player getPlayer() {
      return null;
   }

   public InteractionHand getHand() {
      return null;
   }

   @Nonnull
   public Level getLevel() {
      return this.level;
   }

   @Nonnull
   public Direction getHorizontalDirection() {
      return this.direction.getAxis() == Axis.Y ? Direction.NORTH : this.direction;
   }

   public boolean isSecondaryUseActive() {
      return false;
   }

   public float getRotation() {
      return this.direction.get2DDataValue() * 90;
   }

   public boolean canPlace() {
      return this.getLevel().getBlockState(this.getClickedPos()).canBeReplaced(this);
   }

   public boolean replacingClickedOnBlock() {
      return false;
   }

   @Nonnull
   public Direction getNearestLookingDirection() {
      return this.direction;
   }

   @Nonnull
   public Direction getNearestLookingVerticalDirection() {
      return this.direction.getAxis() == Axis.Y ? Direction.UP : Direction.DOWN;
   }

   @Nonnull
   public Direction[] getNearestLookingDirections() {
      return new Direction[]{this.direction};
   }
}
