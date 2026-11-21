package net.kyori.adventure.text.serializer.gson.legacyimpl;

import net.kyori.adventure.text.serializer.gson.LegacyHoverEventSerializer;
import org.jetbrains.annotations.NotNull;

public interface NBTLegacyHoverEventSerializer extends LegacyHoverEventSerializer {
   @NotNull
   static LegacyHoverEventSerializer get() {
      return NBTLegacyHoverEventSerializerImpl.INSTANCE;
   }
}
