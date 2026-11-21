package com.lishid.openinv.listener;

import com.google.errorprone.annotations.Keep;
import com.lishid.openinv.internal.ViewOnly;
import com.lishid.openinv.util.InternalAccessor;
import com.lishid.openinv.util.Permissions;
import com.lishid.openinv.util.lang.LanguageManager;
import com.lishid.openinv.util.setting.PlayerToggles;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class ContainerListener implements Listener {
   @NotNull
   private final InternalAccessor accessor;
   @NotNull
   private final LanguageManager lang;

   public ContainerListener(@NotNull InternalAccessor accessor, @NotNull LanguageManager lang) {
      this.accessor = accessor;
      this.lang = lang;
   }

   @Keep
   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   private void onPlayerInteract(@NotNull PlayerInteractEvent event) {
      if (PlayerInteractEvent.class.equals(event.getClass())) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && !event.getPlayer().isSneaking()
            && event.useInteractedBlock() != Result.DENY
            && event.getClickedBlock() != null
            && this.accessor.getAnySilentContainer().isAnySilentContainer(event.getClickedBlock())) {
            Player player = event.getPlayer();
            boolean any = PlayerToggles.any().is(player);
            boolean needsAny = this.accessor.getAnySilentContainer().isAnyContainerNeeded(event.getClickedBlock());
            if (any || !needsAny) {
               boolean silent = PlayerToggles.silent().is(player);
               if (any || silent) {
                  if (this.accessor.getAnySilentContainer().activateContainer(player, silent, event.getClickedBlock())) {
                     if (silent && needsAny) {
                        this.lang.sendSystemMessage(player, "messages.info.containerBlockedSilent");
                     } else if (needsAny) {
                        this.lang.sendSystemMessage(player, "messages.info.containerBlocked");
                     } else if (silent) {
                        this.lang.sendSystemMessage(player, "messages.info.containerSilent");
                     }
                  }

                  event.setCancelled(true);
               }
            }
         }
      }
   }

   @Keep
   @EventHandler
   private void onInventoryClose(@NotNull InventoryCloseEvent event) {
      if (event.getPlayer() instanceof Player player) {
         if (PlayerToggles.silent().is(player) && this.accessor.getAnySilentContainer().isAnySilentContainer(event.getInventory())) {
            this.accessor.getAnySilentContainer().deactivateContainer(player);
         }
      }
   }

   @Keep
   @EventHandler(
      priority = EventPriority.LOWEST
   )
   private void onInventoryClick(@NotNull InventoryClickEvent event) {
      this.handleInventoryInteract(event);
   }

   @Keep
   @EventHandler(
      priority = EventPriority.LOWEST
   )
   private void onInventoryDrag(@NotNull InventoryDragEvent event) {
      this.handleInventoryInteract(event);
   }

   private void handleInventoryInteract(@NotNull InventoryInteractEvent event) {
      HumanEntity entity = event.getWhoClicked();
      if (entity.getGameMode() == GameMode.SPECTATOR && Permissions.SPECTATE_CLICK.hasPermission(entity)) {
         event.setCancelled(false);
      }

      if (!event.isCancelled()) {
         Inventory inventory = event.getView().getTopInventory();
         if (inventory instanceof ViewOnly) {
            event.setCancelled(true);
         }
      }
   }
}
