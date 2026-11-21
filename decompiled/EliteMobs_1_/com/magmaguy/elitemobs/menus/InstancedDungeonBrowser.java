package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.commands.DungeonCommands;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.WorldInstancedDungeonPackage;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InstancedDungeonBrowser extends EliteMenu {
   private static final HashMap<Inventory, InstancedDungeonBrowser> inventories = new HashMap<>();
   private static final int BACK_SLOT = 17;
   private final List<Integer> difficultySlots = List.of(2, 4, 6, 0, 8, 1, 3, 5, 7);
   private final List<Integer> validSlots = new ArrayList<>(
      List.of(18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)
   );
   private final EMPackage emPackage;
   private final DungeonCommands.TeleportMenuSource teleportMenuSource;
   private List<DungeonInstance> instancesList;

   public static void shutdown() {
      inventories.clear();
   }

   public InstancedDungeonBrowser(Player player, String instancedDungeonName) {
      this(player, instancedDungeonName, DungeonCommands.TeleportMenuSource.NONE);
   }

   public InstancedDungeonBrowser(Player player, String instancedDungeonName, DungeonCommands.TeleportMenuSource teleportMenuSource) {
      EMPackage emPackage = EMPackage.getEmPackages().get(instancedDungeonName);
      this.emPackage = emPackage;
      this.teleportMenuSource = teleportMenuSource;
      if (!(emPackage instanceof WorldInstancedDungeonPackage)) {
         player.sendMessage(DungeonsConfig.getInvalidInstancedDungeonMessage());
      } else {
         Set<DungeonInstance> dungeonInstances = new HashSet<>();
         DungeonInstance.getDungeonInstances().forEach(instance -> {
            if (instance.getContentPackagesConfigFields().getFilename().equals(instancedDungeonName) && !instance.isDefunct()) {
               dungeonInstances.add(instance);
            }
         });
         this.instancesList = new ArrayList<>(dungeonInstances);
         int slots;
         if (dungeonInstances.isEmpty()) {
            slots = this.hasBackButton() ? 18 : 9;
         } else {
            slots = 54;
         }

         Inventory inventory = Bukkit.createInventory(player, slots);
         int difficultyCounter = 0;

         for (Map map : emPackage.getContentPackagesConfigFields().getDifficulties()) {
            List<String> description = new ArrayList<>();

            for (String string : DungeonsConfig.getInstancedDungeonDescription()) {
               description.add(string.replace("$dungeonName", emPackage.getContentPackagesConfigFields().getName()));
            }

            inventory.setItem(
               this.difficultySlots.get(difficultyCounter),
               ItemStackGenerator.generateItemStack(
                  Material.GREEN_STAINED_GLASS_PANE, DungeonsConfig.getInstancedDungeonTitle().replace("$difficulty", map.get("name") + ""), description
               )
            );
            difficultyCounter++;
         }

         for (int i = 0; i < dungeonInstances.size(); i++) {
            ItemStack itemStack = null;
            if (this.instancesList.get(i).getState().equals(MatchInstance.InstancedRegionState.WAITING)) {
               itemStack = this.playerItem(this.instancesList.get(i));
            } else if (DungeonsConfig.isAllowSpectatorsInInstancedContent()) {
               itemStack = this.spectatorItem(this.instancesList.get(i));
            }

            if (itemStack != null) {
               inventory.setItem(this.validSlots.get(i), itemStack);
            }
         }

         if (this.hasBackButton()) {
            inventory.setItem(17, PlayerStatusMenuConfig.getBackItem());
         }

         player.openInventory(inventory);
         inventories.put(inventory, this);
      }
   }

   private boolean hasBackButton() {
      return this.teleportMenuSource != DungeonCommands.TeleportMenuSource.NONE;
   }

   private ItemStack spectatorItem(DungeonInstance dungeonInstance) {
      List<String> players = new ArrayList<>();
      players.add(DungeonsConfig.getDungeonBrowserPlayersLabel());
      dungeonInstance.getPlayers()
         .forEach(player -> players.add(DungeonsConfig.getDungeonBrowserPlayerEntryFormat().replace("$playerName", player.getDisplayName())));
      return ItemStackGenerator.generateItemStack(
         Material.ORANGE_STAINED_GLASS_PANE,
         DungeonsConfig.getDungeonJoinAsSpectatorText().replace("$dungeonName", dungeonInstance.getContentPackagesConfigFields().getName()),
         players
      );
   }

   private ItemStack playerItem(DungeonInstance dungeonInstance) {
      List<String> players = new ArrayList<>();
      players.add(DungeonsConfig.getDungeonBrowserPlayersLabel());
      dungeonInstance.getPlayers()
         .forEach(player -> players.add(DungeonsConfig.getDungeonBrowserPlayerEntryFormat().replace("$playerName", player.getDisplayName())));
      return ItemStackGenerator.generateItemStack(
         Material.GREEN_STAINED_GLASS_PANE,
         DungeonsConfig.getDungeonJoinAsPlayerText().replace("$dungeonName", dungeonInstance.getContentPackagesConfigFields().getName()),
         players
      );
   }

   @Generated
   public EMPackage getEmPackage() {
      return this.emPackage;
   }

   public static class InstancedDungeonBrowserEvents implements Listener {
      @EventHandler
      public void onInventoryInteract(InventoryClickEvent event) {
         if (EliteMenu.isEliteMenu(event, InstancedDungeonBrowser.inventories.keySet())) {
            event.setCancelled(true);
            if (EliteMenu.isTopMenu(event)) {
               if (event.getCurrentItem() != null && !event.getCurrentItem().getType().equals(Material.AIR)) {
                  InstancedDungeonBrowser instancedDungeonBrowser = InstancedDungeonBrowser.inventories.get(event.getInventory());
                  if (instancedDungeonBrowser.hasBackButton() && event.getSlot() == 17) {
                     event.getWhoClicked().closeInventory();
                     DungeonCommands.reopenTeleportBrowser((Player)event.getWhoClicked(), instancedDungeonBrowser.teleportMenuSource);
                  } else {
                     event.getWhoClicked().closeInventory();
                     if (instancedDungeonBrowser.difficultySlots.contains(event.getSlot())) {
                        DungeonInstance.setupInstancedDungeon(
                           (Player)event.getWhoClicked(),
                           instancedDungeonBrowser.getEmPackage().getContentPackagesConfigFields().getFilename(),
                           (String)instancedDungeonBrowser.getEmPackage()
                              .getContentPackagesConfigFields()
                              .getDifficulties()
                              .get(instancedDungeonBrowser.difficultySlots.indexOf(event.getSlot()))
                              .get("name")
                        );
                     } else {
                        DungeonInstance dungeonInstance = instancedDungeonBrowser.instancesList
                           .get(instancedDungeonBrowser.validSlots.indexOf(event.getSlot()));
                        if (dungeonInstance.isDefunct()) {
                           event.getWhoClicked().sendMessage(DungeonsConfig.getMatchAlreadyEndedMessage());
                        } else {
                           switch (dungeonInstance.getState()) {
                              case ONGOING:
                              case STARTING:
                                 if (DungeonsConfig.isAllowSpectatorsInInstancedContent()) {
                                    dungeonInstance.addSpectator((Player)event.getWhoClicked(), false);
                                 }
                                 break;
                              case WAITING:
                                 dungeonInstance.requestPartyEntry((Player)event.getWhoClicked());
                                 break;
                              case COMPLETED:
                                 event.getWhoClicked().sendMessage(DungeonsConfig.getMatchAlreadyEndedMessage());
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      @EventHandler
      public void onClose(InventoryCloseEvent event) {
         InstancedDungeonBrowser.inventories.remove(event.getInventory());
      }
   }
}
