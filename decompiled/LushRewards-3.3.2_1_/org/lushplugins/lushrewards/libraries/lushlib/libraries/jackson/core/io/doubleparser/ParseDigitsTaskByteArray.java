package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.io.doubleparser;

import java.math.BigInteger;
import java.util.Map;

class ParseDigitsTaskByteArray {
   public static final int RECURSION_THRESHOLD = 400;

   private ParseDigitsTaskByteArray() {
   }

   static BigInteger parseDigitsIterative(byte[] str, int from, int to) {
      int numDigits = to - from;
      BigSignificand bigSignificand = new BigSignificand(FastIntegerMath.estimateNumBits(numDigits));
      int preroll = from + (numDigits & 7);
      int value = FastDoubleSwar.tryToParseUpTo7Digits(str, from, preroll);
      boolean success = value >= 0;
      bigSignificand.add(value);

      for (int var9 = preroll; var9 < to; var9 += 8) {
         int addend = FastDoubleSwar.tryToParseEightDigits(str, var9);
         success &= addend >= 0;
         bigSignificand.fma(100000000, addend);
      }

      if (!success) {
         throw new NumberFormatException("illegal syntax");
      } else {
         return bigSignificand.toBigInteger();
      }
   }

   static BigInteger parseDigitsRecursive(byte[] str, int from, int to, Map<Integer, BigInteger> powersOfTen) {
      int numDigits = to - from;
      if (numDigits <= 400) {
         return parseDigitsIterative(str, from, to);
      } else {
         int mid = FastIntegerMath.splitFloor16(from, to);
         BigInteger high = parseDigitsRecursive(str, from, mid, powersOfTen);
         BigInteger low = parseDigitsRecursive(str, mid, to, powersOfTen);
         high = FftMultiplier.multiply(high, powersOfTen.get(to - mid));
         return low.add(high);
      }
   }
}
