package github.nighter.smartspawner.commands.list.gui.management;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.list.ListSubCommand;
import github.nighter.smartspawner.commands.list.gui.list.enums.FilterOption;
import github.nighter.smartspawner.commands.list.gui.list.enums.SortOption;
import github.nighter.smartspawner.language.MessageService;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.gui.main.SpawnerMenuUI;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class SpawnerManagementHandler implements Listener {
   private final SmartSpawner plugin;
   private final MessageService messageService;
   private final SpawnerManager spawnerManager;
   private final ListSubCommand listSubCommand;
   private final SpawnerMenuUI spawnerMenuUI;

   public SpawnerManagementHandler(SmartSpawner plugin, ListSubCommand listSubCommand) {
      this.plugin = plugin;
      this.messageService = plugin.getMessageService();
      this.spawnerManager = plugin.getSpawnerManager();
      this.listSubCommand = listSubCommand;
      this.spawnerMenuUI = plugin.getSpawnerMenuUI();
   }

   @EventHandler
   public void onSpawnerManagementClick(InventoryClickEvent event) {
      if (event.getInventory().getHolder(false) instanceof SpawnerManagementHolder holder) {
         if (event.getWhoClicked() instanceof Player player) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
               String spawnerId = holder.getSpawnerId();
               String worldName = holder.getWorldName();
               int listPage = holder.getListPage();
               int slot = event.getSlot();
               if (slot == 26) {
                  this.handleBack(player, worldName, listPage);
               } else {
                  SpawnerData spawner = this.spawnerManager.getSpawnerById(spawnerId);
                  if (spawner == null) {
                     this.messageService.sendMessage(player, "list.teleport_failed");
                  } else if (this.plugin.getSpawnerRemovalService().isRemovalPending(spawner)) {
                     this.messageService.sendMessage(player, "action_in_progress");
                  } else {
                     switch (slot) {
                        case 10:
                           this.handleTeleport(player, spawner);
                           break;
                        case 12:
                           this.handleOpenSpawnerGUI(player, spawner);
                           break;
                        case 16:
                           this.handleRemoveSpawner(player, spawner, worldName, listPage);
                     }
                  }
               }
            }
         }
      }
   }

   private void handleTeleport(Player player, SpawnerData spawner) {
      Location loc = spawner.getSpawnerLocation().clone().add(0.5, 1.0, 0.5);
      player.teleportAsync(loc);
      this.messageService.sendMessage(player, "list.teleported_to_spawner");
      player.closeInventory();
   }

   private void handleOpenSpawnerGUI(Player player, SpawnerData spawner) {
      if (this.plugin.getGuiLayoutConfig().isSkipMainGui()) {
         Inventory storageInventory = this.plugin.getSpawnerStorageUI().createStorageInventory(player, spawner, 1, -1);
         player.openInventory(storageInventory);
         this.plugin.getGuiButtonInteractionService().playOpenSound(player);
      } else {
         this.spawnerMenuUI.openSpawnerMenu(player, spawner, false);
         player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
      }
   }

   private void handleRemoveSpawner(Player player, SpawnerData spawner, String worldName, int listPage) {
      this.plugin.getSpawnerRemovalService().removeSpawner(spawner).whenComplete((removed, error) -> Scheduler.runEntityTask(player, () -> {
         if (player.isOnline()) {
            if (error == null && Boolean.TRUE.equals(removed)) {
               Map<String, String> placeholders = new HashMap<>();
               placeholders.put("id", spawner.getSpawnerId());
               this.messageService.sendMessage(player, "list.spawner_removed", placeholders);
               player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
               this.handleBack(player, worldName, listPage);
            } else {
               this.messageService.sendMessage(player, "action_in_progress");
            }
         }
      }));
   }

   private void handleBack(Player player, String worldName, int listPage) {
      FilterOption filter = FilterOption.ALL;
      SortOption sort = SortOption.DEFAULT;

      try {
         filter = this.listSubCommand.getUserFilter(player, worldName);
         sort = this.listSubCommand.getUserSort(player, worldName);
      } catch (Exception var7) {
      }

      this.listSubCommand.openSpawnerListGUI(player, worldName, listPage, filter, sort);
      player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
   }
}
