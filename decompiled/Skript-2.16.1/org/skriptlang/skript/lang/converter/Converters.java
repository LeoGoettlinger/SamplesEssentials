package org.skriptlang.skript.lang.converter;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.util.Pair;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public final class Converters {
   private static final List<ConverterInfo<?, ?>> CONVERTERS = new ArrayList<>(50);
   private static final Map<Pair<Class<?>, Class<?>>, ConverterInfo<?, ?>> QUICK_ACCESS_CONVERTERS = new HashMap<>(50);

   private Converters() {
   }

   @Unmodifiable
   public static List<ConverterInfo<?, ?>> getConverterInfos() {
      assertIsDoneLoading();
      return Collections.unmodifiableList(CONVERTERS);
   }

   public static <F, T> void registerConverter(Class<F> fromType, Class<T> toType, Converter<F, T> converter) {
      registerConverter(fromType, toType, converter, 0);
   }

   public static <F, T> void registerConverter(Class<F> fromType, Class<T> toType, Converter<F, T> converter, int flags) {
      Skript.checkAcceptRegistrations();
      ConverterInfo<F, T> info = new ConverterInfo<>(fromType, toType, converter, flags);
      synchronized (CONVERTERS) {
         if (exactConverterExists_i(fromType, toType)) {
            throw new SkriptAPIException("A Converter from '" + fromType + "' to '" + toType + "' already exists!");
         } else {
            CONVERTERS.add(info);
         }
      }
   }

   public static <F, M, T> void createChainedConverters() {
      Skript.checkAcceptRegistrations();
      synchronized (CONVERTERS) {
         for (int i = 0; i < CONVERTERS.size(); i++) {
            ConverterInfo<?, ?> unknownInfo1 = CONVERTERS.get(i);

            for (int j = 0; j < CONVERTERS.size(); j++) {
               ConverterInfo<?, ?> unknownInfo2 = CONVERTERS.get(j);
               if (unknownInfo2.getFrom() != Object.class
                  && unknownInfo1.getFrom() != unknownInfo2.getTo()
                  && (unknownInfo1.getFlags() & 2) == 0
                  && (unknownInfo2.getFlags() & 1) == 0
                  && unknownInfo2.getFrom().isAssignableFrom(unknownInfo1.getTo())
                  && !exactConverterExists_i(unknownInfo1.getFrom(), unknownInfo2.getTo())) {
                  CONVERTERS.add(
                     new ConverterInfo<>(
                        unknownInfo1.getFrom(),
                        unknownInfo2.getTo(),
                        new ChainedConverter<>(unknownInfo1, unknownInfo2),
                        unknownInfo1.getFlags() | unknownInfo2.getFlags()
                     )
                  );
               } else if (unknownInfo1.getFrom() != Object.class
                  && unknownInfo2.getFrom() != unknownInfo1.getTo()
                  && (unknownInfo1.getFlags() & 1) == 0
                  && (unknownInfo2.getFlags() & 2) == 0
                  && unknownInfo1.getFrom().isAssignableFrom(unknownInfo2.getTo())
                  && !exactConverterExists_i(unknownInfo2.getFrom(), unknownInfo1.getTo())) {
                  CONVERTERS.add(
                     new ConverterInfo<>(
                        unknownInfo2.getFrom(),
                        unknownInfo1.getTo(),
                        new ChainedConverter<>(unknownInfo2, unknownInfo1),
                        unknownInfo2.getFlags() | unknownInfo1.getFlags()
                     )
                  );
               }
            }
         }
      }
   }

   private static boolean exactConverterExists_i(Class<?> fromType, Class<?> toType) {
      for (ConverterInfo<?, ?> info : CONVERTERS) {
         if (fromType == info.getFrom() && toType == info.getTo()) {
            return true;
         }
      }

      return false;
   }

   public static boolean exactConverterExists(Class<?> fromType, Class<?> toType) {
      synchronized (CONVERTERS) {
         return exactConverterExists_i(fromType, toType);
      }
   }

   public static boolean converterExists(Class<?> fromType, Class<?> toType) {
      assertIsDoneLoading();
      return toType.isAssignableFrom(fromType) ? true : getConverter(fromType, toType) != null;
   }

   public static boolean converterExists(Class<?> fromType, Class<?>... toTypes) {
      assertIsDoneLoading();

      for (Class<?> toType : toTypes) {
         if (converterExists(fromType, toType)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   public static <F, T> Converter<F, T> getConverter(Class<F> fromType, Class<T> toType) {
      ConverterInfo<F, T> info = getConverterInfo(fromType, toType);
      return info != null ? info.getConverter() : null;
   }

   @Nullable
   public static <F, T> ConverterInfo<F, T> getConverterInfo(Class<F> fromType, Class<T> toType) {
      assertIsDoneLoading();
      Pair<Class<?>, Class<?>> pair = new Pair<>(fromType, toType);
      synchronized (QUICK_ACCESS_CONVERTERS) {
         ConverterInfo<F, T> converter;
         if (QUICK_ACCESS_CONVERTERS.containsKey(pair)) {
            converter = (ConverterInfo<F, T>)QUICK_ACCESS_CONVERTERS.get(pair);
         } else {
            converter = getConverterInfo_i(fromType, toType);
            QUICK_ACCESS_CONVERTERS.put(pair, converter);
         }

         return converter;
      }
   }

   @Nullable
   private static <F, T extends ParentType, SubType extends F, ParentType> ConverterInfo<F, T> getConverterInfo_i(Class<F> fromType, Class<T> toType) {
      for (ConverterInfo<?, ?> info : CONVERTERS) {
         if (fromType == info.getFrom() && toType == info.getTo()) {
            return (ConverterInfo<F, T>)info;
         }
      }

      for (ConverterInfo<?, ?> infox : CONVERTERS) {
         if (infox.getFrom().isAssignableFrom(fromType) && toType.isAssignableFrom(infox.getTo())) {
            return (ConverterInfo<F, T>)infox;
         }
      }

      if (fromType == Object.class) {
         return new ConverterInfo<>(fromType, toType, fromObject -> convert(fromObject, toType), 1);
      } else {
         for (ConverterInfo<?, ?> unknownInfo : CONVERTERS) {
            int flags = unknownInfo.getFlags();
            if (unknownInfo.getFrom().isAssignableFrom(fromType) && unknownInfo.getTo().isAssignableFrom(toType)) {
               if ((flags & 4) != 0 || (flags & 2) != 2) {
                  return new ConverterInfo<>(fromType, toType, fromObject -> {
                     Object converted = ((Converter<F, ?>)unknownInfo.getConverter()).convert(fromObject);
                     return (T)(toType.isInstance(converted) ? converted : null);
                  }, 0);
               }
            } else if (fromType.isAssignableFrom(unknownInfo.getFrom())
               && toType.isAssignableFrom(unknownInfo.getTo())
               && ((flags & 4) != 0 || (flags & 1) != 1)) {
               return new ConverterInfo<>(
                  fromType,
                  toType,
                  fromObject -> (T)(!unknownInfo.getFrom().isInstance(fromObject) ? null : ((Converter<F, ?>)unknownInfo.getConverter()).convert(fromObject)),
                  0
               );
            }
         }

         for (ConverterInfo<?, ?> unknownInfox : CONVERTERS) {
            if (fromType.isAssignableFrom(unknownInfox.getFrom()) && unknownInfox.getTo().isAssignableFrom(toType)) {
               int flags = unknownInfox.getFlags();
               if ((flags & 4) != 0 || (flags & 1) != 1 && (flags & 2) != 2) {
                  return new ConverterInfo<>(fromType, toType, fromObject -> {
                     if (!unknownInfo.getFrom().isInstance(fromObject)) {
                        return null;
                     } else {
                        Object converted = ((Converter<F, ?>)unknownInfo.getConverter()).convert(fromObject);
                        return (T)(toType.isInstance(converted) ? converted : null);
                     }
                  }, 0);
               }
            }
         }

         return null;
      }
   }

   @Nullable
   public static <From, To> To convert(@Nullable From from, Class<To> toType) {
      assertIsDoneLoading();
      if (from == null) {
         return null;
      } else if (toType.isInstance(from)) {
         return (To)from;
      } else {
         Converter<From, To> converter = getConverter((Class<From>)from.getClass(), toType);
         return converter == null ? null : converter.convert(from);
      }
   }

   @Nullable
   public static <From, To> To convert(@Nullable From from, Class<? extends To>[] toTypes) {
      assertIsDoneLoading();
      if (from == null) {
         return null;
      } else {
         for (Class<? extends To> toType : toTypes) {
            if (toType.isInstance(from)) {
               return (To)from;
            }
         }

         Class<From> fromType = (Class<From>)from.getClass();

         for (Class<? extends To> toTypex : toTypes) {
            Converter<From, ? extends To> converter = getConverter(fromType, toTypex);
            if (converter != null) {
               return (To)converter.convert(from);
            }
         }

         return null;
      }
   }

   public static <From, To> void convert(@Nullable From[] from, To[] destination, Class<? extends To>[] toTypes) {
      assertIsDoneLoading();
      int length = Math.min(from.length, destination.length);
      if (length != 0) {
         for (int i = 0; i < length; i++) {
            destination[i] = from[i] != null ? convert(from[i], toTypes) : null;
         }
      }
   }

   public static <To> To[] convert(@Nullable Object[] from, Class<To> toType) {
      assertIsDoneLoading();
      if (from == null) {
         return (To[])((Object[])Array.newInstance(toType, 0));
      } else if (toType.isAssignableFrom(from.getClass().getComponentType())) {
         return (To[])from;
      } else {
         List<To> converted = new ArrayList<>(from.length);

         for (Object fromSingle : from) {
            To convertedSingle = convert(fromSingle, toType);
            if (convertedSingle != null) {
               converted.add(convertedSingle);
            }
         }

         return (To[])converted.toArray((Object[])Array.newInstance(toType, converted.size()));
      }
   }

   public static <To> To[] convert(@Nullable Object[] from, Class<? extends To>[] toTypes, Class<To> superType) {
      assertIsDoneLoading();
      if (from == null) {
         return (To[])((Object[])Array.newInstance(superType, 0));
      } else {
         Class<?> fromType = from.getClass().getComponentType();

         for (Class<? extends To> toType : toTypes) {
            if (toType.isAssignableFrom(fromType)) {
               return (To[])from;
            }
         }

         List<To> converted = new ArrayList<>(from.length);

         for (Object fromSingle : from) {
            To convertedSingle = convert(fromSingle, toTypes);
            if (convertedSingle != null) {
               converted.add(convertedSingle);
            }
         }

         return (To[])converted.toArray((Object[])Array.newInstance(superType, converted.size()));
      }
   }

   public static <From, To> To[] convert(From[] from, Class<To> toType, Converter<? super From, ? extends To> converter) {
      assertIsDoneLoading();
      To[] converted = (To[])Array.newInstance(toType, from.length);
      int j = 0;

      for (From fromSingle : from) {
         To convertedSingle = (To)(fromSingle == null ? null : converter.convert(fromSingle));
         if (convertedSingle != null) {
            converted[j++] = convertedSingle;
         }
      }

      if (j != converted.length) {
         converted = Arrays.copyOf(converted, j);
      }

      return converted;
   }

   public static <From, To> void convert(From[] from, To[] destination, Converter<? super From, ? extends To> converter) {
      assertIsDoneLoading();
      int length = Math.min(from.length, destination.length);
      if (length != 0) {
         for (int i = 0; i < length; i++) {
            destination[i] = (To)(from[i] != null ? converter.convert(from[i]) : null);
         }
      }
   }

   public static <To> To convertStrictly(Object from, Class<To> toType) {
      To converted = convert(from, toType);
      if (converted == null) {
         throw new ClassCastException("Cannot convert '" + from + "' to an object of type '" + toType + "'");
      } else {
         return converted;
      }
   }

   public static <To> To[] convertStrictly(Object[] from, Class<To> toType) {
      assertIsDoneLoading();
      To[] converted = (To[])Array.newInstance(toType, from.length);

      for (int i = 0; i < from.length; i++) {
         To convertedSingle = convert(from[i], toType);
         if (convertedSingle == null) {
            throw new ClassCastException("Cannot convert '" + from[i] + "' to an object of type '" + toType + "'");
         }

         converted[i] = convertedSingle;
      }

      return converted;
   }

   public static <From, To> To[] convertUnsafe(From[] from, Class<?> toType, Converter<? super From, ? extends To> converter) {
      return (To[])convert(from, toType, converter);
   }

   private static void assertIsDoneLoading() {
      if (Skript.isAcceptRegistrations()) {
         throw new SkriptAPIException("Converters cannot be retrieved until Skript has finished registrations.");
      }
   }
}
