package net.luckperms.api.query.dataorder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.luckperms.api.model.data.DataType;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum DataTypeFilter implements Predicate<DataType> {
   ALL {
      public boolean test(DataType dataType) {
         return true;
      }
   },
   NONE {
      public boolean test(DataType dataType) {
         return false;
      }
   },
   NORMAL_ONLY {
      public boolean test(DataType dataType) {
         return dataType == DataType.NORMAL;
      }
   },
   TRANSIENT_ONLY {
      public boolean test(DataType dataType) {
         return dataType == DataType.TRANSIENT;
      }
   };

   private static final List<DataType> ALL_LIST = Collections.unmodifiableList(Arrays.asList(DataType.NORMAL, DataType.TRANSIENT));
   private static final List<DataType> NORMAL_ONLY_LIST = Collections.singletonList(DataType.NORMAL);
   private static final List<DataType> TRANSIENT_ONLY_LIST = Collections.singletonList(DataType.TRANSIENT);

   private DataTypeFilter() {
   }

   @NonNull
   public static List<DataType> values(@NonNull Predicate<? super DataType> predicate) {
      boolean normal = predicate.test(DataType.NORMAL);
      boolean trans = predicate.test(DataType.TRANSIENT);
      if (normal && trans) {
         return ALL_LIST;
      } else if (normal) {
         return NORMAL_ONLY_LIST;
      } else {
         return trans ? TRANSIENT_ONLY_LIST : Collections.emptyList();
      }
   }
}
