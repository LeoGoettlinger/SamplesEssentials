package com.github.retrooper.packetevents.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.util.logger.ComponentLogManager;
import com.github.retrooper.packetevents.util.logger.JulLegacyLogManager;
import com.github.retrooper.packetevents.util.logger.Slf4jLogManager;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@NonExtendable
public abstract class LogManager {
   protected static final String LOGGER_NAME = "packetevents";
   private static final char LEGACY_COLOR_CHAR = '§';
   protected final PacketEventsAPI<?> packetevents;

   public LogManager(PacketEventsAPI<?> packetevents) {
      this.packetevents = packetevents;
   }

   @Deprecated
   public LogManager() {
      this(PacketEvents.getAPI());
   }

   private static String stripLegacyColors(String message) {
      int sectionIndex;
      while ((sectionIndex = message.indexOf(167)) != -1 && message.length() >= sectionIndex + 2) {
         message = message.substring(0, sectionIndex) + message.substring(sectionIndex + 2);
      }

      return message;
   }

   @Internal
   public static LogManager construct(PacketEventsAPI<?> packetevents) {
      if (ComponentLogManager.exists()) {
         return new ComponentLogManager(packetevents);
      } else {
         return (LogManager)(Slf4jLogManager.exists() ? new Slf4jLogManager(packetevents) : new JulLegacyLogManager(packetevents));
      }
   }

   protected void log(Level level, @Nullable NamedTextColor color, String message) {
      this.log(level, color, message, null);
   }

   protected void log(Level level, @Nullable NamedTextColor color, String message, @Nullable Throwable error) {
      ComponentLike component = Component.text(stripLegacyColors(message));
      if (color != null) {
         component = Component.text().append(component).color(color);
      }

      this.log(level, component, error);
   }

   public abstract void log(Level level, ComponentLike component, @Nullable Throwable error);

   public void info(String message) {
      this.log(Level.INFO, null, message);
   }

   public void warn(String message) {
      this.log(Level.WARNING, NamedTextColor.YELLOW, message);
   }

   public void warn(String message, @Nullable Throwable error) {
      this.log(Level.WARNING, NamedTextColor.YELLOW, message, error);
   }

   public void severe(String message) {
      this.log(Level.SEVERE, NamedTextColor.RED, message);
   }

   public void severe(String message, @Nullable Throwable error) {
      this.log(Level.SEVERE, NamedTextColor.RED, message, error);
   }

   public void debug(String message) {
      if (this.isDebug()) {
         this.log(Level.FINE, NamedTextColor.GRAY, message);
      }
   }

   public boolean isDebug() {
      return this.packetevents.getSettings().isDebugEnabled();
   }
}
