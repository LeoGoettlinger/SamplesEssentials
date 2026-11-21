package com.sk89q.worldedit.world.storage;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public abstract class LegacyChunkStore extends ChunkStore {
   public static String getFilename(BlockVector2 position, String separator) {
      int x = position.x();
      int z = position.z();
      String folder1 = Integer.toString(divisorMod(x, 64), 36);
      String folder2 = Integer.toString(divisorMod(z, 64), 36);
      String filename = "c." + Integer.toString(x, 36) + "." + Integer.toString(z, 36) + ".dat";
      return folder1 + separator + folder2 + separator + filename;
   }

   public static String getFilename(BlockVector2 position) {
      return getFilename(position, File.separator);
   }

   @Override
   public CompoundTag getChunkTag(BlockVector2 position, World world) throws DataException, IOException {
      int x = position.x();
      int z = position.z();
      String folder1 = Integer.toString(divisorMod(x, 64), 36);
      String folder2 = Integer.toString(divisorMod(z, 64), 36);
      String filename = "c." + Integer.toString(x, 36) + "." + Integer.toString(z, 36) + ".dat";
      return ChunkStoreHelper.readCompoundTag(() -> new GZIPInputStream(this.getInputStream(folder1, folder2, filename)));
   }

   private static int divisorMod(int a, int n) {
      return (int)(a - n * Math.floor(Math.floor(a) / n));
   }

   protected abstract InputStream getInputStream(String f1, String f2, String name) throws IOException, DataException;
}
