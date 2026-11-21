package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.io.doubleparser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.NavigableMap;

final class JavaBigDecimalFromCharSequence extends AbstractNumberParser {
   public static final int MAX_INPUT_LENGTH = 1292782635;
   private static final int MANY_DIGITS_THRESHOLD = 32;
   private static final int MAX_DIGIT_COUNT = 1292782621;
   private static final long MAX_EXPONENT_NUMBER = 2147483647L;

   public JavaBigDecimalFromCharSequence() {
   }

   public BigDecimal parseBigDecimalString(CharSequence str, int offset, int length) {
      try {
         if (length >= 32) {
            return this.parseBigDecimalStringWithManyDigits(str, offset, length);
         } else {
            long significand = 0L;
            int decimalPointIndex = -1;
            int endIndex = offset + length;
            int index = offset;
            char ch = charAt(str, offset, endIndex);
            boolean illegal = false;
            boolean isNegative = ch == '-';
            if (isNegative || ch == '+') {
               index = offset + 1;
               ch = charAt(str, index, endIndex);
               if (ch == 0) {
                  throw new NumberFormatException("illegal syntax");
               }
            }

            int integerPartIndex;
            for (integerPartIndex = index; index < endIndex; index++) {
               ch = str.charAt(index);
               if (FastDoubleSwar.isDigit(ch)) {
                  significand = 10L * significand + ch - 48L;
               } else {
                  if (ch != '.') {
                     break;
                  }

                  illegal |= decimalPointIndex >= 0;

                  for (decimalPointIndex = index; index < endIndex - 4; index += 4) {
                     int digits = FastDoubleSwar.tryToParseFourDigits(str, index + 1);
                     if (digits < 0) {
                        break;
                     }

                     significand = 10000L * significand + digits;
                  }
               }
            }

            long exponent;
            int digitCount;
            if (decimalPointIndex < 0) {
               digitCount = index - integerPartIndex;
               decimalPointIndex = index;
               exponent = 0L;
            } else {
               digitCount = index - integerPartIndex - 1;
               exponent = decimalPointIndex - index + 1;
            }

            long expNumber = 0L;
            int exponentIndicatorIndex;
            if ((ch | ' ') == 101) {
               exponentIndicatorIndex = index++;
               ch = charAt(str, index, endIndex);
               boolean isExponentNegative = ch == '-';
               if (isExponentNegative || ch == '+') {
                  ch = charAt(str, ++index, endIndex);
               }

               illegal |= !FastDoubleSwar.isDigit(ch);

               do {
                  if (expNumber < 2147483647L) {
                     expNumber = 10L * expNumber + ch - 48L;
                  }

                  ch = charAt(str, ++index, endIndex);
               } while (FastDoubleSwar.isDigit(ch));

               if (isExponentNegative) {
                  expNumber = -expNumber;
               }

               exponent += expNumber;
            } else {
               exponentIndicatorIndex = endIndex;
            }

            if (illegal || index < endIndex || digitCount == 0 || digitCount > 1292782621) {
               throw new NumberFormatException("illegal syntax");
            } else if (exponent <= -2147483648L || exponent > 2147483647L) {
               throw new NumberFormatException("value exceeds limits");
            } else {
               return digitCount <= 18
                  ? new BigDecimal(isNegative ? -significand : significand).scaleByPowerOfTen((int)exponent)
                  : this.valueOfBigDecimalString(
                     str, integerPartIndex, decimalPointIndex, decimalPointIndex + 1, exponentIndicatorIndex, isNegative, (int)exponent
                  );
            }
         }
      } catch (ArithmeticException var21) {
         NumberFormatException nfe = new NumberFormatException("value exceeds limits");
         nfe.initCause(var21);
         throw nfe;
      }
   }

