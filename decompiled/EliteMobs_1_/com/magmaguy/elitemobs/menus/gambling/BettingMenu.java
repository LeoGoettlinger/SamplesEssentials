package com.magmaguy.elitemobs.menus.gambling;

import com.magmaguy.elitemobs.config.GamblingConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.economy.GamblingEconomyHandler;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BettingMenu {
   private static final Map<UUID, BettingMenu.BetSession> activeSessions = new HashMap<>();
   private static final int GAME_INFO_SLOT = 0;
   private static final int BALANCE_SLOT = 8;
   private static final int BET_DISPLAY_SLOT = 13;
   private static final int PLUS_100_SLOT = 10;
   private static final int PLUS_50_SLOT = 11;
   private static final int PLUS_10_SLOT = 12;
   private static final int MINUS_10_SLOT = 14;
   private static final int MINUS_50_SLOT = 15;
   private static final int MINUS_100_SLOT = 16;
   private static final int CANCEL_SLOT = 18;
   private static final int ALL_IN_SLOT = 21;
   private static final int ZERO_SLOT = 25;
   private static final int PLAY_SLOT = 26;

   public static void openBettingMenu(Player player, BettingMenu.GameType gameType) {
      if (!GamblingConfig.isGamblingEnabled()) {
         player.sendMessage(GamblingConfig.getBettingCasinoClosedMessage());
      } else {
         int defaultBet = Math.max(GamblingConfig.getMinBet(), 10);
         BettingMenu.BetSession session = new BettingMenu.BetSession(player.getUniqueId(), gameType, defaultBet);
         String title = GamblingConfig.getBettingMenuTitle().replace("%game%", gameType.getDisplayName());
         Inventory inventory = Bukkit.createInventory(player, 27, title);
         updateBettingDisplay(inventory, player, session);
         player.closeInventory();
         session.inventory = inventory;
         activeSessions.put(player.getUniqueId(), session);
         BettingMenu.BettingMenuEvents.menus.add(inventory);

         try {
            player.openInventory(inventory);
         } finally {
            if (player.getOpenInventory().getTopInventory() != inventory) {
               BettingMenu.BettingMenuEvents.menus.remove(inventory);
               activeSessions.remove(player.getUniqueId(), session);
            }
         }
      }
   }

   private static void updateBettingDisplay(Inventory inventory, Player player, BettingMenu.BetSession session) {
      UUID uuid = player.getUniqueId();
      ItemStack gameInfo = ItemStackGenerator.generateItemStack(
         Material.BOOK,
         ChatColorConverter.convert("&6&l" + session.gameType.getDisplayName()),
         List.of("", ChatColorConverter.convert("&7" + session.gameType.getDescription()), "", GamblingConfig.getBettingSelectBetLore()),
         1
      );
      inventory.setItem(0, gameInfo);
      double balance = GamblingEconomyHandler.getBalance(uuid);
      double debt = GamblingEconomyHandler.getDebt(uuid);
      double credit = Math.max(0.0, 500.0 - debt);
      double maxAvailable = Double.isFinite(balance + credit) ? Math.max(0.0, balance + credit) : 0.0;
      List<String> balanceLore = new ArrayList<>();
      balanceLore.add("");
      balanceLore.add(GamblingConfig.getBettingBalanceLabel() + String.format("%.2f", balance));
      if (debt > 0.0) {
         balanceLore.add(GamblingConfig.getBettingDebtLabel() + String.format("%.2f", debt));
         balanceLore.add(GamblingConfig.getBettingAvailableCreditLabel() + String.format("%.2f", credit));
      }

      ItemStack balanceItem = ItemStackGenerator.generateItemStack(Material.GOLD_INGOT, GamblingConfig.getBettingYourFinancesTitle(), balanceLore, 1);
      inventory.setItem(8, balanceItem);
      boolean canAfford = EconomyHandler.isReady(uuid) && session.betAmount > 0 && session.betAmount <= maxAvailable;
      ItemStack betDisplay = ItemStackGenerator.generateItemStack(
         canAfford ? Material.EMERALD : Material.REDSTONE,
         ChatColorConverter.convert(
            (canAfford ? GamblingConfig.getBettingAffordableColor() : GamblingConfig.getBettingUnaffordableColor())
               + GamblingConfig.getBettingCurrentBetPrefix()
               + session.betAmount
         ),
         List.of("", canAfford ? GamblingConfig.getBettingClickPlayLore() : GamblingConfig.getBettingCantAffordLore()),
         1
      );
      inventory.setItem(13, betDisplay);
      int[] increments = calculateIncrements(balance, debt);
      session.increments = increments;
      session.maximumDisplayedBet = (int)maxAvailable;
      int smallIncrement = increments[0];
      int mediumIncrement = increments[1];
      int largeIncrement = increments[2];
      inventory.setItem(12, createModifierButton("+" + smallIncrement, Material.LIME_STAINED_GLASS_PANE, smallIncrement));
      inventory.setItem(11, createModifierButton("+" + mediumIncrement, Material.LIME_STAINED_GLASS_PANE, mediumIncrement));
      inventory.setItem(10, createModifierButton("+" + largeIncrement, Material.LIME_STAINED_GLASS_PANE, largeIncrement));
      inventory.setItem(14, createModifierButton("-" + smallIncrement, Material.RED_STAINED_GLASS_PANE, smallIncrement));
      inventory.setItem(15, createModifierButton("-" + mediumIncrement, Material.RED_STAINED_GLASS_PANE, mediumIncrement));
      inventory.setItem(16, createModifierButton("-" + largeIncrement, Material.RED_STAINED_GLASS_PANE, largeIncrement));
      ItemStack allInButton = ItemStackGenerator.generateItemStack(
         Material.DIAMOND,
         GamblingConfig.getBettingAllInButtonText(),
         List.of("", GamblingConfig.getBettingAllInLore(), GamblingConfig.getBettingMaxAvailableLabel() + (int)maxAvailable),
         1
      );
      inventory.setItem(21, allInButton);
      ItemStack zeroButton = ItemStackGenerator.generateItemStack(
         Material.COAL, GamblingConfig.getBettingResetButtonText(), List.of("", GamblingConfig.getBettingResetLore() + GamblingConfig.getMinBet()), 1
      );
      inventory.setItem(25, zeroButton);
      ItemStack cancelButton = ItemStackGenerator.generateItemStack(
         Material.BARRIER, GamblingConfig.getCancelButtonText(), List.of(GamblingConfig.getBettingCancelLore()), 1
      );
      inventory.setItem(18, cancelButton);
      ItemStack playButton = ItemStackGenerator.generateItemStack(
         canAfford ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE,
         canAfford ? GamblingConfig.getPlayButtonText() : GamblingConfig.getBettingCantAffordButtonText(),
         List.of("", canAfford ? GamblingConfig.getBettingStartGameLore() : GamblingConfig.getBettingReduceBetLore()),
         1
      );
      inventory.setItem(26, playButton);
   }

   private static ItemStack createModifierButton(String text, Material material, int amount) {
      return ItemStackGenerator.generateItemStack(
         material, ChatColorConverter.convert("&f" + text), List.of(GamblingConfig.getBettingAdjustBetLore() + text), 1
      );
   }

   private static int[] calculateIncrements(double balance, double debt) {
      if (!(balance < 100.0) && !(debt > 0.0)) {
         int small = Math.max(1, (int)(balance * 0.05));
         int medium = Math.max(1, (int)(balance * 0.1));
         int large = Math.max(1, (int)(balance * 0.25));
         return new int[]{small, medium, large};
      } else {
         return new int[]{10, 50, 100};
      }
   }

   private static void startGame(Player player, BettingMenu.BetSession session) {
      if (!GamblingEconomyHandler.placeBet(player.getUniqueId(), session.betAmount)) {
         player.sendMessage(GamblingConfig.getInsufficientFundsMessage());
      } else {
         if (GamblingEconomyHandler.isInDebt(player.getUniqueId())) {
            player.sendMessage(
               GamblingConfig.getDebtWarningMessage().replace("%debt%", String.format("%.2f", GamblingEconomyHandler.getDebt(player.getUniqueId())))
            );
         }

         player.sendMessage(GamblingConfig.getBetPlacedMessage().replace("%amount%", String.valueOf(session.betAmount)));
         player.closeInventory();
         switch (session.gameType) {
            case BLACKJACK:
               BlackjackGame.startGame(player, session.betAmount);
               break;
            case COIN_FLIP:
               CoinFlipGame.startGame(player, session.betAmount);
               break;
            case SLOTS:
               SlotMachineGame.startGame(player, session.betAmount);
               break;
            case HIGHER_LOWER:
               HigherLowerGame.startGame(player, session.betAmount);
         }
      }
   }

   public static void shutdown() {
      for (BettingMenu.BetSession session : List.copyOf(activeSessions.values())) {
         activeSessions.remove(session.playerUUID, session);
         BettingMenu.BettingMenuEvents.menus.remove(session.inventory);
         Player player = Bukkit.getPlayer(session.playerUUID);
         if (player != null && player.getOpenInventory().getTopInventory() == session.inventory) {
            player.closeInventory();
         }
      }
   }

   private static class BetSession {
      final UUID playerUUID;
      final BettingMenu.GameType gameType;
      int betAmount;
      Inventory inventory;
      int[] increments;
      int maximumDisplayedBet;

      BetSession(UUID playerUUID, BettingMenu.GameType gameType, int betAmount) {
         this.playerUUID = playerUUID;
         this.gameType = gameType;
         this.betAmount = betAmount;
      }
   }

   public static class BettingMenuEvents implements Listener {
      static final Set<Inventory> menus = new HashSet<>();

      @EventHandler
      public void onClick(InventoryClickEvent event) {
         Player player = GamblingDisplay.validateClick(event, menus);
         if (player != null) {
            BettingMenu.BetSession session = BettingMenu.activeSessions.get(player.getUniqueId());
            if (session != null && session.inventory == event.getInventory()) {
               int slot = event.getSlot();
               int minBet = GamblingConfig.getMinBet();
               int[] increments = session.increments;
               int smallIncrement = increments[0];
               int mediumIncrement = increments[1];
               int largeIncrement = increments[2];
               switch (slot) {
                  case 10:
                     session.betAmount = (int)Math.min(2147483647L, (long)session.betAmount + largeIncrement);
                     break;
                  case 11:
                     session.betAmount = (int)Math.min(2147483647L, (long)session.betAmount + mediumIncrement);
                     break;
                  case 12:
                     session.betAmount = (int)Math.min(2147483647L, (long)session.betAmount + smallIncrement);
                     break;
                  case 13:
                  case 17:
                  case 19:
                  case 20:
                  case 22:
                  case 23:
                  case 24:
                  default:
                     return;
                  case 14:
                     session.betAmount = Math.max(minBet, session.betAmount - smallIncrement);
                     break;
                  case 15:
                     session.betAmount = Math.max(minBet, session.betAmount - mediumIncrement);
                     break;
                  case 16:
                     session.betAmount = Math.max(minBet, session.betAmount - largeIncrement);
                     break;
                  case 18:
                     player.closeInventory();
                     return;
                  case 21:
                     session.betAmount = session.maximumDisplayedBet;
                     break;
                  case 25:
                     session.betAmount = minBet;
                     break;
                  case 26:
                     BettingMenu.startGame(player, session);
                     return;
               }

               BettingMenu.updateBettingDisplay(event.getInventory(), player, session);
            } else {
               player.closeInventory();
            }
         }
      }

      @EventHandler
      public void onClose(InventoryCloseEvent event) {
         if (menus.remove(event.getInventory())) {
            BettingMenu.BetSession session = BettingMenu.activeSessions.get(event.getPlayer().getUniqueId());
            if (session != null && session.inventory == event.getInventory()) {
               BettingMenu.activeSessions.remove(session.playerUUID, session);
            }
         }
      }
   }

   public static enum GameType {
      BLACKJACK,
      COIN_FLIP,
      SLOTS,
      HIGHER_LOWER;

      public String getDisplayName() {
         return switch (this) {
            case BLACKJACK -> GamblingConfig.getBettingBlackjackName();
            case COIN_FLIP -> GamblingConfig.getBettingCoinFlipName();
            case SLOTS -> GamblingConfig.getBettingSlotsName();
            case HIGHER_LOWER -> GamblingConfig.getBettingHigherLowerName();
         };
      }

      public String getDescription() {
         return switch (this) {
            case BLACKJACK -> GamblingConfig.getBettingBlackjackDescription();
            case COIN_FLIP -> GamblingConfig.getBettingCoinFlipDescription();
            case SLOTS -> GamblingConfig.getBettingSlotsDescription();
            case HIGHER_LOWER -> GamblingConfig.getBettingHigherLowerDescription();
         };
      }
   }
}
