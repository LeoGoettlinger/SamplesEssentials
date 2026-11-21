package org.popcraft.chunky.platform;

import org.bukkit.WorldBorder;
import org.popcraft.chunky.platform.util.Vector2;

public class BukkitBorder implements Border {
   final WorldBorder worldBorder;

   public BukkitBorder(WorldBorder worldBorder) {
      this.worldBorder = worldBorder;
   }

   @Override
   public Vector2 getCenter() {
      return Vector2.of(this.worldBorder.getCenter().getX(), this.worldBorder.getCenter().getZ());
   }

   @Override
   public double getRadiusX() {
      return this.worldBorder.getSize() / 2.0;
   }

   @Override
   public double getRadiusZ() {
      return this.worldBorder.getSize() / 2.0;
   }

   @Override
   public String getShape() {
      return "square";
   }
}
