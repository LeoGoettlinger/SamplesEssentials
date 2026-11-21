package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.io.doubleparser;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Map;

class JavaBigIntegerFromByteArray extends AbstractNumberParser {
   public static final int MAX_INPUT_LENGTH = 1292782622;
   private static final int MAX_DECIMAL_DIGITS = 646456993;
   private static final int MAX_HEX_DIGITS = 536870912;

   public BigInteger parseBigIntegerLiteral(byte[] str, int offset, int length, int radix) throws NumberFormatException {
      try {
         int endIndex = offset + length;
         if (offset >= 0 && endIndex >= offset && endIndex <= str.length && length <= 1292782622) {
            int index = offset;
            byte ch = str[offset];
            boolean isNegative = ch == 45;
            if (isNegative || ch == 43) {
               index = offset + 1;
               ch = charAt(str, index, endIndex);
               if (ch == 0) {
                  throw new NumberFormatException("illegal syntax");
               }
            }

            switch (radix) {
               case 10:
                  return this.parseDecDigits(str, index, endIndex, isNegative);
               case 16:
                  return this.parseHexDigits(str, index, endIndex, isNegative);
               default:
                  return new BigInteger(new String(str, offset, length, StandardCharsets.ISO_8859_1), radix);
            }
         } else {
            throw new IllegalArgumentException("offset < 0 or length > str.length");
         }
      } catch (ArithmeticException var9) {
         NumberFormatException nfe = new NumberFormatException("value exceeds limits");
         nfe.initCause(var9);
         throw nfe;
      }
   }

   private BigInteger parseDecDigits(byte[] str, int from, int to, boolean isNegative) {
      int numDigits = to - from;
      if (numDigits > 18) {
         return this.parseManyDecDigits(str, from, to, isNegative);
      } else {
         int preroll = from + (numDigits & 7);
         long significand = FastDoubleSwar.tryToParseUpTo7Digits(str, from, preroll);
         boolean success = significand >= 0L;

         for (int var11 = preroll; var11 < to; var11 += 8) {
            int addend = FastDoubleSwar.tryToParseEightDigitsUtf8(str, var11);
            success &= addend >= 0;
            significand = significand * 100000000L + addend;
         }

         if (!success) {
            throw new NumberFormatException("illegal syntax");
         } else {
            return BigInteger.valueOf(isNegative ? -significand : significand);
         }
      }
   }

   private BigInteger parseHexDigits(byte[] str, int from, int to, boolean isNegative) {
      from = this.skipZeroes(str, from, to);
      int numDigits = to - from;
      if (numDigits <= 0) {
         return BigInteger.ZERO;
      } else if (numDigits > 536870912) {
         throw new NumberFormatException("value exceeds limits");
      } else {
         byte[] bytes = new byte[(numDigits + 1 >> 1) + 1];
         int index = 1;
         boolean illegalDigits = false;
         if ((numDigits & 1) != 0) {
            byte chLow = str[from++];
            int valueLow = chLow < 0 ? -1 : AbstractFloatValueParser.CHAR_TO_HEX_MAP[chLow];
            bytes[index++] = (byte)valueLow;
            illegalDigits = valueLow < 0;
         }

         for (int prerollLimit = from + (to - from & 7); from < prerollLimit; from += 2) {
            byte chHigh = str[from];
            byte chLow = str[from + 1];
            int valueHigh = chHigh < 0 ? -1 : AbstractFloatValueParser.CHAR_TO_HEX_MAP[chHigh];
            int valueLow = chLow < 0 ? -1 : AbstractFloatValueParser.CHAR_TO_HEX_MAP[chLow];
            bytes[index++] = (byte)(valueHigh << 4 | valueLow);
            illegalDigits |= valueHigh < 0 || valueLow < 0;
         }

         while (from < to) {
            long value = FastDoubleSwar.tryToParseEightHexDigits(str, from);
            FastDoubleSwar.writeIntBE(bytes, index, (int)value);
            illegalDigits |= value < 0L;
            from += 8;
            index += 4;
         }

         if (illegalDigits) {
            throw new NumberFormatException("illegal syntax");
         } else {
            BigInteger result = new BigInteger(bytes);
            return isNegative ? result.negate() : result;
         }
      }
   }

   private BigInteger parseManyDecDigits(byte[] str, int from, int to, boolean isNegative) {
      from = this.skipZeroes(str, from, to);
      int numDigits = to - from;
      if (numDigits > 646456993) {
         throw new NumberFormatException("value exceeds limits");
      } else {
         Map<Integer, BigInteger> powersOfTen = FastIntegerMath.fillPowersOf10Floor16(from, to);
         BigInteger result = ParseDigitsTaskByteArray.parseDigitsRecursive(str, from, to, powersOfTen);
         return isNegative ? result.negate() : result;
      }
   }

   private int skipZeroes(byte[] str, int from, int to) {
      while (from < to - 8 && FastDoubleSwar.isEightZeroes(str, from)) {
         from += 8;
      }

      while (from < to && str[from] == 48) {
         from++;
      }

      return from;
   }
}
