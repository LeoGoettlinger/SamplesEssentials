package com.sk89q.worldedit.bukkit.adapter.ext.fawe.v1_21_4;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.internal.wna.WorldNativeAccess;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import java.lang.ref.WeakReference;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.enginehub.linbus.tree.LinCompoundTag;

public class PaperweightWorldNativeAccess implements WorldNativeAccess<LevelChunk, BlockState, BlockPos> {
   private static final int UPDATE = 1;
   private static final int NOTIFY = 2;
   private final PaperweightAdapter adapter;
   private final WeakReference<ServerLevel> world;
   private SideEffectSet sideEffectSet;

   public PaperweightWorldNativeAccess(PaperweightAdapter adapter, WeakReference<ServerLevel> world) {
      this.adapter = adapter;
      this.world = world;
   }

   private ServerLevel getWorld() {
      return Objects.requireNonNull(this.world.get(), "The reference to the world was lost");
   }

   @Override
   public void setCurrentSideEffectSet(SideEffectSet sideEffectSet) {
      this.sideEffectSet = sideEffectSet;
   }

   public LevelChunk getChunk(int x, int z) {
      return this.getWorld().getChunk(x, z);
   }

   public BlockState toNative(com.sk89q.worldedit.world.block.BlockState state) {
      int stateId = BlockStateIdAccess.getBlockStateId(state);
      return BlockStateIdAccess.isValidInternalId(stateId) ? Block.stateById(stateId) : ((CraftBlockData)BukkitAdapter.adapt(state)).getState();
   }

   public BlockState getBlockState(LevelChunk chunk, BlockPos position) {
      return chunk.getBlockState(position);
   }

   @Nullable
   public BlockState setBlockState(LevelChunk chunk, BlockPos position, BlockState state) {
      return chunk.setBlockState(position, state, false, this.sideEffectSet.shouldApply(SideEffect.UPDATE));
   }

   public BlockState getValidBlockForPosition(BlockState block, BlockPos position) {
      return Block.updateFromNeighbourShapes(block, this.getWorld(), position);
   }

   public BlockPos getPosition(int x, int y, int z) {
      return new BlockPos(x, y, z);
   }

   public void updateLightingForBlock(BlockPos position) {
      this.getWorld().getChunkSource().getLightEngine().checkBlock(position);
   }

   public boolean updateTileEntity(BlockPos position, LinCompoundTag tag) {
      BlockEntity tileEntity = this.getWorld().getBlockEntity(position);
      if (tileEntity == null) {
         return false;
      } else {
         Tag nativeTag = this.adapter.fromNativeLin(tag);
         PaperweightAdapter.readTagIntoTileEntity((CompoundTag)nativeTag, tileEntity);
         return true;
      }
   }

   public void notifyBlockUpdate(LevelChunk chunk, BlockPos position, BlockState oldState, BlockState newState) {
      if (chunk.getSections()[this.getWorld().getSectionIndex(position.getY())] != null) {
         this.getWorld().sendBlockUpdated(position, oldState, newState, 3);
      }
   }

   public boolean isChunkTicking(LevelChunk chunk) {
      return chunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING);
   }

   public void markBlockChanged(LevelChunk chunk, BlockPos position) {
      if (chunk.getSections()[this.getWorld().getSectionIndex(position.getY())] != null) {
         this.getWorld().getChunkSource().blockChanged(position);
      }
   }

   public void notifyNeighbors(BlockPos pos, BlockState oldState, BlockState newState) {
      ServerLevel world = this.getWorld();
      if (this.sideEffectSet.shouldApply(SideEffect.EVENTS)) {
         world.updateNeighborsAt(pos, oldState.getBlock());
      } else {
         Block block = oldState.getBlock();
         this.fireNeighborChanged(pos, world, block, pos.west());
         this.fireNeighborChanged(pos, world, block, pos.east());
         this.fireNeighborChanged(pos, world, block, pos.below());
         this.fireNeighborChanged(pos, world, block, pos.above());
         this.fireNeighborChanged(pos, world, block, pos.north());
         this.fireNeighborChanged(pos, world, block, pos.south());
      }

      if (newState.hasAnalogOutputSignal()) {
         world.updateNeighbourForOutputSignal(pos, newState.getBlock());
      }
   }

   public void updateBlock(BlockPos pos, BlockState oldState, BlockState newState) {
      ServerLevel world = this.getWorld();
      newState.onPlace(world, pos, oldState, false);
   }

   private void fireNeighborChanged(BlockPos pos, ServerLevel world, Block block, BlockPos neighborPos) {
      world.getBlockState(neighborPos).handleNeighborChanged(world, neighborPos, block, ExperimentalRedstoneUtils.initialOrientation(world, null, null), false);
   }

   public void updateNeighbors(BlockPos pos, BlockState oldState, BlockState newState, int recursionLimit) {
      ServerLevel world = this.getWorld();
      oldState.updateIndirectNeighbourShapes(world, pos, 2, recursionLimit);
      if (this.sideEffectSet.shouldApply(SideEffect.EVENTS)) {
         CraftWorld craftWorld = world.getWorld();
         BlockPhysicsEvent event = new BlockPhysicsEvent(craftWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), CraftBlockData.fromData(newState));
         world.getCraftServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            return;
         }
      }

      newState.updateNeighbourShapes(world, pos, 2, recursionLimit);
      newState.updateIndirectNeighbourShapes(world, pos, 2, recursionLimit);
   }

   public void onBlockStateChange(BlockPos pos, BlockState oldState, BlockState newState) {
      this.getWorld().onBlockStateChange(pos, oldState, newState);
   }

   @Override
   public void flush() {
   }
}
