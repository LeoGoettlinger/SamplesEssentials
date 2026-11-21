package de.oliver.fancynpcs.api.events;

import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.actions.ActionTrigger;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public class NpcPreInteractEvent extends Event implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   @NotNull
   private final Npc npc;
   @NotNull
   private final Player player;
   @NotNull
   private final ActionTrigger actionTrigger;
   private boolean isCancelled;

   public NpcPreInteractEvent(@NotNull Npc npc, @NotNull Player player, @NotNull ActionTrigger actionTrigger) {
      this.npc = npc;
      this.player = player;
      this.actionTrigger = actionTrigger;
   }

   public static HandlerList getHandlerList() {
      return handlerList;
   }

   @NotNull
   public Npc getNpc() {
      return this.npc;
   }

   @NotNull
   public ActionTrigger getInteractionType() {
      return this.actionTrigger;
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
