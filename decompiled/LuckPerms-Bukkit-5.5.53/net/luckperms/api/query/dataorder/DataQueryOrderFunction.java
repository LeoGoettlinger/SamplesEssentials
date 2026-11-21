package net.luckperms.api.query.dataorder;

import java.util.Comparator;
import java.util.Objects;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.data.DataType;
import net.luckperms.api.query.OptionKey;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface DataQueryOrderFunction {
   OptionKey<DataQueryOrderFunction> KEY = OptionKey.of("dataqueryorderfunction", DataQueryOrderFunction.class);

   @NonNull
   static DataQueryOrderFunction always(@NonNull Comparator<DataType> comparator) {
      Objects.requireNonNull(comparator, "comparator");
      return id -> comparator;
   }

   @NonNull
   Comparator<DataType> getOrderComparator(@NonNull PermissionHolder.Identifier var1);
}
