package com.sk89q.worldedit.world;

import com.fastasyncworldedit.core.function.mask.BlockMaskBuilder;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import java.nio.file.Path;
import java.util.PriorityQueue;
import javax.annotation.Nullable;

public abstract class AbstractWorld implements World {
   private final PriorityQueue<AbstractWorld.QueuedEffect> effectQueue = new PriorityQueue<>();
   private int taskId = -1;

   @Override
   public boolean useItem(BlockVector3 position, BaseItem item, Direction face) {
      return false;
   }

   @Override
   public final <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 pt, B block) throws WorldEditException {
      return this.setBlock(pt, block, SideEffectSet.defaults());
   }

   @Override
   public Path getStoragePath() {
      return null;
   }

   @Override
   public int getMinY() {
      return WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMinY();
   }

   @Override
   public int getMaxY() {
      return WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMaxY();
   }

   @Override
   public Mask createLiquidMask() {
      return new BlockMaskBuilder().addTypes(BlockTypes.LAVA, BlockTypes.WATER).build(this);
   }

   @Override
   public void dropItem(Vector3 pt, BaseItemStack item, int times) {
      for (int i = 0; i < times; i++) {
         this.dropItem(pt, item);
      }
   }

   @Override
   public void checkLoadedChunk(BlockVector3 pt) {
   }

   @Override
   public void fixAfterFastMode(Iterable<BlockVector2> chunks) {
   }

   @Override
   public void fixLighting(Iterable<BlockVector2> chunks) {
   }

   @Override
   public boolean playEffect(Vector3 position, int type, int data) {
      return false;
   }

   @Override
   public boolean playBlockBreakEffect(Vector3 position, BlockType type) {
      return false;
   }

   @Override
   public boolean queueBlockBreakEffect(Platform server, BlockVector3 position, BlockType blockType, double priority) {
      if (this.taskId == -1) {
         this.taskId = server.schedule(0L, 1L, () -> {
            synchronized (this.effectQueue) {
               int max = Math.clamp((long)(this.effectQueue.size() / 3), 1, 30);

               for (int i = 0; i < max; i++) {
                  if (this.effectQueue.isEmpty()) {
                     return;
                  }

                  this.effectQueue.poll().play();
               }
            }
         });
      }

      if (this.taskId == -1) {
         return false;
      } else {
         synchronized (this.effectQueue) {
            this.effectQueue.offer(new AbstractWorld.QueuedEffect(position.toVector3(), blockType, priority));
            return true;
         }
      }
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      return BlockVector3.at(-30000000, this.getMinY(), -30000000);
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return BlockVector3.at(30000000, this.getMaxY(), 30000000);
   }

   @Nullable
   @Override
   public Operation commit() {
      return null;
   }

   @Override
   public WeatherType getWeather() {
      return WeatherTypes.CLEAR;
   }

   @Override
   public long getRemainingWeatherDuration() {
      return 0L;
   }

   @Override
   public void setWeather(WeatherType weatherType) {
   }

   @Override
   public void setWeather(WeatherType weatherType, long duration) {
   }

   private class QueuedEffect implements Comparable<AbstractWorld.QueuedEffect> {
      private final Vector3 position;
      private final BlockType blockType;
      private final double priority;

      private QueuedEffect(Vector3 position, BlockType blockType, double priority) {
         this.position = position;
         this.blockType = blockType;
         this.priority = priority;
      }

      public void play() {
         AbstractWorld.this.playBlockBreakEffect(this.position, this.blockType);
      }

      public int compareTo(@Nullable AbstractWorld.QueuedEffect other) {
         return Double.compare(this.priority, other != null ? other.priority : 0.0);
      }
   }
}
