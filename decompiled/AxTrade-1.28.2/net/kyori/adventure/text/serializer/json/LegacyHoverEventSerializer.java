package net.kyori.adventure.text.serializer.json;

import java.io.IOException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.util.Codec.Decoder;
import net.kyori.adventure.util.Codec.Encoder;
import org.jetbrains.annotations.NotNull;

public interface LegacyHoverEventSerializer {
   @NotNull
   ShowItem deserializeShowItem(@NotNull Component input) throws IOException;

   @NotNull
   Component serializeShowItem(@NotNull ShowItem input) throws IOException;

   @NotNull
   ShowEntity deserializeShowEntity(@NotNull Component input, Decoder<Component, String, ? extends RuntimeException> componentDecoder) throws IOException;

   @NotNull
   Component serializeShowEntity(@NotNull ShowEntity input, Encoder<Component, String, ? extends RuntimeException> componentEncoder) throws IOException;
}
