package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.PeaceBannerConfig;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfig;
import com.magmaguy.elitemobs.config.customspawns.CustomSpawnConfigFields;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import com.magmaguy.elitemobs.events.TimedEvent;
import com.magmaguy.elitemobs.magmacore.match.Match;
import com.magmaguy.elitemobs.magmacore.match.MatchCore;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.peacebanner.PeaceBannerManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class CustomSpawn {
   private static final int MAX_LOCATION_ATTEMPTS = 100;
   private static final int LOCATION_ATTEMPTS_PER_TICK = 1;
   private final CustomSpawnConfigFields customSpawnConfigFields;
   private final ArrayList<CustomBossEntity> customBossEntities = new ArrayList<>();
   private boolean isEvent = false;
   private World world;
   private TimedEvent timedEvent;
   private BukkitTask spawnLocationSearchTask;
   private BukkitTask spawnTask;
   private Location spawnLocation;
   private boolean keepTrying = true;

   private static boolean isMobSpawningDisabled(World world) {
      Boolean spawnMobs = getMobSpawningRuleValue(world, "SPAWN_MOBS");
      if (spawnMobs != null) {
         return Boolean.FALSE.equals(spawnMobs);
      } else {
         Boolean legacyRule = getMobSpawningRuleValue(world, "DO_MOB_SPAWNING");
         return Boolean.FALSE.equals(legacyRule);
      }
   }

   private static Boolean getMobSpawningRuleValue(World world, String fieldName) {
      try {
         return GameRule.class.getField(fieldName).get(null) instanceof GameRule<?> gameRule ? (Boolean)world.getGameRuleValue(gameRule) : null;
      } catch (ReflectiveOperationException var4) {
         return null;
      }
   }

   public CustomSpawn(String customSpawnConfig, List<String> customBossesFilenames, TimedEvent timedEvent) {
      this.customSpawnConfigFields = CustomSpawnConfig.getCustomEvent(customSpawnConfig);
      this.timedEvent = timedEvent;
      if (this.customSpawnConfigFields == null) {
         Logger.warn("Invalid custom spawn detected for file " + customSpawnConfig + " in event " + timedEvent.getCustomEventsConfigFields().getFilename());
      } else {
         customBossesFilenames.forEach(bossString -> {
            CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(bossString);
            if (customBossesConfigFields == null) {
               Logger.warn("Attempted to pass invalid boss into CustomSpawn: " + bossString);
            } else {
               CustomBossEntity customBossEntity = new CustomBossEntity(customBossesConfigFields);
               this.customBossEntities.add(customBossEntity);
               customBossEntity.setCustomSpawn(this);
            }
         });
      }
   }

   public CustomSpawn(String customSpawnConfig, CustomBossEntity customBossEntity) {
      this.customSpawnConfigFields = CustomSpawnConfig.getCustomEvent(customSpawnConfig);
      if (this.customSpawnConfigFields == null) {
         Logger.warn("Invalid custom spawn detected for file " + customSpawnConfig);
      } else {
         this.customBossEntities.add(customBossEntity);
         customBossEntity.setCustomSpawn(this);
      }
   }

   public void setKeepTrying(boolean keepTrying) {
      this.keepTrying = keepTrying;
      if (!keepTrying) {
         if (this.spawnLocationSearchTask != null) {
            this.spawnLocationSearchTask.cancel();
            this.spawnLocationSearchTask = null;
         }

         if (this.spawnTask != null) {
            this.spawnTask.cancel();
            this.spawnTask = null;
         }

         this.releaseSummoningEntities();
      }
   }

   public static int getHighestValidBlock(Location location, int highestYLevel) {
      World world = location.getWorld();
      if (world == null) {
         return Integer.MIN_VALUE;
      } else {
         int maximum = Math.min(highestYLevel, world.getMaxHeight() - 2);

         for (int height = Math.max(location.getBlockY(), world.getMinHeight() + 1); height <= maximum; height++) {
            if (!world.getBlockAt(location.getBlockX(), height - 1, location.getBlockZ()).isPassable()
               && world.getBlockAt(location.getBlockX(), height, location.getBlockZ()).getType().isAir()
               && world.getBlockAt(location.getBlockX(), height + 1, location.getBlockZ()).getType().isAir()) {
               return height;
            }
         }

         return Integer.MIN_VALUE;
      }
   }

   public void queueSpawn() {
      if (!Bukkit.isPrimaryThread()) {
         Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, this::queueSpawn);
      } else if (this.keepTrying) {
         if (this.spawnLocation == null) {
            this.generateCustomSpawn();
         } else {
            this.spawn();
         }
      }
   }

   private void spawn() {
      if (this.keepTrying && this.spawnTask == null) {
         this.spawnTask = (new BukkitRunnable() {
               public void run() {
                  if (CustomSpawn.this.spawnLocation != null && CustomSpawn.this.spawnLocation.getWorld() != null) {
                     World spawnWorld = CustomSpawn.this.spawnLocation.getWorld();
                     if (spawnWorld.getTime() >= CustomSpawn.this.customSpawnConfigFields.getEarliestTime()
                        && spawnWorld.getTime() <= CustomSpawn.this.customSpawnConfigFields.getLatestTime()) {
                        if (CustomSpawn.this.customSpawnConfigFields.getMoonPhase() == null
                           || MoonPhaseDetector.detectMoonPhase(CustomSpawn.this.spawnLocation.getWorld())
                              .equals(CustomSpawn.this.customSpawnConfigFields.getMoonPhase())) {
                           if (!CustomSpawn.this.keepTrying) {
                              this.stopSpawning();
                              CustomSpawn.this.releaseSummoningEntities();
                           } else {
                              if (CustomSpawn.this.timedEvent == null) {
                                 this.spawnPreparedBosses();
                              } else {
                                 CustomSpawn.this.timedEvent.trySpawn(this::spawnPreparedBosses);
                              }
                           }
                        }
                     }
                  } else {
                     CustomSpawn.this.scheduleLocationSearchRetry(1L);
                     this.stopSpawning();
                  }
               }

               private void spawnPreparedBosses() {
                  LivingEntity testEntity = (LivingEntity)CustomSpawn.this.spawnLocation
                     .getWorld()
                     .spawn(CustomSpawn.this.spawnLocation, Zombie.class, spawnEntity -> spawnEntity.setAdult());
                  if (!testEntity.isValid()) {
                     CustomSpawn.this.spawnLocation = null;
                     CustomSpawn.this.scheduleLocationSearchRetry(1L);
                     this.stopSpawning();
                  } else {
                     testEntity.remove();
                     if (!CustomSpawn.this.keepTrying) {
                        this.stopSpawning();
                        CustomSpawn.this.releaseSummoningEntities();
                     } else if (PeaceBannerConfig.isSuppressEvents() && PeaceBannerManager.isProtected(CustomSpawn.this.spawnLocation)) {
                        this.stopSpawning();
                        if (CustomSpawn.this.timedEvent != null) {
                           CustomSpawn.this.timedEvent.queueEvent();
                        } else {
                           CustomSpawn.this.setKeepTrying(false);
                        }
                     } else {
                        for (CustomBossEntity customBossEntity : CustomSpawn.this.customBossEntities) {
                           if (!customBossEntity.exists()) {
                              customBossEntity.spawn(CustomSpawn.this.spawnLocation, CustomSpawn.this.isEvent);
                           }

                           if (!CustomSpawn.this.keepTrying || CustomSpawn.this.timedEvent == null && !customBossEntity.exists()) {
                              customBossEntity.remove(RemovalReason.REINFORCEMENT_CULL);
                              break;
                           }
                        }

                        this.stopSpawning();
                        if (CustomSpawn.this.timedEvent != null) {
                           CustomSpawn.this.timedEvent.queueEvent();
                        }
                     }
                  }
               }

               private void stopSpawning() {
                  this.cancel();
                  CustomSpawn.this.spawnTask = null;
               }
            })
            .runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
      }
   }

   private void generateCustomSpawn() {
      if (!Bukkit.isPrimaryThread()) {
         Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, this::generateCustomSpawn);
      } else if (this.spawnLocationSearchTask == null) {
         if (!this.keepTrying) {
            this.releaseSummoningEntities();
         } else if (this.timedEvent == null
            || this.timedEvent.startConditions.areValid() && !(System.currentTimeMillis() < TimedEvent.getNextEventStartMinimum())) {
            this.spawnLocationSearchTask = (new BukkitRunnable() {
               private int attempts;

               public void run() {
                  if (!CustomSpawn.this.keepTrying) {
                     this.stopSearch();
                     CustomSpawn.this.releaseSummoningEntities();
                  } else {
                     for (int attempt = 0; attempt < 1 && this.attempts < 100; attempt++) {
                        this.attempts++;
                        CustomSpawn.this.spawnLocation = CustomSpawn.this.generateRandomSpawnLocation();
                        if (CustomSpawn.this.spawnLocation != null) {
                           this.stopSearch();
                           CustomSpawn.this.spawn();
                           return;
                        }
                     }

                     if (this.attempts >= 100) {
                        this.stopSearch();
                        CustomSpawn.this.scheduleLocationSearchRetry();
                     }
                  }
               }

               private void stopSearch() {
                  this.cancel();
                  CustomSpawn.this.spawnLocationSearchTask = null;
               }
            }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
         } else {
            this.scheduleLocationSearchRetry();
         }
      }
   }

   private void scheduleLocationSearchRetry() {
      this.scheduleLocationSearchRetry(1200L);
   }

   private void scheduleLocationSearchRetry(long delay) {
      if (this.keepTrying && this.spawnLocationSearchTask == null) {
         this.spawnLocationSearchTask = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
            this.spawnLocationSearchTask = null;
            this.generateCustomSpawn();
         }, delay);
      }
   }

   private void releaseSummoningEntities() {
      for (CustomBossEntity customBossEntity : this.customBossEntities) {
         if (customBossEntity.summoningEntity != null) {
            customBossEntity.summoningEntity.removeReinforcement(customBossEntity);
         }
      }
   }

   public Location generateRandomSpawnLocation() {
      if (this.customSpawnConfigFields == null) {
         Logger.warn("Something tried to spawn but has invalid custom spawn config fields! This isn't good.", true);
         Logger.warn("Bosses: ");
         this.getCustomBossEntities().forEach(customBossEntity -> {
            if (customBossEntity != null && customBossEntity.getName() != null) {
               Logger.warn(customBossEntity.getCustomBossesConfigFields().getName());
            }
         });
         if (this.timedEvent != null) {
            Logger.warn("Event: " + this.timedEvent.getCustomEventsConfigFields().getFilename());
            this.timedEvent.end();
         }

         return null;
      } else if (Bukkit.getOnlinePlayers().isEmpty()) {
         this.setKeepTrying(false);
         if (this.timedEvent != null) {
            this.timedEvent.end();
         }

         return null;
      } else {
         List<Player> validPlayers = new ArrayList<>();
         if (this.world == null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
               if (PlayerData.isInMemory(player.getUniqueId())) {
                  Location playerLocation = player.getLocation();
                  if (ValidWorldsConfig.getValidWorlds().contains(playerLocation.getWorld().getName())) {
                     boolean isInMatch = false;

                     for (Match match : MatchCore.matches()) {
                        if (match.contains(playerLocation)) {
                           isInMatch = true;
                           break;
                        }
                     }

                     if (!isInMatch
                        && (this.timedEvent == null || !EliteMobsWorld.isEliteMobsWorld(player.getWorld().getUID()))
                        && !isMobSpawningDisabled(playerLocation.getWorld())
                        && (
                           this.customSpawnConfigFields.getValidWorlds().isEmpty()
                              || this.customSpawnConfigFields.getValidWorlds().contains(playerLocation.getWorld())
                        )
                        && (
                           this.customSpawnConfigFields.getValidWorldEnvironments().isEmpty()
                              || this.customSpawnConfigFields
                                 .getValidWorldEnvironments()
                                 .contains(Objects.requireNonNull(playerLocation.getWorld()).getEnvironment())
                        )) {
                        validPlayers.add(player);
                     }
                  }
               }
            }
         } else {
            validPlayers.addAll(this.world.getPlayers());
         }

         if (validPlayers.isEmpty()) {
            return null;
         } else {
            Player selectedPlayer = validPlayers.get(ThreadLocalRandom.current().nextInt(validPlayers.size()));
            Vector randomizedVector = new Vector(ThreadLocalRandom.current().nextInt(24, 128), 0, ThreadLocalRandom.current().nextInt(24, 128));
            if (ThreadLocalRandom.current().nextBoolean()) {
               randomizedVector.setX(randomizedVector.getX() * -1.0);
            }

            if (ThreadLocalRandom.current().nextBoolean()) {
               randomizedVector.setZ(randomizedVector.getZ() * -1.0);
            }

            Location location = selectedPlayer.getLocation().clone().add(randomizedVector);
            location.setY(ThreadLocalRandom.current().nextInt(0, 256));
            World world = location.getWorld();
            if (world == null || !world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
               return null;
            } else if (!this.customSpawnConfigFields.getValidBiomes().isEmpty()
               && !this.customSpawnConfigFields.getValidBiomes().contains(location.getBlock().getBiome())) {
               return null;
            } else {
               if (this.customSpawnConfigFields.isSurfaceSpawn()) {
                  location = Objects.requireNonNull(location.getWorld()).getHighestBlockAt(location).getLocation().add(new Vector(0.5, 1.0, 0.5));
               } else if (!this.customSpawnConfigFields.isUndergroundSpawn()) {
                  int height = getHighestValidBlock(location, this.customSpawnConfigFields.getHighestYLevel());
                  if (height == Integer.MIN_VALUE) {
                     return null;
                  }

                  location.setY(height);
               } else {
                  int highestBlockYAt = Objects.requireNonNull(location.getWorld()).getHighestBlockYAt(location);
                  Location undergroundLocation = null;
                  if (!(location.getY() > highestBlockYAt) && !(location.getY() > highestBlockYAt / 2.0)) {
                     for (int y = (int)location.getY(); y < highestBlockYAt; y++) {
                        Location tempLocation = location.clone();
                        tempLocation.setY(y);
                        if (tempLocation.getBlock().getType().equals(Material.VOID_AIR)) {
                           return null;
                        }

                        if (y < this.customSpawnConfigFields.getLowestYLevel()) {
                           return null;
                        }

                        Block groundBlock = tempLocation.clone().subtract(new Vector(0, 1, 0)).getBlock();
                        if (groundBlock.getType().isSolid()
                           && tempLocation.getBlock().getType().isAir()
                           && tempLocation.clone().add(new Vector(0, 1, 0)).getBlock().getType().isAir()) {
                           undergroundLocation = tempLocation;
                           break;
                        }
                     }
                  } else {
                     for (int y = (int)location.getY(); y > -64; y--) {
                        Location tempLocationx = location.clone();
                        tempLocationx.setY(y);
                        if (tempLocationx.getBlock().getType().equals(Material.VOID_AIR)) {
                           return null;
                        }

                        if (y < this.customSpawnConfigFields.getLowestYLevel()) {
                           return null;
                        }

                        Block groundBlock = tempLocationx.clone().subtract(new Vector(0, 1, 0)).getBlock();
                        if (groundBlock.getType().isSolid()
                           && tempLocationx.getBlock().getType().isAir()
                           && tempLocationx.clone().add(new Vector(0, 1, 0)).getBlock().getType().isAir()) {
                           undergroundLocation = tempLocationx;
                           break;
                        }
                     }
                  }

                  if (undergroundLocation == null) {
                     return null;
                  }

                  location = undergroundLocation;
               }

               assert world != null;

               for (Player playerx : world.getPlayers()) {
                  if (playerx.getLocation().distanceSquared(location) < Math.pow(24.0, 2.0)) {
                     return null;
                  }
               }

               if (location.getY() > 127.0 && world.getEnvironment().equals(Environment.NETHER)) {
                  return null;
               } else if (!(location.getY() > this.customSpawnConfigFields.getHighestYLevel())
                  && !(location.getY() < this.customSpawnConfigFields.getLowestYLevel())) {
                  if (EliteMobs.worldGuardIsEnabled) {
                     if (!WorldGuardFlagChecker.doEventFlag(location)) {
                        return null;
                     }

                     if (!WorldGuardFlagChecker.doEliteMobsSpawnFlag(location)) {
                        return null;
                     }

                     if (!WorldGuardFlagChecker.doMobSpawnFlag(location)) {
                        return null;
                     }
                  }

                  return !this.customSpawnConfigFields.isCanSpawnInLight() && location.getBlock().getLightLevel() > 8 ? null : location;
               } else {
                  return null;
               }
            }
         }
      }
   }

   @Generated
   public CustomSpawnConfigFields getCustomSpawnConfigFields() {
      return this.customSpawnConfigFields;
   }

   @Generated
   public ArrayList<CustomBossEntity> getCustomBossEntities() {
      return this.customBossEntities;
   }

   @Generated
   public boolean isEvent() {
      return this.isEvent;
   }

   @Generated
   public void setEvent(boolean isEvent) {
      this.isEvent = isEvent;
   }

   @Generated
   public World getWorld() {
      return this.world;
   }

   @Generated
   public void setWorld(World world) {
      this.world = world;
   }

   @Generated
   public Location getSpawnLocation() {
      return this.spawnLocation;
   }

   @Generated
   public void setSpawnLocation(Location spawnLocation) {
      this.spawnLocation = spawnLocation;
   }
}
