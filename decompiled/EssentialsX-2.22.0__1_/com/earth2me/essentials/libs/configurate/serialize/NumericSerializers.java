package com.earth2me.essentials.libs.configurate.serialize;

import java.util.Locale;
import java.util.function.BiFunction;

final class NumericSerializers {
   private static final float EPSILON = Float.MIN_NORMAL;
   static final ScalarSerializer<Float> FLOAT = TypeSerializer.of(Float.class, (v, pass) -> pass.test(Double.class) ? v.doubleValue() : v.toString(), v -> {
      if (v instanceof Number) {
         double d = ((Number)v).doubleValue();
         if (!canRepresentDoubleAsFloat(d)) {
            throw new SerializationException("Value " + d + " cannot be represented as a float without significant loss of precision");
         } else {
            return (float)d;
         }
      } else if (!(v instanceof CharSequence)) {
         throw new CoercionFailedException(v, "float");
      } else {
         String value = v.toString();
         if (value.endsWith("f") || value.endsWith("F")) {
            value = value.substring(0, value.length() - 1);
         }

         try {
            return Float.parseFloat(value);
         } catch (NumberFormatException var3) {
            throw new SerializationException(var3);
         }
      }
   });
   static final ScalarSerializer<Double> DOUBLE = TypeSerializer.of(Double.class, (v, pass) -> v.toString(), v -> {
      if (v instanceof Number) {
         return ((Number)v).doubleValue();
      } else if (!(v instanceof CharSequence)) {
         throw new CoercionFailedException(v, "double");
      } else {
         String value = v.toString();
         if (value.endsWith("d") || value.endsWith("D")) {
            value = value.substring(0, value.length() - 1);
         }

         try {
            return Double.parseDouble(value);
         } catch (NumberFormatException var3) {
            throw new SerializationException(var3);
         }
      }
   });
   static final ScalarSerializer<Byte> BYTE = TypeSerializer.of(Byte.class, (v, pass) -> {
      if (pass.test(Short.class)) {
         return v.shortValue();
      } else if (pass.test(Integer.class)) {
         return v.intValue();
      } else if (pass.test(Double.class)) {
         return v.doubleValue();
      } else {
         return pass.test(Long.class) ? v.longValue() : v.toString();
      }
   }, value -> {
      if (value instanceof Float || value instanceof Double) {
         double absVal = Math.abs(((Number)value).doubleValue());
         if (absVal - Math.floor(absVal) < Float.MIN_NORMAL && absVal <= 127.0) {
            return (byte)absVal;
         } else {
            throw new CoercionFailedException(value, "byte");
         }
      } else if (value instanceof Number) {
         long full = ((Number)value).longValue();
         if (full <= 127L && full >= -128L) {
            return (byte)full;
         } else {
            throw new SerializationException("Value " + full + " is out of range for a byte ([" + -128 + "," + 127 + "])");
         }
      } else if (value instanceof CharSequence) {
         return parseNumber(value.toString(), Byte::parseByte, Byte::parseByte, "b");
      } else {
         throw new CoercionFailedException(value, "byte");
      }
   });
   static final ScalarSerializer<Short> SHORT = TypeSerializer.of(Short.class, (v, pass) -> {
      if (pass.test(Integer.class)) {
         return v.intValue();
      } else if (pass.test(Long.class)) {
         return v.longValue();
      } else {
         return pass.test(Double.class) ? v.doubleValue() : v.toString();
      }
   }, value -> {
      if (value instanceof Float || value instanceof Double) {
         double absVal = Math.abs(((Number)value).doubleValue());
         if (absVal - Math.floor(absVal) < Float.MIN_NORMAL && absVal <= 32767.0) {
            return (short)absVal;
         } else {
            throw new CoercionFailedException(value, "short");
         }
      } else if (value instanceof Number) {
         long full = ((Number)value).longValue();
         if (full <= 32767L && full >= -32768L) {
            return (short)full;
         } else {
            throw new SerializationException("Value " + full + " is out of range for a short ([" + -32768 + "," + 32767 + "])");
         }
      } else if (value instanceof CharSequence) {
         return parseNumber(value.toString(), Short::parseShort, Short::parseShort, "s");
      } else {
         throw new CoercionFailedException(value, "short");
      }
   });
   static final ScalarSerializer<Integer> INTEGER = TypeSerializer.of(Integer.class, (v, pass) -> {
      if (pass.test(Double.class)) {
         return v.doubleValue();
      } else {
         return pass.test(Long.class) ? v.longValue() : v.toString();
      }
   }, value -> {
      if (value instanceof Float || value instanceof Double) {
         double absVal = Math.abs(((Number)value).doubleValue());
         if (absVal - Math.floor(absVal) < Float.MIN_NORMAL && absVal <= 2.147483647E9) {
            return (int)absVal;
         } else {
            throw new CoercionFailedException(value, "int");
         }
      } else if (value instanceof Number) {
         long full = ((Number)value).longValue();
         if (full <= 2147483647L && full >= -2147483648L) {
            return (int)full;
         } else {
            throw new SerializationException("Value " + full + " is out of range for an integer ([" + Integer.MIN_VALUE + "," + Integer.MAX_VALUE + "])");
         }
      } else if (value instanceof CharSequence) {
         return parseNumber(value.toString(), Integer::parseInt, Integer::parseUnsignedInt, "i");
      } else {
         throw new CoercionFailedException(value, "int");
      }
   });
   static final ScalarSerializer<Long> LONG = TypeSerializer.of(Long.class, (v, pass) -> v.toString(), value -> {
      if (value instanceof Float || value instanceof Double) {
         double absVal = Math.abs(((Number)value).doubleValue());
         if (absVal - Math.floor(absVal) < Float.MIN_NORMAL && absVal <= 9.223372E18F) {
            return (long)absVal;
         } else {
            throw new CoercionFailedException(value, "long");
         }
      } else if (value instanceof Number) {
         return ((Number)value).longValue();
      } else if (value instanceof CharSequence) {
         return parseNumber(value.toString(), Long::parseLong, Long::parseUnsignedLong, "l");
      } else {
         throw new CoercionFailedException(value, "long");
      }
   });

