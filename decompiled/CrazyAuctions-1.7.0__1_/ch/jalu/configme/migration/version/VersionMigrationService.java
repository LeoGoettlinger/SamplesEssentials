package ch.jalu.configme.migration.version;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.migration.MigrationService;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.resource.PropertyReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class VersionMigrationService implements MigrationService {
   private final Property<Integer> versionProperty;
   private final Map<Integer, VersionMigration> migrationsByStartVersion;

   public VersionMigrationService(@NotNull Property<Integer> versionProperty, @NotNull Iterable<VersionMigration> migrations) {
      this.versionProperty = versionProperty;
      this.migrationsByStartVersion = this.validateAndGroupMigrationsByFromVersion(migrations);
   }

   public VersionMigrationService(@NotNull Property<Integer> versionProperty, @NotNull VersionMigration... migrations) {
      this(versionProperty, Arrays.asList(migrations));
   }

   @Override
   public boolean checkAndMigrate(@NotNull PropertyReader reader, @NotNull ConfigurationData configurationData) {
      return this.performMigrations(reader, configurationData) || !configurationData.areAllValuesValidInResource();
   }

   @NotNull
   protected final Property<Integer> getVersionProperty() {
      return this.versionProperty;
   }

   @NotNull
   protected final Map<Integer, VersionMigration> getMigrationsByStartVersion() {
      return this.migrationsByStartVersion;
   }

   protected boolean performMigrations(@NotNull PropertyReader reader, @NotNull ConfigurationData configurationData) {
      int readConfigVersion = this.versionProperty.determineValue(reader).getValue();
      int configVersion = this.versionProperty.getDefaultValue();
      if (readConfigVersion == configVersion) {
         return false;
      } else {
         this.runApplicableMigrations(readConfigVersion, reader, configurationData);
         configurationData.setValue(this.versionProperty, configVersion);
         return true;
      }
   }

   protected int runApplicableMigrations(int readConfigVersion, @NotNull PropertyReader reader, @NotNull ConfigurationData configurationData) {
      int updatedVersion = readConfigVersion;

      for (VersionMigration migration = this.migrationsByStartVersion.get(readConfigVersion);
         migration != null;
         migration = this.migrationsByStartVersion.get(updatedVersion)
      ) {
         migration.migrate(reader, configurationData);
         updatedVersion = migration.targetVersion();
      }

      return updatedVersion;
   }

   protected Map<Integer, VersionMigration> validateAndGroupMigrationsByFromVersion(Iterable<VersionMigration> migrations) {
      Map<Integer, VersionMigration> migrationsByStartVersion = new HashMap<>();

      for (VersionMigration migration : migrations) {
         this.validateVersions(migration);
         int fromVersion = migration.fromVersion();
         if (migrationsByStartVersion.put(fromVersion, migration) != null) {
            throw new IllegalArgumentException("Multiple migrations were provided for start version " + fromVersion);
         }
      }

      return migrationsByStartVersion;
   }

   protected void validateVersions(VersionMigration migration) {
      if (migration.targetVersion() > this.versionProperty.getDefaultValue()) {
         throw new IllegalArgumentException(
            "The migration from version "
               + migration.fromVersion()
               + " to version "
               + migration.targetVersion()
               + " has an invalid target version. Current configuration version is: "
               + this.versionProperty.getDefaultValue()
         );
      } else if (migration.fromVersion() >= migration.targetVersion()) {
         throw new IllegalArgumentException(
            "A migration from version "
               + migration.fromVersion()
               + " to version "
               + migration.targetVersion()
               + " was supplied, but it is expected that the target version be larger than the start version"
         );
      }
   }
}
