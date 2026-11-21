package com.oheers.fish.database.migrate;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.Database;
import com.oheers.fish.database.connection.ConnectionFactory;
import com.oheers.fish.database.connection.MigrationManager;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.PrefixType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import org.jspecify.annotations.NonNull;

public class LegacyToV3DatabaseMigration {
   private final Database database;
   private final MigrationManager migrationManager;
   private final Jdbi jdbi;

   public LegacyToV3DatabaseMigration(final Database database, final MigrationManager migrationManager, final ConnectionFactory connectionFactory) {
      this.database = database;
      this.migrationManager = migrationManager;
      this.jdbi = Jdbi.create(connectionFactory::getConnection);
   }

   private void translateFishDataV2() {
      if (!this.migrationManager.queryTableExistence(this.prefixed("fish"))) {
         if (this.migrationManager.queryTableExistence("Fish2")) {
            this.execute("alter table Fish2 rename to " + this.prefixed("fish"));
         } else {
            this.migrationManager.legacyInitVersion();
         }
      }
   }

   private void translateFishReportsV2(final UUID uuid, @NonNull final List<LegacyFishReport> reports) {
      String firstFishID = "";
      long epochFirst = Long.MAX_VALUE;
      String largestFishID = "";
      float largestSize = 0.0F;
      int totalFish = 0;
      int userId = this.database.getUserId(uuid);

      for (LegacyFishReport report : reports) {
         if (report.getTimeEpoch() < epochFirst) {
            epochFirst = report.getTimeEpoch();
            firstFishID = report.getRarity() + ":" + report.getName();
         }

         if (report.getLargestLength() > largestSize) {
            largestSize = report.getLargestLength();
            largestFishID = report.getRarity() + ":" + report.getName();
         }

         totalFish += report.getNumCaught();
         this.jdbi
            .useHandle(
               handle -> handle.createUpdate(
                     "insert into "
                        + this.prefixed("fish_log")
                        + " (id, rarity, fish, quantity, first_catch_time, largest_length) values (:id, :rarity, :fish, :quantity, :first_catch_time, :largest_length)"
                  )
                  .bind("id", userId)
                  .bind("rarity", report.getRarity())
                  .bind("fish", report.getName())
                  .bind("quantity", report.getNumCaught())
                  .bind("first_catch_time", report.getTimeEpoch())
                  .bind("largest_length", report.getLargestLength())
                  .execute()
            );
      }

      this.createFieldForFishFirstTimeFished(uuid, firstFishID, largestFishID, totalFish, largestSize);
   }

   private void createFieldForFishFirstTimeFished(final UUID uuid, final String firstFishID, final String largestFishID, int totalFish, float largestSize) {
      this.jdbi
         .useHandle(
            handle -> handle.createUpdate(
                  "update "
                     + this.prefixed("users")
                     + " set first_fish = :first_fish, largest_fish = :largest_fish, num_fish_caught = :num_fish_caught, largest_length = :largest_length where uuid = :uuid"
               )
               .bind("first_fish", firstFishID)
               .bind("largest_fish", largestFishID)
               .bind("num_fish_caught", totalFish)
               .bind("largest_length", largestSize)
               .bind("uuid", uuid.toString())
               .execute()
         );
   }

