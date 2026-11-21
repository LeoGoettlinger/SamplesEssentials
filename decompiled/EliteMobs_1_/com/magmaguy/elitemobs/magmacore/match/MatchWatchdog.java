package com.magmaguy.elitemobs.magmacore.match;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

final class MatchWatchdog implements Runnable {
   private static final long WAITING_HINT_PERIOD_TICKS = 1200L;
   private static final int RESCUE_ATTEMPTS_BEFORE_FALLBACK = 3;
   private static final int RESCUE_LOOP_WARNING_THRESHOLD = 10;
   private final Match match;
   private BukkitTask task;
   private long ticks;

   private MatchWatchdog(Match match) {
      this.match = match;
   }

   static MatchWatchdog start(Match match) {
      MatchWatchdog watchdog = new MatchWatchdog(match);
      watchdog.task = Bukkit.getScheduler().runTaskTimer(MatchCore.plugin(), watchdog, 0L, 1L);
      return watchdog;
   }

   static void rescue(Match match, MatchPlayer participant) {
      Player player = participant.getPlayer();
      int attempts = ++participant.consecutiveRescues;
      Location destination = participant.lastSafeLocation;
      if (destination == null || attempts > 3) {
         destination = match.startDestination();
      }

      if (attempts == 10 && destination != null && destination.getWorld() != null) {
         Logger.warn(
            "Player "
               + player.getName()
               + " has been rescued from the void "
               + attempts
               + " times in a row in match world '"
               + destination.getWorld().getName()
               + "'. The ground at the rescue point is probably missing, which would otherwise loop forever. Check the geometry around "
               + destination.getBlockX()
               + ","
               + destination.getBlockY()
               + ","
               + destination.getBlockZ()
               + "."
         );
      }

      if (destination != null) {
         player.setFallDistance(0.0F);
         MatchMovement.moveForMatch(match, player, participant, destination, MoveReason.RESCUE);
      }
   }

   void cancel() {
      if (this.task != null) {
         this.task.cancel();
      }

      this.task = null;
   }

   @Override
   public void run() {
      if (this.match.isDestroyed()) {
         this.cancel();
      } else {
         for (MatchPlayer participant : this.match.getMatchPlayers()) {
            if (!participant.getPlayer().isOnline()) {
               this.match.leave(participant.getPlayer(), LeaveReason.DISCONNECT);
            } else if (this.match.getMatchPlayer(participant.getPlayer()) == participant && participant.entered) {
               if (participant.role == MatchRole.PLAYER) {
                  this.watchPlayer(participant);
               } else {
                  this.watchSpectator(participant);
               }
            }
         }

         if (this.match.phase == MatchPhase.STARTING || this.match.phase == MatchPhase.ONGOING) {
            this.ejectIntruders();
         }

         if (this.match.phase == MatchPhase.WAITING && this.ticks % 1200L == 0L) {
            this.remindWaitingPlayers();
         }

         this.ticks++;
      }
   }

   private void watchPlayer(MatchPlayer participant) {
      Location location = participant.getPlayer().getLocation();
      if (!isBelowWorld(location) && this.match.contains(location)) {
         this.rememberSafeLocation(participant, location);
      } else {
         rescue(this.match, participant);
      }
   }

   private void rememberSafeLocation(MatchPlayer participant, Location location) {
      if (participant.getPlayer().isOnGround()) {
         Location stored = participant.lastSafeLocation;
         if (stored == null
            || stored.getBlockX() != location.getBlockX()
            || stored.getBlockY() != location.getBlockY()
            || stored.getBlockZ() != location.getBlockZ()
            || stored.getWorld() != location.getWorld()) {
            participant.lastSafeLocation = location.clone();
         }

         participant.consecutiveRescues = 0;
      }
   }

   private void watchSpectator(MatchPlayer participant) {
      Player player = participant.getPlayer();
      Entity target = player.getSpectatorTarget();
      if (target != null && !target.equals(player)) {
         boolean fellowParticipant = target instanceof Player targetPlayer && this.match.getMatchPlayer(targetPlayer) != null;
         if (!fellowParticipant && player.getGameMode() == GameMode.SPECTATOR) {
            player.setSpectatorTarget(null);
         }
      }

      if (!this.match.contains(player.getLocation())) {
         rescue(this.match, participant);
      }
   }

   private void ejectIntruders() {
      String bypass = this.match.getSettings().getBypassPermission();

      for (World world : this.match.getSettings().getSpace().worlds()) {
         for (Player player : world.getPlayers()) {
            if (this.match.getMatchPlayer(player) == null && (bypass == null || !player.hasPermission(bypass)) && this.match.contains(player.getLocation())) {
               Location destination = null;

               try {
                  destination = this.match.intruderDestination(player);
               } catch (LinkageError | RuntimeException var8) {
                  Logger.warn("Match " + this.match.getRuntimeId() + ": intruderDestination failed: " + var8);
               }

               if (destination != null) {
                  MatchMovement.moveForMatch(this.match, player, null, destination, MoveReason.EXIT);
               }
            }
         }
      }
   }

   private static boolean isBelowWorld(Location location) {
      return location.getWorld() != null && location.getY() < location.getWorld().getMinHeight();
   }

   private void remindWaitingPlayers() {
      MatchSettings settings = this.match.getSettings();

      for (MatchPlayer participant : this.match.getMatchPlayers()) {
         Feedback.message(participant.getPlayer(), settings.getMessages().getWaitingHint(), "$count", settings.getMinPlayers());
      }
   }
}
