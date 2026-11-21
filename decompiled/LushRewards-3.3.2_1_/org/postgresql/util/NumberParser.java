package org.postgresql.util;

public class NumberParser {
   private static final NumberFormatException FAST_NUMBER_FAILED = new NumberFormatException() {
      @Override
      public Throwable fillInStackTrace() {
         return this;
      }
   };
   private static final long MAX_LONG_DIV_TEN = 922337203685477580L;

   public static long getFastLong(byte[] bytes, long minVal, long maxVal) throws NumberFormatException {
      int len = bytes.length;
      if (len == 0) {
         throw FAST_NUMBER_FAILED;
      } else {
         boolean neg = bytes[0] == 45;
         long val = 0L;
         int start = neg ? 1 : 0;

         while (start < len) {
            byte b = bytes[start++];
            if (b < 48 || b > 57) {
               if (b != 46) {
                  throw FAST_NUMBER_FAILED;
               }

               if (neg && len == 2 || !neg && len == 1) {
                  throw FAST_NUMBER_FAILED;
               }

               while (start < len) {
                  b = bytes[start++];
                  if (b < 48 || b > 57) {
                     throw FAST_NUMBER_FAILED;
                  }
               }
               break;
            }

            if (val > 922337203685477580L) {
               throw FAST_NUMBER_FAILED;
            }

            val *= 10L;
            val += b - 48;
         }

         if (val < 0L) {
            if (!neg || val != Long.MIN_VALUE) {
               throw FAST_NUMBER_FAILED;
            }
         } else if (neg) {
            val = -val;
         }

         if (val >= minVal && val <= maxVal) {
            return val;
         } else {
            throw FAST_NUMBER_FAILED;
         }
      }
   }
}
