package org.postgresql.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public class ByteConverter {
   private static final int NUMERIC_DSCALE_MASK = 16383;
   private static final short NUMERIC_POS = 0;
   private static final short NUMERIC_NEG = 16384;
   private static final short NUMERIC_NAN = -16384;
   private static final int SHORT_BYTES = 2;
   private static final int LONG_BYTES = 4;
   private static final int[] INT_TEN_POWERS = new int[6];
   private static final long[] LONG_TEN_POWERS = new long[19];
   private static final BigInteger[] BI_TEN_POWERS = new BigInteger[32];
   private static final BigInteger BI_TEN_THOUSAND = BigInteger.valueOf(10000L);
   private static final BigInteger BI_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

   private ByteConverter() {
   }

   public static int bytesToInt(byte[] bytes) {
      if (bytes.length == 1) {
         return bytes[0];
      } else if (bytes.length == 2) {
         return int2(bytes, 0);
      } else if (bytes.length == 4) {
         return int4(bytes, 0);
      } else {
         throw new IllegalArgumentException("Argument bytes is empty");
      }
   }

   public static Number numeric(byte[] bytes) {
      return numeric(bytes, 0, bytes.length);
   }

   public static Number numeric(byte[] bytes, int pos, int numBytes) {
      if (numBytes < 8) {
         throw new IllegalArgumentException("number of bytes should be at-least 8");
      } else {
         int len = int2(bytes, pos) & '\uffff';
         short weight = int2(bytes, pos + 2);
         short sign = int2(bytes, pos + 4);
         short scale = int2(bytes, pos + 6);
         if (numBytes != len * 2 + 8) {
            throw new IllegalArgumentException("invalid length of bytes \"numeric\" value");
         } else if (sign != 0 && sign != 16384 && sign != -16384) {
            throw new IllegalArgumentException("invalid sign in \"numeric\" value");
         } else if (sign == -16384) {
            return Double.NaN;
         } else if ((scale & 16383) != scale) {
            throw new IllegalArgumentException("invalid scale in \"numeric\" value");
         } else if (len == 0) {
            return new BigDecimal(BigInteger.ZERO, scale);
         } else {
            int idx = pos + 8;
            short d = int2(bytes, idx);
            if (weight >= 0) {
               if (scale == 0) {
                  BigInteger unscaledBI = null;
                  long unscaledInt = d;

                  for (int i = 1; i < len; i++) {
                     if (i == 4) {
                        unscaledBI = BigInteger.valueOf(unscaledInt);
                     }

                     idx += 2;
                     d = int2(bytes, idx);
                     if (unscaledBI == null) {
                        unscaledInt *= 10000L;
                        unscaledInt += d;
                     } else {
                        unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                        if (d != 0) {
                           unscaledBI = unscaledBI.add(BigInteger.valueOf(d));
                        }
                     }
                  }

                  if (unscaledBI == null) {
                     unscaledBI = BigInteger.valueOf(unscaledInt);
                  }

                  if (sign == 16384) {
                     unscaledBI = unscaledBI.negate();
                  }

                  int bigDecScale = (len - (weight + 1)) * 4;
                  return bigDecScale == 0 ? new BigDecimal(unscaledBI) : new BigDecimal(unscaledBI, bigDecScale).setScale(0);
               } else {
                  BigInteger unscaledBI = null;
                  long unscaledInt = d;
                  int effectiveWeight = weight;
                  int effectiveScale = scale;

                  for (int i = 1; i < len; i++) {
                     if (i == 4) {
                        unscaledBI = BigInteger.valueOf(unscaledInt);
                     }

                     idx += 2;
                     d = int2(bytes, idx);
                     if (effectiveWeight > 0) {
                        effectiveWeight--;
                        if (unscaledBI == null) {
                           unscaledInt *= 10000L;
                        } else {
                           unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                        }
                     } else if (effectiveScale >= 4) {
                        effectiveScale -= 4;
                        if (unscaledBI == null) {
                           unscaledInt *= 10000L;
                        } else {
                           unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                        }
                     } else {
                        if (unscaledBI == null) {
                           unscaledInt *= INT_TEN_POWERS[effectiveScale];
                        } else {
                           unscaledBI = unscaledBI.multiply(tenPower(effectiveScale));
                        }

                        d = (short)(d / INT_TEN_POWERS[4 - effectiveScale]);
                        effectiveScale = 0;
                     }

                     if (unscaledBI == null) {
                        unscaledInt += d;
                     } else if (d != 0) {
                        unscaledBI = unscaledBI.add(BigInteger.valueOf(d));
                     }
                  }

                  if (unscaledBI == null) {
                     unscaledBI = BigInteger.valueOf(unscaledInt);
                  }

                  if (effectiveWeight > 0) {
                     unscaledBI = unscaledBI.multiply(tenPower(effectiveWeight * 4));
                  }

                  if (effectiveScale > 0) {
                     unscaledBI = unscaledBI.multiply(tenPower(effectiveScale));
                  }

                  if (sign == 16384) {
                     unscaledBI = unscaledBI.negate();
                  }

                  return new BigDecimal(unscaledBI, scale);
               }
            } else {
               assert scale > 0;

               int effectiveScale = scale;
               if (++weight < 0) {
                  effectiveScale = scale + 4 * weight;
               }

               int i;
               for (i = 1; i < len && d == 0; i++) {
                  effectiveScale -= 4;
                  idx += 2;
                  d = int2(bytes, idx);
               }

               assert effectiveScale > 0;

               if (effectiveScale >= 4) {
                  effectiveScale -= 4;
               } else {
                  d = (short)(d / INT_TEN_POWERS[4 - effectiveScale]);
                  effectiveScale = 0;
               }

               BigInteger unscaledBI = null;

               long unscaledInt;
               for (unscaledInt = d; i < len; i++) {
                  if (i == 4 && effectiveScale > 2) {
                     unscaledBI = BigInteger.valueOf(unscaledInt);
                  }

                  idx += 2;
                  d = int2(bytes, idx);
                  if (effectiveScale >= 4) {
                     if (unscaledBI == null) {
                        unscaledInt *= 10000L;
                     } else {
                        unscaledBI = unscaledBI.multiply(BI_TEN_THOUSAND);
                     }

                     effectiveScale -= 4;
                  } else {
                     if (unscaledBI == null) {
                        unscaledInt *= INT_TEN_POWERS[effectiveScale];
                     } else {
                        unscaledBI = unscaledBI.multiply(tenPower(effectiveScale));
                     }

                     d = (short)(d / INT_TEN_POWERS[4 - effectiveScale]);
                     effectiveScale = 0;
                  }

                  if (unscaledBI == null) {
                     unscaledInt += d;
                  } else if (d != 0) {
                     unscaledBI = unscaledBI.add(BigInteger.valueOf(d));
                  }
               }

               if (unscaledBI == null) {
                  unscaledBI = BigInteger.valueOf(unscaledInt);
               }

               if (effectiveScale > 0) {
                  unscaledBI = unscaledBI.multiply(tenPower(effectiveScale));
               }

               if (sign == 16384) {
                  unscaledBI = unscaledBI.negate();
               }

               return new BigDecimal(unscaledBI, scale);
            }
         }
      }
   }

   public static byte[] numeric(BigDecimal nbr) {
      ByteConverter.PositiveShorts shorts = new ByteConverter.PositiveShorts();
      BigInteger unscaled = nbr.unscaledValue().abs();
      int scale = nbr.scale();
      if (unscaled.equals(BigInteger.ZERO)) {
         byte[] bytes = new byte[]{0, 0, -1, -1, 0, 0, 0, 0};
         int2(bytes, 6, Math.max(0, scale));
         return bytes;
      } else {
         int weight = -1;
         if (scale <= 0) {
            if (scale < 0) {
               scale = Math.abs(scale);
               weight += scale / 4;
               int mod = scale % 4;
               unscaled = unscaled.multiply(tenPower(mod));
               scale = 0;
            }

            for (; unscaled.compareTo(BI_MAX_LONG) > 0; weight++) {
               BigInteger[] pair = unscaled.divideAndRemainder(BI_TEN_THOUSAND);
               unscaled = pair[0];
               short shortValue = pair[1].shortValue();
               if (shortValue != 0 || !shorts.isEmpty()) {
                  shorts.push(shortValue);
               }
            }

            long unscaledLong = unscaled.longValueExact();

            do {
               short shortValue = (short)(unscaledLong % 10000L);
               if (shortValue != 0 || !shorts.isEmpty()) {
                  shorts.push(shortValue);
               }

               unscaledLong /= 10000L;
               weight++;
            } while (unscaledLong != 0L);
         } else {
            BigInteger[] split = unscaled.divideAndRemainder(tenPower(scale));
            BigInteger decimal = split[1];
            BigInteger wholes = split[0];
            weight = -1;
            if (!BigInteger.ZERO.equals(decimal)) {
               int mod = scale % 4;
               int segments = scale / 4;
               if (mod != 0) {
                  decimal = decimal.multiply(tenPower(4 - mod));
                  segments++;
               }

               do {
                  BigInteger[] pair = decimal.divideAndRemainder(BI_TEN_THOUSAND);
                  decimal = pair[0];
                  short shortValue = pair[1].shortValue();
                  if (shortValue != 0 || !shorts.isEmpty()) {
                     shorts.push(shortValue);
                  }

                  segments--;
               } while (!BigInteger.ZERO.equals(decimal));

               if (BigInteger.ZERO.equals(wholes)) {
                  weight -= segments;
               } else {
                  for (int i = 0; i < segments; i++) {
                     shorts.push((short)0);
                  }
               }
            }

            while (!BigInteger.ZERO.equals(wholes)) {
               weight++;
               BigInteger[] pair = wholes.divideAndRemainder(BI_TEN_THOUSAND);
               wholes = pair[0];
               short shortValue = pair[1].shortValue();
               if (shortValue != 0 || !shorts.isEmpty()) {
                  shorts.push(shortValue);
               }
            }
         }

         byte[] bytes = new byte[8 + 2 * shorts.size()];
         int idx = 0;
         int2(bytes, idx, shorts.size());
         idx += 2;
         int2(bytes, idx, weight);
         idx += 2;
         int2(bytes, idx, nbr.signum() == -1 ? 16384 : 0);
         idx += 2;
         int2(bytes, idx, Math.max(0, scale));

         short s;
         for (int var23 = idx + 2; (s = shorts.pop()) != -1; var23 += 2) {
            int2(bytes, var23, s);
         }

         return bytes;
      }
   }

   private static BigInteger tenPower(int exponent) {
      return BI_TEN_POWERS.length > exponent ? BI_TEN_POWERS[exponent] : BigInteger.TEN.pow(exponent);
   }

   public static long int8(byte[] bytes, int idx) {
      return ((long)(bytes[idx + 0] & 255) << 56)
         + ((long)(bytes[idx + 1] & 255) << 48)
         + ((long)(bytes[idx + 2] & 255) << 40)
         + ((long)(bytes[idx + 3] & 255) << 32)
         + ((long)(bytes[idx + 4] & 255) << 24)
         + ((long)(bytes[idx + 5] & 255) << 16)
         + ((long)(bytes[idx + 6] & 255) << 8)
         + (bytes[idx + 7] & 255);
   }

   public static int int4(byte[] bytes, int idx) {
      return ((bytes[idx] & 0xFF) << 24) + ((bytes[idx + 1] & 0xFF) << 16) + ((bytes[idx + 2] & 0xFF) << 8) + (bytes[idx + 3] & 0xFF);
   }

   public static short int2(byte[] bytes, int idx) {
      return (short)(((bytes[idx] & 255) << 8) + (bytes[idx + 1] & 255));
   }

   public static boolean bool(byte[] bytes, int idx) {
      return bytes[idx] == 1;
   }

   public static float float4(byte[] bytes, int idx) {
      return Float.intBitsToFloat(int4(bytes, idx));
   }

   public static double float8(byte[] bytes, int idx) {
      return Double.longBitsToDouble(int8(bytes, idx));
   }

   public static void int8(byte[] target, int idx, long value) {
      target[idx + 0] = (byte)(value >>> 56);
      target[idx + 1] = (byte)(value >>> 48);
      target[idx + 2] = (byte)(value >>> 40);
      target[idx + 3] = (byte)(value >>> 32);
      target[idx + 4] = (byte)(value >>> 24);
      target[idx + 5] = (byte)(value >>> 16);
      target[idx + 6] = (byte)(value >>> 8);
      target[idx + 7] = (byte)value;
   }

   public static void int4(byte[] target, int idx, int value) {
      target[idx + 0] = (byte)(value >>> 24);
      target[idx + 1] = (byte)(value >>> 16);
      target[idx + 2] = (byte)(value >>> 8);
      target[idx + 3] = (byte)value;
   }

   public static void int2(byte[] target, int idx, int value) {
      target[idx + 0] = (byte)(value >>> 8);
      target[idx + 1] = (byte)value;
   }

   public static void bool(byte[] target, int idx, boolean value) {
      target[idx] = (byte)(value ? 1 : 0);
   }

   public static void float4(byte[] target, int idx, float value) {
      int4(target, idx, Float.floatToRawIntBits(value));
   }

   public static void float8(byte[] target, int idx, double value) {
      int8(target, idx, Double.doubleToRawLongBits(value));
   }

   static {
      for (int i = 0; i < INT_TEN_POWERS.length; i++) {
         INT_TEN_POWERS[i] = (int)Math.pow(10.0, i);
      }

      for (int i = 0; i < LONG_TEN_POWERS.length; i++) {
         LONG_TEN_POWERS[i] = (long)Math.pow(10.0, i);
      }

      for (int i = 0; i < BI_TEN_POWERS.length; i++) {
         BI_TEN_POWERS[i] = BigInteger.TEN.pow(i);
      }
   }

   private static final class PositiveShorts {
      private short[] shorts = new short[8];
      private int idx;

      PositiveShorts() {
      }

      public void push(short s) {
         if (s < 0) {
            throw new IllegalArgumentException("only non-negative values accepted: " + s);
         } else {
            if (this.idx == this.shorts.length) {
               this.grow();
            }

            this.shorts[this.idx++] = s;
         }
      }

      public int size() {
         return this.idx;
      }

      public boolean isEmpty() {
         return this.idx == 0;
      }

      public short pop() {
         return this.idx > 0 ? this.shorts[--this.idx] : -1;
      }

      private void grow() {
         int newSize = this.shorts.length <= 1024 ? this.shorts.length << 1 : (int)(this.shorts.length * 1.5);
         this.shorts = Arrays.copyOf(this.shorts, newSize);
      }
   }
}
