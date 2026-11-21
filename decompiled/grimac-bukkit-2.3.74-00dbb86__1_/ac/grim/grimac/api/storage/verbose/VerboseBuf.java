package ac.grim.grimac.api.storage.verbose;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

@ApiStatus.Experimental
public final class VerboseBuf {
   public static final int DEFAULT_CAPACITY = 64;
   public static final int MAX_STRING_BYTES = 65535;
   private static final String UTF_8 = "UTF-8";
   @NotNull
   private byte[] data;
   private int readerIndex;
   private int writerIndex;

   public VerboseBuf() {
      this(64);
   }

   public VerboseBuf(int capacity) {
      if (capacity < 0) {
         throw new IllegalArgumentException("capacity");
      } else {
         this.data = new byte[capacity];
      }
   }

   public VerboseBuf(@NotNull byte[] buffer) {
      this.data = require(buffer, "buffer");
   }

   private VerboseBuf(@NotNull byte[] buffer, int readableLength) {
      this.data = require(buffer, "buffer");
      if (readableLength >= 0 && readableLength <= buffer.length) {
         this.writerIndex = readableLength;
      } else {
         throw new IllegalArgumentException("readableLength");
      }
   }

   @NotNull
   public static VerboseBuf wrap(@NotNull byte[] payload) {
      return new VerboseBuf(payload, payload.length);
   }

   @NotNull
   public static VerboseBuf reuse(@NotNull byte[] buffer) {
      return new VerboseBuf(buffer);
   }

   @NotNull
   public VerboseBuf clear() {
      this.readerIndex = 0;
      this.writerIndex = 0;
      return this;
   }

   @NotNull
   public VerboseBuf rewind() {
      this.readerIndex = 0;
      return this;
   }

   public int remaining() {
      return this.writerIndex - this.readerIndex;
   }

   public int length() {
      assert VerboseSchema.completeWrite(this);

      return this.writerIndex;
   }

   public int capacity() {
      return this.data.length;
   }

   public int readerIndex() {
      return this.readerIndex;
   }

   public int writerIndex() {
      return this.writerIndex;
   }

   @NotNull
   public byte[] array() {
      assert VerboseSchema.completeWrite(this);

      return this.data;
   }

   @NotNull
   public byte[] toByteArray() {
      assert VerboseSchema.completeWrite(this);

      return Arrays.copyOf(this.data, this.writerIndex);
   }

   @NotNull
   public VerboseBuf f64(double v) {
      assert VerboseSchema.recordWrite(this, VerboseSchema.TypeTag.F64);

      long bits = Double.doubleToLongBits(v);
      this.ensureWritable(8);

      for (int i = 0; i < 8; i++) {
         this.data[this.writerIndex++] = (byte)(bits >>> i * 8);
      }

      return this;
   }

   @NotNull
   public VerboseBuf f32(float v) {
      assert VerboseSchema.recordWrite(this, VerboseSchema.TypeTag.F32);

      int bits = Float.floatToIntBits(v);
      this.ensureWritable(4);

      for (int i = 0; i < 4; i++) {
         this.data[this.writerIndex++] = (byte)(bits >>> i * 8);
      }

      return this;
   }

   @NotNull
   public VerboseBuf vi(int v) {
      if (v < 0) {
         throw new IllegalArgumentException("vi cannot encode negative value " + v);
      } else {
         assert VerboseSchema.recordWrite(this, VerboseSchema.TypeTag.VI);

         this.writeRawVarInt(v);
         return this;
      }
   }

   @NotNull
   public VerboseBuf zz(int v) {
      assert VerboseSchema.recordWrite(this, VerboseSchema.TypeTag.ZZ);

      this.writeRawVarInt(v << 1 ^ v >> 31);
      return this;
   }

   @NotNull
   public VerboseBuf vl(long v) {
      if (v < 0L) {
         throw new IllegalArgumentException("vl cannot encode negative value " + v);
      } else {
         assert VerboseSchema.recordWrite(this, VerboseSchema.TypeTag.VL);

         this.writeRawVarLong(v);
         return this;
      }
   }

   @NotNull
   public VerboseBuf bool(boolean v) {
      assert VerboseSchema.recordWrite(this, VerboseSchema.TypeTag.BOOL);

      this.writeByte(v ? 1 : 0);
      return this;
   }

   @NotNull
   public VerboseBuf str(@NotNull CharSequence v) {
      byte[] bytes = utf8(v.toString());
      if (bytes.length > 65535) {
         throw new IllegalArgumentException("string payload exceeds 65535 bytes");
      } else {
         assert VerboseSchema.recordWrite(this, VerboseSchema.TypeTag.STR);

         this.writeRawVarInt(bytes.length);
         this.writeBytes(bytes);
         return this;
      }
   }

   public double rf64() {
      this.requireReadable(8);
      long bits = 0L;

      for (int i = 0; i < 8; i++) {
         bits |= (long)(this.data[this.readerIndex++] & 255) << i * 8;
      }

      return Double.longBitsToDouble(bits);
   }

   public float rf32() {
      this.requireReadable(4);
      int bits = 0;

      for (int i = 0; i < 4; i++) {
         bits |= (this.data[this.readerIndex++] & 255) << i * 8;
      }

      return Float.intBitsToFloat(bits);
   }

