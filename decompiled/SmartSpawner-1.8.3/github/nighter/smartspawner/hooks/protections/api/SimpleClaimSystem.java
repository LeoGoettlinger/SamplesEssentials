package github.nighter.smartspawner.hooks.protections.api;

import fr.xyness.SCS.API.SimpleClaimSystemAPI;
import fr.xyness.SCS.API.SimpleClaimSystemAPI_Provider;
import fr.xyness.SCS.Types.Claim;
import github.nighter.smartspawner.hooks.protections.ProtectionHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SimpleClaimSystem implements ProtectionHook {
   private static final SimpleClaimSystemAPI scs = SimpleClaimSystemAPI_Provider.getAPI();

   @Override
   public boolean canBreak(@NotNull Player player, @NotNull Location location) {
      if (scs == null) {
         return true;
      } else {
         Claim claim = scs.getClaimAtChunk(location.getChunk());
         return claim == null ? true : claim.getPermissionForPlayer("Destroy", player);
      }
   }

   @Override
   public boolean canStack(@NotNull Player player, @NotNull Location location) {
      if (scs == null) {
         return true;
      } else {
         Claim claim = scs.getClaimAtChunk(location.getChunk());
         return claim == null ? true : claim.getPermissionForPlayer("InteractBlocks", player);
      }
   }

   @Override
   public boolean canOpenMenu(@NotNull Player player, @NotNull Location location) {
      if (scs == null) {
         return true;
      } else {
         Claim claim = scs.getClaimAtChunk(location.getChunk());
         return claim == null ? true : claim.getPermissionForPlayer("InteractBlocks", player);
      }
   }
}
