package com.sk89q.worldedit.bukkit.adapter.impl.fawe.v1_21_9;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.math.IntPair;
import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.task.RunnableVal;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.internal.wna.WorldNativeAccess;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.storage.ValueInput;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.enginehub.linbus.tree.LinCompoundTag;

public class PaperweightFaweWorldNativeAccess implements WorldNativeAccess<LevelChunk, BlockState, BlockPos> {
   private static final int UPDATE = 1;
   private static final int NOTIFY = 2;
   private static final Direction[] NEIGHBOUR_ORDER = new Direction[]{
      Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH
   };
   private final PaperweightFaweAdapter paperweightFaweAdapter;
   private final WeakReference<Level> level;
   private final AtomicInteger lastTick;
   private final Set<PaperweightFaweWorldNativeAccess.CachedChange> cachedChanges = new HashSet<>();
   private final Set<IntPair> cachedChunksToSend = new HashSet<>();
   private SideEffectSet sideEffectSet;

   public PaperweightFaweWorldNativeAccess(PaperweightFaweAdapter paperweightFaweAdapter, WeakReference<Level> level) {
      this.paperweightFaweAdapter = paperweightFaweAdapter;
      this.level = level;
      this.lastTick = new AtomicInteger(MinecraftServer.currentTick);
   }

   private Level getLevel() {
      return Objects.requireNonNull(this.level.get(), "The reference to the world was lost");
   }

   @Override
   public void setCurrentSideEffectSet(SideEffectSet sideEffectSet) {
      this.sideEffectSet = sideEffectSet;
   }

   public LevelChunk getChunk(int x, int z) {
      return this.getLevel().getChunk(x, z);
   }

   public BlockState toNative(com.sk89q.worldedit.world.block.BlockState blockState) {
      int stateId = this.paperweightFaweAdapter.ordinalToIbdID(blockState.getOrdinalChar());
      return BlockStateIdAccess.isValidInternalId(stateId) ? Block.stateById(stateId) : ((CraftBlockData)BukkitAdapter.adapt(blockState)).getState();
   }

   public BlockState getBlockState(LevelChunk levelChunk, BlockPos blockPos) {
      return levelChunk.getBlockState(blockPos);
   }

   @Nullable
   public synchronized BlockState setBlockState(LevelChunk levelChunk, BlockPos blockPos, BlockState blockState) {
      int currentTick = MinecraftServer.currentTick;
      if (Fawe.isMainThread()) {
         return levelChunk.setBlockState(blockPos, blockState, this.sideEffectSet.shouldApply(SideEffect.UPDATE) ? 0 : 512);
      } else {
         this.cachedChanges.add(new PaperweightFaweWorldNativeAccess.CachedChange(levelChunk, blockPos, blockState));
         this.cachedChunksToSend.add(new IntPair(levelChunk.locX, levelChunk.locZ));
         boolean nextTick = this.lastTick.get() > currentTick;
         if (nextTick || this.cachedChanges.size() >= 1024) {
            if (nextTick) {
               this.lastTick.set(currentTick);
            }

            this.flushAsync(nextTick);
         }

         return blockState;
      }
   }

   public BlockState getValidBlockForPosition(BlockState blockState, BlockPos blockPos) {
      return Block.updateFromNeighbourShapes(blockState, this.getLevel(), blockPos);
   }

   public BlockPos getPosition(int x, int y, int z) {
      return new BlockPos(x, y, z);
   }

   public void updateLightingForBlock(BlockPos blockPos) {
      this.getLevel().getChunkSource().getLightEngine().checkBlock(blockPos);
   }

   public boolean updateTileEntity(BlockPos blockPos, LinCompoundTag tag) {
      BlockEntity blockEntity = this.getLevel().getBlockEntity(blockPos);
      if (blockEntity == null) {
         return false;
      } else {
         Tag nativeTag = this.paperweightFaweAdapter.fromNativeLin(tag);
         ValueInput input = PaperweightPlatformAdapter.createInput((CompoundTag)nativeTag);
         blockEntity.loadWithComponents(input);
         return true;
      }
   }

   public void notifyBlockUpdate(LevelChunk levelChunk, BlockPos blockPos, BlockState oldState, BlockState newState) {
      if (levelChunk.getSections()[this.level.get().getSectionIndex(blockPos.getY())] != null) {
         this.getLevel().sendBlockUpdated(blockPos, oldState, newState, 3);
      }
   }

