package github.nighter.smartspawner.api.impl;

import github.nighter.smartspawner.api.data.SpawnerDataModifier;
import github.nighter.smartspawner.spawner.properties.SpawnerData;

public class SpawnerDataModifierImpl implements SpawnerDataModifier {
   private final SpawnerData spawnerData;
   private int pendingMaxStackSize;
   private int pendingBaseMaxStoragePages;
   private int pendingBaseMinMobs;
   private int pendingBaseMaxMobs;
   private long pendingBaseMaxStoredExp;
   private long pendingBaseSpawnerDelay;
   private boolean maxStackSizeChanged = false;
   private boolean baseMaxStoragePagesChanged = false;
   private boolean baseMinMobsChanged = false;
   private boolean baseMaxMobsChanged = false;
   private boolean baseMaxStoredExpChanged = false;
   private boolean baseSpawnerDelayChanged = false;

   public SpawnerDataModifierImpl(SpawnerData spawnerData) {
      this.spawnerData = spawnerData;
      this.pendingMaxStackSize = spawnerData.getMaxStackSize();
      this.pendingBaseMaxStoragePages = spawnerData.getBaseMaxStoragePages();
      this.pendingBaseMinMobs = spawnerData.getBaseMinMobs();
      this.pendingBaseMaxMobs = spawnerData.getBaseMaxMobs();
      this.pendingBaseMaxStoredExp = spawnerData.getBaseMaxStoredExp();
      this.pendingBaseSpawnerDelay = spawnerData.getSpawnDelay();
   }

   @Override
   public int getStackSize() {
      return this.spawnerData.getStackSize();
   }

   @Override
   public int getMaxStackSize() {
      return this.pendingMaxStackSize;
   }

   @Override
   public SpawnerDataModifier setMaxStackSize(int maxStackSize) {
      this.pendingMaxStackSize = maxStackSize;
      this.maxStackSizeChanged = true;
      return this;
   }

   @Override
   public int getBaseMaxStoragePages() {
      return this.pendingBaseMaxStoragePages;
   }

   @Override
   public SpawnerDataModifier setBaseMaxStoragePages(int baseMaxStoragePages) {
      this.pendingBaseMaxStoragePages = baseMaxStoragePages;
      this.baseMaxStoragePagesChanged = true;
      return this;
   }

   @Override
   public int getBaseMinMobs() {
      return this.pendingBaseMinMobs;
   }

   @Override
   public SpawnerDataModifier setBaseMinMobs(int baseMinMobs) {
      this.pendingBaseMinMobs = baseMinMobs;
      this.baseMinMobsChanged = true;
      return this;
   }

   @Override
   public int getBaseMaxMobs() {
      return this.pendingBaseMaxMobs;
   }

   @Override
   public SpawnerDataModifier setBaseMaxMobs(int baseMaxMobs) {
      this.pendingBaseMaxMobs = baseMaxMobs;
      this.baseMaxMobsChanged = true;
      return this;
   }

   @Override
   public long getBaseMaxStoredExp() {
      return this.pendingBaseMaxStoredExp;
   }

   @Override
   public SpawnerDataModifier setBaseMaxStoredExp(long baseMaxStoredExp) {
      this.pendingBaseMaxStoredExp = baseMaxStoredExp;
      this.baseMaxStoredExpChanged = true;
      return this;
   }

   @Override
   public long getBaseSpawnerDelay() {
      return this.pendingBaseSpawnerDelay;
   }

   @Override
   public SpawnerDataModifier setBaseSpawnerDelay(long baseSpawnerDelay) {
      this.pendingBaseSpawnerDelay = baseSpawnerDelay;
      this.baseSpawnerDelayChanged = true;
      return this;
   }

   @Override
   public void applyChanges() {
      if (this.maxStackSizeChanged) {
         this.spawnerData.setMaxStackSize(this.pendingMaxStackSize);
      }

      if (this.baseMaxStoragePagesChanged) {
         this.spawnerData.setBaseMaxStoragePages(this.pendingBaseMaxStoragePages);
      }

      if (this.baseMinMobsChanged) {
         this.spawnerData.setBaseMinMobs(this.pendingBaseMinMobs);
      }

      if (this.baseMaxMobsChanged) {
         this.spawnerData.setBaseMaxMobs(this.pendingBaseMaxMobs);
      }

      if (this.baseMaxStoredExpChanged) {
         this.spawnerData.setBaseMaxStoredExp(this.pendingBaseMaxStoredExp);
      }

      if (this.baseSpawnerDelayChanged) {
         this.spawnerData.setSpawnDelay(this.pendingBaseSpawnerDelay);
      }

      this.spawnerData.recalculateAfterAPIModification();
   }
}
