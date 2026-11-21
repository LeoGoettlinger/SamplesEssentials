package net.thenextlvl.protect.area.event.player;

import net.thenextlvl.protect.area.Area;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;

public abstract class PlayerAreaEvent extends PlayerEvent {
   private final Area area;

   @Internal
   protected PlayerAreaEvent(Player player, Area area) {
      super(player);
      this.area = area;
   }

   @Contract(
      pure = true
   )
   public Area getArea() {
      return this.area;
   }
}
