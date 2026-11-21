package com.viaversion.viaversion.io;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;

public final class FastByteBufOutputStream extends OutputStream implements DataOutput {
   private final ByteBuf buf;

   public FastByteBufOutputStream(ByteBuf buffer) {
      Preconditions.checkNotNull(buffer);
      this.buf = buffer;
   }

   @Override
   public void writeUTF(String s) throws IOException {
      int len = s.length();
      if (len == 0) {
         this.buf.writeShort(0);
      } else if (len > 65535) {
         throw new UTFDataFormatException("encoded string too long: " + len + " characters");
      } else {
         int lenIndex = this.buf.writerIndex();
         int startIndex = lenIndex + 2;
         this.buf.ensureWritable(2 + len);
         this.buf.writeShort(len);

         int i;
         for (i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c >= 128 || c == 0) {
               break;
            }

            this.buf.writeByte(c);
         }

         if (i != len) {
            for (; i < len; i++) {
               char c = s.charAt(i);
               if (c < 128 && c != 0) {
                  this.buf.writeByte(c);
               } else if (c >= 2048) {
                  this.buf.writeByte(224 | c >> '\f' & 15);
                  this.buf.writeByte(128 | c >> 6 & 63);
                  this.buf.writeByte(128 | c & '?');
               } else {
                  this.buf.writeByte(192 | c >> 6 & 31);
                  this.buf.writeByte(128 | c & '?');
               }
            }

            int utflen = this.buf.writerIndex() - startIndex;
            if (utflen > 65535) {
               this.buf.writerIndex(lenIndex);
               throw new UTFDataFormatException("encoded string too long: " + utflen + " bytes");
            } else {
               this.buf.setShort(lenIndex, utflen);
            }
         }
      }
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException {
      this.buf.writeBytes(b, off, len);
   }

   @Override
   public void write(byte[] b) throws IOException {
      this.buf.writeBytes(b);
   }

   @Override
   public void write(int b) throws IOException {
      this.buf.writeByte(b);
   }

   @Override
   public void writeBoolean(boolean v) throws IOException {
      this.buf.writeBoolean(v);
   }

   @Override
   public void writeByte(int v) throws IOException {
      this.buf.writeByte(v);
   }

   @Override
   public void writeBytes(String s) throws IOException {
      int length = s.length();
      this.buf.ensureWritable(length);
      int offset = this.buf.writerIndex();

      for (int i = 0; i < length; i++) {
         this.buf.setByte(offset + i, (byte)s.charAt(i));
      }

      this.buf.writerIndex(offset + length);
   }

   @Override
   public void writeChar(int v) {
      this.buf.writeChar(v);
   }

   @Override
   public void writeChars(String s) {
      int len = s.length();

      for (int i = 0; i < len; i++) {
         this.buf.writeChar(s.charAt(i));
      }
   }

   @Override
   public void writeDouble(double v) throws IOException {
      this.buf.writeDouble(v);
   }

   @Override
   public void writeFloat(float v) throws IOException {
      this.buf.writeFloat(v);
   }

   @Override
   public void writeInt(int v) throws IOException {
      this.buf.writeInt(v);
   }

   @Override
   public void writeLong(long v) throws IOException {
      this.buf.writeLong(v);
   }

   @Override
   public void writeShort(int v) throws IOException {
      this.buf.writeShort((short)v);
   }
}
