package github.nighter.smartspawner.spawner.lootgen;

import java.util.UUID;
import org.bukkit.Location;

record PlayerRangeWrapper(UUID worldUID, double x, double y, double z, boolean spawnConditions) {
   double distanceSquared(Location loc2) {
      double dx = this.x - loc2.getX();
      double dy = this.y - loc2.getY();
      double dz = this.z - loc2.getZ();
      return dx * dx + dy * dy + dz * dz;
   }
}
