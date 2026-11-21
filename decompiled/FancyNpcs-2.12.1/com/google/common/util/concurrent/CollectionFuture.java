package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible(
   emulated = true
)
abstract class CollectionFuture<V, C> extends AggregateFuture<V, C> {
   @CheckForNull
   private List<CollectionFuture.Present<V>> values;

   CollectionFuture(ImmutableCollection<? extends ListenableFuture<? extends V>> futures, boolean allMustSucceed) {
      super(futures, allMustSucceed, true);
      List<CollectionFuture.Present<V>> values = (List<CollectionFuture.Present<V>>)(futures.isEmpty()
         ? Collections.emptyList()
         : Lists.newArrayListWithCapacity(futures.size()));

      for (int i = 0; i < futures.size(); i++) {
         values.add(null);
      }

      this.values = values;
   }

   @Override
   final void collectOneValue(int index, @ParametricNullness V returnValue) {
      List<CollectionFuture.Present<V>> localValues = this.values;
      if (localValues != null) {
         localValues.set(index, new CollectionFuture.Present<>(returnValue));
      }
   }

   @Override
   final void handleAllCompleted() {
      List<CollectionFuture.Present<V>> localValues = this.values;
      if (localValues != null) {
         this.set(this.combine(localValues));
      }
   }

   @Override
   void releaseResources(AggregateFuture.ReleaseResourcesReason reason) {
      super.releaseResources(reason);
      this.values = null;
   }

   abstract C combine(List<CollectionFuture.Present<V>> values);

   static final class ListFuture<V> extends CollectionFuture<V, List<V>> {
      ListFuture(ImmutableCollection<? extends ListenableFuture<? extends V>> futures, boolean allMustSucceed) {
         super(futures, allMustSucceed);
         this.init();
      }

      public List<V> combine(List<CollectionFuture.Present<V>> values) {
         List<V> result = Lists.newArrayListWithCapacity(values.size());

         for (CollectionFuture.Present<V> element : values) {
            result.add(element != null ? element.value : null);
         }

         return Collections.unmodifiableList(result);
      }
   }

   private static final class Present<V> {
      @ParametricNullness
      final V value;

      Present(@ParametricNullness V value) {
         this.value = value;
      }
   }
}
