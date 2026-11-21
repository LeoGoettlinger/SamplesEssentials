package org.sethomegui.SetHomeGUI.libs.redis.jedis.executors;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandObject;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Connection;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.IOUtils;

public class SimpleCommandExecutor implements CommandExecutor {
   protected final Connection connection;

   public SimpleCommandExecutor(Connection connection) {
      this.connection = connection;
   }

   @Override
   public void close() {
      IOUtils.closeQuietly(this.connection);
   }

   @Override
   public final <T> T executeCommand(CommandObject<T> commandObject) {
      return this.connection.executeCommand(commandObject);
   }
}
