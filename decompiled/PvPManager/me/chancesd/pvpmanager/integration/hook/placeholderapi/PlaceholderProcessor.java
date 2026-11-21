package me.chancesd.pvpmanager.integration.hook.placeholderapi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.utils.Utils;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.utils.CombatUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderProcessor {
   private final PvPManager plugin;
   @NotNull
   private final List<String> placeholders = new ArrayList<>();
   private final DecimalFormat df = new DecimalFormat("#.##");

   public PlaceholderProcessor(PvPManager plugin) {
      this.plugin = plugin;
      this.registerPlaceholder("in_combat");
      this.registerPlaceholder("combat_timeleft");
      this.registerPlaceholder("combat_timeleft_ms");
      this.registerPlaceholder("pvp_status");
      this.registerPlaceholder("pvp_command_timeleft");
      this.registerPlaceholder("is_newbie");
      this.registerPlaceholder("newbie_timeleft");
      this.registerPlaceholder("has_override");
      this.registerPlaceholder("has_respawn_prot");
      this.registerPlaceholder("combat_prefix");
      this.registerPlaceholder("player_health");
      this.registerPlaceholder("current_enemy");
      this.registerPlaceholder("current_enemy_health");
      this.registerPlaceholder("global_pvp_status");
   }

   public String replacePlaceholders(Player player, String message) {
      if (message != null && player != null) {
         String result = message;

         for (String placeholder : this.placeholders) {
            if (result.contains("%" + placeholder + "%")) {
               String identifier = placeholder.substring("pvpmanager_".length());
               String replacement = this.processPlaceholder(player, identifier);
               if (replacement != null) {
                  result = result.replace("%" + placeholder + "%", replacement);
               }
            }
         }

         return result;
      } else {
         return message;
      }
   }

   public String processPlaceholder(Player player, String identifier) {
      if (player == null) {
         return "";
      } else {
         CombatPlayer p = this.plugin.getPlayerManager().getUnchecked(player);
         if (p == null) {
            return null;
         } else {
            switch (identifier) {
               case "in_combat":
                  return this.getPAPIBoolean(p.isInCombat());
               case "pvp_status":
                  return this.getPAPIBoolean(p.hasPvPEnabled());
               case "is_newbie":
                  return this.getPAPIBoolean(p.isNewbie());
               case "has_override":
                  return this.getPAPIBoolean(p.hasOverride());
               case "has_respawn_prot":
                  return this.getPAPIBoolean(p.hasRespawnProtection());
               case "combat_prefix":
                  return p.isInCombat() ? Conf.NAMETAG_PREFIX.asString() : "";
               case "combat_timeleft":
                  return String.valueOf(Math.round(p.getTagTimeLeft() / 1000.0));
               case "combat_timeleft_ms":
                  return String.valueOf(Utils.roundTo1Decimal(p.getTagTimeLeft() / 1000.0));
               case "newbie_timeleft":
                  return String.valueOf(p.getNewbieTimeLeft() / 1000L);
               case "pvp_command_timeleft":
                  int timeLeft = CombatUtils.getTimeLeft(p.getToggleTime(), Conf.TOGGLE_COOLDOWN.asInt());
                  return String.valueOf(Math.max(timeLeft, 0));
               case "player_health":
                  return this.df.format(p.getPlayer().getHealth());
               case "current_enemy":
                  CombatPlayer pPlayer = p.getEnemy();
                  return pPlayer != null ? pPlayer.getName() : "None";
               case "current_enemy_health":
                  CombatPlayer enemy = p.getEnemy();
                  return enemy != null ? this.df.format(enemy.getPlayer().getHealth()) : "0";
               case "global_pvp_status":
                  return this.getPAPIBoolean(this.plugin.getPlayerManager().getGlobalStatus());
               default:
                  return null;
            }
         }
      }
   }

   private String getPAPIBoolean(boolean value) {
      return value ? Conf.PLACEHOLDER_BOOLEAN_TRUE.asString() : Conf.PLACEHOLDER_BOOLEAN_FALSE.asString();
   }

   private void registerPlaceholder(String name) {
      this.placeholders.add("pvpmanager_" + name);
   }

   public List<String> getPlaceholders() {
      return this.placeholders;
   }
}
