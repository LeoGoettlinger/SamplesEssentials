package me.byteful.plugin.leveltools.api.event;

import java.util.Objects;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LevelToolsXPIncreaseEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   @NotNull
   private final LevelToolsItem item;
   @NotNull
   private final Player player;
   private final double initialXp;
   private double newXp;
   private boolean isCancelled;
   private boolean isModified = false;

   public LevelToolsXPIncreaseEvent(@NotNull LevelToolsItem item, @NotNull Player player, double initialXp) {
      this.item = item;
      this.player = player;
      this.initialXp = initialXp;
   }

   public LevelToolsXPIncreaseEvent(@NotNull LevelToolsItem item, @NotNull Player player, double initialXp, double newXp, boolean isCancelled) {
      this.item = item;
      this.player = player;
      this.initialXp = initialXp;
      this.newXp = newXp;
      this.isCancelled = isCancelled;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.isCancelled = cancelled;
   }

   @NotNull
   public LevelToolsItem getItem() {
      return this.item;
   }

   @NotNull
   public Player getPlayer() {
      return this.player;
   }

   public double getNewXp() {
      return this.newXp;
   }

   public void setNewXp(double newXp) {
      this.newXp = newXp;
      this.isModified = true;
   }

   public double getInitialXp() {
      return this.initialXp;
   }

   public boolean isModified() {
      return this.isModified;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         LevelToolsXPIncreaseEvent that = (LevelToolsXPIncreaseEvent)object;
         return Double.compare(this.getInitialXp(), that.getInitialXp()) == 0
            && Double.compare(this.getNewXp(), that.getNewXp()) == 0
            && this.isCancelled() == that.isCancelled()
            && Objects.equals(this.getItem(), that.getItem())
            && Objects.equals(this.getPlayer(), that.getPlayer());
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(this.getItem(), this.getPlayer(), this.getInitialXp(), this.getNewXp(), this.isCancelled());
   }

   public String toString() {
      return "LevelToolsXPIncreaseEvent{item="
         + this.item
         + ", player="
         + this.player
         + ", initialXp="
         + this.initialXp
         + ", newXp="
         + this.newXp
         + ", isCancelled="
         + this.isCancelled
         + '}';
   }
}
