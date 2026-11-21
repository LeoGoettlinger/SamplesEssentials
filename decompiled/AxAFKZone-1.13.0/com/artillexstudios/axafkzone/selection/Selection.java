package com.artillexstudios.axafkzone.selection;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.libs.axapi.collections.ThreadSafeList;
import com.artillexstudios.axafkzone.libs.axapi.scheduler.Scheduler;
import com.artillexstudios.axafkzone.libs.axapi.selection.Cuboid;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Selection {
   private final Player player;
   private Location position1 = null;
   private Location position2 = null;
   private final ThreadSafeList<Location> borders = new ThreadSafeList<>();

   public Selection(Player player) {
      this.player = player;
   }

   public Location getPosition1() {
      return this.position1;
   }

   public void setPosition1(@NotNull Location position1) {
      this.position1 = position1;
      this.update();
   }

   public Location getPosition2() {
      return this.position2;
   }

   public void setPosition2(@NotNull Location position2) {
      this.position2 = position2;
      this.update();
   }

   private void update() {
      this.updateVisualizer();
      if (this.position1 != null && this.position2 != null) {
         if (this.position1.getBlockY() == this.position2.getBlockY()) {
            Scheduler.get().run(() -> AxAFKZone.MESSAGEUTILS.sendLang(this.player, "selection.small-selection"));
         }
      }
   }

   public void show(@NotNull Player player) {
      if (!this.borders.isEmpty()) {
         for (int i = 0; i < this.borders.size(); i++) {
            Location l2 = this.borders.get(i).clone();
            l2.add(0.5, 0.5, 0.5);
            player.spawnParticle(Particle.WAX_ON, l2, 1, 0.0, 0.0, 0.0, 0.0);
         }
      }
   }

   private void updateVisualizer() {
      if (this.position1 != null && this.position2 != null && Objects.equals(this.position1.getWorld(), this.position2.getWorld())) {
         Cuboid cube = new Cuboid(
            this.position1.getWorld(),
            this.position1.getBlockX(),
            this.position2.getBlockX(),
            this.position1.getBlockZ(),
            this.position2.getBlockZ(),
            this.position1.getBlockY(),
            this.position2.getBlockY()
         );
         this.borders.clear();

         for (double x = cube.getMinX(); x <= cube.getMaxX(); x += 0.25) {
            for (double y = cube.getMinY(); y <= cube.getMaxY(); y += 0.25) {
               for (double z = cube.getMinZ(); z <= cube.getMaxZ(); z += 0.25) {
                  int components = 0;
                  if (x == cube.getMinX() || x == cube.getMaxX()) {
                     components++;
                  }

                  if (y == cube.getMinY() || y == cube.getMaxY()) {
                     components++;
                  }

                  if (z == cube.getMinZ() || z == cube.getMaxZ()) {
                     components++;
                  }

                  if (components >= 2) {
                     this.borders.add(new Location(cube.getWorld(), x, y, z));
                  }
               }
            }
         }
      }
   }
}
