package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import java.util.List;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Response;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.BitCountOption;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.BitOP;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.BitPosParams;

public interface BitPipelineCommands {
   Response<Boolean> setbit(String var1, long var2, boolean var4);

   Response<Boolean> getbit(String var1, long var2);

   Response<Long> bitcount(String var1);

   Response<Long> bitcount(String var1, long var2, long var4);

   Response<Long> bitcount(String var1, long var2, long var4, BitCountOption var6);

   Response<Long> bitpos(String var1, boolean var2);

   Response<Long> bitpos(String var1, boolean var2, BitPosParams var3);

   Response<List<Long>> bitfield(String var1, String... var2);

   Response<List<Long>> bitfieldReadonly(String var1, String... var2);

   Response<Long> bitop(BitOP var1, String var2, String... var3);
}
