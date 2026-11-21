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

public class SlotMachineGame {
   private static final Map<UUID, SlotMachineGame.SlotSession> activeSessions = new HashMap<>();
   private static final int REEL_1_TOP = 3;
   private static final int REEL_1_MID = 12;
   private static final int REEL_1_BOT = 21;
   private static final int REEL_2_TOP = 4;
   private static final int REEL_2_MID = 13;
   private static final int REEL_2_BOT = 22;
   private static final int REEL_3_TOP = 5;
   private static final int REEL_3_MID = 14;
   private static final int REEL_3_BOT = 23;
   private static final int SPIN_BUTTON_SLOT = 31;
   private static final int BET_INFO_SLOT = 27;
   private static final int SPIN_AGAIN_SLOT = 30;
   private static final int PAYOUT_INFO_SLOT = 35;
   private static final int PAYLINE_LEFT = 11;
   private static final int PAYLINE_RIGHT = 15;
   private static final int TOTAL_WEIGHT;

   public static void startGame(Player player, int betAmount) {
      SlotMachineGame.SlotSession session = new SlotMachineGame.SlotSession(player.getUniqueId(), betAmount);

      try {
         String title = GamblingConfig.getSlotsMenuTitle();
         Inventory inventory = Bukkit.createInventory(player, 36, title);
         session.inventory = inventory;
         activeSessions.put(player.getUniqueId(), session);
         setupInitialDisplay(inventory, session);
         SlotMachineGame.SlotMachineMenuEvents.menus.add(inventory);

         try {
            player.openInventory(inventory);
         } finally {
            if (player.getOpenInventory().getTopInventory() != inventory) {
               throw new IllegalStateException("Gambling inventory opening was cancelled");
            }
         }
      } catch (RuntimeException var11) {
         activeSessions.remove(session.playerUUID, session);
         SlotMachineGame.SlotMachineMenuEvents.menus.remove(session.inventory);

         try {
            GamblingEconomyHandler.resolveOutcome(session.playerUUID, session.betAmount);
         } catch (RuntimeException var9) {
            var11.addSuppressed(var9);
         }

         Logger.warn("Could not open gambling game for " + session.playerUUID + ": " + var11);
         player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
      }
   }

   private static SlotMachineGame.Symbol getWeightedRandomSymbol() {
      int roll = ThreadLocalRandom.current().nextInt(TOTAL_WEIGHT);
      int cumulative = 0;

      for (SlotMachineGame.Symbol s : SlotMachineGame.Symbol.values()) {
         cumulative += s.weight;
         if (roll < cumulative) {
            return s;
         }
      }

      return SlotMachineGame.Symbol.CHERRY;
   }

   private static void setupInitialDisplay(Inventory inventory, SlotMachineGame.SlotSession session) {
      ItemStack glass = ItemStackGenerator.generateItemStack(Material.BLACK_STAINED_GLASS_PANE, " ", List.of(), 1);

      for (int i = 0; i < 36; i++) {
         inventory.setItem(i, glass);
      }

      ItemStack paylineIndicator = ItemStackGenerator.generateItemStack(Material.YELLOW_STAINED_GLASS_PANE, GamblingConfig.getSlotsPaylineLabel(), List.of(), 1);
      inventory.setItem(11, paylineIndicator);
      inventory.setItem(15, paylineIndicator);
      updateReelDisplay(inventory, 3, 12, 21, getWeightedRandomSymbol(), getWeightedRandomSymbol(), getWeightedRandomSymbol());
      updateReelDisplay(inventory, 4, 13, 22, getWeightedRandomSymbol(), getWeightedRandomSymbol(), getWeightedRandomSymbol());
      updateReelDisplay(inventory, 5, 14, 23, getWeightedRandomSymbol(), getWeightedRandomSymbol(), getWeightedRandomSymbol());
      ItemStack spinButton = ItemStackGenerator.generateItemStack(
         Material.LIME_CONCRETE,
         GamblingConfig.getSlotsSpinButtonText(),
         List.of(
            "",
            GamblingConfig.getSlotsSpinClickLore(),
            GamblingConfig.getSlotsSpinBetLore() + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord()
         ),
         1
      );
      inventory.setItem(31, spinButton);
      ItemStack betInfo = ItemStackGenerator.generateItemStack(
         Material.EMERALD,
         GamblingConfig.getSlotsBetLabel() + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord(),
         List.of("", GamblingConfig.getSlotsMatch3Lore(), GamblingConfig.getSlotsMatch2Lore()),
         1
      );
      inventory.setItem(27, betInfo);
      List<String> payoutLore = new ArrayList<>();
      payoutLore.add("");
      payoutLore.add(GamblingConfig.getSlotsPayoutsTitle());
      payoutLore.add("");

      for (SlotMachineGame.Symbol symbol : SlotMachineGame.Symbol.values()) {
         if (symbol.getChancePercent() >= 20.0) {
            String var10000 = "&7";
         } else if (symbol.getChancePercent() >= 10.0) {
            String var16 = "&e";
         } else {
            String var17 = symbol.getChancePercent() >= 5.0 ? "&6" : "&b";
         }

         String multiplier = String.format("%.1f", symbol.getPayoutMultiplier());
         String twoMatch = String.format("%.2f", symbol.getPayoutMultiplier() * 0.25);
         payoutLore.add(
            ChatColorConverter.convert(
               GamblingConfig.getSlotsPayoutSymbolFormat()
                  .replace("$symbol", symbol.displayName)
                  .replace("$multiplier", multiplier)
                  .replace("$twoMatch", twoMatch)
            )
         );
      }

      payoutLore.add("");
      payoutLore.add(GamblingConfig.getSlotsChanceRarityNote());
      ItemStack payoutInfo = ItemStackGenerator.generateItemStack(Material.BOOK, GamblingConfig.getSlotsPayoutTableTitle(), payoutLore, 1);
      inventory.setItem(35, payoutInfo);
   }

