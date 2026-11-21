package org.lushplugins.lushrewards.module.playtimetracker;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.libraries.lushlib.module.Module;
import org.lushplugins.lushrewards.libraries.morepaperlib.scheduling.ScheduledTask;

public class PlaytimeTrackerModule extends Module {
   private ConcurrentHashMap<UUID, PlaytimeTracker> playtimeTrackers;
   private ScheduledTask heartbeat;

   public PlaytimeTrackerModule() {
      super("playtime-tracker");
   }

   @Override
   public void onEnable() {
      this.playtimeTrackers = new ConcurrentHashMap<>();
      this.heartbeat = LushRewards.getMorePaperLib().scheduling().asyncScheduler().runAtFixedRate(() -> {
         if (LushRewards.getInstance().getModule("playtime-tracker").isEmpty()) {
            this.heartbeat.cancel();
         } else {
            this.playtimeTrackers.values().forEach(PlaytimeTracker::tick);
         }
      }, Duration.of(0L, ChronoUnit.MILLIS), Duration.of(1000L, ChronoUnit.MILLIS));
   }

   @Override
   public void onDisable() {
      if (this.heartbeat != null) {
         this.heartbeat.cancel();
         this.heartbeat = null;
      }

      if (this.playtimeTrackers != null) {
         this.playtimeTrackers.values().forEach(PlaytimeTracker::saveData);
         this.playtimeTrackers.clear();
         this.playtimeTrackers = null;
      }
   }

   public PlaytimeTracker getPlaytimeTracker(UUID uuid) {
      return this.playtimeTrackers.get(uuid);
   }

   public void startPlaytimeTracker(Player player) {
      this.playtimeTrackers.put(player.getUniqueId(), new PlaytimeTracker(player));
   }

   public PlaytimeTracker stopPlaytimeTracker(UUID uuid) {
      PlaytimeTracker playtimeTracker = this.getPlaytimeTracker(uuid);
      this.playtimeTrackers.remove(uuid);
      return playtimeTracker;
   }
}
