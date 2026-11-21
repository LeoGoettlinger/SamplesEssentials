package com.magmaguy.elitemobs.instanced.arena;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.ArenaCompleteEvent;
import com.magmaguy.elitemobs.api.ArenaStartEvent;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.ArenasConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfig;
import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfigFields;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.thirdparty.mythicmobs.MythicMobsInterface;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.EventCaller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ArenaInstance extends MatchInstance {
   private static final HashMap<String, ArenaInstance> arenaInstances = new HashMap<>();
   private static final Map<String, ArenaContainer> containers = new HashMap<>();
   private static final Map<String, List<CustomArenasConfigFields>> pendingArenas = new ConcurrentHashMap<>();
   private final HashSet<CustomBossEntity> customBosses = new HashSet<>();
   private final HashSet<Entity> nonEliteMobsEntities = new HashSet<>();
   private final HashMap<Integer, String> waveMessage = new HashMap<>();
   private final HashMap<Player, Double> roundDamage = new HashMap<>();
   private CustomArenasConfigFields customArenasConfigFields;
   private ArenaWaves arenaWaves;
   private final ArenaContainer container;
   private int currentWave = 0;
   private ArenaInstance.ArenaState arenaState = ArenaInstance.ArenaState.IDLE;
   private int highestArenaMobLevel = -1;
   private long runGeneration;
   private boolean resetting;

   public static void shutdown() {
      arenaInstances.clear();
      pendingArenas.clear();
      containers.clear();
   }

   public ArenaInstance(CustomArenasConfigFields customArenasConfigFields, Location corner1, Location corner2, Location startLocation, Location exitLocation) {
      this(customArenasConfigFields, new ArenaContainer(customArenasConfigFields, corner1, corner2, startLocation, exitLocation));
   }

   public ArenaInstance(CustomArenasConfigFields customArenasConfigFields, ArenaContainer container) {
      this(customArenasConfigFields, container, prepareWaves(customArenasConfigFields, container));
   }

   private static ArenaWaves prepareWaves(CustomArenasConfigFields fields, ArenaContainer container) {
      ArenaWaves waves = new ArenaWaves(fields.getBossList());
      waves.validate(fields.getWaveCount(), container);
      return waves;
   }

   private ArenaInstance(CustomArenasConfigFields customArenasConfigFields, ArenaContainer container, ArenaWaves waves) {
      super(container.start(), container.exit(), customArenasConfigFields.getMinimumPlayerCount(), customArenasConfigFields.getMaximumPlayerCount());
      this.container = container;
      if (!this.cancelled) {
         super.lobbyLocation = container.lobby();
         this.world = container.start().getWorld();
         this.state = MatchInstance.InstancedRegionState.WAITING;
         this.customArenasConfigFields = customArenasConfigFields;
         this.arenaWaves = waves;
         arenaInstances.put(customArenasConfigFields.getFilename(), this);
         this.arenaWatchdog();

         for (String string : customArenasConfigFields.getArenaMessages()) {
            String message = "";
            int wave = 0;
            int start = 0;

            while (start < string.length()) {
               int end = string.indexOf(58, start);
               String subString = end < 0 ? string.substring(start) : string.substring(start, end);
               String[] finalString = subString.split("=", 2);
               if (finalString.length < 2) {
                  Logger.warn("Failed to parse arena message entry " + subString + " for arena " + customArenasConfigFields.getFilename());
               } else {
                  String key = ChatColor.stripColor(finalString[0]).toLowerCase(Locale.ROOT);
                  switch (key) {
                     case "wave":
                        try {
                           wave = Integer.parseInt(finalString[1]);
                        } catch (Exception var17) {
                           Logger.warn("Failed to parse wave for entry " + subString + " for arena " + customArenasConfigFields.getFilename());
                        }
                        break;
                     case "message":
                        int textStart = start + subString.indexOf(61) + 1;
                        int nextWave = string.toLowerCase(Locale.ROOT).indexOf(":wave=", textStart);
                        end = nextWave;
                        message = nextWave < 0 ? string.substring(textStart) : string.substring(textStart, nextWave);
                        break;
                     default:
                        Logger.warn("Failed to parse arena message entry " + subString + " for arena " + customArenasConfigFields.getFilename());
                  }
               }

               if (end < 0) {
                  break;
               }

               start = end + 1;
            }

            if (!message.isEmpty() && wave > 0) {
               this.waveMessage.put(wave, ChatColorConverter.convert(message));
            }
         }

         super.permission = customArenasConfigFields.getPermission();
      }
   }

   public static void initializeArena(CustomArenasConfigFields customArenasConfigFields) {
      if (!Bukkit.isPrimaryThread()) {
         Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> initializeArena(customArenasConfigFields));
      } else if (!arenaInstances.containsKey(customArenasConfigFields.getFilename())) {
         CustomArenasConfigFields geometry;
         try {
            geometry = geometryDefinition(customArenasConfigFields);
         } catch (IllegalArgumentException var8) {
            Logger.warn(var8.getMessage());
            return;
         }

         Location corner1 = ConfigurationLocation.serialize(geometry.getCorner1());
         Location corner2 = ConfigurationLocation.serialize(geometry.getCorner2());
         Location startLocation = ConfigurationLocation.serialize(geometry.getStartLocation());
         Location exitLocation = ConfigurationLocation.serialize(geometry.getExitLocation());
         if (corner1 != null && corner2 != null && startLocation != null && exitLocation != null) {
            if (corner1.getWorld() != null && corner2.getWorld() != null && startLocation.getWorld() != null && exitLocation.getWorld() != null) {
               try {
                  ArenaContainer container = containers.computeIfAbsent(
                     geometry.getFilename(), ignored -> new ArenaContainer(geometry, corner1, corner2, startLocation, exitLocation)
                  );
                  new ArenaInstance(customArenasConfigFields, container);
               } catch (IllegalArgumentException var7) {
                  Logger.warn("Cannot initialize arena " + customArenasConfigFields.getFilename() + ": " + var7.getMessage());
               }
            } else {
               String worldName = extractWorldName(geometry.getCorner1());
               if (worldName != null) {
                  pendingArenas.computeIfAbsent(worldName.toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(customArenasConfigFields);
               }
            }
         }
      }
   }

   private static CustomArenasConfigFields geometryDefinition(CustomArenasConfigFields definition) {
      Set<String> visited = new HashSet<>();

      while (visited.add(definition.getFilename())) {
         String reference = definition.getArenaContainer();
         if (reference == null || reference.isBlank()) {
            return definition;
         }

         CustomArenasConfigFields parent = CustomArenasConfig.getCustomArena(reference);
         if (parent == null) {
            throw new IllegalArgumentException("Unknown arenaContainer '" + reference + "' in " + definition.getFilename());
         }

         definition = parent;
      }

      throw new IllegalArgumentException("Cyclic arenaContainer reference: " + visited);
   }

   private static String extractWorldName(String configurationLocationString) {
      if (configurationLocationString == null) {
         return null;
      } else {
         int comma = configurationLocationString.indexOf(44);
         return comma <= 0 ? null : configurationLocationString.substring(0, comma).trim();
      }
   }

   @Override
   protected boolean isInRegion(Location location) {
      return this.container.contains(location);
   }

   @Override
   public boolean isAcceptingNewPlayers() {
      return !this.resetting && super.isAcceptingNewPlayers() && this.container.availableTo(this);
   }

   @Override
   protected boolean reserveAdmission() {
      return this.container.acquire(this);
   }

   @Override
   protected void abortAdmission() {
      if (this.players.isEmpty() && this.spectators.isEmpty()) {
         this.container.release(this);
      }
   }

   @Override
   protected boolean isAcceptingSpectator(Player player, boolean wasPlayer) {
      return !this.isDefunct() && this.container.availableTo(this);
   }

   @Override
   public void removeSpectator(Player player) {
      super.removeSpectator(player);
      if (this.state == MatchInstance.InstancedRegionState.WAITING) {
         this.abortAdmission();
      }
   }

   @Override
   protected void startMatch() {
      super.startMatch();
      new EventCaller(new ArenaStartEvent(this));
      this.nextWave();
   }

   private void nextWave() {
      this.currentWave++;
      this.arenaState = ArenaInstance.ArenaState.COOLDOWN;
      if (this.waveMessage.get(this.currentWave) != null) {
         this.players.forEach(player -> player.sendMessage(this.waveMessage.get(this.currentWave)));
         this.spectators.forEach(player -> player.sendMessage(this.waveMessage.get(this.currentWave)));
      }

      this.doRewards();
      if (this.currentWave > this.customArenasConfigFields.getWaveCount()) {
         this.victory();
      } else {
         int delayBetweenWaves = this.customArenasConfigFields.getDelayBetweenWaves();
         if (this.customArenasConfigFields.getIntermissionWaves().contains(this.currentWave)) {
            delayBetweenWaves *= 2;
         }

         long scheduledRun = this.runGeneration;
         Bukkit.getScheduler()
            .scheduleSyncDelayedTask(
               MetadataHandler.PLUGIN,
               () -> {
                  if (scheduledRun == this.runGeneration && this.arenaState != ArenaInstance.ArenaState.IDLE) {
                     String title = ArenasConfig.getWaveTitle();
                     String subtitle = ArenasConfig.getWaveSubtitle();
                     if (title == null) {
                        title = "";
                     }

                     if (subtitle == null) {
                        subtitle = "";
                     }

                     String finalTitle = title;
                     String finalSubtitle = subtitle;
                     this.players
                        .forEach(
                           player -> player.sendTitle(
                              finalTitle.replace("$wave", this.currentWave + ""), finalSubtitle.replace("$wave", this.currentWave + ""), 0, 20, 0
                           )
                        );
                     this.spectators
                        .forEach(
                           player -> player.sendTitle(
                              finalTitle.replace("$wave", this.currentWave + ""), finalSubtitle.replace("$wave", this.currentWave + ""), 0, 20, 0
                           )
                        );

                     try {
                        if (!this.spawnBosses()) {
                           Logger.warn(
                              "Arena "
                                 + this.customArenasConfigFields.getFilename()
                                 + " wave "
                                 + this.currentWave
                                 + " could not spawn its required mobs. Ending this run without completing the wave."
                           );
                           if (scheduledRun == this.runGeneration && this.arenaState != ArenaInstance.ArenaState.IDLE) {
                              this.defeat();
                           }

                           return;
                        }
                     } catch (RuntimeException var8) {
                        Logger.warn("Arena wave admission failed: " + var8.getMessage());
                        if (scheduledRun == this.runGeneration && this.arenaState != ArenaInstance.ArenaState.IDLE) {
                           this.defeat();
                        }

                        return;
                     }

                     if (scheduledRun == this.runGeneration && this.arenaState != ArenaInstance.ArenaState.IDLE) {
                        this.arenaState = ArenaInstance.ArenaState.ACTIVE;
                        this.roundDamage.clear();
                     }
                  }
               },
               20L * delayBetweenWaves
            );
      }
   }

   private void arenaWatchdog() {
      (new BukkitRunnable() {
         public void run() {
            if (ArenaInstance.this.arenaState == ArenaInstance.ArenaState.ACTIVE) {
               for (CustomBossEntity customBossEntity : (HashSet)ArenaInstance.this.customBosses.clone()) {
                  if (!customBossEntity.exists()) {
                     ArenaInstance.this.removeBoss(customBossEntity);
                  }
               }

               if (!ArenaInstance.this.nonEliteMobsEntities.isEmpty()) {
                  for (Entity entity : (HashSet)ArenaInstance.this.nonEliteMobsEntities.clone()) {
                     if (!entity.isValid()) {
                        ArenaInstance.this.removeBoss(entity);
                     }
                  }
               }
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 20L);
   }

   public void removeBoss(CustomBossEntity customBossEntity) {
      this.customBosses.remove(customBossEntity);
      if (this.arenaState == ArenaInstance.ArenaState.ACTIVE && this.customBosses.isEmpty() && this.nonEliteMobsEntities.isEmpty()) {
         this.nextWave();
      }
   }

   public void removeBoss(Entity nonEliteEntity) {
      this.nonEliteMobsEntities.remove(nonEliteEntity);
      if (this.arenaState == ArenaInstance.ArenaState.ACTIVE && this.customBosses.isEmpty() && this.nonEliteMobsEntities.isEmpty()) {
         this.nextWave();
      }
   }

   private boolean spawnBosses() {
      if (this.arenaWaves.getWaveEntities(this.currentWave) == null) {
         return false;
      } else {
         long generation = this.runGeneration;
         this.highestArenaMobLevel = -1;

         for (ArenaEntity arenaEntity : this.arenaWaves.getWaveEntities(this.currentWave)) {
            if (!arenaEntity.isMythicMob()) {
               CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(arenaEntity.getBossfile());
               if (customBossEntity == null) {
                  Logger.warn("Failed to generate custom boss " + arenaEntity.getBossfile() + " because the filename was not valid!");
                  return false;
               }

               customBossEntity.setNormalizedCombat();
               customBossEntity.setEliteLoot(false);
               customBossEntity.setVanillaLoot(false);
               customBossEntity.setRandomLoot(false);
               customBossEntity.spawn(this.container.spawnPoint(arenaEntity.getSpawnPointName()), true);
               if (generation != this.runGeneration || this.arenaState == ArenaInstance.ArenaState.IDLE) {
                  customBossEntity.remove(RemovalReason.OTHER);
                  return false;
               }

               if (customBossEntity.getLevel() > this.highestArenaMobLevel) {
                  this.highestArenaMobLevel = customBossEntity.getLevel();
               }

               if (!customBossEntity.exists()) {
                  Logger.warn(
                     "Arena "
                        + this.getCustomArenasConfigFields().getArenaName()
                        + " failed to spawn boss "
                        + customBossEntity.getCustomBossesConfigFields().getFilename()
                  );
                  customBossEntity.remove(RemovalReason.OTHER);
                  return false;
               }

               this.customBosses.add(customBossEntity);
            } else {
               try {
                  Entity mythicMob = MythicMobsInterface.spawn(
                     this.container.spawnPoint(arenaEntity.getSpawnPointName()), arenaEntity.getBossfile(), arenaEntity.getLevel()
                  );
                  if (mythicMob == null || generation != this.runGeneration || this.arenaState == ArenaInstance.ArenaState.IDLE) {
                     if (mythicMob != null) {
                        mythicMob.remove();
                     }

                     Logger.warn(
                        "Failed to spawn MythicMobs entity '"
                           + arenaEntity.getBossfile()
                           + "' at spawn point "
                           + arenaEntity.getSpawnPointName()
                           + " with level "
                           + arenaEntity.getLevel()
                           + " because MythicMobs did not recognize the name of the entity!"
                     );
                     return false;
                  }

                  this.nonEliteMobsEntities.add(mythicMob);
               } catch (Exception var6) {
                  Logger.warn(
                     "Failed to spawn MythicMobs entity '"
                        + arenaEntity.getBossfile()
                        + "' at spawn point "
                        + arenaEntity.getSpawnPointName()
                        + " with level "
                        + arenaEntity.getLevel()
                        + " due to a MythicMobs error - there is a high chance mob spawning is being prevented in this area!"
                  );
                  return false;
               }
            }
         }

         return !this.customBosses.isEmpty() || !this.nonEliteMobsEntities.isEmpty();
      }
   }

   private void doRewards() {
      ArrayList<Player> validPlayers = new ArrayList<>();
      double minimumDamageThreshold = 0.0;
      double totalDamage = 0.0;

      for (Entry<Player, Double> entry : this.roundDamage.entrySet()) {
         totalDamage += entry.getValue();
      }

      minimumDamageThreshold = totalDamage * 0.1;

      for (Entry<Player, Double> entry : this.roundDamage.entrySet()) {
         if (entry.getValue() >= minimumDamageThreshold) {
            validPlayers.add(entry.getKey());
         }
      }

      super.players
         .forEach(
            player -> {
               if (this.highestArenaMobLevel > 0
                  && Math.abs(ElitePlayerInventory.getPlayer(player).getFullPlayerTier(true) - this.highestArenaMobLevel)
                     > ItemSettingsConfig.getLootLevelDifferenceLockout()) {
                  Logger.sendSimpleMessage(
                     player,
                     ItemSettingsConfig.getLevelRangeTooDifferent()
                        .replace("$playerLevel", ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(false) + "")
                        .replace("$bossLevel", this.highestArenaMobLevel + "")
                  );
               } else {
                  this.customArenasConfigFields.getArenaRewards().arenaReward(player, this.currentWave - 1);
               }
            }
         );
   }

   @Override
   protected void endMatch() {
      super.endMatch();
      this.arenaState = ArenaInstance.ArenaState.IDLE;
      if (this.currentWave > this.getCustomArenasConfigFields().getWaveCount()) {
         this.participants
            .forEach(
               playerx -> playerx.sendTitle(
                  ArenasConfig.getVictoryTitle().replace("$wave", this.customArenasConfigFields.getWaveCount() + ""),
                  ArenasConfig.getVictorySubtitle().replace("$wave", this.customArenasConfigFields.getWaveCount() + ""),
                  20,
                  200,
                  20
               )
            );
         StringBuilder playerNames = new StringBuilder();

         for (Player player : this.participants) {
            playerNames.append(player.getName()).append(" ");
         }

         Bukkit.getServer()
            .broadcastMessage(
               ArenasConfig.getVictoryBroadcast()
                  .replace("$players", playerNames.toString())
                  .replace("$arenaName", this.customArenasConfigFields.getArenaName())
            );
         ArenaCompleteEvent.ArenaCompleteEventHandler.call(this);
      } else {
         this.participants
            .forEach(
               playerx -> playerx.sendTitle(
                  ArenasConfig.getDefeatTitle().replace("$wave", this.currentWave + ""),
                  ArenasConfig.getDefeatSubtitle().replace("$wave", this.currentWave + ""),
                  20,
                  200,
                  20
               )
            );
      }

      long endingRun = this.runGeneration;
      Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, () -> {
         if (endingRun == this.runGeneration) {
            this.destroyMatch();
         }
      }, this.customArenasConfigFields.getDelayBetweenWaves());
   }

   @Override
   protected void destroyMatch() {
      if (!this.resetting) {
         this.resetting = true;
         this.runGeneration++;

         try {
            this.arenaState = ArenaInstance.ArenaState.IDLE;
            this.currentWave = 0;
            new HashSet<>(this.customBosses).forEach(customBoss -> customBoss.remove(RemovalReason.ARENA_RESET));
            this.customBosses.clear();
            this.nonEliteMobsEntities.forEach(Entity::remove);
            this.nonEliteMobsEntities.clear();
            super.destroyMatch();
            this.container.release(this);
         } finally {
            this.resetting = false;
         }
      }
   }

   @Generated
   public static HashMap<String, ArenaInstance> getArenaInstances() {
      return arenaInstances;
   }

   @Generated
   public HashSet<CustomBossEntity> getCustomBosses() {
      return this.customBosses;
   }

   @Generated
   public HashMap<Player, Double> getRoundDamage() {
      return this.roundDamage;
   }

   @Generated
   public CustomArenasConfigFields getCustomArenasConfigFields() {
      return this.customArenasConfigFields;
   }

   @Generated
   public ArenaWaves getArenaWaves() {
      return this.arenaWaves;
   }

   @Generated
   public ArenaContainer getContainer() {
      return this.container;
   }

   @Generated
   public int getCurrentWave() {
      return this.currentWave;
   }

   @Generated
   public ArenaInstance.ArenaState getArenaState() {
      return this.arenaState;
   }

   public static class ArenaInstanceEvents implements Listener {
      @EventHandler
      public void onEliteDeath(EliteMobDeathEvent event) {
         if (event.getEliteEntity() instanceof CustomBossEntity) {
            for (ArenaInstance arenaInstance : ArenaInstance.arenaInstances.values()) {
               if (arenaInstance.getCustomBosses().contains((CustomBossEntity)event.getEliteEntity())) {
                  arenaInstance.removeBoss((CustomBossEntity)event.getEliteEntity());
                  return;
               }
            }
         }
      }

      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.HIGHEST
      )
      public void onEliteDamages(EliteMobDamagedByPlayerEvent event) {
         if (PlayerData.getMatchInstance(event.getPlayer()) != null) {
            if (PlayerData.getMatchInstance(event.getPlayer()) instanceof ArenaInstance arenaInstance) {
               arenaInstance.getRoundDamage().merge(event.getPlayer(), event.getDamage(), Double::sum);
            }
         }
      }
   }

   public static class ArenaInstanceLoader implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onWorldLoad(WorldLoadEvent event) {
         String worldKey = event.getWorld().getName().toLowerCase(Locale.ROOT);
         List<CustomArenasConfigFields> pending = ArenaInstance.pendingArenas.remove(worldKey);
         if (pending != null && !pending.isEmpty()) {
            for (CustomArenasConfigFields fields : pending) {
               ArenaInstance.initializeArena(fields);
            }
         }
      }
   }

   private static enum ArenaState {
      IDLE,
      COOLDOWN,
      ACTIVE;
   }
}
