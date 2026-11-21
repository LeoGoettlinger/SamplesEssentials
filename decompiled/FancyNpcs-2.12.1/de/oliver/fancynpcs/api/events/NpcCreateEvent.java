package de.oliver.fancynpcs.api.events;

import de.oliver.fancynpcs.api.Npc;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NpcCreateEvent extends Event implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   @NotNull
   private final Npc npc;
   @NotNull
   private final CommandSender creator;
   private boolean isCancelled;

   public NpcCreateEvent(@NotNull Npc npc, @NotNull CommandSender creator) {
      this.npc = npc;
      this.creator = creator;
   }

   public static HandlerList getHandlerList() {
      return handlerList;
   }

   @NotNull
   public Npc getNpc() {
      return this.npc;
   }

   @NotNull
   public CommandSender getCreator() {
      return this.creator;
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
