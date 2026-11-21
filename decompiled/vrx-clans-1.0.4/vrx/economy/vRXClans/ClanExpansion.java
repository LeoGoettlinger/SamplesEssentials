package vrx.economy.vRXClans;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class ClanExpansion extends PlaceholderExpansion {
   private final VRXClans plugin;

   public ClanExpansion(VRXClans plugin) {
      this.plugin = plugin;
   }

   public String getIdentifier() {
      return "vrxclans";
   }

   public String getAuthor() {
      return "VRX";
   }

   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   public boolean persist() {
      return true;
   }

   public String onPlaceholderRequest(Player player, String params) {
      if (player == null) {
         return "";
      } else {
         Clan clan = this.plugin.getClanManager().getClanByPlayer(player);
         if (params.equalsIgnoreCase("has_clan")) {
            return clan != null ? "Ja" : "Nein";
         } else if (clan == null) {
            return "-";
         } else {
            String var4 = params.toLowerCase();
            switch (var4) {
               case "name":
                  return clan.getName();
               case "level":
                  return String.valueOf(clan.getLevel());
               case "rank":
                  return clan.getRankDisplayName(clan.getRank(player.getUniqueId()));
               case "tag":
                  return clan.getTag();
               case "tag_color":
                  return clan.getTagColor();
               case "homes":
                  return String.valueOf(clan.getHomes().size());
               case "achievements":
                  return String.valueOf(clan.getCompletedAchievements().size());
               case "members":
                  return String.valueOf(clan.getMembers().size());
               case "bank":
                  return String.valueOf(clan.getBankBalance());
               case "radius":
                  return String.valueOf(clan.getDynamicRadius(this.plugin));
               default:
                  return null;
            }
         }
      }
   }
}
