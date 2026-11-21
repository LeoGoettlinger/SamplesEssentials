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

public class CoinFlipGame {
   private static final Map<UUID, CoinFlipGame.CoinFlipSession> activeSessions = new HashMap<>();
   private static final int COIN_SLOT = 4;
   private static final int HEADS_SLOT = 11;
   private static final int RESULT_SLOT = 13;
   private static final int TAILS_SLOT = 15;
   private static final int BET_INFO_SLOT = 22;

   public static void startGame(Player player, int betAmount) {
      CoinFlipGame.CoinFlipSession session = new CoinFlipGame.CoinFlipSession(player.getUniqueId(), betAmount);

      try {
         String title = GamblingConfig.getCoinFlipMenuTitle();
         Inventory inventory = Bukkit.createInventory(player, 27, title);
         session.inventory = inventory;
         activeSessions.put(player.getUniqueId(), session);
         setupInitialDisplay(inventory, session);
         CoinFlipGame.CoinFlipMenuEvents.menus.add(inventory);

         try {
            player.openInventory(inventory);
         } finally {
            if (player.getOpenInventory().getTopInventory() != inventory) {
               throw new IllegalStateException("Gambling inventory opening was cancelled");
            }
         }
      } catch (RuntimeException var11) {
         activeSessions.remove(session.playerUUID, session);
         CoinFlipGame.CoinFlipMenuEvents.menus.remove(session.inventory);

         try {
            GamblingEconomyHandler.resolveOutcome(session.playerUUID, session.betAmount);
         } catch (RuntimeException var9) {
            var11.addSuppressed(var9);
         }

         Logger.warn("Could not open gambling game for " + session.playerUUID + ": " + var11);
         player.sendMessage(EconomySettingsConfig.getShopTransactionFailedMessage());
      }
   }

   private static void setupInitialDisplay(Inventory inventory, CoinFlipGame.CoinFlipSession session) {
      ItemStack coin = ItemStackGenerator.generateItemStack(
         Material.GOLD_INGOT,
         GamblingConfig.getCoinFlipCoinTitle(),
         List.of("", GamblingConfig.getCoinFlipChooseLore(), GamblingConfig.getCoinFlipWillFlipLore()),
         1
      );
      inventory.setItem(4, coin);
      ItemStack headsButton = ItemStackGenerator.generateItemStack(
         Material.PLAYER_HEAD,
         GamblingConfig.getCoinFlipHeadsButtonText(),
         List.of(
            "",
            GamblingConfig.getCoinFlipHeadsBetLore(),
            ChatColorConverter.convert(
               GamblingConfig.getCoinFlipWinLore()
                  + String.format("%.2f", session.betAmount * GamblingConfig.getCoinFlipPayout())
                  + " "
                  + GamblingConfig.getGamblingCurrencyWord()
            )
         ),
         1
      );
      inventory.setItem(11, headsButton);
      ItemStack resultDisplay = ItemStackGenerator.generateItemStack(Material.GRAY_STAINED_GLASS_PANE, GamblingConfig.getCoinFlipMakeYourChoice(), List.of(), 1);
      inventory.setItem(13, resultDisplay);
      ItemStack tailsButton = ItemStackGenerator.generateItemStack(
         Material.SUNFLOWER,
         GamblingConfig.getCoinFlipTailsButtonText(),
         List.of(
            "",
            GamblingConfig.getCoinFlipTailsBetLore(),
            ChatColorConverter.convert(
               GamblingConfig.getCoinFlipWinLore()
                  + String.format("%.2f", session.betAmount * GamblingConfig.getCoinFlipPayout())
                  + " "
                  + GamblingConfig.getGamblingCurrencyWord()
            )
         ),
         1
      );
      inventory.setItem(15, tailsButton);
      ItemStack betInfo = ItemStackGenerator.generateItemStack(
         Material.EMERALD,
         GamblingConfig.getCoinFlipBetLabel() + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord(),
         List.of(
            "",
            GamblingConfig.getCoinFlipNormalWinLabel() + String.format("%.2f", session.betAmount * GamblingConfig.getCoinFlipPayout()),
            GamblingConfig.getCoinFlipPayoutLabel() + GamblingConfig.getCoinFlipPayout() + "x",
            "",
            GamblingConfig.getCoinFlipEdgeChanceLore(),
            ChatColorConverter.convert(
               GamblingConfig.getCoinFlipEdgePayoutLore()
                  + String.format("%.2f", session.betAmount * GamblingConfig.getCoinFlipEdgePayout())
                  + " "
                  + GamblingConfig.getGamblingCurrencyWord()
                  + "&7)"
            )
         ),
         1
      );
      inventory.setItem(22, betInfo);
   }

