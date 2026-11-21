package com.earth2me.essentials.userstorage;

import com.earth2me.essentials.utils.StringUtil;
import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import net.ess3.api.IEssentials;

public class ModernUUIDCache {
   private final IEssentials ess;
   private final ConcurrentHashMap<String, UUID> nameToUuidMap = new ConcurrentHashMap<>();
   private final Set<UUID> uuidCache = ConcurrentHashMap.newKeySet();
   private final ScheduledExecutorService writeExecutor = Executors.newSingleThreadScheduledExecutor();
   private final AtomicBoolean pendingNameWrite = new AtomicBoolean(false);
   private final AtomicBoolean pendingUuidWrite = new AtomicBoolean(false);
   private final File nameToUuidFile;
   private final File uuidCacheFile;

   public ModernUUIDCache(final IEssentials ess) {
      this.ess = ess;
      this.nameToUuidFile = new File(ess.getDataFolder(), "usermap.bin");
      this.uuidCacheFile = new File(ess.getDataFolder(), "uuids.bin");
      this.loadCache();
      this.writeExecutor.scheduleWithFixedDelay(() -> {
         if (this.pendingNameWrite.compareAndSet(true, false)) {
            this.saveNameToUuidCache();
         }

         if (this.pendingUuidWrite.compareAndSet(true, false)) {
            this.saveUuidCache();
         }
      }, 5L, 5L, TimeUnit.SECONDS);
   }

   protected UUID getCachedUUID(final String name) {
      return this.nameToUuidMap.get(this.getSanitizedName(name));
   }

   protected Set<UUID> getCachedUUIDs() {
      return Collections.unmodifiableSet(this.uuidCache);
   }

   protected Map<String, UUID> getNameCache() {
      return Collections.unmodifiableMap(this.nameToUuidMap);
   }

   protected int getCacheSize() {
      return this.uuidCache.size();
   }

   protected String getSanitizedName(final String name) {
      return this.ess.getSettings().isSafeUsermap() ? StringUtil.safeString(name) : name;
   }

   protected void updateCache(final UUID uuid, final String name) {
      if (this.uuidCache.add(uuid)) {
         this.pendingUuidWrite.set(true);
      }

      if (name != null) {
         String sanitizedName = this.getSanitizedName(name);
         UUID replacedUuid = this.nameToUuidMap.put(sanitizedName, uuid);
         if (!uuid.equals(replacedUuid)) {
            if (this.ess.getSettings().isDebug()) {
               this.ess.getLogger().log(Level.WARNING, "Replaced UUID during cache update for " + sanitizedName + ": " + replacedUuid + " -> " + uuid);
            }

            this.pendingNameWrite.set(true);
         }
      }
   }

   protected void removeCache(final UUID uuid) {
      if (uuid != null) {
         if (this.uuidCache.remove(uuid)) {
            this.pendingUuidWrite.set(true);
         }

         Set<String> toRemove = new HashSet<>();

         for (Entry<String, UUID> entry : this.nameToUuidMap.entrySet()) {
            if (uuid.equals(entry.getValue())) {
               toRemove.add(entry.getKey());
            }
         }

         for (String name : toRemove) {
            this.nameToUuidMap.remove(name);
         }

         if (!toRemove.isEmpty()) {
            this.pendingNameWrite.set(true);
         }
      }
   }

   private void loadCache() {
      boolean debug = this.ess.getSettings().isDebug();

      try {
         if (!this.nameToUuidFile.exists()) {
            if (!this.nameToUuidFile.createNewFile()) {
               throw new RuntimeException("Error while creating usermap.bin");
            }

            return;
         }

         if (debug) {
            this.ess.getLogger().log(Level.INFO, "Loading Name->UUID cache from disk...");
         }

         this.nameToUuidMap.clear();
         DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(this.nameToUuidFile)));

