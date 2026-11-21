package com.magmaguy.elitemobs.treasurechest;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.SoundsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestConfigFields;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestsConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DynamicDungeonInstance;
import com.magmaguy.elitemobs.items.customloottable.CustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.CustomLootTable;
import com.magmaguy.elitemobs.items.customloottable.EliteCustomLootEntry;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.Round;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WeightedProbability;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

public class TreasureChest implements PersistentObject {
   private static final HashMap<Location, TreasureChest> treasureChestHashMap = new HashMap<>();
   private static final Map<String, Set<TreasureChest>> itemSources = new HashMap<>();
   private final Set<String> indexedItems = new HashSet<>();
   private static final ArrayListMultimap<String, TreasureChest> instancedTreasureChests = ArrayListMultimap.create();
   private final CustomTreasureChestConfigFields customTreasureChestConfigFields;
   private final String locationString;
   private final String worldName;
   private final HashSet<UUID> blacklistedPlayersInstance = new HashSet<>();
   private Location location;
   private long restockTime;
   private BukkitTask restockTask;
   private PersistentObjectHandler persistentObjectHandler;
   private EMPackage emPackage = null;

   public static Collection<TreasureChest> chestsDropping(String itemFilename) {
      return List.copyOf(itemSources.getOrDefault(itemFilename, Set.of()));
   }

   private void removeItemSources() {
      for (String item : this.indexedItems) {
         Set<TreasureChest> sources = itemSources.get(item);
         if (sources != null) {
            sources.remove(this);
            if (sources.isEmpty()) {
               itemSources.remove(item);
            }
         }
      }

      this.indexedItems.clear();
   }

   public TreasureChest(CustomTreasureChestConfigFields customTreasureChestConfigFields, String locationString, long restockTime) {
      this.customTreasureChestConfigFields = customTreasureChestConfigFields;
      this.locationString = locationString;
      this.worldName = ConfigurationLocation.worldName(locationString);
      this.location = blockLocation(ConfigurationLocation.serialize(locationString));
      this.restockTime = restockTime;
      this.emPackage = EMPackage.getContent(customTreasureChestConfigFields.getFilename());
      if (customTreasureChestConfigFields.isEnabled()) {
         if (customTreasureChestConfigFields.getChestMaterial() != null) {
            if (!customTreasureChestConfigFields.isInstanced()) {
               this.registerPersistentHandler();
               this.registerChest();
               this.scheduleRestock();
            } else {
               instancedTreasureChests.put(this.worldName, this);
            }
         }
      }
   }

   private TreasureChest(TreasureChest blueprint, World instancedWorld) {
      this.customTreasureChestConfigFields = blueprint.customTreasureChestConfigFields;
      this.locationString = blueprint.locationString;
      this.worldName = blueprint.worldName;
      this.location = blockLocation(ConfigurationLocation.serializeWithInstance(instancedWorld, this.locationString));
      this.restockTime = 0L;
      this.emPackage = blueprint.emPackage;
      if (this.hasValidConfiguration() && this.location != null) {
         this.registerPersistentHandler();
         this.registerChest();
         this.scheduleRestock();
      }
   }

   public static void initializeInstancedTreasureChests(String instanceWorldName, World instancedWorld) {
      List<TreasureChest> chests = instancedTreasureChests.get(instanceWorldName);
      chests.forEach(blueprint -> new TreasureChest(blueprint, instancedWorld));
   }

   public static void clearTreasureChests() {
      Set<TreasureChest> activeChests = Collections.newSetFromMap(new IdentityHashMap<>());
      activeChests.addAll(treasureChestHashMap.values());
      activeChests.forEach(treasureChest -> {
         treasureChest.cancelRestock();
         treasureChest.unregisterPersistentHandler();
      });
      treasureChestHashMap.clear();
      itemSources.clear();
   }

   public static void removeInstancedTreasureChests(World world) {
      if (world != null) {
         UUID worldUUID = world.getUID();
         Set<TreasureChest> removedChests = Collections.newSetFromMap(new IdentityHashMap<>());
         treasureChestHashMap.entrySet().removeIf(entry -> {
            Location keyLocation = entry.getKey();
            Location chestLocation = entry.getValue().location;
            boolean remove = isLocationInWorld(keyLocation, worldUUID) || isLocationInWorld(chestLocation, worldUUID);
            if (remove) {
               entry.getValue().removeItemSources();
               removedChests.add(entry.getValue());
            }

            return remove;
         });
         instancedTreasureChests.values().forEach(treasureChest -> {
            if (isLocationInWorld(treasureChest.location, worldUUID)) {
               removedChests.add(treasureChest);
            }
         });
         removedChests.forEach(treasureChest -> treasureChest.deactivateInstance(worldUUID));
      }
   }

