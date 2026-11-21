package com.magmaguy.elitemobs.parties;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.thirdparty.BedrockChecker;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public final class PartyInventoryMenu implements Listener {
   private static final Map<Inventory, PartyInventoryMenu.MenuState> menus = new HashMap<>();
   private static final int PAGE_SIZE = 45;

   public static boolean usesInventoryFallback(Player player) {
      return BedrockChecker.isBedrock(player) || DefaultConfig.isOnlyUseBedrockMenus() || !PlayerData.getUseBookMenus(player.getUniqueId());
   }

   public static void open(Player player) {
      if (canUse(player)) {
         if (!PartyDungeonReadyCheckManager.openPendingInventory(player) && !PartyManager.openPendingInviteInventory(player)) {
            Inventory inventory = Bukkit.createInventory(player, 27, color(PartyConfig.getInventoryControlsTitle()));
            inventory.setItem(11, item(Material.PLAYER_HEAD, PartyConfig.getInventoryInviteName(), PartyConfig.getInventoryInviteLore()));
            if (PartyManager.isInParty(player.getUniqueId())) {
               inventory.setItem(15, item(Material.BARRIER, PartyConfig.getInventoryLeaveName(), PartyConfig.getInventoryLeaveLore()));
            } else {
               inventory.setItem(15, item(Material.LIME_DYE, PartyConfig.getInventoryCreateName(), PartyConfig.getInventoryCreateLore()));
            }

            inventory.setItem(22, item(Material.ARROW, PartyConfig.getInventoryBackName()));
            openMenu(player, inventory, new PartyInventoryMenu.MenuState(player.getUniqueId(), PartyInventoryMenu.MenuType.CONTROLS, 0, null, false, List.of()));
         }
      }
   }

   public static void openInteractionHint(Player player, Player target) {
      if (canUse(player)) {
         Inventory inventory = Bukkit.createInventory(player, 27, color(PartyConfig.getInventoryInteractionTitle().replace("$player", target.getName())));
         inventory.setItem(
            11,
            playerHead(
               target, PartyConfig.getInventoryInteractionInviteName().replace("$player", target.getName()), PartyConfig.getInventoryInteractionInviteLore()
            )
         );
         inventory.setItem(15, item(Material.GRAY_DYE, PartyConfig.getInventoryNeverShowName(), PartyConfig.getInventoryNeverShowLore()));
         openMenu(
            player,
            inventory,
            new PartyInventoryMenu.MenuState(player.getUniqueId(), PartyInventoryMenu.MenuType.INTERACTION_HINT, 0, target.getUniqueId(), false, List.of())
         );
      }
   }

   public static void openInvitePrompt(Player player, Player inviter) {
      if (canUse(player)) {
         Inventory inventory = Bukkit.createInventory(player, 27, color(PartyConfig.getInventoryInvitationTitle()));
         inventory.setItem(
            11,
            playerHead(inviter, PartyConfig.getInventoryAcceptInviteName().replace("$player", inviter.getName()), PartyConfig.getInventoryAcceptInviteLore())
         );
         inventory.setItem(15, item(Material.BARRIER, PartyConfig.getInventoryDeclineName(), PartyConfig.getInventoryDeclineLore()));
         openMenu(
            player,
            inventory,
            new PartyInventoryMenu.MenuState(player.getUniqueId(), PartyInventoryMenu.MenuType.INVITE_PROMPT, 0, inviter.getUniqueId(), false, List.of())
         );
      }
   }

   public static void openReadyCheck(Player player, UUID token, String dungeonDescription, boolean initiator, boolean ready) {
      if (canUse(player)) {
         Inventory inventory = Bukkit.createInventory(player, 27, color(PartyConfig.getInventoryReadyCheckTitle()));
         inventory.setItem(
            13,
            item(
               Material.MAP,
               PartyConfig.getInventoryReadyDungeonName().replace("$dungeon", dungeonDescription),
               ready ? PartyConfig.getInventoryReadyWaitingLore() : PartyConfig.getInventoryReadyConfirmLore()
            )
         );
         if (!ready) {
            inventory.setItem(11, item(Material.LIME_DYE, PartyConfig.getInventoryReadyName(), PartyConfig.getInventoryReadyLore()));
         }

         inventory.setItem(
            15,
            item(
               Material.BARRIER,
               initiator ? PartyConfig.getInventoryCancelReadyName() : PartyConfig.getInventoryDeclineName(),
               PartyConfig.getInventoryCancelReadyLore()
            )
         );
         openMenu(
            player, inventory, new PartyInventoryMenu.MenuState(player.getUniqueId(), PartyInventoryMenu.MenuType.READY_CHECK, 0, token, initiator, List.of())
         );
      }
   }

   private static void openInvites(Player player, int page) {
      List<Player> targets = PartyManager.getInvitablePlayers(player);
      int maxPage = Math.max(0, (targets.size() - 1) / 45);
      int safePage = Math.max(0, Math.min(page, maxPage));
      Inventory inventory = Bukkit.createInventory(player, 54, color(PartyConfig.getInventoryInvitePlayersTitle()));
      int start = safePage * 45;

      for (int index = start; index < Math.min(start + 45, targets.size()); index++) {
         Player target = targets.get(index);
         inventory.setItem(
            index - start,
            playerHead(target, PartyConfig.getInventoryInvitePlayerName().replace("$player", target.getName()), PartyConfig.getInventorySelectInviteLore())
         );
      }

      if (targets.isEmpty()) {
         inventory.setItem(22, item(Material.GRAY_DYE, PartyConfig.getInventoryNoInvitePlayersName(), PartyConfig.getInventoryNoInvitePlayersLore()));
      }

      if (safePage > 0) {
         inventory.setItem(45, item(Material.ARROW, PartyConfig.getInventoryPreviousName()));
      }

      inventory.setItem(49, item(Material.BARRIER, PartyConfig.getInventoryBackToPartyName()));
      if (safePage < maxPage) {
         inventory.setItem(53, item(Material.ARROW, PartyConfig.getInventoryNextName()));
      }

      openMenu(
         player,
         inventory,
         new PartyInventoryMenu.MenuState(
            player.getUniqueId(), PartyInventoryMenu.MenuType.INVITES, safePage, null, false, targets.stream().<UUID>map(OfflinePlayer::getUniqueId).toList()
         )
      );
   }

   private static void openMenu(Player player, Inventory inventory, PartyInventoryMenu.MenuState state) {
      if (player.openInventory(inventory) != null) {
         menus.put(inventory, state);
      }
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      PartyInventoryMenu.MenuState state = menus.get(event.getView().getTopInventory());
      if (state != null) {
         event.setCancelled(true);
         if (event.getWhoClicked() instanceof Player player
            && player.getUniqueId().equals(state.ownerId)
            && event.getClickedInventory() == event.getView().getTopInventory()) {
            boolean ownLeaveAction = state.type == PartyInventoryMenu.MenuType.CONTROLS
               && event.getSlot() == 15
               && PartyManager.isInParty(player.getUniqueId());
            if (!ownLeaveAction && !canUse(player)) {
               player.closeInventory();
            } else {
               switch (state.type) {
                  case CONTROLS:
                     handleControls(player, event.getSlot());
                     break;
                  case INVITES:
                     handleInvites(player, state, event.getSlot());
                     break;
                  case INTERACTION_HINT:
                     handleInteractionHint(player, state, event.getSlot());
                     break;
                  case INVITE_PROMPT:
                     handleInvitePrompt(player, event.getSlot());
                     break;
                  case READY_CHECK:
                     handleReadyCheck(player, state, event.getSlot());
               }
            }
         }
      }
   }

   private static void handleControls(Player player, int slot) {
      if (slot == 22) {
         player.closeInventory();
         new PlayerStatusScreen(player);
      } else {
         if (slot == 11) {
            openInvites(player, 0);
         } else if (!PartyManager.isInParty(player.getUniqueId()) && slot == 15) {
            player.closeInventory();
            PartyManager.create(player);
         } else if (PartyManager.isInParty(player.getUniqueId()) && slot == 15) {
            player.closeInventory();
            PartyManager.leave(player);
         }
      }
   }

   private static void handleInvites(Player player, PartyInventoryMenu.MenuState state, int slot) {
      if (slot == 45 && state.page > 0) {
         openInvites(player, state.page - 1);
      } else if (slot == 49) {
         open(player);
      } else if (slot == 53) {
         openInvites(player, state.page + 1);
      } else {
         int targetIndex = state.page * 45 + slot;
         if (slot >= 0 && slot < 45 && targetIndex < state.inviteTargetIds.size()) {
            Player target = Bukkit.getPlayer(state.inviteTargetIds.get(targetIndex));
            if (target == null) {
               PartyManager.sendConfiguredMessage(player, PartyConfig.getPlayerUnavailableMessage());
            } else {
               player.closeInventory();
               PartyManager.invite(player, target.getName());
            }
         }
      }
   }

   private static void handleInteractionHint(Player player, PartyInventoryMenu.MenuState state, int slot) {
      if (slot == 15) {
         player.closeInventory();
         PartyInteractionHint.disable(player);
      } else if (slot == 11 && state.targetId != null) {
         Player target = Bukkit.getPlayer(state.targetId);
         player.closeInventory();
         if (target == null) {
            PartyManager.sendConfiguredMessage(player, PartyConfig.getPlayerUnavailableMessage());
         } else {
            PartyManager.invite(player, target.getName());
         }
      }
   }

   private static void handleInvitePrompt(Player player, int slot) {
      if (slot == 11) {
         player.closeInventory();
         PartyManager.accept(player);
      } else if (slot == 15) {
         player.closeInventory();
         PartyManager.ignoreInvite(player);
      }
   }

   private static void handleReadyCheck(Player player, PartyInventoryMenu.MenuState state, int slot) {
      if (state.targetId != null) {
         if (slot == 11 && !state.initiator) {
            player.closeInventory();
            PartyDungeonReadyCheckManager.ready(player, state.targetId.toString());
         } else if (slot == 15) {
            player.closeInventory();
            PartyDungeonReadyCheckManager.decline(player, state.targetId.toString());
         }
      }
   }

   @EventHandler
   public void onClose(InventoryCloseEvent event) {
      menus.remove(event.getInventory());
   }

   @EventHandler
   public void onDrag(InventoryDragEvent event) {
      if (menus.containsKey(event.getView().getTopInventory())) {
         int topSize = event.getView().getTopInventory().getSize();
         if (event.getRawSlots().stream().anyMatch(slot -> slot < topSize)) {
            event.setCancelled(true);
         }
      }
   }

   static void closeReadyCheck(UUID token) {
      for (Entry<Inventory, PartyInventoryMenu.MenuState> entry : List.copyOf(menus.entrySet())) {
         PartyInventoryMenu.MenuState state = entry.getValue();
         if (state.type == PartyInventoryMenu.MenuType.READY_CHECK && token.equals(state.targetId)) {
            Player owner = Bukkit.getPlayer(state.ownerId);
            if (owner != null && owner.getOpenInventory().getTopInventory().equals(entry.getKey())) {
               owner.closeInventory();
            }

            menus.remove(entry.getKey());
         }
      }
   }

   static void closeInvitePrompt(UUID playerId) {
      for (Entry<Inventory, PartyInventoryMenu.MenuState> entry : List.copyOf(menus.entrySet())) {
         PartyInventoryMenu.MenuState state = entry.getValue();
         if (state.type == PartyInventoryMenu.MenuType.INVITE_PROMPT && playerId.equals(state.ownerId)) {
            Player owner = Bukkit.getPlayer(state.ownerId);
            if (owner != null && owner.getOpenInventory().getTopInventory().equals(entry.getKey())) {
               owner.closeInventory();
            }

            menus.remove(entry.getKey());
         }
      }
   }

   public static void shutdown() {
      for (Entry<Inventory, PartyInventoryMenu.MenuState> entry : List.copyOf(menus.entrySet())) {
         Player owner = Bukkit.getPlayer(entry.getValue().ownerId);
         if (owner != null && owner.getOpenInventory().getTopInventory().equals(entry.getKey())) {
            owner.closeInventory();
         }
      }

      menus.clear();
   }

   private static ItemStack playerHead(Player player, String name, String... lore) {
      ItemStack head = item(Material.PLAYER_HEAD, name, lore);
      SkullMeta meta = (SkullMeta)head.getItemMeta();
      meta.setOwningPlayer(player);
      head.setItemMeta(meta);
      return head;
   }

   private static ItemStack item(Material material, String name, String... lore) {
      return ItemStackGenerator.generateItemStack(material, color(name), Arrays.stream(lore).map(PartyInventoryMenu::color).toList());
   }

   private static String color(String value) {
      return ChatColorConverter.convert(value);
   }

   private static boolean canUse(Player player) {
      if (PartyConfig.isEnabled() && player.hasPermission("elitemobs.party")) {
         return true;
      } else {
         if (!PartyConfig.isEnabled()) {
            PartyManager.sendConfiguredMessage(player, PartyConfig.getDisabledMessage());
         } else {
            player.sendMessage(color(PartyConfig.getInventoryNoPermissionMessage()));
         }

         return false;
      }
   }

   private record MenuState(UUID ownerId, PartyInventoryMenu.MenuType type, int page, UUID targetId, boolean initiator, List<UUID> inviteTargetIds) {
   }

   private static enum MenuType {
      CONTROLS,
      INVITES,
      INTERACTION_HINT,
      INVITE_PROMPT,
      READY_CHECK;
   }
}
