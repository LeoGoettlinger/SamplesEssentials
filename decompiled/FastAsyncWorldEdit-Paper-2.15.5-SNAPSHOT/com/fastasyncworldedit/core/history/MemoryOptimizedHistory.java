package com.fastasyncworldedit.core.history;

import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.history.changeset.FaweStreamChangeSet;
import com.fastasyncworldedit.core.internal.io.FastByteArrayOutputStream;
import com.fastasyncworldedit.core.internal.io.FastByteArraysInputStream;
import com.fastasyncworldedit.core.internal.io.FaweInputStream;
import com.fastasyncworldedit.core.internal.io.FaweOutputStream;
import com.fastasyncworldedit.core.util.MainUtil;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.worldedit.world.World;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MemoryOptimizedHistory extends FaweStreamChangeSet {
   private byte[][] ids;
   private FastByteArrayOutputStream idsStream;
   private FaweOutputStream idsStreamZip;
   private byte[][] biomes;
   private FastByteArrayOutputStream biomeStream;
   private FaweOutputStream biomeStreamZip;
   private byte[][] entC;
   private FastByteArrayOutputStream entCStream;
   private NBTOutputStream entCStreamZip;
   private byte[][] entR;
   private FastByteArrayOutputStream entRStream;
   private NBTOutputStream entRStreamZip;
   private byte[][] tileC;
   private FastByteArrayOutputStream tileCStream;
   private NBTOutputStream tileCStreamZip;
   private byte[][] tileR;
   private FastByteArrayOutputStream tileRStream;
   private NBTOutputStream tileRStreamZip;

   public MemoryOptimizedHistory(World world) {
      super(world);
   }

   @Override
   public void flush() {
      super.flush();
      synchronized (this) {
         try {
            if (this.idsStream != null) {
               this.idsStreamZip.flush();
            }

            if (this.biomeStream != null) {
               this.biomeStreamZip.flush();
            }

            if (this.entCStream != null) {
               this.entCStreamZip.flush();
            }

            if (this.entRStream != null) {
               this.entRStreamZip.flush();
            }

            if (this.tileCStream != null) {
               this.tileCStreamZip.flush();
            }

            if (this.tileRStream != null) {
               this.tileRStreamZip.flush();
            }
         } catch (IOException var4) {
            var4.printStackTrace();
         }
      }
   }

   @Override
   public void close() throws IOException {
      super.close();
      synchronized (this) {
         try {
            if (this.idsStream != null) {
               this.idsStreamZip.close();
               this.ids = this.idsStream.toByteArrays();
               this.idsStream = null;
               this.idsStreamZip = null;
            }

            if (this.biomeStream != null) {
               this.biomeStreamZip.close();
               this.biomes = this.biomeStream.toByteArrays();
               this.biomeStream = null;
               this.biomeStreamZip = null;
            }

            if (this.entCStream != null) {
               this.entCStreamZip.close();
               this.entC = this.entCStream.toByteArrays();
               this.entCStream = null;
               this.entCStreamZip = null;
            }

            if (this.entRStream != null) {
               this.entRStreamZip.close();
               this.entR = this.entRStream.toByteArrays();
               this.entRStream = null;
               this.entRStreamZip = null;
            }

            if (this.tileCStream != null) {
               this.tileCStreamZip.close();
               this.tileC = this.tileCStream.toByteArrays();
               this.tileCStream = null;
               this.tileCStreamZip = null;
            }

            if (this.tileRStream != null) {
               this.tileRStreamZip.close();
               this.tileR = this.tileRStream.toByteArrays();
               this.tileRStream = null;
               this.tileRStreamZip = null;
            }
         } catch (IOException var4) {
            var4.printStackTrace();
         }
      }
   }

   @Override
   public int getCompressedSize() {
      if (this.ids == null) {
         return 0;
      } else {
         int count = 0;

         for (byte[] array : this.ids) {
            count += 4 + array.length;
         }

         return count;
      }
   }

   @Override
   public long getSizeInMemory() {
      return 92 + this.getCompressedSize();
   }

   @Override
   public FaweOutputStream getBlockOS(int x, int y, int z) throws IOException {
      if (this.idsStreamZip != null) {
         return this.idsStreamZip;
      } else {
         synchronized (this) {
            this.setOrigin(x, z);
            this.idsStream = new FastByteArrayOutputStream(Settings.settings().HISTORY.BUFFER_SIZE);
            this.idsStreamZip = this.getCompressedOS(this.idsStream);
            this.writeHeader(this.idsStreamZip, x, y, z);
            return this.idsStreamZip;
         }
      }
   }

   @Override
   public FaweInputStream getBiomeIS() throws IOException {
      return this.biomes == null ? null : MainUtil.getCompressedIS(new FastByteArraysInputStream(this.biomes));
   }

   @Override
   public FaweOutputStream getBiomeOS() throws IOException {
      if (this.biomeStreamZip != null) {
         return this.biomeStreamZip;
      } else {
         synchronized (this) {
            this.biomeStream = new FastByteArrayOutputStream(Settings.settings().HISTORY.BUFFER_SIZE);
            this.biomeStreamZip = this.getCompressedOS(this.biomeStream);
            return this.biomeStreamZip;
         }
      }
   }

   @Override
   public FaweInputStream getBlockIS() throws IOException {
      if (this.ids == null) {
         return null;
      } else {
         FaweInputStream result = MainUtil.getCompressedIS(new FastByteArraysInputStream(this.ids));
         this.readHeader(result);
         return result;
      }
   }

   @Override
   public NBTOutputStream getEntityCreateOS() throws IOException {
      if (this.entCStreamZip != null) {
         return this.entCStreamZip;
      } else {
         this.entCStream = new FastByteArrayOutputStream(Settings.settings().HISTORY.BUFFER_SIZE);
         return this.entCStreamZip = new NBTOutputStream((DataOutputStream)this.getCompressedOS(this.entCStream));
      }
   }

   @Override
   public NBTOutputStream getEntityRemoveOS() throws IOException {
      if (this.entRStreamZip != null) {
         return this.entRStreamZip;
      } else {
         this.entRStream = new FastByteArrayOutputStream(Settings.settings().HISTORY.BUFFER_SIZE);
         return this.entRStreamZip = new NBTOutputStream((DataOutputStream)this.getCompressedOS(this.entRStream));
      }
   }

   @Override
   public NBTOutputStream getTileCreateOS() throws IOException {
      if (this.tileCStreamZip != null) {
         return this.tileCStreamZip;
      } else {
         this.tileCStream = new FastByteArrayOutputStream(Settings.settings().HISTORY.BUFFER_SIZE);
         return this.tileCStreamZip = new NBTOutputStream((DataOutputStream)this.getCompressedOS(this.tileCStream));
      }
   }

   @Override
   public NBTOutputStream getTileRemoveOS() throws IOException {
      if (this.tileRStreamZip != null) {
         return this.tileRStreamZip;
      } else {
         this.tileRStream = new FastByteArrayOutputStream(Settings.settings().HISTORY.BUFFER_SIZE);
         return this.tileRStreamZip = new NBTOutputStream((DataOutputStream)this.getCompressedOS(this.tileRStream));
      }
   }

   @Override
   public NBTInputStream getEntityCreateIS() throws IOException {
      return this.entC == null ? null : new NBTInputStream((DataInputStream)MainUtil.getCompressedIS(new FastByteArraysInputStream(this.entC)));
   }

   @Override
   public NBTInputStream getEntityRemoveIS() throws IOException {
      return this.entR == null ? null : new NBTInputStream((DataInputStream)MainUtil.getCompressedIS(new FastByteArraysInputStream(this.entR)));
   }

   @Override
   public NBTInputStream getTileCreateIS() throws IOException {
      return this.tileC == null ? null : new NBTInputStream((DataInputStream)MainUtil.getCompressedIS(new FastByteArraysInputStream(this.tileC)));
   }

   @Override
   public NBTInputStream getTileRemoveIS() throws IOException {
      return this.tileR == null ? null : new NBTInputStream((DataInputStream)MainUtil.getCompressedIS(new FastByteArraysInputStream(this.tileR)));
   }

   @Override
   public boolean isRecordingChanges() {
      return false;
   }

   @Override
   public void setRecordChanges(boolean recordChanges) {
   }
}
