package org.sethomegui.SetHomeGUI.libs.redis.jedis.csc;

import java.util.List;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.commands.ProtocolCommand;

public interface Cacheable {
   boolean isCacheable(ProtocolCommand var1, List<Object> var2);
}
