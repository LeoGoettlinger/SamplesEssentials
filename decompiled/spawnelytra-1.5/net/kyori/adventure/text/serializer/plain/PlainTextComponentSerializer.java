package net.kyori.adventure.text.serializer.plain;

import java.util.function.Consumer;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.util.Buildable;
import net.kyori.adventure.util.PlatformAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface PlainTextComponentSerializer
   extends ComponentSerializer<Component, TextComponent, String>,
   Buildable<PlainTextComponentSerializer, PlainTextComponentSerializer.Builder> {
   @NotNull
   static PlainTextComponentSerializer plainText() {
      return PlainTextComponentSerializerImpl.Instances.INSTANCE;
   }

   @NotNull
   static PlainTextComponentSerializer.Builder builder() {
      return new PlainTextComponentSerializerImpl.BuilderImpl();
   }

   @NotNull
   default TextComponent deserialize(@NotNull final String input) {
      return Component.text(input);
   }

   @NotNull
   default String serialize(@NotNull final Component component) {
      StringBuilder sb = new StringBuilder();
      this.serialize(sb, component);
      return sb.toString();
   }

   void serialize(@NotNull final StringBuilder sb, @NotNull final Component component);

   public interface Builder extends AbstractBuilder<PlainTextComponentSerializer>, Buildable.Builder<PlainTextComponentSerializer> {
      @NotNull
      PlainTextComponentSerializer.Builder flattener(@NotNull final ComponentFlattener flattener);
   }

   @PlatformAPI
   @Internal
   public interface Provider {
      @PlatformAPI
      @Internal
      @NotNull
      PlainTextComponentSerializer plainTextSimple();

      @PlatformAPI
      @Internal
      @NotNull
      Consumer<PlainTextComponentSerializer.Builder> plainText();
   }
}
