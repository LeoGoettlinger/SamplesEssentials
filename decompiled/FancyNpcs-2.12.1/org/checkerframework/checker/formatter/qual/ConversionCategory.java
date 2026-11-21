package org.checkerframework.checker.formatter.qual;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

public enum ConversionCategory {
   GENERAL("bBhHsS", (Class<?>[])null),
   CHAR("cC", Character.class, Byte.class, Short.class, Integer.class),
   INT("doxX", Byte.class, Short.class, Integer.class, Long.class, BigInteger.class),
   FLOAT("eEfgGaA", Float.class, Double.class, BigDecimal.class),
   TIME("tT", Long.class, Calendar.class, Date.class),
   CHAR_AND_INT(null, Byte.class, Short.class, Integer.class),
   INT_AND_TIME(null, Long.class),
   NULL(null),
   UNUSED(null, (Class<?>[])null);

   @Nullable
   public final Class<?>[] types;
   @Nullable
   public final String chars;
   private static final ConversionCategory[] conversionCategoriesWithChar = new ConversionCategory[]{GENERAL, CHAR, INT, FLOAT, TIME};
   private static final ConversionCategory[] conversionCategoriesForIntersect = new ConversionCategory[]{
      CHAR, INT, FLOAT, TIME, CHAR_AND_INT, INT_AND_TIME, NULL
   };
   private static final ConversionCategory[] conversionCategoriesForUnion = new ConversionCategory[]{NULL, CHAR_AND_INT, INT_AND_TIME, CHAR, INT, FLOAT, TIME};

   private ConversionCategory(@Nullable String chars, @Nullable Class<?>... types) {
      this.chars = chars;
      if (types == null) {
         this.types = types;
      } else {
         List<Class<?>> typesWithPrimitives = new ArrayList<>(types.length);

         for (Class<?> type : types) {
            typesWithPrimitives.add(type);
            Class<?> unwrapped = unwrapPrimitive(type);
            if (unwrapped != null) {
               typesWithPrimitives.add(unwrapped);
            }
         }

         this.types = typesWithPrimitives.toArray(new Class[typesWithPrimitives.size()]);
      }
   }

   @Nullable
   private static Class<? extends Object> unwrapPrimitive(Class<?> c) {
      if (c == Byte.class) {
         return byte.class;
      } else if (c == Character.class) {
         return char.class;
      } else if (c == Short.class) {
         return short.class;
      } else if (c == Integer.class) {
         return int.class;
      } else if (c == Long.class) {
         return long.class;
      } else if (c == Float.class) {
         return float.class;
      } else if (c == Double.class) {
         return double.class;
      } else {
         return c == Boolean.class ? boolean.class : null;
      }
   }

   public static ConversionCategory fromConversionChar(char c) {
      for (ConversionCategory v : conversionCategoriesWithChar) {
         if (v.chars.contains(String.valueOf(c))) {
            return v;
         }
      }

      throw new IllegalArgumentException("Bad conversion character " + c);
   }

   private static <E> Set<E> arrayToSet(E[] a) {
      return new HashSet<>(Arrays.asList(a));
   }

   public static boolean isSubsetOf(ConversionCategory a, ConversionCategory b) {
      return intersect(a, b) == a;
   }

   public static ConversionCategory intersect(ConversionCategory a, ConversionCategory b) {
      if (a == UNUSED) {
         return b;
      } else if (b == UNUSED) {
         return a;
      } else if (a == GENERAL) {
         return b;
      } else if (b == GENERAL) {
         return a;
      } else {
         Set<Class<?>> as = arrayToSet(a.types);
         Set<Class<?>> bs = arrayToSet(b.types);
         as.retainAll(bs);

         for (ConversionCategory v : conversionCategoriesForIntersect) {
            Set<Class<?>> vs = arrayToSet(v.types);
            if (vs.equals(as)) {
               return v;
            }
         }

         throw new RuntimeException();
      }
   }

   public static ConversionCategory union(ConversionCategory a, ConversionCategory b) {
      if (a == UNUSED || b == UNUSED) {
         return UNUSED;
      } else if (a != GENERAL && b != GENERAL) {
         if ((a != CHAR_AND_INT || b != INT_AND_TIME) && (a != INT_AND_TIME || b != CHAR_AND_INT)) {
            Set<Class<?>> as = arrayToSet(a.types);
            Set<Class<?>> bs = arrayToSet(b.types);
            as.addAll(bs);

            for (ConversionCategory v : conversionCategoriesForUnion) {
               Set<Class<?>> vs = arrayToSet(v.types);
               if (vs.equals(as)) {
                  return v;
               }
            }

            return GENERAL;
         } else {
            return INT;
         }
      } else {
         return GENERAL;
      }
   }

   public boolean isAssignableFrom(Class<?> argType) {
      if (this.types == null) {
         return true;
      } else if (argType == void.class) {
         return true;
      } else {
         for (Class<?> c : this.types) {
            if (c.isAssignableFrom(argType)) {
               return true;
            }
         }

         return false;
      }
   }

   @Pure
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.name());
      sb.append(" conversion category");
      if (this.types != null && this.types.length != 0) {
         StringJoiner sj = new StringJoiner(", ", "(one of: ", ")");

         for (Class<?> cls : this.types) {
            sj.add(cls.getSimpleName());
         }

         sb.append(" ");
         sb.append(sj);
         return sb.toString();
      } else {
         return sb.toString();
      }
   }
}
