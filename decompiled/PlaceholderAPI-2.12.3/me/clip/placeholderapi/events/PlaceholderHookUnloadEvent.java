package me.clip.placeholderapi.events;

import me.clip.placeholderapi.PlaceholderHook;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Deprecated
public final class PlaceholderHookUnloadEvent extends Event {
   @NotNull
   private static final HandlerList HANDLERS = new HandlerList();
   @NotNull
   private final String plugin;
   @NotNull
   private final PlaceholderHook placeholderHook;

   public PlaceholderHookUnloadEvent(@NotNull String plugin, @NotNull PlaceholderHook placeholderHook) {
      this.plugin = plugin;
      this.placeholderHook = placeholderHook;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   @NotNull
   public String getHookName() {
      return this.plugin;
   }

   @NotNull
   public PlaceholderHook getHook() {
      return this.placeholderHook;
   }

   @NotNull
   public HandlerList getHandlers() {
      return HANDLERS;
   }
}
