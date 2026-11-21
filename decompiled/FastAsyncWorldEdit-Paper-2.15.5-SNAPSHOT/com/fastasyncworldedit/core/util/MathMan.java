package com.fastasyncworldedit.core.util;

public class MathMan {
   public static int usqrt(int a) {
      return a < 65536 ? CachedMathMan.usqrt(a) : (int)Math.round(Math.sqrt(a));
   }

   public static float sinInexact(double paramFloat) {
      return CachedMathMan.sinInexact(paramFloat);
   }

   public static float cosInexact(double paramFloat) {
      return CachedMathMan.cosInexact(paramFloat);
   }

   public static int log2nlz(int bits) {
      return 32 - Integer.numberOfLeadingZeros(bits);
   }

   public static int floorZero(double d0) {
      int i = (int)d0;
      return d0 < i ? i - 1 : i;
   }

   public static double max(double... values) {
      double max = Double.MIN_VALUE;

      for (double d : values) {
         if (d > max) {
            max = d;
         }
      }

      return max;
   }

   public static int max(int... values) {
      int max = Integer.MIN_VALUE;

      for (int d : values) {
         if (d > max) {
            max = d;
         }
      }

      return max;
   }

   public static int min(int... values) {
      return Math.min(Math.min(values[0], values[1]), values[2]);
   }

   public static double min(double... values) {
      return Math.min(Math.min(values[0], values[1]), values[2]);
   }

   public static int ceilZero(float floatNumber) {
      int floor = (int)floatNumber;
      return floatNumber > floor ? floor + 1 : floor;
   }

   public static int sqr(int val) {
      return val * val;
   }

   public static int clamp(int check, int min, int max) {
      return Math.clamp((long)check, min, max);
   }

   public static float clamp(float check, float min, float max) {
      return Math.clamp(check, min, max);
   }

   public static double hypot(final double... pars) {
      double sum = 0.0;

      for (double d : pars) {
         sum += Math.pow(d, 2.0);
      }

      return Math.sqrt(sum);
   }

   public static double hypot2(final double... pars) {
      double sum = 0.0;

      for (double d : pars) {
         sum += Math.pow(d, 2.0);
      }

      return sum;
   }

   public static int wrap(int value, int min, int max) {
      if (max < min) {
         return value;
      } else if (min == max) {
         return min;
      } else {
         int diff = max - min + 1;
         if (value < min) {
            return max - (min - value) % diff;
         } else {
            return value > max ? min + (value - min) % diff : value;
         }
      }
   }

   public static long inverseRound(double val) {
      long round = Math.round(val);
      return (long)(round + Math.signum(val - round));
   }

   public static int pair(short x, short y) {
      return x << 16 | y & 65535;
   }

   public static short unpairX(int hash) {
      return (short)(hash >> 16);
   }

   public static short unpairY(int hash) {
      return (short)(hash & 65535);
   }

   public static short pairByte(int a, int b) {
      return (short)(a << 8 | b & 0xFF);
   }

   public static byte unpairShortX(short pair) {
      return (byte)(pair >> 8);
   }

   public static byte unpairShortY(short pair) {
      return (byte)pair;
   }

   public static long pairInt(int a, int b) {
      return (long)a << 32 | b & 4294967295L;
   }

   public static long tripleWorldCoord(int x, int y, int z) {
      return (y + 256 & 65535) + ((x & 16777215L) << 16) + ((z & 16777215L) << 40);
   }

   public static long untripleWorldCoordX(long triple) {
      return (triple >> 16 & 16777215L) << 38 >> 38;
   }

   public static long untripleWorldCoordY(long triple) {
      return (triple & 65535L) - 256L;
   }

   public static long untripleWorldCoordZ(long triple) {
      return (triple >> 40 & 16777215L) << 38 >> 38;
   }

   public static int tripleBlockCoord(int x, int y, int z) {
      if (y >= -2048 && y <= 2047) {
         y += 2048;
         return (x & 15) << 16 | (z & 15) << 12 | y;
      } else {
         throw new UnsupportedOperationException("TripleBlockCoord Y value cannot be outside range -2048 <= y <= 2047");
      }
   }

   public static char tripleBlockCoordChar(int x, int y, int z) {
      return (char)((x & 15) << 16 | (z & 15) << 12 | y);
   }

   public static int untripleBlockCoordX(int triple) {
      return triple >> 16 & 15;
   }

   public static int untripleBlockCoordY(int triple) {
      return (triple & 4095) - 2048;
   }

   public static int untripleBlockCoordZ(int triple) {
      return triple >> 12 & 15;
   }

   public static int tripleSearchCoords(int x, int y, int z) {
      if (x <= 1023 && x >= -1024 && y <= 255 && y >= -256 && z <= 1023 && z >= -1024) {
         int b1 = Math.abs(y) & 0xFF;
         int b3 = x & 0xFF;
         int b4 = z & 0xFF;
         int x16 = x >> 8 & 3 | (x < 0 ? 4 : 0);
         int z16 = z >> 8 & 3 | (z < 0 ? 4 : 0);
         int y16 = y < 0 ? 1 : 0;
         int b2 = x16 + (z16 << 3) + (y16 << 6);
         return b1 | b2 << 8 | b3 << 15 | b4 << 23;
      } else {
         throw new UnsupportedOperationException(String.format("Check range on x=%s, y=%s and z=%s!", x, y, z));
      }
   }

   public static int pairSearchCoords(int x, int y) {
      byte b1 = (byte)((x & 15) + ((y & 15) << 4));
      byte b2 = (byte)(x >> 4 & 0xFF);
      byte b3 = (byte)(y >> 4 & 0xFF);
      int x16 = x >> 12 & 15;
      int y16 = y >> 12 & 15;
      byte b4 = (byte)((x16 & 15) + ((y16 & 15) << 4));
      return (b1 & 0xFF) + ((b2 & 0xFF) << 8) + ((b3 & 0xFF) << 16) + ((b4 & 0xFF) << 24);
   }

