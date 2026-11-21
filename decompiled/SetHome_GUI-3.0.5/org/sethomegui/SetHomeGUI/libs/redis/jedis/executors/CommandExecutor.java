package org.sethomegui.SetHomeGUI.libs.redis.jedis.executors;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandObject;

public interface CommandExecutor extends AutoCloseable {
   <T> T executeCommand(CommandObject<T> var1);

   default <T> T broadcastCommand(CommandObject<T> commandObject) {
      return this.executeCommand(commandObject);
   }
}
