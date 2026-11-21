package com.sk89q.worldedit.world.storage;

import com.sk89q.worldedit.world.DataException;
import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipException;
import javax.annotation.Nullable;

public class TrueZipMcRegionChunkStore extends McRegionChunkStore {
   protected File zipFile;
   protected ZipFile zip;
   protected String folder;

   public TrueZipMcRegionChunkStore(File zipFile, String folder) throws IOException, ZipException {
      this.zipFile = zipFile;
      this.folder = folder;
      this.zip = new ZipFile(zipFile);
   }

   public TrueZipMcRegionChunkStore(File zipFile) throws IOException, ZipException {
      this.zipFile = zipFile;
      this.zip = new ZipFile(zipFile);
   }

   @Override
   protected InputStream getInputStream(String name, String worldName, @Nullable String folderOverride) throws IOException, DataException {
      if (this.folder != null) {
         if (!this.folder.isEmpty()) {
            name = this.folder + "/" + name;
            if (folderOverride != null) {
               name = name.replace("region", folderOverride);
            }
         }
      } else {
         Pattern pattern = Pattern.compile(".*\\.mc[ra]$");
         Pattern worldPattern = Pattern.compile(worldName + "[\\\\/].*");
         Enumeration<? extends ZipEntry> e = this.zip.entries();

         while (e.hasMoreElements()) {
            ZipEntry testEntry = e.nextElement();
            String entryName = testEntry.getName();
            if (worldPattern.matcher(entryName).matches() && pattern.matcher(entryName).matches()) {
               int endIndex = entryName.lastIndexOf(47);
               if (endIndex < 0) {
                  endIndex = entryName.lastIndexOf(92);
               }

               this.folder = entryName.substring(0, endIndex);
               if (folderOverride != null && this.folder.endsWith(folderOverride)) {
                  name = this.folder + "/" + name;
                  break;
               }

               if (!this.folder.endsWith("poi") && !this.folder.endsWith("entities")) {
                  name = this.folder + "/" + name;
                  break;
               }
            }
         }

         if (this.folder == null) {
            throw new MissingWorldException("Target world is not present in ZIP.", worldName);
         }
      }

      ZipEntry entry = this.getEntry(name);
      if (entry == null) {
         if (!name.endsWith(".mca")) {
            throw new MissingChunkException();
         }

         entry = this.getEntry(name.replace(".mca", ".mcr"));
         if (entry == null) {
            throw new MissingChunkException();
         }
      }

      try {
         return this.zip.getInputStream(entry);
      } catch (ZipException var10) {
         throw new IOException("Failed to read " + name + " in ZIP");
      }
   }

   private ZipEntry getEntry(String file) {
      ZipEntry entry = this.zip.getEntry(file);
      return entry != null ? entry : this.zip.getEntry(file.replace("/", "\\"));
   }

   @Override
   public void close() throws IOException {
      this.zip.close();
   }

   @Override
   public boolean isValid() {
      Enumeration<? extends ZipEntry> e = this.zip.entries();

      while (e.hasMoreElements()) {
         ZipEntry testEntry = e.nextElement();
         if (testEntry.getName().matches(".*\\.mcr$") || testEntry.getName().matches(".*\\.mca$")) {
            return true;
         }
      }

      return false;
   }
}
