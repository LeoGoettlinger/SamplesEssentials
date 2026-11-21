package me.chancesd.pvpmanager.event;

import me.chancesd.pvpmanager.player.CombatPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

public final class PlayerTagEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancelled;
   private final Player player;
   private final CombatPlayer pvplayer;
   private final boolean isAttacker;
   private final Player enemy;

   public PlayerTagEvent(Player player, CombatPlayer pvplayer, boolean isAttacker, Player enemy) {
      this.player = player;
      this.pvplayer = pvplayer;
      this.isAttacker = isAttacker;
      this.enemy = enemy;
   }

   public PlayerTagEvent(Player player, CombatPlayer pvplayer, boolean isAttacker) {
      this(player, pvplayer, isAttacker, null);
   }

   public final Player getPlayer() {
      return this.player;
   }

   public final CombatPlayer getCombatPlayer() {
      return this.pvplayer;
   }

   public final boolean isAttacker() {
      return this.isAttacker;
   }

   @Nullable
   public final Player getEnemy() {
      return this.enemy;
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
