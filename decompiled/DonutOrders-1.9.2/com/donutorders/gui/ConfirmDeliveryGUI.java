package com.donutorders.gui;

import com.donutorders.DonutOrders;
import com.donutorders.manager.GUIManager;
import com.donutorders.model.Order;
import com.donutorders.util.DeliveryItemAccount;
import com.donutorders.util.DeliveryItemUtils;
import com.donutorders.util.ItemUtils;
import com.donutorders.util.MessageHelper;
import com.donutorders.util.NumberFormatter;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ConfirmDeliveryGUI extends BaseGUI {
   private static final int SLOT_CONFIRM = 11;
   private static final int SLOT_SUMMARY = 13;
   private static final int SLOT_CANCEL = 15;
   private final GUIManager guiManager;
   private final Order order;
   private final DeliveryItemAccount account;
   private final int deliverCount;
   private final double payout;

   public ConfirmDeliveryGUI(GUIManager guiManager, Player seller, Order order, ItemStack[] items) {
      super(Bukkit.createInventory(null, 27, MessageHelper.get("gui.confirm-delivery.title", "ᴄᴏɴꜰɪʀᴍ ᴅᴇʟɪᴠᴇʀʏ")));
      this.guiManager = guiManager;
      this.order = order;
      this.account = new DeliveryItemAccount(items);
      this.deliverCount = Math.min(DeliveryItemUtils.countAvailable(seller, this.account.snapshot(), order.getItemTemplate()), order.getAmountRemaining());
      this.payout = order.getPricePerItem() * this.deliverCount;
      this.build();
   }

   private void build() {
      String itemName = ItemUtils.describeOrderItem(this.order.getItemTemplate());
      this.inventory
         .setItem(
            13,
            ItemUtils.createGuiItem(
               this.order.getItemTemplate().getType(),
               MessageHelper.getNamed("gui.confirm-delivery.summary.name", "&f&l{item}", "item", itemName),
               MessageHelper.getList(
                  "gui.confirm-delivery.summary.lore", "count", NumberFormatter.format(this.deliverCount), "payout", NumberFormatter.formatPrice(this.payout)
               )
            )
         );
      this.inventory
         .setItem(
            11,
            ItemUtils.createGuiItem(
               Material.LIME_WOOL,
               MessageHelper.get("gui.confirm-delivery.confirm.name", "&a&lᴄᴏɴꜰɪʀᴍ"),
               MessageHelper.getList("gui.confirm-delivery.confirm.lore")
            )
         );
      this.inventory
         .setItem(
            15,
            ItemUtils.createGuiItem(
               Material.RED_WOOL,
               MessageHelper.get("gui.confirm-delivery.cancel.name", "&c&lᴄᴀɴᴄᴇʟ"),
               MessageHelper.getList("gui.confirm-delivery.cancel.lore")
            )
         );
      this.fillEmpty();
   }

   @Override
   public void handleClick(Player player, int slot, ItemStack clicked, ClickType type) {
      if (slot == 11) {
         if (!this.account.trySubmit()) {
            return;
         }

         log("ConfirmDeliveryGUI submitted by " + player.getName() + " — dispatching fulfillOrder for " + this.deliverCount + " items.");
         this.guiManager
            .getOrderManager()
            .fulfillOrder(
               player,
               this.order.getOrderId(),
               this.account.snapshot(),
               (success, errorMsg) -> {
                  if (success) {
                     MessageHelper.sendPrefixed(
                        player,
                        "delivery-success",
                        "&aᴅᴇʟɪᴠᴇʀᴇᴅ &f{0}× {1}&a. ʏᴏᴜ ᴇᴀʀɴᴇᴅ &f{2}&a.",
                        NumberFormatter.format(this.deliverCount),
                        ItemUtils.describeOrderItem(this.order.getItemTemplate()),
                        NumberFormatter.formatPrice(this.payout)
                     );
                  } else {
                     player.sendMessage(errorMsg != null ? errorMsg : MessageHelper.get("delivery-failed", "&cᴅᴇʟɪᴠᴇʀʏ ꜰᴀɪʟᴇᴅ. ᴘʟᴇᴀꜱᴇ ᴛʀʏ ᴀɢᴀɪɴ."));
                  }

                  this.guiManager.openPublicOrders(player, 0);
               }
            );
      } else if (slot == 15) {
         this.returnItems(player);
         this.guiManager.openPublicOrders(player, 0);
      }
   }

   public void returnItems(Player player) {
      this.returnItems(player, null);
   }

   public void returnItems(Player player, List<ItemStack> deathDrops) {
      List<ItemStack> toGive = this.account.takeForReturn();
      if (toGive.isEmpty()) {
         log(
            "ConfirmDeliveryGUI.returnItems skipped for "
               + (player != null ? player.getName() : "unknown")
               + " — delivery already "
               + this.account.state()
               + "."
         );
      } else {
         log("ConfirmDeliveryGUI.returnItems — returning snapshot to " + (player != null ? player.getName() : "unknown"));
         if (deathDrops != null) {
            deathDrops.addAll(toGive);
         } else {
            ItemUtils.giveOrDropAll(player, toGive);
         }
      }
   }

   private static void log(String message) {
      DonutOrders plugin = DonutOrders.getInstance();
      if (plugin != null) {
         plugin.getLogger().info("[DonutOrders] " + message);
      }
   }
}
