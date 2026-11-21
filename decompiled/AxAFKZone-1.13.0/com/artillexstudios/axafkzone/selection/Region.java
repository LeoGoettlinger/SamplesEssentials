package com.artillexstudios.axafkzone.selection;

import com.artillexstudios.axafkzone.zones.Zone;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Region {
   private final Location corner1;
   private final Location corner2;
   private final Location center;
   private World world;
   private final Zone zone;

   public Region(Location corner1, Location corner2, @NotNull Zone zone) {
      this.corner1 = corner1;
      this.corner2 = corner2;
      this.world = corner1.getWorld();
      this.zone = zone;
      this.center = new Location(
         corner1.getWorld(),
         (corner1.getBlockX() + corner2.getBlockX()) / 2.0,
         (corner1.getBlockY() + corner2.getBlockY()) / 2.0,
         (corner1.getBlockZ() + corner2.getBlockZ()) / 2.0
      );
   }

   public Set<Player> getPlayersInZone() {
      if (this.world == null) {
         return Set.of();
      } else {
         HashSet<Player> players = new HashSet<>();
         String permission = this.zone.getSettings().getString("permission");

         for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.isDead() && player.getWorld().equals(this.world) && (permission.isBlank() || player.hasPermission(permission))) {
               Location loc = player.getLocation();
               int x1 = Math.min(this.corner1.getBlockX(), this.corner2.getBlockX());
               int y1 = Math.min(this.corner1.getBlockY(), this.corner2.getBlockY());
               int z1 = Math.min(this.corner1.getBlockZ(), this.corner2.getBlockZ());
               int x2 = Math.max(this.corner1.getBlockX(), this.corner2.getBlockX());
               int y2 = Math.max(this.corner1.getBlockY(), this.corner2.getBlockY());
               int z2 = Math.max(this.corner1.getBlockZ(), this.corner2.getBlockZ());
               if (loc.getBlockX() >= x1
                  && loc.getBlockX() <= x2
                  && loc.getBlockY() >= y1
                  && loc.getBlockY() <= y2
                  && loc.getBlockZ() >= z1
                  && loc.getBlockZ() <= z2) {
                  players.add(player);
               }
            }
         }

         return players;
      }
   }

   @NotNull
   public Location getCorner1() {
      return this.corner1;
   }

   @NotNull
   public Location getCorner2() {
      return this.corner2;
   }

   public long getCenterX() {
      return this.center.getBlockX();
   }

   public long getCenterY() {
      return this.center.getBlockY();
   }

   public long getCenterZ() {
      return this.center.getBlockZ();
   }

   @NotNull
   public Location getCenter() {
      return this.center;
   }

   @Nullable
   public World getWorld() {
      return this.world;
   }

   public void setWorld(World world) {
      this.world = world;
   }
}
