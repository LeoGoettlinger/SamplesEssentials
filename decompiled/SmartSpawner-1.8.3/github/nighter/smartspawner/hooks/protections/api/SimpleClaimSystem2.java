package github.nighter.smartspawner.hooks.protections.api;

import fr.xyness.SimpleClaimSystem.API.SCS_API;
import fr.xyness.SimpleClaimSystem.API.SCS_API_Provider;
import fr.xyness.SimpleClaimSystem.Types.Claim;
import github.nighter.smartspawner.hooks.protections.ProtectionHook;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SimpleClaimSystem2 implements ProtectionHook {
   private static final SCS_API api = SCS_API_Provider.get();

   @Override
   public boolean canBreak(@NotNull Player player, @NotNull Location location) {
      if (api == null) {
         return true;
      } else {
         Optional<Claim> claim = api.getClaim(player.getLocation().getChunk());
         if (claim.isPresent()) {
            Claim c = claim.get();
            boolean canDestroy = c.getPermission(c.getRole(player.getUniqueId()), "destroy_block");
            return canDestroy ? c.getPermission(c.getRole(player.getUniqueId()), "destroy_spawners") : canDestroy;
         } else {
            return true;
         }
      }
   }

   @Override
   public boolean canStack(@NotNull Player player, @NotNull Location location) {
      if (api == null) {
         return true;
      } else {
         Optional<Claim> claim = api.getClaim(player.getLocation().getChunk());
         if (claim.isPresent()) {
            Claim c = claim.get();
            return c.getPermission(c.getRole(player.getUniqueId()), "interact_spawner");
         } else {
            return true;
         }
      }
   }

   @Override
   public boolean canOpenMenu(@NotNull Player player, @NotNull Location location) {
      if (api == null) {
         return true;
      } else {
         Optional<Claim> claim = api.getClaim(player.getLocation().getChunk());
         if (claim.isPresent()) {
            Claim c = claim.get();
            return c.getPermission(c.getRole(player.getUniqueId()), "interact_spawner");
         } else {
            return true;
         }
      }
   }
}