   public boolean isChunkTicking(LevelChunk levelChunk) {
      return levelChunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING);
   }

   public void markBlockChanged(LevelChunk levelChunk, BlockPos blockPos) {
      if (levelChunk.getSections()[this.level.get().getSectionIndex(blockPos.getY())] != null) {
         ((ServerChunkCache)this.getLevel().getChunkSource()).blockChanged(blockPos);
      }
   }

   public void notifyNeighbors(BlockPos blockPos, BlockState oldState, BlockState newState) {
      Level level = this.getLevel();
      if (this.sideEffectSet.shouldApply(SideEffect.EVENTS)) {
         level.updateNeighborsAt(blockPos, oldState.getBlock());
      } else {
         for (Direction direction : NEIGHBOUR_ORDER) {
            BlockPos shifted = blockPos.relative(direction);
            level.getBlockState(shifted)
               .handleNeighborChanged(level, shifted, oldState.getBlock(), ExperimentalRedstoneUtils.initialOrientation(level, null, null), false);
         }
      }

      if (newState.hasAnalogOutputSignal()) {
         level.updateNeighbourForOutputSignal(blockPos, newState.getBlock());
      }
   }

   public void updateNeighbors(BlockPos blockPos, BlockState oldState, BlockState newState, int recursionLimit) {
      Level level = this.getLevel();
      oldState.updateIndirectNeighbourShapes(level, blockPos, 2, recursionLimit);
      if (this.sideEffectSet.shouldApply(SideEffect.EVENTS)) {
         CraftWorld craftWorld = level.getWorld();
         if (craftWorld != null) {
            BlockPhysicsEvent event = new BlockPhysicsEvent(
               craftWorld.getBlockAt(blockPos.getX(), blockPos.getY(), blockPos.getZ()), CraftBlockData.fromData(newState)
            );
            level.getCraftServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               return;
            }
         }
      }

      newState.triggerEvent(level, blockPos, 2, recursionLimit);
      newState.updateIndirectNeighbourShapes(level, blockPos, 2, recursionLimit);
   }

   public void updateBlock(BlockPos pos, BlockState oldState, BlockState newState) {
      Level world = this.getLevel();
      newState.onPlace(world, pos, oldState, false);
   }

   public void onBlockStateChange(BlockPos blockPos, BlockState oldState, BlockState newState) {
      this.getLevel().updatePOIOnBlockStateChange(blockPos, oldState, newState);
   }

   private synchronized void flushAsync(final boolean sendChunks) {
      final Set<PaperweightFaweWorldNativeAccess.CachedChange> changes = Set.copyOf(this.cachedChanges);
      this.cachedChanges.clear();
      final Set<IntPair> toSend;
      if (sendChunks) {
         toSend = Set.copyOf(this.cachedChunksToSend);
         this.cachedChunksToSend.clear();
      } else {
         toSend = Collections.emptySet();
      }

      RunnableVal<Object> runnableVal = new RunnableVal<Object>() {
         @Override
         public void run(Object value) {
            changes.forEach(
               cc -> cc.levelChunk
                  .setBlockState(cc.blockPos, cc.blockState, PaperweightFaweWorldNativeAccess.this.sideEffectSet.shouldApply(SideEffect.UPDATE) ? 0 : 512)
            );
            if (sendChunks) {
               for (IntPair chunk : toSend) {
                  PaperweightPlatformAdapter.sendChunk(chunk, PaperweightFaweWorldNativeAccess.this.getLevel().getWorld().getHandle(), chunk.x(), chunk.z());
               }
            }
         }
      };
      TaskManager.taskManager().async(() -> TaskManager.taskManager().sync(runnableVal));
   }

   @Override
   public synchronized void flush() {
      RunnableVal<Object> runnableVal = new RunnableVal<Object>() {
         @Override
         public void run(Object value) {
            PaperweightFaweWorldNativeAccess.this.cachedChanges
               .forEach(
                  cc -> cc.levelChunk
                     .setBlockState(cc.blockPos, cc.blockState, PaperweightFaweWorldNativeAccess.this.sideEffectSet.shouldApply(SideEffect.UPDATE) ? 0 : 512)
               );

            for (IntPair chunk : PaperweightFaweWorldNativeAccess.this.cachedChunksToSend) {
               PaperweightPlatformAdapter.sendChunk(chunk, PaperweightFaweWorldNativeAccess.this.getLevel().getWorld().getHandle(), chunk.x(), chunk.z());
            }
         }
      };
      if (Fawe.isMainThread()) {
         runnableVal.run();
      } else {
         TaskManager.taskManager().sync(runnableVal);
      }

      this.cachedChanges.clear();
      this.cachedChunksToSend.clear();
   }

   private record CachedChange(LevelChunk levelChunk, BlockPos blockPos, BlockState blockState) {
   }
}
