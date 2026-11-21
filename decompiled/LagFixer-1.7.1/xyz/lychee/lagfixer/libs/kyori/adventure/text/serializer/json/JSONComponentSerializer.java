package xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.json;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.ComponentSerializer;
import xyz.lychee.lagfixer.libs.kyori.adventure.util.PlatformAPI;
import xyz.lychee.lagfixer.libs.kyori.option.OptionState;

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
   @ApiStatus.Internal
   public interface Provider {
      @PlatformAPI
      @ApiStatus.Internal
      @NotNull
      JSONComponentSerializer instance();

      @PlatformAPI
      @ApiStatus.Internal
      @NotNull
      Supplier<JSONComponentSerializer.Builder> builder();
   }
}
