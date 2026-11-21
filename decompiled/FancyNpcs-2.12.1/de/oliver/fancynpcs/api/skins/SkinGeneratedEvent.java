package de.oliver.fancynpcs.api.skins;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkinGeneratedEvent extends Event {
   private static final HandlerList handlerList = new HandlerList();
   @NotNull
   private final String id;
   @Nullable
   private final SkinData skin;

   public SkinGeneratedEvent(@NotNull String id, @Nullable SkinData skin) {
      super(!Bukkit.isPrimaryThread());
      this.id = id;
      this.skin = skin;
   }

   public static HandlerList getHandlerList() {
      return handlerList;
   }

   @NotNull
   public String getId() {
      return this.id;
   }

   @Nullable
   public SkinData getSkin() {
      return this.skin;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlerList;
   }
}