   private static void processChoice(Player player, CoinFlipGame.CoinFlipSession session, boolean playerChoseHeads, Inventory inventory) {
      if (!session.hasChosen) {
         session.hasChosen = true;
         double roll = ThreadLocalRandom.current().nextDouble();
         if (GamblingConfig.getCoinFlipEdgeChance() > 0.0 && roll < GamblingConfig.getCoinFlipEdgeChance()) {
            session.landedOnEdge = true;
            session.playerWon = true;
            double payout = GamblingEconomyHandler.calculatePayout(session.betAmount, GamblingConfig.getCoinFlipEdgePayout());
            session.winAmount = GamblingEconomyHandler.resolveOutcome(player.getUniqueId(), payout);
         } else {
            boolean coinIsHeads = ThreadLocalRandom.current().nextBoolean();
            boolean playerWins = coinIsHeads == playerChoseHeads;
            session.coinResult = coinIsHeads;
            session.playerWon = playerWins;
            if (playerWins) {
               double payout = GamblingEconomyHandler.calculatePayout(session.betAmount, GamblingConfig.getCoinFlipPayout());
               session.winAmount = GamblingEconomyHandler.resolveOutcome(player.getUniqueId(), payout);
            } else {
               GamblingEconomyHandler.resolveOutcome(player.getUniqueId(), 0.0);
            }
         }

         disableChoiceButtons(inventory);
         playCoinFlipAnimation(player, session, inventory);
      }
   }

   private static void disableChoiceButtons(Inventory inventory) {
      ItemStack disabled = ItemStackGenerator.generateItemStack(Material.GRAY_STAINED_GLASS_PANE, GamblingConfig.getCoinFlipFlipping(), List.of(), 1);
      inventory.setItem(11, disabled);
      inventory.setItem(15, disabled);
   }

