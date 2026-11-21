package me.chancesd.pvpmanager.event;

import me.chancesd.pvpmanager.player.CombatPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class NewbieProtectionEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final CombatPlayer pvplayer;
   private final boolean starting;

   public NewbieProtectionEvent(Player player, CombatPlayer pvplayer, boolean starting) {
      this.player = player;
      this.pvplayer = pvplayer;
      this.starting = starting;
   }

   public final Player getPlayer() {
      return this.player;
   }

   public final CombatPlayer getPvPlayer() {
      return this.pvplayer;
   }

   public final boolean isStarting() {
      return this.starting;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}
