package de.oliver.fancynpcs.api.events;

import de.oliver.fancynpcs.api.Npc;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NpcStartLookingEvent extends Event {
   private static final HandlerList handlerList = new HandlerList();
   @NotNull
   private final Npc npc;
   @NotNull
   private final Player player;

   public NpcStartLookingEvent(@NotNull Npc npc, @NotNull Player player) {
      this.npc = npc;
      this.player = player;
   }

   public static HandlerList getHandlerList() {
      return handlerList;
   }

   @NotNull
   public Npc getNpc() {
      return this.npc;
   }

   @NotNull
   public Player getPlayer() {
      return this.player;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlerList;
   }
}