   public static int unpairSearchCoordsX(int pair) {
      int x1 = pair >> 24 & 7;
      int x2 = pair >> 8 & 0xFF;
      int x3 = pair & 15;
      return x3 + (x2 << 4) + (x1 << 12);
   }

   public static int unpairSearchCoordsY(int pair) {
      int y1 = (pair >> 24 & 127) >> 3;
      int y2 = pair >> 16 & 0xFF;
      int y3 = (pair & 0xFF) >> 4;
      return y3 + (y2 << 4) + (y1 << 12);
   }

   public static long chunkXZ2Int(int x, int z) {
      return x & 4294967295L | (z & 4294967295L) << 32;
   }

   public static int unpairIntX(long pair) {
      return (int)(pair >> 32);
   }

   public static int unpairIntY(long pair) {
      return (int)pair;
   }

   public static byte pair16(int x, int y) {
      return (byte)(x + (y << 4));
   }

   public static byte unpair16x(byte value) {
      return (byte)(value & 15);
   }

   public static byte unpair16y(byte value) {
      return (byte)(value >> 4 & 15);
   }

   public static byte pair8(int x, int y) {
      return (byte)(x + (y << 3));
   }

   public static byte unpair8x(int value) {
      return (byte)(value & 7);
   }

   public static byte unpair8y(int value) {
      return (byte)(value >> 3 & 127);
   }

   public static int lossyFastDivide(int a, int b) {
      return a * (65536 / b) >> 16;
   }

   public static int gcd(int a, int b) {
      return b == 0 ? a : gcd(b, a % b);
   }

   public static int gcd(int[] a) {
      int result = a[0];

      for (int i = 1; i < a.length; i++) {
         result = gcd(result, a[i]);
      }

      return result;
   }

   public static double getMean(int[] array) {
      double count = 0.0;

      for (int i : array) {
         count += i;
      }

      return count / array.length;
   }

   public static double getMean(double[] array) {
      double count = 0.0;

      for (double i : array) {
         count += i;
      }

      return count / array.length;
   }

   public static float[] getDirection(float yaw, float pitch) {
      double pitch_sin = Math.sin(pitch);
      return new float[]{(float)(pitch_sin * Math.cos(yaw)), (float)(pitch_sin * Math.sin(yaw)), (float)Math.cos(pitch)};
   }

   public static int roundInt(double value) {
      return (int)(value < 0.0 ? (value == (int)value ? value : value - 1.0) : value);
   }

   public static float[] getPitchAndYaw(float x, float y, float z) {
      float distance = sqrtApprox(z * z + x * x);
      return new float[]{atan2(y, distance), atan2(x, z)};
   }

   public static float atan2(float i, float j) {
      return CachedMathMan.atan2(i, j);
   }

   public static float sqrtApprox(float f) {
      return f * Float.intBitsToFloat(1597463174 - (Float.floatToIntBits(f) >> 1));
   }

   public static double sqrtApprox(double d) {
      return Double.longBitsToDouble((Double.doubleToLongBits(d) - 4503599627370496L >> 1) + 2305843009213693952L);
   }

   public static float invSqrt(float x) {
      float xhalf = 0.5F * x;
      int i = Float.floatToIntBits(x);
      i = 1597463007 - (i >> 1);
      x = Float.intBitsToFloat(i);
      return x * (1.5F - xhalf * x * x);
   }

   public static boolean isInteger(CharSequence str) {
      if (str == null) {
         return false;
      } else {
         int length = str.length();
         if (length == 0) {
            return false;
         } else {
            int i = 0;
            if (str.charAt(0) == '-') {
               if (length == 1) {
                  return false;
               }

               if (length > 11) {
                  return false;
               }

               if (length == 11 && (str.charAt(0) > '2' || str.charAt(0) < '0') && (str.charAt(1) > '1' || str.charAt(1) < '0')) {
                  return false;
               }

               i = 1;
            } else {
               if (length > 10) {
                  return false;
               }

               if (length == 10 && (str.charAt(0) > '2' || str.charAt(0) < '0') && (str.charAt(1) > '1' || str.charAt(1) < '0')) {
                  return false;
               }
            }

            while (i < length) {
               char c = str.charAt(i);
               if (c <= '/' || c >= ':') {
                  return false;
               }

               i++;
            }

            return true;
         }
      }
   }

   public static double getSD(double[] array, double av) {
      double sd = 0.0;

      for (double element : array) {
         sd += Math.pow(Math.abs(element - av), 2.0);
      }

      return Math.sqrt(sd / array.length);
   }

   public static double getSD(int[] array, double av) {
      double sd = 0.0;

      for (int element : array) {
         sd += Math.pow(Math.abs(element - av), 2.0);
      }

      return Math.sqrt(sd / array.length);
   }

   public static int absByte(int value) {
      return (value ^ value >> 8) - (value >> 8);
   }

   public static int mod(int a, int b) {
      return isPowerOfTwo(b) ? a & b - 1 : a % b;
   }

   public static int unsignedMod(int a, int b) {
      return isPowerOfTwo(b) ? a & b - 1 : a % b;
   }

   public static boolean isPowerOfTwo(int a) {
      return (a & a - 1) == 0;
   }

   public static int longArrayLength(int bitsPerEntry, int numberOfEntries) {
      int entriesPerLong = 64 / bitsPerEntry;
      return Math.ceilDiv(numberOfEntries, entriesPerLong);
   }
}
