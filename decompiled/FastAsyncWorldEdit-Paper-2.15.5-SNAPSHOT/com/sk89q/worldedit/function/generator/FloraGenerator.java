package com.sk89q.worldedit.function.generator;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

public class FloraGenerator implements RegionFunction {
   private final EditSession editSession;
   private final boolean biomeAware = false;
   private final Pattern desertPattern = getDesertPattern();
   private final Pattern temperatePattern = getTemperatePattern();
   private final Pattern mushroomPattern = mushroomPattern();
   private final Pattern netherPattern = netherPattern();
   private final Pattern warpedNyliumPattern = warpedNyliumPattern();

   public FloraGenerator(EditSession editSession) {
      this.editSession = editSession;
   }

   public boolean isBiomeAware() {
      return false;
   }

   public void setBiomeAware(boolean biomeAware) {
      if (biomeAware) {
         throw new IllegalArgumentException("Cannot enable biome-aware mode; not yet implemented");
      }
   }

   public static Pattern getDesertPattern() {
      RandomPattern pattern = new RandomPattern();
      pattern.add(BlockTypes.DEAD_BUSH.getDefaultState(), 30.0);
      pattern.add(BlockTypes.CACTUS.getDefaultState(), 20.0);
      pattern.add(BlockTypes.AIR.getDefaultState(), 300.0);
      return pattern;
   }

   public static Pattern getTemperatePattern() {
      RandomPattern pattern = new RandomPattern();
      BlockType grass = BlockTypes.SHORT_GRASS;
      pattern.add(grass.getDefaultState(), 300.0);
      pattern.add(BlockTypes.POPPY.getDefaultState(), 5.0);
      pattern.add(BlockTypes.DANDELION.getDefaultState(), 5.0);
      return pattern;
   }

   public static Pattern mushroomPattern() {
      RandomPattern pattern = new RandomPattern();
      pattern.add(BlockTypes.RED_MUSHROOM.getDefaultState(), 10.0);
      pattern.add(BlockTypes.BROWN_MUSHROOM.getDefaultState(), 10.0);
      return pattern;
   }

   public static Pattern netherPattern() {
      RandomPattern pattern = new RandomPattern();
      pattern.add(BlockTypes.CRIMSON_ROOTS.getDefaultState(), 10.0);
      pattern.add(BlockTypes.CRIMSON_FUNGUS.getDefaultState(), 20.0);
      pattern.add(BlockTypes.WARPED_FUNGUS.getDefaultState(), 5.0);
      return pattern;
   }

   public static Pattern warpedNyliumPattern() {
      RandomPattern pattern = new RandomPattern();
      pattern.add(BlockTypes.WARPED_ROOTS.getDefaultState(), 15.0);
      pattern.add(BlockTypes.NETHER_SPROUTS.getDefaultState(), 20.0);
      pattern.add(BlockTypes.WARPED_FUNGUS.getDefaultState(), 7.0);
      pattern.add(BlockTypes.CRIMSON_ROOTS.getDefaultState(), 10.0);
      return pattern;
   }

   @Override
   public boolean apply(BlockVector3 position) throws WorldEditException {
      int dataVersion = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getDataVersion();
      BlockState block = this.editSession.getBlock(position);
      if (block.getBlockType() == BlockTypes.GRASS_BLOCK) {
         this.editSession.setBlock(position.add(0, 1, 0), this.temperatePattern.applyBlock(position));
         return true;
      } else if (block.getBlockType() == BlockTypes.SAND || block.getBlockType() == BlockTypes.RED_SAND) {
         this.editSession.setBlock(position.add(0, 1, 0), this.desertPattern.applyBlock(position));
         return true;
      } else if (block.getBlockType() != BlockTypes.MYCELIUM && block.getBlockType() != BlockTypes.NETHERRACK) {
         if (dataVersion >= 2566) {
            if (block.getBlockType() == BlockTypes.SOUL_SOIL || block.getBlockType() == BlockTypes.CRIMSON_NYLIUM) {
               this.editSession.setBlock(position.add(0, 1, 0), this.netherPattern.applyBlock(position));
               return true;
            }

            if (block.getBlockType() == BlockTypes.WARPED_NYLIUM) {
               this.editSession.setBlock(position.add(0, 1, 0), this.warpedNyliumPattern.applyBlock(position));
            }
         }

         return false;
      } else {
         this.editSession.setBlock(position.add(0, 1, 0), this.mushroomPattern.applyBlock(position));
         return true;
      }
   }
}
