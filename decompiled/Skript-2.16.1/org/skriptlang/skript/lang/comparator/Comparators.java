package org.skriptlang.skript.lang.comparator;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.converter.Converters;

public final class Comparators {
   private static final ComparatorInfo<Object, Object> EQUALS_COMPARATOR_INFO = new ComparatorInfo<>(
      Object.class, Object.class, (o1, o2) -> Relation.get(o1.equals(o2))
   );
   private static final List<ComparatorInfo<?, ?>> COMPARATORS = new ArrayList<>(50);
   private static final Map<Pair<Class<?>, Class<?>>, ComparatorInfo<?, ?>> QUICK_ACCESS_COMPARATORS = new HashMap<>(50);

   private Comparators() {
   }

   @Unmodifiable
   public static List<ComparatorInfo<?, ?>> getComparatorInfos() {
      assertIsDoneLoading();
      return Collections.unmodifiableList(COMPARATORS);
   }

   public static <T1, T2> void registerComparator(Class<T1> firstType, Class<T2> secondType, Comparator<T1, T2> comparator) {
      Skript.checkAcceptRegistrations();
      if (firstType == Object.class && secondType == Object.class) {
         throw new IllegalArgumentException("It is not possible to add a comparator between objects");
      } else {
         synchronized (COMPARATORS) {
            if (exactComparatorExists_i(firstType, secondType)) {
               throw new SkriptAPIException("A Comparator comparing '" + firstType + "' and '" + secondType + "' already exists!");
            } else {
               COMPARATORS.add(new ComparatorInfo<>(firstType, secondType, comparator));
            }
         }
      }
   }

   private static boolean exactComparatorExists_i(Class<?> firstType, Class<?> secondType) {
      for (ComparatorInfo<?, ?> info : COMPARATORS) {
         if (info.getFirstType() == firstType && info.getSecondType() == secondType) {
            return true;
         }
      }

      return false;
   }

   public static boolean exactComparatorExists(Class<?> firstType, Class<?> secondType) {
      synchronized (COMPARATORS) {
         return exactComparatorExists_i(firstType, secondType);
      }
   }

   public static boolean comparatorExists(Class<?> firstType, Class<?> secondType) {
      assertIsDoneLoading();
      return firstType != Object.class && firstType == secondType ? true : getComparator(firstType, secondType) != null;
   }

   public static <T1, T2> Relation compare(@Nullable T1 first, @Nullable T2 second) {
      assertIsDoneLoading();
      if (first == null || second == null) {
         return Relation.NOT_EQUAL;
      } else if (first == second) {
         return Relation.EQUAL;
      } else {
         Comparator<T1, T2> comparator = getComparator((Class<T1>)first.getClass(), (Class<T2>)second.getClass());
         return comparator == null ? Relation.NOT_EQUAL : comparator.compare(first, second);
      }
   }

   @Nullable
   public static <T1, T2> Comparator<T1, T2> getComparator(Class<T1> firstType, Class<T2> secondType) {
      ComparatorInfo<T1, T2> info = getComparatorInfo(firstType, secondType);
      return info != null ? info.getComparator() : null;
   }

   @Nullable
   public static <T1, T2> ComparatorInfo<T1, T2> getComparatorInfo(Class<T1> firstType, Class<T2> secondType) {
      assertIsDoneLoading();
      Pair<Class<?>, Class<?>> pair = new Pair<>(firstType, secondType);
      synchronized (QUICK_ACCESS_COMPARATORS) {
         ComparatorInfo<T1, T2> comparator;
         if (QUICK_ACCESS_COMPARATORS.containsKey(pair)) {
            comparator = (ComparatorInfo<T1, T2>)QUICK_ACCESS_COMPARATORS.get(pair);
         } else {
            comparator = getComparatorInfo_i(firstType, secondType);
            QUICK_ACCESS_COMPARATORS.put(pair, comparator);
         }

         return comparator;
      }
   }

