package com.sk89q.worldedit.internal.wna;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import javax.annotation.Nullable;
import org.enginehub.linbus.tree.LinCompoundTag;

public interface WorldNativeAccess<NC, NBS, NP> {
   default <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException {
      Preconditions.checkNotNull(position);
      Preconditions.checkNotNull(block);
      this.setCurrentSideEffectSet(sideEffects);
      int x = position.x();
      int y = position.y();
      int z = position.z();
      NC chunk = this.getChunk(x >> 4, z >> 4);
      NP pos = this.getPosition(x, y, z);
      NBS old = this.getBlockState(chunk, pos);
      NBS newState = this.toNative(block.toImmutableState());
      if (sideEffects.shouldApply(SideEffect.VALIDATION)) {
         newState = this.getValidBlockForPosition(newState, pos);
      }

      NBS successState = this.setBlockState(chunk, pos, newState);
      boolean successful = successState != null;
      if ((successful || old == newState) && block instanceof BaseBlock baseBlock) {
         LinCompoundTag tag = baseBlock.getNbt();
         if (tag != null) {
            tag = tag.toBuilder()
               .putString("id", baseBlock.getNbtId())
               .putInt("x", position.getX())
               .putInt("y", position.getY())
               .putInt("z", position.getZ())
               .build();
            successful = this.updateTileEntity(pos, tag);
         }
      }

      if (successful) {
         if (sideEffects.getState(SideEffect.LIGHTING) == SideEffect.State.ON) {
            this.updateLightingForBlock(pos);
         }

         this.markAndNotifyBlock(pos, chunk, old, newState, sideEffects);
      }

      return successful;
   }

   default void applySideEffects(BlockVector3 position, BlockState previousType, SideEffectSet sideEffectSet) {
      this.setCurrentSideEffectSet(sideEffectSet);
      NP pos = this.getPosition(position.x(), position.y(), position.z());
      NC chunk = this.getChunk(position.x() >> 4, position.z() >> 4);
      NBS oldData = this.toNative(previousType);
      NBS newData = this.getBlockState(chunk, pos);
      if (sideEffectSet.getState(SideEffect.LIGHTING) == SideEffect.State.ON) {
         this.updateLightingForBlock(pos);
      }

      this.markAndNotifyBlock(pos, chunk, oldData, newData, sideEffectSet);
   }

   default void setCurrentSideEffectSet(SideEffectSet sideEffectSet) {
   }

   NC getChunk(int x, int z);

   NBS toNative(BlockState state);

   NBS getBlockState(NC chunk, NP position);

   @Nullable
   NBS setBlockState(NC chunk, NP position, NBS state);

   NBS getValidBlockForPosition(NBS block, NP position);

   NP getPosition(int x, int y, int z);

   void updateLightingForBlock(NP position);

   boolean updateTileEntity(NP position, LinCompoundTag tag);

   void notifyBlockUpdate(NC chunk, NP position, NBS oldState, NBS newState);

   boolean isChunkTicking(NC chunk);

   void markBlockChanged(NC chunk, NP position);

   void notifyNeighbors(NP pos, NBS oldState, NBS newState);

   void updateBlock(NP pos, NBS oldState, NBS newState);

   void updateNeighbors(NP pos, NBS oldState, NBS newState, int recursionLimit);

   void onBlockStateChange(NP pos, NBS oldState, NBS newState);

   default void markAndNotifyBlock(NP pos, NC chunk, NBS oldState, NBS newState, SideEffectSet sideEffectSet) {
      NBS blockState1 = this.getBlockState(chunk, pos);
      if (blockState1 == newState) {
         if (this.isChunkTicking(chunk)) {
            if (sideEffectSet.shouldApply(SideEffect.ENTITY_AI)) {
               this.notifyBlockUpdate(chunk, pos, oldState, newState);
            } else if (sideEffectSet.shouldApply(SideEffect.NETWORK)) {
               this.markBlockChanged(chunk, pos);
            }
         }

         if (sideEffectSet.shouldApply(SideEffect.NEIGHBORS)) {
            this.notifyNeighbors(pos, oldState, newState);
         }

         if (sideEffectSet.shouldApply(SideEffect.NEIGHBORS)) {
            this.updateNeighbors(pos, oldState, newState, 512);
         }

         if (sideEffectSet.shouldApply(SideEffect.POI_UPDATE)) {
            this.onBlockStateChange(pos, oldState, blockState1);
         }
      }
   }

   void flush();
}
