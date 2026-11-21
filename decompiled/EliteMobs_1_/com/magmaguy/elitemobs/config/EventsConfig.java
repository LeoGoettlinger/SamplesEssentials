package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import java.util.List;
import lombok.Generated;

public class EventsConfig extends ConfigurationFile {
   private static boolean announcementBroadcastWorldOnly;
   private static int actionEventMinimumCooldown;
   private static int timedEventMinimumCooldown;
   private static boolean actionEventsEnabled;
   private static boolean timedEventsEnabled;
   private static EventsConfig instance;

   public EventsConfig() {
      super("events.yml");
   }

   public static void setEventsEnabled(boolean enabled) {
      ConfigurationEngine.writeValue(enabled, instance.file, instance.fileConfiguration, "actionEventsEnabled");
      ConfigurationEngine.writeValue(enabled, instance.file, instance.fileConfiguration, "timedEventsEnabled");
      timedEventsEnabled = enabled;
      actionEventsEnabled = enabled;
   }

   @Override
   public void initializeValues() {
      instance = this;
      announcementBroadcastWorldOnly = ConfigurationEngine.setBoolean(
         List.of("Whether event announcements are only broadcast in the world where the event is happening."),
         this.fileConfiguration,
         "announcementBroadcastWorldOnly",
         false
      );
      actionEventMinimumCooldown = ConfigurationEngine.setInt(
         List.of("Sets the minimum cooldown, in minutes, between action events"), this.fileConfiguration, "actionEventMinimumCooldownMinutes", 4
      );
      timedEventMinimumCooldown = ConfigurationEngine.setInt(
         List.of("Sets the minimum cooldown, in minutes, between timed events"), this.fileConfiguration, "timedEventMinimumCooldownMinutes", 4
      );
      actionEventsEnabled = ConfigurationEngine.setBoolean(
         List.of("Sets if action events will happen.", "https://github.com/MagmaGuy/EliteMobs/wiki/Creating-Custom-Events#action-events"),
         this.fileConfiguration,
         "actionEventsEnabled",
         true
      );
      timedEventsEnabled = ConfigurationEngine.setBoolean(
         List.of("Sets if timed events will happen.", "https://github.com/MagmaGuy/EliteMobs/wiki/Creating-Custom-Events#timed-events"),
         this.fileConfiguration,
         "timedEventsEnabled",
         true
      );
   }

   @Generated
   public static boolean isAnnouncementBroadcastWorldOnly() {
      return announcementBroadcastWorldOnly;
   }

   @Generated
   public static int getActionEventMinimumCooldown() {
      return actionEventMinimumCooldown;
   }

   @Generated
   public static int getTimedEventMinimumCooldown() {
      return timedEventMinimumCooldown;
   }

   @Generated
   public static boolean isActionEventsEnabled() {
      return actionEventsEnabled;
   }

   @Generated
   public static boolean isTimedEventsEnabled() {
      return timedEventsEnabled;
   }
}