   private NumericSerializers() {
   }

   private static boolean canRepresentDoubleAsFloat(final double test) {
      if (test != 0.0 && test != -0.0 && Double.isFinite(test)) {
         int exponent = Math.getExponent(test);
         return exponent >= -126 && exponent <= 127;
      } else {
         return true;
      }
   }

   static <T extends Number> T parseNumber(
      String input, final BiFunction<String, Integer, T> parseFunc, final BiFunction<String, Integer, T> unsignedParseFunc, final String suffix
   ) throws SerializationException {
      boolean unsigned = false;
      boolean negative = false;
      int startIdx = 0;
      int endIdx = input.length();
      if (input.endsWith(suffix) || input.endsWith(suffix.toUpperCase(Locale.ROOT))) {
         endIdx--;
      }

      if (endIdx > 0 && input.charAt(endIdx - 1) == 'u') {
         unsigned = true;
         endIdx--;
      }

      if (endIdx > startIdx && input.charAt(startIdx) == '-') {
         if (unsigned) {
            throw new SerializationException("Negative numbers cannot be unsigned! (both - prefix and u suffix were used)");
         }

         negative = true;
         startIdx++;
      } else if (endIdx > startIdx && input.charAt(startIdx) == '+') {
         startIdx++;
      }

      int radix = 10;
      if (input.startsWith("0x", startIdx)) {
         radix = 16;
         startIdx += 2;
      } else if (input.length() > startIdx && input.charAt(startIdx) == '#') {
         radix = 16;
         startIdx++;
      } else if (input.startsWith("0b", startIdx)) {
         radix = 2;
         startIdx += 2;
      }

      input = input.substring(startIdx, endIdx);
      if (negative) {
         input = "-" + input;
      }

      try {
         return (unsigned ? unsignedParseFunc : parseFunc).apply(input, radix);
      } catch (IllegalArgumentException var10) {
         throw new SerializationException(var10);
      }
   }
}
