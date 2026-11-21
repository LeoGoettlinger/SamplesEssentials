package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.io.doubleparser;

abstract class AbstractJavaFloatingPointBitsFromCharSequence extends AbstractFloatValueParser {
   private static int skipWhitespace(CharSequence str, int index, int endIndex) {
      while (index < endIndex && str.charAt(index) <= ' ') {
         index++;
      }

      return index;
   }

   abstract long nan();

   abstract long negativeInfinity();

   private long parseDecFloatLiteral(CharSequence str, int index, int startIndex, int endIndex, boolean isNegative, boolean hasLeadingZero) {
      long significand = 0L;
      int significandStartIndex = index;
      int virtualIndexOfPoint = -1;
      boolean illegal = false;

      char ch;
      for (ch = '\u0000'; index < endIndex; index++) {
         ch = str.charAt(index);
         if (FastDoubleSwar.isDigit(ch)) {
            significand = 10L * significand + ch - 48L;
         } else {
            if (ch != '.') {
               break;
            }

            illegal |= virtualIndexOfPoint >= 0;
            virtualIndexOfPoint = index;
         }
      }

      int significandEndIndex = index;
      int digitCount;
      int exponent;
      if (virtualIndexOfPoint < 0) {
         digitCount = index - significandStartIndex;
         virtualIndexOfPoint = index;
         exponent = 0;
      } else {
         digitCount = index - significandStartIndex - 1;
         exponent = virtualIndexOfPoint - index + 1;
      }

      int expNumber = 0;
      if ((ch | ' ') == 101) {
         ch = charAt(str, ++index, endIndex);
         boolean isExponentNegative = ch == '-';
         if (isExponentNegative || ch == '+') {
            ch = charAt(str, ++index, endIndex);
         }

         illegal |= !FastDoubleSwar.isDigit(ch);

         do {
            if (expNumber < 1024) {
               expNumber = 10 * expNumber + ch - 48;
            }

            ch = charAt(str, ++index, endIndex);
         } while (FastDoubleSwar.isDigit(ch));

         if (isExponentNegative) {
            expNumber = -expNumber;
         }

         exponent += expNumber;
      }

      if (ch == 'd' | ch == 'D' | ch == 'f' | ch == 'F') {
         index++;
      }

      index = skipWhitespace(str, index, endIndex);
      if (!illegal && index >= endIndex && (hasLeadingZero || digitCount != 0)) {
         int skipCountInTruncatedDigits = 0;
         int exponentOfTruncatedSignificand;
         boolean isSignificandTruncated;
         if (digitCount > 19) {
            significand = 0L;

            for (index = significandStartIndex; index < significandEndIndex; index++) {
               ch = str.charAt(index);
               if (ch == '.') {
                  skipCountInTruncatedDigits++;
               } else {
                  if (Long.compareUnsigned(significand, 1000000000000000000L) >= 0) {
                     break;
                  }

                  significand = 10L * significand + ch - 48L;
               }
            }

            isSignificandTruncated = index < significandEndIndex;
            exponentOfTruncatedSignificand = virtualIndexOfPoint - index + skipCountInTruncatedDigits + expNumber;
         } else {
            isSignificandTruncated = false;
            exponentOfTruncatedSignificand = 0;
         }

         return this.valueOfFloatLiteral(str, startIndex, endIndex, isNegative, significand, exponent, isSignificandTruncated, exponentOfTruncatedSignificand);
      } else {
         throw new NumberFormatException("illegal syntax");
      }
   }

   public final long parseFloatingPointLiteral(CharSequence str, int offset, int length) {
      int endIndex = offset + length;
      if (offset >= 0 && endIndex >= offset && endIndex <= str.length() && length <= 2147483643) {
         int index = skipWhitespace(str, offset, endIndex);
         if (index == endIndex) {
            throw new NumberFormatException("illegal syntax");
         } else {
            char ch = str.charAt(index);
            boolean isNegative = ch == '-';
            if (isNegative || ch == '+') {
               ch = charAt(str, ++index, endIndex);
               if (ch == 0) {
                  throw new NumberFormatException("illegal syntax");
               }
            }

            if (ch >= 'I') {
               return this.parseNaNOrInfinity(str, index, endIndex, isNegative);
            } else {
               boolean hasLeadingZero = ch == '0';
               if (hasLeadingZero) {
                  ch = charAt(str, ++index, endIndex);
                  if (ch == 'x' || ch == 'X') {
                     return this.parseHexFloatLiteral(str, index + 1, offset, endIndex, isNegative);
                  }
               }

               return this.parseDecFloatLiteral(str, index, offset, endIndex, isNegative, hasLeadingZero);
            }
         }
      } else {
         throw new IllegalArgumentException("offset < 0 or length > str.length");
      }
   }

