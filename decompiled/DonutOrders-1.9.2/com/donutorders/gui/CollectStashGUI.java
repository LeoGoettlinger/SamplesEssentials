package com.donutorders.gui;

import com.donutorders.manager.GUIManager;
import com.donutorders.model.Order;
import com.donutorders.util.ItemUtils;
import com.donutorders.util.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class CollectStashGUI extends BaseGUI {
   private static final int SLOT_COLLECT = 49;
   private static final int SLOT_CLOSE = 53;
   private static final int DISPLAY_SLOTS = 45;
   private final GUIManager guiManager;
   private final Order order;

   public CollectStashGUI(GUIManager guiManager, Order order, ItemStack[] stash) {
      super(Bukkit.createInventory(null, 54, MessageHelper.get("gui.collect-stash.title", "ᴄᴏʟʟᴇᴄᴛ")));
      this.guiManager = guiManager;
      this.order = order;
      this.build(stash);
   }

   private void build(ItemStack[] stash) {
      for (int i = 0; i < 45 && i < stash.length; i++) {
         if (stash[i] != null && stash[i].getType() != Material.AIR) {
            this.inventory.setItem(i, ItemUtils.cleanedCopy(stash[i]));
         }
      }

      this.inventory
         .setItem(
            49,
            ItemUtils.createGuiItem(
               Material.HOPPER,
               MessageHelper.get("gui.collect-stash.collect-all.name", "&a&lᴄᴏʟʟᴇᴄᴛ ᴀʟʟ"),
               MessageHelper.getList("gui.collect-stash.collect-all.lore")
            )
         );
      this.inventory
         .setItem(
            53,
            ItemUtils.createGuiItem(
               Material.BARRIER, MessageHelper.get("gui.collect-stash.close.name", "&c&lᴄʟᴏꜱᴇ"), MessageHelper.getList("gui.collect-stash.close.lore")
            )
         );
      this.fillEmpty();
   }

   @Override
   public void handleClick(Player player, int slot, ItemStack clicked, ClickType type) {
      if (slot == 49) {
         this.guiManager.getOrderManager().collectStash(player, this.order.getOrderId(), success -> {
            if (success) {
               MessageHelper.send(player, "collect-success", "&aɪᴛᴇᴍꜱ ᴄᴏʟʟᴇᴄᴛᴇᴅ ꜰʀᴏᴍ ꜱᴛᴀꜱʜ.");
            } else {
               MessageHelper.send(player, "collect-nothing", "&7ɴᴏᴛʜɪɴɢ ᴛᴏ ᴄᴏʟʟᴇᴄᴛ.");
            }

            this.guiManager.openYourOrders(player, 0);
         });
      } else if (slot == 53) {
         player.closeInventory();
      }
   }
}
