package su.nightexpress.nightcore.util.placeholder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.util.StringUtil;

public class TypedPlaceholder<T> {
   private final Map<String, Function<T, String>> entries;

   private TypedPlaceholder(@NonNull Map<String, Function<T, String>> entries) {
      this.entries = new LinkedHashMap<>(entries);
   }

   @NonNull
   public static <T> TypedPlaceholder.Builder<T> builder() {
      return new TypedPlaceholder.Builder<>();
   }

   @NonNull
   public static <T> TypedPlaceholder.Builder<T> builder(@NonNull Class<T> type) {
      return new TypedPlaceholder.Builder<>();
   }

   @NonNull
   public Map<String, Function<T, String>> getEntries() {
      return Map.copyOf(this.entries);
   }

   @NonNull
   public PlaceholderResolver resolver(@NonNull T source) {
      return key -> {
         Function<T, String> func = this.entries.get(key);
         return func == null ? null : func.apply(source);
      };
   }

   @NonNull
   public UnaryOperator<String> replacer(@NonNull T source) {
      return str -> StringUtil.replacePlaceholders(str, this.resolver(source));
   }

   public static class Builder<T> {
      private final Map<String, Function<T, String>> entries = new HashMap<>();

      @NonNull
      public TypedPlaceholder<T> build() {
         return new TypedPlaceholder<>(this.entries);
      }

      @NonNull
      public TypedPlaceholder.Builder<T> include(@NonNull TypedPlaceholder<? super T> parent) {
         parent.getEntries().forEach((key, fun) -> this.entries.putIfAbsent(key, (Function<T, String>)fun));
         return this;
      }

      @NonNull
      public TypedPlaceholder.Builder<T> with(@NonNull String key, @NonNull Function<T, String> function) {
         this.entries.put(key, function);
         return this;
      }
   }
}
