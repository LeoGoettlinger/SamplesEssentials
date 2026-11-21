package net.luckperms.api.query.dataorder;

import java.util.Objects;
import java.util.function.Predicate;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.data.DataType;
import net.luckperms.api.query.OptionKey;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface DataTypeFilterFunction {
   OptionKey<DataTypeFilterFunction> KEY = OptionKey.of("datatypefilterfunction", DataTypeFilterFunction.class);

   @NonNull
   static DataTypeFilterFunction always(@NonNull Predicate<DataType> predicate) {
      Objects.requireNonNull(predicate, "predicate");
      return id -> predicate;
   }

   @NonNull
   Predicate<DataType> getTypeFilter(@NonNull PermissionHolder.Identifier var1);
}
