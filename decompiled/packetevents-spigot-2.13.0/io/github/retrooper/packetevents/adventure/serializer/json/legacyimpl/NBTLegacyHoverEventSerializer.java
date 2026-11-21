package io.github.retrooper.packetevents.adventure.serializer.json.legacyimpl;

import io.github.retrooper.packetevents.adventure.serializer.json.LegacyHoverEventSerializer;
import org.jetbrains.annotations.NotNull;

public interface NBTLegacyHoverEventSerializer extends LegacyHoverEventSerializer {
   @NotNull
   static LegacyHoverEventSerializer get() {
      return NBTLegacyHoverEventSerializerImpl.INSTANCE;
   }
}
