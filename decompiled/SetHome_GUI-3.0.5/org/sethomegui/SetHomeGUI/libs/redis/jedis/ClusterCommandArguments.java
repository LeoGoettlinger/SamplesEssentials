package org.sethomegui.SetHomeGUI.libs.redis.jedis;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.commands.ProtocolCommand;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.exceptions.JedisClusterOperationException;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.JedisClusterCRC16;

public class ClusterCommandArguments extends CommandArguments {
   private int commandHashSlot = -1;

   public ClusterCommandArguments(ProtocolCommand command) {
      super(command);
   }

   public int getCommandHashSlot() {
      return this.commandHashSlot;
   }

   @Override
   protected CommandArguments processKey(byte[] key) {
      int hashSlot = JedisClusterCRC16.getSlot(key);
      if (this.commandHashSlot < 0) {
         this.commandHashSlot = hashSlot;
      } else if (this.commandHashSlot != hashSlot) {
         throw new JedisClusterOperationException("Keys must belong to same hashslot.");
      }

      return this;
   }

   @Override
   protected CommandArguments processKey(String key) {
      int hashSlot = JedisClusterCRC16.getSlot(key);
      if (this.commandHashSlot < 0) {
         this.commandHashSlot = hashSlot;
      } else if (this.commandHashSlot != hashSlot) {
         throw new JedisClusterOperationException("Keys must belong to same hashslot.");
      }

      return this;
   }
}