   @Nullable
   private static <T1, T2, C1, C2> ComparatorInfo<T1, T2> getComparatorInfo_i(Class<T1> firstType, Class<T2> secondType) {
      for (ComparatorInfo<?, ?> info : COMPARATORS) {
         if (info.getFirstType() == firstType && info.getSecondType() == secondType) {
            return (ComparatorInfo<T1, T2>)info;
         }
      }

      for (ComparatorInfo<?, ?> infox : COMPARATORS) {
         if (infox.getFirstType().isAssignableFrom(firstType) && infox.getSecondType().isAssignableFrom(secondType)) {
            return (ComparatorInfo<T1, T2>)infox;
         }
      }

      for (ComparatorInfo<?, ?> infoxx : COMPARATORS) {
         if (infoxx.getComparator().supportsInversion()
            && infoxx.getFirstType().isAssignableFrom(secondType)
            && infoxx.getSecondType().isAssignableFrom(firstType)) {
            return new ComparatorInfo<>(firstType, secondType, new InverseComparator<>((ComparatorInfo<T2, T1>)infoxx));
         }
      }

      if (Utils.getSuperType(firstType, secondType) == Object.class) {
         ConverterInfo<T1, T2> fs = Converters.getConverterInfo(firstType, secondType);
         if (fs != null) {
            return new ComparatorInfo<>(firstType, secondType, new ConvertedComparator<>(fs, getComparatorInfo(secondType, secondType), null));
         }

         ConverterInfo<T2, T1> sf = Converters.getConverterInfo(secondType, firstType);
         if (sf != null) {
            return new ComparatorInfo<>(firstType, secondType, new ConvertedComparator<>(null, getComparatorInfo(firstType, firstType), sf));
         }
      }

      for (ComparatorInfo<?, ?> unknownInfo : COMPARATORS) {
         if (unknownInfo.getFirstType().isAssignableFrom(firstType)) {
            ConverterInfo<T2, C2> sc2 = Converters.getConverterInfo(secondType, (Class<C2>)unknownInfo.getSecondType());
            if (sc2 != null) {
               return new ComparatorInfo<>(firstType, secondType, new ConvertedComparator<>(null, unknownInfo, sc2));
            }
         }

         if (unknownInfo.getSecondType().isAssignableFrom(secondType)) {
            ConverterInfo<T1, C1> fc1 = Converters.getConverterInfo(firstType, (Class<C1>)unknownInfo.getFirstType());
            if (fc1 != null) {
               return new ComparatorInfo<>(firstType, secondType, new ConvertedComparator<>(fc1, (ComparatorInfo<C1, ?>)unknownInfo, null));
            }
         }
      }

      for (ComparatorInfo<?, ?> unknownInfo : COMPARATORS) {
         if (unknownInfo.getComparator().supportsInversion()) {
            if (unknownInfo.getSecondType().isAssignableFrom(firstType)) {
               ConverterInfo<T2, C1> sc1 = Converters.getConverterInfo(secondType, (Class<C1>)unknownInfo.getFirstType());
               if (sc1 != null) {
                  return new ComparatorInfo<>(
                     firstType,
                     secondType,
                     new InverseComparator<>(
                        new ComparatorInfo<>(secondType, firstType, new ConvertedComparator<>(sc1, (ComparatorInfo<C1, ?>)unknownInfo, null))
                     )
                  );
               }
            }

            if (unknownInfo.getFirstType().isAssignableFrom(secondType)) {
               ConverterInfo<T1, C2> fc2 = Converters.getConverterInfo(firstType, (Class<C2>)unknownInfo.getSecondType());
               if (fc2 != null) {
                  return new ComparatorInfo<>(
                     firstType,
                     secondType,
                     new InverseComparator<>(new ComparatorInfo<>(secondType, firstType, new ConvertedComparator<>(null, unknownInfo, fc2)))
                  );
               }
            }
         }
      }

      for (ComparatorInfo<?, ?> unknownInfox : COMPARATORS) {
         ConverterInfo<T1, C1> c1 = Converters.getConverterInfo(firstType, (Class<C1>)unknownInfox.getFirstType());
         ConverterInfo<T2, C2> c2 = Converters.getConverterInfo(secondType, (Class<C2>)unknownInfox.getSecondType());
         if (c1 != null && c2 != null) {
            return new ComparatorInfo<>(firstType, secondType, new ConvertedComparator<>(c1, (ComparatorInfo<C1, ?>)unknownInfox, c2));
         }
      }

      for (ComparatorInfo<?, ?> unknownInfoxx : COMPARATORS) {
         if (unknownInfoxx.getComparator().supportsInversion()) {
            ConverterInfo<T1, C2> c1 = Converters.getConverterInfo(firstType, (Class<C2>)unknownInfoxx.getSecondType());
            ConverterInfo<T2, C1> c2 = Converters.getConverterInfo(secondType, (Class<C1>)unknownInfoxx.getFirstType());
            if (c1 != null && c2 != null) {
               return new ComparatorInfo<>(
                  firstType,
                  secondType,
                  new InverseComparator<>(new ComparatorInfo<>(secondType, firstType, new ConvertedComparator<>(c2, (ComparatorInfo<C1, ?>)unknownInfoxx, c1)))
               );
            }
         }
      }

      return (ComparatorInfo<T1, T2>)(firstType != Object.class && Classes.getSuperClassInfo(firstType) == Classes.getSuperClassInfo(secondType)
         ? EQUALS_COMPARATOR_INFO
         : null);
   }

   private static void assertIsDoneLoading() {
      if (Skript.isAcceptRegistrations()) {
         throw new SkriptAPIException("Comparators cannot be retrieved until Skript has finished registrations.");
      }
   }
}
