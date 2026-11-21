package org.sethomegui.SetHomeGUI.libs.redis.jedis.commands;

import java.util.List;
import java.util.Map;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.GeoCoordinate;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Response;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.GeoUnit;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.GeoAddParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.GeoRadiusParam;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.GeoRadiusStoreParam;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.GeoSearchParam;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.resps.GeoRadiusResponse;

public interface GeoPipelineCommands {
   Response<Long> geoadd(String var1, double var2, double var4, String var6);

   Response<Long> geoadd(String var1, Map<String, GeoCoordinate> var2);

   Response<Long> geoadd(String var1, GeoAddParams var2, Map<String, GeoCoordinate> var3);

   Response<Double> geodist(String var1, String var2, String var3);

   Response<Double> geodist(String var1, String var2, String var3, GeoUnit var4);

   Response<List<String>> geohash(String var1, String... var2);

   Response<List<GeoCoordinate>> geopos(String var1, String... var2);

   Response<List<GeoRadiusResponse>> georadius(String var1, double var2, double var4, double var6, GeoUnit var8);

   Response<List<GeoRadiusResponse>> georadiusReadonly(String var1, double var2, double var4, double var6, GeoUnit var8);

   Response<List<GeoRadiusResponse>> georadius(String var1, double var2, double var4, double var6, GeoUnit var8, GeoRadiusParam var9);

   Response<List<GeoRadiusResponse>> georadiusReadonly(String var1, double var2, double var4, double var6, GeoUnit var8, GeoRadiusParam var9);

   Response<List<GeoRadiusResponse>> georadiusByMember(String var1, String var2, double var3, GeoUnit var5);

   Response<List<GeoRadiusResponse>> georadiusByMemberReadonly(String var1, String var2, double var3, GeoUnit var5);

   Response<List<GeoRadiusResponse>> georadiusByMember(String var1, String var2, double var3, GeoUnit var5, GeoRadiusParam var6);

   Response<List<GeoRadiusResponse>> georadiusByMemberReadonly(String var1, String var2, double var3, GeoUnit var5, GeoRadiusParam var6);

   Response<Long> georadiusStore(String var1, double var2, double var4, double var6, GeoUnit var8, GeoRadiusParam var9, GeoRadiusStoreParam var10);

   Response<Long> georadiusByMemberStore(String var1, String var2, double var3, GeoUnit var5, GeoRadiusParam var6, GeoRadiusStoreParam var7);

   Response<List<GeoRadiusResponse>> geosearch(String var1, String var2, double var3, GeoUnit var5);

   Response<List<GeoRadiusResponse>> geosearch(String var1, GeoCoordinate var2, double var3, GeoUnit var5);

   Response<List<GeoRadiusResponse>> geosearch(String var1, String var2, double var3, double var5, GeoUnit var7);

   Response<List<GeoRadiusResponse>> geosearch(String var1, GeoCoordinate var2, double var3, double var5, GeoUnit var7);

   Response<List<GeoRadiusResponse>> geosearch(String var1, GeoSearchParam var2);

   Response<Long> geosearchStore(String var1, String var2, String var3, double var4, GeoUnit var6);

   Response<Long> geosearchStore(String var1, String var2, GeoCoordinate var3, double var4, GeoUnit var6);

   Response<Long> geosearchStore(String var1, String var2, String var3, double var4, double var6, GeoUnit var8);

   Response<Long> geosearchStore(String var1, String var2, GeoCoordinate var3, double var4, double var6, GeoUnit var8);

   Response<Long> geosearchStore(String var1, String var2, GeoSearchParam var3);

   Response<Long> geosearchStoreStoreDist(String var1, String var2, GeoSearchParam var3);
}
