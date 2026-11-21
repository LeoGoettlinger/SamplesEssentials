package org.sethomegui.SetHomeGUI.libs.redis.jedis.providers;

import java.util.Collections;
import java.util.Map;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Connection;

public interface ConnectionProvider extends AutoCloseable {
   Connection getConnection();

   Connection getConnection(CommandArguments var1);

   default Map<?, ?> getConnectionMap() {
      Connection c = this.getConnection();
      return Collections.singletonMap(c.toString(), c);
   }
}
