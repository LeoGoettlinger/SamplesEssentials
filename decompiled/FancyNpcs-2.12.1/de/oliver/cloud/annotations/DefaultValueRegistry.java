package de.oliver.cloud.annotations;

import java.util.Optional;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

@API(
   status = Status.STABLE
)
public interface DefaultValueRegistry<C> {
   @This
   @NonNull
   <T> DefaultValueRegistry<C> register(@NonNull String name, @NonNull DefaultValueFactory<C, T> defaultValue);

   @NonNull
   Optional<DefaultValueFactory<C, ?>> named(@NonNull String name);
}
