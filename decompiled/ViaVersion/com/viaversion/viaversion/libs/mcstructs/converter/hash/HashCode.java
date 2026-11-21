package com.viaversion.viaversion.libs.mcstructs.converter.hash;

public class HashCode {
   private static final String HEX_CHARS = "0123456789abcdef";
   private final int hashCode;

   public static HashCode of(int hashCode) {
      return new HashCode(hashCode);
   }

   private HashCode(int hashCode) {
      this.hashCode = hashCode;
   }

   public byte[] asBytes() {
      return new byte[]{(byte)this.hashCode, (byte)(this.hashCode >> 8), (byte)(this.hashCode >> 16), (byte)(this.hashCode >> 24)};
   }

   public int asInt() {
      return this.hashCode;
   }

   public long asLong() {
      return this.hashCode & 4294967295L;
   }

   @Override
   public String toString() {
      byte[] bytes = this.asBytes();
      char[] chars = new char[bytes.length * 2];

      for (int i = 0; i < bytes.length; i++) {
         byte b = bytes[i];
         chars[i * 2] = "0123456789abcdef".charAt(b >> 4 & 15);
         chars[i * 2 + 1] = "0123456789abcdef".charAt(b & 15);
      }

      return new String(chars);
   }
}
