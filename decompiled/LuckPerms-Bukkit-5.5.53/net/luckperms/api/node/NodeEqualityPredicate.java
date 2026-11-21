package net.luckperms.api.node;

import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface NodeEqualityPredicate {
   NodeEqualityPredicate EXACT = new DummyNodeEqualityPredicate("EXACT");
   NodeEqualityPredicate ONLY_KEY = new DummyNodeEqualityPredicate("ONLY_KEY");
   NodeEqualityPredicate IGNORE_VALUE = new DummyNodeEqualityPredicate("IGNORE_VALUE");
   NodeEqualityPredicate IGNORE_EXPIRY_TIME = new DummyNodeEqualityPredicate("IGNORE_EXPIRY_TIME");
   NodeEqualityPredicate IGNORE_EXPIRY_TIME_AND_VALUE = new DummyNodeEqualityPredicate("IGNORE_EXPIRY_TIME_AND_VALUE");
   NodeEqualityPredicate IGNORE_VALUE_OR_IF_TEMPORARY = new DummyNodeEqualityPredicate("IGNORE_VALUE_OR_IF_TEMPORARY");

   boolean areEqual(@NonNull Node var1, @NonNull Node var2);

   default Predicate<Node> equalTo(Node node) {
      return other -> this.areEqual(node, other);
   }
}
