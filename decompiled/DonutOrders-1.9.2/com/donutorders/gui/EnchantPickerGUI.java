package com.donutorders.gui;

import com.donutorders.manager.GUIManager;
import com.donutorders.util.EnchantOrderUtils;
import com.donutorders.util.ItemUtils;
import com.donutorders.util.MessageHelper;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantPickerGUI extends BaseGUI {
   private static final int PAGE_SIZE = 45;
   private static final int SLOT_PREV = 45;
   private static final int SLOT_NEXT = 53;
   private static final int SLOT_BACK = 49;
   private final GUIManager guiManager;
   private final List<Enchantment> enchants;
   private final int page;
   private final int maxPage;

   public EnchantPickerGUI(GUIManager guiManager, int page) {
      super(Bukkit.createInventory(null, 54, MessageHelper.get("gui.enchant-picker.title", "ꜱᴇʟᴇᴄᴛ ᴇɴᴄʜᴀɴᴛ")));
      this.guiManager = guiManager;
      this.enchants = EnchantOrderUtils.listBookEnchantments();
      this.page = page;
      this.maxPage = this.enchants.isEmpty() ? 0 : Math.max(0, (this.enchants.size() - 1) / 45);
      this.build();
   }

   private void build() {
      int start = this.page * 45;
      int end = Math.min(start + 45, this.enchants.size());

      for (int i = start; i < end; i++) {
         this.inventory.setItem(i - start, this.buildEnchantItem(this.enchants.get(i)));
      }

      if (this.page > 0) {
         this.inventory
            .setItem(
               45,
               ItemUtils.createGuiItem(
                  Material.ARROW,
                  MessageHelper.get("buttons.previous.name", "&7« ᴘʀᴇᴠɪᴏᴜꜱ"),
                  MessageHelper.getList("buttons.previous.lore", "page", String.valueOf(this.page), "max_page", String.valueOf(this.maxPage + 1))
               )
            );
      }

      if (this.page < this.maxPage) {
         this.inventory
            .setItem(
               53,
               ItemUtils.createGuiItem(
                  Material.ARROW,
                  MessageHelper.get("buttons.next.name", "&7ɴᴇxᴛ »"),
                  MessageHelper.getList("buttons.next.lore", "page", String.valueOf(this.page + 2), "max_page", String.valueOf(this.maxPage + 1))
               )
            );
      }

      this.inventory
         .setItem(
            49,
            ItemUtils.createGuiItem(
               Material.BARRIER, MessageHelper.get("gui.enchant-picker.back.name", "&c&lʙᴀᴄᴋ"), MessageHelper.getList("gui.enchant-picker.back.lore")
            )
         );
      this.fillEmpty();
   }

   private ItemStack buildEnchantItem(Enchantment enchant) {
      ItemStack book = ItemUtils.cleanedCopy(EnchantOrderUtils.buildEnchantedBook(enchant, 1));
      ItemMeta meta = book.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(MessageHelper.getNamed("gui.enchant-picker.enchant.name", "&f{enchant}", "enchant", EnchantOrderUtils.prettyEnchantName(enchant)));
         meta.setLore(MessageHelper.getList("gui.enchant-picker.enchant.lore"));
         meta.addItemFlags(ItemFlag.values());
         book.setItemMeta(meta);
      }

      return book;
   }

   @Override
   public void handleClick(Player player, int slot, ItemStack clicked, ClickType type) {
      if (slot == 45 && this.page > 0) {
         this.guiManager.openEnchantPicker(player, this.page - 1);
      } else if (slot == 53 && this.page < this.maxPage) {
         this.guiManager.openEnchantPicker(player, this.page + 1);
      } else if (slot == 49) {
         this.guiManager.openNewOrderPicker(player, 0);
      } else {
         int enchantIndex = this.page * 45 + slot;
         if (slot < 45 && enchantIndex < this.enchants.size()) {
            Enchantment selected = this.enchants.get(enchantIndex);
            GUIManager.PlayerGUIState state = this.guiManager.getState(player.getUniqueId());
            if (state != null) {
               state.enchantPickerPage = this.page;
            }

            this.guiManager.openEnchantLevel(player, selected);
         }
      }
   }
}
