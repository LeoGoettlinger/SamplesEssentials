package com.magmaguy.elitemobs.playerdata.database;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class GenerateDatabase {
   private GenerateDatabase() {
   }

   public static void generate() throws Exception {
      synchronized (PlayerDataRepository.jdbcMonitor()) {
         generateLocked();
      }
   }

   private static void generateLocked() throws Exception {
      try (Statement statement = PlayerData.getConnection().createStatement()) {
         String sql = "CREATE TABLE IF NOT EXISTS "
            + PlayerData.getPLAYER_DATA_TABLE_NAME()
            + " (PlayerUUID VARCHAR(36) PRIMARY KEY NOT NULL, DisplayName TEXT, CurrencyV2 DOUBLE, CurrencyCents BIGINT, QuestStatus BLOB, Score INT, Kills INT, HighestLevelKilled INT, Deaths INT, QuestsCompleted INT, DungeonsCompleted INT, PlayerQuestCooldowns BLOB, BackTeleportLocation TEXT, UseBookMenus TINYINT(1), DismissEMStatusScreenMessage TINYINT(1), DungeonBossLockouts BLOB, QuestLockouts BLOB, SkillXP_ARMOR BIGINT, SkillXP_SWORDS BIGINT, SkillXP_AXES BIGINT, SkillXP_BOWS BIGINT, SkillXP_CROSSBOWS BIGINT, SkillXP_TRIDENTS BIGINT, SkillXP_HOES BIGINT, SkillXP_MACES BIGINT, SkillXP_SPEARS BIGINT, SkillXP_STAVES BIGINT, SkillXP_WANDS BIGINT, SkillBonusSelections BLOB, GamblingDebt DOUBLE, GamblingDebtCents BIGINT);";
         statement.executeUpdate(sql);
      }

      createAdvancedCombatTables();
      Set<String> columns = new HashSet<>();

      try (ResultSet existing = PlayerData.getConnection().getMetaData().getColumns(null, null, PlayerData.getPLAYER_DATA_TABLE_NAME(), null)) {
         while (existing.next()) {
            columns.add(existing.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
         }
      }

      addEntryIfEmpty(columns, "DisplayName", GenerateDatabase.ColumnValues.TEXT);
      addEntryIfEmpty(columns, "CurrencyV2", GenerateDatabase.ColumnValues.REAL);
      addEntryIfEmpty(columns, "CurrencyCents", GenerateDatabase.ColumnValues.BIGINT);
      addEntryIfEmpty(columns, "QuestStatus", GenerateDatabase.ColumnValues.BLOB);
      addEntryIfEmpty(columns, "Score", GenerateDatabase.ColumnValues.INT);
      addEntryIfEmpty(columns, "Kills", GenerateDatabase.ColumnValues.INT);
      addEntryIfEmpty(columns, "HighestLevelKilled", GenerateDatabase.ColumnValues.INT);
      addEntryIfEmpty(columns, "Deaths", GenerateDatabase.ColumnValues.INT);
      addEntryIfEmpty(columns, "QuestsCompleted", GenerateDatabase.ColumnValues.INT);
      addEntryIfEmpty(columns, "DungeonsCompleted", GenerateDatabase.ColumnValues.INT);
      addEntryIfEmpty(columns, "PlayerQuestCooldowns", GenerateDatabase.ColumnValues.BLOB);
      addEntryIfEmpty(columns, "BackTeleportLocation", GenerateDatabase.ColumnValues.TEXT);
      addEntryIfEmpty(columns, "UseBookMenus", GenerateDatabase.ColumnValues.BOOLEAN);
      addEntryIfEmpty(columns, "DismissEMStatusScreenMessage", GenerateDatabase.ColumnValues.BOOLEAN);
      addEntryIfEmpty(columns, "DungeonBossLockouts", GenerateDatabase.ColumnValues.BLOB);
      addEntryIfEmpty(columns, "QuestLockouts", GenerateDatabase.ColumnValues.BLOB);
      addEntryIfEmpty(columns, "SkillXP_ARMOR", GenerateDatabase.ColumnValues.BIGINT);
      addEntryIfEmpty(columns, "SkillXP_SWORDS", GenerateDatabase.ColumnValues.BIGINT);
      addEntryIfEmpty(columns, "SkillXP_AXES", GenerateDatabase.ColumnValues.BIGINT);
      addEntryIfEmpty(columns, "SkillXP_BOWS", GenerateDatabase.ColumnValues.BIGINT);
      addEntryIfEmpty(columns, "SkillXP_CROSSBOWS", GenerateDatabase.ColumnValues.BIGINT);
      addEntryIfEmpty(columns, "SkillXP_TRIDENTS", GenerateDatabase.ColumnValues.BIGINT);
      addEntryIfEmpty(columns, "SkillXP_HOES", GenerateDatabase.ColumnValues.BIGINT);
      addEntryIfEmpty(columns, "SkillXP_MACES", GenerateDatabase.ColumnValues.BIGINT);
      addEntryIfEmpty(columns, "SkillXP_SPEARS", GenerateDatabase.ColumnValues.BIGINT);
      addEntryIfEmpty(columns, "SkillXP_STAVES", GenerateDatabase.ColumnValues.BIGINT);
      addEntryIfEmpty(columns, "SkillXP_WANDS", GenerateDatabase.ColumnValues.BIGINT);
      addEntryIfEmpty(columns, "SkillBonusSelections", GenerateDatabase.ColumnValues.BLOB);
      addEntryIfEmpty(columns, "GamblingDebt", GenerateDatabase.ColumnValues.REAL);
      addEntryIfEmpty(columns, "GamblingDebtCents", GenerateDatabase.ColumnValues.BIGINT);
   }

   private static void createAdvancedCombatTables() throws Exception {
      synchronized (PlayerDataRepository.jdbcMonitor()) {
         try (Statement statement = PlayerDataRepository.connection().createStatement()) {
            statement.executeUpdate(
               "CREATE TABLE IF NOT EXISTS PlayerClassProfile (PlayerUUID VARCHAR(36) PRIMARY KEY NOT NULL, SelectedFormId VARCHAR(64), SelectedInputId VARCHAR(64), TutorialSkillsUsed INTEGER NOT NULL DEFAULT 0, CatalogVersion INTEGER NOT NULL DEFAULT 0)"
            );
            statement.executeUpdate(
               "CREATE TABLE IF NOT EXISTS PlayerClassProgress (PlayerUUID VARCHAR(36) NOT NULL, FormId VARCHAR(64) NOT NULL, XP BIGINT NOT NULL DEFAULT 0, ChallengeCompleted INTEGER NOT NULL DEFAULT 0, CatalogVersion INTEGER NOT NULL DEFAULT 0, PRIMARY KEY (PlayerUUID, FormId))"
            );

            boolean hasTutorialColumn;
            try (ResultSet columns = PlayerDataRepository.connection().getMetaData().getColumns(null, null, "PlayerClassProfile", "TutorialSkillsUsed")) {
               hasTutorialColumn = columns.next();
            }

            if (!hasTutorialColumn) {
               statement.executeUpdate("ALTER TABLE PlayerClassProfile ADD TutorialSkillsUsed INTEGER NOT NULL DEFAULT 0");
            }

            boolean hasChallengeColumn;
            try (ResultSet columns = PlayerDataRepository.connection().getMetaData().getColumns(null, null, "PlayerClassProgress", "ChallengeCompleted")) {
               hasChallengeColumn = columns.next();
            }

            if (!hasChallengeColumn) {
               statement.executeUpdate("ALTER TABLE PlayerClassProgress ADD ChallengeCompleted INTEGER NOT NULL DEFAULT 1");
            }
         }
      }
   }

   private static void addEntryIfEmpty(Set<String> columns, String columnName, GenerateDatabase.ColumnValues type) throws Exception {
      if (!columns.contains(columnName.toLowerCase(Locale.ROOT))) {
         Logger.info("Adding new database column " + columnName);

         try (Statement statement = PlayerData.getConnection().createStatement()) {
            statement.executeUpdate("ALTER TABLE " + PlayerData.getPLAYER_DATA_TABLE_NAME() + " ADD " + columnName + " " + type);
         }

         columns.add(columnName.toLowerCase(Locale.ROOT));
      }
   }

   private static enum ColumnValues {
      BLOB,
      INT,
      BIGINT,
      TEXT,
      REAL,
      BOOLEAN;
   }
}
