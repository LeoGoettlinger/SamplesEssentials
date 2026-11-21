package simplexity.simpleplayerfreeze.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import simplexity.simpleplayerfreeze.freeze.FreezeType;

public class PlayerFreezeEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final boolean frozen;
   private final FreezeType type;

   public PlayerFreezeEvent(Player player, boolean frozen, FreezeType type) {
      this.player = player;
      this.frozen = frozen;
      this.type = type;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   public Player getPlayer() {
      return this.player;
   }

   public boolean setFrozen() {
      return this.frozen;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public FreezeType getType() {
      return this.type;
   }
}
