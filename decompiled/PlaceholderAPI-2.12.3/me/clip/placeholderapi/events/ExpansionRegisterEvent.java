package me.clip.placeholderapi.events;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class ExpansionRegisterEvent extends Event implements Cancellable {
   @NotNull
   private static final HandlerList HANDLERS = new HandlerList();
   @NotNull
   private final PlaceholderExpansion expansion;
   private boolean cancelled;

   public ExpansionRegisterEvent(@NotNull PlaceholderExpansion expansion) {
      this.expansion = expansion;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   @NotNull
   public PlaceholderExpansion getExpansion() {
      return this.expansion;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @NotNull
   public HandlerList getHandlers() {
      return HANDLERS;
   }
}
