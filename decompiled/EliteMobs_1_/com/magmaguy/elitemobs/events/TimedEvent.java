package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.CustomEventStartEvent;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfig;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.CustomSpawn;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.WeightedProbability;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TimedEvent extends CustomEvent implements Listener {
   protected static List<TimedEvent> blueprintEvents = new ArrayList<>();
   protected static List<TimedEvent> timedEvents = new ArrayList<>();
   private static BukkitTask eventPickerTask = null;
   private static TimedEvent startingEvent;
   private static double nextEventTrigger = System.currentTimeMillis() + 300000.0;
   private static double nextEventStartMinimum = System.currentTimeMillis();
   private final double localCooldown;
   private final double globalCooldown;
   private final double weight;
   private final String filename;
   private double nextLocalEventTrigger = 0.0;
   private CustomSpawn customSpawn;
   private boolean silentRetry = false;

   public TimedEvent(CustomEventsConfigFields customEventsConfigFields) {
      super(customEventsConfigFields);
      this.localCooldown = customEventsConfigFields.getLocalCooldown();
      this.globalCooldown = customEventsConfigFields.getGlobalCooldown();
      this.weight = customEventsConfigFields.getWeight();
      this.filename = customEventsConfigFields.getFilename();
   }

   public static void shutdown() {
      if (eventPickerTask != null && !eventPickerTask.isCancelled()) {
         eventPickerTask.cancel();
         eventPickerTask = null;
      }

      blueprintEvents.clear();
      timedEvents.clear();
      startingEvent = null;
   }

   public static void initializeBlueprintEvents() {
      if (EventsConfig.isTimedEventsEnabled()) {
         for (CustomEventsConfigFields customEventsConfigFields : CustomEventsConfig.getCustomEvents().values()) {
            if (customEventsConfigFields.isEnabled() && customEventsConfigFields.getEventType() == CustomEvent.EventType.TIMED) {
               blueprintEvents.add(new TimedEvent(customEventsConfigFields));
            }
         }

         startEventPicker();
      }
   }

   private static void startEventPicker() {
      eventPickerTask = (new BukkitRunnable() {
         public void run() {
            if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {
               if (!(System.currentTimeMillis() < TimedEvent.nextEventTrigger)) {
                  TimedEvent.pickEvent();
               }
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 6000L, 1200L);
   }

   private static void pickEvent() {
      HashMap<String, Double> weighedProbabilities = new HashMap<>();

      for (TimedEvent timedEvent : blueprintEvents) {
         if (timedEvent.startConditions.areValid()) {
            boolean isRunning = false;

            for (TimedEvent activeTimedEvent : timedEvents) {
               if (activeTimedEvent != null
                  && activeTimedEvent.getCustomEventsConfigFields().getFilename().equals(timedEvent.getCustomEventsConfigFields().getFilename())) {
                  isRunning = true;
                  break;
               }
            }

            if (!isRunning && timedEvent.nextLocalEventTrigger < System.currentTimeMillis()) {
               weighedProbabilities.put(timedEvent.filename, timedEvent.weight);
            }
         }
      }

      String pickedEvent = WeightedProbability.pickWeighedProbability(weighedProbabilities);

      for (TimedEvent timedEventx : blueprintEvents) {
         if (timedEventx.filename.equals(pickedEvent)) {
            timedEventx.nextLocalEventTrigger = System.currentTimeMillis() + timedEventx.localCooldown * 60.0 * 1000.0;
            timedEventx.instantiateEvent();
            return;
         }
      }
   }

   public TimedEvent.EventStartResult instantiateEvent() {
      TimedEvent timedEvent = new TimedEvent(this.customEventsConfigFields);
      CustomEventStartEvent customEventStartEvent = new CustomEventStartEvent(timedEvent);
      new EventCaller(customEventStartEvent);
      if (customEventStartEvent.isCancelled()) {
         Logger.warn("Event " + this.getCustomEventsConfigFields().getFilename() + " was cancelled by another plugin.");
         return TimedEvent.EventStartResult.CANCELLED_BY_PLUGIN;
      } else {
         String spawnType = this.customEventsConfigFields.getSpawnType();
         if (spawnType != null && !spawnType.isEmpty()) {
            timedEvent.customSpawn = new CustomSpawn(spawnType, this.customEventsConfigFields.getBossFilenames(), timedEvent);
            if (timedEvent.customSpawn.getCustomSpawnConfigFields() == null) {
               Logger.warn(
                  "Event "
                     + this.getCustomEventsConfigFields().getFilename()
                     + " failed to start because its spawnType '"
                     + spawnType
                     + "' is not a valid custom spawn file. Make sure the file exists in the customspawns folder and is enabled."
               );
               return TimedEvent.EventStartResult.INVALID_SPAWN_TYPE;
            } else if (timedEvent.customSpawn.getCustomBossEntities().isEmpty()) {
               Logger.warn(
                  "Event "
                     + this.getCustomEventsConfigFields().getFilename()
                     + " failed to start because none of its boss filenames are valid: "
                     + this.customEventsConfigFields.getBossFilenames()
               );
               return TimedEvent.EventStartResult.NO_VALID_BOSSES;
            } else {
               Logger.info("Event " + this.getCustomEventsConfigFields().getFilename() + " has been queued!");
               timedEvent.customSpawn.setEvent(true);
               timedEvent.customSpawn.queueSpawn();
               this.setNextEventTrigger();
               timedEvents.add(timedEvent);
               return TimedEvent.EventStartResult.SUCCESS;
            }
         } else {
            Logger.warn(
               "Event " + this.getCustomEventsConfigFields().getFilename() + " has no spawnType set! Add a valid custom spawn filename to the event config."
            );
            return TimedEvent.EventStartResult.NO_SPAWN_TYPE;
         }
      }
   }

   private void setNextEventTrigger() {
      this.setNextEventTrigger(this.globalCooldown);
   }

   private void setNextEventTrigger(double timeMinutes) {
      nextEventTrigger = System.currentTimeMillis() + timeMinutes * 60.0 * 1000.0;
   }

   public boolean trySpawn(Runnable spawnAttempt) {
      if (startingEvent == null && this.startConditions.areValid() && !(System.currentTimeMillis() < nextEventStartMinimum)) {
         startingEvent = this;

         boolean var2;
         try {
            spawnAttempt.run();
            var2 = true;
         } finally {
            startingEvent = null;
         }

         return var2;
      } else {
         return false;
      }
   }

   public void queueEvent() {
      this.primaryEliteMobs = this.customSpawn.getCustomBossEntities();
      this.setEventStartLocation(this.customSpawn.getSpawnLocation());

      for (CustomBossEntity customBossEntity : this.primaryEliteMobs) {
         if (!customBossEntity.exists()) {
            if (!this.silentRetry) {
               Logger.info(
                  "Boss "
                     + customBossEntity.getCustomBossesConfigFields().getFilename()
                     + " for event "
                     + this.getCustomEventsConfigFields().getFilename()
                     + " wasn't considered to be valid. Trying spawn again ."
               );
               Logger.info(
                  "Note: further failures will be silent. EliteMobs can only predict WorldGuard protections, so it will keep trying to spawn things until plugins preventing spawning allow it to do so. This might take a while."
               );
               this.silentRetry = true;
            }

            Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
               this.customSpawn.setSpawnLocation(null);
               this.customSpawn.queueSpawn();
            }, 1L);
            return;
         }
      }

      this.primaryEliteMobs.forEach(CustomBossEntity::announceSpawn);
      this.start();
      this.setNextEventTrigger();
      nextEventStartMinimum = System.currentTimeMillis() + EventsConfig.getTimedEventMinimumCooldown() * 60 * 1000.0;
   }

   @Override
   public void startModifiers() {
      this.nextLocalEventTrigger = System.currentTimeMillis() + this.localCooldown * 60000.0;
   }

   @Override
   public void eventWatchdog() {
   }

   @Override
   public void endModifiers() {
      timedEvents.remove(this);
   }

   @Generated
   public static List<TimedEvent> getBlueprintEvents() {
      return blueprintEvents;
   }

   @Generated
   public static double getNextEventTrigger() {
      return nextEventTrigger;
   }

   @Generated
   public static double getNextEventStartMinimum() {
      return nextEventStartMinimum;
   }

   public static enum EventStartResult {
      SUCCESS,
      CANCELLED_BY_PLUGIN,
      NO_SPAWN_TYPE,
      INVALID_SPAWN_TYPE,
      NO_VALID_BOSSES;
   }
}
