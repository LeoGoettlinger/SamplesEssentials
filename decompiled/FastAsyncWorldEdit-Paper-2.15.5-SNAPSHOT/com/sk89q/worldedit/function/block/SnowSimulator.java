package com.sk89q.worldedit.function.block;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.LayerFunction;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Comparator;

public class SnowSimulator implements LayerFunction {
   public static final BooleanProperty SNOWY = (BooleanProperty)BlockTypes.GRASS_BLOCK.<Boolean>getProperty("snowy");
   private static final EnumProperty PROPERTY_SLAB = (EnumProperty)BlockTypes.OAK_SLAB.<String>getProperty("type");
   private static final EnumProperty PROPERTY_STAIR = (EnumProperty)BlockTypes.OAK_STAIRS.<String>getProperty("half");
   private static final EnumProperty PROPERTY_TRAPDOOR = (EnumProperty)BlockTypes.OAK_TRAPDOOR.<String>getProperty("half");
   private static final BooleanProperty PROPERTY_TRAPDOOR_OPEN = (BooleanProperty)BlockTypes.OAK_TRAPDOOR.<Boolean>getProperty("open");
   private static final BlockState ICE = BlockTypes.ICE.getDefaultState();
   private static final BlockState SNOW = BlockTypes.SNOW.getDefaultState();
   private static final BlockState SNOW_BLOCK = BlockTypes.SNOW_BLOCK.getDefaultState();
   private static final Property<Integer> PROPERTY_SNOW_LAYERS = BlockTypes.SNOW.getProperty("layers");
   private static final Property<Integer> PROPERTY_WATER_LEVEL = BlockTypes.WATER.getProperty("level");
   private static final String PROPERTY_VALUE_TOP = "top";
   private static final String PROPERTY_VALUE_BOTTOM = "bottom";
   private static final int MAX_SNOW_LAYER = PROPERTY_SNOW_LAYERS.getValues().stream().max(Comparator.naturalOrder()).orElse(8);
   private final Extent extent;
   private final boolean stack;
   private int affected;

   public SnowSimulator(Extent extent, boolean stack) {
      this.extent = extent;
      this.stack = stack;
      this.affected = 0;
   }

   public int getAffected() {
      return this.affected;
   }

   @Override
   public boolean isGround(BlockVector3 position) {
      BlockState block = this.extent.getBlock(position);
      BlockType blockType = block.getBlockType();
      if (!blockType.getMaterial().isAir() && (!this.stack || blockType != BlockTypes.SNOW)) {
         return blockType == BlockTypes.WATER ? true : blockType.getMaterial().isMovementBlocker();
      } else {
         return false;
      }
   }

   @Override
   public boolean apply(BlockVector3 position, int depth) throws WorldEditException {
      if (depth > 0) {
         return false;
      } else {
         BlockState block = this.extent.getBlock(position);
         BlockType blockType = block.getBlockType();
         if (blockType == BlockTypes.WATER) {
            if (this.shouldFreeze(position, block) && this.extent.setBlock(position.x(), position.y(), position.z(), ICE)) {
               this.affected++;
            }

            return false;
         } else if (position.y() == this.extent.getMaximumPoint().y()) {
            return false;
         } else {
            BlockVector3 abovePosition = position.add(0, 1, 0);
            BlockState above = this.extent.getBlock(abovePosition);
            if (!this.shouldSnow(block, above)) {
               return false;
            } else {
               if (this.stack && above.getBlockType() == BlockTypes.SNOW) {
                  int layers = above.getState(PROPERTY_SNOW_LAYERS);
                  if (layers == MAX_SNOW_LAYER - 1 && !this.extent.setBlock(abovePosition, SNOW_BLOCK)) {
                     return false;
                  }

                  if (!this.extent.setBlock(abovePosition, above.with(PROPERTY_SNOW_LAYERS, layers + 1))) {
                     return false;
                  }
               } else if (!this.extent.setBlock(abovePosition, SNOW)) {
                  return false;
               }

               if (block.getBlockType().hasProperty(SNOWY)) {
                  this.extent.setBlock(position, block.with(SNOWY, true));
               }

               this.affected++;
               return false;
            }
         }
      }
   }

   private boolean shouldSnow(BlockState blockState, BlockState above) {
      if (above.isAir() || above.getBlockType() == BlockTypes.SNOW && this.stack) {
         if (BlockCategories.SNOW_LAYER_CANNOT_SURVIVE_ON.contains(blockState)) {
            return false;
         } else if (BlockCategories.SNOW_LAYER_CAN_SURVIVE_ON.contains(blockState)) {
            return true;
         } else {
            BlockType type = blockState.getBlockType();
            if (type.getMaterial().isFullCube()) {
               return true;
            } else if (type == BlockTypes.SNOW && blockState.getState(PROPERTY_SNOW_LAYERS) == MAX_SNOW_LAYER) {
               return true;
            } else if (type.hasProperty(PROPERTY_SLAB)) {
               return "top".equals(blockState.getState(PROPERTY_SLAB));
            } else if (type.hasProperty(PROPERTY_TRAPDOOR_OPEN) && blockState.getState(PROPERTY_TRAPDOOR_OPEN)) {
               return false;
            } else if (type.hasProperty(PROPERTY_TRAPDOOR)) {
               return "top".equals(blockState.getState(PROPERTY_TRAPDOOR));
            } else {
               return type.hasProperty(PROPERTY_STAIR) ? "bottom".equals(blockState.getState(PROPERTY_STAIR)) : false;
            }
         }
      } else {
         return false;
      }
   }

   private boolean shouldFreeze(BlockVector3 position, BlockState blockState) {
      return blockState.getBlockType() == BlockTypes.WATER && blockState.getState(PROPERTY_WATER_LEVEL) == 0 && this.extent.getEmittedLight(position) < 10;
   }
}
