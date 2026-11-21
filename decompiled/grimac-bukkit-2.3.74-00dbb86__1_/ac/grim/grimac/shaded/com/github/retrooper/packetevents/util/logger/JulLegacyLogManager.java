package ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.logger;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEventsAPI;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.LogManager;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.kyori.adventure.text.ComponentLike;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ApiStatus.Internal
public final class JulLegacyLogManager extends LogManager {
   private static final Logger LOGGER = Logger.getLogger("packetevents");

   public JulLegacyLogManager(PacketEventsAPI<?> packetevents) {
      super(packetevents);
   }

   public static Logger getLogger() {
      return LOGGER;
   }

   @Override
   public void log(Level level, ComponentLike component, @Nullable Throwable error) {
      LOGGER.log(level, LegacyComponentSerializer.legacySection().serialize(component.asComponent()), error);
   }
}
