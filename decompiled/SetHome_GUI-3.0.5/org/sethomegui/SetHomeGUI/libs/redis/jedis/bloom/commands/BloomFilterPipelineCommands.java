package org.sethomegui.SetHomeGUI.libs.redis.jedis.bloom.commands;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Response;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.bloom.BFInsertParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.bloom.BFReserveParams;

public interface BloomFilterPipelineCommands {
   Response<String> bfReserve(String var1, double var2, long var4);

   Response<String> bfReserve(String var1, double var2, long var4, BFReserveParams var6);

   Response<Boolean> bfAdd(String var1, String var2);

   Response<List<Boolean>> bfMAdd(String var1, String... var2);

   Response<List<Boolean>> bfInsert(String var1, String... var2);

   Response<List<Boolean>> bfInsert(String var1, BFInsertParams var2, String... var3);

   Response<Boolean> bfExists(String var1, String var2);

   Response<List<Boolean>> bfMExists(String var1, String... var2);

   Response<Entry<Long, byte[]>> bfScanDump(String var1, long var2);

   Response<String> bfLoadChunk(String var1, long var2, byte[] var4);

   Response<Long> bfCard(String var1);

   Response<Map<String, Object>> bfInfo(String var1);
}
