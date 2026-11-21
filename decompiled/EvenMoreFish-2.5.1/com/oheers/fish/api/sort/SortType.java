package com.oheers.fish.api.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import org.jspecify.annotations.NonNull;

public enum SortType {
   ALPHABETICAL(Comparator.comparing(Sortable::getId)),
   WEIGHT(Comparator.comparing(Sortable::getWeight).reversed().thenComparing(Sortable::getId)),
   INDEX(Comparator.comparing(Sortable::getIndex).thenComparing(Sortable::getId));

   private final Comparator<Sortable> comparator;

   private SortType(Comparator<Sortable> comparator) {
      this.comparator = comparator;
   }

   public <T extends Sortable> TreeSet<T> sort(@NonNull Collection<T> collection) {
      TreeSet<T> set = new TreeSet<>(this.comparator);
      set.addAll(collection);
      return set;
   }
}
