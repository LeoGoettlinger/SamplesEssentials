package me.chancesd.pvpmanager.event;

import me.chancesd.pvpmanager.player.CombatPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerCombatLogEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancelled = false;
   private final Player player;
   private final CombatPlayer pvplayer;

   public PlayerCombatLogEvent(Player player, CombatPlayer pvplayer) {
      this.player = player;
      this.pvplayer = pvplayer;
   }

   public final Player getPlayer() {
      return this.player;
   }

   public final CombatPlayer getCombatPlayer() {
      return this.pvplayer;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
