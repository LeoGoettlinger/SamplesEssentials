package ac.grim.grimac.internal.storage.backend.mongo.v2;

import ac.grim.grimac.api.storage.registry.MigrationContext;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import com.mongodb.client.MongoDatabase;
import java.util.logging.Logger;

@ApiStatus.Internal
public final class MongoMigrationContext implements MigrationContext {
   @NotNull
   private final MongoDatabase database;
   @NotNull
   private final Logger logger;

   public MongoMigrationContext(@NotNull MongoDatabase database, @NotNull Logger logger) {
      this.database = database;
      this.logger = logger;
   }

   @NotNull
   public MongoDatabase database() {
      return this.database;
   }

   @NotNull
   public Logger logger() {
      return this.logger;
   }
}
