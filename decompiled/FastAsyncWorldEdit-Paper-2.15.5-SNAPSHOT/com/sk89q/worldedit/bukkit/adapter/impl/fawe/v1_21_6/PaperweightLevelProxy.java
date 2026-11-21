package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v1_21_6;

import com.fastasyncworldedit.core.util.ReflectionUtils;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import org.enginehub.linbus.tree.LinCompoundTag;
import sun.misc.Unsafe;

public class PaperweightLevelProxy extends ServerLevel {
   protected ServerLevel serverLevel;
   private PaperweightPlacementStateProcessor processor;
   private PaperweightFaweAdapter adapter;

   private PaperweightLevelProxy() {
      super(null, null, null, null, null, null, null, true, 0L, null, true, null, null, null, null);
      throw new IllegalStateException("Cannot be instantiated");
   }

   public static PaperweightLevelProxy getInstance(ServerLevel serverLevel, PaperweightPlacementStateProcessor processor) {
      Unsafe unsafe = ReflectionUtils.getUnsafe();

      PaperweightLevelProxy newLevel;
      try {
         newLevel = (PaperweightLevelProxy)unsafe.allocateInstance(PaperweightLevelProxy.class);
      } catch (InstantiationException var5) {
         throw new RuntimeException(var5);
      }

      newLevel.processor = processor;
      newLevel.adapter = (PaperweightFaweAdapter)WorldEditPlugin.getInstance().getBukkitImplAdapter();
      newLevel.serverLevel = serverLevel;
      return newLevel;
   }

   @Nullable
   public BlockEntity getBlockEntity(@Nonnull BlockPos blockPos) {
      if (blockPos.getX() == Integer.MAX_VALUE) {
         return null;
      } else {
         LinCompoundTag tag = this.processor.getTileAt(blockPos.getX(), blockPos.getY(), blockPos.getZ());
         if (tag == null) {
            return null;
         } else {
            BlockState state = this.adapter.adapt(this.processor.getBlockStateAt(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            if (state.getBlock() instanceof EntityBlock entityBlock) {
               BlockEntity var7 = entityBlock.newBlockEntity(blockPos, state);
               ValueInput input = PaperweightPlatformAdapter.createInput((CompoundTag)this.adapter.fromNativeLin(tag));
               var7.loadWithComponents(input);
               return var7;
            } else {
               return null;
            }
         }
      }
   }

   @Nonnull
   public BlockState getBlockState(@Nonnull BlockPos blockPos) {
      if (blockPos.getX() == Integer.MAX_VALUE) {
         return Blocks.AIR.defaultBlockState();
      } else {
         com.sk89q.worldedit.world.block.BlockState state = this.processor.getBlockStateAt(blockPos.getX(), blockPos.getY(), blockPos.getZ());
         return this.adapter.adapt(state);
      }
   }

   @Nonnull
   public FluidState getFluidState(@Nonnull BlockPos pos) {
      return pos.getX() == Integer.MAX_VALUE ? Fluids.EMPTY.defaultFluidState() : this.getBlockState(pos).getFluidState();
   }

   public boolean isWaterAt(@Nonnull BlockPos pos) {
      return pos.getX() == Integer.MAX_VALUE ? false : this.getBlockState(pos).getFluidState().is(FluidTags.WATER);
   }

   public int getHeight() {
      return this.serverLevel.getHeight();
   }

   public int getMinY() {
      return this.serverLevel.getMinY();
   }

   public int getMaxY() {
      return this.serverLevel.getMaxY();
   }

   public boolean isInsideBuildHeight(int blockY) {
      return this.serverLevel.isInsideBuildHeight(blockY);
   }

   public boolean isOutsideBuildHeight(BlockPos pos) {
      return this.serverLevel.isOutsideBuildHeight(pos);
   }

   public boolean isOutsideBuildHeight(int blockY) {
      return this.serverLevel.isOutsideBuildHeight(blockY);
   }

   public WorldBorder getWorldBorder() {
      return this.serverLevel.getWorldBorder();
   }
}
