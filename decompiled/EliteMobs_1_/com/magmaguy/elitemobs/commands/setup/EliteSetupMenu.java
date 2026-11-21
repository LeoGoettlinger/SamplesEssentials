package com.magmaguy.elitemobs.commands.setup;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.dungeons.DynamicDungeonPackage;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.EventsPackage;
import com.magmaguy.elitemobs.dungeons.MetaPackage;
import com.magmaguy.elitemobs.dungeons.ModelsPackage;
import com.magmaguy.elitemobs.dungeons.WorldDungeonPackage;
import com.magmaguy.elitemobs.dungeons.WorldInstancedDungeonPackage;
import com.magmaguy.elitemobs.magmacore.menus.MenuButton;
import com.magmaguy.elitemobs.magmacore.menus.SetupMenu;
import com.magmaguy.elitemobs.magmacore.menus.SetupMenuBuilder;
import com.magmaguy.elitemobs.magmacore.nightbreak.DownloadAllContentPackage;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakPluginUpdater;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakSetupControls;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.versionnotifier.VersionChecker;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EliteSetupMenu {
   private static final String MENU_TITLE = "Setup menu";
   private static final long PLUGIN_UPDATE_MENU_REFRESH_MS = 60000L;

   public static void createMenu(Player player) {
      createMenu(player, true);
   }

   private static void createMenu(Player player, boolean requestRefresh) {
      VersionChecker.applyCachedContentAndAccess();
      List<EMPackage> rawEmPackages = new ArrayList<>(EMPackage.getEmPackages().values());
      List<EMPackage> emPackages = rawEmPackages.stream()
         .sorted(Comparator.comparing(pkg -> ChatColor.stripColor(ChatColorConverter.convert(pkg.getContentPackagesConfigFields().getName()))))
         .collect(Collectors.toList());
      rawEmPackages.stream()
         .filter(MetaPackage.class::isInstance)
         .forEach(rawEmPackage -> ((MetaPackage)rawEmPackage).getPackages().forEach(emPackages::remove));
      MenuButton infoButton = NightbreakSetupControls.setupInfoButton(
         EliteMobs.NIGHTBREAK_PLUGIN_SPEC, "https://wiki.nightbreak.io/EliteMobs/setup/", "https://discord.gg/eSxvPbWYy4"
      );
      SetupMenuBuilder builder = new SetupMenuBuilder(MetadataHandler.PLUGIN, player)
         .title("Setup menu")
         .titleIconPrefix(DefaultConfig.useResourcePackModels() ? "\udb83\udef1\udb83\ude0b\udb83\udef5          " : null)
         .infoButton(infoButton)
         .packages(emPackages)
         .appendPackage(
            new DownloadAllContentPackage<>(
               () -> new ArrayList<>(EMPackage.getEmPackages().values()), "EliteMobs", "https://nightbreak.io/plugin/elitemobs/", "elitemobs downloadall"
            )
         )
         .addFilter(Material.GRASS_BLOCK, "Filter By Dungeons", EliteSetupMenu::filterDungeon)
         .addFilter(Material.SKULL_BANNER_PATTERN, "Filter By Events", EliteSetupMenu::filterEvents)
         .addFilter(Material.ARMOR_STAND, "Filter By Custom Models", EliteSetupMenu::filterModels);
      NightbreakSetupControls.prependStandardControls(builder, MetadataHandler.PLUGIN, EliteMobs.NIGHTBREAK_PLUGIN_SPEC).open();
      if (requestRefresh) {
         requestContentStatusRefresh(player);
         requestPluginUpdateRefresh(player);
      }
   }

   private static void requestContentStatusRefresh(Player player) {
      VersionChecker.refreshContentAndAccess(() -> reopenIfStillViewingSetup(player));
   }

   private static void requestPluginUpdateRefresh(Player player) {
      JavaPlugin plugin = MetadataHandler.PLUGIN;
      NightbreakPluginUpdater.CachedPluginUpdateCheck snapshot = NightbreakPluginUpdater.getCachedUpdateCheck(plugin, EliteMobs.NIGHTBREAK_PLUGIN_SPEC);
      long checkedAt = snapshot.checkedAtMillis();
      boolean shouldRefresh = !snapshot.checking() && (checkedAt == 0L || System.currentTimeMillis() - checkedAt > 60000L);
      if (shouldRefresh) {
         NightbreakPluginUpdater.refreshPluginUpdateCheckAsync(plugin, EliteMobs.NIGHTBREAK_PLUGIN_SPEC, ignored -> reopenIfStillViewingSetup(player));
      }
   }

   private static void reopenIfStillViewingSetup(Player player) {
      if (player != null && player.isOnline()) {
         String title = player.getOpenInventory().getTitle();
         if (title != null && title.contains("Setup menu")) {
            if (!SetupMenu.refreshInPlaceFor(player)) {
               createMenu(player, false);
            }
         }
      }
   }

   private static boolean filterDungeon(EMPackage pkg) {
      return pkg instanceof WorldDungeonPackage
         || pkg instanceof WorldInstancedDungeonPackage
         || pkg instanceof DynamicDungeonPackage
         || pkg instanceof MetaPackage metaPackage
            && metaPackage.getPackages()
               .stream()
               .anyMatch(p -> p instanceof WorldDungeonPackage || p instanceof WorldInstancedDungeonPackage || p instanceof DynamicDungeonPackage);
   }

   private static boolean filterEvents(EMPackage pkg) {
      return pkg instanceof EventsPackage;
   }

   private static boolean filterModels(EMPackage pkg) {
      return pkg instanceof ModelsPackage;
   }
}
