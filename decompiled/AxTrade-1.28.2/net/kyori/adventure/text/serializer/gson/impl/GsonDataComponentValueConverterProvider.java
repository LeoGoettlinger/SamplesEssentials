package net.kyori.adventure.text.serializer.gson.impl;

import com.google.auto.service.AutoService;
import com.google.gson.JsonNull;
import java.util.Collections;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.DataComponentValue.Removed;
import net.kyori.adventure.text.event.DataComponentValueConverterRegistry.Conversion;
import net.kyori.adventure.text.event.DataComponentValueConverterRegistry.Provider;
import net.kyori.adventure.text.serializer.gson.GsonDataComponentValue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@AutoService({Provider.class})
@ApiStatus.Internal
public final class GsonDataComponentValueConverterProvider implements Provider {
   private static final Key ID = Key.key("adventure", "serializer/gson");

   @NotNull
   public Key id() {
      return ID;
   }

   @NotNull
   public Iterable<Conversion<?, ?>> conversions() {
      return Collections.singletonList(
         Conversion.convert(Removed.class, GsonDataComponentValue.class, (k, removed) -> GsonDataComponentValue.gsonDataComponentValue(JsonNull.INSTANCE))
      );
   }
}
