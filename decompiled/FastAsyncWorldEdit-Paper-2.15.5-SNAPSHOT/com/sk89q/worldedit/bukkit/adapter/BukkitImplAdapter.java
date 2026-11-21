package com.sk89q.worldedit.bukkit.adapter;

import com.fastasyncworldedit.bukkit.FaweBukkit;
import com.fastasyncworldedit.bukkit.adapter.IBukkitAdapter;
import com.fastasyncworldedit.bukkit.adapter.NMSRelighterFactory;
import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.extent.processor.PlacementStateProcessor;
import com.fastasyncworldedit.core.extent.processor.lighting.RelighterFactory;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.implementation.packet.ChunkPacket;
import com.sk89q.jnbt.LinBusConverter;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.internal.wna.WorldNativeAccess;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.generation.TreeType;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTag;

public interface BukkitImplAdapter<T> extends IBukkitAdapter {
   @Nullable
   DataFixer getDataFixer();

   default boolean supportsWatchdog() {
      return false;
   }

   default void tickWatchdog() {
   }

   BlockState getBlock(Location location);

   BaseBlock getFullBlock(Location location);

   WorldNativeAccess<?, ?, ?> createWorldNativeAccess(World world);

   @Nullable
   BaseEntity getEntity(Entity entity);

   @Nullable
   Entity createEntity(Location location, BaseEntity state);

   Component getRichBlockName(BlockType blockType);

   Component getRichItemName(ItemType itemType);

   Component getRichItemName(BaseItemStack itemStack);

   Map<String, ? extends Property<?>> getProperties(BlockType blockType);

   @Deprecated(
      since = "2.15.1"
   )
   void sendFakeNBT(Player player, BlockVector3 pos, LinCompoundTag nbtData);

   void sendFakeNBT(Player player, BlockVector3 pos, TileState tileState, LinCompoundTag nbtData);

   void sendFakeOP(Player player);

   default boolean simulateItemUse(World world, BlockVector3 position, BaseItem item, Direction face) {
      return false;
   }

   boolean canPlaceAt(World world, BlockVector3 position, BlockState blockState);

   @Override
   ItemStack adapt(BaseItemStack item);

   @Override
   BaseItemStack adapt(ItemStack itemStack);

   Set<SideEffect> getSupportedSideEffects();

   default OptionalInt getInternalBlockStateId(BlockData data) {
      return this.getInternalBlockStateId(BukkitAdapter.adapt(data));
   }

   default OptionalInt getInternalBlockStateId(BlockState state) {
      return OptionalInt.empty();
   }

   default boolean regenerate(World world, Region region, Extent extent, RegenOptions options) throws Exception {
      throw new UnsupportedOperationException("This adapter does not support regeneration.");
   }

   default boolean clearContainerBlockContents(World world, BlockVector3 pt) {
      throw new UnsupportedOperationException("This adapter does not support clearing block contents.");
   }

   default void setBiome(Location location, BiomeType biome) {
      throw new UnsupportedOperationException("This adapter does not support custom biomes.");
   }

   default BiomeType getBiome(Location location) {
      throw new UnsupportedOperationException("This adapter does not support custom biomes.");
   }

   default void initializeRegistries() {
   }

   default void sendBiomeUpdates(World world, Iterable<BlockVector2> chunks) {
   }

   default boolean generateTree(TreeType treeType, World world, EditSession session, BlockVector3 pt) throws MaxChangedBlocksException {
      throw new UnsupportedOperationException("This adapter does not support generating features.");
   }

   default boolean generateFeature(ConfiguredFeatureType feature, World world, EditSession session, BlockVector3 pt) {
      throw new UnsupportedOperationException("This adapter does not support generating features.");
   }

   default boolean generateStructure(StructureType feature, World world, EditSession session, BlockVector3 pt) {
      throw new UnsupportedOperationException("This adapter does not support generating features.");
   }

   default BlockMaterial getMaterial(BlockType blockType) {
      return this.getMaterial(blockType.getDefaultState());
   }

   default BlockMaterial getMaterial(BlockState blockState) {
      return null;
   }

   default Collection<String> getRegisteredDefaultBlockStates() {
      ArrayList<String> blocks = new ArrayList<>();

      for (Material m : Material.values()) {
         if (!m.isLegacy() && m.isBlock()) {
            BlockData blockData = m.createBlockData();
            blocks.add(blockData.getAsString());
         }
      }

      return blocks;
   }

   @Deprecated
   default Tag toNative(T foreign) {
      return LinBusConverter.toJnbtTag(this.toNativeLin(foreign));
   }

   default LinTag<?> toNativeLin(T foreign) {
      return this.toNative(foreign).toLinTag();
   }

   @Deprecated
   default T fromNative(Tag foreign) {
      return foreign == null ? null : this.fromNativeLin(foreign.toLinTag());
   }

   default T fromNativeLin(LinTag<?> foreign) {
      return foreign == null ? null : this.fromNative(LinBusConverter.toJnbtTag(foreign));
   }

   @Nullable
   default World createWorld(WorldCreator creator) {
      return Fawe.<FaweBukkit>platform().createWorldUnloaded(creator::createWorld);
   }

   default void sendFakeChunk(World world, Player player, ChunkPacket packet) {
      throw new UnsupportedOperationException("Cannot send fake chunks");
   }

   default IChunkGet get(World world, int chunkX, int chunkZ) {
      throw new UnsupportedOperationException();
   }

   default int getInternalBiomeId(BiomeType biome) {
      return Biome.BADLANDS.ordinal();
   }

   default Iterable<NamespacedKey> getRegisteredBiomes() {
      return Arrays.stream(Biome.values()).<NamespacedKey>map(Keyed::getKey).collect(Collectors.toList());
   }

   default RelighterFactory getRelighterFactory() {
      return new NMSRelighterFactory();
   }

   default Map<String, List<Property<?>>> getAllProperties() {
      return Collections.emptyMap();
   }

   default IBatchProcessor getTickingPostProcessor() {
      return null;
   }

   default PlacementStateProcessor getPlatformPlacementProcessor(Extent extent, BlockTypeMask mask, Region region) {
      return null;
   }
}
