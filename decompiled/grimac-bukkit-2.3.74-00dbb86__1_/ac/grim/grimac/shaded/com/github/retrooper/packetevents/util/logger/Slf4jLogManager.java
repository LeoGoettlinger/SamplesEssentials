package ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.logger;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEventsAPI;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.LogManager;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.reflection.Reflection;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.kyori.adventure.text.ComponentLike;
import ac.grim.grimac.shaded.slf4j.Logger;
import ac.grim.grimac.shaded.slf4j.LoggerFactory;
import java.util.logging.Level;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@ApiStatus.Internal
public final class Slf4jLogManager extends LogManager {
   private static final boolean EXISTS = Reflection.getClassByNameWithoutException("ac.grim.grimac.shaded.slf4j.Logger") != null;
   private final Logger logger = LoggerFactory.getLogger("packetevents");

   public Slf4jLogManager(PacketEventsAPI<?> packetevents) {
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
