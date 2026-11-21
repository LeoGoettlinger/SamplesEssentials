package com.magmaguy.elitemobs.menus.gambling;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.GamblingConfig;
import com.magmaguy.elitemobs.economy.GamblingEconomyHandler;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class BlackjackGame {
   private static final Map<UUID, BlackjackGame.BlackjackSession> activeSessions = new HashMap<>();
   private static final int[] DEALER_CARD_SLOTS = new int[]{1, 2, 3, 4, 5};
   private static final int[] PLAYER_CARD_SLOTS = new int[]{28, 29, 30, 31, 32};
   private static final int DEALER_TOTAL_SLOT = 7;
   private static final int PLAYER_TOTAL_SLOT = 34;
   private static final int HIT_SLOT = 45;
   private static final int STAND_SLOT = 47;
   private static final int DOUBLE_DOWN_SLOT = 49;
   private static final int BET_INFO_SLOT = 51;
   private static final int RESULT_SLOT = 22;
   private static final String[] CARD_NAMES = new String[]{"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
   private static final String[] SUIT_COLORS = new String[]{"&8", "&c", "&c", "&8"};

   public static void startGame(Player player, int betAmount) {
      BlackjackGame.BlackjackSession session = new BlackjackGame.BlackjackSession(player.getUniqueId(), betAmount);

      try {
         session.playerHand.add(drawCard(session));
         session.dealerHand.add(drawCard(session));
         session.playerHand.add(drawCard(session));
         session.dealerHand.add(drawCard(session));
         String title = GamblingConfig.getBlackjackMenuTitle();
         Inventory inventory = Bukkit.createInventory(player, 54, title);
         session.inventory = inventory;
         activeSessions.put(player.getUniqueId(), session);
         updateDisplay(inventory, session, false);
         if (calculateHandValue(session.playerHand) == 21) {
            session.gameState = BlackjackGame.GameState.PLAYER_BLACKJACK;
            processEndGame(player, session, inventory);
         }

         BlackjackGame.BlackjackMenuEvents.menus.add(inventory);

         try {
            player.openInventory(inventory);
         } finally {
            if (player.getOpenInventory().getTopInventory() != inventory) {
               throw new IllegalStateException("Gambling inventory opening was cancelled");
            }
         }
      } catch (RuntimeException var11) {
         activeSessions.remove(session.playerUUID, session);
         BlackjackGame.BlackjackMenuEvents.menus.remove(session.inventory);

         try {
            GamblingEconomyHandler.resolveOutcome(session.playerUUID, session.betAmount);
         } catch (RuntimeException var9) {
            var11.addSuppressed(var9);
         }

         Logger.warn("Could not open gambling game for " + session.playerUUID + ": " + var11);
         player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
      }
   }

   private static BlackjackGame.Card drawCard(BlackjackGame.BlackjackSession session) {
      int value = ThreadLocalRandom.current().nextInt(1, 14);
      int suit = ThreadLocalRandom.current().nextInt(0, 4);
      return new BlackjackGame.Card(value, suit);
   }

   private static int calculateHandValue(List<BlackjackGame.Card> hand) {
      int total = 0;
      int aces = 0;

      for (BlackjackGame.Card card : hand) {
         if (card.value == 1) {
            aces++;
            total += 11;
         } else {
            total += card.getBlackjackValue();
         }
      }

      while (total > 21 && aces > 0) {
         total -= 10;
         aces--;
      }

      return total;
   }

   private static void updateDisplay(Inventory inventory, BlackjackGame.BlackjackSession session, boolean showDealerHand) {
      for (int slot : DEALER_CARD_SLOTS) {
         inventory.setItem(slot, null);
      }

      for (int slot : PLAYER_CARD_SLOTS) {
         inventory.setItem(slot, null);
      }

      for (int i = 0; i < session.dealerHand.size() && i < DEALER_CARD_SLOTS.length; i++) {
         if (i == 1 && !showDealerHand && session.gameState == BlackjackGame.GameState.PLAYING) {
            inventory.setItem(DEALER_CARD_SLOTS[i], createHiddenCardItem());
         } else {
            inventory.setItem(DEALER_CARD_SLOTS[i], createCardItem(session.dealerHand.get(i)));
         }
      }

      for (int ix = 0; ix < session.playerHand.size() && ix < PLAYER_CARD_SLOTS.length; ix++) {
         inventory.setItem(PLAYER_CARD_SLOTS[ix], createCardItem(session.playerHand.get(ix)));
      }

      int dealerVisible = showDealerHand ? calculateHandValue(session.dealerHand) : session.dealerHand.get(0).getBlackjackValue();
      String dealerTotalText = showDealerHand ? String.valueOf(calculateHandValue(session.dealerHand)) : dealerVisible + " + ?";
      ItemStack dealerTotal = ItemStackGenerator.generateItemStack(
         Material.RED_BANNER, GamblingConfig.getBlackjackDealerLabel() + dealerTotalText, List.of(), 1
      );
      inventory.setItem(7, dealerTotal);
      int playerTotal = calculateHandValue(session.playerHand);
      Material totalMaterial = playerTotal > 21 ? Material.REDSTONE_BLOCK : (playerTotal == 21 ? Material.EMERALD_BLOCK : Material.BLUE_BANNER);
      ItemStack playerTotalItem = ItemStackGenerator.generateItemStack(totalMaterial, GamblingConfig.getBlackjackPlayerLabel() + playerTotal, List.of(), 1);
      inventory.setItem(34, playerTotalItem);
      if (session.gameState == BlackjackGame.GameState.PLAYING) {
         ItemStack hitButton = ItemStackGenerator.generateItemStack(
            Material.LIME_CONCRETE, GamblingConfig.getBlackjackHitButtonText(), List.of("", GamblingConfig.getBlackjackHitButtonLore()), 1
         );
         inventory.setItem(45, hitButton);
         ItemStack standButton = ItemStackGenerator.generateItemStack(
            Material.YELLOW_CONCRETE, GamblingConfig.getBlackjackStandButtonText(), List.of("", GamblingConfig.getBlackjackStandButtonLore()), 1
         );
         inventory.setItem(47, standButton);
         if (session.playerHand.size() == 2 && session.canDoubleDown) {
            boolean canAffordDouble = GamblingEconomyHandler.canAffordBet(session.playerUUID, session.betAmount);
            ItemStack doubleButton = ItemStackGenerator.generateItemStack(
               canAffordDouble ? Material.GOLD_BLOCK : Material.GRAY_CONCRETE,
               canAffordDouble ? GamblingConfig.getBlackjackDoubleDownButtonText() : GamblingConfig.getBlackjackDoubleDownDisabledText(),
               List.of("", canAffordDouble ? GamblingConfig.getBlackjackDoubleDownButtonLore() : GamblingConfig.getBlackjackDoubleDownCantAffordLore()),
               1
            );
            inventory.setItem(49, doubleButton);
         } else {
            inventory.setItem(49, null);
         }
      } else {
         inventory.setItem(45, null);
         inventory.setItem(47, null);
         inventory.setItem(49, null);
      }

      ItemStack betInfo = ItemStackGenerator.generateItemStack(
         Material.EMERALD,
         GamblingConfig.getBlackjackBetLabel() + session.betAmount,
         List.of(
            "",
            GamblingConfig.getBlackjackWinPayoutLabel() + String.format("%.2f", session.betAmount * GamblingConfig.getBlackjackPayoutNormal()),
            GamblingConfig.getBlackjackBlackjackPayoutLabel() + String.format("%.2f", session.betAmount * GamblingConfig.getBlackjackPayoutBlackjack())
         ),
         1
      );
      inventory.setItem(51, betInfo);
   }

   private static ItemStack createCardItem(BlackjackGame.Card card) {
      Material material = card.suit < 2 ? Material.BLACK_BANNER : Material.RED_BANNER;
      return ItemStackGenerator.generateItemStack(
         material, ChatColorConverter.convert(card.getName()), List.of(GamblingConfig.getBlackjackCardValueLabel() + card.getBlackjackValue()), 1
      );
   }

   private static ItemStack createHiddenCardItem() {
      return ItemStackGenerator.generateItemStack(
         Material.GRAY_BANNER, GamblingConfig.getBlackjackHiddenCardText(), List.of(GamblingConfig.getBlackjackHiddenCardLore()), 1
      );
   }

   private static void onHit(Player player, BlackjackGame.BlackjackSession session, Inventory inventory) {
      if (session.gameState == BlackjackGame.GameState.PLAYING) {
         session.playerHand.add(drawCard(session));
         session.canDoubleDown = false;
         int total = calculateHandValue(session.playerHand);
         if (total > 21) {
            session.gameState = BlackjackGame.GameState.PLAYER_BUST;
            processEndGame(player, session, inventory);
         } else if (total == 21) {
            onStand(player, session, inventory);
         } else {
            updateDisplay(inventory, session, false);
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
         }
      }
   }

   private static void onStand(Player player, BlackjackGame.BlackjackSession session, Inventory inventory) {
      if (session.gameState == BlackjackGame.GameState.PLAYING) {
         session.gameState = BlackjackGame.GameState.DEALER_TURN;
         playDealerTurn(player, session, inventory);
      }
   }

   private static void onDoubleDown(Player player, BlackjackGame.BlackjackSession session, Inventory inventory) {
      if (session.gameState == BlackjackGame.GameState.PLAYING && session.canDoubleDown) {
         if (session.playerHand.size() == 2) {
            long doubledBet = session.betAmount * 2L;
            if (doubledBet > 2147483647L) {
               player.sendMessage(GamblingConfig.getInsufficientFundsMessage());
            } else if (!GamblingEconomyHandler.placeBet(player.getUniqueId(), session.betAmount)) {
               player.sendMessage(GamblingConfig.getInsufficientFundsMessage());
            } else {
               session.betAmount = (int)doubledBet;
               session.canDoubleDown = false;
               session.playerHand.add(drawCard(session));
               int total = calculateHandValue(session.playerHand);
               if (total > 21) {
                  session.gameState = BlackjackGame.GameState.PLAYER_BUST;
                  processEndGame(player, session, inventory);
               } else {
                  onStand(player, session, inventory);
               }
            }
         }
      }
   }

   private static void playDealerTurn(final Player player, final BlackjackGame.BlackjackSession session, final Inventory inventory) {
      (new BukkitRunnable() {
         public void run() {
            if (player.isOnline() && BlackjackGame.BlackjackMenuEvents.menus.contains(inventory)) {
               int dealerTotal = BlackjackGame.calculateHandValue(session.dealerHand);
               if (dealerTotal < 17) {
                  session.dealerHand.add(BlackjackGame.drawCard(session));
                  BlackjackGame.updateDisplay(inventory, session, true);
                  player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
               } else {
                  if (dealerTotal > 21) {
                     session.gameState = BlackjackGame.GameState.DEALER_BUST;
                  } else {
                     int playerTotal = BlackjackGame.calculateHandValue(session.playerHand);
                     if (playerTotal > dealerTotal) {
                        session.gameState = BlackjackGame.GameState.PLAYER_WINS;
                     } else if (dealerTotal > playerTotal) {
                        session.gameState = BlackjackGame.GameState.DEALER_WINS;
                     } else {
                        session.gameState = BlackjackGame.GameState.PUSH;
                     }
                  }

                  BlackjackGame.processEndGame(player, session, inventory);
                  this.cancel();
               }
            } else {
               this.cancel();
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 20L, 20L);
   }

   private static void processEndGame(Player player, BlackjackGame.BlackjackSession session, Inventory inventory) {
      double payout = 0.0;
      String resultMessage;
      Material resultMaterial;
      Sound resultSound;
      switch (session.gameState) {
         case PLAYER_BUST:
            resultMessage = GamblingConfig.getBlackjackResultBust();
            resultMaterial = Material.REDSTONE_BLOCK;
            resultSound = Sound.ENTITY_VILLAGER_NO;
            break;
         case DEALER_BUST:
            payout = session.betAmount * GamblingConfig.getBlackjackPayoutNormal();
            resultMessage = GamblingConfig.getBlackjackResultDealerBusts();
            resultMaterial = Material.EMERALD_BLOCK;
            resultSound = Sound.ENTITY_PLAYER_LEVELUP;
            break;
         case PLAYER_WINS:
            payout = session.betAmount * GamblingConfig.getBlackjackPayoutNormal();
            resultMessage = GamblingConfig.getBlackjackResultYouWin();
            resultMaterial = Material.EMERALD_BLOCK;
            resultSound = Sound.ENTITY_PLAYER_LEVELUP;
            break;
         case DEALER_WINS:
            resultMessage = GamblingConfig.getBlackjackResultDealerWins();
            resultMaterial = Material.REDSTONE_BLOCK;
            resultSound = Sound.ENTITY_VILLAGER_NO;
            break;
         case PLAYER_BLACKJACK:
            payout = session.betAmount * GamblingConfig.getBlackjackPayoutBlackjack();
            resultMessage = GamblingConfig.getBlackjackResultBlackjack();
            resultMaterial = Material.EMERALD_BLOCK;
            resultSound = Sound.UI_TOAST_CHALLENGE_COMPLETE;
            break;
         case PUSH:
            payout = session.betAmount;
            resultMessage = GamblingConfig.getBlackjackResultPush();
            resultMaterial = Material.YELLOW_CONCRETE;
            resultSound = Sound.BLOCK_NOTE_BLOCK_PLING;
            break;
         default:
            resultMessage = GamblingConfig.getBlackjackResultGameOver();
            resultMaterial = Material.GRAY_CONCRETE;
            resultSound = Sound.BLOCK_NOTE_BLOCK_BASS;
      }

      payout = GamblingEconomyHandler.resolveOutcome(player.getUniqueId(), payout);
      updateDisplay(inventory, session, true);
      List<String> resultLore = new ArrayList<>();
      resultLore.add("");
      resultLore.add(GamblingConfig.getBlackjackResultPlayerLabel() + calculateHandValue(session.playerHand));
      resultLore.add(GamblingConfig.getBlackjackResultDealerLabel() + calculateHandValue(session.dealerHand));
      resultLore.add("");
      if (payout > 0.0) {
         resultLore.add(
            payout > session.betAmount
               ? GamblingConfig.getBlackjackResultWonPrefix()
                  + String.format("%.2f", payout - session.betAmount)
                  + GamblingConfig.getBlackjackResultWonSuffix()
               : GamblingConfig.getBlackjackResultBetReturned()
         );
      } else {
         resultLore.add(GamblingConfig.getBlackjackResultLostPrefix() + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord() + ".");
      }

      resultLore.add("");
      resultLore.add(GamblingConfig.getBlackjackResultCloseToContinue());
      ItemStack resultItem = ItemStackGenerator.generateItemStack(resultMaterial, resultMessage, resultLore, 1);
      inventory.setItem(22, resultItem);
      player.playSound(player.getLocation(), resultSound, 1.0F, 1.0F);
      if (payout > session.betAmount) {
         GamblingDisplay.sendWinMessage(player, payout - session.betAmount, GamblingConfig.getBettingBlackjackName());
      } else if (payout == 0.0) {
         GamblingDisplay.sendLoseMessage(player, session.betAmount, GamblingConfig.getBettingBlackjackName());
      }
   }

   public static void shutdown() {
      for (BlackjackGame.BlackjackSession session : List.copyOf(activeSessions.values())) {
         closeSession(session);
         Player player = Bukkit.getPlayer(session.playerUUID);
         if (player != null && player.getOpenInventory().getTopInventory() == session.inventory) {
            player.closeInventory();
         }
      }
   }

   private static void closeSession(BlackjackGame.BlackjackSession session) {
      if (activeSessions.remove(session.playerUUID, session)) {
         BlackjackGame.BlackjackMenuEvents.menus.remove(session.inventory);
         double payout = 0.0;
         if (session.gameState == BlackjackGame.GameState.DEALER_TURN) {
            while (calculateHandValue(session.dealerHand) < 17) {
               session.dealerHand.add(drawCard(session));
            }

            int dealer = calculateHandValue(session.dealerHand);
            int player = calculateHandValue(session.playerHand);
            if (dealer > 21 || player > dealer) {
               payout = GamblingEconomyHandler.calculatePayout(session.betAmount, GamblingConfig.getBlackjackPayoutNormal());
            } else if (player == dealer) {
               payout = session.betAmount;
            }
         }

         try {
            double awarded = GamblingEconomyHandler.resolveOutcome(session.playerUUID, payout);
            Player player = Bukkit.getPlayer(session.playerUUID);
            if (awarded > 0.0 && player != null && player.isOnline()) {
               GamblingDisplay.sendWinMessage(player, awarded - session.betAmount, GamblingConfig.getBettingBlackjackName());
            }
         } catch (RuntimeException var6) {
            Logger.warn("Gambling close could not settle " + session.playerUUID + ": " + var6);
         }
      }
   }

   public static class BlackjackMenuEvents implements Listener {
      static final Set<Inventory> menus = new HashSet<>();

      @EventHandler
      public void onClick(InventoryClickEvent event) {
         Player player = GamblingDisplay.validateClick(event, menus);
         if (player != null) {
            BlackjackGame.BlackjackSession session = BlackjackGame.activeSessions.get(player.getUniqueId());
            if (session != null && session.inventory == event.getInventory() && session.gameState == BlackjackGame.GameState.PLAYING) {
               int slot = event.getSlot();
               if (slot == 45) {
                  BlackjackGame.onHit(player, session, event.getInventory());
               } else if (slot == 47) {
                  BlackjackGame.onStand(player, session, event.getInventory());
               } else if (slot == 49) {
                  BlackjackGame.onDoubleDown(player, session, event.getInventory());
               }
            }
         }
      }

      @EventHandler
      public void onClose(InventoryCloseEvent event) {
         if (menus.contains(event.getInventory())) {
            BlackjackGame.BlackjackSession session = BlackjackGame.activeSessions.get(event.getPlayer().getUniqueId());
            if (session != null && session.inventory == event.getInventory()) {
               BlackjackGame.closeSession(session);
            }
         }
      }
   }

   private static class BlackjackSession extends GamblingSession {
      final List<BlackjackGame.Card> playerHand = new ArrayList<>();
      final List<BlackjackGame.Card> dealerHand = new ArrayList<>();
      BlackjackGame.GameState gameState = BlackjackGame.GameState.PLAYING;
      boolean canDoubleDown = true;

      BlackjackSession(UUID playerUUID, int betAmount) {
         super(playerUUID, betAmount);
      }
   }

   private static class Card {
      final int value;
      final int suit;

      Card(int value, int suit) {
         this.value = value;
         this.suit = suit;
      }

      String getName() {
         String[] suitNames = new String[]{
            GamblingConfig.getBlackjackCardSuitSpades(),
            GamblingConfig.getBlackjackCardSuitHearts(),
            GamblingConfig.getBlackjackCardSuitDiamonds(),
            GamblingConfig.getBlackjackCardSuitClubs()
         };
         return BlackjackGame.SUIT_COLORS[this.suit]
            + BlackjackGame.CARD_NAMES[this.value - 1]
            + GamblingConfig.getBlackjackCardOfText()
            + suitNames[this.suit];
      }

      int getBlackjackValue() {
         if (this.value == 1) {
            return 11;
         } else {
            return this.value >= 10 ? 10 : this.value;
         }
      }
   }

   private static enum GameState {
      PLAYING,
      DEALER_TURN,
      PLAYER_BUST,
      DEALER_BUST,
      PLAYER_WINS,
      DEALER_WINS,
      PLAYER_BLACKJACK,
      PUSH;
   }
}
