package com.sk89q.worldedit.world.snapshot;

import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.storage.ChunkStore;
import com.sk89q.worldedit.world.storage.FileLegacyChunkStore;
import com.sk89q.worldedit.world.storage.FileMcRegionChunkStore;
import com.sk89q.worldedit.world.storage.TrueZipLegacyChunkStore;
import com.sk89q.worldedit.world.storage.TrueZipMcRegionChunkStore;
import com.sk89q.worldedit.world.storage.ZippedLegacyChunkStore;
import com.sk89q.worldedit.world.storage.ZippedMcRegionChunkStore;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.zip.ZipFile;
import org.apache.logging.log4j.Logger;

public class Snapshot implements Comparable<Snapshot> {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   protected File file;
   protected String name;
   protected ZonedDateTime date;

   public Snapshot(SnapshotRepository repo, String snapshot) {
      this.file = new File(repo.getDirectory(), snapshot);
      this.name = snapshot;
   }

   public ChunkStore getChunkStore() throws IOException, DataException {
      ChunkStore chunkStore = this.internalGetChunkStore();
      LOGGER.info("WorldEdit: Using " + chunkStore.getClass().getCanonicalName() + " for loading snapshot '" + this.file.getAbsolutePath() + "'");
      return chunkStore;
   }

   private ChunkStore internalGetChunkStore() throws IOException, DataException {
      String lowerCaseFileName = this.file.getName().toLowerCase(Locale.ROOT);
      if (lowerCaseFileName.endsWith(".zip")) {
         try {
            ChunkStore chunkStore = new TrueZipMcRegionChunkStore(this.file);
            return (ChunkStore)(!chunkStore.isValid() ? new TrueZipLegacyChunkStore(this.file) : chunkStore);
         } catch (NoClassDefFoundError var5) {
            ChunkStore chunkStorex = new ZippedMcRegionChunkStore(this.file);
            return (ChunkStore)(!chunkStorex.isValid() ? new ZippedLegacyChunkStore(this.file) : chunkStorex);
         }
      } else if (!lowerCaseFileName.endsWith(".tar.bz2") && !lowerCaseFileName.endsWith(".tar.gz") && !lowerCaseFileName.endsWith(".tar")) {
         ChunkStore chunkStore = new FileMcRegionChunkStore(this.file);
         return (ChunkStore)(!chunkStore.isValid() ? new FileLegacyChunkStore(this.file) : chunkStore);
      } else {
         try {
            ChunkStore chunkStore = new TrueZipMcRegionChunkStore(this.file);
            return (ChunkStore)(!chunkStore.isValid() ? new TrueZipLegacyChunkStore(this.file) : chunkStore);
         } catch (NoClassDefFoundError var4) {
            throw new DataException("TrueZIP is required for .tar support");
         }
      }
   }

   public boolean containsWorld(String worldname) {
      try {
         String lowerCaseFileName = this.file.getName().toLowerCase(Locale.ROOT);
         if (lowerCaseFileName.endsWith(".zip")) {
            boolean var4;
            try (ZipFile entry = new ZipFile(this.file)) {
               var4 = entry.getEntry(worldname) != null || entry.getEntry(worldname + "/level.dat") != null;
            }

            return var4;
         }

         if (!lowerCaseFileName.endsWith(".tar.bz2") && !lowerCaseFileName.endsWith(".tar.gz") && !lowerCaseFileName.endsWith(".tar")) {
            return this.file.getName().equalsIgnoreCase(worldname);
         }

         try {
            de.schlichtherle.util.zip.ZipFile entry = new de.schlichtherle.util.zip.ZipFile(this.file);
            return entry.getEntry(worldname) != null;
         } catch (NoClassDefFoundError var7) {
            throw new DataException("TrueZIP is required for .tar support");
         }
      } catch (IOException var9) {
         LOGGER.info("Could not load snapshot: " + this.file.getPath());
      } catch (DataException var10) {
      }

      return false;
   }

   public String getName() {
      return this.name;
   }

   public File getFile() {
      return this.file;
   }

   public ZonedDateTime getDate() {
      return this.date;
   }

   public void setDate(ZonedDateTime date) {
      this.date = date;
   }

   public int compareTo(Snapshot o) {
      if (o.date != null && this.date != null) {
         return this.date.compareTo(o.date);
      } else {
         int ourIndex = this.name.indexOf(47);
         int theirIndex = o.name.indexOf(47);
         return this.name.substring(Math.min(ourIndex, 0)).compareTo(o.name.substring(Math.min(theirIndex, 0)));
      }
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof Snapshot && this.file.equals(((Snapshot)o).file);
   }

   @Override
   public int hashCode() {
      return this.file.hashCode();
   }
}
