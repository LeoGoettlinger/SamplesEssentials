package org.sethomegui.SetHomeGUI.libs.redis.jedis.executors;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandObject;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Connection;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.providers.ConnectionProvider;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.IOUtils;

public class DefaultCommandExecutor implements CommandExecutor {
   protected final ConnectionProvider provider;

   public DefaultCommandExecutor(ConnectionProvider provider) {
      this.provider = provider;
   }

   @Override
   public void close() {
      IOUtils.closeQuietly(this.provider);
   }

   @Override
   public final <T> T executeCommand(CommandObject<T> commandObject) {
      Object var4;
      try (Connection connection = this.provider.getConnection(commandObject.getArguments())) {
         var4 = connection.executeCommand(commandObject);
      }

      return (T)var4;
   }
}
