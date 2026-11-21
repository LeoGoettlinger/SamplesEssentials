package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.magmacore.config.ConfigurationFile;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;
import lombok.Generated;

public class GamblingConfig extends ConfigurationFile {
   private static boolean gamblingEnabled;
   private static int minBet;
   private static double debtCollectorSpawnChance;
   private static int debtCollectorCheckIntervalMinutes;
   private static int debtCollectorTimeoutSeconds;
   private static double debtReductionOnPlayerDeath;
   private static double blackjackPayoutNormal;
   private static double blackjackPayoutBlackjack;
   private static double coinFlipPayout;
   private static double coinFlipEdgeChance;
   private static double coinFlipEdgePayout;
   private static double higherLowerMultiplier;
   private static String insufficientFundsMessage;
   private static String winMessage;
   private static String loseMessage;
   private static String debtWarningMessage;
   private static String betPlacedMessage;
   private static String bettingMenuTitle;
   private static String playButtonText;
   private static String cancelButtonText;
   private static String blackjackMenuTitle;
   private static String coinFlipMenuTitle;
   private static String slotsMenuTitle;
   private static String higherLowerMenuTitle;
   private static String blackjackHitButtonText;
   private static String blackjackHitButtonLore;
   private static String blackjackStandButtonText;
   private static String blackjackStandButtonLore;
   private static String blackjackDoubleDownButtonText;
   private static String blackjackDoubleDownButtonLore;
   private static String blackjackDoubleDownDisabledText;
   private static String blackjackDoubleDownCantAffordLore;
   private static String blackjackDealerLabel;
   private static String blackjackPlayerLabel;
   private static String blackjackBetLabel;
   private static String blackjackWinPayoutLabel;
   private static String blackjackBlackjackPayoutLabel;
   private static String blackjackCardValueLabel;
   private static String blackjackHiddenCardText;
   private static String blackjackHiddenCardLore;
   private static String blackjackResultBlackjack;
   private static String blackjackResultYouWin;
   private static String blackjackResultDealerBusts;
   private static String blackjackResultPush;
   private static String blackjackResultBust;
   private static String blackjackResultDealerWins;
   private static String blackjackResultGameOver;
   private static String blackjackResultPlayerLabel;
   private static String blackjackResultDealerLabel;
   private static String blackjackResultWonPrefix;
   private static String blackjackResultWonSuffix;
   private static String blackjackResultBetReturned;
   private static String blackjackResultLostPrefix;
   private static String blackjackResultCloseToContinue;
   private static String blackjackCardSuitSpades;
   private static String blackjackCardSuitHearts;
   private static String blackjackCardSuitDiamonds;
   private static String blackjackCardSuitClubs;
   private static String blackjackCardOfText;
   private static String coinFlipCoinTitle;
   private static String coinFlipChooseLore;
   private static String coinFlipWillFlipLore;
   private static String coinFlipHeadsButtonText;
   private static String coinFlipHeadsBetLore;
   private static String coinFlipTailsButtonText;
   private static String coinFlipTailsBetLore;
   private static String coinFlipMakeYourChoice;
   private static String coinFlipBetLabel;
   private static String coinFlipNormalWinLabel;
   private static String coinFlipPayoutLabel;
   private static String coinFlipEdgeChanceLore;
   private static String coinFlipEdgePayoutLore;
   private static String coinFlipFlipping;
   private static String coinFlipHeadsText;
   private static String coinFlipTailsText;
   private static String coinFlipEdgeCoinTitle;
   private static String coinFlipEdgeCoinLore;
   private static String coinFlipEdgeResultTitle;
   private static String coinFlipEdgeLandedLore;
   private static String coinFlipEdgeRareLore;
   private static String coinFlipEdgeWonPrefix;
   private static String coinFlipEdgePayoutMultiplier;
   private static String coinFlipEdgeChatMessage;
   private static String coinFlipLandedOnLore;
   private static String coinFlipHeadsWord;
   private static String coinFlipTailsWord;
   private static String coinFlipYouWin;
   private static String coinFlipWonPrefix;
   private static String coinFlipCloseToContinue;
   private static String coinFlipYouLose;
   private static String coinFlipLostPrefix;
   private static String slotsPaylineLabel;
   private static String slotsSpinButtonText;
   private static String slotsSpinClickLore;
   private static String slotsSpinBetLore;
   private static String slotsBetLabel;
   private static String slotsMatch3Lore;
   private static String slotsMatch2Lore;
   private static String slotsPayoutsTitle;
   private static String slotsPayoutTableTitle;
   private static String slotsChanceRarityNote;
   private static String slotsSpinning;
   private static String slotsJackpotTitle;
   private static String slotsBigWinTitle;
   private static String slotsSmallWinTitle;
   private static String slotsWonPrefix;
   private static String slotsCloseToContinue;
   private static String slotsNoMatchTitle;
   private static String slotsLostPrefix;
   private static String higherLowerCardValueLabel;
   private static String higherLowerNextCardPrompt;
   private static String higherLowerHiddenCardText;
   private static String higherLowerMakeGuessLore;
   private static String higherLowerHigherButtonText;
   private static String higherLowerHigherLore1;
   private static String higherLowerHigherLore2;
   private static String higherLowerCashOutButtonText;
   private static String higherLowerCurrentWinningsLabel;
   private static String higherLowerCashOutClickLore;
   private static String higherLowerCashOutDisabledText;
   private static String higherLowerCashOutWinOnceLore;
   private static String higherLowerLowerButtonText;
   private static String higherLowerLowerLore1;
   private static String higherLowerLowerLore2;
   private static String higherLowerStreakLabel;
   private static String higherLowerMultiplierLabel;
   private static String higherLowerPotentialWinLabel;
   private static String higherLowerMaxStreakLabel;
   private static String higherLowerBetLabel;
   private static String higherLowerEachGuessLore;
   private static String higherLowerWait;
   private static String higherLowerRevealing;
   private static String higherLowerMaxStreakChat;
   private static String higherLowerCorrectChat;
   private static String higherLowerGameOverTitle;
   private static String higherLowerLostCardLore;
   private static String higherLowerLostCoinsLore;
   private static String higherLowerCloseToContinue;
   private static String higherLowerCashedOutTitle;
   private static String higherLowerCashedOutLore;
   private static String higherLowerCashedOutStreakLore;
   private static String higherLowerAutoCashOutChat;
   private static String debtCollectorSpawnMessage;
   private static String debtCollectorTimeoutMessage;
   private static String debtCollectorDeathMessage;
   private static String debtCollectorKillMessage;
   private static String debtPaidMessage;
   private static String debtClearedMessage;
   private static String debtAutoCollectedMessage;
   private static String debtAutoClearedMessage;
   private static String bettingCasinoClosedMessage;
   private static String bettingSelectBetLore;
   private static String bettingYourFinancesTitle;
   private static String bettingBalanceLabel;
   private static String bettingDebtLabel;
   private static String bettingAvailableCreditLabel;
   private static String bettingCurrentBetPrefix;
   private static String bettingClickPlayLore;
   private static String bettingCantAffordLore;
   private static String bettingAdjustBetLore;
   private static String bettingAllInButtonText;
   private static String bettingAllInLore;
   private static String bettingMaxAvailableLabel;
   private static String bettingResetButtonText;
   private static String bettingResetLore;
   private static String bettingCancelLore;
   private static String bettingCantAffordButtonText;
   private static String bettingStartGameLore;
   private static String bettingReduceBetLore;
   private static String bettingBlackjackName;
   private static String bettingBlackjackDescription;
   private static String bettingCoinFlipName;
   private static String bettingCoinFlipDescription;
   private static String bettingSlotsName;
   private static String bettingSlotsDescription;
   private static String bettingHigherLowerName;
   private static String bettingHigherLowerDescription;
   private static String gamblingCurrencyWord;
   private static String coinFlipWinLore;
   private static String slotsPayoutSymbolFormat;
   private static String higherLowerAutoCashoutSuffix;
   private static String bettingAffordableColor;
   private static String bettingUnaffordableColor;
   private static String houseEarningsLabel;

   public GamblingConfig() {
      super("GamblingSettings.yml");
   }

   private boolean hasScalarCoinFlipSettings() {
      return this.fileConfiguration.contains("payouts.coinFlip") && !this.fileConfiguration.isConfigurationSection("payouts.coinFlip");
   }

   @Override
   public void saveDefaults() {
      if (!this.hasScalarCoinFlipSettings()) {
         super.saveDefaults();
      }
   }

