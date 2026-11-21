package com.sk89q.worldedit.bukkit;

import com.fastasyncworldedit.bukkit.adapter.IBukkitAdapter;
import com.fastasyncworldedit.bukkit.adapter.SimpleBukkitAdapter;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.PlayerProxy;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.item.ItemType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum BukkitAdapter {
   INSTANCE;

   private final IBukkitAdapter adapter;
   private static final ParserContext TO_BLOCK_CONTEXT = new ParserContext();
   private static final Map<Biome, BiomeType> biomeBiomeTypeCache = new ConcurrentHashMap<>();
   private static final Map<BiomeType, Biome> biomeTypeBiomeCache = new ConcurrentHashMap<>();
   private static final Map<Material, BlockType> materialBlockTypeCache = new ConcurrentHashMap<>();
   private static final Map<Material, ItemType> materialItemTypeCache = new ConcurrentHashMap<>();
   private static final Int2ObjectMap<BlockState> blockStateCache = new Int2ObjectOpenHashMap();
   private static final Map<String, BlockState> blockStateStringCache = new HashMap<>();
   private static final Int2ObjectMap<BlockData> blockDataCache = new Int2ObjectOpenHashMap();

   private BukkitAdapter() {
      BukkitImplAdapter tmp = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      this.adapter = Objects.requireNonNullElseGet(tmp, SimpleBukkitAdapter::new);
   }

   private static IBukkitAdapter getAdapter() {
      return INSTANCE.adapter;
   }

   public static boolean equals(BlockType blockType, Material type) {
      return getAdapter().equals(blockType, type);
   }

   public static BukkitWorld asBukkitWorld(World world) {
      return getAdapter().asBukkitWorld(world);
   }

   public static World adapt(org.bukkit.World world) {
      return getAdapter().adapt(world);
   }

   public static Actor adapt(CommandSender sender) {
      return WorldEditPlugin.getInstance().wrapCommandSender(sender);
   }

   public static BukkitPlayer adapt(Player player) {
      return WorldEditPlugin.getInstance().wrapPlayer(player);
   }

   public static CommandSender adapt(Actor actor) {
      if (actor instanceof com.sk89q.worldedit.entity.Player) {
         return adapt((com.sk89q.worldedit.entity.Player)actor);
      } else {
         return (CommandSender)(actor instanceof BukkitBlockCommandSender
            ? ((BukkitBlockCommandSender)actor).getSender()
            : ((BukkitCommandSender)actor).getSender());
      }
   }

   public static Player adapt(com.sk89q.worldedit.entity.Player player) {
      player = PlayerProxy.unwrap(player);
      return player == null ? null : ((BukkitPlayer)player).getPlayer();
   }

   public static Direction adapt(@Nullable BlockFace face) {
      if (face == null) {
         return null;
      } else {
         switch (face) {
            case NORTH:
               return Direction.NORTH;
            case SOUTH:
               return Direction.SOUTH;
            case WEST:
               return Direction.WEST;
            case EAST:
               return Direction.EAST;
            case DOWN:
               return Direction.DOWN;
            case UP:
            default:
               return Direction.UP;
         }
      }
   }

   public static org.bukkit.World adapt(World world) {
      return getAdapter().adapt(world);
   }

   public static Location adapt(org.bukkit.Location location) {
      Preconditions.checkNotNull(location);
      Vector3 position = asVector(location);
      return new Location(adapt(location.getWorld()), position, location.getYaw(), location.getPitch());
   }

   public static org.bukkit.Location adapt(Location location) {
      Preconditions.checkNotNull(location);
      return new org.bukkit.Location(adapt((World)location.getExtent()), location.x(), location.y(), location.z(), location.getYaw(), location.getPitch());
   }

   public static org.bukkit.Location adapt(org.bukkit.World world, Vector3 position) {
      Preconditions.checkNotNull(world);
      Preconditions.checkNotNull(position);
      return new org.bukkit.Location(world, position.x(), position.y(), position.z());
   }

   public static org.bukkit.Location adapt(org.bukkit.World world, BlockVector3 position) {
      Preconditions.checkNotNull(world);
      Preconditions.checkNotNull(position);
      return new org.bukkit.Location(world, position.x(), position.y(), position.z());
   }

   public static org.bukkit.Location adapt(org.bukkit.World world, Location location) {
      Preconditions.checkNotNull(world);
      Preconditions.checkNotNull(location);
      return new org.bukkit.Location(world, location.x(), location.y(), location.z(), location.getYaw(), location.getPitch());
   }

   public static Vector3 asVector(org.bukkit.Location location) {
      Preconditions.checkNotNull(location);
      return Vector3.at(location.getX(), location.getY(), location.getZ());
   }

   public static BlockVector3 asBlockVector(org.bukkit.Location location) {
      Preconditions.checkNotNull(location);
      return BlockVector3.at(location.getX(), location.getY(), location.getZ());
   }

   public static Entity adapt(org.bukkit.entity.Entity entity) {
      return getAdapter().adapt(entity);
   }

   public static Material adapt(ItemType itemType) {
      return getAdapter().adapt(itemType);
   }

   public static Material adapt(BlockType blockType) {
      return getAdapter().adapt(blockType);
   }

   public static GameMode adapt(org.bukkit.GameMode gameMode) {
      return getAdapter().adapt(gameMode);
   }

   public static BiomeType adapt(Biome biome) {
      return getAdapter().adapt(biome);
   }

   public static Biome adapt(BiomeType biomeType) {
      return getAdapter().adapt(biomeType);
   }

   public static EntityType adapt(org.bukkit.entity.EntityType entityType) {
      return getAdapter().adapt(entityType);
   }

   public static org.bukkit.entity.EntityType adapt(EntityType entityType) {
      return getAdapter().adapt(entityType);
   }

   @Nullable
   public static BlockType asBlockType(Material material) {
      return getAdapter().asBlockType(material);
   }

   @Nullable
   public static ItemType asItemType(Material material) {
      return getAdapter().asItemType(material);
   }

   public static BlockState adapt(@Nonnull BlockData blockData) {
      return getAdapter().adapt(blockData);
   }

   public static BlockData adapt(@Nonnull BlockStateHolder block) {
      return getAdapter().adapt(block);
   }

   public static BlockState asBlockState(ItemStack itemStack) throws WorldEditException {
      return getAdapter().asBlockState(itemStack);
   }

   public static BaseItemStack adapt(ItemStack itemStack) {
      return getAdapter().adapt(itemStack);
   }

   public static ItemStack adapt(BaseItemStack item) {
      return getAdapter().adapt(item);
   }

   static {
      TO_BLOCK_CONTEXT.setRestricted(false);
      TO_BLOCK_CONTEXT.setTryLegacy(false);
   }
}
