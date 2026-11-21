package org.popcraft.chunky.platform;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;
import org.popcraft.chunky.ChunkyBukkit;
import org.popcraft.chunky.util.Input;

public class BukkitWorld implements World {
   private static final boolean IS_GENERATED_SUPPORTED;
   private static final int TICKING_LOAD_DURATION = Input.tryInteger(System.getProperty("chunky.tickingLoadDuration")).orElse(0);
   private static final boolean AWAIT_TICKET_REMOVAL = Boolean.getBoolean("chunky.awaitTicketRemoval");
   private final JavaPlugin plugin = JavaPlugin.getPlugin(ChunkyBukkit.class);
   private final org.bukkit.World world;
   private final Border worldBorder;

   public BukkitWorld(org.bukkit.World world) {
      this.world = world;
      this.worldBorder = new BukkitBorder(world.getWorldBorder());
   }

   @Override
   public String getName() {
      return this.world.getName();
   }

   @Override
   public String getKey() {
      return this.world.getKey().toString();
   }

   @Override
   public CompletableFuture<Boolean> isChunkGenerated(int x, int z) {
      if (Paper.isPaper()) {
         return CompletableFuture.supplyAsync(() -> {
            try {
               return this.world.isChunkGenerated(x, z);
            } catch (CompletionException var4) {
               return false;
            }
         });
      } else {
         return IS_GENERATED_SUPPORTED
            ? CompletableFuture.completedFuture(this.world.getChunkAt(x, z, false).isGenerated())
            : CompletableFuture.completedFuture(false);
      }
   }

   @Override
   public CompletableFuture<Void> getChunkAtAsync(int x, int z) {
      CompletableFuture<Void> chunkFuture;
      if (Paper.isPaper()) {
         chunkFuture = CompletableFuture.allOf(Paper.getChunkAtAsync(this.world, x, z));
      } else {
         chunkFuture = CompletableFuture.allOf(CompletableFuture.completedFuture(this.world.getChunkAt(x, z)));
      }

      if (TICKING_LOAD_DURATION > 0) {
         CompletableFuture<Void> removeTicketFuture = new CompletableFuture<>();
         chunkFuture.thenAccept(
            ignored -> {
               Runnable addTicketTask = () -> this.world.addPluginChunkTicket(x, z, this.plugin);
               Runnable removeTicketTask = () -> {
                  this.world.removePluginChunkTicket(x, z, this.plugin);
                  removeTicketFuture.complete(null);
               };
               if (Folia.isFolia()) {
                  Location location = new Location(this.world, x << 4, 0.0, z << 4);
                  Folia.schedule(this.plugin, location, addTicketTask);
                  CompletableFuture.runAsync(
                     () -> Folia.schedule(this.plugin, location, removeTicketTask), CompletableFuture.delayedExecutor(TICKING_LOAD_DURATION, TimeUnit.SECONDS)
                  );
               } else {
                  this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, addTicketTask);
                  this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, removeTicketTask, TICKING_LOAD_DURATION * 20L);
               }
            }
         );
         if (AWAIT_TICKET_REMOVAL) {
            return removeTicketFuture;
         }
      }

      return chunkFuture;
   }

   @Override
   public UUID getUUID() {
      return this.world.getUID();
   }

   @Override
   public int getSeaLevel() {
      return this.world.getSeaLevel();
   }

   @Override
   public org.popcraft.chunky.platform.util.Location getSpawn() {
      Location spawnLocation = this.world.getSpawnLocation();
      return new org.popcraft.chunky.platform.util.Location(
         this, spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), spawnLocation.getYaw(), spawnLocation.getPitch()
      );
   }

   @Override
   public Border getWorldBorder() {
      return this.worldBorder;
   }

   @Override
   public int getElevation(int x, int z) {
      Location location = new Location(this.world, x, 0.0, z);
      return Folia.isFolia() && !Folia.isTickThread(location)
         ? CompletableFuture.<Integer>supplyAsync(() -> this.getElevationForLocation(x, z), command -> Folia.schedule(this.plugin, location, command)).join()
         : this.getElevationForLocation(x, z);
   }

   private int getElevationForLocation(int x, int z) {
      int height = this.world.getHighestBlockYAt(x, z) + 1;
      int logicalHeight = this.world.getLogicalHeight();
      if (height >= logicalHeight) {
         Block block = this.world.getBlockAt(x, logicalHeight, z);
         int air = 0;

         while (block.getY() > this.world.getMinHeight()) {
            block = block.getRelative(BlockFace.DOWN);
            Material type = block.getType();
            if (type.isSolid() && air > 1) {
               return block.getY() + 1;
            }

            air = type.isAir() ? air + 1 : 0;
         }
      }

      return height;
   }

   @Override
   public int getMaxElevation() {
      return this.world.getLogicalHeight();
   }

   @Override
   public void playEffect(Player player, String effect) {
      Effect effectType;
      try {
         effectType = Effect.valueOf(effect.toUpperCase());
      } catch (IllegalArgumentException var6) {
         return;
      }

      org.popcraft.chunky.platform.util.Location location = player.getLocation();
      Location bukkitLocation = new Location(this.world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
      this.world.playEffect(bukkitLocation, effectType, 0);
   }

   @Override
   public void playSound(Player player, String sound) {
      Sound soundType;
      try {
         soundType = Sound.valueOf(sound.toUpperCase());
      } catch (IllegalArgumentException var6) {
         return;
      }

      org.popcraft.chunky.platform.util.Location location = player.getLocation();
      Location bukkitLocation = new Location(this.world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
      this.world.playSound(bukkitLocation, soundType, 2.0F, 1.0F);
   }

   @Override
   public Optional<Path> getDirectory(String name) {
      if (name == null) {
         return Optional.empty();
      } else {
         Environment environment = this.world.getEnvironment();
         String parent;
         if (Environment.NETHER.equals(environment)) {
            parent = "DIM-1";
         } else if (Environment.THE_END.equals(environment)) {
            parent = "DIM1";
         } else {
            parent = "";
         }

         Path directory = this.world.getWorldFolder().toPath().resolve(parent).normalize().resolve(name);
         return Files.isDirectory(directory) ? Optional.of(directory) : Optional.empty();
      }
   }

   static {
      boolean isGeneratedSupported;
      try {
         Chunk.class.getMethod("isGenerated");
         isGeneratedSupported = true;
      } catch (NoSuchMethodException var2) {
         isGeneratedSupported = false;
      }

      IS_GENERATED_SUPPORTED = isGeneratedSupported;
   }
}
