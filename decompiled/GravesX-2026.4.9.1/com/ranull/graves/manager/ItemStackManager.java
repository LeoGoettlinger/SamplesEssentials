package com.ranull.graves.manager;

import com.ranull.graves.Graves;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.libraries.kyori.adventure.text.Component;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.StringUtil;
import dev.cwhead.GravesX.util.CustomModelDataUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackManager extends EntityDataManager {
   private final Graves plugin;

   public ItemStackManager(Graves plugin) {
      super(plugin);
      this.plugin = plugin;
   }

   public ItemStack getGraveObituary(Grave grave) {
      ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK, 1);
      BookMeta bookMeta = (BookMeta)itemStack.getItemMeta();
      Enchantment durability = this.plugin.getVersionManager().getEnchantmentForVersion("DURABILITY");
      if (bookMeta == null) {
         return itemStack;
      } else if (this.plugin.getIntegrationManager().hasMiniMessage()) {
         List<String> lineList = new ArrayList<>();
         List<String> loreList = new ArrayList<>();

         for (String lore : this.plugin.getConfigManager().getConfigSection("obituary.line", grave).getStringList("obituary.line")) {
            lineList.add(MiniMessage.convertLegacyToMiniMessage(StringUtil.parseString(lore, grave.getLocationDeath(), grave, this.plugin)));
         }

         for (String string : this.plugin.getConfigManager().getConfigSection("obituary.lore", grave).getStringList("obituary.lore")) {
            loreList.add(MiniMessage.convertLegacyToMiniMessage(StringUtil.parseString(string, grave.getLocationDeath(), grave, this.plugin)));
         }

         List<List<String>> pages = this.splitIntoPages(lineList, 13);
         int customModelData = this.plugin.getConfigManager().getConfigSection("obituary.model-data", grave).getInt("obituary.model-data", -1);
         CustomModelDataUtil.applyCustomModelData(bookMeta, customModelData);
         if (this.plugin.getConfigManager().getConfigSection("obituary.glow", grave).getBoolean("obituary.glow")) {
            bookMeta.addEnchant(durability, 1, true);
            if (!this.plugin.getVersionManager().is_v1_7()) {
               bookMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            }
         }

         if (!this.plugin.getVersionManager().is_v1_7() && !this.plugin.getVersionManager().is_v1_8() && !this.plugin.getVersionManager().is_v1_9()) {
            bookMeta.setGeneration(null);
         }

         String title = this.plugin.getConfigManager().getConfigSection("obituary.title", grave).getString("obituary.title");
         String author = this.plugin.getConfigManager().getConfigSection("obituary.author", grave).getString("obituary.author");
         String titleOriginal = StringUtil.parseString(title, grave, this.plugin);
         String authorOriginal = StringUtil.parseString(author, grave, this.plugin);
         Component titleConverted = MiniMessage.convertLegacyToComponent(titleOriginal);
         Component authorConverted = MiniMessage.convertLegacyToComponent(authorOriginal);
         List<Component> componentPages = pages.stream()
            .map(page -> MiniMessage.convertLegacyToComponent(String.join("\n", page)))
            .collect(Collectors.toList());
         List<Component> componentLore = loreList.stream().map(MiniMessage::convertLegacyToComponent).collect(Collectors.toList());
         return MiniMessage.formatBookMeta(this.plugin, grave, itemStack, titleConverted, authorConverted, componentPages, componentLore);
      } else {
         List<String> lineList = new ArrayList<>();
         List<String> loreList = new ArrayList<>();

         for (String lore : this.plugin.getConfigManager().getConfigSection("obituary.line", grave).getStringList("obituary.line")) {
            lineList.add(StringUtil.parseString(lore, grave.getLocationDeath(), grave, this.plugin));
         }

         for (String string : this.plugin.getConfigManager().getConfigSection("obituary.lore", grave).getStringList("obituary.lore")) {
            loreList.add(StringUtil.parseString(string, grave.getLocationDeath(), grave, this.plugin));
         }

         List<List<String>> pagesx = this.splitIntoPages(lineList, 13);
         int customModelDatax = this.plugin.getConfigManager().getConfigSection("obituary.model-data", grave).getInt("obituary.model-data", -1);
         CustomModelDataUtil.applyCustomModelData(bookMeta, customModelDatax);
         if (this.plugin.getConfigManager().getConfigSection("obituary.glow", grave).getBoolean("obituary.glow")) {
            bookMeta.addEnchant(durability, 1, true);
            if (!this.plugin.getVersionManager().is_v1_7()) {
               bookMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            }
         }

         if (!this.plugin.getVersionManager().is_v1_7() && !this.plugin.getVersionManager().is_v1_8() && !this.plugin.getVersionManager().is_v1_9()) {
            bookMeta.setGeneration(null);
         }

         List<String> stringPages = pagesx.stream().map(page -> String.join("\n", page)).collect(Collectors.toList());
         bookMeta.setPages(stringPages);
         bookMeta.setLore(loreList);
         bookMeta.setTitle(
            ChatColor.WHITE
               + StringUtil.parseString(
                  this.plugin.getConfigManager().getConfigSection("obituary.title", grave).getString("obituary.title"), grave, this.plugin
               )
         );
         bookMeta.setAuthor(
            StringUtil.parseString(this.plugin.getConfigManager().getConfigSection("obituary.author", grave).getString("obituary.author"), grave, this.plugin)
         );
         itemStack.setItemMeta(bookMeta);
         return itemStack;
      }
   }

   private List<List<String>> splitIntoPages(List<String> lines, int maxLinesPerPage) {
      List<List<String>> pages = new ArrayList<>();
      int i = 0;

      while (i < lines.size()) {
         pages.add(new ArrayList<>(lines.subList(i, Math.min(i + maxLinesPerPage, lines.size()))));
         i += maxLinesPerPage;
      }

      return pages;
   }

   public ItemStack getGraveHead(Grave grave) {
      ItemStack itemStack = this.plugin.getCompatibility().getSkullItemStack(grave, this.plugin);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta == null) {
         return itemStack;
      } else {
         List<String> loreList = new ArrayList<>();

         for (String string : this.plugin.getConfigManager().getConfigSection("head.lore", grave).getStringList("head.lore")) {
            if (this.plugin.getIntegrationManager().hasMiniMessage()) {
               String loreNew = StringUtil.parseString("&7" + string, grave.getLocationDeath(), grave, this.plugin);
               loreList.add(MiniMessage.parseString(loreNew));
            } else {
               loreList.add(ChatColor.GRAY + StringUtil.parseString(string, grave.getLocationDeath(), grave, this.plugin));
            }
         }

         int customModelData = this.plugin.getConfigManager().getConfigSection("head.model-data", grave).getInt("head.model-data", -1);
         CustomModelDataUtil.applyCustomModelData(itemMeta, customModelData);
         itemMeta.setLore(loreList);
         String displayName;
         if (this.plugin.getIntegrationManager().hasMiniMessage()) {
            String displayNameNew = StringUtil.parseString(
               "&f" + this.plugin.getConfigManager().getConfigSection("head.name", grave).getString("head.name"), grave, this.plugin
            );
            displayName = MiniMessage.parseString(displayNameNew);
         } else {
            displayName = ChatColor.WHITE
               + StringUtil.parseString(this.plugin.getConfigManager().getConfigSection("head.name", grave).getString("head.name"), grave, this.plugin);
         }

         itemMeta.setDisplayName(displayName);
         itemStack.setItemMeta(itemMeta);
         return itemStack;
      }
   }

   public ItemStack createGraveListItemStack(int number, Grave grave) {
      Enchantment durability = this.plugin.getVersionManager().getEnchantmentForVersion("DURABILITY");
      Material material;
      if (this.plugin.getConfigManager().getConfigSection("gui.menu.list.item.block", grave).getBoolean("gui.menu.list.item.block")) {
         String materialString = this.plugin.getConfigManager().getConfigSection("block.material", grave).getString("block.material", "CHEST");
         if ("PLAYER_HEAD".equals(materialString) && !this.plugin.getVersionManager().hasBlockData()) {
            materialString = "SKULL_ITEM";
         }

         material = Material.matchMaterial(materialString);
      } else {
         material = Material.matchMaterial(
            this.plugin.getConfigManager().getConfigSection("gui.menu.list.item.material", grave).getString("gui.menu.list.item.block", "CHEST")
         );
      }

      ItemStack itemStack = new ItemStack(material != null ? material : Material.CHEST);
      if ("PLAYER_HEAD".equals(itemStack.getType().name()) || "SKULL_ITEM".equals(itemStack.getType().name())) {
         itemStack = this.plugin.getCompatibility().getSkullItemStack(grave, this.plugin);
      }

      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta == null) {
         return itemStack;
      } else {
         String name;
         if (this.plugin.getIntegrationManager().hasMiniMessage()) {
            String newName = StringUtil.parseString(
                  "&f" + this.plugin.getConfigManager().getConfigSection("gui.menu.list.name", grave).getString("gui.menu.list.name"), grave, this.plugin
               )
               .replace("%number%", String.valueOf(number));
            name = MiniMessage.parseString(newName);
         } else {
            name = ChatColor.WHITE
               + StringUtil.parseString(
                     this.plugin.getConfigManager().getConfigSection("gui.menu.list.name", grave).getString("gui.menu.list.name"), grave, this.plugin
                  )
                  .replace("%number%", String.valueOf(number));
         }

         List<String> loreList = new ArrayList<>();
         int customModelData = this.plugin.getConfigManager().getConfigSection("gui.menu.list.model-data", grave).getInt("gui.menu.list.model-data", -1);

         for (String string : this.plugin.getConfigManager().getConfigSection("gui.menu.list.lore", grave).getStringList("gui.menu.list.lore")) {
            Entity ownerEntity = Bukkit.getEntity(grave.getOwnerUUID());
            if (this.plugin.getIntegrationManager().hasMiniMessage()) {
               String loreOriginal = StringUtil.parseString("&7" + string, ownerEntity, grave.getLocationDeath(), grave, this.plugin);
               loreList.add(MiniMessage.parseString(loreOriginal));
            } else {
               loreList.add(ChatColor.GRAY + StringUtil.parseString(string, ownerEntity, grave.getLocationDeath(), grave, this.plugin));
            }
         }

         if (this.plugin.getConfig().getBoolean("gui.menu.list.glow")) {
            itemMeta.addEnchant(durability, 1, true);
            itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         }

         CustomModelDataUtil.applyCustomModelData(itemMeta, customModelData);
         itemMeta.setDisplayName(name);
         itemMeta.setLore(loreList);
         itemStack.setItemMeta(itemMeta);
         return itemStack;
      }
   }

   public ItemStack createGraveMenuItemStack(int slot, Grave grave) {
      String materialString = this.plugin
         .getConfigManager()
         .getConfigSection("gui.menu.grave.slot." + slot + ".material", grave)
         .getString("gui.menu.grave.slot." + slot + ".material", "PAPER");
      Material material = Material.matchMaterial(materialString);
      Enchantment durability = this.plugin.getVersionManager().getEnchantmentForVersion("DURABILITY");
      if (material == null) {
         material = Material.PAPER;
         this.plugin.debugMessage(materialString.toUpperCase() + " is not a Material ENUM", 1);
      }

      ItemStack itemStack = new ItemStack(material);
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta == null) {
         return itemStack;
      } else {
         String name;
         if (this.plugin.getIntegrationManager().hasMiniMessage()) {
            String newName = StringUtil.parseString(
               "&f"
                  + this.plugin
                     .getConfigManager()
                     .getConfigSection("gui.menu.grave.slot." + slot + ".name", grave)
                     .getString("gui.menu.grave.slot." + slot + ".name"),
               grave,
               this.plugin
            );
            name = MiniMessage.parseString(newName);
         } else {
            name = ChatColor.WHITE
               + StringUtil.parseString(
                  this.plugin
                     .getConfigManager()
                     .getConfigSection("gui.menu.grave.slot." + slot + ".name", grave)
                     .getString("gui.menu.grave.slot." + slot + ".name"),
                  grave,
                  this.plugin
               );
         }

         List<String> loreList = new ArrayList<>();
         int customModelData = this.plugin
            .getConfigManager()
            .getConfigSection("gui.menu.grave.slot." + slot + ".model-data", grave)
            .getInt("gui.menu.grave.slot." + slot + ".model-data", -1);

         for (String string : this.plugin
            .getConfigManager()
            .getConfigSection("gui.menu.grave.slot." + slot + ".lore", grave)
            .getStringList("gui.menu.grave.slot." + slot + ".lore")) {
            Entity ownerEntity = Bukkit.getEntity(grave.getOwnerUUID());
            if (this.plugin.getIntegrationManager().hasMiniMessage()) {
               String newLore = StringUtil.parseString("&7" + string, ownerEntity, grave.getLocationDeath(), grave, this.plugin);
               loreList.add(MiniMessage.parseString(newLore));
            } else {
               loreList.add(ChatColor.GRAY + StringUtil.parseString(string, ownerEntity, grave.getLocationDeath(), grave, this.plugin));
            }
         }

         if (this.plugin.getConfig().getBoolean("gui.menu.grave.slot." + slot + ".glow")) {
            itemMeta.addEnchant(durability, 1, true);
            itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         }

         CustomModelDataUtil.applyCustomModelData(itemMeta, customModelData);
         itemMeta.setDisplayName(name);
         itemMeta.setLore(loreList);
         itemStack.setItemMeta(itemMeta);
         return itemStack;
      }
   }
}
