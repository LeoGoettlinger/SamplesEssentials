package io.github.retrooper.packetevents.adventure.serializer.gson.impl;

import com.google.auto.service.AutoService;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import io.github.retrooper.packetevents.adventure.serializer.json.JSONComponentSerializer;
import java.util.function.Supplier;
import net.kyori.adventure.util.Services;
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
