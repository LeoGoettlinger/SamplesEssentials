package me.chancesd.pvpmanager.storage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.database.DatabaseConfigBuilder;
import me.chancesd.pvpmanager.sdutils.utils.Log;

public interface Storage {
   boolean userExists(UUID var1);

   default boolean userExists(CombatPlayer player) {
      return this.userExists(player.getUUID());
   }

   Map<String, Object> getUserData(UUID var1);

   default Map<String, Object> getUserData(CombatPlayer player) {
      return this.getUserData(player.getUUID());
   }

   void removeUserData(UUID var1);

   default void removeUserData(CombatPlayer player) {
      this.removeUserData(player.getUUID());
   }

   boolean saveUserData(UUID var1, Map<String, Object> var2);

   default void saveUserData(CombatPlayer player) {
      long start = System.nanoTime();
      this.saveUserData(player.getUUID(), player.exportPlayerData().toMap());
      Log.debug("Finished saving " + player + " - " + (System.nanoTime() - start) / 1000000.0 + " ms");
   }

   List<Map<String, Object>> getAllUserData();

   void saveUserDataBatch(Collection<CombatPlayer> var1);

   void increment(String var1, UUID var2);

   Map<String, Object> getWorldData(String var1);

   boolean saveWorldData(String var1, Map<String, Object> var2);

   void shutdown();

   DatabaseConfigBuilder.DatabaseType getDatabaseType();
}
