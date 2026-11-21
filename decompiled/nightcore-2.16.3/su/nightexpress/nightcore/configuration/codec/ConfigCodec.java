package su.nightexpress.nightcore.configuration.codec;

import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.exception.CodecReadException;

@NullMarked
public interface ConfigCodec<T> {
   T read(FileConfig var1, String var2) throws CodecReadException;

   void write(FileConfig var1, String var2, @NonNull T var3);

   @Nullable
   default Class<? super T> getDispatcherType() {
      return null;
   }

   default Optional<T> readOptional(FileConfig config, String path) {
      if (!config.contains(path)) {
         return Optional.empty();
      } else {
         try {
            return Optional.ofNullable(this.read(config, path));
         } catch (Exception var4) {
            return Optional.empty();
         }
      }
   }

   default T read(FileConfig config, String path, T defaultValue) {
      return this.readOptional(config, path).orElse(defaultValue);
   }

   static <T> ConfigCodec<T> of(final ConfigCodec.Loader<T> loader, final ConfigCodec.Writer<T> writer) {
      return new ConfigCodec<T>() {
         @Override
         public T read(FileConfig config, String path) throws CodecReadException {
            return loader.read(config, path);
         }

         @Override
         public void write(FileConfig config, String path, @NonNull T value) {
            writer.write(config, path, value);
         }
      };
   }

   static <T> ConfigCodec<T> of(ConfigCodec.Loader<T> loader) {
      return of(loader, FileConfig::set);
   }

   @FunctionalInterface
   public interface Loader<T> {
      T read(FileConfig var1, String var2);
   }

   @FunctionalInterface
   public interface Writer<T> {
      void write(FileConfig var1, String var2, T var3);
   }
}