   private long parseHexFloatLiteral(CharSequence str, int index, int startIndex, int endIndex, boolean isNegative) {
      long significand = 0L;
      int exponent = 0;
      int significandStartIndex = index;
      int virtualIndexOfPoint = -1;
      boolean illegal = false;

      char ch;
      for (ch = '\u0000'; index < endIndex; index++) {
         ch = str.charAt(index);
         int hexValue = lookupHex(ch);
         if (hexValue >= 0) {
            significand = significand << 4 | hexValue;
         } else {
            if (hexValue != -4) {
               break;
            }

            illegal |= virtualIndexOfPoint >= 0;

            for (virtualIndexOfPoint = index; index < endIndex - 8; index += 8) {
               long parsed = FastDoubleSwar.tryToParseEightHexDigits(str, index + 1);
               if (parsed < 0L) {
                  break;
               }

               significand = (significand << 32) + parsed;
            }
         }
      }

      int significandEndIndex = index;
      int digitCount;
      if (virtualIndexOfPoint < 0) {
         digitCount = index - significandStartIndex;
         virtualIndexOfPoint = index;
      } else {
         digitCount = index - significandStartIndex - 1;
         exponent = Math.min(virtualIndexOfPoint - index + 1, 1024) * 4;
      }

      int expNumber = 0;
      boolean hasExponent = (ch | ' ') == 112;
      if (hasExponent) {
         ch = charAt(str, ++index, endIndex);
         boolean isExponentNegative = ch == '-';
         if (isExponentNegative || ch == '+') {
            ch = charAt(str, ++index, endIndex);
         }

         illegal |= !FastDoubleSwar.isDigit(ch);

         do {
            if (expNumber < 1024) {
               expNumber = 10 * expNumber + ch - 48;
            }

            ch = charAt(str, ++index, endIndex);
         } while (FastDoubleSwar.isDigit(ch));

         if (isExponentNegative) {
            expNumber = -expNumber;
         }

         exponent += expNumber;
      }

      if (ch == 'd' | ch == 'D' | ch == 'f' | ch == 'F') {
         index++;
      }

      index = skipWhitespace(str, index, endIndex);
      if (!illegal && index >= endIndex && digitCount != 0 && hasExponent) {
         int skipCountInTruncatedDigits = 0;
         boolean isSignificandTruncated;
         if (digitCount > 16) {
            significand = 0L;

            for (index = significandStartIndex; index < significandEndIndex; index++) {
               ch = str.charAt(index);
               int hexValue = lookupHex(ch);
               if (hexValue >= 0) {
                  if (Long.compareUnsigned(significand, 1000000000000000000L) >= 0) {
                     break;
                  }

                  significand = significand << 4 | hexValue;
               } else {
                  skipCountInTruncatedDigits++;
               }
            }

            isSignificandTruncated = index < significandEndIndex;
         } else {
            isSignificandTruncated = false;
         }

         return this.valueOfHexLiteral(
            str,
            startIndex,
            endIndex,
            isNegative,
            significand,
            exponent,
            isSignificandTruncated,
            (virtualIndexOfPoint - index + skipCountInTruncatedDigits) * 4 + expNumber
         );
      } else {
         throw new NumberFormatException("illegal syntax");
      }
   }

   private long parseNaNOrInfinity(CharSequence str, int index, int endIndex, boolean isNegative) {
      if (str.charAt(index) == 'N') {
         if (index + 2 < endIndex && str.charAt(index + 1) == 'a' && str.charAt(index + 2) == 'N') {
            index = skipWhitespace(str, index + 3, endIndex);
            if (index == endIndex) {
               return this.nan();
            }
         }
      } else if (index + 7 < endIndex
         && str.charAt(index) == 'I'
         && str.charAt(index + 1) == 'n'
         && str.charAt(index + 2) == 'f'
         && str.charAt(index + 3) == 'i'
         && str.charAt(index + 4) == 'n'
         && str.charAt(index + 5) == 'i'
         && str.charAt(index + 6) == 't'
         && str.charAt(index + 7) == 'y') {
         index = skipWhitespace(str, index + 8, endIndex);
         if (index == endIndex) {
            return isNegative ? this.negativeInfinity() : this.positiveInfinity();
         }
      }

      throw new NumberFormatException("illegal syntax");
   }

   abstract long positiveInfinity();

   abstract long valueOfFloatLiteral(CharSequence var1, int var2, int var3, boolean var4, long var5, int var7, boolean var8, int var9);

   abstract long valueOfHexLiteral(CharSequence var1, int var2, int var3, boolean var4, long var5, int var7, boolean var8, int var9);
}
