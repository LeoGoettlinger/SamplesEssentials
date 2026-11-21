package org.sethomegui.SetHomeGUI.libs.redis.jedis.csc.util;

import java.util.List;
import java.util.Set;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.commands.ProtocolCommand;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.csc.Cacheable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.csc.DefaultCacheable;

public class AllowAndDenyListWithStringKeys implements Cacheable {
   private final Set<ProtocolCommand> allowCommands;
   private final Set<ProtocolCommand> denyCommands;
   private final Set<String> allowKeys;
   private final Set<String> denyKeys;

   public AllowAndDenyListWithStringKeys(Set<ProtocolCommand> allowCommands, Set<ProtocolCommand> denyCommands, Set<String> allowKeys, Set<String> denyKeys) {
      this.allowCommands = allowCommands;
      this.denyCommands = denyCommands;
      this.allowKeys = allowKeys;
      this.denyKeys = denyKeys;
   }

   @Override
   public boolean isCacheable(ProtocolCommand command, List<Object> keys) {
      if (this.allowCommands != null && !this.allowCommands.contains(command)) {
         return false;
      } else if (this.denyCommands != null && this.denyCommands.contains(command)) {
         return false;
      } else {
         for (Object key : keys) {
            if (!(key instanceof String)) {
               return false;
            }

            if (this.allowKeys != null && !this.allowKeys.contains((String)key)) {
               return false;
            }

            if (this.denyKeys != null && this.denyKeys.contains((String)key)) {
               return false;
            }
         }

         return DefaultCacheable.isDefaultCacheableCommand(command);
      }
   }
}
