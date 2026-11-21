package com.viaversion.viaversion.api.minecraft;

import org.checkerframework.checker.nullness.qual.Nullable;

public record ResolvableProfile(
   GameProfile profile, @Nullable String bodyTexture, @Nullable String capeTexture, @Nullable String elytraTexture, @Nullable Integer modelType
) {
   public ResolvableProfile(GameProfile profile) {
      this(profile, null, null, null, null);
   }
}
