package vrx.economy.vRXClans;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RegionProtectionService {
   private final VRXClans plugin;
   private final ClanCache cache;

   public RegionProtectionService(VRXClans plugin, ClanCache cache) {
      this.plugin = plugin;
      this.cache = cache;
   }

   public boolean isProtected(Location loc, Player player) {
      if (player.hasPermission("vrxclans.admin.bypass")) {
         return false;
      } else {
         Clan playerClan = this.cache.getClan(player);

         for (Clan clan : this.cache.getCandidateBaseClans(loc)) {
            Location base = clan.getBaseLocation();
            if (base != null && base.getWorld() != null && base.getWorld().equals(loc.getWorld())) {
               int radius = clan.getDynamicRadius(this.plugin);
               if (base.distanceSquared(loc) <= radius * radius) {
                  if (playerClan != null && playerClan.getName().equals(clan.getName())) {
                     return false;
                  }

                  player.sendMessage(this.plugin.getLang().get("msg.base-protected", "%clan%", clan.getName()));
                  return true;
               }
            }
         }

         return false;
      }
   }
}
