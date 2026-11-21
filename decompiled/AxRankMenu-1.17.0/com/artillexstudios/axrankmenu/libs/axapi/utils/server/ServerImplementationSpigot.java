package com.artillexstudios.axrankmenu.libs.axapi.utils.server;

import com.artillexstudios.axrankmenu.libs.axapi.utils.PaperUtils;
import com.artillexstudios.axrankmenu.libs.axapi.utils.StringUtils;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ServerImplementationSpigot implements ServerImplementation {
   @Override
   public CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, TeleportCause teleportCause) {
      return CompletableFuture.completedFuture(entity.teleport(location, teleportCause));
   }

   @Override
   public CompletableFuture<Chunk> getChunkAtAsync(World world, int x, int z, boolean gen, boolean isUrgent) {
      return !gen && !PaperUtils.isChunkGenerated(world, x, z)
         ? CompletableFuture.completedFuture(null)
         : CompletableFuture.completedFuture(world.getChunkAt(x, z));
   }

   @Override
   public boolean isChunkGenerated(World world, int x, int z) {
      return true;
   }

   @Override
   public InventoryHolder getHolder(Inventory inventory, boolean useSnapshot) {
      return inventory.getHolder();
   }

   @Override
   public Inventory createInventory(InventoryHolder holder, int size, Component title) {
      return Bukkit.createInventory(holder, size, StringUtils.formatToString((String)StringUtils.MINI_MESSAGE.serialize(title)));
   }

   @Override
   public Inventory createInventory(InventoryHolder holder, InventoryType type, Component title) {
      return Bukkit.createInventory(holder, type, StringUtils.formatToString((String)StringUtils.MINI_MESSAGE.serialize(title)));
   }
}
