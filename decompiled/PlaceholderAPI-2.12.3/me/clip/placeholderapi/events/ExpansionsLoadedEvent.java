package me.clip.placeholderapi.events;

import java.util.Collections;
import java.util.List;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ExpansionsLoadedEvent extends Event {
   private final List<PlaceholderExpansion> expansions;
   @NotNull
   private static final HandlerList HANDLERS = new HandlerList();

   public ExpansionsLoadedEvent(List<PlaceholderExpansion> expansions) {
      this.expansions = Collections.unmodifiableList(expansions);
   }

   @NotNull
   public final List<PlaceholderExpansion> getExpansions() {
      return this.expansions;
   }

   @NotNull
   public HandlerList getHandlers() {
      return HANDLERS;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return HANDLERS;
   }
}
