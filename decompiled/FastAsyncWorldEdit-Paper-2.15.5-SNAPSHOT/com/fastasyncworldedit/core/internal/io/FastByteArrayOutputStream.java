package com.fastasyncworldedit.core.internal.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Arrays;

public class FastByteArrayOutputStream extends OutputStream {
   private static final int DEFAULT_BLOCK_SIZE = 8192;
   private final ArrayDeque<byte[]> buffers = new ArrayDeque<>();
   private byte[] buffer;
   private final int blockSize;
   private int index;
   private int size;

   public FastByteArrayOutputStream() {
      this(8192);
   }

   public FastByteArrayOutputStream(int aSize) {
      this.blockSize = aSize;
      this.buffer = new byte[this.blockSize];
   }

   public FastByteArrayOutputStream(byte[] buffer) {
      this.blockSize = buffer.length;
      this.buffer = buffer;
   }

   public int getSize() {
      return this.size + this.index;
   }

   public byte[][] toByteArrays() {
      if (this.index > 0) {
         byte[] buf2 = new byte[this.index];
         System.arraycopy(this.buffer, 0, buf2, 0, this.index);
         this.buffers.addLast(buf2);
         this.size = this.size + this.index;
         this.index = 0;
      }

      byte[][] res = new byte[this.buffers.size()][];
      int i = 0;

      for (byte[] bytes : this.buffers) {
         res[i++] = bytes;
      }

      return res;
   }

   public byte[] toByteArray() {
      if (this.buffers.isEmpty()) {
         if (this.buffer.length == this.index) {
            return this.buffer;
         } else {
            this.buffer = Arrays.copyOfRange(this.buffer, 0, this.index);
            return this.buffer;
         }
      } else {
         byte[] data = new byte[this.getSize()];
         int pos = 0;

         for (byte[] bytes : this.buffers) {
            System.arraycopy(bytes, 0, data, pos, bytes.length);
            pos += bytes.length;
         }

         System.arraycopy(this.buffer, 0, data, pos, this.index);
         this.index = this.size + this.index;
         this.buffer = data;
         this.buffers.clear();
         return this.buffer;
      }
   }

   @Override
   public String toString() {
      return new String(this.toByteArray());
   }

   @Override
   public void write(byte[] b) throws IOException {
      if (b.length > this.blockSize) {
         if (this.index > 0) {
            byte[] buf2 = new byte[this.index];
            System.arraycopy(this.buffer, 0, buf2, 0, this.index);
            this.buffer = buf2;
            this.addBuffer();
         }

         this.size += b.length;
         this.buffers.addLast(b);
      } else {
         this.write(b, 0, b.length);
      }
   }

   @Override
   public void write(int datum) {
      if (this.index == this.blockSize) {
         this.addBuffer();
      }

      this.buffer[this.index++] = (byte)datum;
   }

   @Override
   public void write(byte[] data, int offset, int length) throws IOException {
      if (offset >= 0 && offset + length <= data.length && length >= 0) {
         if (this.index + length <= this.blockSize) {
            System.arraycopy(data, offset, this.buffer, this.index, length);
            this.index += length;
         } else {
            do {
               if (this.index == this.blockSize) {
                  this.addBuffer();
               }

               int copyLength = this.blockSize - this.index;
               if (length < copyLength) {
                  copyLength = length;
               }

               System.arraycopy(data, offset, this.buffer, this.index, copyLength);
               offset += copyLength;
               this.index += copyLength;
               length -= copyLength;
            } while (length > 0);
         }
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   public void writeTo(OutputStream out) throws IOException {
      if (this.buffers != null) {
         for (byte[] bytes : this.buffers) {
            out.write(bytes, 0, this.blockSize);
         }
      }

      out.write(this.buffer, 0, this.index);
   }

   public void writeTo(RandomAccessFile out) throws IOException {
      if (this.buffers != null) {
         for (byte[] bytes : this.buffers) {
            out.write(bytes, 0, this.blockSize);
         }
      }

      out.write(this.buffer, 0, this.index);
   }

   public void writeTo(Writer out, String encoding) throws IOException {
      if (this.buffers != null) {
         this.writeToViaSmoosh(out, encoding);
      } else {
         this.writeToViaString(out, encoding);
      }
   }

   private void writeToViaString(Writer out, String encoding) throws IOException {
      byte[] bufferToWrite = this.buffer;
      int bufferToWriteLen = this.index;
      this.writeToImpl(out, encoding, bufferToWrite, bufferToWriteLen);
   }

   private void writeToViaSmoosh(Writer out, String encoding) throws IOException {
      byte[] bufferToWrite = this.toByteArray();
      int bufferToWriteLen = bufferToWrite.length;
      this.writeToImpl(out, encoding, bufferToWrite, bufferToWriteLen);
   }

   private void writeToImpl(Writer out, String encoding, byte[] bufferToWrite, int bufferToWriteLen) throws IOException {
      String writeStr;
      if (encoding != null) {
         writeStr = new String(bufferToWrite, 0, bufferToWriteLen, encoding);
      } else {
         writeStr = new String(bufferToWrite, 0, bufferToWriteLen);
      }

      out.write(writeStr);
   }

   private void addBuffer() {
      this.buffers.addLast(this.buffer);
      this.buffer = new byte[this.blockSize];
      this.size = this.size + this.index;
      this.index = 0;
   }

   @Override
   public void close() {
   }

   public void reset() {
      Arrays.fill(this.buffer, (byte)0);
      this.index = 0;
      this.size = 0;
      this.buffers.clear();
   }
}
