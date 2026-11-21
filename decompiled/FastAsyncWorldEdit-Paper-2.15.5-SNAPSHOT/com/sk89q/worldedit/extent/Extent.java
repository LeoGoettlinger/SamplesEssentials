package com.sk89q.worldedit.extent;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.clipboard.WorldCopyClipboard;
import com.fastasyncworldedit.core.extent.filter.block.ExtentFilterBlock;
import com.fastasyncworldedit.core.extent.processor.ProcessorScope;
import com.fastasyncworldedit.core.function.generator.CavesGen;
import com.fastasyncworldedit.core.function.generator.GenBase;
import com.fastasyncworldedit.core.function.generator.OreGen;
import com.fastasyncworldedit.core.function.generator.Resource;
import com.fastasyncworldedit.core.function.generator.SchemGen;
import com.fastasyncworldedit.core.function.pattern.MaskedPattern;
import com.fastasyncworldedit.core.history.changeset.AbstractChangeSet;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.registry.state.PropertyGroup;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.RegionMaskingFilter;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.mask.BlockCategoryMask;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.function.mask.SolidBlockMask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.MathUtils;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import javax.annotation.Nullable;

public interface Extent extends InputExtent, OutputExtent {
   BlockVector3 getMinimumPoint();

   BlockVector3 getMaximumPoint();

   default List<? extends Entity> getEntities(Region region) {
      return Collections.emptyList();
   }

   default List<? extends Entity> getEntities() {
      return Collections.emptyList();
   }

   @Nullable
   default Entity createEntity(Location location, BaseEntity entity) {
      return null;
   }

   @Nullable
   default Entity createEntity(Location location, BaseEntity entity, UUID uuid) {
      return null;
   }

   default void removeEntity(int x, int y, int z, UUID uuid) {
   }

   default int removeEntities(Region region) {
      return this.getEntities(region).stream().mapToInt(entity -> entity.remove() ? 1 : 0).sum();
   }

   default boolean isQueueEnabled() {
      return false;
   }

   default void enableQueue() {
      if (!this.isQueueEnabled()) {
         throw FaweException._enableQueue;
      }
   }

   default void disableQueue() {
      if (this.isQueueEnabled()) {
         throw FaweException._disableQueue;
      }
   }

   default boolean isWorld() {
      return false;
   }

   default boolean regenerateChunk(int x, int z, @Nullable BiomeType type, @Nullable Long seed) {
      return false;
   }

   default int getHighestTerrainBlock(final int x, final int z, int minY, int maxY) {
      maxY = Math.min(maxY, this.getMaxY());
      minY = Math.max(this.getMinY(), minY);

      for (int y = maxY; y >= minY; y--) {
         BlockState block = this.getBlock(x, y, z);
         if (block.getBlockType().getMaterial().isMovementBlocker()) {
            return y;
         }
      }

      return minY;
   }

   default int getHighestTerrainBlock(final int x, final int z, int minY, int maxY, Mask filter) {
      if (filter == null) {
         return this.getHighestTerrainBlock(x, z, minY, maxY);
      } else {
         maxY = Math.min(maxY, this.getMaxY());
         minY = Math.max(this.getMinY(), minY);
         MutableBlockVector3 mutable = new MutableBlockVector3();

         for (int y = maxY; y >= minY; y--) {
            if (filter.test(mutable.setComponents(x, y, z))) {
               return y;
            }
         }

         return minY;
      }
   }

