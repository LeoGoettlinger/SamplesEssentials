package com.skyblockexp.ezrtp.storage;

import java.util.List;

public interface HotspotStorage {
   void saveHotspot(String var1, String var2, double var3, double var5, double var7);

   List<HotspotStorage.HotspotRecord> loadAll();

   void close();

   public record HotspotRecord(String world, String biome, double x, double y, double z, long timestamp) {
   }
}
