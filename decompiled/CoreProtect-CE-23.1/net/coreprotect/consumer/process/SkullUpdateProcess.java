package net.coreprotect.consumer.process;

import java.sql.Statement;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.statement.SkullStatement;
import net.coreprotect.utility.BlockUtils;
import org.bukkit.block.BlockState;

class SkullUpdateProcess {
   static void process(Statement statement, Object object, int rowId) {
      if (object instanceof BlockState) {
         BlockState block = (BlockState)object;
         String query = "SELECT owner, skin FROM " + ConfigHandler.prefix + "skull WHERE rowid='" + rowId + "' LIMIT 0, 1";
         SkullStatement.getData(statement, block, query);
         BlockUtils.updateBlock(block);
      }
   }
}
