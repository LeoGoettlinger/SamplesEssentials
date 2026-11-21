package com.earth2me.essentials.libs.kyori.adventure.text.serializer.gson.legacyimpl;

import com.earth2me.essentials.libs.kyori.adventure.text.serializer.gson.LegacyHoverEventSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

@Deprecated
@ScheduledForRemoval(
   inVersion = "5.0.0"
)
public interface NBTLegacyHoverEventSerializer
   extends LegacyHoverEventSerializer,
   com.earth2me.essentials.libs.kyori.adventure.text.serializer.json.legacyimpl.NBTLegacyHoverEventSerializer {
   @Deprecated
   @ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   static LegacyHoverEventSerializer get() {
      return NBTLegacyHoverEventSerializerImpl.INSTANCE;
   }
}
