package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

public class PlayerLeaveArenaEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final ArenaInstance arenaInstance;
   @Nullable
   private final Player player;

   @Deprecated(
      forRemoval = false
   )
   public PlayerLeaveArenaEvent(ArenaInstance arenaInstance) {
      this(arenaInstance, null);
   }

   public PlayerLeaveArenaEvent(ArenaInstance arenaInstance, @Nullable Player player) {
      this.arenaInstance = arenaInstance;
      this.player = player;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   @Generated
   public ArenaInstance getArenaInstance() {
      return this.arenaInstance;
   }

   @Nullable
   @Generated
   public Player getPlayer() {
      return this.player;
   }
}
