package com.viaversion.viaversion.io;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;
import java.nio.charset.StandardCharsets;

public final class FastByteBufInputStream extends InputStream implements DataInput {
   private static final byte[] EMPTY_BYTES = new byte[0];
   private static final char[] EMPTY_CHARS = new char[0];
   private byte[] bytearr = EMPTY_BYTES;
   private char[] chararr = EMPTY_CHARS;
   private final ByteBuf buf;

   public FastByteBufInputStream(ByteBuf buffer) {
      Preconditions.checkNotNull(buffer);
      this.buf = buffer;
      buffer.markReaderIndex();
   }

   @Override
   public String readUTF() throws IOException {
      int utflen = this.readUnsignedShort();
      if (utflen == 0) {
         return "";
      } else {
         ByteBuf buf = this.buf;
         int readerIdx = buf.readerIndex();
         if (buf.readableBytes() < utflen) {
            throw new EOFException();
         } else {
            String result;
            if (buf.hasArray()) {
               result = this.decodeFromArray(buf.array(), buf.arrayOffset() + readerIdx, utflen);
               buf.readerIndex(readerIdx + utflen);
            } else {
               byte[] data = this.ensureBytes(utflen);
               buf.readBytes(data, 0, utflen);
               result = this.decodeFromArray(data, 0, utflen);
            }

            return result;
         }
      }
   }

   private String decodeFromArray(byte[] data, int offset, int utflen) throws UTFDataFormatException {
      int i = 0;

      while (i < utflen && data[offset + i] >= 0) {
         i++;
      }

      if (i == utflen) {
         return new String(data, offset, utflen, StandardCharsets.ISO_8859_1);
      } else {
         char[] chars = this.ensureChars(utflen);

         for (int j = 0; j < i; j++) {
            chars[j] = (char)data[offset + j];
         }

         int charCount = decode(data, offset + i, offset + utflen, chars, i);
         return new String(chars, 0, charCount);
      }
   }

   private static int decode(byte[] data, int pos, int end, char[] chars, int charCount) throws UTFDataFormatException {
      while (pos < end) {
         int c = data[pos] & 255;
         switch (c >> 4) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
               pos++;
               chars[charCount++] = (char)c;
               break;
            case 8:
            case 9:
            case 10:
            case 11:
            default:
               throw new UTFDataFormatException("malformed input around byte " + pos);
            case 12:
            case 13:
               if (pos + 2 > end) {
                  throw new UTFDataFormatException("malformed input: partial character at end");
               }

               int char2 = data[pos + 1];
               if ((char2 & 192) != 128) {
                  throw new UTFDataFormatException("malformed input around byte " + (pos + 2));
               }

               chars[charCount++] = (char)((c & 31) << 6 | char2 & 63);
               pos += 2;
               break;
            case 14:
               if (pos + 3 > end) {
                  throw new UTFDataFormatException("malformed input: partial character at end");
               }

               int char2 = data[pos + 1];
               int char3 = data[pos + 2];
               if ((char2 & 192) != 128 || (char3 & 192) != 128) {
                  throw new UTFDataFormatException("malformed input around byte " + (pos + 2));
               }

               chars[charCount++] = (char)((c & 15) << 12 | (char2 & 63) << 6 | char3 & 63);
               pos += 3;
         }
      }

      return charCount;
   }

   private byte[] ensureBytes(int len) {
      if (this.bytearr.length < len) {
         this.bytearr = new byte[Math.max(len, 64)];
      }

      return this.bytearr;
   }

   private char[] ensureChars(int len) {
      if (this.chararr.length < len) {
         this.chararr = new char[Math.max(len, 64)];
      }

      return this.chararr;
   }

   @Override
   public int available() {
      return this.buf.readableBytes();
   }

   @Override
   public void mark(int readlimit) {
      this.buf.markReaderIndex();
   }

   @Override
   public boolean markSupported() {
      return true;
   }

   @Override
   public int read() throws IOException {
      return !this.buf.isReadable() ? -1 : this.buf.readByte() & 0xFF;
   }

   @Override
   public int read(byte[] b, int off, int len) throws IOException {
      int available = this.buf.readableBytes();
      if (available == 0) {
         return -1;
      } else {
         len = Math.min(available, len);
         this.buf.readBytes(b, off, len);
         return len;
      }
   }

   @Override
   public void reset() throws IOException {
      this.buf.resetReaderIndex();
   }

   @Override
   public long skip(long n) throws IOException {
      return n > 2147483647L ? this.skipBytes(Integer.MAX_VALUE) : this.skipBytes((int)n);
   }

   @Override
   public boolean readBoolean() throws IOException {
      this.checkAvailable(1);
      return this.buf.readBoolean();
   }

   @Override
   public byte readByte() throws IOException {
      this.checkAvailable(1);
      return this.buf.readByte();
   }

   @Override
   public char readChar() {
      return (char)this.buf.readShort();
   }

   @Override
   public double readDouble() {
      return Double.longBitsToDouble(this.buf.readLong());
   }

   @Override
   public float readFloat() throws IOException {
      return Float.intBitsToFloat(this.buf.readInt());
   }

   @Override
   public void readFully(byte[] b) throws IOException {
      this.readFully(b, 0, b.length);
   }

   @Override
   public void readFully(byte[] b, int off, int len) throws IOException {
      if (len < 0) {
         throw new IndexOutOfBoundsException("fieldSize cannot be a negative number");
      } else {
         this.checkAvailable(len);
         this.buf.readBytes(b, off, len);
      }
   }

   @Override
   public int readInt() throws IOException {
      this.checkAvailable(4);
      return this.buf.readInt();
   }

   @Override
   public String readLine() {
      StringBuilder lineBuf = new StringBuilder();
      int available = this.available();
      if (available == 0) {
         return null;
      } else {
         while (true) {
            int c = this.buf.readUnsignedByte();
            available--;
            switch (c) {
               case 10:
                  return !lineBuf.isEmpty() ? lineBuf.toString() : "";
               case 13:
                  if (available > 0 && (char)this.buf.getUnsignedByte(this.buf.readerIndex()) == '\n') {
                     this.buf.skipBytes(1);
                     available--;
                  }

                  return !lineBuf.isEmpty() ? lineBuf.toString() : "";
               default:
                  lineBuf.append((char)c);
                  if (available <= 0) {
                     return !lineBuf.isEmpty() ? lineBuf.toString() : "";
                  }
            }
         }
      }
   }

   @Override
   public long readLong() throws IOException {
      this.checkAvailable(8);
      return this.buf.readLong();
   }

   @Override
   public short readShort() throws IOException {
      this.checkAvailable(2);
      return this.buf.readShort();
   }

   @Override
   public int readUnsignedByte() {
      return this.buf.readByte() & 0xFF;
   }

   @Override
   public int readUnsignedShort() {
      return this.buf.readShort() & 65535;
   }

   @Override
   public int skipBytes(int n) {
      int nBytes = Math.min(this.available(), n);
      this.buf.skipBytes(nBytes);
      return nBytes;
   }

   private void checkAvailable(int fieldSize) throws IOException {
      if (fieldSize > this.buf.readableBytes()) {
         throw new EOFException("fieldSize is too long! Length is " + fieldSize + ", but maximum is " + this.available());
      }
   }
}