   default int getNearestSurfaceLayer(int x, int z, int y, int minY, int maxY) {
      maxY = Math.min(maxY, this.getMaxY());
      minY = Math.max(this.getMinY(), minY);
      int clearanceAbove = maxY - y;
      int clearanceBelow = y - minY;
      int clearance = Math.min(clearanceAbove, clearanceBelow);
      BlockState block = this.getBlock(x, y, z);
      boolean state = !block.getBlockType().getMaterial().isMovementBlocker();
      int data1 = PropertyGroup.LEVEL.get(block);
      int data2 = data1;
      int offset = state ? 0 : 1;

      for (int d = 0; d <= clearance; d++) {
         int y1 = y + d;
         block = this.getBlock(x, y1, z);
         if (block.getBlockType().getMaterial().isMovementBlocker() == state) {
            return (y1 - offset << 4) - (15 - (state ? PropertyGroup.LEVEL.get(block) : data1));
         }

         data1 = PropertyGroup.LEVEL.get(block);
         int y2 = y - d;
         block = this.getBlock(x, y2, z);
         if (block.getBlockType().getMaterial().isMovementBlocker() == state) {
            return (y2 + offset << 4) - (15 - (state ? PropertyGroup.LEVEL.get(block) : data2));
         }

         data2 = PropertyGroup.LEVEL.get(block);
      }

      if (clearanceAbove != clearanceBelow) {
         if (clearanceAbove < clearanceBelow) {
            for (int layer = y - clearance - 1; layer >= minY; layer--) {
               block = this.getBlock(x, layer, z);
               if (block.getBlockType().getMaterial().isMovementBlocker() == state) {
                  return layer + offset << 4;
               }

               data1 = PropertyGroup.LEVEL.get(block);
            }
         } else {
            for (int layer = y + clearance + 1; layer <= maxY; layer++) {
               block = this.getBlock(x, layer, z);
               if (block.getBlockType().getMaterial().isMovementBlocker() == state) {
                  return (layer - offset << 4) - (15 - (state ? PropertyGroup.LEVEL.get(block) : data2));
               }

               data2 = PropertyGroup.LEVEL.get(block);
            }
         }
      }

      return (state ? minY : maxY) << 4;
   }

