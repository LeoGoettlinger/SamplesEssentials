package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.io.doubleparser;

import java.util.Arrays;

abstract class AbstractNumberParser {
   public static final String ILLEGAL_OFFSET_OR_ILLEGAL_LENGTH = "offset < 0 or length > str.length";
   public static final String SYNTAX_ERROR = "illegal syntax";
   public static final String VALUE_EXCEEDS_LIMITS = "value exceeds limits";
   static final byte DECIMAL_POINT_CLASS = -4;
   static final byte OTHER_CLASS = -1;
   static final byte[] CHAR_TO_HEX_MAP = new byte[256];

   protected static byte charAt(byte[] str, int i, int endIndex) {
      return i < endIndex ? str[i] : 0;
   }

   protected static char charAt(char[] str, int i, int endIndex) {
      return i < endIndex ? str[i] : '\u0000';
   }

   protected static char charAt(CharSequence str, int i, int endIndex) {
      return i < endIndex ? str.charAt(i) : '\u0000';
   }

   protected static int lookupHex(byte ch) {
      return CHAR_TO_HEX_MAP[ch & 0xFF];
   }

   protected static int lookupHex(char ch) {
      return ch < 128 ? CHAR_TO_HEX_MAP[ch] : -1;
   }

   static {
      Arrays.fill(CHAR_TO_HEX_MAP, (byte)-1);

      for (char ch = '0'; ch <= '9'; ch++) {
         CHAR_TO_HEX_MAP[ch] = (byte)(ch - '0');
      }

      for (char ch = 'A'; ch <= 'F'; ch++) {
         CHAR_TO_HEX_MAP[ch] = (byte)(ch - 'A' + 10);
      }

      for (char ch = 'a'; ch <= 'f'; ch++) {
         CHAR_TO_HEX_MAP[ch] = (byte)(ch - 'a' + 10);
      }

      for (char ch = '.'; ch <= '.'; ch++) {
         CHAR_TO_HEX_MAP[ch] = -4;
      }
   }
}
