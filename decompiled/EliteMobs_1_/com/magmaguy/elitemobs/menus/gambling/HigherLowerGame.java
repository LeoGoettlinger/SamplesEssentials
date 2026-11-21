package com.magmaguy.elitemobs.menus.gambling;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.GamblingConfig;
import com.magmaguy.elitemobs.economy.GamblingEconomyHandler;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.magmacore.util.Logger;
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

public class HigherLowerGame {
   private static final Map<UUID, HigherLowerGame.HigherLowerSession> activeSessions = new HashMap<>();
   private static final int CURRENT_CARD_SLOT = 11;
   private static final int NEXT_CARD_SLOT = 15;
   private static final int HIGHER_SLOT = 19;
   private static final int CASH_OUT_SLOT = 22;
   private static final int LOWER_SLOT = 25;
   private static final int STREAK_SLOT = 4;
   private static final int BET_INFO_SLOT = 13;
   private static final int MAX_STREAK = 12;
   private static final String[] CARD_NAMES = new String[]{"", "", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King", "Ace"};
   private static final Material[] CARD_MATERIALS = new Material[]{
      Material.WHITE_BANNER,
      Material.WHITE_BANNER,
      Material.WHITE_BANNER,
      Material.WHITE_BANNER,
      Material.ORANGE_BANNER,
      Material.ORANGE_BANNER,
      Material.ORANGE_BANNER,
      Material.ORANGE_BANNER,
      Material.RED_BANNER,
      Material.PURPLE_BANNER,
      Material.PURPLE_BANNER,
      Material.YELLOW_BANNER,
      Material.GOLD_BLOCK
   };

   public static void startGame(Player player, int betAmount) {
      HigherLowerGame.HigherLowerSession session = new HigherLowerGame.HigherLowerSession(player.getUniqueId(), betAmount);

      try {
         session.currentCard = drawCard();
         String title = GamblingConfig.getHigherLowerMenuTitle();
         Inventory inventory = Bukkit.createInventory(player, 27, title);
         session.inventory = inventory;
         activeSessions.put(player.getUniqueId(), session);
         updateDisplay(inventory, session);
         HigherLowerGame.HigherLowerMenuEvents.menus.add(inventory);

         try {
            player.openInventory(inventory);
         } finally {
            if (player.getOpenInventory().getTopInventory() != inventory) {
               throw new IllegalStateException("Gambling inventory opening was cancelled");
            }
         }
      } catch (RuntimeException var11) {
         activeSessions.remove(session.playerUUID, session);
         HigherLowerGame.HigherLowerMenuEvents.menus.remove(session.inventory);

         try {
            GamblingEconomyHandler.resolveOutcome(session.playerUUID, session.betAmount);
         } catch (RuntimeException var9) {
            var11.addSuppressed(var9);
         }

         Logger.warn("Could not open gambling game for " + session.playerUUID + ": " + var11);
         player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
      }
   }

   private static int drawCard() {
      return ThreadLocalRandom.current().nextInt(2, 15);
   }

   private static String getCardName(int value) {
      return value >= 2 && value <= 14 ? CARD_NAMES[value] : "?";
   }

   private static Material getCardMaterial(int value) {
      return value >= 2 && value <= 14 ? CARD_MATERIALS[value - 2] : Material.GRAY_BANNER;
   }

   private static void updateDisplay(Inventory inventory, HigherLowerGame.HigherLowerSession session) {
      ItemStack currentCard = ItemStackGenerator.generateItemStack(
         getCardMaterial(session.currentCard),
         ChatColorConverter.convert("&6&l" + getCardName(session.currentCard)),
         List.of("", GamblingConfig.getHigherLowerCardValueLabel() + session.currentCard, GamblingConfig.getHigherLowerNextCardPrompt()),
         1
      );
      inventory.setItem(11, currentCard);
      ItemStack nextCard = ItemStackGenerator.generateItemStack(
         Material.GRAY_BANNER, GamblingConfig.getHigherLowerHiddenCardText(), List.of("", GamblingConfig.getHigherLowerMakeGuessLore()), 1
      );
      inventory.setItem(15, nextCard);
      ItemStack higherButton = ItemStackGenerator.generateItemStack(
         Material.LIME_CONCRETE,
         GamblingConfig.getHigherLowerHigherButtonText(),
         List.of("", GamblingConfig.getHigherLowerHigherLore1(), GamblingConfig.getHigherLowerHigherLore2() + getCardName(session.currentCard)),
         1
      );
      inventory.setItem(19, higherButton);
      double currentPayout = session.betAmount * session.multiplier;
      ItemStack cashOutButton;
      if (session.streak > 0) {
         cashOutButton = ItemStackGenerator.generateItemStack(
            Material.GOLD_INGOT,
            GamblingConfig.getHigherLowerCashOutButtonText(),
            List.of(
               "", GamblingConfig.getHigherLowerCurrentWinningsLabel() + String.format("%.2f", currentPayout), GamblingConfig.getHigherLowerCashOutClickLore()
            ),
            1
         );
      } else {
         cashOutButton = ItemStackGenerator.generateItemStack(
            Material.GRAY_STAINED_GLASS_PANE,
            GamblingConfig.getHigherLowerCashOutDisabledText(),
            List.of("", GamblingConfig.getHigherLowerCashOutWinOnceLore()),
            1
         );
      }

      inventory.setItem(22, cashOutButton);
      ItemStack lowerButton = ItemStackGenerator.generateItemStack(
         Material.RED_CONCRETE,
         GamblingConfig.getHigherLowerLowerButtonText(),
         List.of("", GamblingConfig.getHigherLowerLowerLore1(), GamblingConfig.getHigherLowerLowerLore2() + getCardName(session.currentCard)),
         1
      );
      inventory.setItem(25, lowerButton);
      ItemStack streakDisplay = ItemStackGenerator.generateItemStack(
         Material.BLAZE_POWDER,
         GamblingConfig.getHigherLowerStreakLabel() + session.streak + "/12",
         List.of(
            "",
            GamblingConfig.getHigherLowerMultiplierLabel() + String.format("%.2f", session.multiplier) + "x",
            GamblingConfig.getHigherLowerPotentialWinLabel() + String.format("%.2f", currentPayout),
            ChatColorConverter.convert(GamblingConfig.getHigherLowerMaxStreakLabel() + "12" + GamblingConfig.getHigherLowerAutoCashoutSuffix())
         ),
         1
      );
      inventory.setItem(4, streakDisplay);
      ItemStack betInfo = ItemStackGenerator.generateItemStack(
         Material.EMERALD,
         GamblingConfig.getHigherLowerBetLabel() + session.betAmount,
         List.of("", GamblingConfig.getHigherLowerEachGuessLore() + GamblingConfig.getHigherLowerMultiplier() + "x"),
         1
      );
      inventory.setItem(13, betInfo);
   }

   private static void processGuess(Player player, HigherLowerGame.HigherLowerSession session, boolean guessHigher, Inventory inventory) {
      if (!session.isProcessing && !session.gameOver) {
         session.isProcessing = true;
         int nextCard = drawCard();
         boolean isHigher = nextCard > session.currentCard;
         boolean isEqual = nextCard == session.currentCard;
         boolean playerWins = !isEqual && guessHigher == isHigher;
         session.nextCard = nextCard;
         session.lastGuessCorrect = playerWins;
         if (playerWins) {
            session.streak++;
            session.multiplier = session.multiplier * GamblingConfig.getHigherLowerMultiplier();
            if (session.streak >= 12) {
               session.gameOver = true;
               session.forcedCashOut = true;
               double payout = session.betAmount * session.multiplier;
               session.winAmount = GamblingEconomyHandler.resolveOutcome(player.getUniqueId(), payout);
            }
         } else {
            session.gameOver = true;
            GamblingEconomyHandler.resolveOutcome(player.getUniqueId(), 0.0);
         }

         disableButtons(inventory);
         revealCardAnimation(player, session, inventory, nextCard, playerWins);
      }
   }

   private static void cashOut(Player player, HigherLowerGame.HigherLowerSession session, Inventory inventory) {
      if (session.streak >= 1 && !session.gameOver) {
         session.gameOver = true;
         double payout = session.betAmount * session.multiplier;
         payout = GamblingEconomyHandler.resolveOutcome(player.getUniqueId(), payout);
         disableButtons(inventory);
         showCashOutResult(inventory, session, payout);
         GamblingDisplay.playWinSound(player);
         GamblingDisplay.sendWinMessage(player, payout, GamblingConfig.getBettingHigherLowerName());
      }
   }

   private static void disableButtons(Inventory inventory) {
      ItemStack disabled = ItemStackGenerator.generateItemStack(Material.GRAY_STAINED_GLASS_PANE, GamblingConfig.getHigherLowerWait(), List.of(), 1);
      inventory.setItem(19, disabled);
      inventory.setItem(25, disabled);
      inventory.setItem(22, disabled);
   }

   private static void revealCardAnimation(
      final Player player, final HigherLowerGame.HigherLowerSession session, final Inventory inventory, final int nextCard, final boolean playerWins
   ) {
      (new BukkitRunnable() {
            int ticks = 0;

            public void run() {
               if (player.isOnline() && HigherLowerGame.HigherLowerMenuEvents.menus.contains(inventory)) {
                  this.ticks++;
                  if (this.ticks <= 10) {
                     int randomValue = ThreadLocalRandom.current().nextInt(2, 15);
                     ItemStack shuffledCard = ItemStackGenerator.generateItemStack(
                        HigherLowerGame.getCardMaterial(randomValue),
                        ChatColorConverter.convert("&8" + HigherLowerGame.getCardName(randomValue)),
                        List.of(GamblingConfig.getHigherLowerRevealing()),
                        1
                     );
                     inventory.setItem(15, shuffledCard);
                     player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5F, 1.0F);
                  } else if (this.ticks == 12) {
                     ItemStack revealedCard = ItemStackGenerator.generateItemStack(
                        HigherLowerGame.getCardMaterial(nextCard),
                        ChatColorConverter.convert("&6&l" + HigherLowerGame.getCardName(nextCard)),
                        List.of("", GamblingConfig.getHigherLowerCardValueLabel() + nextCard),
                        1
                     );
                     inventory.setItem(15, revealedCard);
                  } else if (this.ticks == 20) {
                     if (playerWins) {
                        HigherLowerGame.showWinResult(player, session, inventory);
                     } else {
                        HigherLowerGame.showLoseResult(player, session, inventory);
                     }

                     this.cancel();
                  }
               } else {
                  this.cancel();
               }
            }
         })
         .runTaskTimer(MetadataHandler.PLUGIN, 0L, 2L);
   }

