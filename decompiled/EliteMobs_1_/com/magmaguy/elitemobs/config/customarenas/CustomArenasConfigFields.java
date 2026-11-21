package com.magmaguy.elitemobs.config.customarenas;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.items.customloottable.CustomLootTable;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Location;

public class CustomArenasConfigFields extends CustomConfigFields {
   private String arenaContainer;
   private String arenaName;
   private String corner1;
   private String corner2;
   private String startLocation;
   private String exitLocation;
   private int waveCount;
   private int delayBetweenWaves;
   private List<String> spawnPoints = new ArrayList<>();
   private List<String> bossList = new ArrayList<>();
   private List<Object> rawArenaRewards = new ArrayList<>();
   private CustomLootTable arenaRewards;
   private int minimumPlayerCount = 1;
   private int maximumPlayerCount;
   private List<String> arenaMessages = new ArrayList<>();
   private boolean cylindricalArena = false;
   private String permission = null;
   private Location teleportLocation = null;
   private final List<Integer> intermissionWaves = new ArrayList<>();
   protected List<String> rawIntermissionWaves = new ArrayList<>();

   public CustomArenasConfigFields(String filename, boolean isEnabled) {
      super(filename, isEnabled);
   }

   @Override
   public void processConfigFields() {
      this.arenaContainer = this.processString("arenaContainer", this.arenaContainer, null, false);
      this.isEnabled = this.processBoolean("isEnabled", this.isEnabled, true, true);
      this.arenaName = this.translatable(this.filename, "arenaName", this.processString("arenaName", this.arenaName, "Default name", false));
      this.corner1 = this.processString("corner1", this.corner1, null, false);
      this.corner2 = this.processString("corner2", this.corner2, null, false);
      this.startLocation = this.processString("startLocation", this.startLocation, null, false);
      this.exitLocation = this.processString("exitLocation", this.exitLocation, null, false);
      this.waveCount = this.processInt("waveCount", this.waveCount, 0, false);
      this.delayBetweenWaves = this.processInt("delayBetweenWaves", this.delayBetweenWaves, 0, false);
      this.spawnPoints = this.processStringList("spawnPoints", this.spawnPoints, new ArrayList<>(), false);
      this.bossList = this.processStringList("bossList", this.bossList, new ArrayList<>(), false);
      this.rawArenaRewards = this.processList("rawArenaReward", this.rawArenaRewards, new ArrayList<>(), false);
      this.arenaRewards = new CustomLootTable(this);
      this.minimumPlayerCount = this.processInt("minimumPlayerCount", this.minimumPlayerCount, 1, false);
      this.maximumPlayerCount = this.processInt("maximumPlayerCount", this.maximumPlayerCount, 100, false);
      this.arenaMessages = this.translatable(
         this.filename, "arenaMessages", this.processStringList("arenaMessages", this.arenaMessages, new ArrayList<>(), false)
      );
      this.cylindricalArena = this.processBoolean("cylindricalArena", this.cylindricalArena, false, false);
      this.permission = this.processString("permission", this.permission, null, false);
      this.teleportLocation = this.processLocation("teleportLocation", this.teleportLocation, null, false);
      this.rawIntermissionWaves = this.processStringList("rawIntermissionWaves", this.rawIntermissionWaves, new ArrayList<>(), false);
      this.rawIntermissionWaves.forEach(wave -> {
         try {
            this.intermissionWaves.add(Integer.parseInt(wave));
         } catch (Exception var3) {
            Logger.warn("Failed to parse intermission wave " + wave + " for arena " + this.filename);
         }
      });
   }

   @Generated
   public String getArenaContainer() {
      return this.arenaContainer;
   }

   @Generated
   public void setArenaContainer(String arenaContainer) {
      this.arenaContainer = arenaContainer;
   }

   @Generated
   public String getArenaName() {
      return this.arenaName;
   }

   @Generated
   public void setArenaName(String arenaName) {
      this.arenaName = arenaName;
   }

   @Generated
   public String getCorner1() {
      return this.corner1;
   }

   @Generated
   public void setCorner1(String corner1) {
      this.corner1 = corner1;
   }

   @Generated
   public String getCorner2() {
      return this.corner2;
   }

   @Generated
   public void setCorner2(String corner2) {
      this.corner2 = corner2;
   }

   @Generated
   public String getStartLocation() {
      return this.startLocation;
   }

   @Generated
   public void setStartLocation(String startLocation) {
      this.startLocation = startLocation;
   }

   @Generated
   public String getExitLocation() {
      return this.exitLocation;
   }

   @Generated
   public void setExitLocation(String exitLocation) {
      this.exitLocation = exitLocation;
   }

   @Generated
   public int getWaveCount() {
      return this.waveCount;
   }

   @Generated
   public void setWaveCount(int waveCount) {
      this.waveCount = waveCount;
   }

   @Generated
   public int getDelayBetweenWaves() {
      return this.delayBetweenWaves;
   }

   @Generated
   public void setDelayBetweenWaves(int delayBetweenWaves) {
      this.delayBetweenWaves = delayBetweenWaves;
   }

   @Generated
   public List<String> getSpawnPoints() {
      return this.spawnPoints;
   }

   @Generated
   public void setSpawnPoints(List<String> spawnPoints) {
      this.spawnPoints = spawnPoints;
   }

   @Generated
   public List<String> getBossList() {
      return this.bossList;
   }

   @Generated
   public void setBossList(List<String> bossList) {
      this.bossList = bossList;
   }

   @Generated
   public List<Object> getRawArenaRewards() {
      return this.rawArenaRewards;
   }

   @Generated
   public void setRawArenaRewards(List<Object> rawArenaRewards) {
      this.rawArenaRewards = rawArenaRewards;
   }

   @Generated
   public CustomLootTable getArenaRewards() {
      return this.arenaRewards;
   }

   @Generated
   public void setArenaRewards(CustomLootTable arenaRewards) {
      this.arenaRewards = arenaRewards;
   }

   @Generated
   public int getMinimumPlayerCount() {
      return this.minimumPlayerCount;
   }

   @Generated
   public void setMinimumPlayerCount(int minimumPlayerCount) {
      this.minimumPlayerCount = minimumPlayerCount;
   }

   @Generated
   public int getMaximumPlayerCount() {
      return this.maximumPlayerCount;
   }

   @Generated
   public void setMaximumPlayerCount(int maximumPlayerCount) {
      this.maximumPlayerCount = maximumPlayerCount;
   }

   @Generated
   public List<String> getArenaMessages() {
      return this.arenaMessages;
   }

   @Generated
   public void setArenaMessages(List<String> arenaMessages) {
      this.arenaMessages = arenaMessages;
   }

   @Generated
   public boolean isCylindricalArena() {
      return this.cylindricalArena;
   }

   @Generated
   public void setCylindricalArena(boolean cylindricalArena) {
      this.cylindricalArena = cylindricalArena;
   }

   @Generated
   public String getPermission() {
      return this.permission;
   }

   @Generated
   public void setPermission(String permission) {
      this.permission = permission;
   }

   @Generated
   public Location getTeleportLocation() {
      return this.teleportLocation;
   }

   @Generated
   public List<Integer> getIntermissionWaves() {
      return this.intermissionWaves;
   }
}
