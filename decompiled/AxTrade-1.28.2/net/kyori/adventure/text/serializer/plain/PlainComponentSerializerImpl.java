package net.kyori.adventure.text.serializer.plain;

import java.util.function.Function;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.flattener.ComponentFlattener.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
final class PlainComponentSerializerImpl {
   @Deprecated
   static final PlainComponentSerializer INSTANCE = new PlainComponentSerializer();

   private PlainComponentSerializerImpl() {
   }

   @Deprecated
   static PlainTextComponentSerializer createRealSerializerFromLegacyFunctions(
      @Nullable final Function<KeybindComponent, String> keybind, @Nullable final Function<TranslatableComponent, String> translatable
   ) {
      if (keybind == null && translatable == null) {
         return PlainTextComponentSerializer.plainText();
      } else {
         Builder builder = (Builder)ComponentFlattener.basic().toBuilder();
         if (keybind != null) {
            builder.mapper(KeybindComponent.class, keybind);
         }

         if (translatable != null) {
            builder.mapper(TranslatableComponent.class, translatable);
         }

         return (PlainTextComponentSerializer)PlainTextComponentSerializer.builder().flattener((ComponentFlattener)builder.build()).build();
      }
   }

   @Deprecated
   static final class BuilderImpl implements PlainComponentSerializer.Builder {
      private final PlainTextComponentSerializer.Builder builder = PlainTextComponentSerializer.builder();

      @Deprecated
      BuilderImpl() {
      }

      @Deprecated
      BuilderImpl(final PlainComponentSerializer serializer) {
         this.builder.flattener(((PlainTextComponentSerializerImpl)serializer.serializer).flattener);
      }

      @NotNull
      @Override
      public PlainComponentSerializer.Builder flattener(@NotNull final ComponentFlattener flattener) {
         this.builder.flattener(flattener);
         return this;
      }

      @NotNull
      public PlainComponentSerializer build() {
         return new PlainComponentSerializer((PlainTextComponentSerializer)this.builder.build());
      }
   }
}
