package com.fastasyncworldedit.core.extent.processor;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extent.NullExtent;
import com.fastasyncworldedit.core.extent.filter.block.FilterBlock;
import com.fastasyncworldedit.core.function.mask.AdjacentAny2DMask;
import com.fastasyncworldedit.core.math.BlockVector3ChunkMap;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.math.MutableVector3;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.BlockCategoryMask;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.enginehub.linbus.tree.LinCompoundTag;

public abstract class PlacementStateProcessor extends AbstractDelegateExtent implements IBatchProcessor, Pattern {
   private static final Direction[] NESW = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
   private static final int CHUNK_BLOCK_POS_MASK = -16;
   private static volatile boolean SETUP = false;
   private static BlockTypeMask DEFAULT_MASK = null;
   private static BlockTypeMask IN_FIRST_PASS = null;
   private static BlockTypeMask REQUIRES_SECOND_PASS = null;
   private static BlockTypeMask IN_FIRST_PASS_WITHOUT_SECOND = null;
   private static AdjacentAny2DMask ADJACENT_STAIR_MASK = null;
   protected final Extent extent;
   protected final BlockTypeMask mask;
   protected final Region region;
   protected final Map<PlacementStateProcessor.SecondPass, Character> postCompleteSecondPasses;
   protected final ThreadLocal<PlacementStateProcessor> threadProcessors;
   protected final AtomicBoolean finished;
   private final MutableVector3 clickPos = new MutableVector3();
   private final MutableBlockVector3 clickedBlock = new MutableBlockVector3();
   private final MutableBlockVector3 placedBlock = new MutableBlockVector3(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
   private IChunkGet processChunkGet = null;
   private IChunkSet processChunkSet = null;
   private int processChunkX;
   private int processChunkZ;

   public PlacementStateProcessor(Extent extent, BlockTypeMask mask, Region region) {
      super(extent);
      if (!SETUP) {
         synchronized (PlacementStateProcessor.class) {
            if (!SETUP) {
               setup();
            }
         }
      }

      this.extent = extent;
      this.mask = mask == null ? DEFAULT_MASK : mask;
      this.region = region;
      this.postCompleteSecondPasses = new ConcurrentHashMap<>();
      this.threadProcessors = ThreadLocal.withInitial(this::fork);
      this.finished = new AtomicBoolean();
   }

   protected PlacementStateProcessor(
      Extent extent,
      BlockTypeMask mask,
      Map<PlacementStateProcessor.SecondPass, Character> crossChunkSecondPasses,
      ThreadLocal<PlacementStateProcessor> threadProcessors,
      Region region,
      AtomicBoolean finished
   ) {
      super(extent);
      this.extent = extent;
      this.mask = mask;
      this.region = region;
      this.postCompleteSecondPasses = crossChunkSecondPasses;
      this.threadProcessors = threadProcessors;
      this.finished = finished;
   }

   protected static World getWorldFromExtent(Extent extent) throws UnsupportedOperationException {
      World world = ExtentTraverser.getWorldFromExtent(extent);
      if (world == null) {
         throw new UnsupportedOperationException(
            "World is required for PlacementStateProcessor but none found in given extent (" + extent.getClass().getName() + ")."
         );
      } else {
         return world;
      }
   }

   private static void setup() {
      NullExtent nullExtent = new NullExtent(com.sk89q.worldedit.extent.NullExtent.INSTANCE, Caption.of("PlacementStateProcessor fell through to null extent"));
      IN_FIRST_PASS = new BlockTypeMask(nullExtent);
      IN_FIRST_PASS.add(BlockTypes.CHEST, BlockTypes.TRAPPED_CHEST);
      IN_FIRST_PASS.add(BlockCategories.STAIRS.getAll());
      IN_FIRST_PASS_WITHOUT_SECOND = new BlockTypeMask(nullExtent);
      IN_FIRST_PASS_WITHOUT_SECOND.add(BlockCategories.STAIRS.getAll());
      REQUIRES_SECOND_PASS = new BlockTypeMask(nullExtent);
      REQUIRES_SECOND_PASS.add(
         BlockTypes.IRON_BARS,
         BlockTypes.GLASS_PANE,
         BlockTypes.BLACK_STAINED_GLASS_PANE,
         BlockTypes.BLUE_STAINED_GLASS_PANE,
         BlockTypes.BROWN_STAINED_GLASS_PANE,
         BlockTypes.LIGHT_BLUE_STAINED_GLASS_PANE,
         BlockTypes.PINK_STAINED_GLASS_PANE,
         BlockTypes.LIGHT_GRAY_STAINED_GLASS_PANE,
         BlockTypes.GRAY_STAINED_GLASS_PANE,
         BlockTypes.CYAN_STAINED_GLASS_PANE,
         BlockTypes.PURPLE_STAINED_GLASS_PANE,
         BlockTypes.GREEN_STAINED_GLASS_PANE,
         BlockTypes.LIME_STAINED_GLASS_PANE,
         BlockTypes.MAGENTA_STAINED_GLASS_PANE,
         BlockTypes.YELLOW_STAINED_GLASS_PANE,
         BlockTypes.ORANGE_STAINED_GLASS_PANE,
         BlockTypes.RED_STAINED_GLASS_PANE,
         BlockTypes.WHITE_STAINED_GLASS_PANE,
         BlockTypes.TRIPWIRE,
         BlockTypes.TWISTING_VINES_PLANT,
         BlockTypes.CAVE_VINES_PLANT,
         BlockTypes.WEEPING_VINES_PLANT,
         BlockTypes.VINE,
         BlockTypes.REDSTONE_WIRE
      );
      BlockCategory[] categories = new BlockCategory[]{BlockCategories.FENCES, BlockCategories.FENCE_GATES, BlockCategories.WALLS, BlockCategories.CAVE_VINES};

      for (BlockCategory category : categories) {
         if (category != null) {
            REQUIRES_SECOND_PASS.add(category.getAll());
         }
      }

      DEFAULT_MASK = REQUIRES_SECOND_PASS.copy();
      DEFAULT_MASK.add(
         BlockTypes.CHORUS_PLANT,
         BlockTypes.DRIPSTONE_BLOCK,
         BlockTypes.POINTED_DRIPSTONE,
         BlockTypes.BIG_DRIPLEAF,
         BlockTypes.BIG_DRIPLEAF_STEM,
         BlockTypes.CAMPFIRE,
         BlockTypes.CHEST,
         BlockTypes.TRAPPED_CHEST,
         BlockTypes.CRAFTER,
         BlockTypes.MUSHROOM_STEM,
         BlockTypes.BROWN_MUSHROOM_BLOCK,
         BlockTypes.RED_MUSHROOM_BLOCK
      );
      categories = new BlockCategory[]{BlockCategories.STAIRS, BlockCategories.BAMBOO_BLOCKS, BlockCategories.TALL_FLOWERS};

      for (BlockCategory categoryx : categories) {
         if (categoryx != null) {
            DEFAULT_MASK.add(categoryx.getAll());
         }
      }

      ADJACENT_STAIR_MASK = new AdjacentAny2DMask(new BlockCategoryMask(nullExtent, BlockCategories.STAIRS), false);
      SETUP = true;
   }

   @Override
   public IChunkSet processSet(IChunk iChunk, IChunkGet chunkGet, IChunkSet chunkSet) {
      if (this.finished.get()) {
         return chunkSet;
      } else {
         PlacementStateProcessor threadProcessor = this.threadProcessors.get();

         IChunkSet var5;
         try {
            threadProcessor.initProcess(iChunk, chunkGet, chunkSet);
            var5 = threadProcessor.process();
         } finally {
            threadProcessor.uninit();
         }

         return var5;
      }
   }

   private void initProcess(IChunk iChunk, IChunkGet chunkGet, IChunkSet chunkSet) {
      this.processChunkX = iChunk.getX() << 4;
      this.processChunkZ = iChunk.getZ() << 4;
      this.processChunkGet = chunkGet;
      this.processChunkSet = chunkSet;
   }

   private void uninit() {
      this.processChunkGet = null;
      this.processChunkSet = null;
   }

   private IChunkSet process() {
      Map<BlockVector3, FaweCompoundTag> setTiles = this.processChunkSet.tiles();

      for (int layer = this.processChunkGet.getMinSectionPosition(); layer <= this.processChunkGet.getMaxSectionPosition(); layer++) {
         int layerY = layer << 4;
         char[] set = this.processChunkSet.loadIfPresent(layer);
         if (set != null) {
            int y = 0;

            for (int i = 0; y < 16; i += 256) {
               int blockY = layerY + y;
               this.checkAndPerformUpdate(setTiles, set, i, blockY, true);
               this.checkAndPerformUpdate(setTiles, set, i, blockY, false);
               y++;
            }
         }
      }

      return this.processChunkSet;
   }

   private void checkAndPerformUpdate(Map<BlockVector3, FaweCompoundTag> setTiles, char[] set, int index, int blockY, boolean firstPass) {
      for (int z = 0; z < 16; z++) {
         int blockZ = this.processChunkZ + z;
         int x = 0;

         while (x < 16) {
            int blockX = this.processChunkX + x;
            char ordinal = set[index];
            BlockState state = BlockTypesCache.states[ordinal];
            if ((firstPass ? IN_FIRST_PASS.test(state) : !IN_FIRST_PASS_WITHOUT_SECOND.test(state)) && this.mask.test(state)) {
               boolean atEdge = x == 0 || x == 15 || z == 0 || z == 15;
               if (!firstPass && atEdge && REQUIRES_SECOND_PASS.test(state)) {
                  this.postCompleteSecondPasses
                     .put(
                        new PlacementStateProcessor.SecondPass(
                           blockX, blockY, blockZ, setTiles.isEmpty() ? null : (FaweCompoundTag)((BlockVector3ChunkMap)setTiles).remove(x, blockY, z)
                        ),
                        ordinal
                     );
                  set[index] = 0;
               } else {
                  if (!state.getBlockType().equals(BlockTypes.CHEST) && !state.getBlockType().equals(BlockTypes.TRAPPED_CHEST)) {
                     this.placedBlock.setComponents(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
                  } else {
                     this.placedBlock.setComponents(blockX, blockY, blockZ);
                  }

                  char newOrdinal = this.getBlockOrdinal(blockX, blockY, blockZ, state);
                  if (newOrdinal != ordinal) {
                     set[index] = newOrdinal;
                  }
               }
            }

            x++;
            index++;
         }
      }
   }

   @Override
   public ProcessorScope getScope() {
      return ProcessorScope.CHANGING_BLOCKS;
   }

   @Override
   public void finish() {
      this.flush();
   }

   @Override
   public void flush() {
      this.finished.set(true);

      for (Entry<PlacementStateProcessor.SecondPass, Character> entry : this.postCompleteSecondPasses.entrySet()) {
         char ordinal = entry.getValue();
         PlacementStateProcessor.SecondPass secondPass = entry.getKey();
         BlockState state;
         if (ordinal != 0) {
            state = BlockTypesCache.states[ordinal];
         } else {
            state = this.extent.getBlock(secondPass.x, secondPass.y, secondPass.z);
         }

         char newOrdinal = this.getBlockOrdinal(secondPass.x, secondPass.y, secondPass.z, state);
         if (newOrdinal != state.getOrdinalChar() || ordinal != 0) {
            if (secondPass.tile != null) {
               this.extent.tile(secondPass.x, secondPass.y, secondPass.z, secondPass.tile);
            }

            this.extent.setBlock(secondPass.x, secondPass.y, secondPass.z, BlockTypesCache.states[newOrdinal]);
         }
      }

      this.postCompleteSecondPasses.clear();
   }

   public abstract PlacementStateProcessor fork();

   protected abstract char getStateAtFor(int x, int y, int z, BlockState state, Vector3 clickPos, Direction clickedFaceDirection, BlockVector3 clickedBlock);

   public BlockState getBlockStateAt(int x, int y, int z) {
      Character ord = this.postCompleteSecondPasses.get(new PlacementStateProcessor.SecondPass(x, y, z, null));
      if (ord != null && ord != 0) {
         return BlockTypesCache.states[ord];
      } else if (this.processChunkSet != null && (x & -16) == this.processChunkX && (z & -16) == this.processChunkZ) {
         char[] set = this.processChunkSet.loadIfPresent(y >> 4);
         if (set == null) {
            return this.processChunkGet.getBlock(x & 15, y, z & 15);
         } else {
            char ordinal = set[(y & 15) << 8 | (z & 15) << 4 | x & 15];
            if (ordinal == 0) {
               return this.processChunkGet.getBlock(x & 15, y, z & 15);
            } else {
               BlockState state = BlockTypesCache.states[ordinal];
               if (state.getBlockType().equals(BlockTypes.CHEST) || state.getBlockType().equals(BlockTypes.TRAPPED_CHEST)) {
                  String shape = state.getState(PropertyKey.TYPE).toString();
                  if (shape.equals("right")) {
                     Direction facing = state.getState(PropertyKey.FACING);
                     Direction left = facing.getLeft();
                     int testX = x + left.getBlockX();
                     int testZ = z + left.getBlockZ();
                     if (this.placedBlock.isAt(testX, y, testZ)) {
                        return state.with(PropertyKey.TYPE, "single");
                     }
                  } else if (shape.equals("left")) {
                     Direction facing = state.getState(PropertyKey.FACING);
                     Direction right = facing.getRight();
                     int testX = x + right.getBlockX();
                     int testZ = z + right.getBlockZ();
                     if (this.placedBlock.isAt(testX, y, testZ)) {
                        return state.with(PropertyKey.TYPE, "single");
                     }
                  }
               }

               return state;
            }
         }
      } else {
         return this.extent.getBlock(x, y, z);
      }
   }

   public LinCompoundTag getTileAt(int x, int y, int z) {
      PlacementStateProcessor.SecondPass secondPass = new PlacementStateProcessor.SecondPass(x, y, z, null);
      Character ord = this.postCompleteSecondPasses.get(secondPass);
      if (ord != null && ord != 0) {
         for (PlacementStateProcessor.SecondPass pass : this.postCompleteSecondPasses.keySet()) {
            if (pass.hashCode() == secondPass.hashCode()) {
               return pass.tile != null ? pass.tile.linTag() : BlockTypesCache.states[ord].getNbt();
            }
         }

         return BlockTypesCache.states[ord].getNbt();
      } else if (this.processChunkSet != null && (x & -16) == this.processChunkX && (z & -16) == this.processChunkZ) {
         FaweCompoundTag tile = this.processChunkSet.tile(x & 15, y, z & 15);
         if (tile != null) {
            return tile.linTag();
         } else {
            char[] set = this.processChunkSet.loadIfPresent(y >> 4);
            if (set == null) {
               return this.processChunkGet.getFullBlock(x & 15, y, z & 15).getNbt();
            } else {
               char ordinal = set[(y & 15) << 8 | (z & 15) << 4 | x & 15];
               return ordinal == 0 ? this.processChunkGet.getFullBlock(x & 15, y, z & 15).getNbt() : BlockTypesCache.states[ordinal].getNbt();
            }
         }
      } else {
         return this.extent.getFullBlock(x, y, z).getNbt();
      }
   }

   private char getBlockOrdinal(int blockX, int blockY, int blockZ, BlockState state) {
      char override = this.getOverrideBlockOrdinal(blockX, blockY, blockZ, state);
      if (override != 0) {
         return override;
      } else {
         EnumSet<Direction> dirs = Direction.getDirections(state);
         Direction clickedFaceDirection = null;
         Set<String> states = state.getStates().keySet().stream().map(Property::getName).collect(Collectors.toSet());
         if (!dirs.isEmpty() && (!states.contains("NORTH") || !states.contains("EAST"))) {
            boolean hadNesw = false;

            for (Direction dir : NESW) {
               if (dirs.contains(dir)) {
                  clickedFaceDirection = dir.getLeft().getLeft();
                  if (state.getBlockType() == BlockTypes.CHEST || state.getBlockType() == BlockTypes.TRAPPED_CHEST) {
                     Direction tmp = clickedFaceDirection;
                     clickedFaceDirection = dir;
                     dir = tmp;
                  }

                  this.clickPos.setComponents(blockX + 0.5 * (1 + dir.getBlockX()), blockY + 0.2, blockZ + 0.5 * (1 + dir.getBlockZ()));
                  this.clickedBlock.setComponents(blockX, blockY, blockZ).add(dir.toBlockVector());
                  hadNesw = true;
                  break;
               }
            }

            if (hadNesw) {
               if (dirs.contains(Direction.UP)) {
                  this.clickPos.mutY(blockY + 0.6);
               }
            } else if (dirs.contains(Direction.UP)) {
               clickedFaceDirection = Direction.DOWN;
               this.clickPos.setComponents(blockX + 0.5, blockY + 1.0, blockZ + 0.5);
               this.clickedBlock.setComponents(blockX, blockY + 1, blockZ);
            } else if (dirs.contains(Direction.DOWN)) {
               clickedFaceDirection = Direction.UP;
               this.clickPos.setComponents(blockX + 0.5, blockY - 1.0, blockZ + 0.5);
               this.clickedBlock.setComponents(blockX, blockY - 1, blockZ);
            }
         } else {
            this.clickPos.setComponents(blockX + 0.5, (double)blockY, blockZ + 0.5);
            clickedFaceDirection = Direction.UP;
            this.clickedBlock.setComponents(blockX, blockY - 1, blockZ);
         }

         return this.getStateAtFor(blockX, blockY, blockZ, state, this.clickPos, clickedFaceDirection, this.clickedBlock);
      }
   }

   protected char getOverrideBlockOrdinal(int blockX, int blockY, int blockZ, BlockState state) {
      if (BlockCategories.TALL_FLOWERS.contains(state)) {
         PropertyKey propertyKey = PropertyKey.HALF;
         BlockState plantState = this.extent.getBlock(blockX, blockY - 1, blockZ).getBlockType().equals(state.getBlockType())
            ? state.with(propertyKey, "upper")
            : state.with(propertyKey, "lower");
         return plantState.getOrdinalChar();
      } else {
         return '\u0000';
      }
   }

   @Override
   public void applyBlock(FilterBlock block) {
      if (!this.finished.get()) {
         BlockState state = BlockTypesCache.states[block.getOrdinal()];
         if (this.mask.test(state)) {
            if (REQUIRES_SECOND_PASS.test(block.getBlock()) && ADJACENT_STAIR_MASK.test(this.extent, block)) {
               this.postCompleteSecondPasses.put(new PlacementStateProcessor.SecondPass(block), '\u0000');
            }

            char ordinal = (char)block.getOrdinal();
            char newOrdinal = this.getBlockOrdinal(block.x(), block.y(), block.z(), block.getBlock());
            if (ordinal != newOrdinal) {
               block.setBlock(BlockTypesCache.states[newOrdinal]);
            }
         }
      }
   }

   @Override
   public boolean apply(Extent orDefault, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      if (orDefault == null) {
         orDefault = this.extent;
      }

      BaseBlock block = orDefault.getFullBlock(get);
      if (!this.mask.test(block)) {
         return false;
      } else if (REQUIRES_SECOND_PASS.test(block) && ADJACENT_STAIR_MASK.test(this.extent, set)) {
         this.postCompleteSecondPasses.put(new PlacementStateProcessor.SecondPass(set), '\u0000');
         return false;
      } else {
         char newOrdinal = this.getBlockOrdinal(set.x(), set.y(), set.z(), block.toBlockState());
         if (block.getOrdinalChar() != newOrdinal) {
            BlockState newState = BlockTypesCache.states[newOrdinal];
            orDefault.setBlock(set.x(), set.y(), set.z(), newState);
            LinCompoundTag nbt = block.getNbt();
            if (nbt != null && newState.getBlockType() == block.getBlockType()) {
               orDefault.tile(set.x(), set.y(), set.z(), FaweCompoundTag.of(nbt));
            }

            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      BaseBlock block = this.extent.getFullBlock(position);
      if (this.finished.get() || !this.mask.test(block)) {
         return block;
      } else if (REQUIRES_SECOND_PASS.test(block) && ADJACENT_STAIR_MASK.test(this.extent, position)) {
         this.postCompleteSecondPasses.put(new PlacementStateProcessor.SecondPass(position), '\u0000');
         return block;
      } else {
         char newOrdinal = this.getBlockOrdinal(position.x(), position.y(), position.z(), block.toBlockState());
         if (block.getOrdinalChar() != newOrdinal) {
            BlockState state = BlockTypesCache.states[newOrdinal];
            LinCompoundTag nbt = block.getNbt();
            if (nbt != null && state.getBlockType() == block.getBlockType()) {
               state.toBaseBlock(nbt);
            }

            return state.toBaseBlock();
         } else {
            return block;
         }
      }
   }

   protected record SecondPass(int x, int y, int z, FaweCompoundTag tile) {
      private SecondPass(BlockVector3 pos) {
         this(pos.x(), pos.y(), pos.z(), null);
      }

      @Override
      public int hashCode() {
         return this.x ^ this.z << 12 ^ this.y << 24;
      }
   }
}
