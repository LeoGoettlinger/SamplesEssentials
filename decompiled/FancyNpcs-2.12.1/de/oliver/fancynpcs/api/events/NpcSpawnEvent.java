package de.oliver.fancynpcs.api.events;

import de.oliver.fancynpcs.api.Npc;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NpcSpawnEvent extends Event implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   @NotNull
   private final Npc npc;
   @NotNull
   private final Player player;
   private boolean isCancelled;

   public NpcSpawnEvent(@NotNull Npc npc, @NotNull Player player) {
      super(true);
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

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean cancel) {
      this.isCancelled = cancel;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlerList;
   }
}
