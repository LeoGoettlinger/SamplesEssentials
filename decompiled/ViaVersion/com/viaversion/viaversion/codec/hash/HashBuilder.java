package com.viaversion.viaversion.codec.hash;

import java.util.Arrays;

public final class HashBuilder {
   private static final byte[] EMPTY_BYTES = new byte[0];
   private static final int MAX_RETAINED_CAPACITY = 4096;
   private final HashFunction hashFunction;
   private byte[] bytes = EMPTY_BYTES;
   private int index;
   private boolean direct;

   public HashBuilder(HashFunction hashFunction) {
      this.hashFunction = hashFunction;
   }

   public HashBuilder writeByte(byte b) {
      this.ensureSize(1);
      this.bytes[this.index++] = b;
      return this;
   }

   public HashBuilder writeBytes(byte[] bytes) {
      this.ensureSize(bytes.length);
      System.arraycopy(bytes, 0, this.bytes, this.index, bytes.length);
      this.index += bytes.length;
      return this;
   }

   public void writeBytesDirect(byte[] bytes) {
      if (this.bytes.length == 0) {
         this.bytes = bytes;
         this.index = bytes.length;
         this.direct = true;
      } else {
         this.writeBytes(bytes);
      }
   }

   public HashBuilder writeBoolean(boolean value) {
      this.ensureSize(1);
      this.bytes[this.index++] = (byte)(value ? 1 : 0);
      return this;
   }

   public HashBuilder writeShort(short s) {
      this.ensureSize(2);
      this.bytes[this.index++] = (byte)s;
      this.bytes[this.index++] = (byte)(s >> 8);
      return this;
   }

   public HashBuilder writeChar(char c) {
      this.ensureSize(2);
      this.bytes[this.index++] = (byte)c;
      this.bytes[this.index++] = (byte)(c >> '\b');
      return this;
   }

   public HashBuilder writeString(CharSequence sequence) {
      int length = sequence.length();
      this.ensureSize(length * 2);

      for (int i = 0; i < length; i++) {
         char c = sequence.charAt(i);
         this.bytes[this.index++] = (byte)c;
         this.bytes[this.index++] = (byte)(c >> '\b');
      }

      return this;
   }

   public HashBuilder writeInt(int i) {
      this.ensureSize(4);
      this.bytes[this.index++] = (byte)i;
      this.bytes[this.index++] = (byte)(i >> 8);
      this.bytes[this.index++] = (byte)(i >> 16);
      this.bytes[this.index++] = (byte)(i >> 24);
      return this;
   }

   public HashBuilder writeLong(long l) {
      this.ensureSize(8);
      this.bytes[this.index++] = (byte)l;
      this.bytes[this.index++] = (byte)(l >> 8);
      this.bytes[this.index++] = (byte)(l >> 16);
      this.bytes[this.index++] = (byte)(l >> 24);
      this.bytes[this.index++] = (byte)(l >> 32);
      this.bytes[this.index++] = (byte)(l >> 40);
      this.bytes[this.index++] = (byte)(l >> 48);
      this.bytes[this.index++] = (byte)(l >> 56);
      return this;
   }

   public HashBuilder writeFloat(float f) {
      this.writeInt(Float.floatToIntBits(f));
      return this;
   }

   public HashBuilder writeDouble(double d) {
      this.writeLong(Double.doubleToLongBits(d));
      return this;
   }

   public HashBuilder preSize(int bytes) {
      if (this.bytes.length == 0) {
         this.bytes = new byte[bytes];
      } else {
         this.ensureSize(bytes);
      }

      return this;
   }

   private void ensureSize(int bytes) {
      int length = this.bytes.length;
      int required = this.index + bytes;
      if (required > length) {
         int newLength = Math.max(length * 2, required);
         this.bytes = Arrays.copyOf(this.bytes, newLength);
         this.direct = false;
      }
   }

   public int hash() {
      return this.hashFunction.hashBytes(this.bytes, this.index);
   }

   public void reset() {
      this.index = 0;
      if (this.direct || this.bytes.length > 4096) {
         this.bytes = EMPTY_BYTES;
         this.direct = false;
      }
   }

   public HashFunction function() {
      return this.hashFunction;
   }
}
