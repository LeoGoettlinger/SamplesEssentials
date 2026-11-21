package github.nighter.smartspawner.commands.list.gui.list;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.list.ListSubCommand;
import github.nighter.smartspawner.commands.list.gui.list.enums.FilterOption;
import github.nighter.smartspawner.commands.list.gui.list.enums.SortOption;
import github.nighter.smartspawner.commands.list.gui.worldselection.WorldSelectionHolder;
import github.nighter.smartspawner.language.MessageService;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class SpawnerListGUI implements Listener {
   private final MessageService messageService;
   private final SpawnerManager spawnerManager;
   private final ListSubCommand listSubCommand;
   private final NamespacedKey worldNameKey;
   private static final Set<Material> SPAWNER_MATERIALS = EnumSet.of(
      Material.PLAYER_HEAD,
      Material.SPAWNER,
      Material.ZOMBIE_HEAD,
      Material.SKELETON_SKULL,
      Material.WITHER_SKELETON_SKULL,
      Material.CREEPER_HEAD,
      Material.PIGLIN_HEAD
   );
   private static final String patternString = "#([A-Za-z0-9]+)";

   public SpawnerListGUI(SmartSpawner plugin) {
      this.messageService = plugin.getMessageService();
      this.spawnerManager = plugin.getSpawnerManager();
      this.listSubCommand = plugin.getListSubCommand();
      this.worldNameKey = new NamespacedKey(plugin, "world_name");
   }

   @EventHandler
   public void onWorldSelectionClick(InventoryClickEvent event) {
      if (event.getInventory().getHolder(false) instanceof WorldSelectionHolder holder) {
         if (event.getWhoClicked() instanceof Player player) {
            if (!player.hasPermission("smartspawner.command.list")) {
               this.messageService.sendMessage(player, "no_permission");
            } else {
               event.setCancelled(true);
               ItemStack clickedItem = event.getCurrentItem();
               if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                  String targetServer = holder.getTargetServer();
                  boolean isRemote = holder.isRemoteServer();
                  if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
                     this.listSubCommand.openServerSelectionGUI(player);
                  } else {
                     String worldName = (String)clickedItem.getItemMeta().getPersistentDataContainer().get(this.worldNameKey, PersistentDataType.STRING);
                     if (worldName != null) {
                        if (isRemote) {
                           this.listSubCommand.openSpawnerListGUIForServer(player, targetServer, worldName, 1);
                        } else {
                           this.listSubCommand.openSpawnerListGUI(player, worldName, 1);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onSpawnerListClick(InventoryClickEvent event) {
      if (event.getInventory().getHolder(false) instanceof SpawnerListHolder holder) {
         if (event.getWhoClicked() instanceof Player player) {
            if (!player.hasPermission("smartspawner.list")) {
               this.messageService.sendMessage(player, "no_permission");
            } else {
               event.setCancelled(true);
               if (event.getCurrentItem() != null) {
                  String worldName = holder.getWorldName();
                  int currentPage = holder.getCurrentPage();
                  int totalPages = holder.getTotalPages();
                  FilterOption currentFilter = holder.getFilterOption();
                  SortOption currentSort = holder.getSortType();
                  String targetServer = holder.getTargetServer();
                  boolean isRemote = holder.isRemoteServer();
                  if (event.getSlot() == 48) {
                     FilterOption nextFilter = currentFilter.getNextOption();
                     if (!isRemote) {
                        this.listSubCommand.saveUserPreference(player, worldName, nextFilter, currentSort);
                     }

                     if (isRemote) {
                        this.listSubCommand.openSpawnerListGUIForServer(player, targetServer, worldName, 1, nextFilter, currentSort);
                     } else {
                        this.listSubCommand.openSpawnerListGUI(player, worldName, 1, nextFilter, currentSort);
                     }
                  } else if (event.getSlot() == 50) {
                     SortOption nextSort = currentSort.getNextOption();
                     if (!isRemote) {
                        this.listSubCommand.saveUserPreference(player, worldName, currentFilter, nextSort);
                     }

                     if (isRemote) {
                        this.listSubCommand.openSpawnerListGUIForServer(player, targetServer, worldName, 1, currentFilter, nextSort);
                     } else {
                        this.listSubCommand.openSpawnerListGUI(player, worldName, 1, currentFilter, nextSort);
                     }
                  } else if (event.getSlot() == 45 && currentPage > 1) {
                     if (isRemote) {
                        this.listSubCommand.openSpawnerListGUIForServer(player, targetServer, worldName, currentPage - 1);
                     } else {
                        this.listSubCommand.openSpawnerListGUI(player, worldName, currentPage - 1, currentFilter, currentSort);
                     }
                  } else if (event.getSlot() == 49) {
                     if (!isRemote) {
                        this.listSubCommand.saveUserPreference(player, worldName, currentFilter, currentSort);
                     }

                     if (isRemote) {
                        this.listSubCommand.openWorldSelectionGUIForServer(player, targetServer);
                     } else {
                        this.listSubCommand.openWorldSelectionGUI(player);
                     }
                  } else if (event.getSlot() == 53 && currentPage < totalPages) {
                     if (isRemote) {
                        this.listSubCommand.openSpawnerListGUIForServer(player, targetServer, worldName, currentPage + 1);
                     } else {
                        this.listSubCommand.openSpawnerListGUI(player, worldName, currentPage + 1, currentFilter, currentSort);
                     }
                  } else {
                     if (this.isSpawnerItemSlot(event.getSlot()) && this.isSpawnerItem(event.getCurrentItem())) {
                        this.handleSpawnerItemClick(player, event.getCurrentItem(), holder);
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      if (event.getInventory().getHolder(false) instanceof SpawnerListHolder holder) {
         if (event.getPlayer() instanceof Player player) {
            String var8 = holder.getWorldName();
            FilterOption currentFilter = holder.getFilterOption();
            SortOption currentSort = holder.getSortType();
            this.listSubCommand.saveUserPreference(player, var8, currentFilter, currentSort);
         }
      }
   }

   private boolean isSpawnerItemSlot(int slot) {
      return slot < 45;
   }

   private boolean isSpawnerItem(ItemStack item) {
      return item != null && SPAWNER_MATERIALS.contains(item.getType()) && item.hasItemMeta() && item.getItemMeta().hasDisplayName();
   }

   private void handleSpawnerItemClick(Player player, ItemStack item, SpawnerListHolder holder) {
      if (!holder.isRemoteServer()) {
         String displayName = item.getItemMeta().getDisplayName();
         Pattern pattern = Pattern.compile("#([A-Za-z0-9]+)");
         Matcher matcher = pattern.matcher(displayName);
         if (matcher.find()) {
            String spawnerId = matcher.group(1);
            SpawnerData spawner = this.spawnerManager.getSpawnerById(spawnerId);
            if (spawner != null) {
               this.listSubCommand.openSpawnerManagementGUI(player, spawnerId, holder.getWorldName(), holder.getCurrentPage());
            } else {
               this.messageService.sendMessage(player, "list.teleport_failed");
            }
         }
      }
   }
}
