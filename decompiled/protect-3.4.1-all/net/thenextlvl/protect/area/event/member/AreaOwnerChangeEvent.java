package net.thenextlvl.protect.area.event.member;

import java.util.UUID;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.area.event.AreaEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;

public final class AreaOwnerChangeEvent extends AreaEvent<Area> implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   @Nullable
   private UUID owner;
   private boolean cancelled;

   @Internal
   public AreaOwnerChangeEvent(Area area, @Nullable UUID owner) {
      super(area);
      this.owner = owner;
   }

   @Contract(
      pure = true
   )
   @Nullable
   public UUID getOwner() {
      return this.owner;
   }

   @Contract(
      mutates = "this"
   )
   public void setOwner(@Nullable UUID owner) {
      this.owner = owner;
   }

   @Contract(
      pure = true
   )
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Contract(
      mutates = "this"
   )
   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   @Contract(
      pure = true
   )
   public HandlerList getHandlers() {
      return handlerList;
   }

   @Contract(
      pure = true
   )
   public static HandlerList getHandlerList() {
      return handlerList;
   }
}