   public int rvi() {
      int raw = this.readRawVarInt();
      if (raw < 0) {
         throw new IllegalArgumentException("uvarint exceeds signed int range");
      } else {
         return raw;
      }
   }

   public int rzz() {
      int raw = this.readRawVarInt();
      return raw >>> 1 ^ -(raw & 1);
   }

   public long rvl() {
      long raw = this.readRawVarLong();
      if (raw < 0L) {
         throw new IllegalArgumentException("varlong exceeds signed long range");
      } else {
         return raw;
      }
   }

   public boolean rbool() {
      this.requireReadable(1);
      return this.data[this.readerIndex++] != 0;
   }

   @NotNull
   public String rstr() {
      int len = this.rvi();
      if (len > 65535) {
         throw new IllegalArgumentException("string payload exceeds 65535 bytes");
      } else {
         this.requireReadable(len);
         String out = utf8(this.data, this.readerIndex, len);
         this.readerIndex += len;
         return out;
      }
   }

   public void skip(int typeTag) {
      switch (typeTag) {
         case 1:
            this.skipFixed(8);
            break;
         case 2:
            this.skipFixed(4);
            break;
         case 3:
         case 8:
            this.readRawVarInt();
            break;
         case 4:
            this.readRawVarInt();
            break;
         case 5:
            this.readRawVarLong();
            break;
         case 6:
            this.skipFixed(1);
            break;
         case 7:
            int len = this.rvi();
            if (len > 65535) {
               throw new IllegalArgumentException("string payload exceeds 65535 bytes");
            }

            this.skipFixed(len);
            break;
         default:
            throw new IllegalArgumentException("unknown verbose type tag " + typeTag);
      }
   }

   void writeByte(int value) {
      this.ensureWritable(1);
      this.data[this.writerIndex++] = (byte)value;
   }

   void writeBytes(@NotNull byte[] bytes) {
      this.ensureWritable(bytes.length);
      System.arraycopy(bytes, 0, this.data, this.writerIndex, bytes.length);
      this.writerIndex += bytes.length;
   }

   int readUnsignedByte() {
      this.requireReadable(1);
      return this.data[this.readerIndex++] & 0xFF;
   }

   private void skipFixed(int bytes) {
      this.requireReadable(bytes);
      this.readerIndex += bytes;
   }

   private void writeRawVarInt(int value) {
      int v;
      for (v = value; (v & -128) != 0; v >>>= 7) {
         this.writeByte(v & 127 | 128);
      }

      this.writeByte(v);
   }

   private int readRawVarInt() {
      int value = 0;

      for (int shift = 0; shift < 35; shift += 7) {
         int b = this.readUnsignedByte();
         if (shift == 28 && (b & 240) != 0) {
            throw new IllegalArgumentException("varint exceeds 32 bits");
         }

         value |= (b & 127) << shift;
         if ((b & 128) == 0) {
            return value;
         }
      }

      throw new IllegalArgumentException("varint is too long");
   }

   private void writeRawVarLong(long value) {
      long v;
      for (v = value; (v & -128L) != 0L; v >>>= 7) {
         this.writeByte((int)(v & 127L | 128L));
      }

      this.writeByte((int)v);
   }

   private long readRawVarLong() {
      long value = 0L;

      for (int shift = 0; shift < 70; shift += 7) {
         int b = this.readUnsignedByte();
         if (shift == 63 && (b & 254) != 0) {
            throw new IllegalArgumentException("varlong exceeds 64 bits");
         }

         value |= (long)(b & 127) << shift;
         if ((b & 128) == 0) {
            return value;
         }
      }

      throw new IllegalArgumentException("varlong is too long");
   }

   private void ensureWritable(int bytes) {
      int needed = this.writerIndex + bytes;
      if (needed > this.data.length) {
         int next = Math.max(64, this.data.length);

         while (next < needed) {
            next = next + (next >>> 1) + 1;
         }

         this.data = Arrays.copyOf(this.data, next);
      }
   }

   private void requireReadable(int bytes) {
      if (bytes < 0 || this.remaining() < bytes) {
         throw new VerboseBuf.UnderflowException("verbose payload truncated");
      }
   }

   @NotNull
   private static byte[] utf8(@NotNull String value) {
      try {
         return value.getBytes("UTF-8");
      } catch (UnsupportedEncodingException var2) {
         throw new AssertionError("UTF-8 unavailable", var2);
      }
   }

   @NotNull
   private static String utf8(@NotNull byte[] bytes, int offset, int length) {
      try {
         return new String(bytes, offset, length, "UTF-8");
      } catch (UnsupportedEncodingException var4) {
         throw new AssertionError("UTF-8 unavailable", var4);
      }
   }

   @NotNull
   private static byte[] require(@NotNull byte[] value, @NotNull String name) {
      if (value == null) {
         throw new IllegalArgumentException(name);
      } else {
         return value;
      }
   }

   @ApiStatus.Experimental
   public static final class UnderflowException extends RuntimeException {
      public UnderflowException(@NotNull String message) {
         super(message);
      }
   }
}
