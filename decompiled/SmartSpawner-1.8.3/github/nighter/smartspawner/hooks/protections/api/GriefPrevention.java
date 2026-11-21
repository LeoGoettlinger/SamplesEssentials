package github.nighter.smartspawner.hooks.protections.api;

import github.nighter.smartspawner.hooks.protections.ProtectionHook;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GriefPrevention implements ProtectionHook {
   private boolean hasTrust(@NotNull Player player, @NotNull Location location, @NotNull ClaimPermission level) {
      Claim claim = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
      return claim == null ? true : claim.checkPermission(player, level, null) == null;
   }

   @Override
   public boolean canBreak(@NotNull Player player, @NotNull Location location) {
      return this.hasTrust(player, location, ClaimPermission.Build);
   }

   @Override
   public boolean canStack(@NotNull Player player, @NotNull Location location) {
      return this.hasTrust(player, location, ClaimPermission.Build);
   }

   @Override
   public boolean canOpenMenu(@NotNull Player player, @NotNull Location location) {
      return this.hasTrust(player, location, ClaimPermission.Inventory);
   }
}
