package com.earth2me.essentials.libs.configurate.reference;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.reactive.Publisher;
import java.util.function.Function;

public interface ValueReference<T, N extends ConfigurationNode> extends Publisher<T> {
   @Nullable
   T get();

   boolean set(@Nullable T value);

   boolean setAndSave(@Nullable T value);

   Publisher<Boolean> setAndSaveAsync(@Nullable T value);

   boolean update(Function<T, ? extends T> action);

   Publisher<Boolean> updateAsync(Function<T, ? extends T> action);

   N node();
}
