package de.theredend2000.advancedhunt.menu.place;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.PagedMenu;
import de.theredend2000.advancedhunt.model.PlaceItem;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import de.theredend2000.advancedhunt.util.ItemSerializer;
import de.theredend2000.advancedhunt.util.ItemsAdderAdapter;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PlaceItemListMenu extends PagedMenu {
   private final String group;

   public PlaceItemListMenu(Player player, Main plugin, String group) {
      super(player, plugin);
      this.group = group;
      this.maxItemsPerPage = 28;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.place_items.list.title", false, "%group%", this.group);
   }

   @Override
   public int getSlots() {
      return 54;
   }

   @Override
   public void handleMenu(InventoryClickEvent e) {
   }

   @Override
   public void setMenuItems() {
      this.index = 0;
      List<PlaceItem> items = this.plugin.getPlaceItemManager().getItemsInGroup(this.group);
      if (items.isEmpty()) {
         this.addMenuBorder();
         this.addStaticItem(
            22,
            this.getWarningIcon(
               this.plugin.getMessageManager().getMessage("gui.place_items.list.none.name", false),
               this.plugin.getMessageManager().getMessageList("gui.place_items.list.none.lore", false)
            )
         );
      } else {
         this.addPagedButtons(items.size());
         int startIndex = this.page * this.maxItemsPerPage;
         int endIndex = Math.min(startIndex + this.maxItemsPerPage, items.size());
         this.hasNextPage = endIndex < items.size();

         for (int i = startIndex; i < endIndex; i++) {
            PlaceItem preset = items.get(i);
            ItemStack icon = ItemSerializer.deserialize(preset.getItemData());
            if (icon == null
               || icon.getType() == XMaterial.AIR.get()
               || !icon.getType().isBlock() && !ItemsAdderAdapter.isCustomBlockItem(icon) && !ItemsAdderAdapter.isCustomFurniture(icon)) {
               icon = new ItemStack(XMaterial.BARRIER.get());
            }

            icon.setAmount(1);
            icon = new ItemBuilder(icon.clone())
               .setDisplayName(this.plugin.getMessageManager().getMessage("gui.place_items.list.preset.name", false, "%name%", preset.getName()))
               .setLore(
                  this.plugin
                     .getMessageManager()
                     .getMessageList(
                        "gui.place_items.list.preset.lore",
                        false,
                        "%action%",
                        this.plugin.getMessageManager().getMessage("gui.place_items.list.preset.action.give", false),
                        "%action2%",
                        this.plugin.getMessageManager().getMessage("gui.place_items.list.preset.action.manage", false)
                     )
                     .toArray(new String[0])
               )
               .build();
            this.addPagedItem(this.index++, icon, click -> {
               if (click.isRightClick()) {
                  new PlaceItemActionsMenu(this.playerMenuUtility, this.plugin, preset).setPreviousMenu(this).open();
               } else {
                  this.givePresetItem(preset);
               }
            });
         }

         this.addMenuBorder();
      }

      this.addButton(
         52,
         new ItemBuilder(XMaterial.EMERALD)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.place_items.create_in_group.name", false))
            .setLore(
               this.plugin.getMessageManager().getMessageList("gui.place_items.create_in_group.lore", false, "%group%", this.group).toArray(new String[0])
            )
            .build(),
         click -> {
            if (!this.playerMenuUtility.hasPermission("advancedhunt.admin.place_items")) {
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
            } else {
               new AddPlaceItemMenu(this.playerMenuUtility, this.plugin, this.group, this).open();
            }
         },
         "advancedhunt.admin.place_items"
      );
   }

   String generateUniqueNameForCreate(String group, ItemStack item) {
      return this.generateUniqueName(group, item);
   }

   private String generateUniqueName(String group, ItemStack item) {
      String base = "Preset";
      if (item != null && item.hasItemMeta() && item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
         String display = ChatColor.stripColor(item.getItemMeta().getDisplayName());
         if (display != null && !display.trim().isEmpty()) {
            base = display.trim();
         }
      } else if (item != null && item.getType() != null) {
         base = item.getType().name();
      }

      String candidate = base;

      for (int counter = 2; this.plugin.getPlaceItemManager().hasPresetNameInGroup(group, candidate); counter++) {
         candidate = base + " " + counter;
      }

      return candidate;
   }

   private void givePresetItem(PlaceItem preset) {
      ItemStack item = ItemSerializer.deserialize(preset.getItemData());
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
            .sendMessage(this.plugin.getMessageManager().getMessage("feedback.place_items.given", "%group%", preset.getGroup(), "%name%", preset.getName()));
      }
   }
}
