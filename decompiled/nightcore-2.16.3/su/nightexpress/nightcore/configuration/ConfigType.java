package su.nightexpress.nightcore.configuration;

import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.codec.ConfigCodec;
import su.nightexpress.nightcore.configuration.exception.CodecReadException;

@NullMarked
@Deprecated(
   forRemoval = true
)
public class ConfigType<T> implements ConfigCodec<T> {
   private final ConfigType.Loader<T> loader;
   private final ConfigType.Writer<T> writer;

   public ConfigType(ConfigType.Loader<T> loader, ConfigType.Writer<T> writer) {
      this.loader = loader;
      this.writer = writer;
   }

   public static <T> ConfigType<T> of(ConfigType.Loader<T> loader, ConfigType.Writer<T> writer) {
      return new ConfigType<>(loader, writer);
   }

   public static <T> ConfigType<T> of(ConfigType.Loader<T> loader) {
      return of(loader, FileConfig::set);
   }

   @Override
   public Optional<T> readOptional(FileConfig config, String path) {
      return !config.contains(path) ? Optional.empty() : Optional.ofNullable(this.loader.read(config, path));
   }

   @Nullable
   @Override
   public T read(FileConfig config, String path) throws CodecReadException {
      return this.loader.read(config, path);
   }

   @Override
   public T read(FileConfig config, String path, T defaultValue) {
      return this.readOptional(config, path).orElse(defaultValue);
   }

   @Override
   public void write(FileConfig config, String path, T value) {
      this.writer.write(config, path, value);
   }

   @FunctionalInterface
   public interface Loader<T> {
      @Nullable
      T read(FileConfig var1, String var2);
   }

   @FunctionalInterface
   public interface Writer<T> {
      void write(FileConfig var1, String var2, T var3);
   }
}
