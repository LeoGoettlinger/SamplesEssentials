package net.thenextlvl.protect.area.event.member;

import java.util.UUID;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.area.event.AreaEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class AreaMemberRemoveEvent extends AreaEvent<Area> implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   private UUID member;
   private boolean cancelled;

   @Internal
   public AreaMemberRemoveEvent(Area area, UUID member) {
      super(area);
      this.member = member;
   }

   @Contract(
      pure = true
   )
   public UUID getMember() {
      return this.member;
   }

   @Contract(
      mutates = "this"
   )
   public void setMember(UUID member) {
      this.member = member;
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
