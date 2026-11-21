package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.StreamEntryID;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.XAddParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.XAutoClaimParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.XClaimParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.XPendingParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.XReadGroupParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.XReadParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.XTrimParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.StreamConsumerInfo;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.StreamConsumersInfo;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.StreamEntry;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.StreamFullInfo;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.StreamGroupInfo;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.StreamInfo;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.StreamPendingEntry;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.StreamPendingSummary;

public interface StreamCommands {
   StreamEntryID xadd(String var1, StreamEntryID var2, Map<String, String> var3);

   default StreamEntryID xadd(String key, Map<String, String> hash, XAddParams params) {
      return this.xadd(key, params, hash);
   }

   StreamEntryID xadd(String var1, XAddParams var2, Map<String, String> var3);

   long xlen(String var1);

   List<StreamEntry> xrange(String var1, StreamEntryID var2, StreamEntryID var3);

   List<StreamEntry> xrange(String var1, StreamEntryID var2, StreamEntryID var3, int var4);

   List<StreamEntry> xrevrange(String var1, StreamEntryID var2, StreamEntryID var3);

   List<StreamEntry> xrevrange(String var1, StreamEntryID var2, StreamEntryID var3, int var4);

   List<StreamEntry> xrange(String var1, String var2, String var3);

   List<StreamEntry> xrange(String var1, String var2, String var3, int var4);

   List<StreamEntry> xrevrange(String var1, String var2, String var3);

   List<StreamEntry> xrevrange(String var1, String var2, String var3, int var4);

   long xack(String var1, String var2, StreamEntryID... var3);

   String xgroupCreate(String var1, String var2, StreamEntryID var3, boolean var4);

   String xgroupSetID(String var1, String var2, StreamEntryID var3);

   long xgroupDestroy(String var1, String var2);

   boolean xgroupCreateConsumer(String var1, String var2, String var3);

   long xgroupDelConsumer(String var1, String var2, String var3);

   long xdel(String var1, StreamEntryID... var2);

   long xtrim(String var1, long var2, boolean var4);

   long xtrim(String var1, XTrimParams var2);

   StreamPendingSummary xpending(String var1, String var2);

   List<StreamPendingEntry> xpending(String var1, String var2, XPendingParams var3);

   List<StreamEntry> xclaim(String var1, String var2, String var3, long var4, XClaimParams var6, StreamEntryID... var7);

   List<StreamEntryID> xclaimJustId(String var1, String var2, String var3, long var4, XClaimParams var6, StreamEntryID... var7);

   Entry<StreamEntryID, List<StreamEntry>> xautoclaim(String var1, String var2, String var3, long var4, StreamEntryID var6, XAutoClaimParams var7);

   Entry<StreamEntryID, List<StreamEntryID>> xautoclaimJustId(String var1, String var2, String var3, long var4, StreamEntryID var6, XAutoClaimParams var7);

   StreamInfo xinfoStream(String var1);

   StreamFullInfo xinfoStreamFull(String var1);

   StreamFullInfo xinfoStreamFull(String var1, int var2);

   List<StreamGroupInfo> xinfoGroups(String var1);

   @Deprecated
   List<StreamConsumersInfo> xinfoConsumers(String var1, String var2);

   List<StreamConsumerInfo> xinfoConsumers2(String var1, String var2);

   List<Entry<String, List<StreamEntry>>> xread(XReadParams var1, Map<String, StreamEntryID> var2);

   Map<String, List<StreamEntry>> xreadAsMap(XReadParams var1, Map<String, StreamEntryID> var2);

   List<Entry<String, List<StreamEntry>>> xreadGroup(String var1, String var2, XReadGroupParams var3, Map<String, StreamEntryID> var4);

   Map<String, List<StreamEntry>> xreadGroupAsMap(String var1, String var2, XReadGroupParams var3, Map<String, StreamEntryID> var4);
}
