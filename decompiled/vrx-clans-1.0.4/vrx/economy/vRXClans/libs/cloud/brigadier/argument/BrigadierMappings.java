package vrx.economy.vRXClans.libs.cloud.brigadier.argument;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParser;

@API(
   status = API.Status.INTERNAL,
   since = "2.0.0"
)
public interface BrigadierMappings<C, S> {
   @NonNull
   static <C, S> BrigadierMappings<C, S> create() {
      return new BrigadierMappingsImpl<>();
   }

   @Nullable
   <T, K extends ArgumentParser<C, T>> BrigadierMapping<C, K, S> mapping(@NonNull Class<K> parserType);

   default <T, K extends ArgumentParser<C, T>> void registerMapping(@NonNull Class<K> parserType, @NonNull BrigadierMapping<?, K, S> mapping) {
      this.registerMappingUnsafe(parserType, mapping);
   }

   <K extends ArgumentParser<C, ?>> void registerMappingUnsafe(@NonNull Class<K> parserType, @NonNull BrigadierMapping<?, ?, S> mapping);
}
