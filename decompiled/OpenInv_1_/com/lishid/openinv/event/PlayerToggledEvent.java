package com.lishid.openinv.event;

import com.google.errorprone.annotations.RestrictedApi;
import com.lishid.openinv.util.setting.PlayerToggle;
import java.util.UUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

public class PlayerToggledEvent extends Event {
   private static final HandlerList HANDLERS = new HandlerList();
   @NotNull
   private final PlayerToggle toggle;
   @NotNull
   private final UUID uuid;
   private final boolean enabled;

   @RestrictedApi(
      explanation = "Constructor is not considered part of the API and may be subject to change.",
      allowedOnPath = ".*/com/lishid/openinv/event/OpenEvents.java"
   )
   @Internal
   PlayerToggledEvent(@NotNull PlayerToggle toggle, @NotNull UUID uuid, boolean enabled) {
      this.toggle = toggle;
      this.uuid = uuid;
      this.enabled = enabled;
   }

   @NotNull
   public PlayerToggle getToggle() {
      return this.toggle;
   }

   @NotNull
   public UUID getPlayerId() {
      return this.uuid;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   @NotNull
   public HandlerList getHandlers() {
      return HANDLERS;
   }

   public static HandlerList getHandlerList() {
      return HANDLERS;
   }
}
