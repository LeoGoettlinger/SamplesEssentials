package com.viaversion.viaversion.libs.mcstructs.converter.hash;

import java.util.Arrays;

public class HashBuilder {
   private final HashFunction hashFunction;
   private byte[] bytes;
   private int index = 0;

   public HashBuilder(HashFunction hashFunction) {
      this(hashFunction, 16);
   }

   public HashBuilder(HashFunction hashFunction, int initialSize) {
      this.hashFunction = hashFunction;
      this.bytes = new byte[initialSize];
   }

   public HashBuilder addByte(byte b) {
      this.ensureSize(1);
      this.bytes[this.index++] = b;
      return this;
   }

   public HashBuilder addBytes(byte[] bytes) {
      this.ensureSize(bytes.length);
      System.arraycopy(bytes, 0, this.bytes, this.index, bytes.length);
      this.index += bytes.length;
      return this;
   }

   public HashBuilder addShort(short s) {
      this.ensureSize(2);
      this.bytes[this.index++] = (byte)s;
      this.bytes[this.index++] = (byte)(s >> 8);
      return this;
   }

   public HashBuilder addChar(char c) {
      this.ensureSize(2);
      this.bytes[this.index++] = (byte)c;
      this.bytes[this.index++] = (byte)(c >> '\b');
      return this;
   }

   public HashBuilder addCharSequence(CharSequence sequence) {
      this.ensureSize(sequence.length());

      for (int i = 0; i < sequence.length(); i++) {
         this.addChar(sequence.charAt(i));
      }

      return this;
   }

   public HashBuilder addInt(int i) {
      this.ensureSize(4);
      this.bytes[this.index++] = (byte)i;
      this.bytes[this.index++] = (byte)(i >> 8);
      this.bytes[this.index++] = (byte)(i >> 16);
      this.bytes[this.index++] = (byte)(i >> 24);
      return this;
   }

   public HashBuilder addLong(long l) {
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

   public HashBuilder addFloat(float f) {
      return this.addInt(Float.floatToIntBits(f));
   }

   public HashBuilder addDouble(double d) {
      return this.addLong(Double.doubleToLongBits(d));
   }

   public HashCode hash() {
      return this.hashFunction.hash(Arrays.copyOf(this.bytes, this.index));
   }

   private void ensureSize(int extra) {
      if (this.index + extra > this.bytes.length) {
         this.bytes = Arrays.copyOf(this.bytes, this.bytes.length * 2 + extra);
      }
   }
}
