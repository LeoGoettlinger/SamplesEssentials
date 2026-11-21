package xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.gson.legacyimpl;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.gson.LegacyHoverEventSerializer;

@Deprecated
@ApiStatus.ScheduledForRemoval(
   inVersion = "5.0.0"
)
public interface NBTLegacyHoverEventSerializer
   extends LegacyHoverEventSerializer,
   xyz.lychee.lagfixer.libs.kyori.adventure.text.serializer.json.legacyimpl.NBTLegacyHoverEventSerializer {
   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   static LegacyHoverEventSerializer get() {
      return NBTLegacyHoverEventSerializerImpl.INSTANCE;
   }
}
