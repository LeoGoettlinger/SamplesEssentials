package com.magmaguy.elitemobs.magmacore.match;

import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;

public abstract class Match {
   private final MatchSettings settings;
   private final UUID runtimeId = UUID.randomUUID();
   final List<MatchPlayer> participants = new ArrayList<>();
   MatchPhase phase = MatchPhase.WAITING;
   MatchOutcome outcome;
   private boolean open;
   boolean destroyed;
   private boolean resetting;
   final Map<Block, ReviveBanner> reviveBanners = new HashMap<>();
   private final Set<UUID> startingRoster = new LinkedHashSet<>();
   private MatchWatchdog watchdog;
   private BukkitTask countdownTask;
   private BukkitTask destroyTask;

   protected Match(MatchSettings settings) {
      this.settings = Objects.requireNonNull(settings, "settings");
      MatchCore.plugin();
   }

   public final boolean open() {
      if (!this.open && !this.destroyed) {
         if (!this.guard("instantiateAttempt", () -> this.settings.getApi().instantiateAttempt(this), false)) {
            this.destroy();
            return false;
         } else {
            this.open = true;
            MatchCore.register(this);
            this.watchdog = MatchWatchdog.start(this);
            return true;
         }
      } else {
         return this.open;
      }
   }

   public final StartResult start() {
      if (!this.destroyed && this.phase == MatchPhase.WAITING) {
         if (this.getActivePlayers().size() >= this.settings.getMinPlayers()) {
            if (!this.guard("startAttempt", () -> this.settings.getApi().startAttempt(this), false)) {
               return StartResult.VETOED;
            } else if (this.destroyed || this.phase != MatchPhase.WAITING) {
               return StartResult.NOT_WAITING;
            } else if (this.settings.getCountdownSeconds() == 0) {
               this.beginOngoing();
               return StartResult.STARTED;
            } else {
               this.phase = MatchPhase.STARTING;
               this.countdownTask = Bukkit.getScheduler().runTaskTimer(MatchCore.plugin(), new Match.Countdown(), 0L, 20L);
               return StartResult.STARTED;
            }
         } else {
            for (MatchPlayer participant : this.participants) {
               Feedback.message(participant.getPlayer(), this.settings.getMessages().getNotEnoughPlayers(), "$amount", this.settings.getMinPlayers());
            }

            return StartResult.NOT_ENOUGH_PLAYERS;
         }
      } else {
         return StartResult.NOT_WAITING;
      }
   }

   private void beginOngoing() {
      this.phase = MatchPhase.ONGOING;
      Location start = this.startDestination();

      for (MatchPlayer participant : List.copyOf(this.participants)) {
         if (start != null && participant.entered && participant.role == MatchRole.PLAYER) {
            MatchMovement.moveForMatch(this, participant.getPlayer(), participant, start, MoveReason.START);
         }
      }

      this.startingRoster.clear();
      this.getActivePlayers().forEach(participantx -> this.startingRoster.add(participantx.getUniqueId()));

      try {
         this.onStart();
      } catch (LinkageError | RuntimeException var4) {
         Logger.warn("Match " + this.runtimeId + ": onStart failed, destroying the match: " + var4);
         this.destroy();
         return;
      }

      this.guard("started callback", () -> this.settings.getApi().started(this));
   }

   private void cancelCountdown() {
      if (this.countdownTask != null) {
         this.countdownTask.cancel();
      }

      this.countdownTask = null;
   }

   public final AdmissionResult admit(Collection<? extends Player> players) {
      return this.admit(players, () -> true);
   }

   public final AdmissionResult admit(Collection<? extends Player> players, BooleanSupplier stillAuthorized) {
      return Admission.admit(this, players, stillAuthorized);
   }

   public final AdmissionResult admitSpectator(Player player) {
      return Admission.admitSpectator(this, player);
   }

   public final boolean moveParticipant(Player player, Location destination) {
      return MatchMovement.moveForMatch(this, player, destination, MoveReason.MATCH);
   }

   final boolean waitingToStart(Player player) {
      MatchPlayer participant = this.getMatchPlayer(player);
      return (this.phase == MatchPhase.WAITING || this.phase == MatchPhase.STARTING)
         && participant != null
         && participant.role == MatchRole.PLAYER
         && participant.entered
         && player.isOnline()
         && !player.isDead()
         && this.contains(player.getLocation());
   }

