package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import java.util.List;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Response;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.FlushMode;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.KeyValue;

public interface SampleKeyedPipelineCommands {
   Response<Long> waitReplicas(String var1, int var2, long var3);

   Response<KeyValue<Long, Long>> waitAOF(String var1, long var2, long var4, long var6);

   Response<Object> eval(String var1, String var2);

   Response<Object> evalsha(String var1, String var2);

   Response<List<Boolean>> scriptExists(String var1, String... var2);

   Response<String> scriptLoad(String var1, String var2);

   Response<String> scriptFlush(String var1);

   Response<String> scriptFlush(String var1, FlushMode var2);

   Response<String> scriptKill(String var1);
}
