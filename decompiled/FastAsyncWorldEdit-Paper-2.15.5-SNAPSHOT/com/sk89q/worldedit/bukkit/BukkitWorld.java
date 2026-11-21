package com.sk89q.worldedit.bukkit;

import com.fastasyncworldedit.bukkit.util.WorldUnloadedException;
import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.implementation.packet.ChunkPacket;
import com.fastasyncworldedit.core.util.TaskManager;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.bukkit.adapter.UnsupportedVersionEditException;
import com.sk89q.worldedit.bukkit.paperlib.PaperLib;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.internal.wna.WorldNativeAccess;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BukkitWorld extends AbstractWorld {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final boolean HAS_3D_BIOMES;
   public static final boolean HAS_MIN_Y;
   private static final Map<Integer, Effect> effects = new HashMap<>();
   protected WeakReference<World> worldRef;
   protected final String worldNameRef;
   private final WorldNativeAccess<?, ?, ?> worldNativeAccess;
   @Deprecated
   private static final EnumMap<TreeGenerator.TreeType, TreeType> treeTypeMapping;
   private static volatile boolean hasWarnedImplError;

   public BukkitWorld(World world) {
      this.worldRef = new WeakReference<>(world);
      this.worldNameRef = world.getName();
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      if (adapter != null) {
         this.worldNativeAccess = adapter.createWorldNativeAccess(world);
      } else {
         this.worldNativeAccess = null;
      }
   }

   @Override
   public List<com.sk89q.worldedit.entity.Entity> getEntities(Region region) {
      World world = this.getWorld();
      List<Entity> ents = TaskManager.taskManager().sync(world::getEntities);
      List<com.sk89q.worldedit.entity.Entity> entities = new ArrayList<>();

      for (Entity ent : ents) {
         if (region.contains(BukkitAdapter.asBlockVector(ent.getLocation()))) {
            entities.add(BukkitAdapter.adapt(ent));
         }
      }

      return entities;
   }

   @Override
   public List<com.sk89q.worldedit.entity.Entity> getEntities() {
      List<com.sk89q.worldedit.entity.Entity> list = new ArrayList<>();

      for (Entity entity : (List)TaskManager.taskManager().sync(this.getWorld()::getEntities)) {
         list.add(BukkitAdapter.adapt(entity));
      }

      return (T)list;
   }

   @Override
   public int removeEntities(final Region region) {
      List<com.sk89q.worldedit.entity.Entity> entities = this.getEntities(region);
      return TaskManager.taskManager().sync(() -> entities.stream().mapToInt(entity -> entity.remove() ? 1 : 0).sum());
   }

   public World getWorld() {
      World tmp = this.worldRef.get();
      if (tmp == null) {
         tmp = Bukkit.getWorld(this.worldNameRef);
         if (tmp != null) {
            this.worldRef = new WeakReference<>(tmp);
         }
      }

      return (World)Preconditions.checkNotNull(tmp, "The world was unloaded and the reference is unavailable");
   }

   protected World getWorldChecked() throws WorldEditException {
      World tmp = this.worldRef.get();
      if (tmp == null) {
         tmp = Bukkit.getWorld(this.worldNameRef);
         if (tmp != null) {
            this.worldRef = new WeakReference<>(tmp);
         }
      }

      if (tmp == null) {
         throw new WorldUnloadedException(this.worldNameRef);
      } else {
         return tmp;
      }
   }

   @Override
   public String getName() {
      return this.getWorldChecked().getName();
   }

   @Override
   public String getNameUnsafe() {
      return this.worldNameRef;
   }

   @Override
   public String id() {
      return this.getWorld().getName().replace(" ", "_").toLowerCase(Locale.ROOT);
   }

   @Override
   public Path getStoragePath() {
      Path worldFolder = this.getWorld().getWorldFolder().toPath();

      return switch (this.getWorld().getEnvironment()) {
         case NETHER -> worldFolder.resolve("DIM-1");
         case THE_END -> worldFolder.resolve("DIM1");
         default -> worldFolder;
      };
   }

   @Override
   public int getBlockLightLevel(BlockVector3 pt) {
      this.testCoords(pt);
      return this.getWorld().getBlockAt(pt.x(), pt.y(), pt.z()).getLightLevel();
   }

   @Override
   public boolean regenerate(Region region, Extent extent, RegenOptions options) {
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();

      try {
         if (adapter != null) {
            return adapter.regenerate(this.getWorld(), region, extent, options);
         } else {
            throw new UnsupportedOperationException("Missing BukkitImplAdapter for this version.");
         }
      } catch (FaweException var6) {
         throw var6;
      } catch (Exception var7) {
         LOGGER.warn("Regeneration via adapter failed.", var7);
         return false;
      }
   }

   @Override
   public boolean clearContainerBlockContents(BlockVector3 pt) {
      Preconditions.checkNotNull(pt);
      this.testCoords(pt);
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      if (adapter != null) {
         try {
            return adapter.clearContainerBlockContents(this.getWorld(), pt);
         } catch (Exception var5) {
         }
      }

      if (!this.getBlock(pt).getBlockType().getMaterial().hasContainer()) {
         return false;
      } else {
         Block block = this.getWorld().getBlockAt(pt.x(), pt.y(), pt.z());
         BlockState state = PaperLib.getBlockState(block, false).getState();
         if (!(state instanceof InventoryHolder)) {
            return false;
         } else {
            TaskManager.taskManager().sync(() -> {
               InventoryHolder chest = (InventoryHolder)state;
               Inventory inven = chest.getInventory();
               if (chest instanceof Chest) {
                  inven = ((Chest)chest).getBlockInventory();
               }

               inven.clear();
               return null;
            });
            return true;
         }
      }
   }

   private static void generateTreeMap() {
      for (TreeGenerator.TreeType type : TreeGenerator.TreeType.values()) {
         try {
            TreeType bukkitType = TreeType.valueOf(type.name());
            treeTypeMapping.put(type, bukkitType);
         } catch (IllegalArgumentException var5) {
         }
      }

      treeTypeMapping.put(TreeGenerator.TreeType.SHORT_JUNGLE, TreeType.SMALL_JUNGLE);
      treeTypeMapping.put(TreeGenerator.TreeType.RANDOM, TreeType.BROWN_MUSHROOM);
      treeTypeMapping.put(TreeGenerator.TreeType.RANDOM_REDWOOD, TreeType.REDWOOD);
      treeTypeMapping.put(TreeGenerator.TreeType.PINE, TreeType.REDWOOD);
      treeTypeMapping.put(TreeGenerator.TreeType.RANDOM_BIRCH, TreeType.BIRCH);
      treeTypeMapping.put(TreeGenerator.TreeType.RANDOM_JUNGLE, TreeType.JUNGLE);
      treeTypeMapping.put(TreeGenerator.TreeType.RANDOM_MUSHROOM, TreeType.BROWN_MUSHROOM);

      for (TreeGenerator.TreeType type : TreeGenerator.TreeType.values()) {
         if (treeTypeMapping.get(type) == null) {
            LOGGER.info(
               "No TreeType mapping for TreeGenerator.TreeType.{}\nThe above message is displayed because your FAWE version is newer than {}\nand contains features of future Minecraft versions which do not exist in {} hence the tree type\n{} is not available. This is not an error. This version of FAWE will work on your version of\nMinecraft. This is an informative message only.",
               type,
               Bukkit.getVersion(),
               Bukkit.getVersion(),
               type
            );
         }
      }
   }

   @Deprecated
   public static TreeType toBukkitTreeType(TreeGenerator.TreeType type) {
      return treeTypeMapping.get(type);
   }

   @Deprecated
   @Override
   public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, BlockVector3 pt) {
      this.testCoords(pt);
      return WorldEditPlugin.getInstance().getBukkitImplAdapter().generateTree(type, editSession, pt, this.getWorld());
   }

   @Override
   public boolean generateTree(final com.sk89q.worldedit.world.generation.TreeType type, final EditSession editSession, final BlockVector3 position) throws MaxChangedBlocksException {
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      return adapter != null ? adapter.generateTree(type, this.getWorld(), editSession, position) : false;
   }

   @Override
   public void dropItem(Vector3 pt, BaseItemStack item) {
      World world = this.getWorld();
      world.dropItemNaturally(BukkitAdapter.adapt(world, pt), BukkitAdapter.adapt(item));
   }

   @Override
   public void checkLoadedChunk(BlockVector3 pt) {
      this.testCoords(pt);
      World world = this.getWorld();
      int X = pt.x() >> 4;
      int Z = pt.z() >> 4;
      if (Fawe.isMainThread()) {
         world.getChunkAt(X, Z);
      } else if (PaperLib.isPaper()) {
         PaperLib.getChunkAtAsync(world, X, Z, true);
      }
   }

   @Override
   public boolean equals(Object other) {
      World ref = this.worldRef.get();
      if (ref == null) {
         return false;
      } else if (other == null) {
         return false;
      } else if (other instanceof BukkitWorld) {
         World otherWorld = ((BukkitWorld)other).worldRef.get();
         return ref.equals(otherWorld);
      } else {
         return other instanceof com.sk89q.worldedit.world.World ? ((com.sk89q.worldedit.world.World)other).getName().equals(ref.getName()) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.getWorld().hashCode();
   }

   @Override
   public int getMaxY() {
      return this.getWorld().getMaxHeight() - 1;
   }

   @Override
   public int getMinY() {
      return HAS_MIN_Y ? this.getWorld().getMinHeight() : super.getMinY();
   }

   @Override
   public void fixAfterFastMode(Iterable<BlockVector2> chunks) {
      World world = this.getWorld();

      for (BlockVector2 chunkPos : chunks) {
         world.refreshChunk(chunkPos.x(), chunkPos.z());
      }
   }

   @Override
   public boolean playEffect(Vector3 position, int type, int data) {
      World world = this.getWorld();
      Effect effect = effects.get(type);
      if (effect == null) {
         return false;
      } else {
         world.playEffect(BukkitAdapter.adapt(world, position), effect, data);
         return true;
      }
   }

   @Override
   public boolean playBlockBreakEffect(Vector3 position, BlockType type) {
      World world = this.getWorld();
      world.playEffect(BukkitAdapter.adapt(world, position), Effect.STEP_SOUND, BukkitAdapter.adapt(type));
      return true;
   }

   @Override
   public WeatherType getWeather() {
      if (this.getWorld().isThundering()) {
         return WeatherTypes.THUNDER_STORM;
      } else {
         return this.getWorld().hasStorm() ? WeatherTypes.RAIN : WeatherTypes.CLEAR;
      }
   }

   @Override
   public long getRemainingWeatherDuration() {
      return this.getWorld().getWeatherDuration();
   }

   @Override
   public void setWeather(WeatherType weatherType) {
      if (weatherType == WeatherTypes.THUNDER_STORM) {
         this.getWorld().setThundering(true);
      } else if (weatherType == WeatherTypes.RAIN) {
         this.getWorld().setStorm(true);
      } else {
         this.getWorld().setStorm(false);
         this.getWorld().setThundering(false);
      }
   }

   @Override
   public void setWeather(WeatherType weatherType, long duration) {
      if (weatherType == WeatherTypes.THUNDER_STORM) {
         this.getWorld().setThundering(true);
         this.getWorld().setThunderDuration((int)duration);
         this.getWorld().setWeatherDuration((int)duration);
      } else if (weatherType == WeatherTypes.RAIN) {
         this.getWorld().setStorm(true);
         this.getWorld().setWeatherDuration((int)duration);
      } else {
         this.getWorld().setStorm(false);
         this.getWorld().setThundering(false);
         this.getWorld().setWeatherDuration((int)duration);
      }
   }

   @Override
   public BlockVector3 getSpawnPosition() {
      return BukkitAdapter.asBlockVector(this.getWorld().getSpawnLocation());
   }

   @Override
   public void simulateBlockMine(BlockVector3 pt) {
      this.testCoords(pt);
      this.getWorld().getBlockAt(pt.x(), pt.y(), pt.z()).breakNaturally();
   }

   @Override
   public Collection<BaseItemStack> getBlockDrops(BlockVector3 position) {
      return this.getWorld().getBlockAt(position.x(), position.y(), position.z()).getDrops().stream().map(BukkitAdapter::adapt).collect(Collectors.toList());
   }

   @Override
   public boolean canPlaceAt(BlockVector3 position, com.sk89q.worldedit.world.block.BlockState blockState) {
      this.testCoords(position);
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      return adapter != null ? adapter.canPlaceAt(this.getWorld(), position, blockState) : true;
   }

   @Override
   public boolean generateFeature(ConfiguredFeatureType type, EditSession editSession, BlockVector3 position) {
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      return adapter != null ? adapter.generateFeature(type, this.getWorld(), editSession, position) : false;
   }

   @Override
   public boolean generateStructure(StructureType type, EditSession editSession, BlockVector3 position) {
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      return adapter != null ? adapter.generateStructure(type, this.getWorld(), editSession, position) : false;
   }

   @Override
   public com.sk89q.worldedit.world.block.BlockState getBlock(BlockVector3 position) {
      this.testCoords(position);
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      if (adapter != null) {
         try {
            return adapter.getBlock(BukkitAdapter.adapt(this.getWorld(), position)).toImmutableState();
         } catch (Exception var4) {
            if (!hasWarnedImplError) {
               hasWarnedImplError = true;
               LOGGER.warn("Unable to retrieve block via impl adapter", var4);
            }
         }
      }

      if (WorldEditPlugin.getInstance().getLocalConfiguration().unsupportedVersionEditing) {
         Block bukkitBlock = this.getWorld().getBlockAt(position.x(), position.y(), position.z());
         return BukkitAdapter.adapt(bukkitBlock.getBlockData());
      } else {
         throw new RuntimeException(new UnsupportedVersionEditException());
      }
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) {
      this.testCoords(position);
      if (this.worldNativeAccess != null) {
         try {
            return this.worldNativeAccess.setBlock(position, block, sideEffects);
         } catch (Exception var5) {
            if (block instanceof BaseBlock && ((BaseBlock)block).getNbt() != null) {
               LOGGER.warn("Tried to set a corrupt tile entity at " + position.toString() + ": " + ((BaseBlock)block).getNbt(), var5);
            } else {
               LOGGER.warn("Failed to set block via adapter, falling back to generic", var5);
            }
         }
      }

      Block bukkitBlock = this.getWorld().getBlockAt(position.x(), position.y(), position.z());
      bukkitBlock.setBlockData(BukkitAdapter.adapt(block), sideEffects.doesApplyAny());
      return true;
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 position) {
      this.testCoords(position);
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      return adapter != null ? adapter.getFullBlock(BukkitAdapter.adapt(this.getWorld(), position)) : this.getBlock(position).toBaseBlock();
   }

   private void testCoords(BlockVector3 position) throws FaweException {
      if (Settings.settings().REGION_RESTRICTIONS_OPTIONS.RESTRICT_TO_SAFE_RANGE) {
         int x = position.x();
         int z = position.z();
         if (x > 30000000 || z > 30000000 || x < -30000000 || z < -30000000) {
            throw FaweCache.OUTSIDE_SAFE_REGION;
         }
      }
   }

   @Override
   public Set<SideEffect> applySideEffects(BlockVector3 position, com.sk89q.worldedit.world.block.BlockState previousType, SideEffectSet sideEffectSet) {
      this.testCoords(position);
      if (this.worldNativeAccess != null) {
         this.worldNativeAccess.applySideEffects(position, previousType, sideEffectSet);
         return Sets.intersection(WorldEditPlugin.getInstance().getInternalPlatform().getSupportedSideEffects(), sideEffectSet.getSideEffectsToApply());
      } else {
         return ImmutableSet.of();
      }
   }

   @Override
   public boolean useItem(BlockVector3 position, BaseItem item, Direction face) {
      this.testCoords(position);
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      return adapter != null ? adapter.simulateItemUse(this.getWorld(), position, item, face) : false;
   }

   @Override
   public boolean fullySupports3DBiomes() {
      return HAS_3D_BIOMES && this.getWorld().getEnvironment() != Environment.NORMAL || PaperLib.isVersion(18);
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      this.testCoords(position);
      return HAS_3D_BIOMES
         ? BukkitAdapter.adapt(this.getWorld().getBiome(position.x(), position.y(), position.z()))
         : BukkitAdapter.adapt(this.getWorld().getBiome(position.x(), position.z()));
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      this.testCoords(position);
      if (HAS_3D_BIOMES) {
         this.getWorld().setBiome(position.x(), position.y(), position.z(), BukkitAdapter.adapt(biome));
      } else {
         this.getWorld().setBiome(position.x(), position.z(), BukkitAdapter.adapt(biome));
      }

      return true;
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      return this.setBlock(BlockVector3.at(x, y, z), block);
   }

   @Override
   public boolean tile(int x, int y, int z, FaweCompoundTag tile) throws WorldEditException {
      return false;
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      return this.setBiome(BlockVector3.at(x, y, z), biome);
   }

   @Override
   public void refreshChunk(int chunkX, int chunkZ) {
      this.testCoords(BlockVector3.at(chunkX << 4, 0, chunkZ << 4));
      this.getWorld().refreshChunk(chunkX, chunkZ);
   }

   @Override
   public IChunkGet get(int chunkX, int chunkZ) {
      this.testCoords(BlockVector3.at(chunkX << 4, 0, chunkZ << 4));
      return WorldEditPlugin.getInstance().getBukkitImplAdapter().get(this.getWorldChecked(), chunkX, chunkZ);
   }

   @Override
   public void sendFakeChunk(Player player, ChunkPacket packet) {
      org.bukkit.entity.Player bukkitPlayer = BukkitAdapter.adapt(player);
      WorldEditPlugin.getInstance().getBukkitImplAdapter().sendFakeChunk(this.getWorld(), bukkitPlayer, packet);
   }

   @Override
   public void flush() {
      if (this.worldNativeAccess != null) {
         this.worldNativeAccess.flush();
      }
   }

   static {
      for (Effect effect : Effect.values()) {
         int id = effect.getId();
         effects.put(id, effect);
      }

      boolean temp;
      try {
         World.class.getMethod("getBiome", int.class, int.class, int.class);
         temp = true;
      } catch (NoSuchMethodException var6) {
         temp = false;
      }

      HAS_3D_BIOMES = temp;

      try {
         World.class.getMethod("getMinHeight");
         temp = true;
      } catch (NoSuchMethodException var5) {
         temp = false;
      }

      HAS_MIN_Y = temp;
      treeTypeMapping = new EnumMap<>(TreeGenerator.TreeType.class);
      generateTreeMap();
      hasWarnedImplError = false;
   }
}
