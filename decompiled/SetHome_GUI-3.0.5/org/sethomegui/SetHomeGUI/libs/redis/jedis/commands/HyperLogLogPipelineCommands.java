package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.Response;

public interface HyperLogLogPipelineCommands {
   Response<Long> pfadd(String var1, String... var2);

   Response<String> pfmerge(String var1, String... var2);

   Response<Long> pfcount(String var1);

   Response<Long> pfcount(String... var1);
}
