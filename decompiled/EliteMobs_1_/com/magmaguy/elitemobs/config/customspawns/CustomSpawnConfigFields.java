package com.magmaguy.elitemobs.config.customspawns;

import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.events.MoonPhaseDetector;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.Generated;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;

public class CustomSpawnConfigFields extends CustomConfigFields {
   int lowestYLevel = 0;
   int highestYLevel = 320;
   List<World> validWorlds = new ArrayList<>();
   List<Environment> validWorldEnvironments = new ArrayList<>();
   List<String> validBiomesStrings = new ArrayList<>();
   List<Biome> validBiomes = new ArrayList<>();
   private long earliestTime = 0L;
   private long latestTime = 24000L;
   private MoonPhaseDetector.MoonPhase moonPhase = null;
   private boolean bypassWorldGuard = false;
   private boolean canSpawnInLight = false;
   private boolean isSurfaceSpawn = false;
   private boolean isUndergroundSpawn = false;

   public CustomSpawnConfigFields(String filename, boolean isEnabled) {
      super(filename, isEnabled);
   }

   @Override
   public void processConfigFields() {
      this.isEnabled = this.processBoolean("isEnabled", this.isEnabled, true, true);
      this.lowestYLevel = this.processInt("lowestYLevel", this.lowestYLevel, 0, false);
      this.highestYLevel = this.processInt("highestYLevel", this.highestYLevel, 320, false);
      this.validWorlds = this.processWorldList("validWorlds", this.validWorlds, new ArrayList<>(), false);
      this.validWorldEnvironments = this.processEnumList("validWorldEnvironments", this.validWorldEnvironments, new ArrayList(), Environment.class, false);
      this.validBiomesStrings = this.processStringList("validBiomesV2", this.validBiomesStrings, new ArrayList<>(), false);
      List<Biome> resolvedBiomes = new ArrayList<>();

      for (String configuredBiome : this.validBiomesStrings) {
         NamespacedKey key = NamespacedKey.fromString(configuredBiome.toLowerCase(Locale.ROOT));
         Biome biome = key == null ? null : (Biome)Registry.BIOME.get(key);
         if (biome == null) {
            Logger.warn("Invalid biome '" + configuredBiome + "' in custom spawn " + this.filename + "; spawn definition disabled.");
            this.isEnabled = false;
         } else if (!resolvedBiomes.contains(biome)) {
            resolvedBiomes.add(biome);
         }
      }

      this.validBiomes = List.copyOf(resolvedBiomes);
      this.earliestTime = this.processLong("earliestTime", this.earliestTime, 0L, false);
      this.latestTime = this.processLong("latestTime", this.latestTime, 24000L, false);
      this.moonPhase = this.processEnum("moonPhase", this.moonPhase, null, MoonPhaseDetector.MoonPhase.class, false);
      this.bypassWorldGuard = this.processBoolean("bypassWorldGuard", this.bypassWorldGuard, false, false);
      this.canSpawnInLight = this.processBoolean("canSpawnInLight", this.canSpawnInLight, false, false);
      this.isSurfaceSpawn = this.processBoolean("isSurfaceSpawn", this.isSurfaceSpawn, false, false);
      this.isUndergroundSpawn = this.processBoolean("isUndergroundSpawn", this.isUndergroundSpawn, false, false);
   }

   @Generated
   public int getLowestYLevel() {
      return this.lowestYLevel;
   }

   @Generated
   public void setLowestYLevel(int lowestYLevel) {
      this.lowestYLevel = lowestYLevel;
   }

   @Generated
   public int getHighestYLevel() {
      return this.highestYLevel;
   }

   @Generated
   public void setHighestYLevel(int highestYLevel) {
      this.highestYLevel = highestYLevel;
   }

   @Generated
   public List<World> getValidWorlds() {
      return this.validWorlds;
   }

   @Generated
   public void setValidWorlds(List<World> validWorlds) {
      this.validWorlds = validWorlds;
   }

   @Generated
   public List<Environment> getValidWorldEnvironments() {
      return this.validWorldEnvironments;
   }

   @Generated
   public void setValidWorldEnvironments(List<Environment> validWorldEnvironments) {
      this.validWorldEnvironments = validWorldEnvironments;
   }

   @Generated
   public List<String> getValidBiomesStrings() {
      return this.validBiomesStrings;
   }

   @Generated
   public void setValidBiomesStrings(List<String> validBiomesStrings) {
      this.validBiomesStrings = validBiomesStrings;
   }

   @Generated
   public List<Biome> getValidBiomes() {
      return this.validBiomes;
   }

   @Generated
   public void setValidBiomes(List<Biome> validBiomes) {
      this.validBiomes = validBiomes;
   }

   @Generated
   public long getEarliestTime() {
      return this.earliestTime;
   }

   @Generated
   public void setEarliestTime(long earliestTime) {
      this.earliestTime = earliestTime;
   }

   @Generated
   public long getLatestTime() {
      return this.latestTime;
   }

   @Generated
   public void setLatestTime(long latestTime) {
      this.latestTime = latestTime;
   }

   @Generated
   public MoonPhaseDetector.MoonPhase getMoonPhase() {
      return this.moonPhase;
   }

   @Generated
   public void setMoonPhase(MoonPhaseDetector.MoonPhase moonPhase) {
      this.moonPhase = moonPhase;
   }

   @Generated
   public boolean isBypassWorldGuard() {
      return this.bypassWorldGuard;
   }

   @Generated
   public void setBypassWorldGuard(boolean bypassWorldGuard) {
      this.bypassWorldGuard = bypassWorldGuard;
   }

   @Generated
   public boolean isCanSpawnInLight() {
      return this.canSpawnInLight;
   }

   @Generated
   public void setCanSpawnInLight(boolean canSpawnInLight) {
      this.canSpawnInLight = canSpawnInLight;
   }

   @Generated
   public boolean isSurfaceSpawn() {
      return this.isSurfaceSpawn;
   }

   @Generated
   public void setSurfaceSpawn(boolean isSurfaceSpawn) {
      this.isSurfaceSpawn = isSurfaceSpawn;
   }

   @Generated
   public boolean isUndergroundSpawn() {
      return this.isUndergroundSpawn;
   }

   @Generated
   public void setUndergroundSpawn(boolean isUndergroundSpawn) {
      this.isUndergroundSpawn = isUndergroundSpawn;
   }
}
