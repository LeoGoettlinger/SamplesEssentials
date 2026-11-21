package com.donutorders.gui;

import com.donutorders.manager.GUIManager;
import com.donutorders.model.Order;
import com.donutorders.model.OrderStatus;
import com.donutorders.util.ItemUtils;
import com.donutorders.util.MessageHelper;
import com.donutorders.util.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class OrderDetailGUI extends BaseGUI {
   private static final int SLOT_COLLECT = 11;
   private static final int SLOT_SUMMARY = 13;
   private static final int SLOT_CANCEL = 15;
   private static final int SLOT_BACK = 22;
   private final GUIManager guiManager;
   private final Order order;
   private final boolean hasStashItems;

   public OrderDetailGUI(GUIManager guiManager, Order order, boolean hasStashItems) {
      super(Bukkit.createInventory(null, 27, MessageHelper.get("gui.order-detail.title", "ᴏʀᴅᴇʀ ᴅᴇᴛᴀɪʟꜱ")));
      this.guiManager = guiManager;
      this.order = order;
      this.hasStashItems = hasStashItems;
      this.build();
   }

   private void build() {
      this.inventory.setItem(13, this.buildSummaryItem());
      boolean canCollect = this.order.getStatus() == OrderStatus.PENDING;
      if (canCollect) {
         String stashLine = this.hasStashItems
            ? MessageHelper.get("gui.your-orders.stash-has-items", "&e⬛ ꜱᴛᴀꜱʜ ʜᴀꜱ ɪᴛᴇᴍꜱ")
            : MessageHelper.get("gui.your-orders.stash-empty", "&7ꜱᴛᴀꜱʜ ɪꜱ ᴇᴍᴘᴛʏ");
         this.inventory
            .setItem(
               11,
               ItemUtils.createGuiItem(
                  Material.CHEST,
                  MessageHelper.get("gui.order-detail.collect.name", "&a&lᴄᴏʟʟᴇᴄᴛ"),
                  MessageHelper.getList("gui.order-detail.collect.lore", "stash_line", stashLine)
               )
            );
      }

      if (this.order.getStatus() == OrderStatus.ACTIVE) {
         this.inventory
            .setItem(
               15,
               ItemUtils.createGuiItem(
                  Material.RED_WOOL,
                  MessageHelper.get("gui.order-detail.cancel.name", "&c&lᴄᴀɴᴄᴇʟ ᴏʀᴅᴇʀ"),
                  MessageHelper.getList("gui.order-detail.cancel.lore", "funds", NumberFormatter.formatPrice(this.order.getRemainingFunds()))
               )
            );
      }

      this.inventory
         .setItem(
            22,
            ItemUtils.createGuiItem(
               Material.ARROW, MessageHelper.get("gui.order-detail.back.name", "&7« ʙᴀᴄᴋ"), MessageHelper.getList("gui.order-detail.back.lore")
            )
         );
      this.fillEmpty();
   }

   private ItemStack buildSummaryItem() {
      String statusColor = MessageHelper.statusColor(this.order.getStatus());
      String statusName = MessageHelper.statusName(this.order.getStatus());
      return ItemUtils.createGuiItem(
         this.order.getItemTemplate().getType(),
         MessageHelper.getNamed("gui.order-detail.summary.name", "&f&l{item}", "item", ItemUtils.describeOrderItem(this.order.getItemTemplate())),
         MessageHelper.getList(
            "gui.order-detail.summary.lore",
            "status_color",
            statusColor,
            "status",
            statusName,
            "fulfilled",
            NumberFormatter.format(this.order.getAmountFulfilled()),
            "requested",
            NumberFormatter.format(this.order.getAmountRequested()),
            "price",
            NumberFormatter.formatPrice(this.order.getPricePerItem()),
            "funds",
            NumberFormatter.formatPrice(this.order.getRemainingFunds()),
            "expiry",
            this.order.getFormattedExpiry()
         )
      );
   }

   @Override
   public void handleClick(Player player, int slot, ItemStack clicked, ClickType type) {
      if (slot == 11) {
         this.guiManager.openCollectStash(player, this.order.getOrderId());
      } else if (slot == 15 && this.order.getStatus() == OrderStatus.ACTIVE) {
         double refundAmount = this.order.getRemainingFunds();
         this.guiManager.getOrderManager().cancelOrder(player, this.order.getOrderId(), success -> {
            if (success) {
               MessageHelper.sendPrefixed(player, "order-cancelled", "&aᴏʀᴅᴇʀ ᴄᴀɴᴄᴇʟʟᴇᴅ. &f{0} &7ʀᴇꜰᴜɴᴅᴇᴅ.", NumberFormatter.formatPrice(refundAmount));
            } else {
               MessageHelper.send(player, "order-cancel-failed", "&cꜰᴀɪʟᴇᴅ ᴛᴏ ᴄᴀɴᴄᴇʟ ᴏʀᴅᴇʀ.");
            }

            this.guiManager.openYourOrders(player, 0);
         });
      } else if (slot == 22) {
         this.guiManager.openYourOrders(player, 0);
      }
   }
}