   private static void playCoinFlipAnimation(final Player player, final CoinFlipGame.CoinFlipSession session, final Inventory inventory) {
      (new BukkitRunnable() {
            final int totalTicks = 30;
            int ticks = 0;
            boolean showingHeads = true;

            public void run() {
               if (player.isOnline() && CoinFlipGame.CoinFlipMenuEvents.menus.contains(inventory)) {
                  if (this.ticks < 30) {
                     Material coinMaterial = this.showingHeads ? Material.PLAYER_HEAD : Material.SUNFLOWER;
                     String coinName = this.showingHeads ? GamblingConfig.getCoinFlipHeadsText() : GamblingConfig.getCoinFlipTailsText();
                     ItemStack animatedCoin = ItemStackGenerator.generateItemStack(
                        coinMaterial, ChatColorConverter.convert(coinName), List.of(GamblingConfig.getCoinFlipFlipping()), 1
                     );
                     inventory.setItem(4, animatedCoin);
                     player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5F, 1.0F + this.ticks / 20.0F);
                     this.showingHeads = !this.showingHeads;
                     this.ticks += 2;
                  } else {
                     CoinFlipGame.showResult(player, session, inventory);
                     this.cancel();
                  }
               } else {
                  this.cancel();
               }
            }
         })
         .runTaskTimer(MetadataHandler.PLUGIN, 0L, 2L);
   }

   private static void showResult(Player player, CoinFlipGame.CoinFlipSession session, Inventory inventory) {
      if (session.landedOnEdge) {
         ItemStack edgeCoin = ItemStackGenerator.generateItemStack(
            Material.IRON_NUGGET, GamblingConfig.getCoinFlipEdgeCoinTitle(), List.of(GamblingConfig.getCoinFlipEdgeCoinLore()), 1
         );
         inventory.setItem(4, edgeCoin);
         ItemStack edgeResult = ItemStackGenerator.generateItemStack(
            Material.DIAMOND_BLOCK,
            GamblingConfig.getCoinFlipEdgeResultTitle(),
            List.of(
               "",
               GamblingConfig.getCoinFlipEdgeLandedLore(),
               GamblingConfig.getCoinFlipEdgeRareLore(),
               "",
               ChatColorConverter.convert(
                  GamblingConfig.getCoinFlipEdgeWonPrefix() + String.format("%.2f", session.winAmount) + " " + GamblingConfig.getGamblingCurrencyWord() + "&7!"
               ),
               GamblingConfig.getCoinFlipEdgePayoutMultiplier(),
               "",
               GamblingConfig.getCoinFlipCloseToContinue()
            ),
            1
         );
         inventory.setItem(13, edgeResult);
         GamblingDisplay.playBigWinSound(player);
         player.sendMessage(GamblingConfig.getCoinFlipEdgeChatMessage().replace("%amount%", String.format("%.2f", session.winAmount)));
         session.gameOver = true;
      } else {
         Material resultMaterial = session.coinResult ? Material.PLAYER_HEAD : Material.SUNFLOWER;
         String resultName = session.coinResult ? GamblingConfig.getCoinFlipHeadsText() : GamblingConfig.getCoinFlipTailsText();
         ItemStack finalCoin = ItemStackGenerator.generateItemStack(
            resultMaterial,
            ChatColorConverter.convert(resultName),
            List.of(
               GamblingConfig.getCoinFlipLandedOnLore()
                  + (session.coinResult ? GamblingConfig.getCoinFlipHeadsWord() : GamblingConfig.getCoinFlipTailsWord())
                  + "!"
            ),
            1
         );
         inventory.setItem(4, finalCoin);
         if (session.playerWon) {
            ItemStack winResult = ItemStackGenerator.generateItemStack(
               Material.EMERALD_BLOCK,
               GamblingConfig.getCoinFlipYouWin(),
               List.of(
                  "",
                  ChatColorConverter.convert(
                     GamblingConfig.getCoinFlipWonPrefix() + String.format("%.2f", session.winAmount) + " " + GamblingConfig.getGamblingCurrencyWord() + "&7!"
                  ),
                  "",
                  GamblingConfig.getCoinFlipCloseToContinue()
               ),
               1
            );
            inventory.setItem(13, winResult);
            GamblingDisplay.playWinSound(player);
            GamblingDisplay.sendWinMessage(player, session.winAmount, GamblingConfig.getBettingCoinFlipName());
         } else {
            ItemStack loseResult = ItemStackGenerator.generateItemStack(
               Material.REDSTONE_BLOCK,
               GamblingConfig.getCoinFlipYouLose(),
               List.of(
                  "",
                  ChatColorConverter.convert(
                     GamblingConfig.getCoinFlipLostPrefix() + session.betAmount + " " + GamblingConfig.getGamblingCurrencyWord() + "&7."
                  ),
                  "",
                  GamblingConfig.getCoinFlipCloseToContinue()
               ),
               1
            );
            inventory.setItem(13, loseResult);
            GamblingDisplay.playLoseSound(player);
            GamblingDisplay.sendLoseMessage(player, session.betAmount, GamblingConfig.getBettingCoinFlipName());
         }

         session.gameOver = true;
      }
   }

   public static void shutdown() {
      for (CoinFlipGame.CoinFlipSession session : List.copyOf(activeSessions.values())) {
         closeSession(session);
         Player player = Bukkit.getPlayer(session.playerUUID);
         if (player != null && player.getOpenInventory().getTopInventory() == session.inventory) {
            player.closeInventory();
         }
      }
   }

   private static void closeSession(CoinFlipGame.CoinFlipSession session) {
      if (activeSessions.remove(session.playerUUID, session)) {
         CoinFlipGame.CoinFlipMenuEvents.menus.remove(session.inventory);
         double payout = 0.0;

         try {
            GamblingEconomyHandler.resolveOutcome(session.playerUUID, payout);
         } catch (RuntimeException var4) {
            Logger.warn("Gambling close could not settle " + session.playerUUID + ": " + var4);
         }
      }
   }

   public static class CoinFlipMenuEvents implements Listener {
      static final Set<Inventory> menus = new HashSet<>();

      @EventHandler
      public void onClick(InventoryClickEvent event) {
         Player player = GamblingDisplay.validateClick(event, menus);
         if (player != null) {
            CoinFlipGame.CoinFlipSession session = CoinFlipGame.activeSessions.get(player.getUniqueId());
            if (session != null && session.inventory == event.getInventory() && !session.hasChosen) {
               int slot = event.getSlot();
               if (slot == 11) {
                  CoinFlipGame.processChoice(player, session, true, event.getInventory());
               } else if (slot == 15) {
                  CoinFlipGame.processChoice(player, session, false, event.getInventory());
               }
            }
         }
      }

      @EventHandler
      public void onClose(InventoryCloseEvent event) {
         if (menus.contains(event.getInventory())) {
            CoinFlipGame.CoinFlipSession session = CoinFlipGame.activeSessions.get(event.getPlayer().getUniqueId());
            if (session != null && session.inventory == event.getInventory()) {
               CoinFlipGame.closeSession(session);
            }
         }
      }
   }

   private static class CoinFlipSession extends GamblingSession {
      boolean hasChosen = false;
      boolean coinResult;
      boolean landedOnEdge = false;
      boolean playerWon;
      double winAmount;
      boolean gameOver = false;

      CoinFlipSession(UUID playerUUID, int betAmount) {
         super(playerUUID, betAmount);
      }
   }
}
