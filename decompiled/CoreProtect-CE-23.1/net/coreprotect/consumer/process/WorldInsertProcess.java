package net.coreprotect.consumer.process;

import java.sql.PreparedStatement;
import java.sql.Statement;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.statement.MaterialStatement;
import net.coreprotect.database.statement.WorldStatement;
import net.coreprotect.language.Phrase;
import net.coreprotect.utility.Chat;

class WorldInsertProcess {
   static void process(PreparedStatement preparedStmt, int batchCount, Statement statement, Object world, int worldId) {
      if (world instanceof String) {
         String query = "SELECT id FROM " + ConfigHandler.prefix + "world WHERE id = '" + worldId + "' LIMIT 0, 1";
         boolean hasMaterial = MaterialStatement.hasMaterial(statement, query);
         if (!hasMaterial) {
            WorldStatement.insert(preparedStmt, batchCount, worldId, (String)world);
            ConfigHandler.worlds.put((String)world, worldId);
            ConfigHandler.worldsReversed.put(worldId, (String)world);
            if (worldId > ConfigHandler.worldId) {
               ConfigHandler.worldId = worldId;
            }
         } else {
            Chat.console(Phrase.build(Phrase.CACHE_ERROR, "world"));
            Chat.console(Phrase.build(Phrase.CACHE_RELOAD, "{2}"));
            ConfigHandler.loadWorlds(statement);
         }
      }
   }
}
