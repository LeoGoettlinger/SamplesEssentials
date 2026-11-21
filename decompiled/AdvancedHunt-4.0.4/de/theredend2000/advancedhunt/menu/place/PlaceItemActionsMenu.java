package de.theredend2000.advancedhunt.menu.place;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.menu.common.ConfirmationMenu;
import de.theredend2000.advancedhunt.model.PlaceItem;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import de.theredend2000.advancedhunt.util.ItemSerializer;
import de.theredend2000.advancedhunt.util.ItemsAdderAdapter;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PlaceItemActionsMenu extends Menu {
   private final PlaceItem preset;

   public PlaceItemActionsMenu(Player player, Main plugin, PlaceItem preset) {
      super(player, plugin);
      this.preset = preset;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.place_items.actions.title", false, "%name%", this.preset.getName());
   }

   @Override
   public int getSlots() {
      return 27;
   }

   @Override
   public void handleMenu(InventoryClickEvent e) {
   }

   @Override
   public void setMenuItems() {
      this.fillBorders(this.FILLER_GLASS);
      this.addButton(
         12,
         new ItemBuilder(XMaterial.CHEST)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.place_items.actions.give.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.place_items.actions.give.lore", false).toArray(new String[0]))
            .build(),
         e -> {
            this.give();
            this.openPreviousMenu();
         }
      );
      this.addButton(
         14,
         new ItemBuilder(XMaterial.TNT)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.place_items.actions.delete.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.place_items.actions.delete.lore", false).toArray(new String[0]))
            .build(),
         e -> this.handleDelete()
      );
      this.fillBackground(this.FILLER_GLASS);
   }

   private void give() {
      ItemStack item = ItemSerializer.deserialize(this.preset.getItemData());
      if (item == null || item.getType() == XMaterial.AIR.get()) {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.place_items.deserialize_failed"));
      } else if (!item.getType().isBlock() && !ItemsAdderAdapter.isCustomBlockItem(item) && !ItemsAdderAdapter.isCustomFurniture(item)) {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.place_items.not_a_block"));
      } else {
         ItemStack toGive = item.clone();
         toGive.setAmount(1);
         Map<Integer, ItemStack> leftovers = this.playerMenuUtility.getInventory().addItem(new ItemStack[]{toGive});

         for (ItemStack leftover : leftovers.values()) {
            this.playerMenuUtility.getWorld().dropItemNaturally(this.playerMenuUtility.getLocation(), leftover);
         }

         this.playerMenuUtility
            .sendMessage(
               this.plugin.getMessageManager().getMessage("feedback.place_items.given", "%group%", this.preset.getGroup(), "%name%", this.preset.getName())
            );
      }
   }

   private void handleDelete() {
      if (!this.playerMenuUtility.hasPermission("advancedhunt.admin.place_items")) {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
      } else {
         new ConfirmationMenu(
               this.playerMenuUtility,
               this.plugin,
               this.plugin.getMessageManager().getMessage("gui.place_items.actions.delete.confirm_title", false, "%name%", this.preset.getName()),
               confirmEvent -> this.plugin
                  .getPlaceItemManager()
                  .deleteItem(this.preset.getId())
                  .thenRun(
                     () -> Bukkit.getScheduler()
                        .runTask(
                           this.plugin,
                           () -> {
                              this.playerMenuUtility
                                 .sendMessage(
                                    this.plugin
                                       .getMessageManager()
                                       .getMessage("feedback.place_items.deleted", "%group%", this.preset.getGroup(), "%name%", this.preset.getName())
                                 );
                              this.openPreviousMenu();
                           }
                        )
                  ),
               cancelEvent -> this.open()
            )
            .setPreviousMenu(this)
            .open();
      }
   }
}
