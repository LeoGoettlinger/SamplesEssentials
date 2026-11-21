package com.fastasyncworldedit.bukkit.adapter;

import com.fastasyncworldedit.core.queue.implementation.packet.ChunkPacket;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import java.util.Collection;
import java.util.Map;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTag;

public interface IDelegateBukkitImplAdapter<T> extends BukkitImplAdapter<T> {
   BukkitImplAdapter<T> getParent();

   @Nullable
   @Override
   default DataFixer getDataFixer() {
      return this.getParent().getDataFixer();
   }

   @Override
   default boolean supportsWatchdog() {
      return this.getParent().supportsWatchdog();
   }

   @Override
   default void tickWatchdog() {
      this.getParent().tickWatchdog();
   }

   @Override
   default BlockState getBlock(Location location) {
      return this.getParent().getBlock(location);
   }

   @Nullable
   @Override
   default BaseEntity getEntity(Entity entity) {
      return this.getParent().getEntity(entity);
   }

   @Nullable
   @Override
   default Entity createEntity(Location location, BaseEntity state) {
      return this.getParent().createEntity(location, state);
   }

   @Override
   default Map<String, ? extends Property<?>> getProperties(BlockType blockType) {
      return this.getParent().getProperties(blockType);
   }

   @Override
   default void sendFakeNBT(Player player, BlockVector3 pos, LinCompoundTag nbtData) {
      this.getParent().sendFakeNBT(player, pos, nbtData);
   }

   @Override
   default void sendFakeNBT(Player player, BlockVector3 pos, TileState tileState, LinCompoundTag nbtData) {
      this.getParent().sendFakeNBT(player, pos, tileState, nbtData);
   }

   @Override
   default void sendFakeOP(Player player) {
      this.getParent().sendFakeOP(player);
   }

   @Override
   default boolean simulateItemUse(World world, BlockVector3 position, BaseItem item, Direction face) {
      return this.getParent().simulateItemUse(world, position, item, face);
   }

   @Override
   default ItemStack adapt(BaseItemStack item) {
      return this.getParent().adapt(item);
   }

   @Override
   default BaseItemStack adapt(ItemStack itemStack) {
      return this.getParent().adapt(itemStack);
   }

   @Override
   default OptionalInt getInternalBlockStateId(BlockData data) {
      return this.getParent().getInternalBlockStateId(data);
   }

   @Override
   default OptionalInt getInternalBlockStateId(BlockState state) {
      return this.getParent().getInternalBlockStateId(state);
   }

   @Override
   default boolean clearContainerBlockContents(World world, BlockVector3 pt) {
      return this.getParent().clearContainerBlockContents(world, pt);
   }

   @Override
   default void setBiome(Location location, BiomeType biome) {
      this.getParent().setBiome(location, biome);
   }

   @Override
   default BiomeType getBiome(Location location) {
      return this.getParent().getBiome(location);
   }

   @Override
   default void sendBiomeUpdates(World world, Iterable<BlockVector2> chunks) {
      this.getParent().sendBiomeUpdates(world, chunks);
   }

   @Override
   default Collection<String> getRegisteredDefaultBlockStates() {
      return this.getParent().getRegisteredDefaultBlockStates();
   }

   @Override
   default BlockMaterial getMaterial(BlockType blockType) {
      return this.getParent().getMaterial(blockType);
   }

   @Override
   default BlockMaterial getMaterial(BlockState blockState) {
      return this.getParent().getMaterial(blockState);
   }

   @Override
   default Tag toNative(T foreign) {
      return this.getParent().toNative(foreign);
   }

   @Override
   default LinTag<?> toNativeLin(T foreign) {
      return this.getParent().toNativeLin(foreign);
   }

   @Override
   default T fromNative(Tag foreign) {
      return this.getParent().fromNative(foreign);
   }

   @Override
   default T fromNativeLin(LinTag foreign) {
      return this.getParent().fromNativeLin(foreign);
   }

   @Nullable
   @Override
   default World createWorld(WorldCreator creator) {
      return this.getParent().createWorld(creator);
   }

   @Override
   default void sendFakeChunk(World world, Player player, ChunkPacket packet) {
      this.getParent().sendFakeChunk(world, player, packet);
   }

   @Override
   default BukkitWorld asBukkitWorld(com.sk89q.worldedit.world.World world) {
      return this.getParent().asBukkitWorld(world);
   }

   @Override
   default World adapt(com.sk89q.worldedit.world.World world) {
      return this.getParent().adapt(world);
   }

   @Override
   default Location adapt(World world, Vector3 position) {
      return this.getParent().adapt(world, position);
   }

   @Override
   default Location adapt(World world, BlockVector3 position) {
      return this.getParent().adapt(world, position);
   }

   @Override
   default Location adapt(World world, com.sk89q.worldedit.util.Location location) {
      return this.getParent().adapt(world, location);
   }

   @Override
   default Vector3 asVector(Location location) {
      return this.getParent().asVector(location);
   }

   @Override
   default BlockVector3 asBlockVector(Location location) {
      return this.getParent().asBlockVector(location);
   }

   @Override
   default com.sk89q.worldedit.entity.Entity adapt(Entity entity) {
      return this.getParent().adapt(entity);
   }

   @Override
   default Material adapt(ItemType itemType) {
      return this.getParent().adapt(itemType);
   }

   @Override
   default Material adapt(BlockType blockType) {
      return this.getParent().adapt(blockType);
   }

   @Override
   default EntityType adapt(com.sk89q.worldedit.world.entity.EntityType entityType) {
      return this.getParent().adapt(entityType);
   }

   @Override
   default BlockType asBlockType(Material material) {
      return this.getParent().asBlockType(material);
   }

   @Override
   default ItemType asItemType(Material material) {
      return this.getParent().asItemType(material);
   }

   @Override
   default BlockState adapt(BlockData blockData) {
      return this.getParent().adapt(blockData);
   }

   @Override
   default <B extends BlockStateHolder<B>> BlockData adapt(B block) {
      return this.getParent().adapt(block);
   }

   @Override
   default BukkitPlayer adapt(Player player) {
      return this.getParent().adapt(player);
   }

   @Override
   default Player adapt(com.sk89q.worldedit.entity.Player player) {
      return this.getParent().adapt(player);
   }

   @Override
   default Biome adapt(BiomeType biomeType) {
      return this.getParent().adapt(biomeType);
   }

   @Override
   default BiomeType adapt(Biome biome) {
      return this.getParent().adapt(biome);
   }

   @Override
   default boolean equals(BlockType blockType, Material type) {
      return this.getParent().equals(blockType, type);
   }

   @Override
   default com.sk89q.worldedit.world.World adapt(World world) {
      return this.getParent().adapt(world);
   }

   @Override
   default GameMode adapt(org.bukkit.GameMode gameMode) {
      return this.getParent().adapt(gameMode);
   }

   @Override
   default com.sk89q.worldedit.world.entity.EntityType adapt(EntityType entityType) {
      return this.getParent().adapt(entityType);
   }

   @Override
   default BlockState asBlockState(ItemStack itemStack) {
      return this.getParent().asBlockState(itemStack);
   }
}
