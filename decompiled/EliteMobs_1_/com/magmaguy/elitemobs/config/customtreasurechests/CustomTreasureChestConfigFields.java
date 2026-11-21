package com.magmaguy.elitemobs.config.customtreasurechests;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.items.customloottable.CustomLootTable;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class CustomTreasureChestConfigFields extends CustomConfigFields {
   private Material chestMaterial = Material.CHEST;
   private BlockFace facing = BlockFace.NORTH;
   private int chestTier = 0;
   private TreasureChest.DropStyle dropStyle = TreasureChest.DropStyle.SINGLE;
   private int restockTimer = 0;
   private List<Object> lootList = null;
   private double mimicChance = 0.0;
   private List<String> mimicCustomBossesList = null;
   private List<String> restockTimers = null;
   private final Map<UUID, Long> cooldowns = new HashMap<>();
   private List<String> effects = null;
   private String worldName;
   private Location location;
   private String locationString;
   private long restockTime = 0L;
   private List<String> locationsString = new ArrayList<>();
   private CustomLootTable customLootTable = null;
   private boolean instanced = false;

   public CustomTreasureChestConfigFields(String filename, boolean isEnabled) {
      super(filename, isEnabled);
   }

   public CustomTreasureChestConfigFields(
      String fileName,
      boolean isEnabled,
      Material chestMaterial,
      BlockFace facing,
      int chestTier,
      Location location,
      TreasureChest.DropStyle dropStyle,
      int restockTimer,
      List<Object> lootList,
      double mimicChance,
      List<String> mimicCustomBossesList,
      long restockTime,
      List<String> restockTimers,
      List<String> effects
   ) {
      super(fileName, isEnabled);
      this.chestMaterial = chestMaterial;
      this.facing = facing;
      this.chestTier = chestTier;
      this.location = location;
      this.dropStyle = dropStyle;
      this.restockTimer = restockTimer;
      this.lootList = lootList;
      this.mimicChance = mimicChance;
      this.mimicCustomBossesList = mimicCustomBossesList;
      this.restockTime = restockTime;
      this.restockTimers = restockTimers;
      this.effects = effects;
   }

   @Override
   public void processConfigFields() {
      this.isEnabled = this.processBoolean("isEnabled", this.isEnabled, false, false);
      this.chestMaterial = this.processEnum("chestType", this.chestMaterial, Material.CHEST, Material.class, true);
      this.facing = this.processEnum("facing", this.facing, BlockFace.NORTH, BlockFace.class, true);
      this.chestTier = this.processInt("chestTier", this.chestTier, 0, true);
      this.worldName = this.processString("location", this.worldName, null, false);
      if (this.worldName != null) {
         this.worldName = this.worldName.split(",")[0];
      }

      this.dropStyle = this.processEnum("dropStyle", this.dropStyle, TreasureChest.DropStyle.SINGLE, TreasureChest.DropStyle.class, true);
      this.restockTimer = this.processInt("restockTimer", this.restockTimer, 0, true);
      this.lootList = this.processList("lootList", this.lootList, new ArrayList<>(), false);
      this.customLootTable = new CustomLootTable(this);
      this.mimicChance = this.processDouble("mimicChance", this.mimicChance, 0.0, true);
      this.mimicCustomBossesList = this.processStringList("mimicCustomBossesList", this.mimicCustomBossesList, new ArrayList<>(), true);
      this.restockTime = this.processLong("restockTime", this.restockTime, 0L, false);
      this.restockTimers = this.processStringList("restockTimers", this.restockTimers, new ArrayList<>(), false);
      if (this.restockTimers == null) {
         this.restockTimers = new ArrayList<>();
      }

      this.cooldowns.clear();

      for (String entry : this.restockTimers) {
         try {
            String[] parts = entry.split(":", -1);
            if (parts.length != 2) {
               throw new IllegalArgumentException("Expected UUID:expiry");
            }

            UUID player = UUID.fromString(parts[0]);
            long expiry = Long.parseLong(parts[1]);
            this.cooldowns.merge(player, expiry, Math::max);
         } catch (IllegalArgumentException var8) {
            Logger.warn("Invalid restock timer in " + this.filename + ": " + entry);
         }
      }

      this.effects = this.processStringList("effects", this.effects, new ArrayList<>(), false);
      this.locationsString = this.processStringList("locations", this.locationsString, new ArrayList<>(), false);
      this.locationString = this.processString("location", this.locationString, null, false);
      this.instanced = this.processBoolean("instanced", this.instanced, false, false);
      if (this.locationString != null) {
         new TreasureChest(this, this.locationString, this.restockTime);
      } else if (this.locationsString != null) {
         for (String string : this.locationsString) {
            String[] strings = string.split(":");
            long timestamp = 0L;
            if (strings.length > 1) {
               try {
                  timestamp = Long.parseLong(strings[1]);
               } catch (Exception var7) {
                  Logger.warn("Bad unix timestamp in locations for " + this.filename + " . Entry: " + strings[0]);
               }
            }

            new TreasureChest(this, strings[0], timestamp);
         }
      } else {
         Logger.warn("No locations found for chest " + this.filename);
      }
   }

   public synchronized TreasureChest addTreasureChest(Location chestInstanceLocation, long unixTimeStamp) {
      Location block = chestInstanceLocation.getBlock().getLocation();
      String coordinates = ConfigurationLocation.deserialize(block);
      List<String> updated = new ArrayList<>(this.locationsString == null ? List.of() : this.locationsString);
      boolean existing = false;

      for (int i = 0; i < updated.size(); i++) {
         if (sameBlock(updated.get(i), block)) {
            updated.set(i, coordinates + ":" + unixTimeStamp);
            existing = true;
            break;
         }
      }

      if (!existing) {
         updated.add(coordinates + ":" + unixTimeStamp);
      }

      YamlConfiguration snapshot = this.configurationSnapshot();
      snapshot.set("locations", updated);
      ConfigurationEngine.fileSaverSerialized(snapshot.saveToString(), this.file);
      this.locationsString = updated;
      this.setFileConfiguration(snapshot);
      return existing ? null : new TreasureChest(this, coordinates, unixTimeStamp);
   }

   public void setRestockTime(Location location, long newRestockTime) {
      if (!this.isInstanced()) {
         if (!this.locationsString.isEmpty()) {
            this.addTreasureChest(location, newRestockTime);
         } else {
            this.restockTime = newRestockTime;
            this.fileConfiguration.set("restockTime", newRestockTime);

            try {
               this.fileConfiguration.save(this.file);
            } catch (Exception var5) {
               Logger.warn("Attempted to update restock time for a custom treasure chest and failed, did you delete it during runtime?");
            }
         }
      }
   }

   public synchronized long cooldownExpiry(UUID playerId) {
      return this.cooldowns.getOrDefault(playerId, 0L);
   }

   public synchronized void reserveCooldown(UUID playerId, long expiry, long now) {
      Map<UUID, Long> updated = new HashMap<>(this.cooldowns);
      updated.values().removeIf(value -> value <= now);
      updated.put(playerId, expiry);
      List<String> serialized = updated.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).toList();
      YamlConfiguration snapshot = this.configurationSnapshot();
      snapshot.set("restockTimers", serialized);
      ConfigurationEngine.fileSaverSerialized(snapshot.saveToString(), this.file);
      this.cooldowns.clear();
      this.cooldowns.putAll(updated);
      this.restockTimers = serialized;
      this.setFileConfiguration(snapshot);
   }

   public synchronized boolean removeLocation(Location selected) {
      List<String> retained = new ArrayList<>(this.locationsString == null ? List.of() : this.locationsString);
      boolean changed = retained.removeIf(entry -> sameBlock(entry, selected));
      boolean clearLegacy = sameBlock(this.locationString, selected);
      if (!changed && !clearLegacy) {
         return false;
      } else {
         YamlConfiguration snapshot = this.configurationSnapshot();
         snapshot.set("locations", retained);
         if (clearLegacy) {
            snapshot.set("location", null);
         }

         ConfigurationEngine.fileSaverSerialized(snapshot.saveToString(), this.file);
         this.locationsString = retained;
         if (clearLegacy) {
            this.locationString = null;
         }

         this.setFileConfiguration(snapshot);
         return true;
      }
   }

   private YamlConfiguration configurationSnapshot() {
      YamlConfiguration snapshot = new YamlConfiguration();

      try {
         snapshot.loadFromString(this.fileConfiguration.saveToString());
         return snapshot;
      } catch (InvalidConfigurationException var3) {
         throw new IllegalStateException("Invalid chest configuration snapshot", var3);
      }
   }

   private static boolean sameBlock(String entry, Location selected) {
      if (entry != null && selected != null && selected.getWorld() != null) {
         Location parsed = ConfigurationLocation.serialize(entry, true);
         return parsed != null
            && selected.getWorld().equals(parsed.getWorld())
            && parsed.getBlockX() == selected.getBlockX()
            && parsed.getBlockY() == selected.getBlockY()
            && parsed.getBlockZ() == selected.getBlockZ();
      } else {
         return false;
      }
   }

   @Generated
   public Material getChestMaterial() {
      return this.chestMaterial;
   }

   @Generated
   public BlockFace getFacing() {
      return this.facing;
   }

   @Generated
   public int getChestTier() {
      return this.chestTier;
   }

   @Generated
   public TreasureChest.DropStyle getDropStyle() {
      return this.dropStyle;
   }

   @Generated
   public int getRestockTimer() {
      return this.restockTimer;
   }

   @Generated
   public List<Object> getLootList() {
      return this.lootList;
   }

   @Generated
   public double getMimicChance() {
      return this.mimicChance;
   }

   @Generated
   public List<String> getMimicCustomBossesList() {
      return this.mimicCustomBossesList;
   }

   @Generated
   public List<String> getEffects() {
      return this.effects;
   }

   @Generated
   public String getWorldName() {
      return this.worldName;
   }

   @Generated
   public Location getLocation() {
      return this.location;
   }

   @Generated
   public String getLocationString() {
      return this.locationString;
   }

   @Generated
   public long getRestockTime() {
      return this.restockTime;
   }

   @Generated
   public List<String> getLocationsString() {
      return this.locationsString;
   }

   @Generated
   public CustomLootTable getCustomLootTable() {
      return this.customLootTable;
   }

   @Generated
   public boolean isInstanced() {
      return this.instanced;
   }

   @Generated
   public void setInstanced(boolean instanced) {
      this.instanced = instanced;
   }
}
