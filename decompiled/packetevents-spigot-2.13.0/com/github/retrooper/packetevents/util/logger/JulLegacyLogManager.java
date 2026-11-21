package com.github.retrooper.packetevents.util.logger;

import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.util.LogManager;
import io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Internal
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
