package de.theredend2000.advancedhunt.menu.collection;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.managers.TreasureDeletionType;
import de.theredend2000.advancedhunt.menu.Menu;
import de.theredend2000.advancedhunt.menu.common.ConfirmationMenu;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class DeleteCollectionHandlingMenu extends Menu {
   private final Collection collection;
   private boolean processing;
   private boolean isCommandExecution;

   public DeleteCollectionHandlingMenu(Player playerMenuUtility, Main plugin, Collection collection, boolean isCommandExecution) {
      super(playerMenuUtility, plugin);
      this.collection = collection;
      this.isCommandExecution = isCommandExecution;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.settings.delete_handling.title", false);
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
      this.fillBorders(super.FILLER_GLASS);
      this.addHandlingButton(
         11,
         XMaterial.CHEST,
         "gui.settings.delete_handling.keep.name",
         "gui.settings.delete_handling.keep.lore",
         "gui.settings.delete_handling.keep.confirm_title",
         TreasureDeletionType.KEEP_ALL
      );
      this.addHandlingButton(
         13,
         XMaterial.PLAYER_HEAD,
         "gui.settings.delete_handling.remove_heads.name",
         "gui.settings.delete_handling.remove_heads.lore",
         "gui.settings.delete_handling.remove_heads.confirm_title",
         TreasureDeletionType.REMOVE_HEADS
      );
      this.addHandlingButton(
         15,
         XMaterial.TNT,
         "gui.settings.delete_handling.remove_blocks.name",
         "gui.settings.delete_handling.remove_blocks.lore",
         "gui.settings.delete_handling.remove_blocks.confirm_title",
         TreasureDeletionType.REMOVE_BLOCKS_AND_FURNITURE
      );
   }

   private void addHandlingButton(int slot, XMaterial material, String nameKey, String loreKey, String confirmTitleKey, TreasureDeletionType handling) {
      ItemStack item = new ItemBuilder(material)
         .setDisplayName(this.plugin.getMessageManager().getMessage(nameKey, false))
         .setLore(this.plugin.getMessageManager().getMessageList(loreKey, false).toArray(new String[0]))
         .build();
      this.addButton(
         slot,
         item,
         e -> {
            if (!this.processing) {
               if (this.collection != null) {
                  new ConfirmationMenu(
                        this.playerMenuUtility,
                        this.plugin,
                        this.plugin.getMessageManager().getMessage(confirmTitleKey, false, "%collection%", this.collection.getName()),
                        confirmEvent -> {
                           this.playerMenuUtility.closeInventory();
                           this.runDelete(confirmEvent, handling);
                        },
                        cancelEvent -> this.openPreviousMenu()
                     )
                     .setPreviousMenu(this)
                     .open();
               }
            }
         }
      );
   }

   private void runDelete(InventoryClickEvent e, TreasureDeletionType handling) {
      if (!this.processing) {
         this.processing = true;
         this.plugin
            .getCollectionManager()
            .deleteCollection(this.collection.getId(), handling)
            .whenComplete(
               (v, ex) -> Bukkit.getScheduler()
                  .runTask(
                     this.plugin,
                     () -> {
                        this.processing = false;
                        if (ex != null) {
                           this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.generic"));
                           this.openPreviousMenu();
                        } else {
                           this.playerMenuUtility
                              .sendMessage(
                                 this.plugin.getMessageManager().getMessage("feedback.settings.delete.success", "%collection%", this.collection.getName())
                              );
                           if (!this.isCommandExecution) {
                              new CollectionEditorMenu(this.playerMenuUtility, this.plugin).open();
                           } else {
                              this.playerMenuUtility.closeInventory();
                           }
                        }
                     }
                  )
            );
      }
   }
}
