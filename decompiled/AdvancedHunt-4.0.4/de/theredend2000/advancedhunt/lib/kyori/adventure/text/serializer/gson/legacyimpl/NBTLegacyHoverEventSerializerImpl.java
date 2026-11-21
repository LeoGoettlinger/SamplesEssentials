package de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.gson.legacyimpl;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.HoverEvent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.gson.LegacyHoverEventSerializer;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.Codec;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

@Deprecated
final class NBTLegacyHoverEventSerializerImpl implements LegacyHoverEventSerializer {
   static final NBTLegacyHoverEventSerializerImpl INSTANCE = new NBTLegacyHoverEventSerializerImpl();
   static final de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer NEW_INSTANCE = de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.json.legacyimpl.NBTLegacyHoverEventSerializer.get();

   private NBTLegacyHoverEventSerializerImpl() {
   }

   @NotNull
   @Override
   public HoverEvent.ShowItem deserializeShowItem(@NotNull final Component input) throws IOException {
      return NEW_INSTANCE.deserializeShowItem(input);
   }

   @NotNull
   @Override
   public HoverEvent.ShowEntity deserializeShowEntity(
      @NotNull final Component input, final Codec.Decoder<Component, String, ? extends RuntimeException> componentCodec
   ) throws IOException {
      return NEW_INSTANCE.deserializeShowEntity(input, componentCodec);
   }

   @NotNull
   @Override
   public Component serializeShowItem(@NotNull final HoverEvent.ShowItem input) throws IOException {
      return NEW_INSTANCE.serializeShowItem(input);
   }

   @NotNull
   @Override
   public Component serializeShowEntity(
      @NotNull final HoverEvent.ShowEntity input, final Codec.Encoder<Component, String, ? extends RuntimeException> componentCodec
   ) throws IOException {
      return NEW_INSTANCE.serializeShowEntity(input, componentCodec);
   }
}
