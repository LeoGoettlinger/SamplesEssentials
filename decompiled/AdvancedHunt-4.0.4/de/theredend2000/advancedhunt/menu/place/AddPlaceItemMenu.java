package de.theredend2000.advancedhunt.menu.place;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.common.SingleItemInputMenu;
import de.theredend2000.advancedhunt.util.ItemSerializer;
import de.theredend2000.advancedhunt.util.ItemsAdderAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AddPlaceItemMenu extends SingleItemInputMenu {
   private final String group;
   private final PlaceItemListMenu parentMenu;

   public AddPlaceItemMenu(Player player, Main plugin, String group, PlaceItemListMenu parentMenu) {
      super(player, plugin, parentMenu, false);
      this.group = group;
      this.parentMenu = parentMenu;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.place_items.add.title", false, "%group%", this.group);
   }

   @Override
   protected String getGuiKeyBase() {
      return "gui.place_items.add";
   }

   @Override
   protected String getNoItemErrorKey() {
      return "error.place_items.no_item";
   }

   @Override
   protected boolean isValidItem(ItemStack item) {
      return item != null && item.getType() != XMaterial.AIR.get()
         ? item.getType().isBlock() || ItemsAdderAdapter.isCustomBlockItem(item) || ItemsAdderAdapter.isCustomFurniture(item)
         : false;
   }

   @Override
   protected String getInvalidItemErrorKey() {
      return "error.place_items.not_a_block";
   }

   @Override
   protected void onConfirm(ItemStack itemSnapshot) {
      if (!this.playerMenuUtility.hasPermission("advancedhunt.admin.place_items")) {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
      } else {
         ItemStack blockSnapshot = itemSnapshot.clone();
         blockSnapshot.setAmount(1);
         String itemData = ItemSerializer.serialize(blockSnapshot);
         if (itemData != null && !itemData.trim().isEmpty()) {
            String name = this.parentMenu.generateUniqueNameForCreate(this.group, blockSnapshot);
            this.plugin
               .getPlaceItemManager()
               .createItem(this.group, name, itemData)
               .whenComplete(
                  (ok, ex) -> Bukkit.getScheduler()
                     .runTask(
                        this.plugin,
                        () -> {
                           if (ex == null && Boolean.TRUE.equals(ok)) {
                              this.playerMenuUtility
                                 .sendMessage(this.plugin.getMessageManager().getMessage("feedback.place_items.created", "%group%", this.group, "%name%", name));
                              this.parentMenu.open();
                           } else {
                              this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.place_items.create_failed"));
                              this.parentMenu.open();
                           }
                        }
                     )
               );
         } else {
            this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.place_items.serialize_failed"));
         }
      }
   }
}
