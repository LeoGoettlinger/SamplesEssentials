package com.artillexstudios.axafkzone.libs.axapi.placeholders;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface PlaceholderArgumentResolver<T> {
   @Nullable
   T resolve(String string);
}
