package net.thenextlvl.protect.area;

import java.util.Optional;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Contract;

public interface AreaProvider {
   @Contract(
      pure = true
   )
   Stream<Area> getAreas();

   @Contract(
      pure = true
   )
   Stream<Area> getAreas(World var1);

   @Contract(
      pure = true
   )
   Stream<Area> getAreas(Location var1);

   @Contract(
      pure = true
   )
   default Stream<Area> getAreas(Block block) {
      return this.getAreas(block.getLocation());
   }

   @Contract(
      pure = true
   )
   default Stream<Area> getAreas(Entity entity) {
      return this.getAreas(entity.getLocation());
   }

   @Contract(
      pure = true
   )
   GlobalArea getArea(World var1);

   @Contract(
      pure = true
   )
   Area getArea(Location var1);

   @Contract(
      pure = true
   )
   default Area getArea(Block block) {
      return this.getArea(block.getLocation());
   }

   @Contract(
      pure = true
   )
   default Area getArea(Entity entity) {
      return this.getArea(entity.getLocation());
   }

   @Contract(
      pure = true
   )
   Optional<Area> getArea(String var1);
}
