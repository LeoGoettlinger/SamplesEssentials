package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import java.util.List;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.FlushMode;

public interface ScriptingControlCommands {
   Boolean scriptExists(String var1);

   List<Boolean> scriptExists(String... var1);

   Boolean scriptExists(byte[] var1);

   List<Boolean> scriptExists(byte[]... var1);

   String scriptLoad(String var1);

   byte[] scriptLoad(byte[] var1);

   String scriptFlush();

   String scriptFlush(FlushMode var1);

   String scriptKill();
}
