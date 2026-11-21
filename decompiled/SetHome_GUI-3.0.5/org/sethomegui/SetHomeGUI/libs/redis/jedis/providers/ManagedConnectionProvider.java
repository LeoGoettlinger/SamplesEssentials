package org.sethomegui.SetHomeGUI.libs.redis.jedis.providers;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Connection;

public class ManagedConnectionProvider implements ConnectionProvider {
   private Connection connection;

   public final void setConnection(Connection connection) {
      this.connection = connection;
   }

   @Override
   public void close() {
   }

   @Override
   public final Connection getConnection() {
      return this.connection;
   }

   @Override
   public final Connection getConnection(CommandArguments args) {
      return this.connection;
   }
}
