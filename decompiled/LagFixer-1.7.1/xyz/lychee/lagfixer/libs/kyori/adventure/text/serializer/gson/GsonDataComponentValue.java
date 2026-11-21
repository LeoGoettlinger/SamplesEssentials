package xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import java.util.Objects;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.event.DataComponentValue;

@ApiStatus.NonExtendable
public interface GsonDataComponentValue extends DataComponentValue {
   static GsonDataComponentValue gsonDataComponentValue(@NotNull final JsonElement data) {
      return (GsonDataComponentValue)(data instanceof JsonNull
         ? GsonDataComponentValueImpl.RemovedGsonComponentValueImpl.INSTANCE
         : new GsonDataComponentValueImpl(Objects.requireNonNull(data, "data")));
   }

   @NotNull
   JsonElement element();
}
