package io.github.retrooper.packetevents.adventure.serializer.json;

import io.github.retrooper.packetevents.adventure.option.OptionState;
import io.github.retrooper.packetevents.adventure.serializer.ComponentSerializer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.PlatformAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface JSONComponentSerializer extends ComponentSerializer<Component, Component, String> {
   @NotNull
   static JSONComponentSerializer json() {
      return JSONComponentSerializerAccessor.Instances.INSTANCE;
   }

   @NotNull
   static JSONComponentSerializer.Builder builder() {
      return JSONComponentSerializerAccessor.Instances.BUILDER_SUPPLIER.get();
   }

   public interface Builder {
      @NotNull
      JSONComponentSerializer.Builder options(@NotNull final OptionState flags);

      @NotNull
      JSONComponentSerializer.Builder editOptions(@NotNull final Consumer<OptionState.Builder> optionEditor);

      @Deprecated
      @NotNull
      JSONComponentSerializer.Builder downsampleColors();

      @NotNull
      JSONComponentSerializer.Builder legacyHoverEventSerializer(@Nullable final LegacyHoverEventSerializer serializer);

      @Deprecated
      @NotNull
      JSONComponentSerializer.Builder emitLegacyHoverEvent();

      @NotNull
      JSONComponentSerializer build();
   }

   @PlatformAPI
   @Internal
   public interface Provider {
      @PlatformAPI
      @Internal
      @NotNull
      JSONComponentSerializer instance();

      @PlatformAPI
      @Internal
      @NotNull
      Supplier<JSONComponentSerializer.Builder> builder();
   }
}
