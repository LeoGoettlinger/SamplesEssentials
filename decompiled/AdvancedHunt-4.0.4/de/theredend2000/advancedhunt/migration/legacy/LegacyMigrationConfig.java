package de.theredend2000.advancedhunt.migration.legacy;

import java.io.File;

public final class LegacyMigrationConfig {
   private static final boolean DEFAULT_CREATE_BACKUPS = true;
   private static final boolean DEFAULT_FAIL_FAST = true;
   private static final int DEFAULT_SNAPSHOT_BATCH_PER_TICK = 250;
   private final boolean enabled;
   private final File pluginDataFolder;

   private LegacyMigrationConfig(boolean enabled, File pluginDataFolder) {
      this.enabled = enabled;
      this.pluginDataFolder = pluginDataFolder;
   }

   public static LegacyMigrationConfig create(File pluginDataFolder) {
      return new LegacyMigrationConfig(false, pluginDataFolder);
   }

   public boolean enabled() {
      return this.enabled;
   }

   public File sourceFolder() {
      return this.pluginDataFolder;
   }

   public boolean createBackups() {
      return true;
   }

   public boolean failFast() {
      return true;
   }

   public int snapshotBatchPerTick() {
      return 250;
   }

   public String markerFileName() {
      return ".legacy-migration-complete";
   }

   public LegacyMigrationConfig withEnabled(boolean enabled) {
      return new LegacyMigrationConfig(enabled, this.pluginDataFolder);
   }
}