   public static void shutdown() {
      Set<TreasureChest> knownChests = Collections.newSetFromMap(new IdentityHashMap<>());
      knownChests.addAll(treasureChestHashMap.values());
      knownChests.addAll(instancedTreasureChests.values());
      knownChests.forEach(treasureChest -> {
         treasureChest.cancelRestock();
         treasureChest.unregisterPersistentHandler();
      });
      treasureChestHashMap.clear();
      itemSources.clear();
      instancedTreasureChests.clear();
   }

   public static TreasureChest getTreasureChest(Location location) {
      return getTreasureChestHashMap().get(blockLocation(location));
   }

   private static Location blockLocation(Location location) {
      return location == null ? null : new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
   }

   private static boolean isLocationInWorld(Location location, UUID worldUUID) {
      return location != null && location.getWorld() != null && location.getWorld().getUID().equals(worldUUID);
   }

   private void registerPersistentHandler() {
      this.unregisterPersistentHandler();
      this.persistentObjectHandler = new PersistentObjectHandler(this);
   }

   private void unregisterPersistentHandler() {
      if (this.persistentObjectHandler != null) {
         this.persistentObjectHandler.remove();
         this.persistentObjectHandler = null;
      }
   }

   private boolean registerChest() {
      if (this.hasValidConfiguration() && this.hasLoadedWorld()) {
         TreasureChest previous = treasureChestHashMap.put(this.location, this);
         if (previous != null) {
            previous.removeItemSources();
         }

         CustomLootTable table = this.customTreasureChestConfigFields.getCustomLootTable();
         if (table != null) {
            for (CustomLootEntry entry : table.getEntries()) {
               if (entry instanceof EliteCustomLootEntry item) {
                  this.indexedItems.add(item.getFilename());
                  itemSources.computeIfAbsent(item.getFilename(), ignored -> new HashSet<>()).add(this);
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isRegistered() {
      return this.location != null && treasureChestHashMap.get(this.location) == this;
   }

   private boolean hasValidConfiguration() {
      return this.customTreasureChestConfigFields.isEnabled() && this.customTreasureChestConfigFields.getChestMaterial() != null;
   }

   private boolean hasLoadedWorld() {
      return this.location != null && this.location.getWorld() != null ? Bukkit.getWorld(this.location.getWorld().getUID()) != null : false;
   }

   private boolean canGenerateChest() {
      return this.isRegistered() && this.hasValidConfiguration() && this.hasLoadedWorld()
         ? this.location.getWorld().isChunkLoaded(this.location.getBlockX() >> 4, this.location.getBlockZ() >> 4)
         : false;
   }

   private void cancelRestock() {
      if (this.restockTask != null && !this.restockTask.isCancelled()) {
         this.restockTask.cancel();
      }

      this.restockTask = null;
   }

   private void deactivateInstance(UUID worldUUID) {
      this.cancelRestock();
      this.unregisterPersistentHandler();
      if (isLocationInWorld(this.location, worldUUID)) {
         treasureChestHashMap.remove(this.location, this);
         this.removeItemSources();
         this.location = null;
      }
   }

   private void scheduleRestock() {
      this.cancelRestock();
      if (this.isRegistered() && this.hasValidConfiguration() && this.hasLoadedWorld()) {
         long secondsUntilRestock = Math.max(0L, this.restockTime - Instant.now().getEpochSecond());
         long delayTicks = secondsUntilRestock > 461168601842738790L ? Long.MAX_VALUE : secondsUntilRestock * 20L;
         this.restockTask = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
            this.restockTask = null;
            if (this.canGenerateChest()) {
               this.generateChest();
            }
         }, delayTicks);
      }
   }

   private void generateChest() {
      if (this.canGenerateChest()) {
         try {
            if (!this.location.getBlock().getType().equals(this.customTreasureChestConfigFields.getChestMaterial())) {
               this.location.getBlock().setType(this.customTreasureChestConfigFields.getChestMaterial());
            }
         } catch (Exception var3) {
            Logger.warn("Custom Treasure Chest " + this.customTreasureChestConfigFields.getFilename() + " has an invalid location and can not be placed.");
            return;
         }

         if (this.location.getBlock().getBlockData() instanceof Directional chest) {
            chest.setFacing(this.customTreasureChestConfigFields.getFacing());
            this.location.getBlock().setBlockData(chest);
         } else {
            Logger.warn(
               "Treasure chest "
                  + this.customTreasureChestConfigFields.getFilename()
                  + " does not have a directional block for the Treasure Chest material "
                  + this.customTreasureChestConfigFields.getChestMaterial()
                  + " ! Chest materials are directional, is your chest a chest?"
            );
         }

         this.location.getBlock().getState().update();
      }
   }

   public void doInteraction(Player player) {
      long now = Instant.now().getEpochSecond();
      if (this.customTreasureChestConfigFields.getDropStyle().equals(TreasureChest.DropStyle.GROUP)) {
         if (this.customTreasureChestConfigFields.isInstanced()) {
            if (!this.blacklistedPlayersInstance.add(player.getUniqueId())) {
               return;
            }
         } else {
            long expiry = this.customTreasureChestConfigFields.cooldownExpiry(player.getUniqueId());
            if (expiry > now) {
               this.groupTimerCooldownMessage(player, expiry);
               return;
            }

            if (this.restockTime > now) {
               return;
            }

            try {
               this.customTreasureChestConfigFields.reserveCooldown(player.getUniqueId(), this.cooldownTime(), now);
            } catch (RuntimeException var7) {
               Logger.warn("Could not reserve treasure chest " + this.customTreasureChestConfigFields.getFilename() + ": " + var7.getMessage());
               player.sendMessage("This chest could not save your claim. Please try again later.");
               return;
            }
         }
      }

      if (ThreadLocalRandom.current().nextDouble() < this.customTreasureChestConfigFields.getMimicChance()) {
         this.doMimic(player);
      } else {
         this.doTreasure(player);
      }

      player.playSound(player.getLocation(), SoundsConfig.treasureChestOpenSound, 1.0F, 1.0F);
      if (!this.customTreasureChestConfigFields.getDropStyle().equals(TreasureChest.DropStyle.GROUP)) {
         this.location.getBlock().setType(Material.AIR);
         this.restockTime = this.cooldownTime();
         this.customTreasureChestConfigFields.setRestockTime(this.location, this.restockTime);
         if (!this.customTreasureChestConfigFields.isInstanced()) {
            this.scheduleRestock();
         }
      }
   }

   private void doMimic(Player player) {
      HashMap<String, Double> weighedValues = new HashMap<>();

      for (String string : this.customTreasureChestConfigFields.getMimicCustomBossesList()) {
         String filename = string.split(":")[0];
         double weight = 1.0;

         try {
            weight = Double.parseDouble(string.split(":")[1]);
         } catch (Exception var9) {
            weight = 1.0;
         }

         weighedValues.put(filename, weight);
      }

      String filename = WeightedProbability.pickWeighedProbability(weighedValues);
      CustomBossesConfigFields fields = CustomBossesConfig.getCustomBoss(filename);
      if (fields == null) {
         Logger.warn("Failed to spawn mimic for treasure chest " + this.customTreasureChestConfigFields.getFilename() + ": custom boss config was not found.");
      } else {
         DungeonInstance dungeonInstance = this.getDungeonInstance(player);
         CustomBossEntity customBossEntity = (CustomBossEntity)(dungeonInstance == null
            ? CustomBossEntity.createCustomBossEntity(filename)
            : dungeonInstance.createEncounterBoss(fields, this.location));
         if (customBossEntity != null) {
            Integer dynamicDungeonLevel = this.getDynamicDungeonLevel(player);
            if (customBossEntity.getCustomBossesConfigFields().getLevel() == -1) {
               if (dynamicDungeonLevel != null) {
                  customBossEntity.spawn(this.location, this.randomizeLevel(dynamicDungeonLevel), false);
               } else {
                  ElitePlayerInventory elitePlayerInventory = ElitePlayerInventory.getPlayer(player);
                  if (elitePlayerInventory != null) {
                     customBossEntity.spawn(this.location, this.randomizeLevel(elitePlayerInventory.getNaturalMobSpawnLevel(false)), false);
                  } else {
                     customBossEntity.spawn(this.location, false);
                  }
               }
            } else {
               customBossEntity.spawn(this.location, this.randomizeTier(), false);
            }
         }
      }
   }

   private void doTreasure(Player player) {
      Integer dynamicDungeonLevel = this.getDynamicDungeonLevel(player);
      if (dynamicDungeonLevel != null) {
         this.customTreasureChestConfigFields.getCustomLootTable().treasureChestDropAtLevel(player, dynamicDungeonLevel, this.location);
      } else {
         if (PlayerData.getMatchInstance(player) == null) {
            ElitePlayerInventory elitePlayerInventory = ElitePlayerInventory.getPlayer(player);
            if (elitePlayerInventory != null) {
               int playerLevel = elitePlayerInventory.getNaturalMobSpawnLevel(false);
               this.customTreasureChestConfigFields
                  .getCustomLootTable()
                  .treasureChestDropScalableToPlayerLevel(player, this.customTreasureChestConfigFields.getChestTier(), playerLevel, this.location);
               return;
            }
         }

         this.customTreasureChestConfigFields
            .getCustomLootTable()
            .treasureChestDrop(player, this.customTreasureChestConfigFields.getChestTier(), this.location);
      }
   }

   private int randomizeTier() {
      return this.customTreasureChestConfigFields.getChestTier() * 10 + ThreadLocalRandom.current().nextInt(11);
   }

   private int randomizeLevel(int baseLevel) {
      return Math.max(1, baseLevel + ThreadLocalRandom.current().nextInt(-1, 2));
   }

   private Integer getDynamicDungeonLevel(Player player) {
      return this.getDungeonInstance(player) instanceof DynamicDungeonInstance dynamic ? dynamic.getSelectedLevel() : null;
   }

   private DungeonInstance getDungeonInstance(Player player) {
      if (this.location != null && this.location.getWorld() != null) {
         if (player != null
            && PlayerData.getMatchInstance(player) instanceof DungeonInstance dungeonInstance
            && this.location.getWorld().equals(dungeonInstance.getWorld())) {
            return dungeonInstance;
         } else {
            for (DungeonInstance dungeonInstance : DungeonInstance.getDungeonInstances()) {
               if (this.location.getWorld().equals(dungeonInstance.getWorld())) {
                  return dungeonInstance;
               }
            }

            return null;
         }
      } else {
         return null;
      }
   }

   private void groupTimerCooldownMessage(Player player, long targetTime) {
      player.sendMessage(DefaultConfig.getChestCooldownMessage().replace("$time", this.timeConverter(targetTime - Instant.now().getEpochSecond())));
   }

   private long cooldownTime() {
      return Instant.now().getEpochSecond() + 60L * this.customTreasureChestConfigFields.getRestockTimer();
   }

   private String timeConverter(long seconds) {
      if (seconds < 0L) {
         seconds = 0L;
      }

      if (seconds < 120L) {
         return seconds + " seconds";
      } else if (seconds < 7200L) {
         return Round.twoDecimalPlaces(seconds / 60.0) + "minutes";
      } else {
         return seconds < 172800L ? Round.twoDecimalPlaces(seconds / 60.0 / 60.0) + "hours" : Round.twoDecimalPlaces(seconds / 60.0 / 60.0 / 24.0) + "days";
      }
   }

   public boolean removeTreasureChest() {
      try {
         if (!CustomTreasureChestsConfig.removeTreasureChestEntry(this.location, this.customTreasureChestConfigFields.getFilename())) {
            return false;
         }
      } catch (RuntimeException var2) {
         Logger.warn("Could not save removal of chest " + this.customTreasureChestConfigFields.getFilename() + ": " + var2.getMessage());
         return false;
      }

      this.cancelRestock();
      this.unregisterPersistentHandler();
      if (this.location != null && this.location.getWorld() != null) {
         this.location.getBlock().setBlockData(Material.AIR.createBlockData());
      }

      treasureChestHashMap.remove(this.location, this);
      this.removeItemSources();
      return true;
   }

   @Override
   public void chunkLoad() {
      if (this.registerChest()) {
         this.scheduleRestock();
      }
   }

   @Override
   public void chunkUnload() {
      this.cancelRestock();
   }

   @Override
   public void worldLoad(World world) {
      this.location = blockLocation(ConfigurationLocation.serializeWithInstance(world, this.locationString));
      if (this.registerChest()) {
         this.scheduleRestock();
      }
   }

   @Override
   public void worldUnload() {
      this.cancelRestock();
      treasureChestHashMap.remove(this.location, this);
      this.removeItemSources();
   }

   @Override
   public Location getPersistentLocation() {
      return this.getLocation();
   }

   @Override
   public String getWorldName() {
      return this.worldName;
   }

   @Generated
   public static HashMap<Location, TreasureChest> getTreasureChestHashMap() {
      return treasureChestHashMap;
   }

   @Generated
   public CustomTreasureChestConfigFields getCustomTreasureChestConfigFields() {
      return this.customTreasureChestConfigFields;
   }

   @Generated
   public Location getLocation() {
      return this.location;
   }

   @Generated
   public EMPackage getEmPackage() {
      return this.emPackage;
   }

   @Generated
   public void setEmPackage(EMPackage emPackage) {
      this.emPackage = emPackage;
   }

   public static enum DropStyle {
      SINGLE,
      GROUP;
   }

   public static class TreasureChestEvents implements Listener {
      @EventHandler(
         priority = EventPriority.HIGHEST,
         ignoreCancelled = true
      )
      public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getClickedBlock() != null) {
            TreasureChest treasureChest = TreasureChest.getTreasureChest(event.getClickedBlock().getLocation());
            if (treasureChest != null) {
               event.setCancelled(true);
               treasureChest.doInteraction(event.getPlayer());
            }
         }
      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void onBreak(BlockBreakEvent event) {
         if (TreasureChest.getTreasureChest(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
         }
      }
   }
}
