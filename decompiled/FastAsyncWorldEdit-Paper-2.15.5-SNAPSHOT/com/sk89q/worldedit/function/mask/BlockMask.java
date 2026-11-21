package com.sk89q.worldedit.function.mask;

import com.fastasyncworldedit.core.function.mask.ABlockMask;
import com.fastasyncworldedit.core.function.mask.SingleBlockStateMask;
import com.fastasyncworldedit.core.function.mask.SingleBlockTypeMask;
import com.fastasyncworldedit.core.world.block.BlanketBaseBlock;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;

public class BlockMask extends ABlockMask {
   private final boolean[] ordinals;

   public BlockMask() {
      this(new NullExtent());
   }

   public BlockMask(Extent extent) {
      this(extent, new boolean[BlockTypesCache.states.length]);
   }

   public BlockMask(Extent extent, boolean[] ordinals) {
      super((Extent)(extent == null ? new NullExtent() : extent));
      this.ordinals = ordinals;
   }

   @Deprecated
   public BlockMask(Extent extent, Collection<BaseBlock> blocks) {
      this(extent);
      Preconditions.checkNotNull(blocks);
      this.add(blocks);
   }

   public BlockMask(Extent extent, BaseBlock... block) {
      this(extent, Arrays.asList((BaseBlock[])Preconditions.checkNotNull(block)));
   }

   public BlockMask add(Predicate<BlockState> predicate) {
      for (int i = 0; i < this.ordinals.length; i++) {
         if (!this.ordinals[i]) {
            BlockState state = BlockTypesCache.states[i];
            if (state != null) {
               this.ordinals[i] = predicate.test(state);
            }
         }
      }

      return this;
   }

   public BlockMask add(BlockState... states) {
      this.addStates(Arrays.asList(states));
      return this;
   }

   public BlockMask remove(BlockState... states) {
      for (BlockState state : states) {
         this.ordinals[state.getOrdinal()] = false;
      }

      return this;
   }

   public BlockMask clear() {
      Arrays.fill(this.ordinals, false);
      return this;
   }

   public boolean isEmpty() {
      for (boolean value : this.ordinals) {
         if (value) {
            return false;
         }
      }

      return true;
   }

   private BlockMask addStates(Collection<BlockState> states) {
      for (BlockState state : states) {
         this.ordinals[state.getOrdinal()] = true;
      }

      return this;
   }

   public BlockMask add(BlockType... types) {
      this.addTypes(Arrays.asList(types));
      return this;
   }

   private BlockMask addTypes(Collection<BlockType> types) {
      for (BlockType type : types) {
         for (BlockState state : type.getAllStates()) {
            this.ordinals[state.getOrdinal()] = true;
         }
      }

      return this;
   }

   @Deprecated
   public void add(Collection<BaseBlock> blocks) {
      Preconditions.checkNotNull(blocks);

      for (BaseBlock block : blocks) {
         if (block instanceof BlanketBaseBlock) {
            for (BlockState state : block.getBlockType().getAllStates()) {
               this.ordinals[state.getOrdinal()] = true;
            }
         } else {
            this.add(block.toBlockState());
         }
      }
   }

   public void add(BaseBlock... block) {
      this.add(Arrays.asList((BaseBlock[])Preconditions.checkNotNull(block)));
   }

   public Collection<BaseBlock> getBlocks() {
      return Collections.emptyList();
   }

   @Override
   public boolean test(BlockState state) {
      return this.ordinals[state.getOrdinal()] || this.replacesAir() && state.getOrdinal() == 0;
   }

   @Override
   public boolean test(BlockVector3 vector) {
      int test = vector.getBlock(this.getExtent()).getOrdinal();
      return this.ordinals[test] || this.replacesAir() && test == 0;
   }

   @Override
   public boolean replacesAir() {
      return this.ordinals[1] || this.ordinals[2] || this.ordinals[3];
   }

   @Override
   public Mask tryCombine(Mask mask) {
      if (mask instanceof ABlockMask other) {
         boolean modified = false;
         boolean hasAny = false;

         for (int i = 0; i < this.ordinals.length; i++) {
            if (this.ordinals[i]) {
               boolean result = other.test(BlockState.getFromOrdinal(i));
               hasAny |= result;
               modified |= !result;
               this.ordinals[i] = result;
            }
         }

         if (modified) {
            if (!hasAny) {
               return Masks.alwaysFalse();
            }

            return this;
         }
      }

      return null;
   }

   @Override
   public Mask tryOr(Mask mask) {
      if (mask instanceof ABlockMask other) {
         boolean modified = false;

         for (int i = 0; i < this.ordinals.length; i++) {
            if (!this.ordinals[i]) {
               boolean result = other.test(BlockState.getFromOrdinal(i));
               modified |= result;
               this.ordinals[i] = result;
            }
         }

         if (modified) {
            return this;
         }
      }

      return null;
   }

   @Nullable
   @Override
   public Mask2D toMask2D() {
      return null;
   }

   @Override
   public Mask tryOptimize() {
      int setStates = 0;
      BlockState setState = null;
      BlockState unsetState = null;
      int totalStates = 0;
      int setTypes = 0;
      BlockType setType = null;
      BlockType unsetType = null;
      int totalTypes = 0;

      for (BlockType type : BlockTypesCache.values) {
         if (type != null) {
            totalTypes++;
            boolean hasAll = true;
            boolean hasAny = false;
            List<BlockState> all = type.getAllStates();

            for (BlockState state : all) {
               totalStates++;
               boolean result = this.test(state);
               hasAll &= result;
               hasAny |= result;
            }

            if (hasAll) {
               setTypes++;
               setType = type;
               setStates += all.size();
               setState = type.getDefaultState();
            } else if (hasAny) {
               for (BlockState state : all) {
                  if (this.test(state)) {
                     setStates++;
                     setState = state;
                  } else {
                     unsetState = state;
                  }
               }
            } else if (all.size() == 1) {
               unsetState = all.get(0);
            } else {
               unsetType = type;
            }
         }
      }

      if (setStates == 0) {
         return Masks.alwaysFalse();
      } else if (setStates == totalStates) {
         return Masks.alwaysTrue();
      } else if (setStates == 1) {
         return new SingleBlockStateMask(this.getExtent(), setState);
      } else if (setStates == totalStates - 1) {
         if (unsetState != null) {
            return new InverseSingleBlockStateMask(this.getExtent(), unsetState);
         } else {
            throw new IllegalArgumentException("unsetState cannot be null when passed to InverseSingleBlockStateMask");
         }
      } else if (setTypes == 1) {
         return new SingleBlockTypeMask(this.getExtent(), setType);
      } else if (setTypes != totalTypes - 1) {
         return null;
      } else if (unsetType != null) {
         return new InverseSingleBlockTypeMask(this.getExtent(), unsetType);
      } else {
         throw new IllegalArgumentException("unsetType cannot be null when passed to InverseSingleBlockTypeMask");
      }
   }

   @Override
   public Mask inverse() {
      boolean[] cloned = new boolean[this.ordinals.length];

      for (int i = 0; i < cloned.length; i++) {
         cloned[i] = !this.ordinals[i];
      }

      if (this.replacesAir()) {
         cloned[0] = false;
         cloned[1] = false;
         cloned[2] = false;
         cloned[3] = false;
      }

      return new BlockMask(this.getExtent(), cloned);
   }

   @Override
   public Mask copy() {
      return new BlockMask(this.getExtent(), (boolean[])this.ordinals.clone());
   }
}
