package de.theredend2000.advancedhunt.lib.kyori.adventure.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public final class MonkeyBars {
   private MonkeyBars() {
   }

   @SafeVarargs
   public static <E extends Enum<E>> Set<E> enumSet(final Class<E> type, final E... constants) {
      Set<E> set = EnumSet.noneOf(type);
      Collections.addAll(set, constants);
      return Set.copyOf(set);
   }

   public static <T> List<T> addOne(final List<T> oldList, final T newElement) {
      if (oldList.isEmpty()) {
         return List.of(newElement);
      } else {
         List<T> newList = new ArrayList<>(oldList.size() + 1);
         newList.addAll(oldList);
         newList.add(newElement);
         return List.copyOf(newList);
      }
   }

   @SafeVarargs
   public static <I, O> List<O> nonEmptyArrayToList(final Function<I, O> mapper, final I first, final I... others) {
      List<O> ret = new ArrayList<>(others.length + 1);
      ret.add(mapper.apply(first));

      for (I other : others) {
         ret.add(Objects.requireNonNull(mapper.apply(Objects.requireNonNull(other, "source[?]")), "mapper(source[?])"));
      }

      return List.copyOf(ret);
   }

   public static <I, O> List<O> toUnmodifiableList(final Function<I, O> mapper, final Iterable<? extends I> source) {
      ArrayList<O> ret = source instanceof Collection ? new ArrayList<>(((Collection)source).size()) : new ArrayList<>();

      for (I el : source) {
         ret.add(Objects.requireNonNull(mapper.apply(Objects.requireNonNull(el, "source[?]")), "mapper(source[?])"));
      }

      return List.copyOf(ret);
   }
}
