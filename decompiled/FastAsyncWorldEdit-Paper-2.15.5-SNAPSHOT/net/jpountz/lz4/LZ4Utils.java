package net.jpountz.lz4;

import java.nio.ByteBuffer;
import java.util.Arrays;

enum LZ4Utils {
   private static final int MAX_INPUT_SIZE = 2113929216;

   static int maxCompressedLength(int length) {
      if (length < 0) {
         throw new IllegalArgumentException("length must be >= 0, got " + length);
      } else if (length >= 2113929216) {
         throw new IllegalArgumentException("length must be < 2113929216");
      } else {
         return length + length / 255 + 16;
      }
   }

   static boolean notEnoughSpace(int available, int required) {
      return required < 0 ? true : available < required;
   }

   static int lengthOfEncodedInteger(int value) {
      return value >= 15 ? (value - 15) / 255 + 1 : 0;
   }

   static int sequenceLength(int runLen, int matchLen) {
      long len = 1L + lengthOfEncodedInteger(runLen) + runLen + 2L + lengthOfEncodedInteger(matchLen);
      if (len > 2147483647L) {
         throw new LZ4Exception("Sequence length too large");
      } else {
         return (int)len;
      }
   }

   static int hash(int i) {
      return i * -1640531535 >>> 20;
   }

   static int hash64k(int i) {
      return i * -1640531535 >>> 19;
   }

   static int hashHC(int i) {
      return i * -1640531535 >>> 17;
   }

   static void zero(byte[] array, int start, int end) {
      Arrays.fill(array, start, end, (byte)0);
   }

   static void zero(ByteBuffer bb, int start, int end) {
      for (int i = start; i < end; i++) {
         bb.put(i, (byte)0);
      }
   }

   static void copyTo(LZ4Utils.Match m1, LZ4Utils.Match m2) {
      m2.len = m1.len;
      m2.start = m1.start;
      m2.ref = m1.ref;
   }

   static class Match {
      int start;
      int ref;
      int len;

      void fix(int correction) {
         this.start += correction;
         this.ref += correction;
         this.len -= correction;
      }

      int end() {
         return this.start + this.len;
      }
   }
}
