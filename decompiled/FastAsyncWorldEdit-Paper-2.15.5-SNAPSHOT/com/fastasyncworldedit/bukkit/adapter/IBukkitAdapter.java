package com.fastasyncworldedit.bukkit.adapter;

import com.fastasyncworldedit.bukkit.util.BukkitItemStack;
import com.fastasyncworldedit.core.util.TaskManager;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.NotABlockException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitEntity;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.EditSessionBlockChangeDelegate;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IBukkitAdapter {
   default BukkitWorld asBukkitWorld(World world) {
      if (world instanceof BukkitWorld) {
         return (BukkitWorld)world;
      } else {
         BukkitWorld bukkitWorld = WorldEditPlugin.getInstance().getInternalPlatform().matchWorld(world);
         if (bukkitWorld == null) {
            throw new RuntimeException("World '" + world.getName() + "' has no matching version in Bukkit");
         } else {
            return bukkitWorld;
         }
      }
   }

   default org.bukkit.World adapt(World world) {
      Preconditions.checkNotNull(world);
      if (world instanceof BukkitWorld) {
         return ((BukkitWorld)world).getWorld();
      } else {
         org.bukkit.World match = Bukkit.getServer().getWorld(world.getName());
         if (match != null) {
            return match;
         } else {
            throw new IllegalArgumentException("Can't find a Bukkit world for " + world);
         }
      }
   }

   default Location adapt(org.bukkit.World world, Vector3 position) {
      Preconditions.checkNotNull(world);
      Preconditions.checkNotNull(position);
      return new Location(world, position.x(), position.y(), position.z());
   }

   default Location adapt(org.bukkit.World world, BlockVector3 position) {
      return this.adapt(world, position.toVector3());
   }

   default Location adapt(org.bukkit.World world, com.sk89q.worldedit.util.Location location) {
      Preconditions.checkNotNull(world);
      Preconditions.checkNotNull(location);
      return new Location(world, location.x(), location.y(), location.z(), location.getYaw(), location.getPitch());
   }

   default Vector3 asVector(Location location) {
      Preconditions.checkNotNull(location);
      return Vector3.at(location.getX(), location.getY(), location.getZ());
   }

   default BlockVector3 asBlockVector(Location location) {
      Preconditions.checkNotNull(location);
      return BlockVector3.at(location.getX(), location.getY(), location.getZ());
   }

   default Entity adapt(org.bukkit.entity.Entity entity) {
      Preconditions.checkNotNull(entity);
      return new BukkitEntity(entity);
   }

   default Material adapt(ItemType itemType) {
      Preconditions.checkNotNull(itemType);
      NamespacedKey key = (NamespacedKey)Preconditions.checkNotNull(NamespacedKey.fromString(itemType.id()), "Item type key is invalid");
      return (Material)Registry.MATERIAL.get(key);
   }

   default Material adapt(BlockType blockType) {
      Preconditions.checkNotNull(blockType);
      NamespacedKey key = (NamespacedKey)Preconditions.checkNotNull(NamespacedKey.fromString(blockType.id()), "Block type key is invalid");
      return (Material)Registry.MATERIAL.get(key);
   }

   default EntityType adapt(com.sk89q.worldedit.world.entity.EntityType entityType) {
      NamespacedKey entityKey = NamespacedKey.fromString(entityType.toString());
      if (entityKey == null) {
         throw new IllegalArgumentException("Entity key '" + entityType + "' does not map to Bukkit");
      } else {
         return (EntityType)Registry.ENTITY_TYPE.get(entityKey);
      }
   }

   default BlockType asBlockType(Material material) {
      Preconditions.checkNotNull(material);
      if (!material.isBlock()) {
         throw new IllegalArgumentException(material.getKey() + " is not a block!") {
            @Override
            public synchronized Throwable fillInStackTrace() {
               return this;
            }
         };
      } else {
         return BlockTypes.get(material.getKey().toString());
      }
   }

   default ItemType asItemType(Material material) {
      return ItemTypes.get(material.getKey().toString());
   }

   default BlockState adapt(BlockData blockData) {
      String id = blockData.getAsString();
      return BlockState.get(id);
   }

   default <B extends BlockStateHolder<B>> BlockData adapt(B block) {
      return Bukkit.createBlockData(block.getAsString());
   }

   default BaseItemStack adapt(ItemStack itemStack) {
      Preconditions.checkNotNull(itemStack);
      return new BukkitItemStack(itemStack);
   }

   default ItemStack adapt(BaseItemStack item) {
      Preconditions.checkNotNull(item);
      return item instanceof BukkitItemStack ? ((BukkitItemStack)item).getBukkitItemStack() : new ItemStack(this.adapt(item.getType()), item.getAmount());
   }

   default BukkitPlayer adapt(Player player) {
      return WorldEditPlugin.getInstance().wrapPlayer(player);
   }

   default Player adapt(com.sk89q.worldedit.entity.Player player) {
      return ((BukkitPlayer)player).getPlayer();
   }

   default Biome adapt(BiomeType biomeType) {
      if (!biomeType.id().startsWith("minecraft:")) {
         throw new IllegalArgumentException("Bukkit only supports vanilla biomes");
      } else {
         try {
            return Biome.valueOf(biomeType.id().substring(10).toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var3) {
            return null;
         }
      }
   }

   default BiomeType adapt(Biome biome) {
      return BiomeTypes.get(biome.name().toLowerCase(Locale.ROOT));
   }

   default boolean equals(BlockType blockType, Material type) {
      return blockType == this.asItemType(type).getBlockType();
   }

   default World adapt(org.bukkit.World world) {
      Preconditions.checkNotNull(world);
      return new BukkitWorld(world);
   }

   default GameMode adapt(org.bukkit.GameMode gameMode) {
      Preconditions.checkNotNull(gameMode);
      return GameModes.get(gameMode.name().toLowerCase(Locale.ROOT));
   }

   default com.sk89q.worldedit.world.entity.EntityType adapt(EntityType entityType) {
      return EntityTypes.get(entityType.getKey().toString());
   }

   default BlockState asBlockState(ItemStack itemStack) {
      Preconditions.checkNotNull(itemStack);
      if (itemStack.getType().isBlock()) {
         return this.adapt(itemStack.getType().createBlockData());
      } else {
         throw new NotABlockException();
      }
   }

   default boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, BlockVector3 pt, org.bukkit.World world) {
      TreeType bukkitType = BukkitWorld.toBukkitTreeType(type);
      if (bukkitType == TreeType.CHORUS_PLANT) {
         pt = pt.add(0, 1, 0);
      }

      return type != null && world.generateTree(BukkitAdapter.adapt(world, pt), bukkitType, new EditSessionBlockChangeDelegate(editSession));
   }

   default List<org.bukkit.entity.Entity> getEntities(org.bukkit.World world) {
      return TaskManager.taskManager().sync(world::getEntities);
   }

   default void setupFeatures() {
   }
}