   public void migrate(CommandSender initiator) {
      if (!this.migrationManager.usingV2()) {
         EMFSingleMessage msg = EMFSingleMessage.fromString("EvenMoreFish is already using the latest V3 database engine.");
         msg.prependMessage(PrefixType.ERROR.getPrefix());
         msg.send(initiator);
      } else {
         EvenMoreFish.getInstance().getLogger().info(() -> initiator.getName() + " has begun the migration to EMF database V3 from V2.");
         EMFSingleMessage msg = EMFSingleMessage.fromString("Beginning conversion to V3 database engine.");
         msg.prependMessage(PrefixType.ADMIN.getPrefix());
         msg.send(initiator);
         File oldDataFolder = new File(EvenMoreFish.getInstance().getDataFolder(), "data");
         File dataFolder = new File(EvenMoreFish.getInstance().getDataFolder(), "data-archived");
         if (oldDataFolder.renameTo(dataFolder)) {
            EMFSingleMessage message = EMFSingleMessage.fromString("Archived /data/ folder.");
            message.prependMessage(PrefixType.ADMIN.getPrefix());
            message.send(initiator);
            message = EMFSingleMessage.fromString("Beginning FishReport migrations. This may take a while.");
            message.prependMessage(PrefixType.ADMIN.getPrefix());
            message.send(initiator);

            try {
               this.translateFishDataV2();
               this.migrationManager.legacyFlywayBaseline();

               for (File file : Objects.requireNonNull(dataFolder.listFiles())) {
                  Type fishReportList = (new TypeToken<List<LegacyFishReport>>() {}).getType();
                  Gson gson = new Gson();

                  List<LegacyFishReport> reports;
                  try (FileReader reader = new FileReader(file)) {
                     reports = (List<LegacyFishReport>)gson.fromJson(reader, fishReportList);
                  }

                  UUID var26 = UUID.fromString(file.getName().substring(0, file.getName().lastIndexOf(".")));
                  this.createEmptyUserReport(var26);
                  this.translateFishReportsV2(var26, reports);
                  EMFSingleMessage migratedMSG = EMFSingleMessage.fromString("Migrated " + reports.size() + " fish for: " + var26);
                  migratedMSG.prependMessage(PrefixType.ADMIN.getPrefix());
                  migratedMSG.send(initiator);
               }
            } catch (FileNotFoundException | NullPointerException var18) {
               EMFSingleMessage messagex = EMFSingleMessage.fromString("Fatal error whilst upgrading to V3 database engine.");
               messagex.prependMessage(PrefixType.ERROR.getPrefix());
               messagex.send(initiator);
               EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Critical SQL/interruption error whilst upgrading to v3 engine.", (Throwable)var18);
            } catch (IOException var19) {
               EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, var19.getMessage(), (Throwable)var19);
               throw new RuntimeException(var19);
            }

            EMFSingleMessage migratedMSG = EMFSingleMessage.fromString(
               "Migration completed. Your database is now using the V3 database engine: to complete the migration, it is recommended to restart your server."
            );
            migratedMSG.prependMessage(PrefixType.ADMIN.getPrefix());
            migratedMSG.send(initiator);
            EMFSingleMessage thankyou = EMFSingleMessage.fromString(
               "Now that migration is complete, you will be able to use functionality in upcoming updates such as quests, deliveries and a fish log. - Oheers"
            );
            thankyou.prependMessage(PrefixType.ERROR.getPrefix());
            thankyou.send(initiator);
            this.migrationManager.migrateFromV5ToLatest();
         } else {
            EMFSingleMessage message = EMFSingleMessage.fromString("Failed to archive /data/ folder. Cancelling migration. [No further information]");
            message.prependMessage(PrefixType.ADMIN.getPrefix());
            message.send(initiator);
         }
      }
   }

   public void createEmptyUserReport(@NonNull UUID uuid) {
      this.jdbi
         .useHandle(
            handle -> {
               Update update = handle.createUpdate(
                  "insert into "
                     + this.prefixed("users")
                     + " (uuid, first_fish, last_fish, largest_fish, largest_length, num_fish_caught, total_fish_length, competitions_won, competitions_joined) values (:uuid, :first_fish, :last_fish, :largest_fish, :largest_length, :num_fish_caught, :total_fish_length, :competitions_won, :competitions_joined)"
               );
               update.bind("uuid", uuid.toString())
                  .bind("first_fish", "None")
                  .bind("last_fish", "None")
                  .bind("largest_fish", "None")
                  .bind("largest_length", 0.0F)
                  .bind("num_fish_caught", 0)
                  .bind("total_fish_length", 0.0F)
                  .bind("competitions_won", 0)
                  .bind("competitions_joined", 0);
               update.execute();
            }
         );
   }

   private void execute(@NonNull String sql) {
      this.jdbi.useHandle(handle -> handle.execute(sql));
   }

   @NonNull
   private String prefixed(@NonNull String tableName) {
      return MainConfig.getInstance().getPrefix() + tableName;
   }
}
