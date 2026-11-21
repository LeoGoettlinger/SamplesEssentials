package xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.json.legacyimpl;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer;

public interface NBTLegacyHoverEventSerializer extends LegacyHoverEventSerializer {
   @NotNull
   static LegacyHoverEventSerializer get() {
      return NBTLegacyHoverEventSerializerImpl.INSTANCE;
   }
}
