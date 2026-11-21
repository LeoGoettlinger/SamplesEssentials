package me.clip.placeholderapi.libs.kyori.adventure.text.serializer.json.legacyimpl;

import me.clip.placeholderapi.libs.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer;
import org.jetbrains.annotations.NotNull;

public interface NBTLegacyHoverEventSerializer extends LegacyHoverEventSerializer {
   @NotNull
   static LegacyHoverEventSerializer get() {
      return NBTLegacyHoverEventSerializerImpl.INSTANCE;
   }
}
