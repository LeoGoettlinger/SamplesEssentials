package com.donutorders.gui;

import com.donutorders.DonutOrders;
import com.donutorders.manager.GUIManager;
import com.donutorders.model.Order;
import com.donutorders.util.DeliveryItemUtils;
import com.donutorders.util.ItemUtils;
import com.donutorders.util.MessageHelper;
import com.donutorders.util.NumberFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class DeliverItemsGUI extends BaseGUI {
   public static final int INPUT_SLOTS = 45;
   private static final int SLOT_ORDER_INFO = 45;
   private static final int SLOT_CONFIRM = 49;
   private static final int SLOT_CANCEL = 53;
   private final GUIManager guiManager;
   private final Order order;
   private final AtomicReference<DeliverItemsGUI.Phase> phase = new AtomicReference<>(DeliverItemsGUI.Phase.OPEN);

   public DeliverItemsGUI(GUIManager guiManager, Order order) {
      super(Bukkit.createInventory(null, 54, MessageHelper.get("gui.deliver-items.title", "ᴅᴇʟɪᴠᴇʀ ɪᴛᴇᴍꜱ")));
      this.guiManager = guiManager;
      this.order = order;
      this.build();
   }

   private void build() {
      String itemName = ItemUtils.describeOrderItem(this.order.getItemTemplate());
      this.inventory
         .setItem(
            45,
            ItemUtils.createGuiItem(
               this.order.getItemTemplate().getType(),
               MessageHelper.getNamed("gui.deliver-items.order-info.name", "&b&l{item}", "item", itemName),
               MessageHelper.getList(
                  "gui.deliver-items.order-info.lore",
                  "buyer",
                  this.order.getBuyerName(),
                  "remaining",
                  NumberFormatter.format(this.order.getAmountRemaining()),
                  "price",
                  NumberFormatter.formatPrice(this.order.getPricePerItem()),
                  "item",
                  itemName
               )
            )
         );
      this.inventory
         .setItem(
            49,
            ItemUtils.createGuiItem(
               Material.LIME_WOOL, MessageHelper.get("gui.deliver-items.confirm.name", "&a&lᴄᴏɴꜰɪʀᴍ"), MessageHelper.getList("gui.deliver-items.confirm.lore")
            )
         );
      this.inventory
         .setItem(
            53,
            ItemUtils.createGuiItem(
               Material.RED_WOOL, MessageHelper.get("gui.deliver-items.cancel.name", "&c&lᴄᴀɴᴄᴇʟ"), MessageHelper.getList("gui.deliver-items.cancel.lore")
            )
         );

      for (int i = 46; i <= 52; i++) {
         if (i != 49 && i != 53) {
            this.inventory.setItem(i, filler());
         }
      }
   }

   @Override
   public void handleClick(Player player, int slot, ItemStack clicked, ClickType type) {
      if (slot >= 45) {
         if (slot == 49) {
            this.handleConfirm(player);
         } else if (slot == 53) {
            this.returnItems(player);
            this.guiManager.openPublicOrders(player, 0);
         }
      }
   }

   private void handleConfirm(Player player) {
      if (this.phase.compareAndSet(DeliverItemsGUI.Phase.OPEN, DeliverItemsGUI.Phase.CONFIRMED)) {
         ItemStack[] inputSlots = new ItemStack[45];

         for (int i = 0; i < 45; i++) {
            ItemStack item = this.inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
               if (item.isSimilar(this.order.getItemTemplate())) {
                  inputSlots[i] = item.clone();
               } else if (DeliveryItemUtils.isShulkerBox(item) && DeliveryItemUtils.shulkerContainsDeliverable(item, this.order.getItemTemplate())) {
                  inputSlots[i] = item.clone();
               }
            }
         }

         int validCount = DeliveryItemUtils.countAvailable(player, inputSlots, this.order.getItemTemplate());
         if (validCount == 0) {
            this.phase.set(DeliverItemsGUI.Phase.OPEN);
            MessageHelper.send(player, "delivery-no-items", "&cʏᴏᴜ ʜᴀᴠᴇ ɴᴏ ᴠᴀʟɪᴅ ɪᴛᴇᴍꜱ ᴛᴏ ᴅᴇʟɪᴠᴇʀ.");
         } else {
            this.returnExcessItems(player, inputSlots);
            int clearedCount = 0;

            for (int ix = 0; ix < 45; ix++) {
               if (this.inventory.getItem(ix) != null) {
                  this.inventory.setItem(ix, null);
                  clearedCount++;
               }
            }

            log("DeliverItemsGUI confirmed for " + player.getName() + " — cleared " + clearedCount + " GUI slots, snapshot item count: " + validCount);
            GUIManager.PlayerGUIState state = this.guiManager.getState(player.getUniqueId());
            if (state != null) {
               state.deliverySlots = inputSlots;
            }

            this.guiManager.openConfirmDelivery(player, this.order.getOrderId(), inputSlots);
         }
      }
   }

   public void returnItems(Player player) {
      this.returnItems(player, null);
   }

   public void returnItems(Player player, List<ItemStack> deathDrops) {
      if (!this.phase.compareAndSet(DeliverItemsGUI.Phase.OPEN, DeliverItemsGUI.Phase.RETURNED)) {
         log("DeliverItemsGUI.returnItems skipped for " + (player != null ? player.getName() : "unknown") + " — delivery already " + this.phase.get() + ".");
      } else {
         for (int i = 0; i < 45; i++) {
            ItemStack item = this.inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
               this.inventory.setItem(i, null);
               if (deathDrops != null) {
                  deathDrops.add(item.clone());
               } else {
                  ItemUtils.giveOrDrop(player, item);
               }
            }
         }
      }
   }

   private static void log(String message) {
      DonutOrders plugin = DonutOrders.getInstance();
      if (plugin != null) {
         plugin.getLogger().info("[DonutOrders] " + message);
      }
   }

   private void returnExcessItems(Player player, ItemStack[] snapshot) {
      int needed = this.order.getAmountRemaining();
      int counted = 0;

      for (int i = 0; i < snapshot.length; i++) {
         ItemStack item = snapshot[i];
         if (item != null && !DeliveryItemUtils.isShulkerBox(item)) {
            if (counted >= needed) {
               ItemUtils.giveOrDrop(player, item);
               this.inventory.setItem(i, null);
               snapshot[i] = null;
            } else {
               int take = Math.min(item.getAmount(), needed - counted);
               counted += take;
               if (take < item.getAmount()) {
                  ItemStack excess = item.clone();
                  excess.setAmount(item.getAmount() - take);
                  ItemUtils.giveOrDrop(player, excess);
                  ItemStack kept = item.clone();
                  kept.setAmount(take);
                  snapshot[i] = kept;
                  this.inventory.setItem(i, kept);
               }
            }
         }
      }
   }

   public Order getOrder() {
      return this.order;
   }

   private static enum Phase {
      OPEN,
      CONFIRMED,
      RETURNED;
   }
}