   public final boolean leave(Player player, LeaveReason reason) {
      MatchPlayer participant = this.getMatchPlayer(player);
      if (participant == null) {
         return false;
      } else {
         participant.leaveReason = reason;
         this.guard("onLeave", () -> this.onLeave(participant, reason));
         this.guard("left callback", () -> this.settings.getApi().left(this, participant, reason));
         this.participants.remove(participant);
         MatchCore.untrack(participant);
         MatchMarker.clear(player);

         for (ReviveBanner banner : List.copyOf(this.reviveBanners.values())) {
            if (banner.dead() == participant) {
               banner.clear(false);
            }
         }

         this.guard("restore " + player.getName(), () -> this.restore(participant));
         if (!this.destroyed
            && !this.resetting
            && this.getActivePlayers().isEmpty()
            && (this.phase == MatchPhase.WAITING || this.phase == MatchPhase.STARTING || this.phase == MatchPhase.ONGOING)) {
            this.requestEnd(MatchOutcome.DEFEAT);
         }

         return true;
      }
   }

   private void restore(MatchPlayer participant) {
      Player player = participant.getPlayer();
      this.guard("custody restore", () -> this.settings.getCustody().restore(player));
      if (this.settings.getGameMode() != null || participant.role == MatchRole.SPECTATOR) {
         player.setGameMode(participant.getPreviousGameMode());
      }

      if (this.phase == MatchPhase.ENDED && player.isOnline() && !player.isDead()) {
         player.setHealth(maxHealth(player));
      }

      if (player.isOnline() && this.contains(player.getLocation())) {
         Location exit = null;

         try {
            exit = this.exitDestination(participant);
         } catch (LinkageError | RuntimeException var5) {
            Logger.warn("Match " + this.runtimeId + ": exitDestination failed: " + var5);
         }

         if (exit != null) {
            MatchMovement.moveForMatch(this, player, participant, exit, MoveReason.EXIT);
         }
      }
   }

   static double maxHealth(Player player) {
      return AttributeManager.getAttributeValue(player, "generic_max_health");
   }

   public final void end(MatchOutcome outcome) {
      if (this.phase != MatchPhase.ENDED && !this.destroyed) {
         this.cancelCountdown();
         this.outcome = outcome;
         this.phase = MatchPhase.ENDED;

         for (MatchPlayer participant : List.copyOf(this.participants)) {
            Player player = participant.getPlayer();
            if (player.isOnline() && !player.isDead()) {
               this.guard("heal", () -> player.setHealth(maxHealth(player)));
            }
         }

         this.guard("onEnd", () -> this.onEnd(outcome));
         this.guard("ended callback", () -> this.settings.getApi().ended(this, outcome));
         if (this.settings.isDestroyAfterEnd()) {
            if (this.settings.getLingerAfterEndTicks() != 0L && !this.getActivePlayers().isEmpty()) {
               this.scheduleDestroy(this.settings.getLingerAfterEndTicks());
            } else {
               this.destroy();
            }
         }
      }
   }

   final void handleLethalDamage(MatchPlayer participant) {
      Player player = participant.getPlayer();

      for (PotionEffect effect : player.getActivePotionEffects()) {
         player.removePotionEffect(effect.getType());
      }

      player.setFireTicks(0);
      player.setFreezeTicks(0);
      player.setFallDistance(0.0F);
      this.guard("onDeath", () -> this.onDeath(participant));
      if (this.getMatchPlayer(player) == participant) {
         boolean handled = this.guard("death policy", () -> {
            this.settings.getDeath().handle(this, participant);
            return true;
         }, false);
         if (!handled && this.getMatchPlayer(player) == participant) {
            this.leave(player, LeaveReason.DIED);
         }
      }
   }

   final void spectateUntilRevived(MatchPlayer participant, Function<MatchPlayer, ReviveMarker> markers) {
      Player player = participant.getPlayer();
      participant.role = MatchRole.SPECTATOR;
      if (this.getActivePlayers().isEmpty()) {
         this.leave(player, LeaveReason.DIED);
      } else {
         ReviveMarker marker = ReviveMarker.NONE;

         try {
            ReviveMarker supplied = markers.apply(participant);
            if (supplied != null) {
               marker = supplied;
            }
         } catch (LinkageError | RuntimeException var6) {
            Logger.warn("Match " + this.runtimeId + ": revive marker failed: " + var6);
         }

         new ReviveBanner(this, participant, marker);
         player.setGameMode(GameMode.SPECTATOR);
         this.guard("onSpectating", () -> this.onSpectating(participant));
      }
   }