   BigDecimal parseBigDecimalStringWithManyDigits(CharSequence str, int offset, int length) {
      if (length > 1292782635) {
         throw new NumberFormatException("illegal syntax");
      } else {
         int decimalPointIndex = -1;
         int nonZeroFractionalPartIndex = -1;
         int endIndex = offset + length;
         int index = offset;
         char ch = charAt(str, offset, endIndex);
         boolean illegal = false;
         boolean isNegative = ch == '-';
         if (isNegative || ch == '+') {
            index = offset + 1;
            ch = charAt(str, index, endIndex);
            if (ch == 0) {
               throw new NumberFormatException("illegal syntax");
            }
         }

         int integerPartIndex = index;

         while (index < endIndex - 8 && FastDoubleSwar.isEightZeroes(str, index)) {
            index += 8;
         }

         while (index < endIndex && str.charAt(index) == '0') {
            index++;
         }

         int nonZeroIntegerPartIndex = index;

         while (index < endIndex - 8 && FastDoubleSwar.isEightDigits(str, index)) {
            index += 8;
         }

         while (index < endIndex && FastDoubleSwar.isDigit(ch = str.charAt(index))) {
            index++;
         }

         if (ch == '.') {
            decimalPointIndex = index++;

            while (index < endIndex - 8 && FastDoubleSwar.isEightZeroes(str, index)) {
               index += 8;
            }

            while (index < endIndex && str.charAt(index) == '0') {
               index++;
            }

            nonZeroFractionalPartIndex = index;

            while (index < endIndex - 8 && FastDoubleSwar.isEightDigits(str, index)) {
               index += 8;
            }

            while (index < endIndex && FastDoubleSwar.isDigit(ch = str.charAt(index))) {
               index++;
            }
         }

         int digitCount;
         long exponent;
         if (decimalPointIndex < 0) {
            digitCount = index - nonZeroIntegerPartIndex;
            decimalPointIndex = index;
            nonZeroFractionalPartIndex = index;
            exponent = 0L;
         } else {
            digitCount = nonZeroIntegerPartIndex == decimalPointIndex ? index - nonZeroFractionalPartIndex : index - nonZeroIntegerPartIndex - 1;
            exponent = decimalPointIndex - index + 1;
         }

         long expNumber = 0L;
         int exponentIndicatorIndex;
         if ((ch | ' ') == 101) {
            exponentIndicatorIndex = index++;
            ch = charAt(str, index, endIndex);
            boolean isExponentNegative = ch == '-';
            if (isExponentNegative || ch == '+') {
               ch = charAt(str, ++index, endIndex);
            }

            illegal = !FastDoubleSwar.isDigit(ch);

            do {
               if (expNumber < 2147483647L) {
                  expNumber = 10L * expNumber + ch - 48L;
               }

               ch = charAt(str, ++index, endIndex);
            } while (FastDoubleSwar.isDigit(ch));

            if (isExponentNegative) {
               expNumber = -expNumber;
            }

            exponent += expNumber;
         } else {
            exponentIndicatorIndex = endIndex;
         }

         if (illegal || index < endIndex) {
            throw new NumberFormatException("illegal syntax");
         } else if (exponentIndicatorIndex - integerPartIndex == 0) {
            throw new NumberFormatException("illegal syntax");
         } else if (exponent >= -2147483648L && exponent <= 2147483647L && digitCount <= 1292782621) {
            return this.valueOfBigDecimalString(
               str, nonZeroIntegerPartIndex, decimalPointIndex, nonZeroFractionalPartIndex, exponentIndicatorIndex, isNegative, (int)exponent
            );
         } else {
            throw new NumberFormatException("value exceeds limits");
         }
      }
   }

   private BigDecimal valueOfBigDecimalString(
      CharSequence str,
      int integerPartIndex,
      int decimalPointIndex,
      int nonZeroFractionalPartIndex,
      int exponentIndicatorIndex,
      boolean isNegative,
      int exponent
   ) {
      int fractionDigitsCount = exponentIndicatorIndex - decimalPointIndex - 1;
      int nonZeroFractionDigitsCount = exponentIndicatorIndex - nonZeroFractionalPartIndex;
      int integerDigitsCount = decimalPointIndex - integerPartIndex;
      NavigableMap<Integer, BigInteger> powersOfTen = null;
      BigInteger integerPart;
      if (integerDigitsCount > 0) {
         if (integerDigitsCount > 400) {
            powersOfTen = FastIntegerMath.createPowersOfTenFloor16Map();
            FastIntegerMath.fillPowersOfNFloor16Recursive(powersOfTen, integerPartIndex, decimalPointIndex);
            integerPart = ParseDigitsTaskCharSequence.parseDigitsRecursive(str, integerPartIndex, decimalPointIndex, powersOfTen);
         } else {
            integerPart = ParseDigitsTaskCharSequence.parseDigitsRecursive(str, integerPartIndex, decimalPointIndex, null);
         }
      } else {
         integerPart = BigInteger.ZERO;
      }

      BigInteger significand;
      if (fractionDigitsCount > 0) {
         BigInteger fractionalPart;
         if (nonZeroFractionDigitsCount > 400) {
            if (powersOfTen == null) {
               powersOfTen = FastIntegerMath.createPowersOfTenFloor16Map();
            }

            FastIntegerMath.fillPowersOfNFloor16Recursive(powersOfTen, nonZeroFractionalPartIndex, exponentIndicatorIndex);
            fractionalPart = ParseDigitsTaskCharSequence.parseDigitsRecursive(str, nonZeroFractionalPartIndex, exponentIndicatorIndex, powersOfTen);
         } else {
            fractionalPart = ParseDigitsTaskCharSequence.parseDigitsRecursive(str, nonZeroFractionalPartIndex, exponentIndicatorIndex, null);
         }

         if (integerPart.signum() == 0) {
            significand = fractionalPart;
         } else {
            BigInteger integerFactor = FastIntegerMath.computePowerOfTen(powersOfTen, fractionDigitsCount);
            significand = FftMultiplier.multiply(integerPart, integerFactor).add(fractionalPart);
         }
      } else {
         significand = integerPart;
      }

      return new BigDecimal(isNegative ? significand.negate() : significand, -exponent);
   }
}
