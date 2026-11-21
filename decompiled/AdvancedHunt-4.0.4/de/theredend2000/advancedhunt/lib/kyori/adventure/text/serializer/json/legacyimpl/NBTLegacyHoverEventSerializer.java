package de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.json.legacyimpl;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer;
import org.jetbrains.annotations.NotNull;

public interface NBTLegacyHoverEventSerializer extends LegacyHoverEventSerializer {
   @NotNull
   static LegacyHoverEventSerializer get() {
      return NBTLegacyHoverEventSerializerImpl.INSTANCE;
   }
}
