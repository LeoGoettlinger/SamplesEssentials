package org.evenmorefish.fish.libs.daisylib.pairs;

import org.jspecify.annotations.Nullable;

public record ReadOnlyPair<L, R>(@Nullable L left, @Nullable R right) {
   public boolean isNotEmpty() {
      return !this.isEmpty();
   }

   public boolean isEmpty() {
      return this.left == null && this.right == null;
   }
}
