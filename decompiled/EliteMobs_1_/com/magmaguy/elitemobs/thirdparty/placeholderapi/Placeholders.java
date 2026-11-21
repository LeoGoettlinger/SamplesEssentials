package com.magmaguy.elitemobs.thirdparty.placeholderapi;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {
   public boolean persist() {
      return true;
   }

   public boolean canRegister() {
      return true;
   }

   public String getAuthor() {
      return MetadataHandler.PLUGIN.getDescription().getAuthors().toString();
   }

   public String getIdentifier() {
      return "elitemobs";
   }

   public String getVersion() {
      return MetadataHandler.PLUGIN.getDescription().getVersion();
   }

   private static String toTinyRoman(int num) {
      if (num > 0 && num <= 3999) {
         String[] thousands = new String[]{"", "ᴍ", "ᴍᴍ", "ᴍᴍᴍ"};
         String[] hundreds = new String[]{"", "ᴄ", "ᴄᴄ", "ᴄᴄᴄ", "ᴄᴅ", "ᴅ", "ᴅᴄ", "ᴅᴄᴄ", "ᴅᴄᴄᴄ", "ᴄᴍ"};
         String[] tens = new String[]{"", "x", "xx", "xxx", "xʟ", "ʟ", "ʟx", "ʟxx", "ʟxxx", "xᴄ"};
         String[] ones = new String[]{"", "ɪ", "ɪɪ", "ɪɪɪ", "ɪᴠ", "ᴠ", "ᴠɪ", "ᴠɪɪ", "ᴠɪɪɪ", "ɪx"};
         return thousands[num / 1000] + hundreds[num % 1000 / 100] + tens[num % 100 / 10] + ones[num % 10];
      } else {
         return String.valueOf(num);
      }
   }

   public String onPlaceholderRequest(Player player, String identifier) {
      if (player == null) {
         return "Only online players!";
      } else if (PlayerData.isInMemory(player.getUniqueId()) && PlayerData.getPlayerData(player.getUniqueId()) != null) {
         switch (identifier) {
            case "player_combat_tier":
            case "player_combat_level":
               return CombatLevelCalculator.calculateCombatLevel(player.getUniqueId()) + "";
            case "player_combat_level_roman":
               return toTinyRoman(CombatLevelCalculator.calculateCombatLevel(player.getUniqueId()));
            case "player_money":
               return EconomyHandler.formatCurrency(EconomyHandler.checkCurrency(player.getUniqueId()));
            case "player_top_tier":
            case "player_top_combat_level":
               return CombatLevelCalculator.highestOnlineCombatLevel() + "";
            case "player_kills":
               return PlayerData.getKills(player.getUniqueId()) + "";
            case "player_deaths":
               return PlayerData.getDeaths(player.getUniqueId()) + "";
            case "player_swords_level":
               return SkillXPCalculator.levelFromTotalXP(PlayerData.getSkillXP(player.getUniqueId(), SkillType.SWORDS)) + "";
            case "player_axes_level":
               return SkillXPCalculator.levelFromTotalXP(PlayerData.getSkillXP(player.getUniqueId(), SkillType.AXES)) + "";
            case "player_bows_level":
               return SkillXPCalculator.levelFromTotalXP(PlayerData.getSkillXP(player.getUniqueId(), SkillType.BOWS)) + "";
            case "player_crossbows_level":
               return SkillXPCalculator.levelFromTotalXP(PlayerData.getSkillXP(player.getUniqueId(), SkillType.CROSSBOWS)) + "";
            case "player_tridents_level":
               return SkillXPCalculator.levelFromTotalXP(PlayerData.getSkillXP(player.getUniqueId(), SkillType.TRIDENTS)) + "";
            case "player_hoes_level":
               return SkillXPCalculator.levelFromTotalXP(PlayerData.getSkillXP(player.getUniqueId(), SkillType.HOES)) + "";
            case "player_maces_level":
               return SkillXPCalculator.levelFromTotalXP(PlayerData.getSkillXP(player.getUniqueId(), SkillType.MACES)) + "";
            case "player_spears_level":
               return SkillXPCalculator.levelFromTotalXP(PlayerData.getSkillXP(player.getUniqueId(), SkillType.SPEARS)) + "";
            case "player_staves_level":
               return SkillXPCalculator.levelFromTotalXP(PlayerData.getSkillXP(player.getUniqueId(), SkillType.STAVES)) + "";
            case "player_wands_level":
               return SkillXPCalculator.levelFromTotalXP(PlayerData.getSkillXP(player.getUniqueId(), SkillType.WANDS)) + "";
            case "player_armor_level":
               return SkillXPCalculator.levelFromTotalXP(PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR)) + "";
            case "player_active_guild_rank_numerical":
            case "player_maximum_guild_rank_numerical":
            case "player_active_guild_rank_name":
            case "player_maximum_guild_rank_name":
            case "player_prestige_guild_rank_numerical":
            case "player_top_guild_rank":
            case "player_shortened_guild_rank":
               return "N/A";
            default:
               return null;
         }
      } else {
         return "Uninitialized player data!";
      }
   }
}
