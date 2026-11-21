package me.byteful.plugin.leveltools.api.event;

import java.util.Objects;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LevelToolsLevelIncreaseEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   @NotNull
   private final LevelToolsItem item;
   @NotNull
   private final Player player;
   private int newLevel;
   private boolean isCancelled;

   public LevelToolsLevelIncreaseEvent(@NotNull LevelToolsItem item, @NotNull Player player) {
      this.item = item;
      this.player = player;
   }

   public LevelToolsLevelIncreaseEvent(@NotNull LevelToolsItem item, @NotNull Player player, int newLevel, boolean isCancelled) {
      this.item = item;
      this.player = player;
      this.newLevel = newLevel;
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

   @NotNull
   public LevelToolsItem getItem() {
      return this.item;
   }

   @NotNull
   public Player getPlayer() {
      return this.player;
   }

   public int getNewLevel() {
      return this.newLevel;
   }

   public void setNewLevel(int newLevel) {
      this.newLevel = newLevel;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.isCancelled = cancelled;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         LevelToolsLevelIncreaseEvent that = (LevelToolsLevelIncreaseEvent)object;
         return this.getNewLevel() == that.getNewLevel()
            && this.isCancelled() == that.isCancelled()
            && Objects.equals(this.getItem(), that.getItem())
            && Objects.equals(this.getPlayer(), that.getPlayer());
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(this.getItem(), this.getPlayer(), this.getNewLevel(), this.isCancelled());
   }

   public String toString() {
      return "LevelToolsLevelIncreaseEvent{item="
         + this.item
         + ", player="
         + this.player
         + ", newLevel="
         + this.newLevel
         + ", isCancelled="
         + this.isCancelled
         + '}';
   }
}
