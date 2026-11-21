package de.oliver.cloud.brigadier.argument;

import de.oliver.cloud.parser.ArgumentParser;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class BrigadierMappingsImpl<C, S> implements BrigadierMappings<C, S> {
   private final Map<Class<?>, BrigadierMapping<?, ?, S>> mappers = new HashMap<>();

   @Nullable
   @Override
   public <T, K extends ArgumentParser<C, T>> BrigadierMapping<C, K, S> mapping(@NonNull final Class<K> parserType) {
      BrigadierMapping<?, ?, S> mapper = this.mappers.get(parserType);
      return (BrigadierMapping<C, K, S>)(mapper == null ? null : mapper);
   }

   @Override
   public <K extends ArgumentParser<C, ?>> void registerMappingUnsafe(@NonNull final Class<K> parserType, @NonNull final BrigadierMapping<?, ?, S> mapping) {
      this.mappers.put(parserType, mapping);
   }
}
