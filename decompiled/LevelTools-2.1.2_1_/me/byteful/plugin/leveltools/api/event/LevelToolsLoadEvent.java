package me.byteful.plugin.leveltools.api.event;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.trigger.TriggerRegistry;
import me.byteful.plugin.leveltools.profile.ProfileManager;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LevelToolsLoadEvent extends Event {
   private static final HandlerList HANDLERS = new HandlerList();
   private final LevelToolsPlugin plugin;
   private final LevelToolsLoadEvent.LoadPhase phase;

   public LevelToolsLoadEvent(@NotNull LevelToolsPlugin plugin, @NotNull LevelToolsLoadEvent.LoadPhase phase) {
      this.plugin = plugin;
      this.phase = phase;
   }

   @NotNull
   public LevelToolsPlugin getPlugin() {
      return this.plugin;
   }

   @NotNull
   public LevelToolsLoadEvent.LoadPhase getPhase() {
      return this.phase;
   }

   @NotNull
   public TriggerRegistry getTriggerRegistry() {
      return this.plugin.getTriggerRegistry();
   }

   @NotNull
   public ProfileManager getProfileManager() {
      return this.plugin.getProfileManager();
   }

   @NotNull
   public HandlerList getHandlers() {
      return HANDLERS;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return HANDLERS;
   }

   public static enum LoadPhase {
      PRE_LOAD,
      POST_TRIGGERS,
      POST_PROFILES,
      COMPLETE;
   }
}
