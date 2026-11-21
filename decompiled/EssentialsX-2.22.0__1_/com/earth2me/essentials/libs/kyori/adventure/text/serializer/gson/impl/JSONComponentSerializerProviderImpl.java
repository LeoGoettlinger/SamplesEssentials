package com.earth2me.essentials.libs.kyori.adventure.text.serializer.gson.impl;

import com.earth2me.essentials.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import com.earth2me.essentials.libs.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import com.earth2me.essentials.libs.kyori.adventure.util.Services;
import com.google.auto.service.AutoService;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@AutoService({JSONComponentSerializer.Provider.class})
public final class JSONComponentSerializerProviderImpl implements JSONComponentSerializer.Provider, Services.Fallback {
   @NotNull
   @Override
   public JSONComponentSerializer instance() {
      return GsonComponentSerializer.gson();
   }

   @NotNull
   @Override
   public Supplier<JSONComponentSerializer.Builder> builder() {
      return GsonComponentSerializer::builder;
   }

   @Override
   public String toString() {
      return "JSONComponentSerializerProviderImpl[GsonComponentSerializer]";
   }
}