   final void revive(ReviveBanner banner) {
      MatchPlayer dead = banner.dead();
      Player player = dead.getPlayer();
      if (!this.destroyed && this.phase == MatchPhase.ONGOING && this.getMatchPlayer(player) == dead) {
         dead.lives--;
         dead.role = MatchRole.PLAYER;
         player.setGameMode(this.settings.getGameMode() != null ? this.settings.getGameMode() : GameMode.SURVIVAL);
         player.setHealth(maxHealth(player));
         MatchMovement.moveForMatch(this, player, dead, banner.respawnLocation(), MoveReason.REVIVE);
         this.guard("onRevive", () -> this.onRevive(dead));
      }
   }

   protected final boolean releaseReviveBanner(Player dead, boolean revive) {
      MatchPlayer participant = this.getMatchPlayer(dead);
      if (participant == null) {
         return false;
      } else {
         for (ReviveBanner banner : List.copyOf(this.reviveBanners.values())) {
            if (banner.dead() == participant) {
               banner.clear(revive);
               return true;
            }
         }

         return false;
      }
   }

   protected final void handleDeath(Player player) {
      MatchPlayer participant = this.getMatchPlayer(player);
      if (participant != null && participant.role == MatchRole.PLAYER) {
         if (this.phase != MatchPhase.ONGOING) {
            this.leave(player, LeaveReason.DIED);
         } else {
            this.handleLethalDamage(participant);
         }
      }
   }

   protected void requestEnd(MatchOutcome outcome) {
      this.end(outcome);
   }

   protected final void scheduleDestroy(long ticks) {
      if (!this.destroyed) {
         if (this.destroyTask != null) {
            this.destroyTask.cancel();
         }

         this.destroyTask = Bukkit.getScheduler().runTaskLater(MatchCore.plugin(), () -> this.destroy(), ticks);
      }
   }

   public final void destroy() {
      if (this.settings.isReusable() && this.open && !this.destroyed) {
         this.reset();
      } else {
         this.destroy(LeaveReason.MATCH_ENDED);
      }
   }

   public final void retire() {
      this.destroy(LeaveReason.MATCH_ENDED);
   }

   private void reset() {
      if (!this.resetting) {
         this.resetting = true;

         try {
            this.cancelCountdown();
            if (this.destroyTask != null) {
               this.destroyTask.cancel();
            }

            this.destroyTask = null;

            for (MatchPlayer participant : List.copyOf(this.participants)) {
               this.guard("remove " + participant.getPlayer().getName(), () -> this.leave(participant.getPlayer(), LeaveReason.MATCH_ENDED));
            }

            this.phase = MatchPhase.WAITING;
            this.outcome = null;
            this.startingRoster.clear();
            this.guard("onReset", this::onReset);
            this.guard("destroyed callback", () -> this.settings.getApi().destroyed(this));
         } finally {
            this.resetting = false;
         }
      }
   }

   public final void destroyForShutdown() {
      this.destroy(LeaveReason.SHUTDOWN);
   }

   private void destroy(LeaveReason reason) {
      if (!this.destroyed) {
         this.destroyed = true;
         if (this.watchdog != null) {
            this.watchdog.cancel();
         }

         this.cancelCountdown();
         if (this.destroyTask != null) {
            this.destroyTask.cancel();
         }

         if (this.outcome == null) {
            this.outcome = MatchOutcome.NEUTRAL;
         }

         for (MatchPlayer participant : List.copyOf(this.participants)) {
            this.guard("remove " + participant.getPlayer().getName(), () -> this.leave(participant.getPlayer(), reason));
         }

         this.guard("onDestroy", this::onDestroy);
         if (this.settings.getSpace() instanceof TemporaryWorlds) {
            this.guard("evacuation", this::evacuateTemporaryWorlds);
         }

         this.guard("space teardown", this.settings.getSpace()::teardown);
         MatchCore.unregister(this);
         this.phase = MatchPhase.DESTROYED;
         if (this.open) {
            this.guard("destroyed callback", () -> this.settings.getApi().destroyed(this));
         }
      }
   }

   private void evacuateTemporaryWorlds() {
      for (World world : this.settings.getSpace().worlds()) {
         for (Player player : List.copyOf(world.getPlayers())) {
            Location destination = this.intruderDestination(player);
            if (destination != null) {
               player.teleport(destination, TeleportCause.PLUGIN);
            }
         }
      }
   }

   public UUID getRuntimeId() {
      return this.runtimeId;
   }

