package net.luckperms.api.model.data;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.util.Tristate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

public interface NodeMap {
   @NonNull
   @Unmodifiable
   Map<ImmutableContextSet, Collection<Node>> toMap();

   @NonNull
   @Unmodifiable
   Collection<Node> toCollection();

   @NonNull
   Tristate contains(@NonNull Node var1, @NonNull NodeEqualityPredicate var2);

   @NonNull
   DataMutateResult add(@NonNull Node var1);

   @NonNull
   DataMutateResult.WithMergedNode add(@NonNull Node var1, @NonNull TemporaryNodeMergeStrategy var2);

   @NonNull
   DataMutateResult remove(@NonNull Node var1);

   void clear();

   void clear(@NonNull Predicate<? super Node> var1);

   void clear(@NonNull ContextSet var1);

   void clear(@NonNull ContextSet var1, @NonNull Predicate<? super Node> var2);
}
