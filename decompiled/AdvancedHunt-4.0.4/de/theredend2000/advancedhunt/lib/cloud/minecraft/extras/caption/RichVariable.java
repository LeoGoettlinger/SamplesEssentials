package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.caption;

import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionVariable;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.ComponentLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import de.theredend2000.advancedhunt.lib.kyori.adventure.translation.GlobalTranslator;
import java.util.Locale;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.NotNull;

@API(
   status = API.Status.STABLE
)
@Immutable
public interface RichVariable extends CaptionVariable, ComponentLike {
   @NonNull
   static RichVariable of(@NonNull final String key, @NonNull final Component component) {
      return RichVariableImpl.of(key, component);
   }

   @NonNull
   @Override
   String key();

   @NonNull
   Component component();

   @NotNull
   @Override
   default Component asComponent() {
      return this.component();
   }

   @NonNull
   @Override
   default String value() {
      return PlainComponentSerializer.plain().serialize(GlobalTranslator.render(this.component(), Locale.getDefault()));
   }
}
