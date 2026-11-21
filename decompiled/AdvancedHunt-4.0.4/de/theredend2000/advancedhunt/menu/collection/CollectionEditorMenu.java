package de.theredend2000.advancedhunt.menu.collection;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.PagedMenu;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CollectionEditorMenu extends PagedMenu {
   public CollectionEditorMenu(Player playerMenuUtility, Main plugin) {
      super(playerMenuUtility, plugin);
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.editor.title");
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
      this.addMenuBorder();
      List<String> collectionNames = this.plugin.getCollectionManager().getAllCollectionNames();
      if (collectionNames != null && !collectionNames.isEmpty()) {
         this.addPagedButtons(collectionNames.size());

         for (int i = 0; i < this.getMaxItemsPerPage(); i++) {
            this.index = this.getMaxItemsPerPage() * this.page + i;
            if (this.index >= collectionNames.size()) {
               break;
            }

            if (collectionNames.get(this.index) != null) {
               String collectionName = collectionNames.get(this.index);
               int finalI = i;
               this.plugin
                  .getCollectionManager()
                  .getCollectionByName(collectionName)
                  .ifPresent(
                     collection -> {
                        String status = collection.isEnabled()
                           ? this.plugin.getMessageManager().getMessage("gui.common.enabled")
                           : this.plugin.getMessageManager().getMessage("gui.common.disabled");
                        boolean isAvailable = this.plugin.getCollectionManager().isCollectionAvailable(collection);
                        String availabilityStatus = isAvailable
                           ? this.plugin.getMessageManager().getMessage("collection.available", false)
                           : this.plugin.getMessageManager().getMessage("collection.not_available", false);
                        String cron = collection.getProgressResetCron() != null
                           ? "§e" + collection.getProgressResetCron()
                           : this.plugin.getMessageManager().getMessage("gui.common.none");
                        int treasureCount = this.plugin.getTreasureManager().getTreasureCoresInCollection(collection.getId()).size();
                        this.addPagedItem(
                           finalI,
                           new ItemBuilder(XMaterial.CHEST)
                              .setDisplayName(
                                 this.plugin.getMessageManager().getMessage("gui.editor.collection_item.name", "%collection%", collection.getName())
                              )
                              .setLore(
                                 this.plugin
                                    .getMessageManager()
                                    .getMessageList(
                                       "gui.editor.collection_item.lore",
                                       "%status%",
                                       status,
                                       "%availability%",
                                       availabilityStatus,
                                       "%treasures%",
                                       String.valueOf(treasureCount),
                                       "%cron%",
                                       cron
                                    )
                                    .toArray(new String[0])
                              )
                              .build(),
                           e -> new CollectionSettingsMenu(this.playerMenuUtility, this.plugin, collection).setPreviousMenu(this).open()
                        );
                     }
                  );
            }
         }
      }

      this.addButton(
         52,
         new ItemBuilder(XMaterial.EMERALD)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.editor.create.name"))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.editor.create.lore").toArray(new String[0]))
            .build(),
         e -> this.plugin
            .getChatInputListener()
            .requestInput(
               this.playerMenuUtility,
               input -> this.plugin.getCollectionManager().createCollection(input).thenAccept(success -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                  if (success) {
                     this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("command.create.success", "%name%", input));
                  } else {
                     this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("command.create.failed"));
                  }

                  this.open();
               }))
            ),
         "advancedhunt.admin.collection.create"
      );
   }
}
