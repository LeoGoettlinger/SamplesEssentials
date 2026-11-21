package com.fastasyncworldedit.core.extent;

import com.fastasyncworldedit.core.extent.processor.ProcessorScope;
import com.fastasyncworldedit.core.limit.PropertyRemap;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.factory.parser.DefaultBlockParser;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import com.sk89q.worldedit.world.block.FuzzyBlockState;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class DisallowedBlocksExtent extends AbstractDelegateExtent implements IBatchProcessor {
   private static final BlockState RESERVED = BlockTypes.__RESERVED__.getDefaultState();
   private final Set<PropertyRemap<?>> remaps;
   private Set<FuzzyBlockState> blockedStates = null;
   private Set<String> blockedBlocks = null;

   public DisallowedBlocksExtent(Extent extent, Set<String> blockedBlocks, Set<PropertyRemap<?>> remaps) {
      super(extent);
      this.remaps = remaps;
      if (blockedBlocks != null && !blockedBlocks.isEmpty()) {
         blockedBlocks = blockedBlocks.stream()
            .map(s -> s.contains(":") ? s.toLowerCase(Locale.ROOT) : ("minecraft:" + s).toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());
         this.blockedBlocks = new HashSet<>();

         for (String block : blockedBlocks) {
            if (block.indexOf(91) != -1 && block.indexOf(93) != -1) {
               String[] properties = block.substring(block.indexOf(91) + 1, block.indexOf(93)).split(",");
               if (properties.length != 0) {
                  BlockType type = BlockTypes.get(block.substring(0, block.indexOf(91)));
                  Map<Property<?>, Object> values = DefaultBlockParser.parseProperties(type, properties, null, true);
                  if (values != null && !values.isEmpty()) {
                     if (this.blockedStates == null) {
                        this.blockedStates = new HashSet<>();
                     }

                     this.blockedStates.add(new FuzzyBlockState(type.getDefaultState(), values));
                  }
               }
            } else {
               this.blockedBlocks.add(block);
            }
         }
      }
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
      if (!(block instanceof BaseBlock) && !(block instanceof BlockState)) {
         return super.setBlock(location, block);
      } else {
         B newBlock = this.checkBlock(block);
         return newBlock.getBlockType() == BlockTypes.__RESERVED__ ? false : super.setBlock(location, newBlock);
      }
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int x, int y, int z, B block) throws WorldEditException {
      if (!(block instanceof BaseBlock) && !(block instanceof BlockState)) {
         return super.setBlock(x, y, z, block);
      } else {
         B newBlock = this.checkBlock(block);
         return newBlock.getBlockType() == BlockTypes.__RESERVED__ ? false : super.setBlock(x, y, z, newBlock);
      }
   }

   private <B extends BlockStateHolder<B>> B checkBlock(B block) {
      if (this.blockedBlocks != null && this.blockedBlocks.contains(block.getBlockType().id())) {
         return (B)(block instanceof BlockState ? RESERVED : RESERVED.toBaseBlock());
      } else if (this.blockedStates == null) {
         return block;
      } else {
         for (FuzzyBlockState state : this.blockedStates) {
            if (state.equalsFuzzy(block)) {
               return (B)(block instanceof BlockState ? RESERVED : RESERVED.toBaseBlock());
            }
         }

         if (this.remaps != null && !this.remaps.isEmpty()) {
            for (PropertyRemap<?> remap : this.remaps) {
               block = remap.apply(block);
            }

            return block;
         } else {
            return block;
         }
      }
   }

   @Override
   public IChunkSet processSet(final IChunk chunk, final IChunkGet get, final IChunkSet set) {
      if (this.blockedStates == null && this.blockedBlocks == null) {
         return set;
      } else {
         for (int layer = set.getMinSectionPosition(); layer <= set.getMaxSectionPosition(); layer++) {
            if (set.hasSection(layer)) {
               char[] blocks = Objects.requireNonNull(set.loadIfPresent(layer));

               label70:
               for (int i = 0; i < blocks.length; i++) {
                  char block = blocks[i];
                  if (block != 0) {
                     BlockState state = BlockTypesCache.states[block];
                     if (this.blockedBlocks != null && this.blockedBlocks.contains(state.getBlockType().id())) {
                        blocks[i] = 0;
                     } else if (this.blockedStates != null) {
                        for (FuzzyBlockState fuzzy : this.blockedStates) {
                           if (fuzzy.equalsFuzzy(state)) {
                              blocks[i] = 0;
                              continue label70;
                           }
                        }

                        if (this.remaps != null && !this.remaps.isEmpty()) {
                           for (PropertyRemap<?> remap : this.remaps) {
                              state = remap.apply(state);
                           }

                           blocks[i] = state.getOrdinalChar();
                        } else {
                           blocks[i] = block;
                        }
                     }
                  }
               }
            }
         }

         return set;
      }
   }

   @Nullable
   @Override
   public Extent construct(final Extent child) {
      if (this.getExtent() != child) {
         new ExtentTraverser<>(this).setNext(child);
      }

      return this;
   }

   @Override
   public ProcessorScope getScope() {
      return ProcessorScope.REMOVING_BLOCKS;
   }
}
