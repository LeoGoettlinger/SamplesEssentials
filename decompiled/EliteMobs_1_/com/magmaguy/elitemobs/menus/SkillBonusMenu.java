package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.config.menus.premade.SkillBonusMenuConfig;
import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusConfigFields;
import com.magmaguy.elitemobs.config.skillbonuses.SkillBonusesConfig;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.freeminecraftmodels.api.ModelItemAPI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class SkillBonusMenu {
   public static void openWeaponSelectMenu(Player player) {
      if (SkillsConfig.isSkillSystemEnabled()) {
         String menuName = SkillBonusMenuConfig.getWeaponSelectMenuName();
         if (DefaultConfig.useResourcePackModels()) {
            menuName = ChatColor.WHITE + "\udb83\udef1\udb83\ude0d\udb83\udef5          " + menuName;
         }

         menuName = ChatColorConverter.convert(menuName);
         Inventory inventory = Bukkit.createInventory(player, 36, menuName);
         inventory.setItem(SkillBonusMenuConfig.getSwordsSlot(), SkillBonusMenuConfig.getSwordsItem());
         inventory.setItem(SkillBonusMenuConfig.getAxesSlot(), SkillBonusMenuConfig.getAxesItem());
         inventory.setItem(SkillBonusMenuConfig.getBowsSlot(), SkillBonusMenuConfig.getBowsItem());
         inventory.setItem(SkillBonusMenuConfig.getCrossbowsSlot(), SkillBonusMenuConfig.getCrossbowsItem());
         inventory.setItem(SkillBonusMenuConfig.getTridentsSlot(), SkillBonusMenuConfig.getTridentsItem());
         inventory.setItem(SkillBonusMenuConfig.getHoesSlot(), SkillBonusMenuConfig.getHoesItem());
         inventory.setItem(SkillBonusMenuConfig.getArmorSlot(), SkillBonusMenuConfig.getArmorItem());
         inventory.setItem(SkillBonusMenuConfig.getMacesSlot(), SkillBonusMenuConfig.getMacesItem());
         inventory.setItem(SkillBonusMenuConfig.getSpearsSlot(), SkillBonusMenuConfig.getSpearsItem());
         inventory.setItem(
            SkillBonusMenuConfig.getStavesSlot(),
            magicWeaponIcon(SkillBonusMenuConfig.getStavesItem(), SkillBonusMenuConfig.getDefaultStavesMaterial(), "fmm_default_arcane_staff")
         );
         inventory.setItem(
            SkillBonusMenuConfig.getWandsSlot(),
            magicWeaponIcon(SkillBonusMenuConfig.getWandsItem(), SkillBonusMenuConfig.getDefaultWandsMaterial(), "fmm_default_arcane_wand")
         );
         player.openInventory(inventory);
         SkillBonusMenu.SkillBonusMenuEvents.weaponSelectMenus.add(inventory);
      }
   }

   private static ItemStack magicWeaponIcon(ItemStack icon, Material defaultMaterial, String fmmModel) {
      if (icon != null && icon.getType() == defaultMaterial && Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
         ItemStack weapon = icon.clone();

         try {
            if (ModelItemAPI.applyDisplayModel(weapon, fmmModel)) {
               return weapon;
            }
         } catch (LinkageError var5) {
         }

         return icon;
      } else {
         return icon;
      }
   }

   public static void openSkillSelectMenu(Player player, SkillType skillType) {
      if (SkillsConfig.isSkillSystemEnabled()) {
         String menuName = SkillBonusMenuConfig.getSkillSelectMenuName().replace("%skill_type%", SkillBonusMenuConfig.getSkillTypeDisplayName(skillType));
         if (DefaultConfig.useResourcePackModels()) {
            menuName = ChatColor.WHITE + "\udb83\udef1\udb83\ude0c\udb83\udef5          " + menuName;
         }

         menuName = ChatColorConverter.convert(menuName);
         Inventory inventory = Bukkit.createInventory(player, 54, menuName);
         long xp = PlayerData.getSkillXP(player.getUniqueId(), skillType);
         int skillLevel = SkillXPCalculator.levelFromTotalXP(xp);
         List<String> activeSkillIds = PlayerSkillSelection.getActiveSkills(player.getUniqueId(), skillType);
         List<SkillBonusConfigFields> skills = SkillBonusesConfig.getEnabledBySkillType(skillType);
         skills.sort(Comparator.comparingInt(SkillBonusConfigFields::getUnlockTier));
         SkillBonusMenu.SkillMenuData menuData = new SkillBonusMenu.SkillMenuData(skillType, player.getUniqueId());
         Map<Integer, List<SkillBonusConfigFields>> tierMap = new LinkedHashMap<>();

         for (SkillBonusConfigFields skillConfig : skills) {
            tierMap.computeIfAbsent(skillConfig.getUnlockTier(), k -> new ArrayList<>()).add(skillConfig);
         }

         int[][] tierSlots = new int[][]{{10, 11, 12}, {14, 15, 16}, {28, 29, 30}, {32, 33, 34}};

         for (int tier = 1; tier <= 4; tier++) {
            List<SkillBonusConfigFields> tierSkills = tierMap.getOrDefault(tier, List.of());
            int[] slots = tierSlots[tier - 1];

            for (int i = 0; i < tierSkills.size() && i < slots.length; i++) {
               SkillBonusConfigFields skillConfig = tierSkills.get(i);
               ItemStack skillItem = createSkillItem(skillConfig, skillLevel, activeSkillIds);
               inventory.setItem(slots[i], skillItem);
               menuData.getSlotToSkill().put(slots[i], skillConfig);
            }
         }

         inventory.setItem(SkillBonusMenuConfig.getBackSlot(), SkillBonusMenuConfig.getBackItem());
         inventory.setItem(SkillBonusMenuConfig.getInfoSlot(), createInfoItem(player, skillType, skillLevel, activeSkillIds));
         player.openInventory(inventory);
         SkillBonusMenu.SkillBonusMenuEvents.skillSelectMenus.put(inventory, menuData);
      }
   }

   private static ItemStack createSkillItem(SkillBonusConfigFields skillConfig, int playerLevel, List<String> activeSkillIds) {
      boolean isActive = activeSkillIds.contains(skillConfig.getSkillId());
      boolean isUnlocked = playerLevel >= skillConfig.getRequiredLevel();
      Material material = skillConfig.getDisplayMaterial();
      List<String> lore = new ArrayList<>();
      String name;
      if (isActive) {
         name = SkillBonusMenuConfig.getActivePrefix() + skillConfig.getName();
      } else if (isUnlocked) {
         name = SkillBonusMenuConfig.getUnlockedPrefix() + skillConfig.getName();
      } else {
         name = SkillBonusMenuConfig.getLockedPrefix() + skillConfig.getName();
         material = Material.GRAY_DYE;
      }

      lore.addAll(skillConfig.getDescription());
      lore.add("");
      lore.add(SkillBonusMenuConfig.getSkillTypeLabel() + SkillBonusMenuConfig.getBonusTypeDisplayName(skillConfig.getBonusType()));
      lore.add(
         SkillBonusMenuConfig.getSkillTierLabel()
            + SkillBonusMenuConfig.getSkillTierLevelFormat()
               .replace("%tier%", String.valueOf(skillConfig.getUnlockTier()))
               .replace("%level%", String.valueOf(skillConfig.getRequiredLevel()))
      );
      if (isUnlocked) {
         SkillBonus skill = SkillBonusRegistry.getSkillById(skillConfig.getSkillId());
         if (skill != null) {
            String formattedBonus = skill.getFormattedBonus(playerLevel);
            lore.add(
               SkillBonusMenuConfig.getSkillValueLabel()
                  + (formattedBonus.isEmpty() ? String.format("%.1f", skill.getBonusValue(playerLevel)) : formattedBonus)
            );
         }
      }

      lore.add("");
      if (isActive) {
         lore.add(SkillBonusMenuConfig.getClickToDeactivate());
      } else if (isUnlocked) {
         lore.add(SkillBonusMenuConfig.getClickToActivate());
      } else {
         lore.add(SkillBonusMenuConfig.getLockedLore().replace("%level%", String.valueOf(skillConfig.getRequiredLevel())));
      }

      ItemStack item = ItemStackGenerator.generateItemStack(material, name, lore);
      ItemMeta meta = item.getItemMeta();
      meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
      item.setItemMeta(meta);
      return item;
   }

   private static ItemStack createInfoItem(Player player, SkillType skillType, int skillLevel, List<String> activeSkillIds) {
      List<String> lore = new ArrayList<>();
      lore.add(SkillBonusMenuConfig.getInfoSkillTypeLabel() + SkillBonusMenuConfig.getSkillTypeDisplayName(skillType));
      lore.add(SkillBonusMenuConfig.getInfoYourLevelLabel() + skillLevel);
      lore.add(SkillBonusMenuConfig.getInfoActiveSkillsLabel().replace("%count%", String.valueOf(activeSkillIds.size())));
      lore.add("");
      if (!activeSkillIds.isEmpty()) {
         lore.add(SkillBonusMenuConfig.getInfoActiveSkillsHeader());

         for (String skillId : activeSkillIds) {
            SkillBonusConfigFields config = SkillBonusesConfig.getBySkillId(skillId);
            if (config != null) {
               lore.add(SkillBonusMenuConfig.getInfoActiveSkillPrefix() + config.getName());
            }
         }
      }

      lore.add("");
      lore.add(SkillBonusMenuConfig.getInfoUnlockLevelsHeader());
      lore.add(SkillBonusMenuConfig.getInfoTier1Unlock());
      lore.add(SkillBonusMenuConfig.getInfoTier2Unlock());
      lore.add(SkillBonusMenuConfig.getInfoTier3Unlock());
      lore.add(SkillBonusMenuConfig.getInfoTier4Unlock());
      return ItemStackGenerator.generateItemStack(Material.BOOK, SkillBonusMenuConfig.getInfoItemTitle(), lore);
   }

   public static class SkillBonusMenuEvents implements Listener {
      private static final Set<Inventory> weaponSelectMenus = new HashSet<>();
      private static final Map<Inventory, SkillBonusMenu.SkillMenuData> skillSelectMenus = new HashMap<>();

      public static void shutdown() {
         weaponSelectMenus.clear();
         skillSelectMenus.clear();
      }

      @EventHandler
      public void onInventoryClick(InventoryClickEvent event) {
         if (event.getWhoClicked() instanceof Player player) {
            if (EliteMenu.isEliteMenu(event, weaponSelectMenus)) {
               event.setCancelled(true);
               if (EliteMenu.isTopMenu(event)) {
                  if (SharedShopElements.itemNullPointerPrevention(event)) {
                     this.handleWeaponSelectClick(player, event);
                  }
               }
            } else {
               if (skillSelectMenus.containsKey(event.getInventory())) {
                  event.setCancelled(true);
                  if (!EliteMenu.isTopMenu(event)) {
                     return;
                  }

                  if (!SharedShopElements.itemNullPointerPrevention(event)) {
                     return;
                  }

                  this.handleSkillSelectClick(player, event);
               }
            }
         }
      }

      private void handleWeaponSelectClick(Player player, InventoryClickEvent event) {
         int slot = event.getSlot();
         SkillType selectedType = null;
         if (slot == SkillBonusMenuConfig.getSwordsSlot()) {
            selectedType = SkillType.SWORDS;
         } else if (slot == SkillBonusMenuConfig.getAxesSlot()) {
            selectedType = SkillType.AXES;
         } else if (slot == SkillBonusMenuConfig.getBowsSlot()) {
            selectedType = SkillType.BOWS;
         } else if (slot == SkillBonusMenuConfig.getCrossbowsSlot()) {
            selectedType = SkillType.CROSSBOWS;
         } else if (slot == SkillBonusMenuConfig.getTridentsSlot()) {
            selectedType = SkillType.TRIDENTS;
         } else if (slot == SkillBonusMenuConfig.getHoesSlot()) {
            selectedType = SkillType.HOES;
         } else if (slot == SkillBonusMenuConfig.getArmorSlot()) {
            selectedType = SkillType.ARMOR;
         } else if (slot == SkillBonusMenuConfig.getMacesSlot()) {
            selectedType = SkillType.MACES;
         } else if (slot == SkillBonusMenuConfig.getSpearsSlot()) {
            selectedType = SkillType.SPEARS;
         } else if (slot == SkillBonusMenuConfig.getStavesSlot()) {
            selectedType = SkillType.STAVES;
         } else if (slot == SkillBonusMenuConfig.getWandsSlot()) {
            selectedType = SkillType.WANDS;
         }

         if (selectedType != null) {
            SkillBonusMenu.openSkillSelectMenu(player, selectedType);
         }
      }

      private void handleSkillSelectClick(Player player, InventoryClickEvent event) {
         SkillBonusMenu.SkillMenuData menuData = skillSelectMenus.get(event.getInventory());
         if (menuData != null) {
            int slot = event.getSlot();
            if (slot == SkillBonusMenuConfig.getBackSlot()) {
               SkillBonusMenu.openWeaponSelectMenu(player);
            } else if (slot != SkillBonusMenuConfig.getInfoSlot()) {
               SkillBonusConfigFields skillConfig = menuData.getSlotToSkill().get(slot);
               if (skillConfig != null) {
                  String skillId = skillConfig.getSkillId();
                  UUID playerUUID = player.getUniqueId();
                  SkillType skillType = menuData.getSkillType();
                  long xp = PlayerData.getSkillXP(playerUUID, skillType);
                  int playerLevel = SkillXPCalculator.levelFromTotalXP(xp);
                  if (playerLevel < skillConfig.getRequiredLevel()) {
                     player.sendMessage(SkillBonusMenuConfig.getSkillRequiresLevelMessage().replace("%level%", String.valueOf(skillConfig.getRequiredLevel())));
                  } else {
                     List<String> activeSkills = PlayerSkillSelection.getActiveSkills(playerUUID, skillType);
                     if (activeSkills.contains(skillId)) {
                        PlayerSkillSelection.removeActiveSkill(playerUUID, skillType, skillId);
                        player.sendMessage(SkillBonusMenuConfig.getSkillDeactivatedMessage().replace("%skill%", skillConfig.getName()));
                        SkillBonus skill = SkillBonusRegistry.getSkillById(skillId);
                        if (skill != null) {
                           skill.onDeactivate(player);
                        }
                     } else {
                        if (activeSkills.size() >= 3) {
                           player.sendMessage(SkillBonusMenuConfig.getMaxActiveSkillsReached());
                           return;
                        }

                        PlayerSkillSelection.addActiveSkill(playerUUID, skillType, skillId);
                        player.sendMessage(SkillBonusMenuConfig.getSkillActivatedMessage().replace("%skill%", skillConfig.getName()));
                        SkillBonus skill = SkillBonusRegistry.getSkillById(skillId);
                        if (skill != null) {
                           skill.onActivate(player);
                        }
                     }

                     SkillBonusMenu.openSkillSelectMenu(player, skillType);
                  }
               }
            }
         }
      }

      @EventHandler
      public void onInventoryClose(InventoryCloseEvent event) {
         weaponSelectMenus.remove(event.getInventory());
         skillSelectMenus.remove(event.getInventory());
      }
   }

   private static class SkillMenuData {
      private final SkillType skillType;
      private final UUID playerUUID;
      private final Map<Integer, SkillBonusConfigFields> slotToSkill = new HashMap<>();

      public SkillMenuData(SkillType skillType, UUID playerUUID) {
         this.skillType = skillType;
         this.playerUUID = playerUUID;
      }

      @Generated
      public SkillType getSkillType() {
         return this.skillType;
      }

      @Generated
      public UUID getPlayerUUID() {
         return this.playerUUID;
      }

      @Generated
      public Map<Integer, SkillBonusConfigFields> getSlotToSkill() {
         return this.slotToSkill;
      }
   }
}
