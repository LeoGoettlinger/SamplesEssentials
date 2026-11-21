package de.theredend2000.advancedhunt.menu.place;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XMaterial;
import de.theredend2000.advancedhunt.menu.PagedMenu;
import de.theredend2000.advancedhunt.util.ItemBuilder;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PlaceItemGroupMenu extends PagedMenu {
   public PlaceItemGroupMenu(Player player, Main plugin) {
      super(player, plugin);
      this.maxItemsPerPage = 28;
   }

   @Override
   public String getMenuName() {
      return this.plugin.getMessageManager().getMessage("gui.place_items.groups.title", false);
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
      this.addMenuBorder();
      List<String> groups = this.plugin.getPlaceItemManager().getGroups();
      if (groups.isEmpty()) {
         this.addStaticItem(
            22,
            this.getWarningIcon(
               this.plugin.getMessageManager().getMessage("gui.place_items.groups.none.name", false),
               this.plugin.getMessageManager().getMessageList("gui.place_items.groups.none.lore", false)
            )
         );
      } else {
         this.addPagedButtons(groups.size());
         int startIndex = this.page * this.maxItemsPerPage;
         int endIndex = Math.min(startIndex + this.maxItemsPerPage, groups.size());
         this.hasNextPage = endIndex < groups.size();

         for (int i = startIndex; i < endIndex; i++) {
            String group = groups.get(i);
            ItemStack icon = new ItemBuilder(XMaterial.CHEST)
               .setDisplayName(this.plugin.getMessageManager().getMessage("gui.place_items.groups.group.name", false, "%group%", group))
               .setLore(
                  this.plugin
                     .getMessageManager()
                     .getMessageList(
                        "gui.place_items.groups.group.lore",
                        false,
                        "%count%",
                        String.valueOf(this.plugin.getPlaceItemManager().getItemsInGroup(group).size()),
                        "%action%",
                        this.plugin.getMessageManager().getMessage("gui.place_items.groups.group.action.open", false),
                        "%action2%",
                        this.plugin.getMessageManager().getMessage("gui.place_items.groups.group.action.manage", false)
                     )
               )
               .build();
            this.addPagedItem(this.index++, icon, click -> {
               if (click.isRightClick()) {
                  new PlaceItemGroupActionsMenu(this.playerMenuUtility, this.plugin, group).setPreviousMenu(this).open();
               } else {
                  new PlaceItemListMenu(this.playerMenuUtility, this.plugin, group).setPreviousMenu(this).open();
               }
            });
         }
      }

      this.addButton(
         52,
         new ItemBuilder(XMaterial.EMERALD)
            .setDisplayName(this.plugin.getMessageManager().getMessage("gui.place_items.create.name", false))
            .setLore(this.plugin.getMessageManager().getMessageList("gui.place_items.create.lore", false).toArray(new String[0]))
            .build(),
         click -> {
            if (!this.playerMenuUtility.hasPermission("advancedhunt.admin.place_items")) {
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.no_permission"));
            } else {
               this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("feedback.place_items.prompt_group"));
               this.plugin
                  .getChatInputListener()
                  .requestInput(
                     this.playerMenuUtility,
                     groupInput -> {
                        String groupx = groupInput == null ? null : groupInput.trim();
                        if (groupx != null && !groupx.isEmpty()) {
                           this.plugin
                              .getPlaceItemManager()
                              .createGroup(groupx)
                              .whenComplete(
                                 (ok, ex) -> Bukkit.getScheduler()
                                    .runTask(
                                       this.plugin,
                                       () -> {
                                          if (ex == null && Boolean.TRUE.equals(ok)) {
                                             this.playerMenuUtility
                                                .sendMessage(
                                                   this.plugin.getMessageManager().getMessage("feedback.place_items.group_created", "%group%", groupx)
                                                );
                                             this.open();
                                          } else {
                                             if (this.plugin.getPlaceItemManager().hasGroup(groupx)) {
                                                this.playerMenuUtility
                                                   .sendMessage(this.plugin.getMessageManager().getMessage("error.place_items.duplicate_group"));
                                             } else {
                                                this.playerMenuUtility
                                                   .sendMessage(this.plugin.getMessageManager().getMessage("error.place_items.create_group_failed"));
                                             }

                                             this.open();
                                          }
                                       }
                                    )
                              );
                        } else {
                           this.playerMenuUtility.sendMessage(this.plugin.getMessageManager().getMessage("error.place_items.invalid_group"));
                           Bukkit.getScheduler().runTask(this.plugin, this::open);
                        }
                     }
                  );
            }
         },
         "advancedhunt.admin.place_items"
      );
   }
}
