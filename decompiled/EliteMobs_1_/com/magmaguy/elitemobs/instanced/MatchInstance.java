package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.PlayerJoinArenaEvent;
import com.magmaguy.elitemobs.api.PlayerJoinDungeonEvent;
import com.magmaguy.elitemobs.api.PlayerLeaveArenaEvent;
import com.magmaguy.elitemobs.api.PlayerLeaveDungeonEvent;
import com.magmaguy.elitemobs.api.instanced.MatchDestroyEvent;
import com.magmaguy.elitemobs.api.instanced.MatchEndEvent;
import com.magmaguy.elitemobs.api.instanced.MatchInstantiateEvent;
import com.magmaguy.elitemobs.api.instanced.MatchJoinEvent;
import com.magmaguy.elitemobs.api.instanced.MatchLeaveEvent;
import com.magmaguy.elitemobs.api.instanced.MatchStartEvent;
import com.magmaguy.elitemobs.collateralminecraftchanges.AlternativeDurabilityLoss;
import com.magmaguy.elitemobs.config.ArenasConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.magmacore.match.DeathPolicy;
import com.magmaguy.elitemobs.magmacore.match.LeaveReason;
import com.magmaguy.elitemobs.magmacore.match.Match;
import com.magmaguy.elitemobs.magmacore.match.MatchApi;
import com.magmaguy.elitemobs.magmacore.match.MatchCore;
import com.magmaguy.elitemobs.magmacore.match.MatchMessages;
import com.magmaguy.elitemobs.magmacore.match.MatchOutcome;
import com.magmaguy.elitemobs.magmacore.match.MatchPhase;
import com.magmaguy.elitemobs.magmacore.match.MatchPlayer;
import com.magmaguy.elitemobs.magmacore.match.MatchRole;
import com.magmaguy.elitemobs.magmacore.match.MatchSettings;
import com.magmaguy.elitemobs.magmacore.match.MatchSpace;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.EventCaller;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class MatchInstance extends Match {
   protected static final HashSet<MatchInstance> instances = new HashSet<>();
   protected final HashMap<Block, InstanceDeathLocation> deathBanners = new HashMap<>();
   protected final Map<Player, Location> previousPlayerLocations = new HashMap<>();
   protected HashSet<Player> players = new HashSet<>();
   protected HashMap<Player, Integer> playerLives = new HashMap<>();
   protected HashSet<Player> participants = new HashSet<>();
   protected final HashSet<UUID> startingParticipantIds = new HashSet<>();
   protected HashSet<Player> spectators = new HashSet<>();
   protected MatchInstance.InstancedRegionState state = MatchInstance.InstancedRegionState.WAITING;
   protected Location lobbyLocation = null;
   protected Location startLocation;
   protected Location exitLocation;
   protected int minPlayers;
   protected int maxPlayers;
   protected World world;
   protected String permission = null;
   protected boolean cancelled = false;
   protected boolean destroyingMatch = false;
   private boolean matchHasStarted = false;
   private boolean matchEndEventFired = false;
   private boolean matchDestroyEventFired = false;

   public MatchInstance(Location startLocation, Location exitLocation, int minPlayers, int maxPlayers) {
      this(new MatchInstance.InstanceSpace(), startLocation, exitLocation, minPlayers, maxPlayers);
   }

   private MatchInstance(MatchInstance.InstanceSpace space, Location startLocation, Location exitLocation, int minPlayers, int maxPlayers) {
      super(settings(space, minPlayers, maxPlayers));
      space.owner = this;
      this.startLocation = startLocation;
      this.exitLocation = exitLocation;
      this.minPlayers = minPlayers;
      this.maxPlayers = maxPlayers;
      if (!this.open()) {
         this.cancelled = true;
      } else {
         instances.add(this);
      }
   }

   private static MatchSettings settings(MatchInstance.InstanceSpace space, int minPlayers, int maxPlayers) {
      MatchCore.enable(MetadataHandler.PLUGIN);
      int max = Math.max(1, maxPlayers);
      return MatchSettings.builder()
         .space(space)
         .players(Math.max(0, Math.min(minPlayers, max)), max)
         .countdownSeconds(3)
         .bypassPermission("elitemobs.*")
         .withinTeleportPermission("elitemobs.instanced.teleport.within")
         .reusable(true)
         .destroyAfterEnd(false)
         .death(
            DungeonsConfig.isAllowSpectatorsInInstancedContent() ? DeathPolicy.spectateAndRevive(3, InstanceDeathLocation::marker) : DeathPolicy.eliminate()
         )
         .messages(
            MatchMessages.builder()
               .notAccepting(ArenasConfig.getArenasOngoingMessage())
               .full(ArenasConfig.getArenaFullMessage())
               .noPermission(null)
               .alreadyInMatch(null)
               .joinedMessage(ArenasConfig.getArenaJoinPlayerMessage())
               .joinedTitle(ArenasConfig.getJoinPlayerTitle())
               .joinedSubtitle(ArenasConfig.getJoinPlayerSubtitle())
               .spectatorMessage(ArenasConfig.getArenaJoinSpectatorMessage())
               .spectatorTitle(ArenasConfig.getJoinSpectatorTitle())
               .spectatorSubtitle(ArenasConfig.getJoinSpectatorSubtitle())
               .notEnoughPlayers(ArenasConfig.getNotEnoughPlayersMessage())
               .startingTitle(ArenasConfig.getStartingTitle())
               .startingSubtitle(ArenasConfig.getStartingSubtitle())
               .waitingHint(ArenasConfig.getArenaStartHintMessage())
               .build()
         )
         .api(MatchInstance.EliteMatchApi.INSTANCE)
         .build();
   }

   public static void shutdown() {
      HashSet<MatchInstance> cloneInstance = new HashSet<>(instances);
      cloneInstance.forEach(matchInstance -> {
         matchInstance.cancelScheduledTasks();
         matchInstance.destroyMatch();
         matchInstance.retire();
      });
      instances.clear();
      MatchCore.shutdown();
   }

   public static MatchInstance getPlayerInstance(Player player) {
      for (MatchInstance matchInstance : instances) {
         for (Player iteratedPlayer : matchInstance.players) {
            if (iteratedPlayer.equals(player)) {
               return matchInstance;
            }
         }
      }

      return null;
   }

   public static MatchInstance getSpectatorInstance(Player player) {
      for (MatchInstance matchInstance : instances) {
         for (Player iteratedPlayer : matchInstance.spectators) {
            if (iteratedPlayer.equals(player)) {
               return matchInstance;
            }
         }
      }

      return null;
   }

   public static MatchInstance getAnyPlayerInstance(Player player) {
      for (MatchInstance matchInstance : instances) {
         for (Player iteratedPlayer : matchInstance.players) {
            if (iteratedPlayer.equals(player)) {
               return matchInstance;
            }
         }

         for (Player iteratedPlayerx : matchInstance.spectators) {
            if (iteratedPlayerx.equals(player)) {
               return matchInstance;
            }
         }
      }

      return null;
   }

   public boolean isDefunct() {
      return this.cancelled || !instances.contains(this);
   }

   public boolean isAcceptingNewPlayers() {
      return !this.isDefunct() && !this.destroyingMatch && this.state == MatchInstance.InstancedRegionState.WAITING;
   }

   public final boolean isWaitingPlayer(Player player) {
      return player != null
         && player.isOnline()
         && !player.isDead()
         && !this.isDefunct()
         && !this.destroyingMatch
         && (this.state == MatchInstance.InstancedRegionState.WAITING || this.state == MatchInstance.InstancedRegionState.STARTING)
         && PlayerData.getMatchInstance(player) == this
         && this.players.contains(player)
         && !this.spectators.contains(player)
         && this.playerLives.containsKey(player)
         && player.getWorld().equals(this.lobbyLocation == null ? this.world : this.lobbyLocation.getWorld());
   }

   protected boolean isAcceptingSpectator(Player player, boolean wasPlayer) {
      return true;
   }

   public final boolean authorizesCombatTarget(Player player, Location targetLocation) {
      return player != null
         && targetLocation != null
         && targetLocation.getWorld() != null
         && this.state == MatchInstance.InstancedRegionState.ONGOING
         && this.players.contains(player)
         && this.world != null
         && this.world.equals(player.getWorld())
         && this.world.equals(targetLocation.getWorld())
         && this.isInRegion(player.getLocation())
         && this.isInRegion(targetLocation);
   }

   public Integer getRemainingLives(Player player) {
      return this.playerLives.get(player);
   }

   public boolean isSpectator(Player player) {
      return this.spectators.contains(player);
   }

   public boolean addNewPlayer(Player player) {
      return InstancePlayerManager.addNewPlayer(player, this);
   }

   public void removePlayer(Player player) {
      this.leave(player, LeaveReason.QUIT);
   }

   protected Location participantExitLocation(Player player) {
      return this.exitLocation;
   }

   protected Location previousLocationOrExit(Player player) {
      Location previous = this.previousPlayerLocations.get(player);
      return previous != null && previous.getWorld() != null && Bukkit.getWorld(previous.getWorld().getUID()) == previous.getWorld()
         ? previous.clone()
         : this.exitLocation;
   }

   public void playerDeath(Player player) {
      this.handleDeath(player);
   }

   public void revivePlayer(Player player, InstanceDeathLocation deathLocation) {
      this.releaseReviveBanner(player, true);
   }

   public void addSpectator(Player player, boolean wasPlayer) {
      if (!wasPlayer) {
         this.admitSpectator(player);
      }
   }

   public void removeSpectator(Player player) {
      this.leave(player, LeaveReason.QUIT);
   }

   public void removeAnyKind(Player player) {
      this.leave(player, LeaveReason.QUIT);
   }

   public void countdownMatch() {
      if (this.state == MatchInstance.InstancedRegionState.WAITING) {
         this.start();
         if (this.getPhase() == MatchPhase.STARTING) {
            this.state = MatchInstance.InstancedRegionState.STARTING;
         }
      }
   }

   protected void cancelScheduledTasks() {
   }

   protected void announce(String message) {
      this.participants.forEach(player -> player.sendMessage(ChatColorConverter.convert(message)));
   }

   protected abstract boolean isInRegion(Location var1);

   protected boolean guardsEntryDuring(MatchPhase phase) {
      return phase == MatchPhase.STARTING || phase == MatchPhase.ONGOING;
   }

   protected void startMatch() {
      this.matchHasStarted = true;
      this.matchEndEventFired = false;
      this.matchDestroyEventFired = false;
      this.state = MatchInstance.InstancedRegionState.ONGOING;
      this.startingParticipantIds.clear();
      this.startingParticipantIds.addAll(this.getStartingRoster());
      new EventCaller(new MatchStartEvent(this));
   }

   protected void victory() {
      this.endWith(MatchOutcome.VICTORY, MatchInstance.InstancedRegionState.COMPLETED_VICTORY);
   }

   protected void defeat() {
      this.endWith(MatchOutcome.DEFEAT, MatchInstance.InstancedRegionState.COMPLETED_DEFEAT);
   }

   private void endWith(MatchOutcome outcome, MatchInstance.InstancedRegionState endState) {
      if (this.getPhase() != MatchPhase.ENDED && !this.isDestroyed()) {
         this.state = endState;
         this.end(outcome);
         this.endMatch();
      }
   }

   protected void endMatch() {
      if (this.getPhase() != MatchPhase.ENDED && !this.isDestroyed()) {
         if (this.state != MatchInstance.InstancedRegionState.COMPLETED_VICTORY && this.state != MatchInstance.InstancedRegionState.COMPLETED_DEFEAT) {
            this.state = MatchInstance.InstancedRegionState.COMPLETED;
         }

         this.end(MatchOutcome.NEUTRAL);
      }

      if (this.matchHasStarted && !this.matchEndEventFired) {
         this.matchEndEventFired = true;
         new EventCaller(new MatchEndEvent(this));
      }
   }

   protected void destroyMatch() {
      if (!this.destroyingMatch) {
         this.destroyingMatch = true;

         try {
            this.destroy();
         } finally {
            this.destroyingMatch = false;
         }
      }
   }

   protected final void retireInstance() {
      instances.remove(this);
      this.retire();
   }

   protected boolean hasMatchDestroyEventFired() {
      return this.matchDestroyEventFired;
   }

   protected InstanceDeathLocation getDeathLocationByPlayer(Player player) {
      for (InstanceDeathLocation deathLocation : this.deathBanners.values()) {
         if (deathLocation.getDeadPlayer().equals(player)) {
            return deathLocation;
         }
      }

      return null;
   }

   boolean releaseBanner(Player dead, boolean revive) {
      return this.releaseReviveBanner(dead, revive);
   }

   @Override
   protected MatchPlayer createPlayer(Player player) {
      EliteMatchPlayer participant = new EliteMatchPlayer(player, this);
      this.participants.add(player);
      this.players.add(player);
      this.previousPlayerLocations.put(player, participant.getPreviousLocation());
      PlayerData.setMatchInstance(player, this);
      return participant;
   }

   @Override
   protected void onAdmissionRolledBack(MatchPlayer participant) {
      Player player = participant.getPlayer();
      this.forget(player);
      this.previousPlayerLocations.remove(player);
   }

   @Override
   protected boolean acceptsPlayers() {
      return this.isAcceptingNewPlayers();
   }

   @Override
   protected boolean acceptsSpectator(Player player) {
      return !this.isDefunct() && !this.destroyingMatch && this.isAcceptingSpectator(player, false);
   }

   @Override
   protected String requiredPermission() {
      return this.permission;
   }

   @Override
   protected Location entryDestination(MatchPlayer participant) {
      return this.state == MatchInstance.InstancedRegionState.WAITING && this.lobbyLocation != null ? this.lobbyLocation : this.startLocation;
   }

   @Override
   protected Location startDestination() {
      return this.startLocation;
   }

   @Override
   protected Location exitDestination(MatchPlayer participant) {
      Player player = participant.getPlayer();
      if (participant.getLeaveReason() == LeaveReason.DIED) {
         Location previous = this.previousPlayerLocations.get(player);
         return previous != null && previous.getWorld() != null && Bukkit.getWorld(previous.getWorld().getUID()) == previous.getWorld()
            ? previous.clone()
            : this.exitLocation;
      } else {
         return this.participantExitLocation(player);
      }
   }

   @Override
   protected Location intruderDestination(Player player) {
      if (this.exitLocation != null) {
         return this.exitLocation;
      } else {
         Location back = PlayerData.getBackTeleportLocation(player);
         if (back != null) {
            return back;
         } else {
            Location spawn = DefaultConfig.getDefaultSpawnLocation();
            return spawn != null && spawn.getWorld() != null ? spawn : null;
         }
      }
   }

   @Override
   protected void onStart() {
      this.state = MatchInstance.InstancedRegionState.ONGOING;
      this.startMatch();
   }

   @Override
   protected void requestEnd(MatchOutcome outcome) {
      switch (outcome) {
         case VICTORY:
            this.victory();
            break;
         case DEFEAT:
            this.defeat();
            break;
         case NEUTRAL:
            this.endMatch();
      }
   }

   @Override
   protected void onDeath(MatchPlayer participant) {
      Player player = participant.getPlayer();
      AlternativeDurabilityLoss.doDurabilityLoss(player);
      AttributeManager.setAttribute(player, "generic_max_health", AttributeManager.getAttributeBaseValue(player, "generic_max_health"));
   }

   @Override
   protected void onSpectating(MatchPlayer participant) {
      this.players.remove(participant.getPlayer());
      this.spectators.add(participant.getPlayer());
   }

   @Override
   protected void onRevive(MatchPlayer participant) {
      Player player = participant.getPlayer();
      this.spectators.remove(player);
      this.players.add(player);
      this.playerLives.put(player, participant.getLives());
   }

   @Override
   protected void onReset() {
      this.state = MatchInstance.InstancedRegionState.WAITING;
      this.startingParticipantIds.clear();
      this.matchHasStarted = false;
   }

   private void forget(Player player) {
      this.participants.remove(player);
      this.players.remove(player);
      this.spectators.remove(player);
      this.playerLives.remove(player);
      if (PlayerData.getMatchInstance(player) == this) {
         PlayerData.setMatchInstance(player, null);
      }
   }

   private void fireTypedJoinEvent(Player player) {
      if (this instanceof ArenaInstance arenaInstance) {
         new EventCaller(new PlayerJoinArenaEvent(arenaInstance, player));
      } else if (this instanceof DungeonInstance dungeonInstance) {
         new EventCaller(new PlayerJoinDungeonEvent(dungeonInstance, player));
      }
   }

   private void fireLeaveEvents(Player player) {
      new EventCaller(new MatchLeaveEvent(this, player));
      if (this instanceof ArenaInstance arenaInstance) {
         new EventCaller(new PlayerLeaveArenaEvent(arenaInstance, player));
      } else if (this instanceof DungeonInstance dungeonInstance) {
         new EventCaller(new PlayerLeaveDungeonEvent(dungeonInstance, player));
      }
   }

   @Generated
   public HashMap<Block, InstanceDeathLocation> getDeathBanners() {
      return this.deathBanners;
   }

   @Generated
   public Map<Player, Location> getPreviousPlayerLocations() {
      return this.previousPlayerLocations;
   }

   @Generated
   public HashSet<Player> getPlayers() {
      return this.players;
   }

   @Generated
   public HashSet<Player> getParticipants() {
      return this.participants;
   }

   @Generated
   public HashSet<UUID> getStartingParticipantIds() {
      return this.startingParticipantIds;
   }

   @Generated
   public MatchInstance.InstancedRegionState getState() {
      return this.state;
   }

   @Generated
   public String getPermission() {
      return this.permission;
   }

   @Generated
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Generated
   public boolean isDestroyingMatch() {
      return this.destroyingMatch;
   }

   private static final class EliteMatchApi implements MatchApi {
      private static final MatchInstance.EliteMatchApi INSTANCE = new MatchInstance.EliteMatchApi();

      @Override
      public boolean instantiateAttempt(Match match) {
         MatchInstantiateEvent event = new MatchInstantiateEvent((MatchInstance)match);
         new EventCaller(event);
         return !event.isCancelled();
      }

      @Override
      public boolean joinAttempt(Match match, Player player) {
         if (!PlayerData.isInMemory(player.getUniqueId())) {
            return false;
         } else {
            MatchJoinEvent event = new MatchJoinEvent((MatchInstance)match, player);
            new EventCaller(event);
            return !event.isCancelled();
         }
      }

      @Override
      public void joined(Match match, MatchPlayer participant) {
         MatchInstance instance = (MatchInstance)match;
         Player player = participant.getPlayer();
         if (participant.getRole() == MatchRole.SPECTATOR) {
            instance.players.remove(player);
            instance.spectators.add(player);
         }

         instance.fireTypedJoinEvent(player);
      }

      @Override
      public void left(Match match, MatchPlayer participant, LeaveReason reason) {
         MatchInstance instance = (MatchInstance)match;
         Player player = participant.getPlayer();
         boolean wasParticipant = instance.participants.contains(player);
         instance.forget(player);
         if (wasParticipant) {
            instance.fireLeaveEvents(player);
         }
      }

      @Override
      public void destroyed(Match match) {
         MatchInstance instance = (MatchInstance)match;
         if (!instance.matchDestroyEventFired) {
            instance.matchDestroyEventFired = true;
            new EventCaller(new MatchDestroyEvent(instance));
         }
      }
   }

   private static final class InstanceSpace implements MatchSpace {
      private MatchInstance owner;

      @Override
      public boolean contains(Location location) {
         if (this.owner != null && location != null && location.getWorld() != null && this.owner.world != null) {
            try {
               return this.owner.isInRegion(location);
            } catch (RuntimeException var3) {
               return false;
            }
         } else {
            return false;
         }
      }

      @Override
      public Collection<World> worlds() {
         return this.owner != null && this.owner.world != null ? List.of(this.owner.world) : List.of();
      }

      @Override
      public boolean guardsEntryDuring(MatchPhase phase) {
         return this.owner != null && this.owner.guardsEntryDuring(phase);
      }

      @Override
      public void teardown() {
      }
   }

   public static enum InstancedRegionState {
      WAITING,
      STARTING,
      ONGOING,
      COMPLETED,
      COMPLETED_VICTORY,
      COMPLETED_DEFEAT;
   }

   public static class MatchInstanceEvents implements Listener {
   }
}
