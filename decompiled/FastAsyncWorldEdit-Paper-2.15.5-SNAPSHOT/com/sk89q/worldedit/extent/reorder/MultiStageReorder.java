package com.sk89q.worldedit.extent.reorder;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractBufferingExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.OperationQueue;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.function.operation.SetBlockMap;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class MultiStageReorder extends AbstractBufferingExtent implements ReorderingExtent {
   private static final Map<BlockType, MultiStageReorder.PlacementPriority> priorityMap = new HashMap<>();
   private final Map<MultiStageReorder.PlacementPriority, BlockMap<BaseBlock>> stages = new HashMap<>();
   private boolean enabled;

   public MultiStageReorder(Extent extent) {
      this(extent, true);
   }

   public MultiStageReorder(Extent extent, boolean enabled) {
      super(extent);
      this.enabled = enabled;

      for (MultiStageReorder.PlacementPriority priority : MultiStageReorder.PlacementPriority.values()) {
         this.stages.put(priority, BlockMap.createForBaseBlock());
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean commitRequired() {
      return this.enabled;
   }

   private <B extends BlockStateHolder<B>> MultiStageReorder.PlacementPriority getPlacementPriority(B block) {
      return priorityMap.getOrDefault(block.getBlockType(), MultiStageReorder.PlacementPriority.FIRST);
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
      if (!this.enabled) {
         return this.setDelegateBlock(location, block);
      } else {
         BlockState existing = this.getExtent().getBlock(location);
         MultiStageReorder.PlacementPriority priority = this.getPlacementPriority(block);
         MultiStageReorder.PlacementPriority srcPriority = this.getPlacementPriority(existing);
         if (srcPriority != MultiStageReorder.PlacementPriority.FIRST) {
            BaseBlock replacement = ((BlockStateHolder)(block.getBlockType().getMaterial().isAir() ? block : BlockTypes.AIR.getDefaultState())).toBaseBlock();
            switch (srcPriority) {
               case LATE:
                  this.stages.get(MultiStageReorder.PlacementPriority.CLEAR_LATE).put(location, replacement);
                  break;
               case LAST:
                  this.stages.get(MultiStageReorder.PlacementPriority.CLEAR_LAST).put(location, replacement);
                  break;
               case FINAL:
                  this.stages.get(MultiStageReorder.PlacementPriority.CLEAR_FINAL).put(location, replacement);
            }

            if (block.getBlockType().getMaterial().isAir()) {
               return !existing.equalsFuzzy(block);
            }
         }

         this.stages.get(priority).put(location, block.toBaseBlock());
         return !existing.equalsFuzzy(block);
      }
   }

   @Override
   protected BaseBlock getBufferedFullBlock(BlockVector3 position) {
      for (BlockMap<BaseBlock> blocks : this.stages.values()) {
         BaseBlock baseBlock = blocks.get(position);
         if (baseBlock != null) {
            return baseBlock;
         }
      }

      return null;
   }

   @Override
   public Operation commitBefore() {
      if (!this.commitRequired()) {
         return null;
      } else {
         List<Operation> operations = new ArrayList<>();

         for (MultiStageReorder.PlacementPriority priority : MultiStageReorder.PlacementPriority.values()) {
            final BlockMap<BaseBlock> blocks = this.stages.get(priority);
            operations.add(new SetBlockMap(this.getExtent(), blocks) {
               @Override
               public Operation resume(RunContext run) throws WorldEditException {
                  Operation operation = super.resume(run);
                  if (operation == null) {
                     blocks.clear();
                  }

                  return operation;
               }
            });
         }

         return new OperationQueue(operations);
      }
   }

   static {
      priorityMap.put(BlockTypes.WATER, MultiStageReorder.PlacementPriority.LATE);
      priorityMap.put(BlockTypes.LAVA, MultiStageReorder.PlacementPriority.LATE);
      priorityMap.put(BlockTypes.SAND, MultiStageReorder.PlacementPriority.LATE);
      priorityMap.put(BlockTypes.GRAVEL, MultiStageReorder.PlacementPriority.LATE);
      BlockCategories.SAPLINGS.getAll().forEach(type -> priorityMap.put(type, MultiStageReorder.PlacementPriority.LAST));
      BlockCategories.FLOWER_POTS.getAll().forEach(type -> priorityMap.put(type, MultiStageReorder.PlacementPriority.LAST));
      BlockCategories.BUTTONS.getAll().forEach(type -> priorityMap.put(type, MultiStageReorder.PlacementPriority.LAST));
      BlockCategories.ANVIL.getAll().forEach(type -> priorityMap.put(type, MultiStageReorder.PlacementPriority.LAST));
      BlockCategories.WOODEN_PRESSURE_PLATES.getAll().forEach(type -> priorityMap.put(type, MultiStageReorder.PlacementPriority.LAST));
      BlockCategory carpets = BlockCategories.CARPETS;
      carpets.getAll().forEach(type -> priorityMap.put(type, MultiStageReorder.PlacementPriority.LAST));
      BlockCategories.WOOL_CARPETS.getAll().forEach(type -> priorityMap.put(type, MultiStageReorder.PlacementPriority.LAST));
      BlockCategories.RAILS.getAll().forEach(type -> priorityMap.put(type, MultiStageReorder.PlacementPriority.LAST));
      BlockCategories.BEDS.getAll().forEach(type -> priorityMap.put(type, MultiStageReorder.PlacementPriority.LAST));
      BlockCategories.SMALL_FLOWERS.getAll().forEach(type -> priorityMap.put(type, MultiStageReorder.PlacementPriority.LAST));
      priorityMap.put(BlockTypes.BLACK_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.BLUE_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.BROWN_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.CYAN_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.GRAY_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.GREEN_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.LIGHT_BLUE_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.LIGHT_GRAY_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.LIME_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.MAGENTA_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.ORANGE_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.PINK_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.PURPLE_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.RED_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.WHITE_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.YELLOW_BED, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.GRASS, MultiStageReorder.PlacementPriority.LAST);
      BlockType grass = BlockTypes.GRASS;
      priorityMap.put(grass, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.SHORT_GRASS, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.TALL_GRASS, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.ROSE_BUSH, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.DANDELION, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.BROWN_MUSHROOM, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.RED_MUSHROOM, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.FERN, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.LARGE_FERN, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.OXEYE_DAISY, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.AZURE_BLUET, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.TORCH, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.WALL_TORCH, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.FIRE, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.REDSTONE_WIRE, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.CARROTS, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.POTATOES, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.WHEAT, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.BEETROOTS, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.COCOA, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.LADDER, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.LEVER, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.REDSTONE_TORCH, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.REDSTONE_WALL_TORCH, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.SNOW, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.NETHER_PORTAL, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.END_PORTAL, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.REPEATER, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.VINE, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.LILY_PAD, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.NETHER_WART, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.PISTON, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.STICKY_PISTON, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.TRIPWIRE_HOOK, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.TRIPWIRE, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.STONE_PRESSURE_PLATE, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.HEAVY_WEIGHTED_PRESSURE_PLATE, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.COMPARATOR, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.IRON_TRAPDOOR, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.ACACIA_TRAPDOOR, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.BIRCH_TRAPDOOR, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.DARK_OAK_TRAPDOOR, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.JUNGLE_TRAPDOOR, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.OAK_TRAPDOOR, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.SPRUCE_TRAPDOOR, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.DAYLIGHT_DETECTOR, MultiStageReorder.PlacementPriority.LAST);
      priorityMap.put(BlockTypes.CAKE, MultiStageReorder.PlacementPriority.LAST);
      BlockCategories.DOORS.getAll().forEach(type -> priorityMap.put(type, MultiStageReorder.PlacementPriority.FINAL));
      BlockCategories.BANNERS.getAll().forEach(type -> priorityMap.put(type, MultiStageReorder.PlacementPriority.FINAL));
      BlockCategories.SIGNS.getAll().forEach(type -> priorityMap.put(type, MultiStageReorder.PlacementPriority.FINAL));
      BlockType sign = BlockTypes.SIGN;
      priorityMap.put(sign, MultiStageReorder.PlacementPriority.FINAL);
      BlockType wallSign = BlockTypes.WALL_SIGN;
      priorityMap.put(wallSign, MultiStageReorder.PlacementPriority.FINAL);
      priorityMap.put(BlockTypes.CACTUS, MultiStageReorder.PlacementPriority.FINAL);
      priorityMap.put(BlockTypes.SUGAR_CANE, MultiStageReorder.PlacementPriority.FINAL);
      priorityMap.put(BlockTypes.PISTON_HEAD, MultiStageReorder.PlacementPriority.FINAL);
      priorityMap.put(BlockTypes.MOVING_PISTON, MultiStageReorder.PlacementPriority.FINAL);
   }

   public static enum PlacementPriority {
      CLEAR_FINAL,
      CLEAR_LAST,
      CLEAR_LATE,
      FIRST,
      LATE,
      LAST,
      FINAL;
   }
}
