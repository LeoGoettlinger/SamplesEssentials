package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

public class PlayerJoinDungeonEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final DungeonInstance dungeonInstance;
   @Nullable
   private final Player player;

   @Deprecated(
      forRemoval = false
   )
   public PlayerJoinDungeonEvent(DungeonInstance dungeonInstance) {
      this(dungeonInstance, null);
   }

   public PlayerJoinDungeonEvent(DungeonInstance dungeonInstance, @Nullable Player player) {
      this.dungeonInstance = dungeonInstance;
      this.player = player;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   @Generated
   public DungeonInstance getDungeonInstance() {
      return this.dungeonInstance;
   }

   @Nullable
   @Generated
   public Player getPlayer() {
      return this.player;
   }
}
