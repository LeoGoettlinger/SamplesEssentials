package xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.adventure.builder.AbstractBuilder;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.json.JSONOptions;
import xyz.lychee.lagfixer.libs.kyori.adventure.util.Buildable;
import xyz.lychee.lagfixer.libs.kyori.adventure.util.PlatformAPI;
import xyz.lychee.lagfixer.libs.kyori.option.OptionState;

public interface GsonComponentSerializer extends JSONComponentSerializer, Buildable<GsonComponentSerializer, GsonComponentSerializer.Builder> {
   @NotNull
   static GsonComponentSerializer gson() {
      return GsonComponentSerializerImpl.Instances.INSTANCE;
   }

   @NotNull
   static GsonComponentSerializer colorDownsamplingGson() {
      return GsonComponentSerializerImpl.Instances.LEGACY_INSTANCE;
   }

   static GsonComponentSerializer.Builder builder() {
      return new GsonComponentSerializerImpl.BuilderImpl();
   }

   @NotNull
   Gson serializer();

   @NotNull
   UnaryOperator<GsonBuilder> populator();

   @NotNull
   Component deserializeFromTree(@NotNull final JsonElement input);

   @NotNull
   JsonElement serializeToTree(@NotNull final Component component);

   public interface Builder extends AbstractBuilder<GsonComponentSerializer>, Buildable.Builder<GsonComponentSerializer>, JSONComponentSerializer.Builder {
      @NotNull
      GsonComponentSerializer.Builder options(@NotNull final OptionState flags);

      @NotNull
      GsonComponentSerializer.Builder editOptions(@NotNull final Consumer<OptionState.Builder> optionEditor);

      @Deprecated
      @NotNull
      default GsonComponentSerializer.Builder downsampleColors() {
         return this.editOptions(features -> features.value(JSONOptions.EMIT_RGB, false));
      }

      @Deprecated
      @NotNull
      default GsonComponentSerializer.Builder legacyHoverEventSerializer(@Nullable final LegacyHoverEventSerializer serializer) {
         return this.legacyHoverEventSerializer((xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer)serializer);
      }

      @NotNull
      GsonComponentSerializer.Builder legacyHoverEventSerializer(
         @Nullable final xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer serializer
      );

      @Deprecated
      @NotNull
      default GsonComponentSerializer.Builder emitLegacyHoverEvent() {
         return this.editOptions(b -> b.value(JSONOptions.EMIT_HOVER_EVENT_TYPE, JSONOptions.HoverEventValueMode.ALL));
      }

      @NotNull
      GsonComponentSerializer build();
   }

   @PlatformAPI
   @ApiStatus.Internal
   public interface Provider {
      @PlatformAPI
      @ApiStatus.Internal
      @NotNull
      GsonComponentSerializer gson();

      @PlatformAPI
      @ApiStatus.Internal
      @NotNull
      GsonComponentSerializer gsonLegacy();

      @PlatformAPI
      @ApiStatus.Internal
      @NotNull
      Consumer<GsonComponentSerializer.Builder> builder();
   }
}
