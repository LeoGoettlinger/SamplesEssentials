package github.nighter.smartspawner.hooks.protections.api;

import github.nighter.smartspawner.hooks.protections.ProtectionHook;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.Flags;
import me.angeschossen.lands.api.land.LandWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Lands implements ProtectionHook {
   private static LandsIntegration landsIntegration;

   public Lands(Plugin smartSpawner) {
      landsIntegration = LandsIntegration.of(smartSpawner);
   }

   @Override
   public boolean canBreak(@NotNull Player player, @NotNull Location location) {
      if (landsIntegration == null) {
         return true;
      } else {
         LandWorld world = landsIntegration.getWorld(location.getWorld());
         return world != null ? world.hasFlag(player, location, Material.SPAWNER, Flags.BLOCK_BREAK, true) : true;
      }
   }

   @Override
   public boolean canStack(@NotNull Player player, @NotNull Location location) {
      if (landsIntegration == null) {
         return true;
      } else {
         LandWorld world = landsIntegration.getWorld(location.getWorld());
         return world != null ? world.hasFlag(player, location, Material.SPAWNER, Flags.BLOCK_PLACE, true) : true;
      }
   }

   @Override
   public boolean canOpenMenu(@NotNull Player player, @NotNull Location location) {
      if (landsIntegration == null) {
         return true;
      } else {
         LandWorld world = landsIntegration.getWorld(location.getWorld());
         return world != null ? world.hasFlag(player, location, Material.SPAWNER, Flags.INTERACT_CONTAINER, true) : true;
      }
   }
}
