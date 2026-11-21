package com.magmaguy.elitemobs.magmacore.match;

import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class MatchPlayer {
   private final Player player;
   private final Match match;
   private final Location previousLocation;
   private final GameMode previousGameMode;
   MatchRole role = MatchRole.PLAYER;
   int lives;
   boolean entered;
   LeaveReason leaveReason;
   Location lastSafeLocation;
   int consecutiveRescues;

   protected MatchPlayer(Player player, Match match) {
      this.player = player;
      this.match = match;
      this.previousLocation = player.getLocation().clone();
      this.previousGameMode = player.getGameMode();
   }

   protected boolean teleport(Location destination, MoveReason reason) {
      return this.player.teleport(destination, TeleportCause.PLUGIN);
   }

   public Player getPlayer() {
      return this.player;
   }

   public UUID getUniqueId() {
      return this.player.getUniqueId();
   }

   public Match getMatch() {
      return this.match;
   }

   public Location getPreviousLocation() {
      return this.previousLocation.clone();
   }

   public GameMode getPreviousGameMode() {
      return this.previousGameMode;
   }

   public MatchRole getRole() {
      return this.role;
   }

   public int getLives() {
      return this.lives;
   }

   public LeaveReason getLeaveReason() {
      return this.leaveReason;
   }

   public boolean hasEntered() {
      return this.entered;
   }
}
