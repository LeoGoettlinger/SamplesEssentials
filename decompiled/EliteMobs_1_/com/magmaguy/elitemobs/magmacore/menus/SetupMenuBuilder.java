package com.magmaguy.elitemobs.magmacore.menus;

import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class SetupMenuBuilder {
   public static final String MENU_SKIN_PREFIX = "\udb83\udef1\udb83\ude0b\udb83\udef5          ";
   private final JavaPlugin plugin;
   private final Player player;
   private final List<SetupMenu.SetupMenuFilter> filters = new ArrayList<>();
   private String title = "Setup menu";
   private String titleIconPrefix = "\udb83\udef1\udb83\ude0b\udb83\udef5          ";
   private MenuButton infoButton = null;
   private List<? extends ContentPackage> packages = null;
   private final List<ContentPackage> prependedPackages = new ArrayList<>();
   private final List<ContentPackage> appendedPackages = new ArrayList<>();
   private final List<SetupMenuBuilder.FilterDefinition> filterDefinitions = new ArrayList<>();
   private final List<MenuButton> toolbarButtons = new ArrayList<>();

   public SetupMenuBuilder(JavaPlugin plugin, Player player) {
      this.plugin = plugin;
      this.player = player;
   }

   public SetupMenuBuilder title(String title) {
      this.title = title;
      return this;
   }

   public SetupMenuBuilder titleIconPrefix(String prefix) {
      this.titleIconPrefix = prefix;
      return this;
   }

   public SetupMenuBuilder infoButton(MenuButton infoButton) {
      this.infoButton = infoButton;
      return this;
   }

   public SetupMenuBuilder packages(List<? extends ContentPackage> packages) {
      this.packages = packages;
      return this;
   }

   public SetupMenuBuilder prependPackage(ContentPackage extraPackage) {
      if (extraPackage != null) {
         this.prependedPackages.add(extraPackage);
      }

      return this;
   }

   public SetupMenuBuilder appendPackage(ContentPackage extraPackage) {
      if (extraPackage != null) {
         this.appendedPackages.add(extraPackage);
      }

      return this;
   }

   public SetupMenuBuilder addToolbarButton(MenuButton toolbarButton) {
      if (toolbarButton != null) {
         this.toolbarButtons.add(toolbarButton);
      }

      return this;
   }

   public SetupMenuBuilder addFilter(Material material, String name, Predicate<? extends ContentPackage> predicate) {
      this.filterDefinitions.add(new SetupMenuBuilder.FilterDefinition(material, name, predicate));
      return this;
   }

   private List<ContentPackage> filterPackages(Predicate<? extends ContentPackage> predicate) {
      List<ContentPackage> result = new ArrayList<>();
      if (this.packages == null) {
         return result;
      } else {
         result.addAll(this.prependedPackages);
         Predicate rawPredicate = predicate;

         for (ContentPackage pkg : this.packages) {
            if (rawPredicate.test(pkg)) {
               result.add(pkg);
            }
         }

         return result;
      }
   }

   public SetupMenu open() {
      if (this.packages == null) {
         Logger.warn("SetupMenuBuilder.open() called without packages set; aborting menu open.");
         return null;
      } else if (this.infoButton == null) {
         Logger.warn("SetupMenuBuilder.open() called without infoButton set; aborting menu open.");
         return null;
      } else {
         this.filters.clear();

         for (SetupMenuBuilder.FilterDefinition filterDefinition : this.filterDefinitions) {
            ItemStack filterItem = ItemStackGenerator.generateItemStack(filterDefinition.material(), filterDefinition.name());
            List<ContentPackage> filtered = this.filterPackages(filterDefinition.predicate());
            this.filters.add(new SetupMenu.SetupMenuFilter(filterItem, filtered));
         }

         List<ContentPackage> finalPackages = new ArrayList<>(this.prependedPackages);
         finalPackages.addAll(this.packages);
         finalPackages.addAll(this.appendedPackages);
         String finalTitle = this.title;
         if (this.titleIconPrefix != null && !this.titleIconPrefix.isEmpty()) {
            finalTitle = ChatColor.WHITE + this.titleIconPrefix + finalTitle;
         }

         return new SetupMenu(this.plugin, this.player, this.infoButton, finalPackages, this.filters, this.toolbarButtons, finalTitle);
      }
   }

   private record FilterDefinition(Material material, String name, Predicate<? extends ContentPackage> predicate) {
   }
}
