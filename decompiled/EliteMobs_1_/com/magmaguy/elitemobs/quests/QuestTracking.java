package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.QuestAcceptEvent;
import com.magmaguy.elitemobs.api.QuestProgressionEvent;
import com.magmaguy.elitemobs.api.QuestRewardEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.parties.PartySidebar;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.objectives.ClassUnlockObjective;
import com.magmaguy.elitemobs.quests.objectives.CustomFetchObjective;
import com.magmaguy.elitemobs.quests.objectives.CustomKillObjective;
import com.magmaguy.elitemobs.quests.objectives.DialogObjective;
import com.magmaguy.elitemobs.quests.objectives.KillObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.BossBarOrderManager;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.SimpleScoreboard;
import com.magmaguy.elitemobs.wormhole.WormholeNavigation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class QuestTracking {
   private static final double VERTICAL_ENTER_RADIUS = 4.0;
   private static final double VERTICAL_EXIT_RADIUS = 6.0;
   private static final double VERTICAL_ENTER_HEIGHT = 5.0;
   private static final double VERTICAL_EXIT_HEIGHT = 3.0;
   private static final HashMap<UUID, QuestTracking> playerTrackingQuests = new HashMap<>();
   private final Player player;
   private final Quest quest;
   private final List<Location> turnInNPCs = new ArrayList<>();
   private List<QuestTracking.ObjectiveDestinations> objectiveDestinations = new ArrayList<>();
   private BukkitTask locationRefresher;
   private BukkitTask compassTask;
   private BossBar compassBar;
   private boolean questIsDone = false;
   private boolean stopped = false;
   private boolean wasWaiting;
   private boolean refreshQueued;
   private Set<QuestTracking.VerticalDestination> previousVerticalDestinations = new HashSet<>();
   private Set<QuestTracking.VerticalDestination> verticalDestinations = new HashSet<>();
   private boolean targetsAbove;
   private boolean targetsBelow;

   private void queueLocationRefresh() {
      if (!this.stopped && !this.refreshQueued) {
         this.refreshQueued = true;
         Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            this.refreshQueued = false;
            if (!this.stopped && this.player.isOnline()) {
               this.updateLocations(this.quest);
            }
         });
      }
   }

   public QuestTracking(Player player, Quest quest) {
      this.player = player;
      this.quest = quest;
      this.startLocationGetter();
      this.startCompass();
      playerTrackingQuests.put(player.getUniqueId(), this);
      quest.getQuestObjectives().displayLazyObjectivesScoreboard(player);
   }

   public static boolean isTracking(Player player) {
      return playerTrackingQuests.containsKey(player.getUniqueId());
   }

   public static void shutdown() {
      new HashMap<>(playerTrackingQuests).values().forEach(QuestTracking::stop);
   }

   public static void toggleTracking(Player player, String questID) {
      Quest quest = PlayerData.getQuest(player.getUniqueId(), questID);
      if (quest == null) {
         player.sendMessage(QuestsConfig.getQuestTrackingInvalidMessage());
      } else {
         toggleTracking(player, quest);
      }
   }

   public static void toggleTracking(Player player, Quest quest) {
      if (playerTrackingQuests.containsKey(player.getUniqueId())) {
         playerTrackingQuests.get(player.getUniqueId()).stop();
      } else {
         if (quest == null) {
            player.sendMessage(QuestsConfig.getQuestTrackingInvalidMessage());
            return;
         }

         if (quest instanceof CustomQuest customQuest && !customQuest.getCustomQuestsConfigFields().isTrackable()) {
            return;
         }

         new QuestTracking(player, quest);
      }
   }

   public void refreshScoreboard() {
      this.quest.getQuestObjectives().displayLazyObjectivesScoreboard(this.player);
   }

   private void startLocationGetter() {
      this.locationRefresher = (new BukkitRunnable() {
         public void run() {
            if (!QuestTracking.this.player.isValid()) {
               QuestTracking.this.stop();
            } else {
               QuestTracking.this.updateLocations(QuestTracking.this.quest);
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1200L);
   }

   public void updateLocations(Quest quest) {
      List<QuestTracking.ObjectiveDestinations> destinations = new ArrayList<>();
      QuestTracking.DestinationLookup lookup = new QuestTracking.DestinationLookup();
      if (!quest.getQuestObjectives().isOver()) {
         this.questIsDone = false;
         this.turnInNPCs.clear();

         for (Objective objective : quest.getQuestObjectives().getObjectives()) {
            if (objective != null && !objective.isObjectiveCompleted()) {
               if (objective instanceof CustomKillObjective) {
                  destinations.addAll(this.getKillLocations((CustomKillObjective)objective, lookup));
               } else if (objective instanceof DialogObjective) {
                  destinations.addAll(this.getDialogLocations((DialogObjective)objective));
               } else if (objective instanceof ClassUnlockObjective unlock && unlock.getNpcFilename() != null) {
                  destinations.add(new QuestTracking.ObjectiveDestinations(unlock, this.getNPCLocations(unlock.getNpcFilename())));
               } else if (objective instanceof CustomFetchObjective) {
                  destinations.addAll(this.getFetchLocations((CustomFetchObjective)objective, lookup));
               }
            }
         }

         this.objectiveDestinations = destinations;
      } else {
         this.questIsDone = true;
         this.getTurnInNPC();
      }
   }

   private List<QuestTracking.ObjectiveDestinations> getKillLocations(CustomKillObjective customKillObjective, QuestTracking.DestinationLookup lookup) {
      List<QuestTracking.ObjectiveDestinations> destinations = new ArrayList<>();
      List<Location> locations = this.getCustomBossLocations(customKillObjective.getCustomBossFilename(), lookup);
      destinations.add(new QuestTracking.ObjectiveDestinations(customKillObjective, locations));
      return destinations;
   }

   private List<QuestTracking.ObjectiveDestinations> getDialogLocations(DialogObjective dialogObjective) {
      List<QuestTracking.ObjectiveDestinations> destinations = new ArrayList<>();
      destinations.add(new QuestTracking.ObjectiveDestinations(dialogObjective, this.getNPCLocations(dialogObjective.getNpcFilename())));
      return destinations;
   }

   private List<QuestTracking.ObjectiveDestinations> getFetchLocations(CustomFetchObjective objective, QuestTracking.DestinationLookup lookup) {
      List<Location> locations = new ArrayList<>();

      for (String filename : CustomBossesConfig.bossesDropping(objective.getKey())) {
         this.getCustomBossLocations(filename, lookup).forEach(location -> this.addLocation(locations, location));
      }

      for (TreasureChest chest : TreasureChest.chestsDropping(objective.getKey())) {
         this.addLocation(locations, chest.getLocation());
      }

      return List.of(new QuestTracking.ObjectiveDestinations(objective, locations));
   }

   private void getTurnInNPC() {
      this.turnInNPCs.clear();
      this.turnInNPCs.addAll(this.getNPCLocations(this.quest.getQuestTaker()));
   }

   private List<Location> getNPCLocations(String npcFilename) {
      List<Location> locations = new ArrayList<>();
      if (npcFilename == null) {
         return locations;
      } else {
         NPCsConfigFields npcsConfigFields = NPCsConfig.getNpcEntities().get(npcFilename);
         if (npcsConfigFields != null && npcsConfigFields.isEnabled()) {
            for (NPCEntity npc : EntityTracker.getNpcEntities().values()) {
               if (npcFilename.equals(npc.getNPCsConfigFields().getFilename())) {
                  Location location = npc.getPersistentLocation();
                  if (location != null && this.player.getWorld().equals(location.getWorld())) {
                     this.addLocation(locations, location);
                  }
               }
            }

            if (!locations.isEmpty()) {
               return locations;
            } else if (!npcsConfigFields.isInstanced()) {
               this.addLocationStrings(locations, npcsConfigFields.getLocations());
               this.addLocationString(locations, npcsConfigFields.getSpawnLocation());
               return locations;
            } else {
               if (PlayerData.getMatchInstance(this.player) instanceof DungeonInstance dungeon && this.player.getWorld().equals(dungeon.getWorld())) {
                  List<String> authored = new ArrayList<>();
                  if (npcsConfigFields.getLocations() != null) {
                     authored.addAll(npcsConfigFields.getLocations());
                  }

                  if (npcsConfigFields.getSpawnLocation() != null) {
                     authored.add(npcsConfigFields.getSpawnLocation());
                  }

                  for (String raw : authored) {
                     if (raw != null && !raw.isBlank() && dungeon.getContentPackagesConfigFields().getWorldName().equals(ConfigurationLocation.worldName(raw))) {
                        this.addLocation(locations, ConfigurationLocation.serializeWithInstance(dungeon.getWorld(), raw));
                     }
                  }
               }

               return locations;
            }
         } else {
            return locations;
         }
      }
   }

   private List<Location> getCustomBossLocations(String customBossFilename, QuestTracking.DestinationLookup lookup) {
      List<Location> cached = lookup.resolved.get(customBossFilename);
      if (cached != null) {
         return cached;
      } else {
         List<Location> locations = new ArrayList<>();
         if (customBossFilename == null) {
            return locations;
         } else {
            CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(customBossFilename);
            if (customBossesConfigFields != null) {
               RegionalBossEntity.getRegionalBossEntities(customBossesConfigFields)
                  .forEach(regionalBossEntity -> this.addRegionalBossLocation(locations, regionalBossEntity));
               if (locations.isEmpty()) {
                  this.addLocationStrings(locations, customBossesConfigFields.getSpawnLocations());
               }
            }

            if (locations.isEmpty()) {
               lookup.loaded(customBossFilename).forEach(location -> this.addLocation(locations, location));
            }

            lookup.resolved.put(customBossFilename, locations);
            return locations;
         }
      }
   }

   private void addRegionalBossLocation(List<Location> locations, RegionalBossEntity regionalBossEntity) {
      Location location = regionalBossEntity.getLocation();
      if (location == null) {
         location = regionalBossEntity.getPersistentLocation();
      }

      this.addLocation(locations, location);
   }

   private boolean tracksSource(CustomBossEntity boss) {
      String current = boss.getCustomBossesConfigFields().getFilename();
      String identity = boss.getPhaseBossEntity() == null ? current : boss.getPhaseBossEntity().getPhase1Config().getFilename();

      for (Objective objective : this.quest.getQuestObjectives().getObjectives()) {
         if (objective != null && !objective.isObjectiveCompleted()) {
            if (objective instanceof CustomKillObjective kill && identity.equals(kill.getCustomBossFilename())) {
               return true;
            }

            if (objective instanceof CustomFetchObjective fetch) {
               Set<String> sources = CustomBossesConfig.bossesDropping(fetch.getKey());
               if (sources.contains(identity) || sources.contains(current)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private void addLocationStrings(List<Location> locations, List<String> rawLocations) {
      if (rawLocations != null) {
         rawLocations.forEach(rawLocation -> this.addLocationString(locations, rawLocation));
      }
   }

   private void addLocationString(List<Location> locations, String rawLocation) {
      if (rawLocation != null && !rawLocation.isBlank()) {
         if (PlayerData.getMatchInstance(this.player) instanceof DungeonInstance dungeon
            && dungeon.getContentPackagesConfigFields().getWorldName().equals(ConfigurationLocation.worldName(rawLocation))) {
            this.addLocation(locations, ConfigurationLocation.serializeWithInstance(dungeon.getWorld(), rawLocation));
         } else {
            this.addLocation(locations, ConfigurationLocation.serialize(rawLocation, true));
         }
      }
   }

   private void addLocation(List<Location> locations, Location location) {
      if (location != null && location.getWorld() != null) {
         if (!(PlayerData.getMatchInstance(this.player) instanceof DungeonInstance dungeon && !location.getWorld().equals(dungeon.getWorld()))) {
            for (Location existingLocation : locations) {
               if (this.isSameBlockLocation(existingLocation, location)) {
                  return;
               }
            }

            locations.add(location);
         }
      }
   }

   private boolean isSameBlockLocation(Location location1, Location location2) {
      return location1 != null && location2 != null && location1.getWorld() != null && location2.getWorld() != null
         ? location1.getWorld().equals(location2.getWorld())
            && location1.getBlockX() == location2.getBlockX()
            && location1.getBlockY() == location2.getBlockY()
            && location1.getBlockZ() == location2.getBlockZ()
         : false;
   }

   public void stop() {
      if (!this.stopped) {
         this.stopped = true;
         this.previousVerticalDestinations.clear();
         this.verticalDestinations.clear();
         playerTrackingQuests.remove(this.player.getUniqueId());
         this.resetPlayerScoreboard();
         if (this.locationRefresher != null) {
            this.locationRefresher.cancel();
         }

         if (this.compassTask != null) {
            this.compassTask.cancel();
         }

         if (this.compassBar != null) {
            BossBarOrderManager.hide(this.player, this.compassBar);
            this.compassBar.removeAll();
         }
      }
   }

   private void resetPlayerScoreboard() {
      if (this.player.isOnline()) {
         Runnable resetScoreboard = () -> {
            if (this.player.isOnline() && Bukkit.getScoreboardManager() != null) {
               if (PartySidebar.isEnabled() && PartyManager.isInParty(this.player.getUniqueId())) {
                  PartySidebar.refresh(this.player);
               } else {
                  SimpleScoreboard.clearScoreboard(this.player);
               }
            }
         };
         if (Bukkit.isPrimaryThread()) {
            resetScoreboard.run();
         } else if (MetadataHandler.PLUGIN != null && MetadataHandler.PLUGIN.isEnabled()) {
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, resetScoreboard);
         }
      }
   }

   private void startCompass() {
      this.compassBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID, new BarFlag[]{BarFlag.PLAY_BOSS_MUSIC});
      this.compassTask = (new BukkitRunnable() {
         public void run() {
            if (!QuestTracking.this.player.isOnline()) {
               QuestTracking.this.stop();
            } else {
               QuestTracking.this.updateCompassContents();
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
   }

   private void updateCompassContents() {
      Set<QuestTracking.VerticalDestination> reusable = this.previousVerticalDestinations;
      this.previousVerticalDestinations = this.verticalDestinations;
      this.verticalDestinations = reusable;
      this.verticalDestinations.clear();
      this.targetsAbove = false;
      this.targetsBelow = false;
      MatchInstance match = PlayerData.getMatchInstance(this.player);
      boolean waiting = match != null && match.isWaitingPlayer(this.player);
      if (waiting != this.wasWaiting) {
         this.wasWaiting = waiting;
         this.updateLocations(this.quest);
      }

      if (waiting) {
         this.previousVerticalDestinations.clear();
         this.compassBar.setTitle("Waiting for the dungeon to start");
         BossBarOrderManager.show(this.player, this.compassBar);
      } else {
         String compassText = "---------------------------------------------------------------";
         List<QuestTracking.LocationAndSymbol> locationAndSymbols = this.projectLocations();
         if (locationAndSymbols.isEmpty() && !this.targetsAbove && !this.targetsBelow) {
            World world = null;
            boolean locationsOutOfBounds = false;

            for (Location location : this.questIsDone
               ? new ArrayList<>(this.turnInNPCs)
               : this.objectiveDestinations.stream().flatMap(objective -> objective.getDestinations().stream()).toList()) {
               if (location != null && location.getWorld() != null) {
                  world = location.getWorld();
                  if (world.equals(this.player.getWorld())) {
                     locationsOutOfBounds = true;
                     break;
                  }
               }
            }

            if (!locationsOutOfBounds) {
               if (world != null) {
                  boolean wormholeIsViable = false;
                  Location wormholeLocation = WormholeNavigation.findDirectWormholeEntry(this.player.getWorld(), world);
                  if (wormholeLocation != null) {
                     QuestTracking.LocationAndSymbol pair = this.processLocations(wormholeLocation, null);
                     if (pair != null) {
                        compassText = compassText.substring(0, pair.getKey()) + pair.getValue() + compassText.substring(pair.getKey() + 1);
                     }

                     wormholeIsViable = true;
                  }

                  if (!wormholeIsViable) {
                     compassText = QuestsConfig.getQuestDestinationInOtherWorld().replace("$world", world.getName());
                  }
               } else {
                  compassText = QuestsConfig.getNoQuestDestinationFound();
               }
            }
         } else {
            for (QuestTracking.LocationAndSymbol pair : locationAndSymbols) {
               compassText = compassText.substring(0, pair.getKey()) + pair.getValue() + compassText.substring(pair.getKey() + 1);
            }
         }

         if (this.targetsAbove || this.targetsBelow) {
            String arrow = this.targetsAbove ? (this.targetsBelow ? "↕" : "↑") : "↓";
            compassText = compassText.substring(0, 31) + arrow + compassText.substring(32);
         }

         this.compassBar.setTitle(compassText);
         BossBarOrderManager.show(this.player, this.compassBar);
      }
   }

   private List<QuestTracking.LocationAndSymbol> projectLocations() {
      List<QuestTracking.LocationAndSymbol> parsedVectors = new ArrayList<>();
      if (this.questIsDone) {
         for (Location location : new ArrayList<>(this.turnInNPCs)) {
            QuestTracking.LocationAndSymbol locationAndSymbol = this.processLocations(location, null);
            if (locationAndSymbol != null) {
               parsedVectors.add(locationAndSymbol);
            }
         }
      } else {
         for (QuestTracking.ObjectiveDestinations iteratedObjectiveDestinations : new ArrayList<>(this.objectiveDestinations)) {
            for (Location locationx : iteratedObjectiveDestinations.getDestinations()) {
               QuestTracking.LocationAndSymbol locationAndSymbol = this.processLocations(locationx, iteratedObjectiveDestinations.getObjective());
               if (locationAndSymbol != null) {
                  parsedVectors.add(locationAndSymbol);
               }
            }
         }
      }

      return parsedVectors;
   }

   private QuestTracking.LocationAndSymbol processLocations(Location location, Objective objective) {
      if (location != null && location.getWorld() != null) {
         if (this.player.getWorld().equals(location.getWorld())) {
            Location playerLocation = this.player.getLocation();
            double deltaX = location.getX() - playerLocation.getX();
            double deltaY = location.getY() - playerLocation.getY();
            double deltaZ = location.getZ() - playerLocation.getZ();
            double horizontalDistanceSquared = deltaX * deltaX + deltaZ * deltaZ;
            QuestTracking.VerticalDestination destination = new QuestTracking.VerticalDestination(
               location.getWorld().getUID(), location.getX(), location.getY(), location.getZ(), deltaY > 0.0
            );
            boolean wasVertical = this.previousVerticalDestinations.contains(destination);
            double radius = wasVertical ? 6.0 : 4.0;
            boolean heightRequiresArrow = wasVertical ? Math.abs(deltaY) > 3.0 : Math.abs(deltaY) >= 5.0;
            if (horizontalDistanceSquared <= radius * radius && heightRequiresArrow) {
               this.verticalDestinations.add(destination);
               if (deltaY > 0.0) {
                  this.targetsAbove = true;
               } else {
                  this.targetsBelow = true;
               }

               return null;
            } else {
               double angle = this.getAngle(deltaX, deltaZ, playerLocation.getYaw());
               return Math.abs(angle) > Math.PI / 2 ? null : new QuestTracking.LocationAndSymbol((int)(angle * 57.0 / 3.0), this.getSymbol(objective));
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private double getAngle(double deltaX, double deltaZ, float yaw) {
      if (deltaX == 0.0 && deltaZ == 0.0) {
         return 0.0;
      } else {
         double facingX = -Math.sin(Math.toRadians(yaw));
         double facingZ = Math.cos(Math.toRadians(yaw));
         return Math.atan2(facingX * deltaZ - facingZ * deltaX, facingX * deltaX + facingZ * deltaZ);
      }
   }

   private String getSymbol(Objective objective) {
      if (objective == null) {
         return "⬯";
      } else if (this.questIsDone) {
         return "⦿";
      } else if (objective instanceof KillObjective) {
         return "☠";
      } else if (objective instanceof DialogObjective) {
         return "⦿";
      } else {
         return objective instanceof CustomFetchObjective ? "⚔" : "⦿";
      }
   }

   @Generated
   public static HashMap<UUID, QuestTracking> getPlayerTrackingQuests() {
      return playerTrackingQuests;
   }

   @Generated
   public Quest getQuest() {
      return this.quest;
   }

   private static final class DestinationLookup {
      private final Map<String, List<Location>> resolved = new HashMap<>();
      private Map<String, List<Location>> loaded;

      private List<Location> loaded(String filename) {
         if (this.loaded == null) {
            this.loaded = new HashMap<>();

            for (EliteEntity entity : EntityTracker.getEliteMobEntities().values()) {
               if (entity instanceof CustomBossEntity boss) {
                  String identity = boss.getPhaseBossEntity() != null
                     ? boss.getPhaseBossEntity().getPhase1Config().getFilename()
                     : boss.getCustomBossesConfigFields().getFilename();
                  this.loaded.computeIfAbsent(identity, ignored -> new ArrayList<>()).add(entity.getLocation());
               }
            }
         }

         return this.loaded.getOrDefault(filename, List.of());
      }
   }

   private class LocationAndSymbol {
      private final int key;
      private final String value;

      private LocationAndSymbol(int key, String value) {
         this.key = key + 31;
         this.value = value;
      }

      @Generated
      public int getKey() {
         return this.key;
      }

      @Generated
      public String getValue() {
         return this.value;
      }
   }

   private class ObjectiveDestinations {
      private final Objective objective;
      private final List<Location> destinations;

      private ObjectiveDestinations(Objective objective, List<Location> destinations) {
         this.objective = objective;
         this.destinations = destinations;
      }

      @Generated
      public Objective getObjective() {
         return this.objective;
      }

      @Generated
      public List<Location> getDestinations() {
         return this.destinations;
      }
   }

   public static class QuestTrackingEvents implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR,
         ignoreCancelled = true
      )
      public void onTargetSpawn(EliteMobSpawnEvent event) {
         if (event.getEliteMobEntity() instanceof CustomBossEntity boss) {
            for (QuestTracking tracking : QuestTracking.playerTrackingQuests.values()) {
               if (tracking.player.getWorld().equals(event.getEntity().getWorld()) && tracking.tracksSource(boss)) {
                  tracking.queueLocationRefresh();
               }
            }
         }
      }

      @EventHandler
      public void onWorldChanged(PlayerChangedWorldEvent event) {
         Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
            QuestTracking tracking = QuestTracking.getPlayerTrackingQuests().get(event.getPlayer().getUniqueId());
            if (tracking != null) {
               tracking.updateLocations(tracking.getQuest());
            }
         });
      }

      @EventHandler
      public void onPlayerLogout(PlayerQuitEvent event) {
         QuestTracking questTracking = QuestTracking.getPlayerTrackingQuests().get(event.getPlayer().getUniqueId());
         if (questTracking != null) {
            questTracking.stop();
         }
      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void onQuestProgressEvent(QuestProgressionEvent event) {
         if (QuestTracking.isTracking(event.getPlayer())) {
            if (QuestTracking.getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).getQuest().getQuestID().equals(event.getQuest().getQuestID())) {
               QuestTracking.getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).queueLocationRefresh();
               QuestTracking.getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).refreshScoreboard();
            }
         }
      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void onQuestCompleteEvent(QuestRewardEvent event) {
         if (QuestTracking.isTracking(event.getPlayer())) {
            if (QuestTracking.getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).getQuest().getQuestID().equals(event.getQuest().getQuestID())) {
               QuestTracking.getPlayerTrackingQuests().get(event.getPlayer().getUniqueId()).stop();
               if (QuestsConfig.isAutoTrackNextQuestOnCompletion()) {
                  Player player = event.getPlayer();
                  UUID completedQuestID = event.getQuest().getQuestID();
                  Bukkit.getScheduler()
                     .runTask(
                        MetadataHandler.PLUGIN,
                        () -> {
                           if (player.isOnline() && !QuestTracking.isTracking(player)) {
                              List<Quest> activeQuests = PlayerData.getQuests(player.getUniqueId());
                              if (activeQuests != null) {
                                 for (Quest nextQuest : new ArrayList<>(activeQuests)) {
                                    if (nextQuest != null
                                       && !nextQuest.getQuestID().equals(completedQuestID)
                                       && (!(nextQuest instanceof CustomQuest customQuest) || customQuest.getCustomQuestsConfigFields().isTrackable())) {
                                       new QuestTracking(player, nextQuest);
                                       player.sendMessage(
                                          QuestsConfig.getQuestAutoTrackNextMessage()
                                             .replace("$questName", nextQuest.getQuestName() == null ? "" : nextQuest.getQuestName())
                                       );
                                       return;
                                    }
                                 }
                              }
                           }
                        }
                     );
               }
            }
         }
      }

      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.HIGH
      )
      public void onQuestAcceptEvent(QuestAcceptEvent event) {
         if (!(event.getQuest() instanceof CustomQuest customQuest && !customQuest.getCustomQuestsConfigFields().isTrackable())) {
            if (QuestsConfig.isAutoTrackQuestsOnAccept()) {
               QuestTracking.toggleTracking(event.getPlayer(), event.getQuest());
               event.getPlayer()
                  .spigot()
                  .sendMessage(
                     SpigotMessage.commandHoverMessage(
                        QuestsConfig.getChatTrackingMessage(),
                        QuestsConfig.getChatTrackingHover(),
                        QuestsConfig.getChatTrackingCommand().replace("$questID", event.getQuest().getQuestID().toString())
                     )
                  );
            } else {
               event.getPlayer()
                  .spigot()
                  .sendMessage(
                     SpigotMessage.commandHoverMessage(
                        QuestsConfig.getChatTrackMessage(),
                        QuestsConfig.getChatTrackHover(),
                        QuestsConfig.getChatTrackCommand().replace("$questID", event.getQuest().getQuestID().toString())
                     )
                  );
            }
         }
      }
   }

   private record VerticalDestination(UUID worldId, double x, double y, double z, boolean above) {
   }
}
