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

public interface GeoPipelineBinaryCommands {
   Response<Long> geoadd(byte[] var1, double var2, double var4, byte[] var6);

   Response<Long> geoadd(byte[] var1, Map<byte[], GeoCoordinate> var2);

   Response<Long> geoadd(byte[] var1, GeoAddParams var2, Map<byte[], GeoCoordinate> var3);

   Response<Double> geodist(byte[] var1, byte[] var2, byte[] var3);

   Response<Double> geodist(byte[] var1, byte[] var2, byte[] var3, GeoUnit var4);

   Response<List<byte[]>> geohash(byte[] var1, byte[]... var2);

   Response<List<GeoCoordinate>> geopos(byte[] var1, byte[]... var2);

   Response<List<GeoRadiusResponse>> georadius(byte[] var1, double var2, double var4, double var6, GeoUnit var8);

   Response<List<GeoRadiusResponse>> georadiusReadonly(byte[] var1, double var2, double var4, double var6, GeoUnit var8);

   Response<List<GeoRadiusResponse>> georadius(byte[] var1, double var2, double var4, double var6, GeoUnit var8, GeoRadiusParam var9);

   Response<List<GeoRadiusResponse>> georadiusReadonly(byte[] var1, double var2, double var4, double var6, GeoUnit var8, GeoRadiusParam var9);

   Response<List<GeoRadiusResponse>> georadiusByMember(byte[] var1, byte[] var2, double var3, GeoUnit var5);

   Response<List<GeoRadiusResponse>> georadiusByMemberReadonly(byte[] var1, byte[] var2, double var3, GeoUnit var5);

   Response<List<GeoRadiusResponse>> georadiusByMember(byte[] var1, byte[] var2, double var3, GeoUnit var5, GeoRadiusParam var6);

   Response<List<GeoRadiusResponse>> georadiusByMemberReadonly(byte[] var1, byte[] var2, double var3, GeoUnit var5, GeoRadiusParam var6);

   Response<Long> georadiusStore(byte[] var1, double var2, double var4, double var6, GeoUnit var8, GeoRadiusParam var9, GeoRadiusStoreParam var10);

   Response<Long> georadiusByMemberStore(byte[] var1, byte[] var2, double var3, GeoUnit var5, GeoRadiusParam var6, GeoRadiusStoreParam var7);

   Response<List<GeoRadiusResponse>> geosearch(byte[] var1, byte[] var2, double var3, GeoUnit var5);

   Response<List<GeoRadiusResponse>> geosearch(byte[] var1, GeoCoordinate var2, double var3, GeoUnit var5);

   Response<List<GeoRadiusResponse>> geosearch(byte[] var1, byte[] var2, double var3, double var5, GeoUnit var7);

   Response<List<GeoRadiusResponse>> geosearch(byte[] var1, GeoCoordinate var2, double var3, double var5, GeoUnit var7);

   Response<List<GeoRadiusResponse>> geosearch(byte[] var1, GeoSearchParam var2);

   Response<Long> geosearchStore(byte[] var1, byte[] var2, byte[] var3, double var4, GeoUnit var6);

   Response<Long> geosearchStore(byte[] var1, byte[] var2, GeoCoordinate var3, double var4, GeoUnit var6);

   Response<Long> geosearchStore(byte[] var1, byte[] var2, byte[] var3, double var4, double var6, GeoUnit var8);

   Response<Long> geosearchStore(byte[] var1, byte[] var2, GeoCoordinate var3, double var4, double var6, GeoUnit var8);

   Response<Long> geosearchStore(byte[] var1, byte[] var2, GeoSearchParam var3);

   Response<Long> geosearchStoreStoreDist(byte[] var1, byte[] var2, GeoSearchParam var3);
}
