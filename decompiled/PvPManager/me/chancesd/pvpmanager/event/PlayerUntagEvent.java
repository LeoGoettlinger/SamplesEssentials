package me.chancesd.pvpmanager.event;

import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.UntagReason;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerUntagEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final CombatPlayer pvplayer;
   private final UntagReason reason;

   public PlayerUntagEvent(Player player, CombatPlayer pvplayer, UntagReason reason) {
      this.player = player;
      this.pvplayer = pvplayer;
      this.reason = reason;
   }

   public final Player getPlayer() {
      return this.player;
   }

   public final CombatPlayer getCombatPlayer() {
      return this.pvplayer;
   }

   public UntagReason getReason() {
      return this.reason;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
