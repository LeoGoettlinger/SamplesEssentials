package org.skriptlang.skript.lang.comparator;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public final class ComparatorInfo<T1, T2> {
   private final Class<T1> firstType;
   private final Class<T2> secondType;
   private final Comparator<T1, T2> comparator;

   ComparatorInfo(@NotNull Class<T1> firstType, @NotNull Class<T2> secondType, @NotNull Comparator<T1, T2> comparator) {
      Preconditions.checkNotNull(firstType, "Cannot create a comparison between nothing and something! (firstType is null)");
      Preconditions.checkNotNull(secondType, "Cannot create a comparison between something and nothing! (secondType is null)");
      Preconditions.checkNotNull(comparator, "Cannot create a comparison with a null comparator!");
      this.firstType = firstType;
      this.secondType = secondType;
      this.comparator = comparator;
   }

   public Class<T1> getFirstType() {
      return this.firstType;
   }

   public Class<T2> getSecondType() {
      return this.secondType;
   }

   public Comparator<T1, T2> getComparator() {
      return this.comparator;
   }

   @Override
   public String toString() {
      return "ComparatorInfo{first=" + this.firstType + ",second=" + this.secondType + ",comparator=" + this.comparator + "}";
   }
}
