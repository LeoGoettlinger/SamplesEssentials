package de.oliver.cloud.annotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

final class DefaultValueRegistryImpl<C> implements DefaultValueRegistry<C> {
   private final Map<String, DefaultValueFactory<C, ?>> factories = new HashMap<>();

   @This
   @NonNull
   @Override
   public <T> DefaultValueRegistry<C> register(@NonNull final String name, @NonNull final DefaultValueFactory<C, T> defaultValue) {
      this.factories.put(Objects.requireNonNull(name, "name"), Objects.requireNonNull(defaultValue, "defaultValue"));
      return this;
   }

   @NonNull
   @Override
   public Optional<DefaultValueFactory<C, ?>> named(@NonNull final String name) {
      return Optional.ofNullable(this.factories.get(Objects.requireNonNull(name, "name")));
   }
}
