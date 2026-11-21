package com.artillexstudios.axafkzone.libs.axapi.utils;

import com.artillexstudios.axafkzone.libs.axapi.reflection.ClassUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.server.ServerImplementation;
import com.artillexstudios.axafkzone.libs.axapi.utils.server.ServerImplementationPaper;
import com.artillexstudios.axafkzone.libs.axapi.utils.server.ServerImplementationSpigot;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class PaperUtils {
   private static final ServerImplementation IMPLEMENTATION;
   private static final boolean FOLIA;

   public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
      return IMPLEMENTATION.teleportAsync(entity, location, TeleportCause.PLUGIN);
   }

   public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, TeleportCause cause) {
      return IMPLEMENTATION.teleportAsync(entity, location, cause);
   }

   public static CompletableFuture<Chunk> getChunkAtAsync(Location location) {
      return IMPLEMENTATION.getChunkAtAsync(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, true);
   }

   public static CompletableFuture<Chunk> getChunkAtAsync(Location location, boolean gen) {
      return IMPLEMENTATION.getChunkAtAsync(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, gen);
   }

   public static CompletableFuture<Chunk> getChunkAtAsync(World world, int x, int z) {
      return IMPLEMENTATION.getChunkAtAsync(world, x, z, true);
   }

   public static CompletableFuture<Chunk> getChunkAtAsync(World world, int x, int z, boolean gen) {
      return IMPLEMENTATION.getChunkAtAsync(world, x, z, gen, false);
   }

   public static CompletableFuture<Chunk> getChunkAtAsync(World world, int x, int z, boolean gen, boolean isUrgent) {
      return IMPLEMENTATION.getChunkAtAsync(world, x, z, gen, isUrgent);
   }

   public static CompletableFuture<Chunk> getChunkAtAsyncUrgently(World world, int x, int z, boolean gen) {
      return IMPLEMENTATION.getChunkAtAsync(world, x, z, gen, true);
   }

   public static boolean isChunkGenerated(Location location) {
      return isChunkGenerated(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
   }

   public static boolean isChunkGenerated(World world, int x, int z) {
      return IMPLEMENTATION.isChunkGenerated(world, x, z);
   }

   public static InventoryHolder getHolder(Inventory inventory, boolean useSnapshot) {
      return IMPLEMENTATION.getHolder(inventory, useSnapshot);
   }

   public static Inventory createInventory(InventoryHolder holder, int size, Component title) {
      return IMPLEMENTATION.createInventory(holder, size, title);
   }

   public static Inventory createInventory(InventoryHolder holder, InventoryType type, Component title) {
      return IMPLEMENTATION.createInventory(holder, type, title);
   }

   public static boolean isPaper() {
      return IMPLEMENTATION instanceof ServerImplementationPaper;
   }

   public static boolean isFolia() {
      return FOLIA;
   }

   static {
      if (!ClassUtils.INSTANCE.classExists("io.papermc.paper.configuration.Configuration")
         && !ClassUtils.INSTANCE.classExists("com.destroystokyo.paper.PaperConfig")) {
         IMPLEMENTATION = new ServerImplementationSpigot();
      } else {
         IMPLEMENTATION = new ServerImplementationPaper();
      }

      FOLIA = ClassUtils.INSTANCE.classExists("io.papermc.paper.threadedregions.RegionizedServer");
   }
}
