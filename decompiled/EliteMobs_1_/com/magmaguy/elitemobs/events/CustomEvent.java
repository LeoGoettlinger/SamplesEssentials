package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.announcements.AnnouncementPriority;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.magmacore.match.Match;
import com.magmaguy.elitemobs.magmacore.match.MatchCore;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.CommandRunner;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public abstract class CustomEvent {
   public StartConditions startConditions;
   public CustomEvent.EventType eventType;
   public ArrayList<CustomBossEntity> primaryEliteMobs = new ArrayList<>();
   public BukkitTask eventWatchdog;
   public int announcementPriority;
   public CustomEventsConfigFields customEventsConfigFields;
   public String startMessage;
   public String endMessage;
   public List<String> startEventCommands;
   public List<String> endEventCommands;
   public List<String> primaryCustomBossFilenames;
   public Location eventStartLocation;
   private long eventStartNanos;
   public int currentDay;

   public CustomEvent(CustomEventsConfigFields customEventsConfigFields) {
      this.startConditions = new StartConditions(customEventsConfigFields);
      this.customEventsConfigFields = customEventsConfigFields;
      this.eventType = customEventsConfigFields.getEventType();
      this.startMessage = customEventsConfigFields.getStartMessage();
      this.endMessage = customEventsConfigFields.getEndMessage();
      this.startEventCommands = customEventsConfigFields.getEventStartCommands();
      this.endEventCommands = customEventsConfigFields.getEventEndCommands();
      this.announcementPriority = customEventsConfigFields.getAnnouncementPriority();
      this.primaryCustomBossFilenames = customEventsConfigFields.getBossFilenames();
   }

   public static boolean isLocationValid(Location location) {
      if (!ValidWorldsConfig.getValidWorlds().contains(Objects.requireNonNull(location.getWorld()).getName())) {
         return false;
      } else if (EliteMobsWorld.isEliteMobsWorld(location.getWorld().getUID())) {
         return false;
      } else {
         for (Match match : MatchCore.matches()) {
            if (match.contains(location)) {
               return false;
            }
         }

         return !EliteMobs.worldGuardIsEnabled || WorldGuardFlagChecker.checkFlag(location, WorldGuardCompatibility.getELITEMOBS_EVENTS());
      }
   }

   public CustomEventsConfigFields getCustomEventsConfigFields() {
      return this.customEventsConfigFields;
   }

   public List<String> getPrimaryCustomBossFilenames() {
      return this.primaryCustomBossFilenames;
   }

   public void setPrimaryCustomBossFilenames(List<String> primaryCustomBossFilenames) {
      this.primaryCustomBossFilenames = primaryCustomBossFilenames;
   }

   public Location getEventStartLocation() {
      return this.eventStartLocation;
   }

   public void setEventStartLocation(Location eventStartLocation) {
      this.eventStartLocation = eventStartLocation;
   }

   public void start() {
      this.startModifiers();
      if (this.primaryEliteMobs.isEmpty()) {
         Logger.warn(
            "Event "
               + this.customEventsConfigFields.getFilename()
               + " has failed to start because the bosses failed to spawn correctly!This could be due to an issue with the configuration of the bosses in the event, with the configuration of the event or due to a protection in the target location!"
         );
         Logger.warn("Target location: " + this.getEventStartLocation().toString());
      }

      if (this.startMessage != null) {
         AnnouncementPriority.announce(this.startMessage, this.eventStartLocation.getWorld(), this.announcementPriority);
      }

      if (this.startEventCommands != null) {
         CommandRunner.runCommandFromList(this.startEventCommands, new ArrayList<>());
      }

      this.eventStartNanos = System.nanoTime();
      this.currentDay = this.dayCalculator();
      this.eventWatchdog = (new BukkitRunnable() {
         public void run() {
            CustomEvent.this.commonWatchdogBehavior();
            CustomEvent.this.eventWatchdog();
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 20L, 20L);
   }

   private int dayCalculator() {
      return (int)Math.floor((float)this.eventStartLocation.getWorld().getFullTime() / 24000.0F);
   }

   public abstract void startModifiers();

   public void commonWatchdogBehavior() {
      if (this.customEventsConfigFields.getEventEndTime() != -1
         && this.eventStartLocation.getWorld().getTime() > this.customEventsConfigFields.getEventEndTime()) {
         this.end();
      } else if (this.customEventsConfigFields.getEventDuration() > 0.0
         && System.nanoTime() - this.eventStartNanos > this.customEventsConfigFields.getEventDuration() * 6.0E10) {
         this.end();
      } else {
         if (this.customEventsConfigFields.isEndEventWithBossDeath()) {
            AtomicBoolean allBossesAreDead = new AtomicBoolean(true);
            this.primaryEliteMobs.forEach(primaryEliteMob -> {
               if (primaryEliteMob.exists()) {
                  allBossesAreDead.set(false);
               }
            });
            if (allBossesAreDead.get()) {
               this.end();
            }
         }
      }
   }

   public abstract void eventWatchdog();

   public void end() {
      Logger.info("Event " + this.customEventsConfigFields.getFilename() + " ended!");
      if (this.eventWatchdog != null) {
         this.eventWatchdog.cancel();
      }

      this.primaryEliteMobs.forEach(eliteMobEntity -> {
         if (eliteMobEntity.exists()) {
            eliteMobEntity.remove(RemovalReason.BOSS_TIMEOUT);
         }
      });
      if (this.endMessage != null && this.eventStartLocation != null && this.eventStartLocation.getWorld() != null) {
         AnnouncementPriority.announce(this.endMessage, this.eventStartLocation.getWorld(), this.announcementPriority);
      }

      if (this.endEventCommands != null) {
         Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> CommandRunner.runCommandFromList(this.endEventCommands, new ArrayList<>()));
      }

      this.endModifiers();
   }

   public abstract void endModifiers();

   public static enum EventType {
      DEFAULT,
      BREAK_BLOCK,
      FISH,
      TILL_SOIL,
      TIMED;
   }
}
