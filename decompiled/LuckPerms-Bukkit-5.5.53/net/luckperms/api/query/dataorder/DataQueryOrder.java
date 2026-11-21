package net.luckperms.api.query.dataorder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import net.luckperms.api.model.data.DataType;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum DataQueryOrder implements Comparator<DataType> {
   TRANSIENT_FIRST {
      public int compare(DataType o1, DataType o2) {
         if (o1 == o2) {
            return 0;
         } else {
            return o1 == DataType.TRANSIENT ? 1 : -1;
         }
      }
   },
   TRANSIENT_LAST {
      public int compare(DataType o1, DataType o2) {
         if (o1 == o2) {
            return 0;
         } else {
            return o1 == DataType.TRANSIENT ? -1 : 1;
         }
      }
   };

   private static final List<DataType> TRANSIENT_FIRST_LIST = Collections.unmodifiableList(Arrays.asList(DataType.TRANSIENT, DataType.NORMAL));
   private static final List<DataType> TRANSIENT_LAST_LIST = Collections.unmodifiableList(Arrays.asList(DataType.NORMAL, DataType.TRANSIENT));

   private DataQueryOrder() {
   }

   @NonNull
   public static List<DataType> order(@NonNull Comparator<? super DataType> comparator) {
      int compare = comparator.compare(DataType.TRANSIENT, DataType.NORMAL);
      if (compare > 0) {
         return TRANSIENT_FIRST_LIST;
      } else if (compare < 0) {
         return TRANSIENT_LAST_LIST;
      } else {
         throw new IllegalStateException("Comparator " + comparator + " does not define an order between DataType.NORMAL and DataType.TRANSIENT!");
      }
   }

   public static void queryInOrder(@NonNull Comparator<? super DataType> comparator, @NonNull Consumer<? super DataType> action) {
      order(comparator).forEach(action);
   }
}
