package su.nightexpress.nightcore.util;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Enums {
   public static final String DEFAULT_DELIMITER = ", ";

   private Enums() {
   }

   @NonNull
   public static <T extends Enum<T>> Optional<T> parse(String str, @NonNull Class<T> type) {
      try {
         return str == null ? Optional.empty() : Optional.of(Enum.valueOf(type, str.toUpperCase()));
      } catch (Exception var3) {
         return Optional.empty();
      }
   }

   @Nullable
   public static <T extends Enum<T>> T get(@NonNull String str, @NonNull Class<T> type) {
      return parse(str, type).orElse(null);
   }

   @NonNull
   public static String inline(@NonNull Class<? extends Enum<?>> type) {
      return inline(type, ", ");
   }

   @NonNull
   public static String inline(@NonNull Class<? extends Enum<?>> type, @NonNull String delimiter) {
      return String.join(delimiter, getNames(type));
   }

   @NonNull
   public static List<String> getNames(@NonNull Class<? extends Enum<?>> type) {
      return Stream.of(type.getEnumConstants()).sorted(Comparator.comparingInt(Enum::ordinal)).map(Object::toString).toList();
   }
}