         try {
            while (dis.available() > 0) {
               String username = dis.readUTF();
               UUID uuid = new UUID(dis.readLong(), dis.readLong());
               UUID previous = this.nameToUuidMap.put(username, uuid);
               if (previous != null && debug) {
                  this.ess.getLogger().log(Level.WARNING, "Replaced UUID during cache load for " + username + ": " + previous + " -> " + uuid);
               }
            }
         } catch (Throwable var10) {
            try {
               dis.close();
            } catch (Throwable var7) {
               var10.addSuppressed(var7);
            }

            throw var10;
         }

         dis.close();
      } catch (IOException var11) {
         this.ess.getLogger().log(Level.SEVERE, "Error while loading Name->UUID cache", (Throwable)var11);
      }

      try {
         if (!this.uuidCacheFile.exists()) {
            if (!this.uuidCacheFile.createNewFile()) {
               throw new RuntimeException("Error while creating uuids.bin");
            }

            return;
         }

         if (debug) {
            this.ess.getLogger().log(Level.INFO, "Loading UUID cache from disk...");
         }

         this.uuidCache.clear();
         DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(this.uuidCacheFile)));

         try {
            while (dis.available() > 0) {
               UUID uuid = new UUID(dis.readLong(), dis.readLong());
               if (this.uuidCache.contains(uuid) && debug) {
                  this.ess.getLogger().log(Level.WARNING, "UUID " + uuid + " duplicated in cache");
               }

               this.uuidCache.add(uuid);
            }
         } catch (Throwable var8) {
            try {
               dis.close();
            } catch (Throwable var6) {
               var8.addSuppressed(var6);
            }

            throw var8;
         }

         dis.close();
      } catch (IOException var9) {
         this.ess.getLogger().log(Level.SEVERE, "Error while loading UUID cache", (Throwable)var9);
      }
   }

   private void saveUuidCache() {
      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "Saving UUID cache to disk...");
      }

      try {
         File tmpMap = File.createTempFile("uuids", ".tmp.bin", this.ess.getDataFolder());
         writeUuidCache(tmpMap, this.uuidCache);
         Files.move(tmpMap, this.uuidCacheFile);
      } catch (IOException var2) {
         this.ess.getLogger().log(Level.SEVERE, "Error while saving UUID cache", (Throwable)var2);
      }
   }

   private void saveNameToUuidCache() {
      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "Saving Name->UUID cache to disk...");
      }

      try {
         File tmpMap = File.createTempFile("usermap", ".tmp.bin", this.ess.getDataFolder());
         writeNameUuidMap(tmpMap, this.nameToUuidMap);
         Files.move(tmpMap, this.nameToUuidFile);
      } catch (IOException var2) {
         this.ess.getLogger().log(Level.SEVERE, "Error while saving Name->UUID cache", (Throwable)var2);
      }
   }

   protected void blockingSave() {
      this.saveUuidCache();
      this.saveNameToUuidCache();
   }

   public static void writeUuidCache(final File file, Set<UUID> uuids) throws IOException {
      DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

      try {
         for (UUID uuid : uuids) {
            dos.writeLong(uuid.getMostSignificantBits());
            dos.writeLong(uuid.getLeastSignificantBits());
         }
      } catch (Throwable var6) {
         try {
            dos.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      dos.close();
   }

   public static void writeNameUuidMap(final File file, final Map<String, UUID> nameToUuidMap) throws IOException {
      DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

      try {
         for (Entry<String, UUID> entry : nameToUuidMap.entrySet()) {
            dos.writeUTF(entry.getKey());
            UUID uuid = entry.getValue();
            dos.writeLong(uuid.getMostSignificantBits());
            dos.writeLong(uuid.getLeastSignificantBits());
         }
      } catch (Throwable var7) {
         try {
            dos.close();
         } catch (Throwable var6) {
            var7.addSuppressed(var6);
         }

         throw var7;
      }

      dos.close();
   }

   public void shutdown() {
      this.writeExecutor.shutdownNow();
      this.blockingSave();
   }
}
