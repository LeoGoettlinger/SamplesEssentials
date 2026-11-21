package github.nighter.smartspawner.spawner.gui.sell;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.api.gui.GuiLayoutType;
import github.nighter.smartspawner.spawner.gui.layout.GuiButton;
import github.nighter.smartspawner.spawner.gui.layout.GuiLayout;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class SpawnerSellConfirmListener implements Listener {
   private final SmartSpawner plugin;

   public SpawnerSellConfirmListener(SmartSpawner plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onInventoryClick(InventoryClickEvent event) {
      InventoryHolder holder = event.getInventory().getHolder();
      if (holder instanceof SpawnerSellConfirmHolder) {
         event.setCancelled(true);
         if (event.getWhoClicked() instanceof Player player) {
            SpawnerSellConfirmHolder confirmHolder = (SpawnerSellConfirmHolder)holder;
            SpawnerData spawner = confirmHolder.getSpawnerData();
            if (spawner == null) {
               player.closeInventory();
            } else {
               int slot = event.getRawSlot();
               ItemStack clickedItem = event.getCurrentItem();
               if (clickedItem != null && !clickedItem.getType().isAir()) {
                  GuiLayout layout = confirmHolder.getLayout();
                  if (layout == null) {
                     player.closeInventory();
                  } else {
                     Optional<GuiButton> buttonOpt = layout.getButtonAtSlot(slot);
                     if (buttonOpt.isPresent()) {
                        GuiButton button = buttonOpt.get();
                        String clickType = this.getClickTypeString(event.getClick());
                        String action = button.getActionWithFallback(clickType);
                        if (action != null) {
                           if (!"none".equals(action)) {
                              if (this.plugin.getGuiButtonInteractionService().tryUse(player, GuiLayoutType.SELL_CONFIRM_GUI, button)) {
                                 switch (action) {
                                    case "cancel":
                                       this.plugin.getGuiButtonInteractionService().playNavigateSound(player, button, clickType);
                                       this.handleCancel(player, spawner, confirmHolder.getPreviousGui());
                                       break;
                                    case "confirm":
                                       this.handleConfirm(player, spawner, confirmHolder.getPreviousGui(), confirmHolder.isCollectExp(), button, clickType);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void handleCancel(Player player, SpawnerData spawner, SpawnerSellConfirmUI.PreviousGui previousGui) {
      this.reopenPreviousGui(player, spawner, previousGui);
   }

   private void handleConfirm(
      Player player, SpawnerData spawner, SpawnerSellConfirmUI.PreviousGui previousGui, boolean collectExp, GuiButton button, String clickType
   ) {
      if (spawner.isSelling()) {
         this.plugin.getGuiButtonInteractionService().playFailSound(player, button, clickType);
      } else {
         long expCollected = 0L;
         long expMending = 0L;
         if (collectExp) {
            long[] expData = this.plugin.getSpawnerMenuAction().collectExpSilently(player, spawner);
            expCollected = expData[0];
            expMending = expData[1];
         }

         Runnable onComplete = () -> {
            if (spawner.getVirtualInventory().getUsedSlots() == 0) {
               this.plugin.getGuiButtonInteractionService().playSuccessSound(player, button, clickType);
            } else {
               this.plugin.getGuiButtonInteractionService().playFailSound(player, button, clickType);
            }

            Scheduler.runEntityTask(player, () -> this.reopenPreviousGui(player, spawner, previousGui));
         };
         this.plugin.getSpawnerSellManager().sellAllItems(player, spawner, onComplete, expCollected, expMending);
      }
   }

   private String getClickTypeString(ClickType clickType) {
      return switch (clickType) {
         case LEFT -> "left_click";
         case RIGHT -> "right_click";
         case SHIFT_LEFT -> "shift_left_click";
         case SHIFT_RIGHT -> "shift_right_click";
         default -> "click";
      };
   }

   private void reopenPreviousGui(Player player, SpawnerData spawner, SpawnerSellConfirmUI.PreviousGui previousGui) {
      switch (previousGui) {
         case MAIN_MENU:
            this.plugin.getSpawnerMenuUI().openSpawnerMenu(player, spawner, true);
            break;
         case STORAGE:
            Inventory storageInventory = this.plugin.getSpawnerStorageUI().createStorageInventory(player, spawner, 1, -1);
            player.openInventory(storageInventory);
      }
   }
}
