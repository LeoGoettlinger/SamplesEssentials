package me.chancesd.pvpmanager.library.metrics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.integration.type.Dependency;
import me.chancesd.pvpmanager.sdutils.metrics.Metrics;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import org.bukkit.Bukkit;

public class CustomMetrics {
   public CustomMetrics(PvPManager plugin) {
      if (!plugin.isReloading()) {
         this.initMetrics(plugin);
      }
   }

   private void initMetrics(PvPManager plugin) {
      Metrics metrics = new Metrics(plugin, 5653, Conf.METRICS_OPT_OUT.asBool());
      metrics.addCustomChart(
         new Metrics.SimplePie("time_in_combat", () -> Conf.COMBAT_TAG_ENABLED.asBool() ? Conf.TIME_IN_COMBAT.asInt() + " seconds" : "Disabled")
      );
      metrics.addCustomChart(new Metrics.DrilldownPie("features", () -> {
         Map<String, Map<String, Integer>> map = new HashMap<>();
         map.put("Newbie Protection", this.getMapEntryFeature(Conf.NEWBIE_ENABLED.asBool()));
         map.put("Kill Abuse", this.getMapEntryFeature(Conf.KILL_ABUSE_ENABLED.asBool()));
         map.put("Update Check", this.getMapEntry(!Conf.CHECK_UPDATES.asBool() ? "Disabled" : (!Conf.AUTO_UPDATE.asBool() ? "Update Check" : "Auto Update")));
         map.put("PvP Blood", this.getMapEntryFeature(Conf.PVP_BLOOD.asBool()));
         map.put("Drop Mode", this.getMapEntry(Conf.PLAYER_DROP_MODE.asEnum(Conf.DropMode.class).toString()));
         map.put("Combat Nametags", this.getMapEntryFeature(Conf.NAMETAG_COMBAT_ENABLED.asBool() || Conf.TOGGLE_NAMETAG_ENABLED.asBool()));
         map.put("Database Type", this.getMapEntry(plugin.getStorageManager().getStorage().getDatabaseType().toString()));
         String displayType = "Display Type";
         if (Conf.BOSS_BAR_ENABLED.asBool() && Conf.ACTION_BAR_ENABLED.asBool()) {
            map.put("Display Type", this.getMapEntry("Both"));
         } else if (!Conf.BOSS_BAR_ENABLED.asBool() && !Conf.ACTION_BAR_ENABLED.asBool()) {
            map.put("Display Type", this.getMapEntry("None"));
         } else {
            map.put("Display Type", this.getMapEntry(Conf.BOSS_BAR_ENABLED.asBool() ? "Only Bossbar" : "Only Actionbar"));
         }

         return map;
      }));
      if (Conf.COMBAT_TAG_ENABLED.asBool()) {
         metrics.addCustomChart(new Metrics.SimplePie("player_drops_on_logout", () -> {
            if (!Conf.KILL_ON_LOGOUT.asBool()) {
               return "Kill On Logout Disabled";
            } else if (!Conf.DROP_INVENTORY.asBool() && !Conf.DROP_EXP.asBool() && !Conf.DROP_ARMOR.asBool()) {
               return "Keep Everything";
            } else if (Conf.DROP_INVENTORY.asBool() && Conf.DROP_EXP.asBool() && Conf.DROP_ARMOR.asBool()) {
               return "Drop Everything";
            } else if (!Conf.DROP_INVENTORY.asBool() && !Conf.DROP_ARMOR.asBool() && Conf.DROP_EXP.asBool()) {
               return "Only Drop Exp";
            } else if (!Conf.DROP_INVENTORY.asBool() && !Conf.DROP_EXP.asBool() && Conf.DROP_ARMOR.asBool()) {
               return "Only Drop Armor";
            } else if (Conf.DROP_INVENTORY.asBool() && !Conf.DROP_EXP.asBool() && !Conf.DROP_ARMOR.asBool()) {
               return "Only Drop Inventory";
            } else if (!Conf.DROP_INVENTORY.asBool() && Conf.DROP_EXP.asBool() && Conf.DROP_ARMOR.asBool()) {
               return "Only Keep Inventory";
            } else if (Conf.DROP_INVENTORY.asBool() && Conf.DROP_EXP.asBool() && !Conf.DROP_ARMOR.asBool()) {
               return "Only Keep Armor";
            } else {
               return Conf.DROP_INVENTORY.asBool() && !Conf.DROP_EXP.asBool() && Conf.DROP_ARMOR.asBool() ? "Only Keep Exp" : "";
            }
         }));
      }

      metrics.addCustomChart(new Metrics.AdvancedPie("hooks", () -> {
         Map<String, Integer> valueMap = new HashMap<>();

         for (Dependency entry : plugin.getDependencyManager().getDependencies()) {
            valueMap.put(entry.getName(), 1);
         }

         for (String extraPlugin : Arrays.asList("GSit", "NametagEdit", "RedProtect", "GriefDefender", "Citizens", "CMI", "ProtectionStones")) {
            if (Bukkit.getPluginManager().isPluginEnabled(extraPlugin)) {
               valueMap.put(extraPlugin, 1);
            }
         }

         return valueMap;
      }));
      metrics.addCustomChart(new Metrics.SimplePie("locale", Lang.getLocale()::name));
      metrics.addCustomChart(new Metrics.SingleLineChart("players_in_combat", () -> PvPManager.getInstance().getPlayerManager().getPlayersInCombat().size()));
      metrics.addCustomChart(new Metrics.DrilldownPie("blocked_actions", () -> {
         Map<String, Map<String, Integer>> map = new HashMap<>();
         map.put("EnderPearls", this.getMapEntry(Conf.BLOCK_ENDERPEARL.asBool()));
         map.put("ChorusFruits", this.getMapEntry(Conf.BLOCK_CHORUSFRUIT.asBool()));
         map.put("Teleport", this.getMapEntry(Conf.BLOCK_TELEPORT.asBool()));
         map.put("Unsafe Teleports", this.getMapEntry(Conf.BLOCK_UNSAFE_TELEPORTS.asBool()));
         map.put("Place Blocks", this.getMapEntry(Conf.BLOCK_PLACE_BLOCKS.asBool()));
         map.put("Interact", this.getMapEntry(Conf.BLOCK_INTERACT_IN_COMBAT.asBool()));
         map.put("Elytra", this.getMapEntry(Conf.BLOCK_GLIDE_IN_COMBAT.asBool()));
         map.put("Eat", this.getMapEntry(Conf.BLOCK_EAT.asBool()));
         map.put("Totem of Undying", this.getMapEntry(Conf.BLOCK_TOTEM_UNDYING.asBool()));
         map.put("Open Inventory", this.getMapEntry(Conf.BLOCK_INVENTORY_OPEN.asBool()));
         HashMap<String, Integer> result = new HashMap<>();
         if (Conf.BLOCK_COMMANDS.asBool()) {
            if (Conf.BLOCK_COMMANDS_WHITELIST.asBool()) {
               result.put("Whitelist", 1);
            } else {
               result.put("Blacklist", 1);
            }
         } else {
            result.put("False", 1);
         }

         map.put("Commands", result);
         return map;
      }));
   }

   private Map<String, Integer> getMapEntryFeature(boolean value) {
      return this.getMapEntry(value ? "Enabled" : "Disabled");
   }

   private Map<String, Integer> getMapEntry(boolean value) {
      return this.getMapEntry(value ? "True" : "False");
   }

   private Map<String, Integer> getMapEntry(String key) {
      HashMap<String, Integer> result = new HashMap<>();
      result.put(key, 1);
      return result;
   }
}
