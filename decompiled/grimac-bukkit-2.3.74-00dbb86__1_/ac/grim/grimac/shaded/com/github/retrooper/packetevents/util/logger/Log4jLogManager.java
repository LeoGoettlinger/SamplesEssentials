package ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.logger;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEventsAPI;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.LogManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.reflection.Reflection;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.kyori.adventure.text.ComponentLike;
import java.util.logging.Level;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ApiStatus.Internal
public final class Log4jLogManager extends LogManager {
   private static final boolean EXISTS = Reflection.getClassByNameWithoutException("org.apache.logging.log4j.Logger") != null
      && Reflection.getClassByNameWithoutException("org.apache.logging.log4j.LogManager") != null;
   private final Logger logger = org.apache.logging.log4j.LogManager.getLogger("packetevents");

   public Log4jLogManager(PacketEventsAPI<?> packetevents) {
      super(packetevents);
   }

   public static boolean exists() {
      return EXISTS;
   }

   @Override
   public void log(Level level, ComponentLike component, @Nullable Throwable error) {
      String message = AdventureSerializer.stringify(component.asComponent());
      if (level == Level.FINEST || level == Level.FINER) {
         this.logger.trace(message, error);
      } else if (level == Level.FINE) {
         this.logger.debug(message, error);
      } else if (level == Level.INFO) {
         this.logger.info(message, error);
      } else if (level == Level.WARNING) {
         this.logger.warn(message, error);
      } else {
         if (level != Level.SEVERE) {
            throw new UnsupportedOperationException(level + " is unsupported (" + component + ")");
         }

         this.logger.error(message, error);
      }
   }
}
