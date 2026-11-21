package net.luckperms.api.node;

import org.checkerframework.checker.nullness.qual.NonNull;

final class DummyNodeEqualityPredicate implements NodeEqualityPredicate {
   private final String name;

   DummyNodeEqualityPredicate(String name) {
      this.name = name;
   }

   @Override
   public boolean areEqual(@NonNull Node o1, @NonNull Node o2) {
      return o1.equals(o2, this);
   }

   @Override
   public String toString() {
      return "NodeEqualityPredicate#" + this.name;
   }
}
