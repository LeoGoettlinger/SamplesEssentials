package su.nightexpress.nightcore.configuration.property;

import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.configuration.codec.ConfigCodec;

@NullMarked
public class ConfigProperty<T> {
   private final ConfigCodec<T> codec;
   private final String relativePath;
   private final T defaultValue;
   @Nullable
   private final String[] description;

   public ConfigProperty(ConfigCodec<T> type, String relPath, T defaultValue, @Nullable String... description) {
      this.codec = type;
      this.relativePath = relPath;
      this.description = description;
      this.defaultValue = defaultValue;
   }

   public static <T> ConfigProperty<T> of(ConfigCodec<T> type, String relPath, T defaultValue, String... description) {
      return new ConfigProperty<>(type, relPath, defaultValue, description);
   }

   public boolean hasComments() {
      String[] comments = this.getDescription();
      return comments != null && comments.length != 0 ? Stream.of(comments).anyMatch(Predicate.not(String::isBlank)) : false;
   }

   public ConfigCodec<T> getCodec() {
      return this.codec;
   }

   public String getRelativePath() {
      return this.relativePath;
   }

   @Nullable
   public String[] getDescription() {
      return this.description;
   }

   public T getDefaultValue() {
      return this.defaultValue;
   }
}
