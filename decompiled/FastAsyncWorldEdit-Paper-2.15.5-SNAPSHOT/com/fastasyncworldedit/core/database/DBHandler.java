package com.fastasyncworldedit.core.database;

import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.world.World;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Logger;

public class DBHandler {
   @Deprecated(
      forRemoval = true,
      since = "2.0.0"
   )
   public static final DBHandler IMP = dbHandler();
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static DBHandler INSTANCE;
   private final Map<World, RollbackDatabase> databases = new ConcurrentHashMap<>(8, 0.9F, 1);

   public static DBHandler dbHandler() {
      if (INSTANCE == null) {
         INSTANCE = new DBHandler();
      }

      return INSTANCE;
   }

   public RollbackDatabase getDatabase(World world) {
      RollbackDatabase database = this.databases.get(world);
      if (database != null) {
         return database;
      } else {
         try {
            database = new RollbackDatabase(world);
            this.databases.put(world, database);
            return database;
         } catch (Throwable var4) {
            LOGGER.error("No JDBC driver found!", var4);
            return null;
         }
      }
   }
}
