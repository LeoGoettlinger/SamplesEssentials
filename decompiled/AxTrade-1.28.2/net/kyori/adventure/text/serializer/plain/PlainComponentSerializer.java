package net.kyori.adventure.text.serializer.plain;

import java.util.function.Function;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.util.Buildable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
@ApiStatus.ScheduledForRemoval(
   inVersion = "5.0.0"
)
public class PlainComponentSerializer
   implements ComponentSerializer<Component, TextComponent, String>,
   Buildable<PlainComponentSerializer, PlainComponentSerializer.Builder> {
   @Deprecated
   final PlainTextComponentSerializer serializer;

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   public static PlainComponentSerializer plain() {
      return PlainComponentSerializerImpl.INSTANCE;
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   public static PlainComponentSerializer.Builder builder() {
      return new PlainComponentSerializerImpl.BuilderImpl();
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   public PlainComponentSerializer() {
      this(PlainTextComponentSerializer.plainText());
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   public PlainComponentSerializer(
      @Nullable final Function<KeybindComponent, String> keybind, @Nullable final Function<TranslatableComponent, String> translatable
   ) {
      this(PlainComponentSerializerImpl.createRealSerializerFromLegacyFunctions(keybind, translatable));
   }

   @Deprecated
   PlainComponentSerializer(@NotNull final PlainTextComponentSerializer serializer) {
      this.serializer = serializer;
   }

   @NotNull
   public TextComponent deserialize(@NotNull final String input) {
      return this.serializer.deserialize(input);
   }

   @NotNull
   public String serialize(@NotNull final Component component) {
      return this.serializer.serialize(component);
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   public void serialize(@NotNull final StringBuilder sb, @NotNull final Component component) {
      this.serializer.serialize(sb, component);
   }

   @NotNull
   public PlainComponentSerializer.Builder toBuilder() {
      return new PlainComponentSerializerImpl.BuilderImpl(this);
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   public interface Builder extends AbstractBuilder<PlainComponentSerializer>, net.kyori.adventure.util.Buildable.Builder<PlainComponentSerializer> {
      @Deprecated
      @ApiStatus.ScheduledForRemoval(
         inVersion = "5.0.0"
      )
      @NotNull
      PlainComponentSerializer.Builder flattener(@NotNull final ComponentFlattener flattener);
   }
}
