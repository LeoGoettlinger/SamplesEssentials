package de.oliver.fancynpcs.skins.uuidcache;

import com.google.gson.Gson;
import de.oliver.fancyanalytics.logger.properties.ThrowableProperty;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.jdb.JDB;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UUIDFileCache implements UUIDCache {
   private static final File usercacheFile = new File("usercache.json");
   private static final Gson gson = new Gson();
   private final JDB storage = new JDB("plugins/FancyNpcs/.data");
   private final Map<String, UUIDCacheData> cache = new ConcurrentHashMap<>();
   private final Map<String, UUID> minecraftUsercache = new ConcurrentHashMap<>();

   public UUIDFileCache() {
      this.loadMinecraftUsercache();
      this.load();
   }

   private void load() {
      UUIDCacheData[] uuids;
      try {
         uuids = this.storage.get("uuids", UUIDCacheData[].class);
      } catch (IOException var6) {
         FancyNpcs.getInstance().getFancyLogger().error("Failed to load UUID cache", ThrowableProperty.of(var6));
         return;
      }

      if (uuids != null) {
         this.cache.clear();

         for (UUIDCacheData data : uuids) {
            if (!data.isExpired()) {
               this.cache.put(data.username(), data);
            }
         }

         FancyNpcs.getInstance().getFancyLogger().debug("Loaded " + this.cache.size() + " UUIDs from file cache");
      }
   }

   private void loadMinecraftUsercache() {
      if (FancyNpcs.USE_MINECRAFT_USERCACHE_FEATURE_FLAG.isEnabled()) {
         if (!usercacheFile.exists()) {
            FancyNpcs.getInstance().getFancyLogger().debug("Minecraft usercache file does not exist, skipping load.");
         } else {
            MinecraftUsercacheData[] data;
            try {
               data = gson.fromJson(new FileReader(usercacheFile), MinecraftUsercacheData[].class);
            } catch (FileNotFoundException var6) {
               FancyNpcs.getInstance().getFancyLogger().error("Failed to load Minecraft usercache file");
               return;
            }

            if (data != null) {
               for (MinecraftUsercacheData d : data) {
                  this.minecraftUsercache.put(d.name(), UUID.fromString(d.uuid()));
               }

               FancyNpcs.getInstance().getFancyLogger().debug("Loaded " + this.minecraftUsercache.size() + " UUIDs from Minecraft usercache");
            }
         }
      }
   }

   private void save() {
      UUIDCacheData[] uuids = this.cache.values().toArray(new UUIDCacheData[0]);

      try {
         this.storage.set("uuids", uuids);
      } catch (IOException var3) {
         throw new RuntimeException("Failed to save UUID cache", var3);
      }
   }

   @Override
   public UUID getUUID(String username) {
      if (this.minecraftUsercache.containsKey(username)) {
         FancyNpcs.getInstance()
            .getFancyLogger()
            .debug("Found UUID for " + username + ": " + this.minecraftUsercache.get(username) + " in Minecraft usercache");
         return this.minecraftUsercache.get(username);
      } else {
         if (this.cache.isEmpty()) {
            this.load();
         }

         if (!this.cache.containsKey(username)) {
            return null;
         } else {
            UUIDCacheData data = this.cache.get(username);
            if (data.isExpired()) {
               this.cache.remove(username);
               this.save();
               return null;
            } else {
               FancyNpcs.getInstance().getFancyLogger().debug("Found UUID for " + username + ": " + data.uuid() + " in file cache");
               return data.uuid();
            }
         }
      }
   }

   @Override
   public void cacheUUID(String username, UUID uuid) {
      UUIDCacheData data = new UUIDCacheData(username, uuid, System.currentTimeMillis(), 2592000000L);
      this.cache.put(username, data);
      this.save();
      FancyNpcs.getInstance().getFancyLogger().debug("Cached UUID for " + username + ": " + uuid);
   }

   @Override
   public void clearCache() {
      this.cache.clear();
      this.storage.delete("uuids");
   }
}