   default int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY, int failedMin, int failedMax, Mask mask) {
      maxY = Math.min(maxY, this.getMaxY());
      minY = Math.max(this.getMinY(), minY);
      y = Math.max(minY, Math.min(maxY, y));
      int clearanceAbove = maxY - y;
      int clearanceBelow = y - minY;
      int clearance = Math.min(clearanceAbove, clearanceBelow);
      BlockVector3 pos = MutableBlockVector3.get(x, y, z);
      boolean state = !mask.test(pos);
      int offset = state ? 0 : 1;

      for (int d = 0; d <= clearance; d++) {
         int y1 = y + d;
         if (mask.test(pos.mutY(y1)) != state) {
            return y1 - offset;
         }

         int y2 = y - d;
         if (mask.test(pos.mutY(y2)) != state) {
            return y2 + offset;
         }
      }

      if (clearanceAbove != clearanceBelow) {
         if (clearanceAbove < clearanceBelow) {
            for (int layer = y - clearance - 1; layer >= minY; layer--) {
               if (mask.test(pos.mutY(layer)) != state) {
                  return layer + offset;
               }
            }
         } else {
            for (int layerx = y + clearance + 1; layerx <= maxY; layerx++) {
               if (mask.test(pos.mutY(layerx)) != state) {
                  return layerx - offset;
               }
            }
         }
      }

      return state ? failedMin : failedMax;
   }

   default int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY, boolean ignoreAir) {
      return this.getNearestSurfaceTerrainBlock(x, z, y, minY, maxY, minY, maxY, ignoreAir);
   }

   default int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY) {
      return this.getNearestSurfaceTerrainBlock(x, z, y, minY, maxY, minY, maxY);
   }

   default int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY, int failedMin, int failedMax) {
      return this.getNearestSurfaceTerrainBlock(x, z, y, minY, maxY, failedMin, failedMax, true);
   }

   default int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY, int failedMin, int failedMax, boolean ignoreAir) {
      maxY = Math.min(maxY, this.getMaxY());
      minY = Math.max(this.getMinY(), minY);
      y = Math.max(minY, Math.min(maxY, y));
      int clearanceAbove = maxY - y;
      int clearanceBelow = y - minY;
      int clearance = Math.min(clearanceAbove, clearanceBelow);
      BlockState block = this.getBlock(x, y, z);
      boolean state = !SolidBlockMask.isSolid(block);
      int offset = state ? 0 : 1;

      for (int d = 0; d <= clearance; d++) {
         int y1 = y + d;
         block = this.getBlock(x, y1, z);
         if (matchesSolidState(block, state)) {
            return y1 - offset;
         }

         int y2 = y - d;
         block = this.getBlock(x, y2, z);
         if (matchesSolidState(block, state)) {
            return y2 + offset;
         }
      }

      if (clearanceAbove != clearanceBelow) {
         if (clearanceAbove < clearanceBelow) {
            for (int layer = y - clearance - 1; layer >= minY; layer--) {
               block = this.getBlock(x, layer, z);
               if (matchesSolidState(block, state)) {
                  return layer + offset;
               }
            }
         } else {
            for (int layerx = y + clearance + 1; layerx <= maxY; layerx++) {
               block = this.getBlock(x, layerx, z);
               if (matchesSolidState(block, state)) {
                  return layerx - offset;
               }
            }
         }
      }

      int result = state ? failedMin : failedMax;
      if (result > minY && !ignoreAir) {
         block = this.getBlock(x, result, z);
         return block.getBlockType().getMaterial().isAir() ? -1 : result;
      } else {
         return result;
      }
   }

   private static boolean matchesSolidState(BlockState block, boolean state) {
      return SolidBlockMask.isSolid(block) == state && block.getBlockType() != BlockTypes.__RESERVED__;
   }

   default void addCaves(Region region) throws WorldEditException {
      this.generate(region, new CavesGen(8));
   }

   default void generate(Region region, GenBase gen) throws WorldEditException {
      for (BlockVector2 chunkPos : region.getChunks()) {
         gen.generate(chunkPos, this);
      }
   }

   default void addSchems(Region region, Mask mask, List<ClipboardHolder> clipboards, int rarity, boolean rotate) throws WorldEditException {
      this.spawnResource(region, new SchemGen(mask, this, clipboards, rotate, region), rarity, 1);
   }

   default void spawnResource(Region region, Resource gen, int rarity, int frequency) throws WorldEditException {
      ThreadLocalRandom random = ThreadLocalRandom.current();

      for (BlockVector2 chunkPos : region.getChunks()) {
         for (int i = 0; i < frequency; i++) {
            if (random.nextInt(100) <= rarity) {
               int x = (chunkPos.x() << 4) + random.nextInt(16);
               int z = (chunkPos.z() << 4) + random.nextInt(16);
               gen.spawn(random, x, z);
            }
         }
      }
   }

   default boolean contains(BlockVector3 pt) {
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();
      return pt.containedWithin(min, max);
   }

   default boolean contains(int x, int y, int z) {
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();
      return min.x() <= x && max.x() >= x && min.y() <= y && max.y() >= y && min.z() <= z && max.z() >= z;
   }

   default void addOre(Region region, Mask mask, Pattern material, int size, int frequency, int rarity, int minY, int maxY) throws WorldEditException {
      this.addOre(region, mask, material, size, frequency, rarity, minY, maxY, false);
   }

   default void addOre(Region region, Mask mask, Pattern material, int size, int frequency, int rarity, int minY, int maxY, boolean triangular) throws WorldEditException {
      this.spawnResource(region, new OreGen(this, mask, material, size, minY, maxY, triangular), rarity, frequency);
   }

   default void addOres(Region region, Mask mask) throws WorldEditException {
      this.addOres(region, mask, false, false);
   }

   default void addOres(Region region, Mask mask, boolean deepslateBelowZero, boolean deepslateWhereDeepslate) throws WorldEditException {
      Mask var8 = new MaskIntersection(new RegionMask(region), mask);
      BiFunction<BlockType, BlockType, Pattern> patternFunc;
      if (deepslateBelowZero) {
         patternFunc = (ore, deepslate_ore) -> new MaskedPattern(new Mask() {
            @Override
            public boolean test(final BlockVector3 vector) {
               return vector.y() < 0;
            }

            @Override
            public Mask copy() {
               return this;
            }
         }, deepslate_ore, ore);
      } else if (deepslateWhereDeepslate) {
         patternFunc = (ore, deepslate_ore) -> new MaskedPattern(new BlockCategoryMask(this, BlockCategories.DEEPSLATE_ORE_REPLACEABLES), deepslate_ore, ore);
      } else {
         patternFunc = (ore, deepslate_ore) -> ore;
      }

      this.addOre(region, var8, BlockTypes.GRAVEL, 33, 14, 100, this.getMinY(), this.getMaxY(), false);
      this.addOre(region, var8, BlockTypes.ANDESITE, 33, 2, 100, 0, 60, false);
      this.addOre(region, var8, BlockTypes.ANDESITE, 33, 1, 17, 64, 128, false);
      this.addOre(region, var8, BlockTypes.DIORITE, 33, 2, 100, 0, 60, false);
      this.addOre(region, var8, BlockTypes.DIORITE, 33, 1, 17, 64, 128, false);
      this.addOre(region, var8, BlockTypes.GRANITE, 33, 2, 100, 0, 60, false);
      this.addOre(region, var8, BlockTypes.GRANITE, 33, 1, 17, 64, 128, false);
      this.addOre(region, var8, BlockTypes.TUFF, 33, 2, 100, this.getMinY(), 0, false);
      this.addOre(region, var8, BlockTypes.DIRT, 33, 7, 100, 0, 160, false);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.COAL_ORE, BlockTypes.DEEPSLATE_COAL_ORE), 17, 20, 100, 0, 192, true);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.COAL_ORE, BlockTypes.DEEPSLATE_COAL_ORE), 17, 30, 100, 136, this.getMaxY(), false);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.COPPER_ORE, BlockTypes.DEEPSLATE_COPPER_ORE), 13, 16, 100, -16, 112, true);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.IRON_ORE, BlockTypes.DEEPSLATE_IRON_ORE), 9, 10, 100, this.getMinY(), 72, false);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.IRON_ORE, BlockTypes.DEEPSLATE_IRON_ORE), 9, 10, 100, -24, 56, true);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.IRON_ORE, BlockTypes.DEEPSLATE_IRON_ORE), 9, 90, 100, 80, 384, true);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.GOLD_ORE, BlockTypes.DEEPSLATE_GOLD_ORE), 9, 1, 50, -64, -48, false);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.GOLD_ORE, BlockTypes.DEEPSLATE_GOLD_ORE), 9, 4, 100, -64, 32, true);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.REDSTONE_ORE, BlockTypes.DEEPSLATE_REDSTONE_ORE), 8, 8, 100, -32, 32, true);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.REDSTONE_ORE, BlockTypes.DEEPSLATE_REDSTONE_ORE), 8, 4, 100, this.getMinY(), 15, false);
      int diaMin = this.getMinY() - 80;
      int diaMax = this.getMinY() + 80;
      this.addOre(region, var8, patternFunc.apply(BlockTypes.DIAMOND_ORE, BlockTypes.DEEPSLATE_DIAMOND_ORE), 5, 7, 100, diaMin, diaMax, true);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.DIAMOND_ORE, BlockTypes.DEEPSLATE_DIAMOND_ORE), 8, 2, 100, -64, -4, false);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.DIAMOND_ORE, BlockTypes.DEEPSLATE_DIAMOND_ORE), 23, 1, 11, diaMin, diaMax, true);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.DIAMOND_ORE, BlockTypes.DEEPSLATE_DIAMOND_ORE), 10, 4, 100, diaMin, diaMax, true);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.LAPIS_ORE, BlockTypes.DEEPSLATE_LAPIS_ORE), 7, 2, 100, -32, 32, true);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.LAPIS_ORE, BlockTypes.DEEPSLATE_LAPIS_ORE), 7, 4, 100, this.getMinY(), 64, false);
      this.addOre(region, var8, patternFunc.apply(BlockTypes.EMERALD_ORE, BlockTypes.DEEPSLATE_EMERALD_ORE), 5, 100, 100, -16, 480, true);
   }

   default List<Countable<BlockType>> getBlockDistribution(final Region region) {
      int[] counter = new int[BlockTypes.size()];

      for (BlockVector3 pt : region) {
         BlockType type = this.getBlock(pt).getBlockType();
         if (type == BlockTypes.__RESERVED__) {
            counter[1]++;
         } else {
            counter[type.getInternalId()]++;
         }
      }

      List<Countable<BlockType>> distribution = new ArrayList<>();

      for (int i = 0; i < counter.length; i++) {
         int count = counter[i];
         if (count != 0) {
            distribution.add(new Countable<>(BlockTypes.get(i), count));
         }
      }

      Collections.sort(distribution);
      return distribution;
   }

   default List<Countable<BlockState>> getBlockDistributionWithData(final Region region) {
      int[][] counter = new int[BlockTypes.size()][];

      for (BlockVector3 pt : region) {
         BlockState blk = this.getBlock(pt);
         BlockType type = blk.getBlockType();
         if (type == BlockTypes.__RESERVED__) {
            int[] stateCounter = counter[1];
            if (stateCounter == null) {
               counter[1] = stateCounter = new int[BlockTypes.AIR.getMaxStateId() + 1];
            }

            stateCounter[BlockTypes.AIR.getDefaultState().getInternalPropertiesId()]++;
         }

         int[] stateCounter = counter[type.getInternalId()];
         if (stateCounter == null) {
            counter[type.getInternalId()] = stateCounter = new int[type.getMaxStateId() + 1];
         }

         stateCounter[blk.getInternalPropertiesId()]++;
      }

      List<Countable<BlockState>> distribution = new ArrayList<>();

      for (int typeId = 0; typeId < counter.length; typeId++) {
         BlockType typex = BlockTypes.get(typeId);
         int[] stateCount = counter[typeId];
         if (stateCount != null) {
            for (int propId = 0; propId < stateCount.length; propId++) {
               int count = stateCount[propId];
               if (count != 0) {
                  BlockState state = typex.withPropertyId(propId);
                  distribution.add(new Countable<>(state, count));
               }
            }
         }
      }

      return distribution;
   }

   @Nullable
   @Override
   default Operation commit() {
      return null;
   }

   default boolean cancel() {
      ExtentTraverser<Extent> traverser = new ExtentTraverser<>(this);
      com.fastasyncworldedit.core.extent.NullExtent nullExtent = new com.fastasyncworldedit.core.extent.NullExtent(this, FaweCache.MANUAL);
      ExtentTraverser<Extent> next = traverser.next();
      if (next != null) {
         Extent child = next.get();
         if (child instanceof com.fastasyncworldedit.core.extent.NullExtent) {
            return true;
         }

         traverser.setNext(nullExtent);
         child.cancel();
      }

      this.addProcessor(nullExtent);
      this.addPostProcessor(nullExtent);
      return true;
   }

   default int getMinY() {
      return this.getMinimumPoint().y();
   }

   default int getMaxY() {
      return this.getMaximumPoint().y();
   }

   default Clipboard lazyCopy(Region region) {
      WorldCopyClipboard faweClipboard = WorldCopyClipboard.of(this, region);
      faweClipboard.setOrigin(region.getMinimumPoint());
      return faweClipboard;
   }

   default int countBlocks(Region region, Set<BaseBlock> searchBlocks) {
      BlockMask mask = new BlockMask(this, searchBlocks);
      return this.countBlocks(region, mask);
   }

   default int countBlocks(Region region, Mask searchMask) {
      RegionVisitor visitor = new RegionVisitor(region, searchMask::test, this);
      Operations.completeBlindly(visitor);
      return visitor.getAffected();
   }

   default <B extends BlockStateHolder<B>> int setBlocks(Region region, B block) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(block);
      int changes = 0;

      for (BlockVector3 pos : region) {
         if (this.setBlock(pos, block)) {
            changes++;
         }
      }

      return changes;
   }

   default int setBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(pattern);
      if (pattern instanceof BlockPattern) {
         return this.setBlocks(region, ((BlockPattern)pattern).getBlock());
      } else if (pattern instanceof BlockStateHolder) {
         return this.setBlocks(region, (BlockStateHolder)pattern);
      } else {
         int count = 0;

         for (BlockVector3 pos : region) {
            if (pattern.apply(this, pos, pos)) {
               count++;
            }
         }

         return count;
      }
   }

   default <B extends BlockStateHolder<B>> int replaceBlocks(Region region, Set<BaseBlock> filter, B replacement) throws MaxChangedBlocksException {
      return this.replaceBlocks(region, filter, replacement);
   }

   default int replaceBlocks(Region region, Set<BaseBlock> filter, Pattern pattern) throws MaxChangedBlocksException {
      Mask mask = (Mask)(filter == null ? new ExistingBlockMask(this) : new BlockMask(this, filter));
      return this.replaceBlocks(region, mask, pattern);
   }

   default int replaceBlocks(Region region, Mask mask, Pattern pattern) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(mask);
      Preconditions.checkNotNull(pattern);
      BlockReplace replace = new BlockReplace(this, pattern);
      RegionMaskingFilter filter = new RegionMaskingFilter(mask, replace);
      RegionVisitor visitor = new RegionVisitor(region, filter, this);
      Operations.completeLegacy(visitor);
      return visitor.getAffected();
   }

   default int center(Region region, Pattern pattern) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(region);
      Preconditions.checkNotNull(pattern);
      Vector3 center = region.getCenter();
      Region centerRegion = new CuboidRegion(
         this instanceof World ? (World)this : null,
         BlockVector3.at((int)center.x(), (int)center.y(), (int)center.z()),
         BlockVector3.at(MathUtils.roundHalfUp(center.x()), center.y(), MathUtils.roundHalfUp(center.z()))
      );
      return this.setBlocks(centerRegion, pattern);
   }

   default int setBlocks(final Set<BlockVector3> vset, final Pattern pattern) {
      if (vset instanceof Region) {
         return this.setBlocks((Region)vset, pattern);
      } else {
         int count = 0;

         for (BlockVector3 pos : vset) {
            if (pattern.apply(this, pos, pos)) {
               count++;
            }
         }

         return count;
      }
   }

   default boolean relight(int x, int y, int z) {
      return false;
   }

   default boolean relightBlock(int x, int y, int z) {
      return false;
   }

   default boolean relightSky(int x, int y, int z) {
      return false;
   }

   default Extent addProcessor(IBatchProcessor processor) {
      return processor.construct(this);
   }

   default Extent addPostProcessor(IBatchProcessor processor) {
      if (processor.getScope() != ProcessorScope.READING_BLOCKS) {
         throw new IllegalArgumentException("You cannot alter blocks in a PostProcessor");
      } else {
         return processor.construct(this);
      }
   }

   default Extent enableHistory(AbstractChangeSet changeSet) {
      return Settings.settings().HISTORY.SEND_BEFORE_HISTORY ? this.addPostProcessor(changeSet) : this.addProcessor(changeSet);
   }

   default Extent disableHistory() {
      return this;
   }

   default <T extends Filter> T apply(Region region, T filter, boolean full) {
      return this.apply(region, filter);
   }

   default <T extends Filter> T apply(Iterable<BlockVector3> positions, T filter) {
      ExtentFilterBlock block = new ExtentFilterBlock(this);

      for (BlockVector3 pos : positions) {
         filter.applyBlock(block.init(pos));
      }

      return filter;
   }
}
