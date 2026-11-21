package com.earth2me.essentials;

import com.earth2me.essentials.config.ConfigurateUtil;
import com.earth2me.essentials.config.EssentialsConfiguration;
import com.earth2me.essentials.config.entities.LazyLocation;
import com.earth2me.essentials.paperlib.PaperLib;
import com.earth2me.essentials.utils.LocationUtil;
import com.earth2me.essentials.utils.VersionUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.ess3.provider.BiomeKeyProvider;
import net.ess3.provider.BiomeNameProvider;
import net.ess3.provider.WorldInfoProvider;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;

public class RandomTeleport implements IConf {
   private static final Random RANDOM = new Random();
   private static final int HIGHEST_BLOCK_Y_OFFSET = VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_15_R01) ? 1 : 0;
   private final IEssentials ess;
   private final EssentialsConfiguration config;
   private final Map<String, ConcurrentLinkedQueue<Location>> cachedLocations = new HashMap<>();
   private WorldInfoProvider worldInfoProvider;

   public RandomTeleport(final IEssentials essentials) {
      this.ess = essentials;
      this.config = new EssentialsConfiguration(
         new File(essentials.getDataFolder(), "tpr.yml"),
         "/tpr.yml",
         "Configuration for the random teleport command.\nUse the /settpr command in-game to set random teleport locations."
      );
   }

   public EssentialsConfiguration getConfig() {
      return this.config;
   }

   @Override
   public void reloadConfig() {
      this.worldInfoProvider = this.ess.provider(WorldInfoProvider.class);
      this.config.load();
      this.cachedLocations.clear();
   }

   public boolean hasLocation(final String name) {
      return this.config.hasProperty("locations." + name);
   }

   public Location getCenter(final String name) {
      LazyLocation center = this.config.getLocation(this.locationKey(name, "center"));
      if (center != null && center.location() != null) {
         return center.location();
      } else {
         Location worldCenter = ((World)this.ess.getServer().getWorlds().get(0)).getWorldBorder().getCenter();
         worldCenter.setY(worldCenter.getWorld().getHighestBlockYAt(worldCenter) + HIGHEST_BLOCK_Y_OFFSET);
         this.setCenter(name, worldCenter);
         return worldCenter;
      }
   }

   public void setCenter(final String name, final Location center) {
      this.config.setProperty(this.locationKey(name, "center"), center);
      this.config.save();
      this.getCachedLocations(name).clear();
   }

   public double getMinRange(final String name) {
      return this.config.getDouble(this.locationKey(name, "min-range"), 0.0);
   }

   public void setMinRange(final String name, final double minRange) {
      this.config.setProperty(this.locationKey(name, "min-range"), minRange);
      this.config.save();
      this.getCachedLocations(name).clear();
   }

   public double getMaxRange(final String name) {
      return this.config.getDouble(this.locationKey(name, "max-range"), this.getCenter(name).getWorld().getWorldBorder().getSize() / 2.0);
   }

   public void setMaxRange(final String name, final double maxRange) {
      this.config.setProperty(this.locationKey(name, "max-range"), maxRange);
      this.config.save();
      this.getCachedLocations(name).clear();
   }

   public String getDefaultLocation() {
      return this.config.getString("default-location", "{world}");
   }

   public boolean isPerLocationPermission() {
      return this.config.getBoolean("per-location-permission", false);
   }

   public Set<String> getExcludedBiomes() {
      Set<String> excludedBiomes = new HashSet<>();

      for (String key : this.config.getList("excluded-biomes", String.class)) {
         excludedBiomes.add(key.toLowerCase());
      }

      return excludedBiomes;
   }

   public int getFindAttempts() {
      return this.config.getInt("find-attempts", 10);
   }

   public int getCacheThreshold() {
      return this.config.getInt("cache-threshold", 10);
   }

   public List<String> listLocations() {
      return new ArrayList<>(ConfigurateUtil.getKeys(this.config.getRootNode().node(new Object[]{"locations"})));
   }

   public Queue<Location> getCachedLocations(final String name) {
      this.cachedLocations.computeIfAbsent(name, x -> new ConcurrentLinkedQueue<>());
      return this.cachedLocations.get(name);
   }

   public CompletableFuture<Location> getRandomLocation(final String name) {
      Queue<Location> cached = this.getCachedLocations(name);
      if (cached.size() < this.getCacheThreshold()) {
         this.cacheRandomLocations(name);
      }

      CompletableFuture<Location> future = new CompletableFuture<>();
      if (cached.isEmpty()) {
         int findAttempts = this.getFindAttempts();
         Location center = this.getCenter(name);
         double minRange = this.getMinRange(name);
         double maxRange = this.getMaxRange(name);
         this.attemptRandomLocation(findAttempts, center, minRange, maxRange).thenAccept(future::complete);
      } else {
         future.complete(cached.poll());
      }

      return future;
   }

   public CompletableFuture<Location> getRandomLocation(final Location center, final double minRange, final double maxRange) {
      return this.attemptRandomLocation(this.getFindAttempts(), center, minRange, maxRange);
   }

   public void cacheRandomLocations(final String name) {
      this.ess.getServer().getScheduler().scheduleSyncDelayedTask(this.ess, () -> {
         for (int i = 0; i < this.getFindAttempts(); i++) {
            this.calculateRandomLocation(this.getCenter(name), this.getMinRange(name), this.getMaxRange(name)).thenAccept(location -> {
               if (this.isValidRandomLocation(location)) {
                  this.getCachedLocations(name).add(location);
               }
            });
         }
      });
   }

   private CompletableFuture<Location> attemptRandomLocation(final int attempts, final Location center, final double minRange, final double maxRange) {
      CompletableFuture<Location> future = new CompletableFuture<>();
      if (attempts > 0) {
         this.calculateRandomLocation(center, minRange, maxRange).thenAccept(location -> {
            if (this.isValidRandomLocation(location)) {
               future.complete(location);
            } else {
               this.attemptRandomLocation(attempts - 1, center, minRange, maxRange).thenAccept(future::complete);
            }
         });
      } else {
         future.complete(center);
      }

      return future;
   }

   private CompletableFuture<Location> calculateRandomLocation(final Location center, final double minRange, final double maxRange) {
      CompletableFuture<Location> future = new CompletableFuture<>();
      double rectX = RANDOM.nextDouble() * (maxRange - minRange) + minRange;
      double rectZ = RANDOM.nextDouble() * (maxRange + minRange) - minRange;
      int transform = RANDOM.nextInt(4);
      double offsetX;
      double offsetZ;
      if (transform == 0) {
         offsetX = rectX;
         offsetZ = rectZ;
      } else if (transform == 1) {
         offsetX = -rectZ;
         offsetZ = rectX;
      } else if (transform == 2) {
         offsetX = -rectX;
         offsetZ = -rectZ;
      } else {
         offsetX = rectZ;
         offsetZ = -rectX;
      }

      Location location = new Location(
         center.getWorld(),
         center.getX() + offsetX,
         this.worldInfoProvider.getMaxHeight(center.getWorld()),
         center.getZ() + offsetZ,
         360.0F * RANDOM.nextFloat() - 180.0F,
         0.0F
      );
      PaperLib.getChunkAtAsync(location).thenAccept(chunk -> {
         if (Environment.NETHER.equals(center.getWorld().getEnvironment())) {
            location.setY(this.getNetherYAt(location));
         } else {
            location.setY(center.getWorld().getHighestBlockYAt(location) + HIGHEST_BLOCK_Y_OFFSET);
         }

         future.complete(location);
      });
      return future;
   }

   private double getNetherYAt(final Location location) {
      World world = location.getWorld();

      for (int y = 32;
         y < this.worldInfoProvider.getMaxHeight(world) && !Material.BEDROCK.equals(world.getBlockAt(location.getBlockX(), y, location.getBlockZ()).getType());
         y++
      ) {
         if (!LocationUtil.isBlockUnsafe(this.ess, world, location.getBlockX(), y, location.getBlockZ())) {
            return y;
         }
      }

      return Double.MIN_VALUE;
   }

   private boolean isValidRandomLocation(final Location location) {
      return location.getBlockY() > this.worldInfoProvider.getMinHeight(location.getWorld()) && !this.isExcludedBiome(location);
   }

   private boolean isExcludedBiome(final Location location) {
      BiomeNameProvider biomeNameProvider = this.ess.provider(BiomeNameProvider.class);
      Set<String> excluded = this.getExcludedBiomes();
      String enumKey = biomeNameProvider.getBiomeName(location.getBlock());
      if (excluded.contains(enumKey)) {
         return true;
      } else if (VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_14_4_R01)) {
         return false;
      } else {
         BiomeKeyProvider biomeKeyProvider = this.ess.provider(BiomeKeyProvider.class);
         String biomeKey;
         if (biomeKeyProvider != null) {
            biomeKey = biomeKeyProvider.getBiomeKey(location.getBlock()).toString();
         } else {
            biomeKey = location.getBlock().getBiome().getKey().toString();
         }

         return excluded.contains(biomeKey);
      }
   }

   private String locationKey(final String name, final String key) {
      return "locations." + name + "." + key;
   }

   public File getFile() {
      return this.config.getFile();
   }
}
