package org.postgresql.util;

import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.PolyNull;

public class PGbytea {
   private static final int MAX_3_BUFF_SIZE = 2097152;
   private static final int[] HEX_VALS = new int[55];

   @PolyNull
   public static byte[] toBytes(@PolyNull byte[] s) throws SQLException {
      if (s == null) {
         return null;
      } else {
         return s.length >= 2 && s[0] == 92 && s[1] == 120 ? toBytesHexEscaped(s) : toBytesOctalEscaped(s);
      }
   }

   private static byte[] toBytesHexEscaped(byte[] s) {
      int realLength = s.length - 2;
      byte[] output = new byte[realLength >>> 1];

      for (int i = 0; i < realLength; i += 2) {
         int val = getHex(s[2 + i]) << 4;
         val |= getHex(s[3 + i]);
         output[i >>> 1] = (byte)val;
      }

      return output;
   }

   private static int getHex(byte b) {
      return HEX_VALS[b - 48];
   }

   private static byte[] toBytesOctalEscaped(byte[] s) {
      int slength = s.length;
      byte[] buf = null;
      int correctSize = slength;
      if (slength > 2097152) {
         for (int i = 0; i < slength; i++) {
            byte current = s[i];
            if (current == 92) {
               byte next = s[++i];
               if (next == 92) {
                  correctSize--;
               } else {
                  correctSize -= 3;
               }
            }
         }

         buf = new byte[correctSize];
      } else {
         buf = new byte[slength];
      }

      int bufpos = 0;

      for (int ix = 0; ix < slength; ix++) {
         byte nextbyte = s[ix];
         if (nextbyte == 92) {
            byte secondbyte = s[++ix];
            if (secondbyte == 92) {
               buf[bufpos++] = 92;
            } else {
               int var10000 = (secondbyte - 48) * 64;
               ix++;
               var10000 += (s[ix] - 48) * 8;
               ix++;
               int thebyte = var10000 + (s[ix] - 48);
               if (thebyte > 127) {
                  thebyte -= 256;
               }

               buf[bufpos++] = (byte)thebyte;
            }
         } else {
            buf[bufpos++] = nextbyte;
         }
      }

      if (bufpos == correctSize) {
         return buf;
      } else {
         byte[] result = new byte[bufpos];
         System.arraycopy(buf, 0, result, 0, bufpos);
         return result;
      }
   }

   @PolyNull
   public static String toPGString(@PolyNull byte[] buf) {
      if (buf == null) {
         return null;
      } else {
         StringBuilder stringBuilder = new StringBuilder(2 * buf.length);

         for (byte element : buf) {
            int elementAsInt = element;
            if (element < 0) {
               elementAsInt = 256 + element;
            }

            if (elementAsInt < 32 || elementAsInt > 126) {
               stringBuilder.append("\\");
               stringBuilder.append((char)((elementAsInt >> 6 & 3) + 48));
               stringBuilder.append((char)((elementAsInt >> 3 & 7) + 48));
               stringBuilder.append((char)((elementAsInt & 7) + 48));
            } else if (element == 92) {
               stringBuilder.append("\\\\");
            } else {
               stringBuilder.append((char)element);
            }
         }

         return stringBuilder.toString();
      }
   }

   static {
      for (int i = 0; i < 10; i++) {
         HEX_VALS[i] = (byte)i;
      }

      for (int i = 0; i < 6; i++) {
         HEX_VALS[65 + i - 48] = (byte)(10 + i);
         HEX_VALS[97 + i - 48] = (byte)(10 + i);
      }
   }
}
