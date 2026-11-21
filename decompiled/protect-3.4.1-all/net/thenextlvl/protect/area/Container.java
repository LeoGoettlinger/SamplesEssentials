package net.thenextlvl.protect.area;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;

public interface Container {
   @Contract(
      pure = true
   )
   boolean contains(Location var1);

   @Contract(
      pure = true
   )
   default boolean contains(Block block) {
      return this.contains(block.getLocation());
   }

   @Contract(
      pure = true
   )
   default boolean contains(Entity entity) {
      return this.contains(entity.getLocation());
   }

   @Contract(
      pure = true
   )
   List<Entity> getEntities();

   @Contract(
      pure = true
   )
   List<Player> getPlayers();

   @Contract(
      pure = true
   )
   List<Entity> getHighestEntities();

   @Contract(
      pure = true
   )
   List<Player> getHighestPlayers();
}
