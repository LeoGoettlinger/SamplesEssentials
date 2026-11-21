package ac.grim.grimac.shaded.kyori.adventure.text.serializer.gson.impl;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.kyori.adventure.key.Key;
import ac.grim.grimac.shaded.kyori.adventure.text.event.DataComponentValue;
import ac.grim.grimac.shaded.kyori.adventure.text.event.DataComponentValueConverterRegistry;
import ac.grim.grimac.shaded.kyori.adventure.text.serializer.gson.GsonDataComponentValue;
import com.google.auto.service.AutoService;
import com.google.gson.JsonNull;
import java.util.Collections;

@AutoService({DataComponentValueConverterRegistry.Provider.class})
@ApiStatus.Internal
public final class GsonDataComponentValueConverterProvider implements DataComponentValueConverterRegistry.Provider {
   private static final Key ID = Key.key("adventure", "serializer/gson");

   @NotNull
   @Override
   public Key id() {
      return ID;
   }

   @NotNull
   @Override
   public Iterable<DataComponentValueConverterRegistry.Conversion<?, ?>> conversions() {
      return Collections.singletonList(
         DataComponentValueConverterRegistry.Conversion.convert(
            DataComponentValue.Removed.class, GsonDataComponentValue.class, (k, removed) -> GsonDataComponentValue.gsonDataComponentValue(JsonNull.INSTANCE)
         )
      );
   }
}
