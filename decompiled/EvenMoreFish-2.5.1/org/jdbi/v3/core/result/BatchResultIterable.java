package org.jdbi.v3.core.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public interface BatchResultIterable<T> extends ResultIterable<T> {
   List<List<T>> listPerBatch();

   static <U> BatchResultIterable<U> of(ResultIterable<U> delegate, Supplier<int[]> modifiedRowCountsSupplier) {
      return new BatchResultIterable<U>() {
         @Override
         public List<List<U>> listPerBatch() {
            List<List<U>> results = new ArrayList<>();

            try (ResultIterator<U> iterator = delegate.iterator()) {
               for (int modCount : modifiedRowCountsSupplier.get()) {
                  if (modCount <= 0) {
                     results.add(Collections.emptyList());
                  } else {
                     List<U> batchResult = new ArrayList<>(modCount);

                     for (int i = 0; i < modCount; i++) {
                        batchResult.add(iterator.next());
                     }

                     results.add(batchResult);
                  }
               }
            }

            return results;
         }

         @Override
         public ResultIterator<U> iterator() {
            return delegate.iterator();
         }
      };
   }
}