   private static void updateReelDisplay(
      Inventory inventory, int topSlot, int midSlot, int botSlot, SlotMachineGame.Symbol top, SlotMachineGame.Symbol mid, SlotMachineGame.Symbol bot
   ) {
      inventory.setItem(topSlot, createGreyedSymbol(top));
      inventory.setItem(midSlot, mid.createItemStack());
      inventory.setItem(botSlot, createGreyedSymbol(bot));
   }

   private static ItemStack createGreyedSymbol(SlotMachineGame.Symbol symbol) {
      return ItemStackGenerator.generateItemStack(symbol.material, ChatColorConverter.convert("&8" + symbol.displayName.substring(2)), List.of(), 1);
   }

   private static void processSpin(Player player, SlotMachineGame.SlotSession session, Inventory inventory) {
      if (!session.isSpinning && !session.gameOver) {
         session.isSpinning = true;
         SlotMachineGame.Symbol[] results = new SlotMachineGame.Symbol[]{getWeightedRandomSymbol(), getWeightedRandomSymbol(), getWeightedRandomSymbol()};
         session.results = results;
         double payout = calculatePayout(results, session.betAmount);
         session.winAmount = payout;
         session.playerWon = payout > 0.0;
         session.winAmount = GamblingEconomyHandler.resolveOutcome(player.getUniqueId(), payout);
         disableSpinButton(inventory);
         playSpinAnimation(player, session, inventory, results, session.winAmount);
      }
   }

   private static double calculatePayout(SlotMachineGame.Symbol[] results, int betAmount) {
      if (results[0] == results[1] && results[1] == results[2]) {
         return betAmount * results[0].getPayoutMultiplier();
      } else {
         SlotMachineGame.Symbol matchedSymbol = null;
         if (results[0] == results[1]) {
            matchedSymbol = results[0];
         } else if (results[1] == results[2]) {
            matchedSymbol = results[1];
         } else if (results[0] == results[2]) {
            matchedSymbol = results[0];
         }

         return matchedSymbol != null ? betAmount * matchedSymbol.getPayoutMultiplier() * 0.25 : 0.0;
      }
   }

   private static void disableSpinButton(Inventory inventory) {
      ItemStack disabled = ItemStackGenerator.generateItemStack(Material.GRAY_CONCRETE, GamblingConfig.getSlotsSpinning(), List.of(), 1);
      inventory.setItem(31, disabled);
   }

