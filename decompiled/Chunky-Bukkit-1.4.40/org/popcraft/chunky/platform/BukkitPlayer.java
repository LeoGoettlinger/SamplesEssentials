package org.popcraft.chunky.platform;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.platform.util.Location;
import org.popcraft.chunky.util.Translator;

public class BukkitPlayer extends BukkitSender implements Player {
   private static final boolean ACTION_BAR_SUPPORTED;
   private final JavaPlugin plugin = JavaPlugin.getPlugin(ChunkyBukkit.class);
   final org.bukkit.entity.Player player;

   public BukkitPlayer(org.bukkit.entity.Player player) {
      super(player);
      this.player = player;
   }

   @Override
   public boolean isPlayer() {
      return true;
   }

   @Override
   public String getName() {
      return this.player.getName();
   }

   @Override
   public World getWorld() {
      return new BukkitWorld(this.player.getWorld());
   }

   @Override
   public Location getLocation() {
      org.bukkit.Location location = this.player.getLocation();
      return new Location(this.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
   }

   @Override
   public UUID getUUID() {
      return this.player.getUniqueId();
   }

   @Override
   public void teleport(Location location) {
      org.bukkit.World world = Bukkit.getWorld(location.getWorld().getName());
      org.bukkit.Location loc = new org.bukkit.Location(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
      Entity vehicle = this.player.getVehicle();
      if (vehicle == null) {
         this.teleportAsync(this.player, loc);
      } else if (Paper.isPaper() && this.player.getWorld().equals(loc.getWorld())) {
         Paper.teleportAsyncWithPassengers(vehicle, loc);
      } else if (Folia.isFolia() && !Folia.isTickThread(this.player.getLocation())) {
         Folia.schedule(this.plugin, this.player, () -> this.teleport(location), 1L);
      } else {
         List<Entity> passengers = vehicle.getPassengers();
         if (Folia.isFolia()) {
            Folia.schedule(this.plugin, vehicle, vehicle::eject, 1L);
         } else {
            vehicle.eject();
         }

         this.teleportAsync(this.player, loc).thenAcceptAsync(ignored -> {
            this.teleportAsync(vehicle, loc);

            for (Entity passenger : passengers) {
               this.teleportAsync(passenger, loc);
               if (passenger instanceof org.bukkit.entity.Player playerPassenger) {
                  playerPassenger.hideEntity(this.plugin, vehicle);
                  playerPassenger.showEntity(this.plugin, vehicle);
               }

               if (Folia.isFolia()) {
                  Folia.schedule(this.plugin, vehicle, () -> vehicle.addPassenger(passenger), 1L);
               } else {
                  vehicle.addPassenger(passenger);
               }
            }
         }, command -> {
            if (Folia.isFolia()) {
               Folia.schedule(this.plugin, this.player, command, 1L);
            } else {
               this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, command, 1L);
            }
         });
      }
   }

   private CompletableFuture<Boolean> teleportAsync(Entity entity, org.bukkit.Location location) {
      return Paper.isPaper() ? Paper.teleportAsync(entity, location) : CompletableFuture.completedFuture(entity.teleport(location));
   }

   @Override
   public void sendActionBar(String key) {
      if (ACTION_BAR_SUPPORTED) {
         String message = this.formatColored(Translator.translateKey(key, false));
         this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
      } else {
         this.sendMessage(key, new Object[0]);
      }
   }

   static {
      boolean barSupported;
      try {
         org.bukkit.entity.Player.class.getMethod("spigot");
         barSupported = true;
      } catch (NoSuchMethodException var2) {
         barSupported = false;
      }

      ACTION_BAR_SUPPORTED = barSupported;
   }
}
