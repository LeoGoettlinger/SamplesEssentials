package me.chancesd.pvpmanager.event;

import me.chancesd.pvpmanager.player.CombatPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerTogglePvPEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancelled;
   private final Player player;
   private final CombatPlayer pvplayer;
   private final boolean pvpState;

   public PlayerTogglePvPEvent(Player player, CombatPlayer pvplayer, boolean pvpState) {
      this.player = player;
      this.pvplayer = pvplayer;
      this.pvpState = pvpState;
   }

   public final Player getPlayer() {
      return this.player;
   }

   public final CombatPlayer getCombatPlayer() {
      return this.pvplayer;
   }

   public final boolean getPvPState() {
      return this.pvpState;
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
