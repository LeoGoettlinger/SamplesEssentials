package github.nighter.smartspawner.hooks.protections.api;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import github.nighter.smartspawner.hooks.protections.ProtectionHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RedProtectAPI implements ProtectionHook {
   @Override
   public boolean canOpenMenu(Player player, Location location) {
      Region rg = RedProtect.get().getAPI().getRegion(location);
      return rg != null && rg.canChest(player);
   }

   @Override
   public boolean canStack(Player player, Location location) {
      Region rg = RedProtect.get().getAPI().getRegion(location);
      return rg != null && rg.canBuild(player);
   }
}
