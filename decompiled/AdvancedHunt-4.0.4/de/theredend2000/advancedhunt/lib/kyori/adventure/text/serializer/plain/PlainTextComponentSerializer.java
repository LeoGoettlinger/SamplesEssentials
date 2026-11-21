package de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.plain;

import de.theredend2000.advancedhunt.lib.kyori.adventure.builder.AbstractBuilder;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.TextComponent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.flattener.ComponentFlattener;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.ComponentSerializer;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.PlatformAPI;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface PlainTextComponentSerializer extends ComponentSerializer<Component, TextComponent, String> {
   static PlainTextComponentSerializer plainText() {
      return PlainTextComponentSerializerImpl.Instances.INSTANCE;
   }

   static PlainTextComponentSerializer.Builder builder() {
      return new PlainTextComponentSerializerImpl.BuilderImpl();
   }

   default TextComponent deserialize(final String input) {
      return Component.text(input);
   }

   default String serialize(final Component component) {
      StringBuilder sb = new StringBuilder();
      this.serialize(sb, component);
      return sb.toString();
   }

   void serialize(final StringBuilder sb, final Component component);

   PlainTextComponentSerializer.Builder toBuilder();

   public interface Builder extends AbstractBuilder<PlainTextComponentSerializer> {
      PlainTextComponentSerializer.Builder flattener(final ComponentFlattener flattener);
   }

   @PlatformAPI
   @Internal
   public interface Provider {
      @PlatformAPI
      @Internal
      PlainTextComponentSerializer plainTextSimple();

      @PlatformAPI
      @Internal
      Consumer<PlainTextComponentSerializer.Builder> plainText();
   }
}
