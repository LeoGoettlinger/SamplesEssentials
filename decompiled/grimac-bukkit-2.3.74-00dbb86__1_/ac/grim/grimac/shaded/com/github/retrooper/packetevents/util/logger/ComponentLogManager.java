package ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.logger;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEventsAPI;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.LogManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.reflection.Reflection;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.shaded.kyori.adventure.text.ComponentLike;
import ac.grim.grimac.shaded.kyori.adventure.text.logger.slf4j.ComponentLogger;
import java.util.logging.Level;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ApiStatus.Internal
public final class ComponentLogManager extends LogManager {
   private static final boolean EXISTS = Reflection.getClassByNameWithoutException("ac.grim.grimac.shaded.kyori.adventure.text.logger.slf4j.ComponentLogger")
      != null;
   private final ComponentLogger logger = ComponentLogger.logger("packetevents");

   public ComponentLogManager(PacketEventsAPI<?> packetevents) {
      super(packetevents);
   }

   public static boolean exists() {
      return EXISTS;
   }

   @Override
   public void log(Level level, ComponentLike component, @Nullable Throwable error) {
      Component message = component.asComponent();
      if (level == Level.FINEST || level == Level.FINER) {
         this.logger.trace(message, error);
      } else if (level == Level.FINE) {
         this.logger.debug(message, error);
      } else if (level == Level.INFO) {
         this.logger.info(message, error);
      } else if (level == Level.WARNING) {
         if (error != null) {
            this.logger.warn(message, error);
         } else {
            this.logger.warn(message);
         }
      } else {
         if (level != Level.SEVERE) {
            throw new UnsupportedOperationException(level + " is unsupported (" + component + ")");
         }

         if (error != null) {
            this.logger.error(message, error);
         } else {
            this.logger.error(message);
         }
      }
   }
}