   private static void playSpinAnimation(
      final Player player,
      final SlotMachineGame.SlotSession session,
      final Inventory inventory,
      final SlotMachineGame.Symbol[] finalResults,
      final double payout
   ) {
      (new BukkitRunnable() {
         final int REEL_1_STOP = 15;
         final int REEL_2_STOP = 25;
         final int REEL_3_STOP = 35;
         final int ANIMATION_END = 45;
         int ticks = 0;

         public void run() {
            if (player.isOnline() && SlotMachineGame.SlotMachineMenuEvents.menus.contains(inventory)) {
               this.ticks++;
               if (this.ticks <= 15) {
                  SlotMachineGame.Symbol top = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.Symbol mid = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.Symbol bot = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.updateReelDisplay(inventory, 3, 12, 21, top, mid, bot);
                  player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.3F, 1.0F);
               } else if (this.ticks == 16) {
                  SlotMachineGame.Symbol above = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.Symbol below = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.updateReelDisplay(inventory, 3, 12, 21, above, finalResults[0], below);
                  player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
               }

               if (this.ticks <= 25 && this.ticks > 5) {
                  SlotMachineGame.Symbol top = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.Symbol mid = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.Symbol bot = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.updateReelDisplay(inventory, 4, 13, 22, top, mid, bot);
               } else if (this.ticks == 26) {
                  SlotMachineGame.Symbol above = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.Symbol below = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.updateReelDisplay(inventory, 4, 13, 22, above, finalResults[1], below);
                  player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
               }

               if (this.ticks <= 35 && this.ticks > 10) {
                  SlotMachineGame.Symbol top = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.Symbol mid = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.Symbol bot = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.updateReelDisplay(inventory, 5, 14, 23, top, mid, bot);
               } else if (this.ticks == 36) {
                  SlotMachineGame.Symbol above = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.Symbol below = SlotMachineGame.getWeightedRandomSymbol();
                  SlotMachineGame.updateReelDisplay(inventory, 5, 14, 23, above, finalResults[2], below);
                  player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
               }

               if (this.ticks >= 45) {
                  SlotMachineGame.showResult(player, session, inventory, payout);
                  this.cancel();
               }
            } else {
               this.cancel();
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 2L);
   }

   private static void showResult(Player player, SlotMachineGame.SlotSession session, Inventory inventory, double payout) {
      session.isSpinning = false;
      session.gameOver = true;
      if (payout > 0.0) {
         boolean isJackpot = session.results[0] == SlotMachineGame.Symbol.SEVEN
            && session.results[1] == SlotMachineGame.Symbol.SEVEN
            && session.results[2] == SlotMachineGame.Symbol.SEVEN;
         boolean isThreeMatch = session.results[0] == session.results[1] && session.results[1] == session.results[2];
         String resultTitle;
         Material resultMaterial;
         Sound resultSound;
         float pitch;
         if (isJackpot) {
            resultTitle = GamblingConfig.getSlotsJackpotTitle();
            resultMaterial = Material.DIAMOND_BLOCK;
            resultSound = Sound.UI_TOAST_CHALLENGE_COMPLETE;
            pitch = 1.0F;
         } else if (isThreeMatch) {
            resultTitle = GamblingConfig.getSlotsBigWinTitle();
            resultMaterial = Material.EMERALD_BLOCK;
            resultSound = Sound.ENTITY_PLAYER_LEVELUP;
            pitch = 1.2F;
         } else {
            resultTitle = GamblingConfig.getSlotsSmallWinTitle();
            resultMaterial = Material.GOLD_BLOCK;
            resultSound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
            pitch = 1.5F;
         }

         ItemStack resultItem = ItemStackGenerator.generateItemStack(
            resultMaterial,
            resultTitle,
            List.of(
               "",
               ChatColorConverter.convert(
                  GamblingConfig.getSlotsWonPrefix() + String.format("%.2f", payout) + " " + GamblingConfig.getGamblingCurrencyWord() + "&7!"
               ),
               "",
               GamblingConfig.getSlotsCloseToContinue()
            ),
            1
         );
         inventory.setItem(31, resultItem);
         player.playSound(player.getLocation(), resultSound, 1.0F, pitch);
         GamblingDisplay.sendWinMessage(player, payout, GamblingConfig.getBettingSlotsName());
      } else {
         ItemStack loseItem = ItemStackGenerator.generateItemStack(
            Material.REDSTONE_BLOCK,
            GamblingConfig.getSlotsNoMatchTitle(),
            List.of(
               "",
               ChatColorConverter.convert(GamblingConfig.getSlotsLostPrefix() + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord() + "&7."),
               "",
               GamblingConfig.getSlotsCloseToContinue()
            ),
            1
         );
         inventory.setItem(31, loseItem);
         GamblingDisplay.playLoseSound(player);
         GamblingDisplay.sendLoseMessage(player, session.betAmount, GamblingConfig.getBettingSlotsName());
      }

      if (GamblingEconomyHandler.canAffordBet(player.getUniqueId(), session.betAmount)) {
         ItemStack spinAgainButton = ItemStackGenerator.generateItemStack(
            Material.LIME_CONCRETE,
            ChatColorConverter.convert("&a&lSPIN AGAIN"),
            List.of(
               "",
               ChatColorConverter.convert("&7Click to spin again!"),
               ChatColorConverter.convert("&7Bet: &6" + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord())
            ),
            1
         );
         inventory.setItem(30, spinAgainButton);
      }
   }

   public static void shutdown() {
      for (SlotMachineGame.SlotSession session : List.copyOf(activeSessions.values())) {
         closeSession(session);
         Player player = Bukkit.getPlayer(session.playerUUID);
         if (player != null && player.getOpenInventory().getTopInventory() == session.inventory) {
            player.closeInventory();
         }
      }
   }

   private static void closeSession(SlotMachineGame.SlotSession session) {
      if (activeSessions.remove(session.playerUUID, session)) {
         SlotMachineGame.SlotMachineMenuEvents.menus.remove(session.inventory);
         double payout = 0.0;

         try {
            GamblingEconomyHandler.resolveOutcome(session.playerUUID, payout);
         } catch (RuntimeException var4) {
            Logger.warn("Gambling close could not settle " + session.playerUUID + ": " + var4);
         }
      }
   }

   static {
      int total = 0;

      for (SlotMachineGame.Symbol s : SlotMachineGame.Symbol.values()) {
         total += s.weight;
      }

      TOTAL_WEIGHT = total;
   }

   public static class SlotMachineMenuEvents implements Listener {
      static final Set<Inventory> menus = new HashSet<>();

      @EventHandler
      public void onClick(InventoryClickEvent event) {
         Player player = GamblingDisplay.validateClick(event, menus);
         if (player != null) {
            SlotMachineGame.SlotSession session = SlotMachineGame.activeSessions.get(player.getUniqueId());
            if (session != null && session.inventory == event.getInventory() && !session.isSpinning) {
               if (event.getSlot() == 31 && !session.gameOver) {
                  SlotMachineGame.processSpin(player, session, event.getInventory());
               } else if (event.getSlot() == 30 && session.gameOver) {
                  if (!GamblingEconomyHandler.canAffordBet(player.getUniqueId(), session.betAmount)) {
                     player.sendMessage(GamblingConfig.getInsufficientFundsMessage());
                     return;
                  }

                  if (!GamblingEconomyHandler.placeBet(player.getUniqueId(), session.betAmount)) {
                     player.sendMessage(GamblingConfig.getInsufficientFundsMessage());
                     return;
                  }

                  session.gameOver = false;
                  session.isSpinning = false;
                  session.results = null;
                  session.playerWon = false;
                  session.winAmount = 0.0;
                  SlotMachineGame.setupInitialDisplay(event.getInventory(), session);
               }
            }
         }
      }

      @EventHandler
      public void onClose(InventoryCloseEvent event) {
         if (menus.contains(event.getInventory())) {
            SlotMachineGame.SlotSession session = SlotMachineGame.activeSessions.get(event.getPlayer().getUniqueId());
            if (session != null && session.inventory == event.getInventory()) {
               SlotMachineGame.closeSession(session);
            }
         }
      }
   }

   private static class SlotSession extends GamblingSession {
      SlotMachineGame.Symbol[] results;
      boolean isSpinning = false;
      boolean playerWon = false;
      double winAmount = 0.0;
      boolean gameOver = false;

      SlotSession(UUID playerUUID, int betAmount) {
         super(playerUUID, betAmount);
      }
   }

   private static enum Symbol {
      CHERRY(Material.SWEET_BERRIES, "&cCherry", 35),
      LEMON(Material.MELON_SLICE, "&eLemon", 28),
      ORANGE(Material.PUMPKIN, "&6Orange", 20),
      BELL(Material.BELL, "&eGolden Bell", 10),
      BAR(Material.IRON_INGOT, "&fSilver Bar", 5),
      SEVEN(Material.DIAMOND, "&b&lLucky 7", 2);

      private final Material material;
      private final String displayName;
      private final int weight;

      private Symbol(Material material, String displayName, int weight) {
         this.material = material;
         this.displayName = displayName;
         this.weight = weight;
      }

      public double getPayoutMultiplier() {
         return (double)SlotMachineGame.TOTAL_WEIGHT / this.weight;
      }

      public double getChancePercent() {
         return this.weight * 100.0 / SlotMachineGame.TOTAL_WEIGHT;
      }

      public ItemStack createItemStack() {
         return ItemStackGenerator.generateItemStack(this.material, ChatColorConverter.convert(this.displayName), List.of(), 1);
      }
   }
}
