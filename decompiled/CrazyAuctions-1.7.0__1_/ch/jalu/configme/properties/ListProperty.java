package ch.jalu.configme.properties;

import ch.jalu.configme.properties.convertresult.ConvertErrorRecorder;
import ch.jalu.configme.properties.types.PropertyType;
import ch.jalu.configme.resource.PropertyReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListProperty<T> extends BaseProperty<List<T>> {
   private final PropertyType<T> type;

   @SafeVarargs
   public ListProperty(@NotNull String path, @NotNull PropertyType<T> type, @NotNull T... defaultValue) {
      this(path, type, Arrays.asList(defaultValue));
   }

   public ListProperty(@NotNull String path, @NotNull PropertyType<T> type, @NotNull List<T> defaultValue) {
      super(path, Collections.unmodifiableList(defaultValue));
      Objects.requireNonNull(type, "type");
      this.type = type;
   }

   @Nullable
   protected List<T> getFromReader(@NotNull PropertyReader reader, @NotNull ConvertErrorRecorder errorRecorder) {
      List<?> list = reader.getList(this.getPath());
      return list != null
         ? Collections.unmodifiableList(list.stream().map(elem -> this.type.convert(elem, errorRecorder)).filter(Objects::nonNull).collect(Collectors.toList()))
         : null;
   }

   @NotNull
   public Object toExportValue(@NotNull List<T> value) {
      return value.stream().map(this.type::toExportValue).collect(Collectors.toList());
   }
}