   private static void showWinResult(Player player, HigherLowerGame.HigherLowerSession session, Inventory inventory) {
      session.currentCard = session.nextCard;
      session.isProcessing = false;
      if (session.forcedCashOut) {
         GamblingDisplay.playBigWinSound(player);
         player.sendMessage(
            GamblingConfig.getHigherLowerMaxStreakChat().replace("%streak%", String.valueOf(12)).replace("%amount%", String.format("%.2f", session.winAmount))
         );
         showCashOutResult(inventory, session, session.winAmount);
      } else {
         player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.5F);
         player.sendMessage(
            GamblingConfig.getHigherLowerCorrectChat()
               .replace("%streak%", String.valueOf(session.streak))
               .replace("%multiplier%", String.format("%.2f", session.multiplier))
         );
         updateDisplay(inventory, session);
      }
   }

   private static void showLoseResult(Player player, HigherLowerGame.HigherLowerSession session, Inventory inventory) {
      ItemStack gameOver = ItemStackGenerator.generateItemStack(
         Material.REDSTONE_BLOCK,
         GamblingConfig.getHigherLowerGameOverTitle(),
         List.of(
            "",
            GamblingConfig.getHigherLowerLostCardLore() + getCardName(session.nextCard),
            GamblingConfig.getHigherLowerLostCoinsLore() + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord(),
            "",
            GamblingConfig.getHigherLowerCloseToContinue()
         ),
         1
      );
      inventory.setItem(13, gameOver);
      GamblingDisplay.playLoseSound(player);
      GamblingDisplay.sendLoseMessage(player, session.betAmount, GamblingConfig.getBettingHigherLowerName());
   }

   private static void showCashOutResult(Inventory inventory, HigherLowerGame.HigherLowerSession session, double payout) {
      ItemStack cashOutResult = ItemStackGenerator.generateItemStack(
         Material.EMERALD_BLOCK,
         GamblingConfig.getHigherLowerCashedOutTitle(),
         List.of(
            "",
            GamblingConfig.getHigherLowerCashedOutLore() + String.format("%.2f", payout) + " " + GamblingConfig.getGamblingCurrencyWord() + "!",
            GamblingConfig.getHigherLowerCashedOutStreakLore() + session.streak,
            "",
            GamblingConfig.getHigherLowerCloseToContinue()
         ),
         1
      );
      inventory.setItem(13, cashOutResult);
   }

   public static void shutdown() {
      for (HigherLowerGame.HigherLowerSession session : List.copyOf(activeSessions.values())) {
         closeSession(session);
         Player player = Bukkit.getPlayer(session.playerUUID);
         if (player != null && player.getOpenInventory().getTopInventory() == session.inventory) {
            player.closeInventory();
         }
      }
   }

   private static void closeSession(HigherLowerGame.HigherLowerSession session) {
      if (activeSessions.remove(session.playerUUID, session)) {
         HigherLowerGame.HigherLowerMenuEvents.menus.remove(session.inventory);
         double payout = 0.0;
         if (session.streak > 0 && !session.gameOver) {
            payout = GamblingEconomyHandler.calculatePayout(session.betAmount, session.multiplier);
         }

         try {
            double awarded = GamblingEconomyHandler.resolveOutcome(session.playerUUID, payout);
            Player player = Bukkit.getPlayer(session.playerUUID);
            if (awarded > 0.0 && player != null && player.isOnline()) {
               player.sendMessage(
                  GamblingConfig.getHigherLowerAutoCashOutChat()
                     .replace("%amount%", String.format("%.2f", awarded))
                     .replace("%streak%", String.valueOf(session.streak))
               );
            }
         } catch (RuntimeException var6) {
            Logger.warn("Gambling close could not settle " + session.playerUUID + ": " + var6);
         }
      }
   }

   public static class HigherLowerMenuEvents implements Listener {
      static final Set<Inventory> menus = new HashSet<>();

      @EventHandler
      public void onClick(InventoryClickEvent event) {
         Player player = GamblingDisplay.validateClick(event, menus);
         if (player != null) {
            HigherLowerGame.HigherLowerSession session = HigherLowerGame.activeSessions.get(player.getUniqueId());
            if (session != null) {
               int slot = event.getSlot();
               if (slot == 19 && !session.isProcessing && !session.gameOver) {
                  HigherLowerGame.processGuess(player, session, true, event.getInventory());
               } else if (slot == 25 && !session.isProcessing && !session.gameOver) {
                  HigherLowerGame.processGuess(player, session, false, event.getInventory());
               } else if (slot == 22 && session.streak > 0 && !session.gameOver) {
                  HigherLowerGame.cashOut(player, session, event.getInventory());
               }
            }
         }
      }

      @EventHandler
      public void onClose(InventoryCloseEvent event) {
         if (menus.contains(event.getInventory())) {
            HigherLowerGame.HigherLowerSession session = HigherLowerGame.activeSessions.get(event.getPlayer().getUniqueId());
            if (session != null && session.inventory == event.getInventory()) {
               HigherLowerGame.closeSession(session);
            }
         }
      }
   }

   private static class HigherLowerSession extends GamblingSession {
      int currentCard;
      int nextCard;
      int streak = 0;
      double multiplier = 1.0;
      boolean isProcessing = false;
      boolean lastGuessCorrect = false;
      boolean gameOver = false;
      boolean forcedCashOut = false;
      double winAmount = 0.0;

      HigherLowerSession(UUID playerUUID, int betAmount) {
         super(playerUUID, betAmount);
      }
   }
}