   @Override
   public void initializeValues() {
      gamblingEnabled = ConfigurationEngine.setBoolean(
         List.of("Enables or disables the gambling system entirely.", "When disabled, gambling NPCs will not work and the Debt Collector will not spawn."),
         this.fileConfiguration,
         "gamblingEnabled",
         true
      );
      minBet = ConfigurationEngine.setInt(List.of("Minimum bet amount for all gambling games."), this.fileConfiguration, "minBet", 10);
      minBet = this.requirePositiveInt("minBet", minBet, 10);
      debtCollectorSpawnChance = ConfigurationEngine.setDouble(
         List.of("Chance (0.0 to 1.0) that the Debt Collector will spawn during each check.", "0.5 = 50% chance each check."),
         this.fileConfiguration,
         "debtCollector.spawnChance",
         0.5
      );
      debtCollectorSpawnChance = this.requireProbability("debtCollector.spawnChance", debtCollectorSpawnChance, 0.5);
      debtCollectorCheckIntervalMinutes = ConfigurationEngine.setInt(
         List.of("How often (in minutes) to check if the Debt Collector should spawn for players in debt."),
         this.fileConfiguration,
         "debtCollector.checkIntervalMinutes",
         60
      );
      debtCollectorCheckIntervalMinutes = this.requirePositiveInt("debtCollector.checkIntervalMinutes", debtCollectorCheckIntervalMinutes, 60);
      debtCollectorTimeoutSeconds = ConfigurationEngine.setInt(
         List.of("How long (in seconds) before the Debt Collector despawns if not killed."), this.fileConfiguration, "debtCollector.timeoutSeconds", 600
      );
      debtCollectorTimeoutSeconds = this.requirePositiveInt("debtCollector.timeoutSeconds", debtCollectorTimeoutSeconds, 600);
      debtReductionOnPlayerDeath = ConfigurationEngine.setDouble(
         List.of("Amount of debt reduced when the Debt Collector kills a player.", "The player 'paid' with their life."),
         this.fileConfiguration,
         "debtCollector.debtReductionOnPlayerDeath",
         50.0
      );
      debtReductionOnPlayerDeath = this.requireNonNegativeFinite("debtCollector.debtReductionOnPlayerDeath", debtReductionOnPlayerDeath, 50.0);
      blackjackPayoutNormal = ConfigurationEngine.setDouble(
         List.of("Payout multiplier for a normal blackjack win (not a blackjack)."), this.fileConfiguration, "payouts.blackjack.normal", 2.0
      );
      blackjackPayoutNormal = this.requireNonNegativeFinite("payouts.blackjack.normal", blackjackPayoutNormal, 2.0);
      blackjackPayoutBlackjack = ConfigurationEngine.setDouble(
         List.of("Payout multiplier for getting a blackjack (21 with first two cards)."), this.fileConfiguration, "payouts.blackjack.blackjack", 2.5
      );
      blackjackPayoutBlackjack = this.requireNonNegativeFinite("payouts.blackjack.blackjack", blackjackPayoutBlackjack, 2.5);
      if (this.hasScalarCoinFlipSettings()) {
         gamblingEnabled = false;
         coinFlipPayout = 1.9;
         coinFlipEdgeChance = 0.01;
         coinFlipEdgePayout = 5.0;
         Logger.warn(
            "GamblingSettings.yml: replace the scalar payouts.coinFlip with a section containing normal: "
               + this.fileConfiguration.get("payouts.coinFlip")
               + ", edgeChance: 0.01 and edgePayout: 5.0. Gambling is disabled until corrected; the file was not changed."
         );
      } else {
         coinFlipPayout = ConfigurationEngine.setDouble(
            List.of("Payout multiplier for winning a coin flip.", "Set below 2.0 for house edge (1.9 = 5% house edge)."),
            this.fileConfiguration,
            "payouts.coinFlip.normal",
            1.9
         );
         coinFlipPayout = this.requireNonNegativeFinite("payouts.coinFlip.normal", coinFlipPayout, 1.9);
         coinFlipEdgeChance = ConfigurationEngine.setDouble(
            List.of("Chance (0.0 to 1.0) for the coin to land on its edge (bonus win).", "Default 0.01 = 1% chance. Set to 0.0 to disable the edge mechanic."),
            this.fileConfiguration,
            "payouts.coinFlip.edgeChance",
            0.01
         );
         coinFlipEdgeChance = this.requireProbability("payouts.coinFlip.edgeChance", coinFlipEdgeChance, 0.01);
         coinFlipEdgePayout = ConfigurationEngine.setDouble(
            List.of(
               "Payout multiplier when the coin lands on its edge.",
               "Combined with the edge chance and normal payout, this determines the overall house edge.",
               "With 1% edge chance and 1.9x normal payout: 5.0x edge = ~1% house edge, 10.0x edge = ~4% player edge."
            ),
            this.fileConfiguration,
            "payouts.coinFlip.edgePayout",
            5.0
         );
         coinFlipEdgePayout = this.requireNonNegativeFinite("payouts.coinFlip.edgePayout", coinFlipEdgePayout, 5.0);
      }

      higherLowerMultiplier = ConfigurationEngine.setDouble(
         List.of(
            "Multiplier applied to the bet for each correct guess in Higher/Lower.",
            "This stacks: 1st correct = 1.3x, 2nd = 1.69x, 3rd = 2.197x, etc.",
            "With ~71% average win rate per guess: 1.3x = ~7.7% house edge, 1.5x = ~6.5% player edge."
         ),
         this.fileConfiguration,
         "payouts.higherLower.multiplierV2",
         1.3
      );
      higherLowerMultiplier = this.requireNonNegativeFinite("payouts.higherLower.multiplierV2", higherLowerMultiplier, 1.3);
      insufficientFundsMessage = ConfigurationEngine.setString(
         List.of("Message shown when a player tries to bet more than they can afford."),
         this.file,
         this.fileConfiguration,
         "messages.insufficientFunds",
         "&c[Casino] You can't afford that bet!",
         true
      );
      winMessage = ConfigurationEngine.setString(
         List.of("Message shown when a player wins a gambling game.", "Use %amount% for the win amount, %game% for the game name."),
         this.file,
         this.fileConfiguration,
         "messages.win",
         "&a[%game%] Congratulations! You won &6%amount% coins&a!",
         true
      );
      loseMessage = ConfigurationEngine.setString(
         List.of("Message shown when a player loses a gambling game.", "Use %amount% for the lost amount, %game% for the game name."),
         this.file,
         this.fileConfiguration,
         "messages.lose",
         "&c[%game%] Better luck next time. You lost &6%amount% coins&c.",
         true
      );
      debtWarningMessage = ConfigurationEngine.setString(
         List.of("Message shown when a player goes into debt.", "Use %debt% for the current debt amount."),
         this.file,
         this.fileConfiguration,
         "messages.debtWarning",
         "&c[Casino] Warning: You are now &6%debt% coins &cin debt! The Debt Collector is watching...",
         true
      );
      betPlacedMessage = ConfigurationEngine.setString(
         List.of("Message shown when a bet is placed.", "Use %amount% for the bet amount."),
         this.file,
         this.fileConfiguration,
         "messages.betPlaced",
         "&7[Casino] Bet placed: &6%amount% coins",
         true
      );
      bettingMenuTitle = ConfigurationEngine.setString(
         List.of("Title for the betting amount selection menu.", "Use %game% for the game name."),
         this.file,
         this.fileConfiguration,
         "menus.bettingTitle",
         "&6&lCasino - %game%",
         true
      );
      playButtonText = ConfigurationEngine.setString(
         List.of("Text for the play button in the betting menu."), this.file, this.fileConfiguration, "menus.playButton", "&a&lPLAY!", true
      );
      cancelButtonText = ConfigurationEngine.setString(
         List.of("Text for the cancel button in the betting menu."), this.file, this.fileConfiguration, "menus.cancelButton", "&c&lCancel", true
      );
      blackjackMenuTitle = ConfigurationEngine.setString(
         List.of("Title for the blackjack game menu."), this.file, this.fileConfiguration, "menus.blackjackTitle", "&6&lBlackjack", true
      );
      coinFlipMenuTitle = ConfigurationEngine.setString(
         List.of("Title for the coin flip game menu."), this.file, this.fileConfiguration, "menus.coinFlipTitle", "&e&lCoin Flip", true
      );
      slotsMenuTitle = ConfigurationEngine.setString(
         List.of("Title for the slot machine game menu."), this.file, this.fileConfiguration, "menus.slotsTitle", "&d&lSlot Machine", true
      );
      higherLowerMenuTitle = ConfigurationEngine.setString(
         List.of("Title for the higher/lower game menu."), this.file, this.fileConfiguration, "menus.higherLowerTitle", "&b&lHigher or Lower", true
      );
      blackjackHitButtonText = ConfigurationEngine.setString(
         List.of("Text for the Hit button in Blackjack."), this.file, this.fileConfiguration, "blackjack.hitButton", "&a&lHIT", true
      );
      blackjackHitButtonLore = ConfigurationEngine.setString(
         List.of("Lore for the Hit button in Blackjack."), this.file, this.fileConfiguration, "blackjack.hitButtonLore", "&7Draw another card", true
      );
      blackjackStandButtonText = ConfigurationEngine.setString(
         List.of("Text for the Stand button in Blackjack."), this.file, this.fileConfiguration, "blackjack.standButton", "&e&lSTAND", true
      );
      blackjackStandButtonLore = ConfigurationEngine.setString(
         List.of("Lore for the Stand button in Blackjack."), this.file, this.fileConfiguration, "blackjack.standButtonLore", "&7Keep your current hand", true
      );
      blackjackDoubleDownButtonText = ConfigurationEngine.setString(
         List.of("Text for the Double Down button in Blackjack when the player can afford it."),
         this.file,
         this.fileConfiguration,
         "blackjack.doubleDownButton",
         "&6&lDOUBLE DOWN",
         true
      );
      blackjackDoubleDownButtonLore = ConfigurationEngine.setString(
         List.of("Lore for the Double Down button in Blackjack."),
         this.file,
         this.fileConfiguration,
         "blackjack.doubleDownButtonLore",
         "&7Double your bet, take one card, then stand",
         true
      );
      blackjackDoubleDownDisabledText = ConfigurationEngine.setString(
         List.of("Text for the Double Down button when the player cannot afford it."),
         this.file,
         this.fileConfiguration,
         "blackjack.doubleDownDisabled",
         "&7Double Down",
         true
      );
      blackjackDoubleDownCantAffordLore = ConfigurationEngine.setString(
         List.of("Lore shown when the player can't afford to double down."),
         this.file,
         this.fileConfiguration,
         "blackjack.doubleDownCantAffordLore",
         "&cYou can't afford to double!",
         true
      );
      blackjackDealerLabel = ConfigurationEngine.setString(
         List.of("Label for the dealer's total display. The value is appended after this."),
         this.file,
         this.fileConfiguration,
         "blackjack.dealerLabel",
         "&cDealer: ",
         true
      );
      blackjackPlayerLabel = ConfigurationEngine.setString(
         List.of("Label for the player's total display. The value is appended after this."),
         this.file,
         this.fileConfiguration,
         "blackjack.playerLabel",
         "&aYou: ",
         true
      );
      blackjackBetLabel = ConfigurationEngine.setString(
         List.of("Label for the bet info display. The bet amount is appended after this."),
         this.file,
         this.fileConfiguration,
         "blackjack.betLabel",
         "&aBet: ",
         true
      );
      blackjackWinPayoutLabel = ConfigurationEngine.setString(
         List.of("Label for the normal win payout in the bet info display."), this.file, this.fileConfiguration, "blackjack.winPayoutLabel", "&7Win: ", true
      );
      blackjackBlackjackPayoutLabel = ConfigurationEngine.setString(
         List.of("Label for the blackjack payout in the bet info display."),
         this.file,
         this.fileConfiguration,
         "blackjack.blackjackPayoutLabel",
         "&7Blackjack: ",
         true
      );
      blackjackCardValueLabel = ConfigurationEngine.setString(
         List.of("Label for the card value in the card display lore."), this.file, this.fileConfiguration, "blackjack.cardValueLabel", "&7Value: ", true
      );
      blackjackHiddenCardText = ConfigurationEngine.setString(
         List.of("Text for the hidden dealer card."), this.file, this.fileConfiguration, "blackjack.hiddenCardText", "&8???", true
      );
      blackjackHiddenCardLore = ConfigurationEngine.setString(
         List.of("Lore for the hidden dealer card."), this.file, this.fileConfiguration, "blackjack.hiddenCardLore", "&7Hidden", true
      );
      blackjackResultBlackjack = ConfigurationEngine.setString(
         List.of("Result text when the player gets a blackjack."), this.file, this.fileConfiguration, "blackjack.resultBlackjack", "&a&lBLACKJACK!", true
      );
      blackjackResultYouWin = ConfigurationEngine.setString(
         List.of("Result text when the player wins normally."), this.file, this.fileConfiguration, "blackjack.resultYouWin", "&a&lYOU WIN!", true
      );
      blackjackResultDealerBusts = ConfigurationEngine.setString(
         List.of("Result text when the dealer busts."), this.file, this.fileConfiguration, "blackjack.resultDealerBusts", "&a&lDEALER BUSTS!", true
      );
      blackjackResultPush = ConfigurationEngine.setString(
         List.of("Result text when the game is a push (tie)."), this.file, this.fileConfiguration, "blackjack.resultPush", "&e&lPUSH - TIE", true
      );
      blackjackResultBust = ConfigurationEngine.setString(
         List.of("Result text when the player busts."), this.file, this.fileConfiguration, "blackjack.resultBust", "&c&lBUST!", true
      );
      blackjackResultDealerWins = ConfigurationEngine.setString(
         List.of("Result text when the dealer wins."), this.file, this.fileConfiguration, "blackjack.resultDealerWins", "&c&lDEALER WINS", true
      );
      blackjackResultGameOver = ConfigurationEngine.setString(
         List.of("Default game over text."), this.file, this.fileConfiguration, "blackjack.resultGameOver", "&7Game Over", true
      );
      blackjackResultPlayerLabel = ConfigurationEngine.setString(
         List.of("Label for the player's hand value in the result lore."), this.file, this.fileConfiguration, "blackjack.resultPlayerLabel", "&7Player: ", true
      );
      blackjackResultDealerLabel = ConfigurationEngine.setString(
         List.of("Label for the dealer's hand value in the result lore."), this.file, this.fileConfiguration, "blackjack.resultDealerLabel", "&7Dealer: ", true
      );
      blackjackResultWonPrefix = ConfigurationEngine.setString(
         List.of("Prefix shown before the win amount in the result lore."), this.file, this.fileConfiguration, "blackjack.resultWonPrefix", "&aYou won ", true
      );
      blackjackResultWonSuffix = ConfigurationEngine.setString(
         List.of("Suffix shown after the win amount in the result lore."), this.file, this.fileConfiguration, "blackjack.resultWonSuffix", " coins!", true
      );
      blackjackResultBetReturned = ConfigurationEngine.setString(
         List.of("Text shown in the result lore when the bet is returned (push)."),
         this.file,
         this.fileConfiguration,
         "blackjack.resultBetReturned",
         "&7Your bet was returned.",
         true
      );
      blackjackResultLostPrefix = ConfigurationEngine.setString(
         List.of("Prefix shown before the lost amount in the result lore."),
         this.file,
         this.fileConfiguration,
         "blackjack.resultLostPrefix",
         "&cYou lost ",
         true
      );
      blackjackResultCloseToContinue = ConfigurationEngine.setString(
         List.of("Text telling the player to close the menu to continue."),
         this.file,
         this.fileConfiguration,
         "blackjack.resultCloseToContinue",
         "&eClose to continue",
         true
      );
      blackjackCardSuitSpades = ConfigurationEngine.setString(
         List.of("Display name for the Spades suit."), this.file, this.fileConfiguration, "blackjack.cardSuitSpades", "Spades", true
      );
      blackjackCardSuitHearts = ConfigurationEngine.setString(
         List.of("Display name for the Hearts suit."), this.file, this.fileConfiguration, "blackjack.cardSuitHearts", "Hearts", true
      );
      blackjackCardSuitDiamonds = ConfigurationEngine.setString(
         List.of("Display name for the Diamonds suit."), this.file, this.fileConfiguration, "blackjack.cardSuitDiamonds", "Diamonds", true
      );
      blackjackCardSuitClubs = ConfigurationEngine.setString(
         List.of("Display name for the Clubs suit."), this.file, this.fileConfiguration, "blackjack.cardSuitClubs", "Clubs", true
      );
      blackjackCardOfText = ConfigurationEngine.setString(
         List.of("The word 'of' between the rank and suit of a card (e.g. 'Ace of Spades')."),
         this.file,
         this.fileConfiguration,
         "blackjack.cardOfText",
         " &7of ",
         true
      );
      coinFlipCoinTitle = ConfigurationEngine.setString(
         List.of("Title for the coin display before the flip."), this.file, this.fileConfiguration, "coinFlip.coinTitle", "&6&lThe Coin", true
      );
      coinFlipChooseLore = ConfigurationEngine.setString(
         List.of("Lore prompting the player to choose Heads or Tails."),
         this.file,
         this.fileConfiguration,
         "coinFlip.chooseLore",
         "&7Choose Heads or Tails!",
         true
      );
      coinFlipWillFlipLore = ConfigurationEngine.setString(
         List.of("Lore telling the player the coin will flip."), this.file, this.fileConfiguration, "coinFlip.willFlipLore", "&7The coin will flip...", true
      );
      coinFlipHeadsButtonText = ConfigurationEngine.setString(
         List.of("Text for the Heads button."), this.file, this.fileConfiguration, "coinFlip.headsButton", "&e&lHEADS", true
      );
      coinFlipHeadsBetLore = ConfigurationEngine.setString(
         List.of("Lore prompting the player to bet on Heads."), this.file, this.fileConfiguration, "coinFlip.headsBetLore", "&7Click to bet on Heads!", true
      );
      coinFlipTailsButtonText = ConfigurationEngine.setString(
         List.of("Text for the Tails button."), this.file, this.fileConfiguration, "coinFlip.tailsButton", "&6&lTAILS", true
      );
      coinFlipTailsBetLore = ConfigurationEngine.setString(
         List.of("Lore prompting the player to bet on Tails."), this.file, this.fileConfiguration, "coinFlip.tailsBetLore", "&7Click to bet on Tails!", true
      );
      coinFlipMakeYourChoice = ConfigurationEngine.setString(
         List.of("Text shown in the result slot before a choice is made."),
         this.file,
         this.fileConfiguration,
         "coinFlip.makeYourChoice",
         "&7Make your choice!",
         true
      );
      coinFlipBetLabel = ConfigurationEngine.setString(
         List.of("Label for the bet info display. Bet amount and 'coins' are appended."),
         this.file,
         this.fileConfiguration,
         "coinFlip.betLabel",
         "&aBet: ",
         true
      );
      coinFlipNormalWinLabel = ConfigurationEngine.setString(
         List.of("Label for the normal win amount in the bet info."), this.file, this.fileConfiguration, "coinFlip.normalWinLabel", "&7Normal Win: &a", true
      );
      coinFlipPayoutLabel = ConfigurationEngine.setString(
         List.of("Label for the payout multiplier in the bet info."), this.file, this.fileConfiguration, "coinFlip.payoutLabel", "&7Payout: &e", true
      );
      coinFlipEdgeChanceLore = ConfigurationEngine.setString(
         List.of("Lore describing the edge chance."), this.file, this.fileConfiguration, "coinFlip.edgeChanceLore", "&b✦ 1% chance to land on EDGE!", true
      );
      coinFlipEdgePayoutLore = ConfigurationEngine.setString(
         List.of("Lore describing the edge payout. Amount is inserted dynamically."),
         this.file,
         this.fileConfiguration,
         "coinFlip.edgePayoutLoreV2",
         "&b✦ Edge Payout: &a5x &7(&a",
         true
      );
      coinFlipFlipping = ConfigurationEngine.setString(
         List.of("Text shown while the coin is flipping."), this.file, this.fileConfiguration, "coinFlip.flipping", "&7Flipping...", true
      );
      coinFlipHeadsText = ConfigurationEngine.setString(
         List.of("Text shown for Heads during the animation."), this.file, this.fileConfiguration, "coinFlip.headsText", "&e&lHEADS", true
      );
      coinFlipTailsText = ConfigurationEngine.setString(
         List.of("Text shown for Tails during the animation."), this.file, this.fileConfiguration, "coinFlip.tailsText", "&6&lTAILS", true
      );
      coinFlipEdgeCoinTitle = ConfigurationEngine.setString(
         List.of("Title for the coin when it lands on edge."), this.file, this.fileConfiguration, "coinFlip.edgeCoinTitle", "&b&l✦ EDGE! ✦", true
      );
      coinFlipEdgeCoinLore = ConfigurationEngine.setString(
         List.of("Lore for the coin when it lands on edge."),
         this.file,
         this.fileConfiguration,
         "coinFlip.edgeCoinLore",
         "&7The coin landed on its &bEDGE&7!",
         true
      );
      coinFlipEdgeResultTitle = ConfigurationEngine.setString(
         List.of("Title for the edge result display."), this.file, this.fileConfiguration, "coinFlip.edgeResultTitle", "&b&l✦ INCREDIBLE! ✦", true
      );
      coinFlipEdgeLandedLore = ConfigurationEngine.setString(
         List.of("Lore explaining the coin landed on its edge."),
         this.file,
         this.fileConfiguration,
         "coinFlip.edgeLandedLore",
         "&7The coin landed on its &bEDGE&7!",
         true
      );
      coinFlipEdgeRareLore = ConfigurationEngine.setString(
         List.of("Lore explaining the rarity of landing on edge."),
         this.file,
         this.fileConfiguration,
         "coinFlip.edgeRareLore",
         "&7This is incredibly rare! (1% chance)",
         true
      );
      coinFlipEdgeWonPrefix = ConfigurationEngine.setString(
         List.of("Prefix before the won amount in the edge result. Amount and 'coins' are appended."),
         this.file,
         this.fileConfiguration,
         "coinFlip.edgeWonPrefix",
         "&7You won &a",
         true
      );
      coinFlipEdgePayoutMultiplier = ConfigurationEngine.setString(
         List.of("Text showing the payout multiplier in the edge result."),
         this.file,
         this.fileConfiguration,
         "coinFlip.edgePayoutMultiplierV2",
         "&6&l5x PAYOUT!",
         true
      );
      coinFlipEdgeChatMessage = ConfigurationEngine.setString(
         List.of("Chat message sent when the coin lands on edge.", "Use %amount% for the win amount."),
         this.file,
         this.fileConfiguration,
         "coinFlip.edgeChatMessageV2",
         "&b&l✦ EDGE! ✦ &7The coin landed on its edge! &a5x PAYOUT! &7You won &a%amount% coins&7!",
         true
      );
      coinFlipLandedOnLore = ConfigurationEngine.setString(
         List.of("Lore prefix for the final coin result. The side name is appended."),
         this.file,
         this.fileConfiguration,
         "coinFlip.landedOnLore",
         "&7The coin landed on ",
         true
      );
      coinFlipHeadsWord = ConfigurationEngine.setString(
         List.of("The word 'Heads' used in the landed-on lore."), this.file, this.fileConfiguration, "coinFlip.headsWord", "Heads", true
      );
      coinFlipTailsWord = ConfigurationEngine.setString(
         List.of("The word 'Tails' used in the landed-on lore."), this.file, this.fileConfiguration, "coinFlip.tailsWord", "Tails", true
      );
      coinFlipYouWin = ConfigurationEngine.setString(
         List.of("Title shown when the player wins the coin flip."), this.file, this.fileConfiguration, "coinFlip.youWin", "&a&lYOU WIN!", true
      );
      coinFlipWonPrefix = ConfigurationEngine.setString(
         List.of("Prefix before the won amount in the win result. Amount and 'coins' are appended."),
         this.file,
         this.fileConfiguration,
         "coinFlip.wonPrefix",
         "&7You won &a",
         true
      );
      coinFlipCloseToContinue = ConfigurationEngine.setString(
         List.of("Text telling the player to close the menu to continue."),
         this.file,
         this.fileConfiguration,
         "coinFlip.closeToContinue",
         "&eClose to continue",
         true
      );
      coinFlipYouLose = ConfigurationEngine.setString(
         List.of("Title shown when the player loses the coin flip."), this.file, this.fileConfiguration, "coinFlip.youLose", "&c&lYOU LOSE", true
      );
      coinFlipLostPrefix = ConfigurationEngine.setString(
         List.of("Prefix before the lost amount in the lose result. Amount and 'coins' are appended."),
         this.file,
         this.fileConfiguration,
         "coinFlip.lostPrefix",
         "&7You lost &c",
         true
      );
      slotsPaylineLabel = ConfigurationEngine.setString(
         List.of("Label for the payline indicators."), this.file, this.fileConfiguration, "slots.paylineLabel", "&e>>> PAYLINE >>>", true
      );
      slotsSpinButtonText = ConfigurationEngine.setString(
         List.of("Text for the Spin button."), this.file, this.fileConfiguration, "slots.spinButton", "&a&lSPIN!", true
      );
      slotsSpinClickLore = ConfigurationEngine.setString(
         List.of("Lore for the Spin button prompting the player to click."),
         this.file,
         this.fileConfiguration,
         "slots.spinClickLore",
         "&7Click to spin the reels!",
         true
      );
      slotsSpinBetLore = ConfigurationEngine.setString(
         List.of("Lore showing the bet amount on the Spin button. Amount is appended."),
         this.file,
         this.fileConfiguration,
         "slots.spinBetLore",
         "&7Bet: &6",
         true
      );
      slotsBetLabel = ConfigurationEngine.setString(
         List.of("Label for the bet info display. Amount and 'coins' are appended."), this.file, this.fileConfiguration, "slots.betLabel", "&aBet: ", true
      );
      slotsMatch3Lore = ConfigurationEngine.setString(
         List.of("Lore explaining 3-symbol match wins."), this.file, this.fileConfiguration, "slots.match3Lore", "&7Match 3 symbols to win!", true
      );
      slotsMatch2Lore = ConfigurationEngine.setString(
         List.of("Lore explaining 2-symbol match returns."),
         this.file,
         this.fileConfiguration,
         "slots.match2Lore",
         "&7Match 2 symbols for a small return",
         true
      );
      slotsPayoutsTitle = ConfigurationEngine.setString(
         List.of("Title header in the payout table lore."), this.file, this.fileConfiguration, "slots.payoutsTitle", "&6&lPAYOUTS &7(Rarer = Higher!)", true
      );
      slotsPayoutTableTitle = ConfigurationEngine.setString(
         List.of("Title for the payout table item."), this.file, this.fileConfiguration, "slots.payoutTableTitle", "&6Payout Table", true
      );
      slotsChanceRarityNote = ConfigurationEngine.setString(
         List.of("Note at the bottom of the payout table."),
         this.file,
         this.fileConfiguration,
         "slots.chanceRarityNote",
         "&8Chance shown as rarity colors",
         true
      );
      slotsSpinning = ConfigurationEngine.setString(
         List.of("Text shown while the reels are spinning."), this.file, this.fileConfiguration, "slots.spinning", "&7Spinning...", true
      );
      slotsJackpotTitle = ConfigurationEngine.setString(
         List.of("Title shown when the player hits the jackpot (3 sevens)."), this.file, this.fileConfiguration, "slots.jackpotTitle", "&6&l✦ JACKPOT! ✦", true
      );
      slotsBigWinTitle = ConfigurationEngine.setString(
         List.of("Title shown when the player matches 3 symbols (non-jackpot)."), this.file, this.fileConfiguration, "slots.bigWinTitle", "&a&lBIG WIN!", true
      );
      slotsSmallWinTitle = ConfigurationEngine.setString(
         List.of("Title shown when the player matches 2 symbols."), this.file, this.fileConfiguration, "slots.smallWinTitle", "&e&lSmall Win!", true
      );
      slotsWonPrefix = ConfigurationEngine.setString(
         List.of("Prefix before the won amount. Amount and 'coins' are appended."), this.file, this.fileConfiguration, "slots.wonPrefix", "&7You won &a", true
      );
      slotsCloseToContinue = ConfigurationEngine.setString(
         List.of("Text telling the player to close the menu to continue."),
         this.file,
         this.fileConfiguration,
         "slots.closeToContinue",
         "&eClose to continue",
         true
      );
      slotsNoMatchTitle = ConfigurationEngine.setString(
         List.of("Title shown when no symbols match."), this.file, this.fileConfiguration, "slots.noMatchTitle", "&c&lNo Match", true
      );
      slotsLostPrefix = ConfigurationEngine.setString(
         List.of("Prefix before the lost amount. Amount and 'coins' are appended."),
         this.file,
         this.fileConfiguration,
         "slots.lostPrefix",
         "&7You lost &c",
         true
      );
      higherLowerCardValueLabel = ConfigurationEngine.setString(
         List.of("Label for displaying a card's numeric value."), this.file, this.fileConfiguration, "higherLower.cardValueLabel", "&7Value: &f", true
      );
      higherLowerNextCardPrompt = ConfigurationEngine.setString(
         List.of("Lore prompting the player about the next card."),
         this.file,
         this.fileConfiguration,
         "higherLower.nextCardPrompt",
         "&7Is the next card higher or lower?",
         true
      );
      higherLowerHiddenCardText = ConfigurationEngine.setString(
         List.of("Text for the hidden next card."), this.file, this.fileConfiguration, "higherLower.hiddenCardText", "&8&l???", true
      );
      higherLowerMakeGuessLore = ConfigurationEngine.setString(
         List.of("Lore prompting the player to make a guess."), this.file, this.fileConfiguration, "higherLower.makeGuessLore", "&7Make your guess!", true
      );
      higherLowerHigherButtonText = ConfigurationEngine.setString(
         List.of("Text for the Higher button."), this.file, this.fileConfiguration, "higherLower.higherButton", "&a&lHIGHER", true
      );
      higherLowerHigherLore1 = ConfigurationEngine.setString(
         List.of("First line of lore for the Higher button."),
         this.file,
         this.fileConfiguration,
         "higherLower.higherLore1",
         "&7Click if you think the next",
         true
      );
      higherLowerHigherLore2 = ConfigurationEngine.setString(
         List.of("Second line of lore for the Higher button. The card name is appended."),
         this.file,
         this.fileConfiguration,
         "higherLower.higherLore2",
         "&7card is &ahigher &7than ",
         true
      );
      higherLowerCashOutButtonText = ConfigurationEngine.setString(
         List.of("Text for the Cash Out button when the player has a streak."),
         this.file,
         this.fileConfiguration,
         "higherLower.cashOutButton",
         "&e&lCASH OUT",
         true
      );
      higherLowerCurrentWinningsLabel = ConfigurationEngine.setString(
         List.of("Label for current winnings in the Cash Out button lore."),
         this.file,
         this.fileConfiguration,
         "higherLower.currentWinningsLabel",
         "&7Current Winnings: &a",
         true
      );
      higherLowerCashOutClickLore = ConfigurationEngine.setString(
         List.of("Lore prompting the player to click to take winnings."),
         this.file,
         this.fileConfiguration,
         "higherLower.cashOutClickLore",
         "&7Click to take your winnings!",
         true
      );
      higherLowerCashOutDisabledText = ConfigurationEngine.setString(
         List.of("Text for the Cash Out button when disabled (no streak)."),
         this.file,
         this.fileConfiguration,
         "higherLower.cashOutDisabled",
         "&7Cash Out",
         true
      );
      higherLowerCashOutWinOnceLore = ConfigurationEngine.setString(
         List.of("Lore shown when the player needs to win once before cashing out."),
         this.file,
         this.fileConfiguration,
         "higherLower.cashOutWinOnceLore",
         "&7Win at least once to cash out!",
         true
      );
      higherLowerLowerButtonText = ConfigurationEngine.setString(
         List.of("Text for the Lower button."), this.file, this.fileConfiguration, "higherLower.lowerButton", "&c&lLOWER", true
      );
      higherLowerLowerLore1 = ConfigurationEngine.setString(
         List.of("First line of lore for the Lower button."),
         this.file,
         this.fileConfiguration,
         "higherLower.lowerLore1",
         "&7Click if you think the next",
         true
      );
      higherLowerLowerLore2 = ConfigurationEngine.setString(
         List.of("Second line of lore for the Lower button. The card name is appended."),
         this.file,
         this.fileConfiguration,
         "higherLower.lowerLore2",
         "&7card is &clower &7than ",
         true
      );
      higherLowerStreakLabel = ConfigurationEngine.setString(
         List.of("Label for the streak display. Streak count and max are appended."),
         this.file,
         this.fileConfiguration,
         "higherLower.streakLabel",
         "&6Streak: ",
         true
      );
      higherLowerMultiplierLabel = ConfigurationEngine.setString(
         List.of("Label for the multiplier in the streak display."), this.file, this.fileConfiguration, "higherLower.multiplierLabel", "&7Multiplier: &e", true
      );
      higherLowerPotentialWinLabel = ConfigurationEngine.setString(
         List.of("Label for the potential win in the streak display."),
         this.file,
         this.fileConfiguration,
         "higherLower.potentialWinLabel",
         "&7Potential Win: &a",
         true
      );
      higherLowerMaxStreakLabel = ConfigurationEngine.setString(
         List.of("Label for the max streak in the streak display."), this.file, this.fileConfiguration, "higherLower.maxStreakLabel", "&7Max streak: &e", true
      );
      higherLowerBetLabel = ConfigurationEngine.setString(
         List.of("Label for the bet amount display."), this.file, this.fileConfiguration, "higherLower.betLabel", "&aBet: ", true
      );
      higherLowerEachGuessLore = ConfigurationEngine.setString(
         List.of("Lore explaining the multiplier per correct guess. The multiplier value is appended."),
         this.file,
         this.fileConfiguration,
         "higherLower.eachGuessLore",
         "&7Each correct guess multiplies by &e",
         true
      );
      higherLowerWait = ConfigurationEngine.setString(
         List.of("Text shown when buttons are disabled during processing."), this.file, this.fileConfiguration, "higherLower.wait", "&7Wait...", true
      );
      higherLowerRevealing = ConfigurationEngine.setString(
         List.of("Text shown during the card reveal animation."), this.file, this.fileConfiguration, "higherLower.revealing", "&7Revealing...", true
      );
      higherLowerMaxStreakChat = ConfigurationEngine.setString(
         List.of(
            "Chat message sent when the player hits max streak and is auto-cashed out.", "Use %streak% for the streak count and %amount% for the win amount."
         ),
         this.file,
         this.fileConfiguration,
         "higherLower.maxStreakChat",
         "&a[Casino] &6MAX STREAK! &aYou hit %streak% correct guesses! Auto-cashing out: &6%amount% coins!",
         true
      );
      higherLowerCorrectChat = ConfigurationEngine.setString(
         List.of("Chat message sent when the player guesses correctly.", "Use %streak% for the streak count and %multiplier% for the multiplier."),
         this.file,
         this.fileConfiguration,
         "higherLower.correctChat",
         "&a[Casino] Correct! Streak: %streak% | Multiplier: %multiplier%x",
         true
      );
      higherLowerGameOverTitle = ConfigurationEngine.setString(
         List.of("Title shown when the player loses in Higher/Lower."), this.file, this.fileConfiguration, "higherLower.gameOverTitle", "&c&lGAME OVER", true
      );
      higherLowerLostCardLore = ConfigurationEngine.setString(
         List.of("Lore prefix showing what the card was. The card name is appended."),
         this.file,
         this.fileConfiguration,
         "higherLower.lostCardLore",
         "&7You lost! The card was ",
         true
      );
      higherLowerLostCoinsLore = ConfigurationEngine.setString(
         List.of("Lore prefix showing how many coins were lost. Amount and 'coins' are appended."),
         this.file,
         this.fileConfiguration,
         "higherLower.lostCoinsLore",
         "&7You lost &c",
         true
      );
      higherLowerCloseToContinue = ConfigurationEngine.setString(
         List.of("Text telling the player to close the menu to continue."),
         this.file,
         this.fileConfiguration,
         "higherLower.closeToContinue",
         "&eClose to continue",
         true
      );
      higherLowerCashedOutTitle = ConfigurationEngine.setString(
         List.of("Title shown when the player cashes out."), this.file, this.fileConfiguration, "higherLower.cashedOutTitle", "&a&lCASHED OUT!", true
      );
      higherLowerCashedOutLore = ConfigurationEngine.setString(
         List.of("Lore prefix showing cash out amount. Amount and 'coins!' are appended."),
         this.file,
         this.fileConfiguration,
         "higherLower.cashedOutLore",
         "&7You cashed out with &a",
         true
      );
      higherLowerCashedOutStreakLore = ConfigurationEngine.setString(
         List.of("Lore prefix showing the streak at cash out. Streak count is appended."),
         this.file,
         this.fileConfiguration,
         "higherLower.cashedOutStreakLore",
         "&7Streak: &e",
         true
      );
      higherLowerAutoCashOutChat = ConfigurationEngine.setString(
         List.of("Chat message sent when auto-cashing out on menu close.", "Use %amount% for the awarded amount and %streak% for the streak."),
         this.file,
         this.fileConfiguration,
         "higherLower.autoCashOutChat",
         "&a[Casino] Auto-cashed out: &6%amount% coins &a(Streak: %streak%)",
         true
      );
      gamblingCurrencyWord = ConfigurationEngine.setString(
         List.of("The currency word displayed in gambling UIs (e.g. 'coins')."), this.file, this.fileConfiguration, "gambling.currencyWord", "coins", true
      );
      coinFlipWinLore = ConfigurationEngine.setString(
         List.of("Label for the win amount shown on Heads/Tails buttons. Amount is appended."),
         this.file,
         this.fileConfiguration,
         "coinFlip.winLore",
         "&7Win: &a",
         true
      );
      slotsPayoutSymbolFormat = ConfigurationEngine.setString(
         List.of(
            "Format for each line in the slot machine payout table.",
            "Use $symbol for the symbol name, $multiplier for 3-match payout, $twoMatch for 2-match payout."
         ),
         this.file,
         this.fileConfiguration,
         "slots.payoutSymbolFormat",
         "$symbol&7: &a$multiplierx &8(2x: $twoMatchx)",
         true
      );
      higherLowerAutoCashoutSuffix = ConfigurationEngine.setString(
         List.of("Suffix shown after the max streak value in Higher/Lower."),
         this.file,
         this.fileConfiguration,
         "higherLower.autoCashoutSuffix",
         " &7(auto-cashout)",
         true
      );
      bettingAffordableColor = ConfigurationEngine.setString(
         List.of("Color code applied to the current bet display when the player can afford it."),
         this.file,
         this.fileConfiguration,
         "betting.affordableColor",
         "&a",
         true
      );
      bettingUnaffordableColor = ConfigurationEngine.setString(
         List.of("Color code applied to the current bet display when the player cannot afford it."),
         this.file,
         this.fileConfiguration,
         "betting.unaffordableColor",
         "&c",
         true
      );
      houseEarningsLabel = ConfigurationEngine.setString(
         List.of("Label displayed above the Gambling Den Owner NPC showing house earnings."),
         this.file,
         this.fileConfiguration,
         "gambling.houseEarningsLabel",
         "&6House Earnings: ",
         true
      );
      debtCollectorSpawnMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message shown when the Debt Collector spawns for a player.",
            "$player is the placeholder for the player name.",
            "$amount is the placeholder for the debt amount."
         ),
         this.file,
         this.fileConfiguration,
         "debtCollector.spawnMessage",
         "&c[Debt Collector] &7Well well well... &e$player&7. You owe us &6$amount coins&7. Time to pay up!",
         true
      );
      debtCollectorTimeoutMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when the Debt Collector times out.", "$player is the placeholder for the player name."),
         this.file,
         this.fileConfiguration,
         "debtCollector.timeoutMessage",
         "&c[Debt Collector] &7Running won't save you, &e$player&7. I'll be back for what you owe!",
         true
      );
      debtCollectorDeathMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when the Debt Collector is killed.", "$player is the placeholder for the player name."),
         this.file,
         this.fileConfiguration,
         "debtCollector.deathMessage",
         "&c[Debt Collector] &7Ugh... I'll be back, &e$player&7. And I'll bring friends!",
         true
      );
      debtCollectorKillMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when the Debt Collector kills a player.", "$player is the placeholder for the player name."),
         this.file,
         this.fileConfiguration,
         "debtCollector.killMessage",
         "&c[Debt Collector] &7Pleasure doing business, &e$player&7. Let that be a lesson about debts!",
         true
      );
      debtPaidMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message shown when debt is partially paid.",
            "$amount is the placeholder for the paid amount.",
            "$remaining is the placeholder for the remaining debt."
         ),
         this.file,
         this.fileConfiguration,
         "debtCollector.debtPaidMessage",
         "&7[Casino] &e$amount coins &7of debt paid. Remaining debt: &c$remaining coins",
         true
      );
      debtClearedMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when a player's debt is fully cleared."),
         this.file,
         this.fileConfiguration,
         "debtCollector.debtClearedMessage",
         "&a[Casino] &7Your debt has been cleared! You're free... for now.",
         true
      );
      debtAutoCollectedMessage = ConfigurationEngine.setString(
         List.of(
            "Sets the message shown when currency is automatically collected toward gambling debt.",
            "$amount is the placeholder for the collected amount.",
            "$remaining is the placeholder for the remaining debt."
         ),
         this.file,
         this.fileConfiguration,
         "debtCollector.debtAutoCollectedMessage",
         "&c[Debt Collector] &7Nice earnings. I'll be taking &6$amount coins &7off the top. You still owe &6$remaining coins&7. Don't forget it.",
         true
      );
      debtAutoClearedMessage = ConfigurationEngine.setString(
         List.of("Sets the message shown when currency collection fully clears the gambling debt."),
         this.file,
         this.fileConfiguration,
         "debtCollector.debtAutoClearedMessage",
         "&c[Debt Collector] &7That settles your debt. You're square with us... for now. Try not to push your luck again.",
         true
      );
      bettingCasinoClosedMessage = ConfigurationEngine.setString(
         List.of("Message shown when the casino is disabled."),
         this.file,
         this.fileConfiguration,
         "betting.casinoClosedMessage",
         "&c[Casino] The casino is currently closed!",
         true
      );
      bettingSelectBetLore = ConfigurationEngine.setString(
         List.of("Lore prompting the player to select their bet."),
         this.file,
         this.fileConfiguration,
         "betting.selectBetLore",
         "&eSelect your bet amount below!",
         true
      );
      bettingYourFinancesTitle = ConfigurationEngine.setString(
         List.of("Title for the finances display item."), this.file, this.fileConfiguration, "betting.yourFinancesTitle", "&6Your Finances", true
      );
      bettingBalanceLabel = ConfigurationEngine.setString(
         List.of("Label for the balance in the finances display."), this.file, this.fileConfiguration, "betting.balanceLabel", "&7Balance: &a", true
      );
      bettingDebtLabel = ConfigurationEngine.setString(
         List.of("Label for the debt in the finances display."), this.file, this.fileConfiguration, "betting.debtLabel", "&7Debt: &c", true
      );
      bettingAvailableCreditLabel = ConfigurationEngine.setString(
         List.of("Label for the available credit in the finances display."),
         this.file,
         this.fileConfiguration,
         "betting.availableCreditLabel",
         "&7Available Credit: &e",
         true
      );
      bettingCurrentBetPrefix = ConfigurationEngine.setString(
         List.of("Prefix for the current bet display. Amount is appended."),
         this.file,
         this.fileConfiguration,
         "betting.currentBetPrefix",
         "Current Bet: ",
         true
      );
      bettingClickPlayLore = ConfigurationEngine.setString(
         List.of("Lore shown when the player can afford the bet."), this.file, this.fileConfiguration, "betting.clickPlayLore", "&7Click Play to start!", true
      );
      bettingCantAffordLore = ConfigurationEngine.setString(
         List.of("Lore shown when the player can't afford the bet."),
         this.file,
         this.fileConfiguration,
         "betting.cantAffordLore",
         "&cYou can't afford this bet!",
         true
      );
      bettingAdjustBetLore = ConfigurationEngine.setString(
         List.of("Lore prefix for the bet adjustment buttons. The increment value is appended."),
         this.file,
         this.fileConfiguration,
         "betting.adjustBetLore",
         "&7Click to adjust bet by ",
         true
      );
      bettingAllInButtonText = ConfigurationEngine.setString(
         List.of("Text for the All In button."), this.file, this.fileConfiguration, "betting.allInButton", "&b&lALL IN", true
      );
      bettingAllInLore = ConfigurationEngine.setString(
         List.of("Lore for the All In button."), this.file, this.fileConfiguration, "betting.allInLore", "&7Bet everything you have!", true
      );
      bettingMaxAvailableLabel = ConfigurationEngine.setString(
         List.of("Label showing the max available bet. Amount is appended."),
         this.file,
         this.fileConfiguration,
         "betting.maxAvailableLabel",
         "&7Max available: &6",
         true
      );
      bettingResetButtonText = ConfigurationEngine.setString(
         List.of("Text for the Reset button."), this.file, this.fileConfiguration, "betting.resetButton", "&8&lRESET", true
      );
      bettingResetLore = ConfigurationEngine.setString(
         List.of("Lore for the Reset button. The min bet amount is appended."),
         this.file,
         this.fileConfiguration,
         "betting.resetLore",
         "&7Reset to minimum bet: &6",
         true
      );
      bettingCancelLore = ConfigurationEngine.setString(
         List.of("Lore for the Cancel button."), this.file, this.fileConfiguration, "betting.cancelLore", "&7Click to leave the casino", true
      );
      bettingCantAffordButtonText = ConfigurationEngine.setString(
         List.of("Text for the Play button when the player can't afford the bet."),
         this.file,
         this.fileConfiguration,
         "betting.cantAffordButton",
         "&c&lCan't Afford",
         true
      );
      bettingStartGameLore = ConfigurationEngine.setString(
         List.of("Lore for the Play button when the player can afford the bet."),
         this.file,
         this.fileConfiguration,
         "betting.startGameLore",
         "&7Start the game with your current bet!",
         true
      );
      bettingReduceBetLore = ConfigurationEngine.setString(
         List.of("Lore for the Play button when the player can't afford the bet."),
         this.file,
         this.fileConfiguration,
         "betting.reduceBetLore",
         "&7Reduce your bet or pay off debt",
         true
      );
      bettingBlackjackName = ConfigurationEngine.setString(
         List.of("Display name for the Blackjack game type."), this.file, this.fileConfiguration, "betting.blackjackName", "Blackjack", true
      );
      bettingBlackjackDescription = ConfigurationEngine.setString(
         List.of("Description for the Blackjack game type."),
         this.file,
         this.fileConfiguration,
         "betting.blackjackDescription",
         "Get as close to 21 as possible without going over!",
         true
      );
      bettingCoinFlipName = ConfigurationEngine.setString(
         List.of("Display name for the Coin Flip game type."), this.file, this.fileConfiguration, "betting.coinFlipName", "Coin Flip", true
      );
      bettingCoinFlipDescription = ConfigurationEngine.setString(
         List.of("Description for the Coin Flip game type."),
         this.file,
         this.fileConfiguration,
         "betting.coinFlipDescription",
         "Heads or Tails? Simple 50/50 chance!",
         true
      );
      bettingSlotsName = ConfigurationEngine.setString(
         List.of("Display name for the Slot Machine game type."), this.file, this.fileConfiguration, "betting.slotsName", "Slot Machine", true
      );
      bettingSlotsDescription = ConfigurationEngine.setString(
         List.of("Description for the Slot Machine game type."),
         this.file,
         this.fileConfiguration,
         "betting.slotsDescription",
         "Match three symbols to win big!",
         true
      );
      bettingHigherLowerName = ConfigurationEngine.setString(
         List.of("Display name for the Higher or Lower game type."), this.file, this.fileConfiguration, "betting.higherLowerName", "Higher or Lower", true
      );
      bettingHigherLowerDescription = ConfigurationEngine.setString(
         List.of("Description for the Higher or Lower game type."),
         this.file,
         this.fileConfiguration,
         "betting.higherLowerDescription",
         "Guess if the next card is higher or lower!",
         true
      );
   }

   private int requirePositiveInt(String path, int value, int fallback) {
      return value > 0 ? value : this.replaceInvalid(path, value, fallback, "a positive whole number");
   }

   private double requireProbability(String path, double value, double fallback) {
      return Double.isFinite(value) && value >= 0.0 && value <= 1.0 ? value : this.replaceInvalid(path, value, fallback, "a finite number between 0 and 1");
   }

   private double requireNonNegativeFinite(String path, double value, double fallback) {
      return Double.isFinite(value) && value >= 0.0 ? value : this.replaceInvalid(path, value, fallback, "a finite non-negative number");
   }

   private int replaceInvalid(String path, int value, int fallback, String requirement) {
      Logger.warn("Invalid GamblingSettings.yml value for " + path + " (" + value + "); expected " + requirement + ". Using " + fallback + ".");
      this.fileConfiguration.set(path, fallback);
      return fallback;
   }

   private double replaceInvalid(String path, double value, double fallback, String requirement) {
      Logger.warn("Invalid GamblingSettings.yml value for " + path + " (" + value + "); expected " + requirement + ". Using " + fallback + ".");
      this.fileConfiguration.set(path, fallback);
      return fallback;
   }

   @Generated
   public static boolean isGamblingEnabled() {
      return gamblingEnabled;
   }

   @Generated
   public static int getMinBet() {
      return minBet;
   }

   @Generated
   public static double getDebtCollectorSpawnChance() {
      return debtCollectorSpawnChance;
   }

   @Generated
   public static int getDebtCollectorCheckIntervalMinutes() {
      return debtCollectorCheckIntervalMinutes;
   }

   @Generated
   public static int getDebtCollectorTimeoutSeconds() {
      return debtCollectorTimeoutSeconds;
   }

   @Generated
   public static double getDebtReductionOnPlayerDeath() {
      return debtReductionOnPlayerDeath;
   }

   @Generated
   public static double getBlackjackPayoutNormal() {
      return blackjackPayoutNormal;
   }

   @Generated
   public static double getBlackjackPayoutBlackjack() {
      return blackjackPayoutBlackjack;
   }

   @Generated
   public static double getCoinFlipPayout() {
      return coinFlipPayout;
   }

   @Generated
   public static double getCoinFlipEdgeChance() {
      return coinFlipEdgeChance;
   }

   @Generated
   public static double getCoinFlipEdgePayout() {
      return coinFlipEdgePayout;
   }

   @Generated
   public static double getHigherLowerMultiplier() {
      return higherLowerMultiplier;
   }

   @Generated
   public static String getInsufficientFundsMessage() {
      return insufficientFundsMessage;
   }

   @Generated
   public static String getWinMessage() {
      return winMessage;
   }

   @Generated
   public static String getLoseMessage() {
      return loseMessage;
   }

   @Generated
   public static String getDebtWarningMessage() {
      return debtWarningMessage;
   }

   @Generated
   public static String getBetPlacedMessage() {
      return betPlacedMessage;
   }

   @Generated
   public static String getBettingMenuTitle() {
      return bettingMenuTitle;
   }

   @Generated
   public static String getPlayButtonText() {
      return playButtonText;
   }

   @Generated
   public static String getCancelButtonText() {
      return cancelButtonText;
   }

   @Generated
   public static String getBlackjackMenuTitle() {
      return blackjackMenuTitle;
   }

   @Generated
   public static String getCoinFlipMenuTitle() {
      return coinFlipMenuTitle;
   }

   @Generated
   public static String getSlotsMenuTitle() {
      return slotsMenuTitle;
   }

   @Generated
   public static String getHigherLowerMenuTitle() {
      return higherLowerMenuTitle;
   }

   @Generated
   public static String getBlackjackHitButtonText() {
      return blackjackHitButtonText;
   }

   @Generated
   public static String getBlackjackHitButtonLore() {
      return blackjackHitButtonLore;
   }

   @Generated
   public static String getBlackjackStandButtonText() {
      return blackjackStandButtonText;
   }

   @Generated
   public static String getBlackjackStandButtonLore() {
      return blackjackStandButtonLore;
   }

   @Generated
   public static String getBlackjackDoubleDownButtonText() {
      return blackjackDoubleDownButtonText;
   }

   @Generated
   public static String getBlackjackDoubleDownButtonLore() {
      return blackjackDoubleDownButtonLore;
   }

   @Generated
   public static String getBlackjackDoubleDownDisabledText() {
      return blackjackDoubleDownDisabledText;
   }

   @Generated
   public static String getBlackjackDoubleDownCantAffordLore() {
      return blackjackDoubleDownCantAffordLore;
   }

   @Generated
   public static String getBlackjackDealerLabel() {
      return blackjackDealerLabel;
   }

   @Generated
   public static String getBlackjackPlayerLabel() {
      return blackjackPlayerLabel;
   }

   @Generated
   public static String getBlackjackBetLabel() {
      return blackjackBetLabel;
   }

   @Generated
   public static String getBlackjackWinPayoutLabel() {
      return blackjackWinPayoutLabel;
   }

   @Generated
   public static String getBlackjackBlackjackPayoutLabel() {
      return blackjackBlackjackPayoutLabel;
   }

   @Generated
   public static String getBlackjackCardValueLabel() {
      return blackjackCardValueLabel;
   }

   @Generated
   public static String getBlackjackHiddenCardText() {
      return blackjackHiddenCardText;
   }

   @Generated
   public static String getBlackjackHiddenCardLore() {
      return blackjackHiddenCardLore;
   }

   @Generated
   public static String getBlackjackResultBlackjack() {
      return blackjackResultBlackjack;
   }

   @Generated
   public static String getBlackjackResultYouWin() {
      return blackjackResultYouWin;
   }

   @Generated
   public static String getBlackjackResultDealerBusts() {
      return blackjackResultDealerBusts;
   }

   @Generated
   public static String getBlackjackResultPush() {
      return blackjackResultPush;
   }

   @Generated
   public static String getBlackjackResultBust() {
      return blackjackResultBust;
   }

   @Generated
   public static String getBlackjackResultDealerWins() {
      return blackjackResultDealerWins;
   }

   @Generated
   public static String getBlackjackResultGameOver() {
      return blackjackResultGameOver;
   }

   @Generated
   public static String getBlackjackResultPlayerLabel() {
      return blackjackResultPlayerLabel;
   }

   @Generated
   public static String getBlackjackResultDealerLabel() {
      return blackjackResultDealerLabel;
   }

   @Generated
   public static String getBlackjackResultWonPrefix() {
      return blackjackResultWonPrefix;
   }

   @Generated
   public static String getBlackjackResultWonSuffix() {
      return blackjackResultWonSuffix;
   }

   @Generated
   public static String getBlackjackResultBetReturned() {
      return blackjackResultBetReturned;
   }

   @Generated
   public static String getBlackjackResultLostPrefix() {
      return blackjackResultLostPrefix;
   }

   @Generated
   public static String getBlackjackResultCloseToContinue() {
      return blackjackResultCloseToContinue;
   }

   @Generated
   public static String getBlackjackCardSuitSpades() {
      return blackjackCardSuitSpades;
   }

   @Generated
   public static String getBlackjackCardSuitHearts() {
      return blackjackCardSuitHearts;
   }

   @Generated
   public static String getBlackjackCardSuitDiamonds() {
      return blackjackCardSuitDiamonds;
   }

   @Generated
   public static String getBlackjackCardSuitClubs() {
      return blackjackCardSuitClubs;
   }

   @Generated
   public static String getBlackjackCardOfText() {
      return blackjackCardOfText;
   }

   @Generated
   public static String getCoinFlipCoinTitle() {
      return coinFlipCoinTitle;
   }

   @Generated
   public static String getCoinFlipChooseLore() {
      return coinFlipChooseLore;
   }

   @Generated
   public static String getCoinFlipWillFlipLore() {
      return coinFlipWillFlipLore;
   }

   @Generated
   public static String getCoinFlipHeadsButtonText() {
      return coinFlipHeadsButtonText;
   }

   @Generated
   public static String getCoinFlipHeadsBetLore() {
      return coinFlipHeadsBetLore;
   }

   @Generated
   public static String getCoinFlipTailsButtonText() {
      return coinFlipTailsButtonText;
   }

   @Generated
   public static String getCoinFlipTailsBetLore() {
      return coinFlipTailsBetLore;
   }

   @Generated
   public static String getCoinFlipMakeYourChoice() {
      return coinFlipMakeYourChoice;
   }

   @Generated
   public static String getCoinFlipBetLabel() {
      return coinFlipBetLabel;
   }

   @Generated
   public static String getCoinFlipNormalWinLabel() {
      return coinFlipNormalWinLabel;
   }

   @Generated
   public static String getCoinFlipPayoutLabel() {
      return coinFlipPayoutLabel;
   }

   @Generated
   public static String getCoinFlipEdgeChanceLore() {
      return coinFlipEdgeChanceLore;
   }

   @Generated
   public static String getCoinFlipEdgePayoutLore() {
      return coinFlipEdgePayoutLore;
   }

   @Generated
   public static String getCoinFlipFlipping() {
      return coinFlipFlipping;
   }

   @Generated
   public static String getCoinFlipHeadsText() {
      return coinFlipHeadsText;
   }

   @Generated
   public static String getCoinFlipTailsText() {
      return coinFlipTailsText;
   }

   @Generated
   public static String getCoinFlipEdgeCoinTitle() {
      return coinFlipEdgeCoinTitle;
   }

   @Generated
   public static String getCoinFlipEdgeCoinLore() {
      return coinFlipEdgeCoinLore;
   }

   @Generated
   public static String getCoinFlipEdgeResultTitle() {
      return coinFlipEdgeResultTitle;
   }

   @Generated
   public static String getCoinFlipEdgeLandedLore() {
      return coinFlipEdgeLandedLore;
   }

   @Generated
   public static String getCoinFlipEdgeRareLore() {
      return coinFlipEdgeRareLore;
   }

   @Generated
   public static String getCoinFlipEdgeWonPrefix() {
      return coinFlipEdgeWonPrefix;
   }

   @Generated
   public static String getCoinFlipEdgePayoutMultiplier() {
      return coinFlipEdgePayoutMultiplier;
   }

   @Generated
   public static String getCoinFlipEdgeChatMessage() {
      return coinFlipEdgeChatMessage;
   }

   @Generated
   public static String getCoinFlipLandedOnLore() {
      return coinFlipLandedOnLore;
   }

   @Generated
   public static String getCoinFlipHeadsWord() {
      return coinFlipHeadsWord;
   }

   @Generated
   public static String getCoinFlipTailsWord() {
      return coinFlipTailsWord;
   }

   @Generated
   public static String getCoinFlipYouWin() {
      return coinFlipYouWin;
   }

   @Generated
   public static String getCoinFlipWonPrefix() {
      return coinFlipWonPrefix;
   }

   @Generated
   public static String getCoinFlipCloseToContinue() {
      return coinFlipCloseToContinue;
   }

   @Generated
   public static String getCoinFlipYouLose() {
      return coinFlipYouLose;
   }

   @Generated
   public static String getCoinFlipLostPrefix() {
      return coinFlipLostPrefix;
   }

   @Generated
   public static String getSlotsPaylineLabel() {
      return slotsPaylineLabel;
   }

   @Generated
   public static String getSlotsSpinButtonText() {
      return slotsSpinButtonText;
   }

   @Generated
   public static String getSlotsSpinClickLore() {
      return slotsSpinClickLore;
   }

   @Generated
   public static String getSlotsSpinBetLore() {
      return slotsSpinBetLore;
   }

   @Generated
   public static String getSlotsBetLabel() {
      return slotsBetLabel;
   }

   @Generated
   public static String getSlotsMatch3Lore() {
      return slotsMatch3Lore;
   }

   @Generated
   public static String getSlotsMatch2Lore() {
      return slotsMatch2Lore;
   }

   @Generated
   public static String getSlotsPayoutsTitle() {
      return slotsPayoutsTitle;
   }

   @Generated
   public static String getSlotsPayoutTableTitle() {
      return slotsPayoutTableTitle;
   }

   @Generated
   public static String getSlotsChanceRarityNote() {
      return slotsChanceRarityNote;
   }

   @Generated
   public static String getSlotsSpinning() {
      return slotsSpinning;
   }

   @Generated
   public static String getSlotsJackpotTitle() {
      return slotsJackpotTitle;
   }

   @Generated
   public static String getSlotsBigWinTitle() {
      return slotsBigWinTitle;
   }

   @Generated
   public static String getSlotsSmallWinTitle() {
      return slotsSmallWinTitle;
   }

   @Generated
   public static String getSlotsWonPrefix() {
      return slotsWonPrefix;
   }

   @Generated
   public static String getSlotsCloseToContinue() {
      return slotsCloseToContinue;
   }

   @Generated
   public static String getSlotsNoMatchTitle() {
      return slotsNoMatchTitle;
   }

   @Generated
   public static String getSlotsLostPrefix() {
      return slotsLostPrefix;
   }

   @Generated
   public static String getHigherLowerCardValueLabel() {
      return higherLowerCardValueLabel;
   }

   @Generated
   public static String getHigherLowerNextCardPrompt() {
      return higherLowerNextCardPrompt;
   }

   @Generated
   public static String getHigherLowerHiddenCardText() {
      return higherLowerHiddenCardText;
   }

   @Generated
   public static String getHigherLowerMakeGuessLore() {
      return higherLowerMakeGuessLore;
   }

   @Generated
   public static String getHigherLowerHigherButtonText() {
      return higherLowerHigherButtonText;
   }

   @Generated
   public static String getHigherLowerHigherLore1() {
      return higherLowerHigherLore1;
   }

   @Generated
   public static String getHigherLowerHigherLore2() {
      return higherLowerHigherLore2;
   }

   @Generated
   public static String getHigherLowerCashOutButtonText() {
      return higherLowerCashOutButtonText;
   }

   @Generated
   public static String getHigherLowerCurrentWinningsLabel() {
      return higherLowerCurrentWinningsLabel;
   }

   @Generated
   public static String getHigherLowerCashOutClickLore() {
      return higherLowerCashOutClickLore;
   }

   @Generated
   public static String getHigherLowerCashOutDisabledText() {
      return higherLowerCashOutDisabledText;
   }

   @Generated
   public static String getHigherLowerCashOutWinOnceLore() {
      return higherLowerCashOutWinOnceLore;
   }

   @Generated
   public static String getHigherLowerLowerButtonText() {
      return higherLowerLowerButtonText;
   }

   @Generated
   public static String getHigherLowerLowerLore1() {
      return higherLowerLowerLore1;
   }

   @Generated
   public static String getHigherLowerLowerLore2() {
      return higherLowerLowerLore2;
   }

   @Generated
   public static String getHigherLowerStreakLabel() {
      return higherLowerStreakLabel;
   }

   @Generated
   public static String getHigherLowerMultiplierLabel() {
      return higherLowerMultiplierLabel;
   }

   @Generated
   public static String getHigherLowerPotentialWinLabel() {
      return higherLowerPotentialWinLabel;
   }

   @Generated
   public static String getHigherLowerMaxStreakLabel() {
      return higherLowerMaxStreakLabel;
   }

   @Generated
   public static String getHigherLowerBetLabel() {
      return higherLowerBetLabel;
   }

   @Generated
   public static String getHigherLowerEachGuessLore() {
      return higherLowerEachGuessLore;
   }

   @Generated
   public static String getHigherLowerWait() {
      return higherLowerWait;
   }

   @Generated
   public static String getHigherLowerRevealing() {
      return higherLowerRevealing;
   }

   @Generated
   public static String getHigherLowerMaxStreakChat() {
      return higherLowerMaxStreakChat;
   }

   @Generated
   public static String getHigherLowerCorrectChat() {
      return higherLowerCorrectChat;
   }

   @Generated
   public static String getHigherLowerGameOverTitle() {
      return higherLowerGameOverTitle;
   }

   @Generated
   public static String getHigherLowerLostCardLore() {
      return higherLowerLostCardLore;
   }

   @Generated
   public static String getHigherLowerLostCoinsLore() {
      return higherLowerLostCoinsLore;
   }

   @Generated
   public static String getHigherLowerCloseToContinue() {
      return higherLowerCloseToContinue;
   }

   @Generated
   public static String getHigherLowerCashedOutTitle() {
      return higherLowerCashedOutTitle;
   }

   @Generated
   public static String getHigherLowerCashedOutLore() {
      return higherLowerCashedOutLore;
   }

   @Generated
   public static String getHigherLowerCashedOutStreakLore() {
      return higherLowerCashedOutStreakLore;
   }

   @Generated
   public static String getHigherLowerAutoCashOutChat() {
      return higherLowerAutoCashOutChat;
   }

   @Generated
   public static String getDebtCollectorSpawnMessage() {
      return debtCollectorSpawnMessage;
   }

   @Generated
   public static String getDebtCollectorTimeoutMessage() {
      return debtCollectorTimeoutMessage;
   }

   @Generated
   public static String getDebtCollectorDeathMessage() {
      return debtCollectorDeathMessage;
   }

   @Generated
   public static String getDebtCollectorKillMessage() {
      return debtCollectorKillMessage;
   }

   @Generated
   public static String getDebtPaidMessage() {
      return debtPaidMessage;
   }

   @Generated
   public static String getDebtClearedMessage() {
      return debtClearedMessage;
   }

   @Generated
   public static String getDebtAutoCollectedMessage() {
      return debtAutoCollectedMessage;
   }

   @Generated
   public static String getDebtAutoClearedMessage() {
      return debtAutoClearedMessage;
   }

   @Generated
   public static String getBettingCasinoClosedMessage() {
      return bettingCasinoClosedMessage;
   }

   @Generated
   public static String getBettingSelectBetLore() {
      return bettingSelectBetLore;
   }

   @Generated
   public static String getBettingYourFinancesTitle() {
      return bettingYourFinancesTitle;
   }

   @Generated
   public static String getBettingBalanceLabel() {
      return bettingBalanceLabel;
   }

   @Generated
   public static String getBettingDebtLabel() {
      return bettingDebtLabel;
   }

   @Generated
   public static String getBettingAvailableCreditLabel() {
      return bettingAvailableCreditLabel;
   }

   @Generated
   public static String getBettingCurrentBetPrefix() {
      return bettingCurrentBetPrefix;
   }

   @Generated
   public static String getBettingClickPlayLore() {
      return bettingClickPlayLore;
   }

   @Generated
   public static String getBettingCantAffordLore() {
      return bettingCantAffordLore;
   }

   @Generated
   public static String getBettingAdjustBetLore() {
      return bettingAdjustBetLore;
   }

   @Generated
   public static String getBettingAllInButtonText() {
      return bettingAllInButtonText;
   }

   @Generated
   public static String getBettingAllInLore() {
      return bettingAllInLore;
   }

   @Generated
   public static String getBettingMaxAvailableLabel() {
      return bettingMaxAvailableLabel;
   }

   @Generated
   public static String getBettingResetButtonText() {
      return bettingResetButtonText;
   }

   @Generated
   public static String getBettingResetLore() {
      return bettingResetLore;
   }

   @Generated
   public static String getBettingCancelLore() {
      return bettingCancelLore;
   }

   @Generated
   public static String getBettingCantAffordButtonText() {
      return bettingCantAffordButtonText;
   }

   @Generated
   public static String getBettingStartGameLore() {
      return bettingStartGameLore;
   }

   @Generated
   public static String getBettingReduceBetLore() {
      return bettingReduceBetLore;
   }

   @Generated
   public static String getBettingBlackjackName() {
      return bettingBlackjackName;
   }

   @Generated
   public static String getBettingBlackjackDescription() {
      return bettingBlackjackDescription;
   }

   @Generated
   public static String getBettingCoinFlipName() {
      return bettingCoinFlipName;
   }

   @Generated
   public static String getBettingCoinFlipDescription() {
      return bettingCoinFlipDescription;
   }

   @Generated
   public static String getBettingSlotsName() {
      return bettingSlotsName;
   }

   @Generated
   public static String getBettingSlotsDescription() {
      return bettingSlotsDescription;
   }

   @Generated
   public static String getBettingHigherLowerName() {
      return bettingHigherLowerName;
   }

   @Generated
   public static String getBettingHigherLowerDescription() {
      return bettingHigherLowerDescription;
   }

   @Generated
   public static String getGamblingCurrencyWord() {
      return gamblingCurrencyWord;
   }

   @Generated
   public static String getCoinFlipWinLore() {
      return coinFlipWinLore;
   }

   @Generated
   public static String getSlotsPayoutSymbolFormat() {
      return slotsPayoutSymbolFormat;
   }

   @Generated
   public static String getHigherLowerAutoCashoutSuffix() {
      return higherLowerAutoCashoutSuffix;
   }

   @Generated
   public static String getBettingAffordableColor() {
      return bettingAffordableColor;
   }

   @Generated
   public static String getBettingUnaffordableColor() {
      return bettingUnaffordableColor;
   }

   @Generated
   public static String getHouseEarningsLabel() {
      return houseEarningsLabel;
   }
}
