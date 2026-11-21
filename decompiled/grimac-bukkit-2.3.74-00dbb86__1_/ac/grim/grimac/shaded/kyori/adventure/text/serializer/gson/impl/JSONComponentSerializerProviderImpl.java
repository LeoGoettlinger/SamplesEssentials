package ac.grim.grimac.shaded.kyori.adventure.text.serializer.gson.impl;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import ac.grim.grimac.shaded.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import ac.grim.grimac.shaded.kyori.adventure.util.Services;
import com.google.auto.service.AutoService;
import java.util.function.Supplier;

@ApiStatus.Internal
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
