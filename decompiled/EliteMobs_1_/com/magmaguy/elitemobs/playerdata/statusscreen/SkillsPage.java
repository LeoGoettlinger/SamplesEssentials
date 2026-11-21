package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.SkillBonusMenuConfig;
import com.magmaguy.elitemobs.menus.SkillBonusMenu;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SkillsPage {
   private static final int[] SKILL_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22};

   private SkillsPage() {
   }

   protected static TextComponent skillsPage(Player targetPlayer) {
      TextComponent textComponent = new TextComponent();
      TextComponent header = new TextComponent(PlayerStatusMenuConfig.getSkillsPageHeader() + "\n\n");
      textComponent.addExtra(header);

      for (SkillType skillType : SkillType.values()) {
         long totalXP = PlayerData.getSkillXP(targetPlayer.getUniqueId(), skillType);
         int level = SkillXPCalculator.levelFromTotalXP(totalXP);
         long xpInLevel = SkillXPCalculator.xpProgressInCurrentLevel(totalXP);
         long xpForNext = SkillXPCalculator.xpToNextLevel(level);
         double progress = SkillXPCalculator.levelProgress(totalXP);
         String progressBar = createProgressBar(progress);
         String levelLine = PlayerStatusMenuConfig.getSkillsPageLevelFormat()
            .replace("$skillName", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType))
            .replace("$level", String.valueOf(level));
         String xpLine = PlayerStatusMenuConfig.getSkillsPageXpFormat()
            .replace("$progressBar", progressBar)
            .replace("$currentXp", formatNumber(xpInLevel))
            .replace("$nextXp", formatNumber(xpForNext));
         TextComponent line = new TextComponent(levelLine + "\n" + xpLine + "\n\n");
         textComponent.addExtra(line);
      }

      return textComponent;
   }

   protected static void skillsPage(Player targetPlayer, Player requestingPlayer) {
      Inventory inventory = Bukkit.createInventory(requestingPlayer, 27, SkillsConfig.getSkillsMenuTitle().replace("&", "§"));
      int slotIndex = 0;
      Map<Integer, SkillType> slotToSkillType = new HashMap<>();

      for (SkillType skillType : SkillType.values()) {
         if (slotIndex >= SKILL_SLOTS.length) {
            break;
         }

         ItemStack skillItem = createSkillItem(targetPlayer, skillType);
         inventory.setItem(SKILL_SLOTS[slotIndex], skillItem);
         slotToSkillType.put(SKILL_SLOTS[slotIndex], skillType);
         slotIndex++;
      }

      inventory.setItem(26, PlayerStatusMenuConfig.getBackItem());
      if (requestingPlayer.openInventory(inventory) != null) {
         StatusInventorySafety.protect(inventory);
         SkillsPage.SkillsPageEvents.pageInventories.put(inventory, slotToSkillType);
      }
   }

   static int skillSlotCapacity() {
      return SKILL_SLOTS.length;
   }

   private static ItemStack createSkillItem(Player player, SkillType skillType) {
      Material material = getMaterialForSkill(skillType);
      ItemStack item = new ItemStack(material);
      ItemMeta meta = item.getItemMeta();
      long totalXP = PlayerData.getSkillXP(player.getUniqueId(), skillType);
      int level = SkillXPCalculator.levelFromTotalXP(totalXP);
      meta.setDisplayName(
         PlayerStatusMenuConfig.getSkillItemDisplayNameFormat()
            .replace("$level", String.valueOf(level))
            .replace("$skillName", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType))
      );
      List<String> lore = new ArrayList<>();
      lore.add(PlayerStatusMenuConfig.getSkillItemSelectLore1());
      lore.add(PlayerStatusMenuConfig.getSkillItemSelectLore2());
      meta.setLore(lore);
      meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
      item.setItemMeta(meta);
      return item;
   }

   private static Material getMaterialForSkill(SkillType skillType) {
      return switch (skillType) {
         case ARMOR -> {
            Material var16 = Material.DIAMOND_CHESTPLATE;
            yield var16;
         }
         case SWORDS -> {
            Material var15 = Material.DIAMOND_SWORD;
            yield var15;
         }
         case AXES -> {
            Material var14 = Material.DIAMOND_AXE;
            yield var14;
         }
         case BOWS -> {
            Material var13 = Material.BOW;
            yield var13;
         }
         case CROSSBOWS -> {
            Material var12 = Material.CROSSBOW;
            yield var12;
         }
         case TRIDENTS -> {
            Material var11 = Material.TRIDENT;
            yield var11;
         }
         case HOES -> {
            Material var10 = Material.DIAMOND_HOE;
            yield var10;
         }
         case MACES -> {
            Material var9 = Material.MACE;
            yield var9;
         }
         case SPEARS -> {
            Material var8;
            try {
               var8 = Material.IRON_SPEAR;
            } catch (NoSuchFieldError var4) {
               var8 = Material.TRIDENT;
               yield var8;
            }

            yield var8;
         }
         case STAVES -> {
            Material var6;
            try {
               var6 = Material.WOODEN_SPEAR;
            } catch (NoSuchFieldError var3) {
               var6 = Material.STICK;
               yield var6;
            }

            yield var6;
         }
         case WANDS -> {
            Material var1 = Material.BLAZE_ROD;
            yield var1;
         }
      };
   }

   private static String createProgressBar(double progress) {
      int totalBars = 20;
      int filledBars = (int)(progress * totalBars);
      int emptyBars = totalBars - filledBars;
      StringBuilder bar = new StringBuilder("§a");

      for (int i = 0; i < filledBars; i++) {
         bar.append("|");
      }

      bar.append("§7");

      for (int i = 0; i < emptyBars; i++) {
         bar.append("|");
      }

      return bar.toString();
   }

   private static String formatNumber(long number) {
      if (number >= 1000000000L) {
         return String.format("%.1fB", number / 1.0E9);
      } else if (number >= 1000000L) {
         return String.format("%.1fM", number / 1000000.0);
      } else {
         return number >= 1000L ? String.format("%.1fK", number / 1000.0) : String.valueOf(number);
      }
   }

   public static class SkillsPageEvents implements Listener {
      private static final Map<Inventory, Map<Integer, SkillType>> pageInventories = new HashMap<>();

      public static void shutdown() {
         pageInventories.clear();
      }

      @EventHandler
      public void onInventoryInteract(InventoryClickEvent event) {
         Player player = ((Player)event.getWhoClicked()).getPlayer();
         if (pageInventories.containsKey(event.getInventory())) {
            event.setCancelled(true);
            if (event.getClickedInventory() == event.getView().getTopInventory()) {
               if (event.getSlot() == 26) {
                  player.closeInventory();
                  CoverPage.coverPage(player);
               } else {
                  Map<Integer, SkillType> slotToSkillType = pageInventories.get(event.getInventory());
                  SkillType clickedSkillType = slotToSkillType.get(event.getSlot());
                  if (clickedSkillType != null) {
                     player.closeInventory();
                     SkillBonusMenu.openSkillSelectMenu(player, clickedSkillType);
                  }
               }
            }
         }
      }

      @EventHandler
      public void onInventoryClose(InventoryCloseEvent event) {
         pageInventories.remove(event.getInventory());
      }
   }
}
