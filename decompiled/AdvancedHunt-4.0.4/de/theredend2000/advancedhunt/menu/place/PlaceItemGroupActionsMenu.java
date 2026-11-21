package de.theredend2000.advancedhunt.menu.place;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.menu.common.ConfirmationMenu;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PlaceItemGroupActionsMenu extends Menu {
   private final String group;

   public PlaceItemGroupActionsMenu(Player player, Main plugin, String group) {
      super(player, plugin);
      this.group = group;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.place_items.group_actions.title", false, "%group%", this.group);
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
         11,
         new ItemBuilder(XMaterial.NAME_TAG)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.place_items.group_actions.rename.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList("gui.place_items.group_actions.rename.lore", false, "%current%", this.group)
                  .toArray(new String[0])
            )
            .build(),
         e -> this.handleRename()
      );
      this.addButton(
         15,
         new ItemBuilder(XMaterial.TNT)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.place_items.group_actions.delete.name", false))
            .setLore(
               this.plugin
                  .getMessageManager()
                  .getMessageList(
                     "gui.place_items.group_actions.delete.lore",
                     false,
                     "%count%",
                     String.valueOf(this.plugin.getPlaceItemManager().getItemsInGroup(this.group).size())
                  )
                  .toArray(new String[0])
            )
            .build(),
         e -> this.handleDelete()
      );
      this.fillBackground(this.FILLER_GLASS);
   }

   private void handleRename() {
      if (!this.playerMenuUtility.hasPermission("advancedhunt.admin.place_items")) {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
      } else {
         this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.place_items.prompt_rename_group", "%group%", this.group));
         this.plugin
            .getChatInputListener()
            .requestInput(
               this.playerMenuUtility,
               input -> {
                  String trimmed = input == null ? null : input.trim();
                  if (trimmed != null && !trimmed.isEmpty()) {
                     if (this.plugin.getPlaceItemManager().hasGroup(trimmed)) {
                        this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.place_items.duplicate_group"));
                        Bukkit.getScheduler().runTask(this.plugin, this::open);
                     } else {
                        this.plugin
                           .getPlaceItemManager()
                           .renameGroup(this.group, trimmed)
                           .whenComplete(
                              (ok, ex) -> Bukkit.getScheduler()
                                 .runTask(
                                    this.plugin,
                                    () -> {
                                       if (ex == null && Boolean.TRUE.equals(ok)) {
                                          this.playerMenuUtility
                                             .sendMessage(
                                                this.plugin
                                                   .getMessageManager()
                                                   .getMessage("feedback.place_items.group_renamed", "%old%", this.group, "%group%", trimmed)
                                             );
                                          this.openPreviousMenu();
                                       } else {
                                          this.playerMenuUtility
                                             .sendMessage(this.plugin.getMessageManager().getMessage("error.place_items.rename_group_failed"));
                                          this.open();
                                       }
                                    }
                                 )
                           );
                     }
                  } else {
                     this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.place_items.invalid_group"));
                     Bukkit.getScheduler().runTask(this.plugin, this::open);
                  }
               }
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
               this.plugin.getMessageManager().getMessage("gui.place_items.group_actions.delete.confirm_title", false, "%group%", this.group),
               confirmEvent -> this.plugin.getPlaceItemManager().deleteGroup(this.group).thenRun(() -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                  this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.place_items.group_deleted", "%group%", this.group));
                  this.openPreviousMenu();
               })),
               cancelEvent -> this.open()
            )
            .setPreviousMenu(this)
            .open();
      }
   }
}
