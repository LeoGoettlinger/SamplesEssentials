package com.blaxk.spawnelytra.listener;

import com.blaxk.spawnelytra.Main;
import com.blaxk.spawnelytra.bedrock.TempElytraManager;
import com.blaxk.spawnelytra.data.PlayerDataManager;
import com.blaxk.spawnelytra.integration.BedrockSupport;
import com.blaxk.spawnelytra.util.MessageUtil;
import com.blaxk.spawnelytra.util.SchedulerUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SpawnElytra implements Listener {
   private final Main plugin;
   private final int multiplyValue;
   private final int spawnRadius;
   private final boolean boostEnabled;
   private final int maxBoosts;
   private final long boostCooldownMs;
   private final World world;
   private final List<Player> flying = new ArrayList<>();
   private final Location spawnLocation;
   private final Sound boostSound;
   private final boolean disableInCreative;
   private final boolean disableInAdventure;
   private final PlayerDataManager playerDataManager;
   private final String boostDirection;
   private final boolean disableFireworksInSpawnElytra;
   private final double fKeyLaunchStrength;
   private final String activationMode;
   private final double minX;
   private final double minY;
   private final double minZ;
   private final double maxX;
   private final double maxY;
   private final double maxZ;
   private final boolean useRectangularArea;
   private final boolean hungerEnabled;
   private final SpawnElytra.HungerMode hungerMode;
   private final int hungerActivationCost;
   private final double hungerDistanceBlocksPerPoint;
   private final int hungerDistanceCost;
   private final long hungerTimeIntervalMillis;
   private final int hungerTimeCost;
   private final int hungerMinimumFoodLevel;
   private final Map<UUID, Double> hungerDistanceProgress = new HashMap<>();
   private final Map<UUID, Long> hungerLastConsumption = new HashMap<>();
   private final Map<UUID, Location> hungerLastLocation = new HashMap<>();
   private static final long FLIGHT_END_GRACE_MS = 1000L;
   private final Map<UUID, Long> sneakJumpCooldown = new HashMap<>();
   private final Map<UUID, Long> flightEndGrace = new HashMap<>();
   private final Map<UUID, Long> boostCooldown = new HashMap<>();
   private final Map<UUID, Integer> boostUseCount = new HashMap<>();
   private final Map<UUID, Boolean> sneakPressed = new HashMap<>();
   private final Set<UUID> spawnElytraAllowFlight = new HashSet<>();
   private final Map<UUID, SchedulerUtil.TaskHandle> visualizationTasks = new HashMap<>();
   private final int visualizationVerticalRange;
   private final int visualizationPillarRange;
   private final int visualizationUpdateFrequency;
   private final float visualizationParticleSize;
   private final boolean visualizationEnhancedParticles;

   public SpawnElytra(Main plugin, String worldName, ConfigurationSection worldConfig) {
      this.plugin = plugin;
      this.multiplyValue = worldConfig.getInt("boost.strength", 2);
      this.spawnRadius = worldConfig.getInt("radius", 100);
      this.boostEnabled = worldConfig.getBoolean("boost.enabled", true);
      this.maxBoosts = Math.max(1, worldConfig.getInt("boost.max_boosts", 1));
      this.boostCooldownMs = Math.max(0L, (long)(worldConfig.getDouble("boost.boost_cooldown", 0.0) * 1000.0));
      this.disableInCreative = plugin.getConfig().getBoolean("game_modes.disable_in_creative", true);
      this.disableInAdventure = plugin.getConfig().getBoolean("game_modes.disable_in_adventure", false);
      this.playerDataManager = plugin.getPlayerDataManager();
      this.boostDirection = worldConfig.getString("boost.direction", "forward").toLowerCase();
      this.disableFireworksInSpawnElytra = plugin.getConfig().getBoolean("fireworks.disable_in_spawn_elytra", false);
      this.fKeyLaunchStrength = worldConfig.getDouble("f_key.launch_strength", 1.5);
      SpawnElytra.HungerConfig hungerConfig = loadHungerConfig(plugin);
      this.hungerEnabled = hungerConfig.enabled;
      this.hungerMode = hungerConfig.mode;
      this.hungerMinimumFoodLevel = hungerConfig.minimumFoodLevel;
      this.hungerActivationCost = hungerConfig.activationCost;
      this.hungerDistanceBlocksPerPoint = hungerConfig.distanceBlocksPerPoint;
      this.hungerDistanceCost = hungerConfig.distanceCost;
      this.hungerTimeIntervalMillis = hungerConfig.timeIntervalMillis;
      this.hungerTimeCost = hungerConfig.timeCost;
      SpawnElytra.VisualizationConfig vizConfig = loadVisualizationConfig(plugin);
      this.visualizationVerticalRange = vizConfig.verticalRange;
      this.visualizationPillarRange = vizConfig.pillarRange;
      this.visualizationUpdateFrequency = vizConfig.updateFrequency;
      this.visualizationParticleSize = vizConfig.particleSize;
      this.visualizationEnhancedParticles = vizConfig.enhancedParticles;
      this.activationMode = worldConfig.getString("activation_mode", "double_jump").toLowerCase();
      this.world = Bukkit.getWorld(worldName);
      String mode = worldConfig.getString("spawn_area.mode", "auto");
      this.boostSound = loadBoostSound(plugin, worldConfig);
      if (this.world == null) {
         plugin.getLogger()
            .severe(
               "Invalid world: "
                  + worldName
                  + ". Available worlds: "
                  + Bukkit.getWorlds().stream().<String>map(WorldInfo::getName).reduce((a, b) -> a + ", " + b).orElse("none")
            );
         this.spawnLocation = null;
         this.minX = this.minY = this.minZ = 0.0;
         this.maxX = this.maxY = this.maxZ = 0.0;
         this.useRectangularArea = false;
      } else {
         SpawnElytra.SpawnAreaConfig areaConfig = this.loadSpawnAreaConfig(worldConfig, mode);
         this.spawnLocation = areaConfig.spawnLocation;
         this.minX = areaConfig.minX;
         this.minY = areaConfig.minY;
         this.minZ = areaConfig.minZ;
         this.maxX = areaConfig.maxX;
         this.maxY = areaConfig.maxY;
         this.maxZ = areaConfig.maxZ;
         this.useRectangularArea = areaConfig.useRectangular;
      }
   }

   private static SpawnElytra.HungerConfig loadHungerConfig(Main plugin) {
      SpawnElytra.HungerConfig config = new SpawnElytra.HungerConfig();
      ConfigurationSection hungerSection = plugin.getConfig().getConfigurationSection("hunger_consumption");
      if (hungerSection != null && hungerSection.getBoolean("enabled", false)) {
         config.enabled = true;
         config.mode = SpawnElytra.HungerMode.fromString(hungerSection.getString("mode"));
         config.minimumFoodLevel = Math.max(0, hungerSection.getInt("minimum_food_level", 0));
         ConfigurationSection activationSection = hungerSection.getConfigurationSection("activation");
         config.activationCost = Math.max(0, activationSection != null ? activationSection.getInt("hunger_cost", 1) : 1);
         ConfigurationSection distanceSection = hungerSection.getConfigurationSection("distance");
         config.distanceBlocksPerPoint = Math.max(1.0, distanceSection != null ? distanceSection.getDouble("blocks_per_point", 50.0) : 50.0);
         config.distanceCost = Math.max(0, distanceSection != null ? distanceSection.getInt("hunger_cost", 1) : 1);
         ConfigurationSection timeSection = hungerSection.getConfigurationSection("time");
         double secondsPerPoint = timeSection != null ? timeSection.getDouble("seconds_per_point", 30.0) : 30.0;
         config.timeIntervalMillis = Math.max(1L, (long)Math.ceil(secondsPerPoint * 1000.0));
         config.timeCost = Math.max(0, timeSection != null ? timeSection.getInt("hunger_cost", 1) : 1);
      } else {
         config.enabled = false;
         config.mode = SpawnElytra.HungerMode.ACTIVATION;
         config.activationCost = 0;
         config.distanceBlocksPerPoint = 1.0;
         config.distanceCost = 0;
         config.timeIntervalMillis = 1000L;
         config.timeCost = 0;
         config.minimumFoodLevel = 0;
      }

      return config;
   }

   private static SpawnElytra.VisualizationConfig loadVisualizationConfig(Main plugin) {
      SpawnElytra.VisualizationConfig config = new SpawnElytra.VisualizationConfig();
      ConfigurationSection visualizationSection = plugin.getConfig().getConfigurationSection("visualization");
      if (visualizationSection != null) {
         config.verticalRange = visualizationSection.getInt("vertical_range", 20);
         config.pillarRange = visualizationSection.getInt("pillar_vertical_range", 25);
         config.updateFrequency = Math.max(1, visualizationSection.getInt("update_frequency", 10));
         config.particleSize = (float)Math.max(0.1, visualizationSection.getDouble("particle_size", 2.0));
         config.enhancedParticles = visualizationSection.getBoolean("enhanced_particles", true);
      } else {
         config.verticalRange = 20;
         config.pillarRange = 25;
         config.updateFrequency = 10;
         config.particleSize = 2.0F;
         config.enhancedParticles = true;
      }

      return config;
   }

   private static Sound loadBoostSound(Main plugin, ConfigurationSection worldConfig) {
      String soundName = worldConfig.getString("boost.sound", "ENTITY_BAT_TAKEOFF");

      try {
         return Sound.valueOf(soundName.toUpperCase());
      } catch (IllegalArgumentException var4) {
         plugin.getLogger().warning("Invalid sound: " + soundName + ". Using default sound.");
         return Sound.ENTITY_BAT_TAKEOFF;
      }
   }

   private SpawnElytra.SpawnAreaConfig loadSpawnAreaConfig(ConfigurationSection worldConfig, String mode) {
      SpawnElytra.SpawnAreaConfig config = new SpawnElytra.SpawnAreaConfig();
      if ("advanced".equalsIgnoreCase(mode)) {
         double spawnX = worldConfig.getDouble("spawn_area.x", this.world.getSpawnLocation().getX());
         double spawnY = worldConfig.getDouble("spawn_area.y", this.world.getSpawnLocation().getY());
         double spawnZ = worldConfig.getDouble("spawn_area.z", this.world.getSpawnLocation().getZ());
         config.spawnLocation = new Location(this.world, spawnX, spawnY, spawnZ);
         double x2 = worldConfig.getDouble("spawn_area.x2", 0.0);
         double y2 = worldConfig.getDouble("spawn_area.y2", 0.0);
         double z2 = worldConfig.getDouble("spawn_area.z2", 0.0);
         boolean allZero = x2 == 0.0 && y2 == 0.0 && z2 == 0.0;
         String areaType = worldConfig.getString("spawn_area.area_type", "circular");
         if (!allZero && "rectangular".equalsIgnoreCase(areaType)) {
            config.minX = Math.min(spawnX, x2);
            config.maxX = Math.max(spawnX, x2);
            config.minY = Math.min(spawnY, y2);
            config.maxY = Math.max(spawnY, y2);
            config.minZ = Math.min(spawnZ, z2);
            config.maxZ = Math.max(spawnZ, z2);
            config.useRectangular = true;
         } else {
            config.minX = config.minY = config.minZ = 0.0;
            config.maxX = config.maxY = config.maxZ = 0.0;
            config.useRectangular = false;
         }
      } else {
         config.spawnLocation = this.world.getSpawnLocation();
         config.minX = config.minY = config.minZ = 0.0;
         config.maxX = config.maxY = config.maxZ = 0.0;
         config.useRectangular = false;
      }

      return config;
   }

   public boolean isValid() {
      return this.world != null && this.spawnLocation != null;
   }

   public boolean isFlying(Player player) {
      return this.flying.contains(player);
   }

   public void resumeBedrockFlight(Player player) {
      if (!this.flying.contains(player)) {
         this.flying.add(player);
      }
   }

   public int getBoostsRemaining(Player player) {
      return !this.boostEnabled ? 0 : Math.max(0, this.maxBoosts - this.boostUseCount.getOrDefault(player.getUniqueId(), 0));
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (this.isValid()) {
         if (this.disableFireworksInSpawnElytra) {
            Player player = event.getPlayer();
            ItemStack item = event.getItem();
            if (this.flying.contains(player) && item != null && Material.FIREWORK_ROCKET == item.getType()) {
               ItemStack chestplate = player.getInventory().getChestplate();
               if (chestplate == null || Material.ELYTRA != chestplate.getType() || this.plugin.getTempElytraManager().isTempElytra(chestplate)) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   private void initializeHungerTracking(Player player) {
      if (this.hungerEnabled) {
         UUID playerId = player.getUniqueId();
         if (SpawnElytra.HungerMode.DISTANCE == this.hungerMode) {
            this.hungerLastLocation.put(playerId, player.getLocation().clone());
            this.hungerDistanceProgress.put(playerId, 0.0);
         } else if (SpawnElytra.HungerMode.TIME == this.hungerMode) {
            this.hungerLastConsumption.put(playerId, System.currentTimeMillis());
         } else {
            this.hungerLastConsumption.remove(playerId);
            this.hungerLastLocation.remove(playerId);
            this.hungerDistanceProgress.remove(playerId);
         }
      }
   }

   private void handleHungerWhileFlying(Player player) {
      UUID playerId = player.getUniqueId();
      if (!this.shouldConsumeHunger(player)) {
         if (SpawnElytra.HungerMode.DISTANCE == this.hungerMode) {
            this.hungerLastLocation.put(playerId, player.getLocation().clone());
            this.hungerDistanceProgress.put(playerId, 0.0);
         } else if (SpawnElytra.HungerMode.TIME == this.hungerMode) {
            this.hungerLastConsumption.putIfAbsent(playerId, System.currentTimeMillis());
         }
      } else {
         if (SpawnElytra.HungerMode.DISTANCE == this.hungerMode) {
            Location current = player.getLocation();
            Location last = this.hungerLastLocation.get(playerId);
            if (last == null) {
               this.hungerLastLocation.put(playerId, current.clone());
               this.hungerDistanceProgress.put(playerId, 0.0);
               return;
            }

            double distance = last.distance(current);
            if (distance <= 0.0) {
               this.hungerLastLocation.put(playerId, current.clone());
               return;
            }

            double accumulated = this.hungerDistanceProgress.getOrDefault(playerId, 0.0) + distance;
            double threshold = this.hungerDistanceBlocksPerPoint <= 0.0 ? 1.0 : this.hungerDistanceBlocksPerPoint;
            if (accumulated >= threshold && this.hungerDistanceCost > 0) {
               int steps = (int)(accumulated / threshold);
               this.consumeHunger(player, this.hungerDistanceCost * steps);
               accumulated -= threshold * steps;
            }

            this.hungerDistanceProgress.put(playerId, accumulated);
            this.hungerLastLocation.put(playerId, current.clone());
         } else if (SpawnElytra.HungerMode.TIME == this.hungerMode) {
            long now = System.currentTimeMillis();
            long lastx = this.hungerLastConsumption.getOrDefault(playerId, now);
            if (now - lastx >= this.hungerTimeIntervalMillis) {
               if (this.hungerTimeCost > 0) {
                  this.consumeHunger(player, this.hungerTimeCost);
               }

               this.hungerLastConsumption.put(playerId, now);
            }
         }
      }
   }

   private void resetHungerTracking(Player player) {
      if (this.hungerEnabled) {
         UUID playerId = player.getUniqueId();
         this.hungerDistanceProgress.remove(playerId);
         this.hungerLastConsumption.remove(playerId);
         this.hungerLastLocation.remove(playerId);
      }
   }

   private boolean shouldConsumeHunger(Player player) {
      if (!this.hungerEnabled) {
         return false;
      } else {
         GameMode mode = player.getGameMode();
         return GameMode.CREATIVE != mode && GameMode.SPECTATOR != mode ? player.getFoodLevel() > this.hungerMinimumFoodLevel : false;
      }
   }

   private void consumeHunger(Player player, int amount) {
      if (this.hungerEnabled && amount > 0) {
         int current = player.getFoodLevel();
         if (current > this.hungerMinimumFoodLevel) {
            int newLevel = Math.max(this.hungerMinimumFoodLevel, current - amount);
            if (newLevel < current) {
               player.setFoodLevel(newLevel);
               if (player.getSaturation() > newLevel) {
                  player.setSaturation(newLevel);
               }
            }
         }
      }
   }

   public void visualizeArea(final Player player, final int seconds) {
      if (this.world != null && this.spawnLocation != null) {
         SchedulerUtil.TaskHandle existingTask = this.visualizationTasks.remove(player.getUniqueId());
         if (existingTask != null) {
            existingTask.cancel();
         }

         MessageUtil.send(player, "visualize_start", Placeholder.unparsed("seconds", String.valueOf(seconds)));
         SchedulerUtil.TaskHandle task = SchedulerUtil.runAtEntityTimer(this.plugin, player, 0L, 1L, new Runnable() {
            private int ticksElapsed;
            private final int maxTicks = seconds * 20;

            @Override
            public void run() {
               if (this.ticksElapsed < this.maxTicks && player.isOnline()) {
                  if (this.ticksElapsed % SpawnElytra.this.visualizationUpdateFrequency == 0) {
                     SpawnElytra.this.showAreaParticles(player);
                  }

                  this.ticksElapsed++;
               } else {
                  SchedulerUtil.TaskHandle handle = SpawnElytra.this.visualizationTasks.remove(player.getUniqueId());
                  if (handle != null) {
                     handle.cancel();
                  }

                  if (player.isOnline()) {
                     MessageUtil.send(player, "visualize_end");
                  }
               }
            }
         });
         this.visualizationTasks.put(player.getUniqueId(), task);
      } else {
         MessageUtil.send(player, "visualize_no_area");
      }
   }

   private void showAreaParticles(Player player) {
      if (this.useRectangularArea) {
         this.showRectangularAreaParticles(player);
      } else {
         this.showCircularAreaParticles(player);
      }
   }

   private void showCircularAreaParticles(Player player) {
      Location center = this.spawnLocation.clone();
      World world = center.getWorld();
      double playerY = player.getLocation().getY();
      DustOptions gold = new DustOptions(Color.fromRGB(255, 215, 0), this.visualizationParticleSize);
      DustOptions brightGold = this.visualizationEnhancedParticles
         ? new DustOptions(Color.fromRGB(255, 255, 100), this.visualizationParticleSize * 0.9F)
         : gold;

      for (int yOffset = -this.visualizationVerticalRange; yOffset <= this.visualizationVerticalRange; yOffset += 2) {
         double y = playerY + yOffset;
         double angleStep = Math.abs(yOffset) <= 5 ? 2.0 : (Math.abs(yOffset) <= 10 ? 3.0 : 4.0);

         for (double angle = 0.0; angle < 360.0; angle += angleStep) {
            double rad = Math.toRadians(angle);
            double x = center.getX() + this.spawnRadius * Math.cos(rad);
            double z = center.getZ() + this.spawnRadius * Math.sin(rad);
            Location particleLocation = new Location(world, x, y, z);
            DustOptions dustOptions = Math.abs(yOffset) <= 8 ? brightGold : gold;
            player.spawnParticle(Particle.DUST, particleLocation, 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
         }
      }

      double[][] cardinals = new double[][]{
         {center.getX() + this.spawnRadius, center.getZ()},
         {center.getX() - this.spawnRadius, center.getZ()},
         {center.getX(), center.getZ() + this.spawnRadius},
         {center.getX(), center.getZ() - this.spawnRadius}
      };

      for (double[] p : cardinals) {
         for (double y = playerY - this.visualizationPillarRange; y <= playerY + this.visualizationPillarRange; y += 0.5) {
            player.spawnParticle(Particle.END_ROD, new Location(world, p[0], y, p[1]), 1, 0.0, 0.0, 0.0, 0.0);
         }

         if (this.visualizationEnhancedParticles) {
            Location cardinalMarker = new Location(world, p[0], playerY, p[1]);
            player.spawnParticle(Particle.FIREWORK, cardinalMarker, 2, 0.1, 0.1, 0.1, 0.02);
         }
      }

      center.setY(playerY);
      player.spawnParticle(Particle.END_ROD, center, 5, 0.2, 0.2, 0.2, 0.0);
      if (this.visualizationEnhancedParticles) {
         player.spawnParticle(Particle.FIREWORK, center, 3, 0.1, 0.1, 0.1, 0.05);
      }

      int centerPillarRange = Math.max(10, this.visualizationVerticalRange * 3 / 4);

      for (double y = playerY - centerPillarRange; y <= playerY + centerPillarRange; y++) {
         Location centerPillar = new Location(world, center.getX(), y, center.getZ());
         if (this.visualizationEnhancedParticles) {
            player.spawnParticle(Particle.SOUL_FIRE_FLAME, centerPillar, 1, 0.0, 0.0, 0.0, 0.0);
         } else {
            player.spawnParticle(Particle.FLAME, centerPillar, 1, 0.0, 0.0, 0.0, 0.0);
         }
      }
   }

   private void showRectangularAreaParticles(Player player) {
      double playerY = player.getLocation().getY();
      DustOptions gold = new DustOptions(Color.fromRGB(255, 215, 0), this.visualizationParticleSize);
      DustOptions brightGold = this.visualizationEnhancedParticles
         ? new DustOptions(Color.fromRGB(255, 255, 100), this.visualizationParticleSize * 0.9F)
         : gold;

      for (int yOffset = -this.visualizationVerticalRange; yOffset <= this.visualizationVerticalRange; yOffset += 3) {
         double y = playerY + yOffset;
         DustOptions dustOptions = Math.abs(yOffset) <= 6 ? brightGold : gold;

         for (double x = this.minX; x <= this.maxX; x += 0.75) {
            player.spawnParticle(Particle.DUST, new Location(this.world, x, y, this.minZ), 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
            player.spawnParticle(Particle.DUST, new Location(this.world, x, y, this.maxZ), 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
         }

         for (double z = this.minZ; z <= this.maxZ; z += 0.75) {
            player.spawnParticle(Particle.DUST, new Location(this.world, this.minX, y, z), 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
            player.spawnParticle(Particle.DUST, new Location(this.world, this.maxX, y, z), 1, 0.0, 0.0, 0.0, 0.0, dustOptions);
         }
      }

      double[][] corners = new double[][]{{this.minX, this.minZ}, {this.minX, this.maxZ}, {this.maxX, this.minZ}, {this.maxX, this.maxZ}};

      for (double[] c : corners) {
         for (double y = playerY - this.visualizationPillarRange; y <= playerY + this.visualizationPillarRange; y += 0.4) {
            player.spawnParticle(Particle.END_ROD, new Location(this.world, c[0], y, c[1]), 1, 0.0, 0.0, 0.0, 0.0);
         }

         if (this.visualizationEnhancedParticles) {
            Location cornerMarker = new Location(this.world, c[0], playerY, c[1]);
            player.spawnParticle(Particle.FIREWORK, cornerMarker, 2, 0.1, 0.1, 0.1, 0.02);
         }
      }

      Location center = new Location(this.world, (this.minX + this.maxX) / 2.0, playerY, (this.minZ + this.maxZ) / 2.0);
      player.spawnParticle(Particle.END_ROD, center, 5, 0.2, 0.2, 0.2, 0.0);
      if (this.visualizationEnhancedParticles) {
         player.spawnParticle(Particle.FIREWORK, center, 3, 0.1, 0.1, 0.1, 0.05);
      }

      int centerPillarRange = Math.max(10, this.visualizationVerticalRange * 3 / 4);

      for (double y = playerY - centerPillarRange; y <= playerY + centerPillarRange; y++) {
         Location centerPillar = new Location(this.world, center.getX(), y, center.getZ());
         if (this.visualizationEnhancedParticles) {
            player.spawnParticle(Particle.SOUL_FIRE_FLAME, centerPillar, 1, 0.0, 0.0, 0.0, 0.0);
         } else {
            player.spawnParticle(Particle.FLAME, centerPillar, 1, 0.0, 0.0, 0.0, 0.0);
         }
      }

      if (this.visualizationEnhancedParticles) {
         double[][] midpoints = new double[][]{
            {(this.minX + this.maxX) / 2.0, this.minZ},
            {(this.minX + this.maxX) / 2.0, this.maxZ},
            {this.minX, (this.minZ + this.maxZ) / 2.0},
            {this.maxX, (this.minZ + this.maxZ) / 2.0}
         };

         for (double[] m : midpoints) {
            int midpointPillarRange = Math.max(8, this.visualizationVerticalRange / 2);

            for (double yx = playerY - midpointPillarRange; yx <= playerY + midpointPillarRange; yx++) {
               player.spawnParticle(Particle.FLAME, new Location(this.world, m[0], yx, m[1]), 1, 0.0, 0.0, 0.0, 0.0);
            }
         }
      }
   }

   private boolean hasAirBelow(Player player) {
      Location loc = player.getLocation();

      for (int i = 1; i <= 3; i++) {
         Block block = loc.clone().subtract(0.0, i, 0.0).getBlock();
         if (Material.AIR != block.getType() && Material.CAVE_AIR != block.getType()) {
            return false;
         }
      }

      return true;
   }

   private boolean isExternalFlyActive(Player player) {
      GameMode mode = player.getGameMode();
      if (GameMode.CREATIVE == mode || GameMode.SPECTATOR == mode) {
         return true;
      } else {
         return player.isFlying() ? true : player.getAllowFlight() && !this.spawnElytraAllowFlight.contains(player.getUniqueId());
      }
   }

   private boolean shouldPrimeDoubleJump(Player player, boolean inArea) {
      return inArea
         && player.hasPermission("spawnelytra.use")
         && this.isElytraAllowedInMode(player)
         && !this.isExternalFlyActive(player)
         && !player.isGliding()
         && !this.flying.contains(player);
   }

   private void grantSpawnElytraAllowFlight(Player player) {
      UUID uuid = player.getUniqueId();
      if (this.spawnElytraAllowFlight.add(uuid)) {
         player.setAllowFlight(true);
      }
   }

   private void updateTemporaryAllowFlight(Player player, boolean inArea) {
      if (!this.isExternalFlyActive(player)) {
         if (this.shouldPrimeDoubleJump(player, inArea)) {
            this.grantSpawnElytraAllowFlight(player);
         } else {
            if (!inArea && !this.flying.contains(player)) {
               this.revokeSpawnElytraAllowFlight(player);
            }
         }
      }
   }

   private void revokeSpawnElytraAllowFlight(Player player) {
      UUID uuid = player.getUniqueId();
      if (this.spawnElytraAllowFlight.remove(uuid)) {
         player.setAllowFlight(false);
      }
   }

   private void updateBedrockPriming(Player player) {
      if (!this.flying.contains(player) && !player.isGliding()) {
         boolean wantPrimed = this.isInSpawnArea(player)
            && player.hasPermission("spawnelytra.use")
            && this.isElytraAllowedInMode(player)
            && !this.isExternalFlyActive(player)
            && this.hasActivationHungerAvailable(player);
         TempElytraManager manager = this.plugin.getTempElytraManager();
         if (wantPrimed) {
            manager.ensureEquipped(player);
         } else {
            manager.ensureRestored(player);
         }
      }
   }

   private boolean hasActivationHungerAvailable(Player player) {
      return this.hungerEnabled && SpawnElytra.HungerMode.ACTIVATION == this.hungerMode ? this.shouldConsumeHunger(player) : true;
   }

   private boolean isFlyToggleCommand(String message) {
      if (message != null && !message.isBlank()) {
         String trimmed = message.charAt(0) == '/' ? message.substring(1) : message;
         int spaceIndex = trimmed.indexOf(32);
         String command = (spaceIndex >= 0 ? trimmed.substring(0, spaceIndex) : trimmed).toLowerCase(Locale.ROOT);
         return "fly".equals(command) || "essentials:fly".equals(command) || "essentialsx:fly".equals(command);
      } else {
         return false;
      }
   }

   private void activateElytraFlight(Player player) {
      if (!this.isExternalFlyActive(player)) {
         if (this.chargeActivationHunger(player)) {
            player.setGliding(true);
            this.revokeSpawnElytraAllowFlight(player);
            this.startFlightBookkeeping(player);
         }
      }
   }

   private boolean chargeActivationHunger(Player player) {
      if (this.hungerEnabled && SpawnElytra.HungerMode.ACTIVATION == this.hungerMode) {
         if (!this.shouldConsumeHunger(player)) {
            MessageUtil.sendActionBar(player, "not_enough_hunger");
            return false;
         }

         if (this.hungerActivationCost > 0) {
            this.consumeHunger(player, this.hungerActivationCost);
         }
      }

      return true;
   }

   private void startFlightBookkeeping(Player player) {
      if (this.playerDataManager != null) {
         this.playerDataManager.incrementFlyCount(player);
      }

      if (this.plugin.getConfig().getBoolean("messages.show_press_to_boost", true) && this.boostEnabled) {
         boolean bedrock = BedrockSupport.isManaged(player);
         if (this.maxBoosts > 1) {
            MessageUtil.sendActionBar(
               player,
               bedrock ? "press_to_boost_remaining_bedrock" : "press_to_boost_remaining",
               Placeholder.unparsed("remaining", String.valueOf(this.maxBoosts))
            );
         } else {
            MessageUtil.sendActionBar(player, bedrock ? "press_to_boost_bedrock" : "press_to_boost");
         }
      }

      this.flying.add(player);
      this.initializeHungerTracking(player);
   }

   private void disableElytraFlight(Player player) {
      this.revokeSpawnElytraAllowFlight(player);
      player.setGliding(false);
      player.setFallDistance(0.0F);
      this.flying.remove(player);
      UUID uuid = player.getUniqueId();
      this.flightEndGrace.put(uuid, System.currentTimeMillis());
      this.boostCooldown.remove(uuid);
      this.boostUseCount.remove(uuid);
      this.resetHungerTracking(player);
   }

   @EventHandler
   public void onDoubleJump(PlayerToggleFlightEvent event) {
      if (this.isValid()) {
         if ("double_jump".equalsIgnoreCase(this.activationMode)) {
            if (event.isFlying()) {
               Player player = event.getPlayer();
               if (!BedrockSupport.isManaged(player)) {
                  if (player.hasPermission("spawnelytra.use")) {
                     if (!this.disableInCreative || GameMode.CREATIVE != player.getGameMode()) {
                        if (!this.isExternalFlyActive(player)) {
                           if (this.isElytraAllowedInMode(player) && this.isInSpawnArea(player)) {
                              event.setCancelled(true);
                              if (player.isGliding()) {
                                 return;
                              }

                              if (this.flying.contains(player)) {
                                 return;
                              }

                              this.activateElytraFlight(player);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerSneak(PlayerToggleSneakEvent event) {
      if (this.isValid()) {
         Player player = event.getPlayer();
         UUID playerId = player.getUniqueId();
         if (event.isSneaking() && BedrockSupport.isManaged(player) && this.flying.contains(player) && player.isGliding()) {
            this.tryBoost(player);
         } else if (player.hasPermission("spawnelytra.use")) {
            if (!this.disableInCreative || GameMode.CREATIVE != player.getGameMode()) {
               if ("sneak_jump".equalsIgnoreCase(this.activationMode)) {
                  if (this.isElytraAllowedInMode(player) && this.isInSpawnArea(player)) {
                     if (!this.isExternalFlyActive(player)) {
                        if (event.isSneaking()) {
                           this.sneakPressed.put(playerId, true);
                           SchedulerUtil.runAtEntityLater(this.plugin, player, 10L, () -> {
                              UUID playerUUID = player.getUniqueId();
                              if (this.sneakPressed.getOrDefault(playerUUID, false) && !player.isOnGround() && !this.flying.contains(player)) {
                                 long currentTime = System.currentTimeMillis();
                                 Long lastActivation = this.sneakJumpCooldown.get(playerUUID);
                                 if (lastActivation == null || currentTime - lastActivation >= 1000L) {
                                    this.activateElytraFlight(player);
                                    this.sneakJumpCooldown.put(playerUUID, currentTime);
                                 }
                              }

                              this.sneakPressed.put(playerUUID, false);
                           });
                        } else {
                           this.sneakPressed.put(playerId, false);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
      if (this.isValid()) {
         if ("double_jump".equalsIgnoreCase(this.activationMode)) {
            Player player = event.getPlayer();
            if (this.spawnElytraAllowFlight.contains(player.getUniqueId())) {
               if (this.isFlyToggleCommand(event.getMessage())) {
                  this.revokeSpawnElytraAllowFlight(player);
               }
            }
         }
      }
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      if (this.isValid()) {
         Player player = event.getPlayer();
         if (BedrockSupport.isManaged(player) && player.getWorld().equals(this.world)) {
            this.updateBedrockPriming(player);
         } else if (this.isElytraAllowedInMode(player)) {
            boolean inArea = this.isInSpawnArea(player);
            if ("double_jump".equalsIgnoreCase(this.activationMode)) {
               this.updateTemporaryAllowFlight(player, inArea);
            }

            if ("auto".equalsIgnoreCase(this.activationMode)
               && inArea
               && !this.flying.contains(player)
               && this.hasAirBelow(player)
               && !player.isOnGround()
               && !player.isFlying()
               && !player.isGliding()
               && !this.isExternalFlyActive(player)
               && player.hasPermission("spawnelytra.use")) {
               this.activateElytraFlight(player);
            }
         }

         if (this.flying.contains(player)) {
            if (!player.isOnGround() && !player.getLocation().getBlock().getRelative(BlockFace.DOWN).isLiquid()) {
               player.setFallDistance(0.0F);
               if (player.isGliding() && this.hungerEnabled) {
                  this.handleHungerWhileFlying(player);
               }
            } else {
               this.disableElytraFlight(player);
            }
         } else if (!player.isGliding()) {
            this.resetHungerTracking(player);
         }
      }
   }

   @EventHandler
   public void onEntityDamage(EntityDamageEvent event) {
      if (EntityType.PLAYER == event.getEntityType()) {
         if (DamageCause.FALL == event.getCause() || DamageCause.FLY_INTO_WALL == event.getCause()) {
            Player player = (Player)event.getEntity();
            if (this.flying.contains(player)) {
               event.setCancelled(true);
            } else if (this.plugin.getTempElytraManager().isTempElytra(player.getInventory().getChestplate())) {
               event.setCancelled(true);
            } else {
               Long flightEnd = this.flightEndGrace.get(player.getUniqueId());
               if (flightEnd != null) {
                  if (System.currentTimeMillis() - flightEnd <= 1000L) {
                     event.setCancelled(true);
                  } else {
                     this.flightEndGrace.remove(player.getUniqueId());
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onSwapItem(PlayerSwapHandItemsEvent event) {
      if (this.isValid()) {
         Player player = event.getPlayer();
         if ("f_key".equalsIgnoreCase(this.activationMode) && !BedrockSupport.isManaged(player)) {
            if (!player.hasPermission("spawnelytra.use")) {
               return;
            }

            if (this.disableInCreative && GameMode.CREATIVE == player.getGameMode()) {
               return;
            }

            if (this.isElytraAllowedInMode(player) && this.isInSpawnArea(player) && !this.flying.contains(player) && !this.isExternalFlyActive(player)) {
               event.setCancelled(true);
               Vector launchVelocity = new Vector(0.0, this.fKeyLaunchStrength, 0.0);
               player.setVelocity(launchVelocity);
               SchedulerUtil.runAtEntityLater(this.plugin, player, 5L, () -> this.activateElytraFlight(player));
               return;
            }
         }

         if (this.tryBoost(player)) {
            event.setCancelled(true);
         }
      }
   }

   private boolean tryBoost(Player player) {
      if (!player.hasPermission("spawnelytra.useboost")) {
         return false;
      } else if (!this.boostEnabled) {
         return false;
      } else if (this.flying.contains(player) && player.isGliding()) {
         UUID uuid = player.getUniqueId();
         int usedBoosts = this.boostUseCount.getOrDefault(uuid, 0);
         if (usedBoosts >= this.maxBoosts) {
            return false;
         } else {
            if (this.boostCooldownMs > 0L) {
               long now = System.currentTimeMillis();
               Long lastBoost = this.boostCooldown.get(uuid);
               if (lastBoost != null && now - lastBoost < this.boostCooldownMs) {
                  return false;
               }
            }

            int newCount = usedBoosts + 1;
            this.boostUseCount.put(uuid, newCount);
            if (this.boostCooldownMs > 0L) {
               this.boostCooldown.put(uuid, System.currentTimeMillis());
            }

            Vector velocity;
            if ("upward".equalsIgnoreCase(this.boostDirection)) {
               velocity = new Vector(0, this.multiplyValue, 0);
            } else {
               velocity = player.getLocation().getDirection().multiply(this.multiplyValue);
            }

            player.setVelocity(velocity);
            if (this.playerDataManager != null) {
               this.playerDataManager.incrementBoostCount(player);
            }

            player.playSound(player.getLocation(), this.boostSound, 1.0F, 1.0F);
            int remaining = this.maxBoosts - newCount;
            boolean showBoostActivated = this.plugin.getConfig().getBoolean("messages.show_boost_activated", true);
            boolean showPressToBoost = this.plugin.getConfig().getBoolean("messages.show_press_to_boost", true);
            if (showBoostActivated) {
               if (remaining > 0 && this.maxBoosts > 1) {
                  MessageUtil.sendActionBar(player, "boost_activated_remaining", Placeholder.unparsed("remaining", String.valueOf(remaining)));
               } else {
                  MessageUtil.sendActionBar(player, "boost_activated");
               }
            }

            if (remaining > 0 && showPressToBoost && this.maxBoosts > 1) {
               boolean bedrock = BedrockSupport.isManaged(player);
               long delayTicks = this.boostCooldownMs > 0L ? Math.max(20L, this.boostCooldownMs / 50L) : 30L;
               SchedulerUtil.runAtEntityLater(
                  this.plugin,
                  player,
                  delayTicks,
                  () -> {
                     if (this.flying.contains(player) && player.isGliding()) {
                        int currentRemaining = this.maxBoosts - this.boostUseCount.getOrDefault(uuid, 0);
                        if (currentRemaining > 0) {
                           MessageUtil.sendActionBar(
                              player,
                              bedrock ? "press_to_boost_remaining_bedrock" : "press_to_boost_remaining",
                              Placeholder.unparsed("remaining", String.valueOf(currentRemaining))
                           );
                        }
                     }
                  }
               );
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @EventHandler
   public void onToggleGlide(EntityToggleGlideEvent event) {
      if (this.isValid()) {
         if (EntityType.PLAYER == event.getEntityType()) {
            Player player = (Player)event.getEntity();
            if (event.isGliding() && !this.flying.contains(player) && BedrockSupport.isManaged(player) && player.getWorld().equals(this.world)) {
               this.handleBedrockGlideStart(player, event);
            } else {
               if (this.flying.contains(player)) {
                  if (!event.isGliding()) {
                     if (BedrockSupport.isManaged(player)) {
                        return;
                     }

                     event.setCancelled(true);
                  } else {
                     this.revokeSpawnElytraAllowFlight(player);
                  }
               }
            }
         }
      }
   }

   private void handleBedrockGlideStart(Player player, EntityToggleGlideEvent event) {
      TempElytraManager manager = this.plugin.getTempElytraManager();
      if (manager.hasTempElytraEquipped(player)) {
         boolean allowed = player.hasPermission("spawnelytra.use")
            && this.isElytraAllowedInMode(player)
            && this.isInSpawnArea(player)
            && !this.isExternalFlyActive(player)
            && this.chargeActivationHunger(player);
         if (!allowed) {
            event.setCancelled(true);
            manager.ensureRestored(player);
         } else {
            this.startFlightBookkeeping(player);
         }
      }
   }

   @EventHandler
   public void onGameModeChange(PlayerGameModeChangeEvent event) {
      if (this.isValid()) {
         if (this.disableInCreative && GameMode.CREATIVE == event.getNewGameMode()) {
            Player player = event.getPlayer();
            if (this.flying.contains(player)) {
               SchedulerUtil.runAtEntityLater(this.plugin, player, 1L, () -> {
                  this.disableElytraFlight(player);
                  SchedulerUtil.runAtEntityLater(this.plugin, player, 5L, () -> player.setAllowFlight(true));
               });
            }
         }
      }
   }

   public boolean isInSpawnArea(Player player) {
      if (!player.getWorld().equals(this.world) || this.spawnLocation == null) {
         return false;
      } else if (!this.useRectangularArea) {
         double distance = this.spawnLocation.distance(player.getLocation());
         return distance <= this.spawnRadius;
      } else {
         Location loc = player.getLocation();
         double x = loc.getX();
         double y = loc.getY();
         double z = loc.getZ();
         return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ;
      }
   }

   private boolean isElytraAllowedInMode(Player player) {
      GameMode mode = player.getGameMode();
      return GameMode.SURVIVAL == mode ? true : GameMode.ADVENTURE == mode && !this.disableInAdventure;
   }

   public void cleanupPlayer(Player player) {
      if (this.flying.contains(player)) {
         this.disableElytraFlight(player);
      } else {
         this.revokeSpawnElytraAllowFlight(player);
      }

      UUID uuid = player.getUniqueId();
      this.sneakJumpCooldown.remove(uuid);
      this.sneakPressed.remove(uuid);
      this.flightEndGrace.remove(uuid);
   }

   public void stopVisualization(Player player) {
      SchedulerUtil.TaskHandle task = this.visualizationTasks.remove(player.getUniqueId());
      if (task != null) {
         task.cancel();
         MessageUtil.send(player, "visualize_stop");
      }
   }

   private static class HungerConfig {
      boolean enabled;
      SpawnElytra.HungerMode mode;
      int minimumFoodLevel;
      int activationCost;
      double distanceBlocksPerPoint;
      int distanceCost;
      long timeIntervalMillis;
      int timeCost;
   }

   private static enum HungerMode {
      ACTIVATION,
      DISTANCE,
      TIME;

      static SpawnElytra.HungerMode fromString(String value) {
         if (value == null) {
            return ACTIVATION;
         } else {
            String var1 = value.toLowerCase(Locale.ROOT);

            return switch (var1) {
               case "distance" -> DISTANCE;
               case "time" -> TIME;
               default -> ACTIVATION;
            };
         }
      }
   }

   private static class SpawnAreaConfig {
      Location spawnLocation;
      double minX;
      double minY;
      double minZ;
      double maxX;
      double maxY;
      double maxZ;
      boolean useRectangular;
   }

   private static class VisualizationConfig {
      int verticalRange;
      int pillarRange;
      int updateFrequency;
      float particleSize;
      boolean enhancedParticles;
   }
}
