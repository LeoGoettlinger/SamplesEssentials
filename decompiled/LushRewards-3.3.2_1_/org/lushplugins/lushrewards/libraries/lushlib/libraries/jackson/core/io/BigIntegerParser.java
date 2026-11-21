package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.io;

import java.math.BigInteger;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.io.doubleparser.JavaBigIntegerParser;

public final class BigIntegerParser {
   private BigIntegerParser() {
   }

   public static BigInteger parseWithFastParser(String valueStr) {
      try {
         return JavaBigIntegerParser.parseBigInteger(valueStr);
      } catch (NumberFormatException var3) {
         String reportNum = valueStr.length() <= 1000 ? valueStr : valueStr.substring(0, 1000) + " [truncated]";
         throw new NumberFormatException("Value \"" + reportNum + "\" can not be represented as `java.math.BigInteger`, reason: " + var3.getMessage());
      }
   }

   public static BigInteger parseWithFastParser(String valueStr, int radix) {
      try {
         return JavaBigIntegerParser.parseBigInteger(valueStr, radix);
      } catch (NumberFormatException var4) {
         String reportNum = valueStr.length() <= 1000 ? valueStr : valueStr.substring(0, 1000) + " [truncated]";
         throw new NumberFormatException(
            "Value \"" + reportNum + "\" can not be represented as `java.math.BigInteger` with radix " + radix + ", reason: " + var4.getMessage()
         );
      }
   }
}
