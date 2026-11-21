package com.sk89q.worldedit.world.storage;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.util.io.ForwardSeekableInputStream;
import com.sk89q.worldedit.world.DataException;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class McRegionReader implements Closeable {
   protected static final int VERSION_GZIP = 1;
   protected static final int VERSION_DEFLATE = 2;
   protected static final int SECTOR_BYTES = 4096;
   protected static final int SECTOR_INTS = 1024;
   public static final int CHUNK_HEADER_SIZE = 5;
   protected ForwardSeekableInputStream stream;
   protected DataInputStream dataStream;
   protected int[] offsets;

   public McRegionReader(InputStream stream) throws IOException {
      this.stream = new ForwardSeekableInputStream(stream);
      this.dataStream = new DataInputStream(this.stream);
      this.readHeader();
   }

   private void readHeader() throws IOException {
      this.offsets = new int[1024];

      for (int i = 0; i < 1024; i++) {
         int offset = this.dataStream.readInt();
         this.offsets[i] = offset;
      }
   }

   public synchronized InputStream getChunkInputStream(BlockVector2 position) throws IOException, DataException {
      int x = position.x() & 31;
      int z = position.z() & 31;
      int offset = this.getOffset(x, z);
      if (offset == 0) {
         throw new DataException("The chunk at " + position + " is not generated");
      } else {
         long sectorNumber = offset >> 8;
         int numSectors = offset & 0xFF;
         this.stream.seek(sectorNumber * 4096L);
         int length = this.dataStream.readInt();
         if (length > 4096 * numSectors) {
            throw new DataException("MCRegion chunk at " + x + "," + z + " has an invalid length of " + length);
         } else {
            byte version = this.dataStream.readByte();
            byte[] data = new byte[length - 1];

            try {
               this.dataStream.readFully(data);
            } catch (EOFException var12) {
               throw new DataException("MCRegion file does not contain " + x + "," + z + " in full");
            }

            if (version == 1) {
               return new GZIPInputStream(new ByteArrayInputStream(data));
            } else if (version == 2) {
               return new InflaterInputStream(new ByteArrayInputStream(data));
            } else {
               throw new DataException("MCRegion chunk at " + x + "," + z + " has an unsupported version of " + version);
            }
         }
      }
   }

   private int getOffset(int x, int z) {
      return this.offsets[x + z * 32];
   }

   public boolean hasChunk(int x, int z) {
      return this.getOffset(x, z) != 0;
   }

   @Override
   public void close() throws IOException {
      this.stream.close();
   }
}