   public final MatchPhase getPhase() {
      return this.phase;
   }

   public final MatchOutcome getOutcome() {
      return this.outcome;
   }

   public final MatchSettings getSettings() {
      return this.settings;
   }

   public final Set<UUID> getStartingRoster() {
      return Set.copyOf(this.startingRoster);
   }

   public final boolean isOpen() {
      return this.open && !this.destroyed;
   }

   public final boolean isDestroyed() {
      return this.destroyed;
   }

   public final boolean contains(Location location) {
      return this.settings.getSpace().contains(location);
   }

   public final MatchPlayer getMatchPlayer(Player player) {
      for (MatchPlayer participant : this.participants) {
         if (participant.getPlayer().equals(player)) {
            return participant;
         }
      }

      return null;
   }

   public final List<MatchPlayer> getMatchPlayers() {
      return List.copyOf(this.participants);
   }

   public final List<MatchPlayer> getActivePlayers() {
      return this.participants.stream().filter(participant -> participant.role == MatchRole.PLAYER).toList();
   }

   protected MatchPlayer createPlayer(Player player) {
      return new MatchPlayer(player, this);
   }

   protected Location entryDestination(MatchPlayer player) {
      return this.phase == MatchPhase.WAITING && this.settings.getLobby() != null ? this.settings.getLobby() : this.startDestination();
   }

   protected Location spectatorDestination(MatchPlayer player) {
      return this.startDestination();
   }

   protected Location startDestination() {
      return this.settings.getStart();
   }

   protected Location exitDestination(MatchPlayer player) {
      if (this.settings.getExit() != null) {
         return this.settings.getExit();
      } else {
         Location previous = player.getPreviousLocation();
         World world = previous.getWorld();
         return world != null && Bukkit.getWorld(world.getUID()) == world ? previous : ((World)Bukkit.getWorlds().getFirst()).getSpawnLocation();
      }
   }

   protected Location intruderDestination(Player player) {
      return this.settings.getExit() != null ? this.settings.getExit() : ((World)Bukkit.getWorlds().getFirst()).getSpawnLocation();
   }

   protected boolean acceptsSpectator(Player player) {
      return this.settings.isSpectatable();
   }

   protected boolean reserveAdmission() {
      return true;
   }

   protected void abortAdmission() {
   }

   protected void onJoin(MatchPlayer player) {
   }

   protected void onStart() {
   }

   protected void onDeath(MatchPlayer player) {
   }

   protected void onLeave(MatchPlayer player, LeaveReason reason) {
   }

   protected void onEnd(MatchOutcome outcome) {
   }

   protected void onDestroy() {
   }

   protected String requiredPermission() {
      return this.settings.getPermission();
   }

   protected void onAdmissionRolledBack(MatchPlayer player) {
   }

   protected boolean acceptsPlayers() {
      return true;
   }

   protected void onSpectating(MatchPlayer player) {
   }

   protected void onRevive(MatchPlayer player) {
   }

   protected void onReset() {
   }

   final boolean guard(String what, BooleanSupplier call, boolean fallback) {
      try {
         return call.getAsBoolean();
      } catch (LinkageError | RuntimeException var5) {
         Logger.warn("Match " + this.runtimeId + ": " + what + " failed: " + var5);
         return fallback;
      }
   }

   final void guard(String what, Runnable call) {
      this.guard(what, () -> {
         call.run();
         return true;
      }, true);
   }

   private final class Countdown implements Runnable {
      private int counter;

      @Override
      public void run() {
         if (!Match.this.destroyed && Match.this.phase == MatchPhase.STARTING) {
            if (Match.this.getActivePlayers().size() < Match.this.settings.getMinPlayers()) {
               Match.this.cancelCountdown();
               Match.this.requestEnd(MatchOutcome.NEUTRAL);
            } else {
               this.counter++;
               MatchMessages messages = Match.this.settings.getMessages();
               int remaining = Match.this.settings.getCountdownSeconds() - this.counter;

               for (MatchPlayer participant : Match.this.participants) {
                  Feedback.title(participant.getPlayer(), messages.getStartingTitle(), messages.getStartingSubtitle(), 0, 20, 0, "$count", remaining);
               }

               if (this.counter >= Match.this.settings.getCountdownSeconds()) {
                  Match.this.cancelCountdown();
                  Match.this.beginOngoing();
               }
            }
         } else {
            Match.this.cancelCountdown();
         }
      }
   }
}
