package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import java.util.List;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.Slowlog;

public interface SlowlogCommands {
   String slowlogReset();

   long slowlogLen();

   List<Slowlog> slowlogGet();

   List<Object> slowlogGetBinary();

   List<Slowlog> slowlogGet(long var1);

   List<Object> slowlogGetBinary(long var1);
}
