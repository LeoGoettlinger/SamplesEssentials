package com.fastasyncworldedit.core.internal.io;

import java.io.InputStream;

public class FastByteArraysInputStream extends InputStream {
   private final byte[][] buffers;
   private final int length;
   private byte[] current;
   private int layer;
   private int localIndex;
   private int globalIndex;
   private int curLen;

   public FastByteArraysInputStream(byte[][] buffers) {
      this.buffers = buffers;
      int size = 0;

      for (byte[] bytes : buffers) {
         size += bytes.length;
      }

      this.length = size;
      this.current = buffers.length == 0 ? new byte[this.layer++] : buffers[this.layer++];
      this.curLen = this.current.length;
   }

   @Override
   public boolean markSupported() {
      return false;
   }

   @Override
   public void reset() {
   }

   @Override
   public void close() {
   }

   @Override
   public void mark(int dummy) {
   }

   @Override
   public int available() {
      return this.length - this.globalIndex;
   }

   @Override
   public long skip(long n) {
      if (n <= this.length - this.globalIndex) {
         this.globalIndex += (int)n;
         this.localIndex += (int)n;
         this.ensureBuffer();
         return n;
      } else {
         n = this.length - this.globalIndex;
         this.layer = this.buffers.length - 1;
         this.current = this.buffers[this.layer];
         this.curLen = this.current.length;
         this.localIndex = this.current.length;
         this.globalIndex = this.length;
         return n;
      }
   }

   @Override
   public int read() {
      if (this.curLen != this.localIndex) {
         this.globalIndex++;
         return this.current[this.localIndex++] & 0xFF;
      } else if (this.length == this.globalIndex) {
         return -1;
      } else {
         this.localIndex = 0;
         this.current = this.buffers[this.layer++];
         this.curLen = this.current.length;
         this.globalIndex++;
         return this.current[this.localIndex++] & 0xFF;
      }
   }

   @Override
   public int read(byte[] b, int offset, int length) {
      if (this.length <= this.globalIndex) {
         return length == 0 ? 0 : -1;
      } else {
         int n = Math.min(length, this.length - this.globalIndex);
         int read = 0;
         int amount = Math.min(this.curLen - this.localIndex, n - read);
         System.arraycopy(this.current, this.localIndex, b, offset + read, amount);
         read += amount;
         this.localIndex += amount;
         this.globalIndex += amount;
         this.ensureBuffer();
         return read;
      }
   }

   public void ensureBuffer() {
      while (this.localIndex >= this.curLen && this.layer < this.buffers.length) {
         this.localIndex = this.localIndex - this.curLen;
         this.current = this.buffers[this.layer++];
         this.curLen = this.current.length;
      }
   }

   public long length() {
      return this.length;
   }
}
