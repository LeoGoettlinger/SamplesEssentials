package com.fastasyncworldedit.core.internal.io;

import java.io.DataOutput;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import javax.annotation.Nonnull;

public class LittleEndianOutputStream extends FilterOutputStream implements DataOutput {
   protected int written;

   public LittleEndianOutputStream(OutputStream out) {
      super(out);
   }

   @Override
   public synchronized void write(int b) throws IOException {
      this.out.write(b);
      this.written++;
   }

   @Override
   public synchronized void write(@Nonnull byte[] data, int offset, int length) throws IOException {
      this.out.write(data, offset, length);
      this.written += length;
   }

   @Override
   public void writeBoolean(boolean b) throws IOException {
      if (b) {
         this.write(1);
      } else {
         this.write(0);
      }
   }

   @Override
   public void writeByte(int b) throws IOException {
      this.out.write(b);
      this.written++;
   }

   @Override
   public void writeShort(int s) throws IOException {
      this.out.write(s & 0xFF);
      this.out.write(s >>> 8 & 0xFF);
      this.written += 2;
   }

   @Override
   public void writeChar(int c) throws IOException {
      this.out.write(c & 0xFF);
      this.out.write(c >>> 8 & 0xFF);
      this.written += 2;
   }

   @Override
   public void writeInt(int i) throws IOException {
      this.out.write(i & 0xFF);
      this.out.write(i >>> 8 & 0xFF);
      this.out.write(i >>> 16 & 0xFF);
      this.out.write(i >>> 24 & 0xFF);
      this.written += 4;
   }

   @Override
   public void writeLong(long l) throws IOException {
      this.out.write((int)l & 0xFF);
      this.out.write((int)(l >>> 8) & 0xFF);
      this.out.write((int)(l >>> 16) & 0xFF);
      this.out.write((int)(l >>> 24) & 0xFF);
      this.out.write((int)(l >>> 32) & 0xFF);
      this.out.write((int)(l >>> 40) & 0xFF);
      this.out.write((int)(l >>> 48) & 0xFF);
      this.out.write((int)(l >>> 56) & 0xFF);
      this.written += 8;
   }

   @Override
   public final void writeFloat(float f) throws IOException {
      this.writeInt(Float.floatToIntBits(f));
   }

   @Override
   public final void writeDouble(double d) throws IOException {
      this.writeLong(Double.doubleToLongBits(d));
   }

   @Override
   public void writeBytes(String s) throws IOException {
      int length = s.length();

      for (int i = 0; i < length; i++) {
         this.out.write((byte)s.charAt(i));
      }

      this.written += length;
   }

   @Override
   public void writeChars(String s) throws IOException {
      int length = s.length();

      for (int i = 0; i < length; i++) {
         int c = s.charAt(i);
         this.out.write(c & 0xFF);
         this.out.write(c >>> 8 & 0xFF);
      }

      this.written += length * 2;
   }

   @Override
   public void writeUTF(String s) throws IOException {
      int numchars = s.length();
      int numbytes = 0;

      for (int i = 0; i < numchars; i++) {
         int c = s.charAt(i);
         if (c >= 1 && c <= 127) {
            numbytes++;
         } else if (c > 2047) {
            numbytes += 3;
         } else {
            numbytes += 2;
         }
      }

      if (numbytes > 65535) {
         throw new UTFDataFormatException();
      } else {
         this.out.write(numbytes >>> 8 & 0xFF);
         this.out.write(numbytes & 0xFF);

         for (int ix = 0; ix < numchars; ix++) {
            int c = s.charAt(ix);
            if (c >= 1 && c <= 127) {
               this.out.write(c);
            } else if (c > 2047) {
               this.out.write(224 | c >> 12 & 15);
               this.out.write(128 | c >> 6 & 63);
               this.out.write(128 | c & 63);
               this.written += 2;
            } else {
               this.out.write(192 | c >> 6 & 31);
               this.out.write(128 | c & 63);
               this.written++;
            }
         }

         this.written += numchars + 2;
      }
   }

   public int size() {
      return this.written;
   }
}
