package com.artillexstudios.axtrade.trade;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.hooks.HookManager;
import com.artillexstudios.axtrade.hooks.currency.CurrencyHook;
import com.artillexstudios.axtrade.utils.NumberUtils;
import com.artillexstudios.axtrade.utils.SoundUtils;
import com.artillexstudios.axtrade.utils.Utils;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TradePlayer {
   private final Player player;
   private TradePlayer otherPlayer;
   private TradeGui tradeGui;
   private final Trade trade;
   private final ConcurrentHashMap<CurrencyHook, Double> currencies = new ConcurrentHashMap<>();
   private Integer confirmed = null;

   public TradePlayer(Trade trade, Player player) {
      this.player = player;
      this.trade = trade;
   }

   public void setOtherPlayer(TradePlayer otherPlayer) {
      this.otherPlayer = otherPlayer;
      this.tradeGui = new TradeGui(this.trade, this);
   }

   public Player getPlayer() {
      return this.player;
   }

   public TradePlayer getOtherPlayer() {
      return this.otherPlayer;
   }

   public TradeGui getTradeGui() {
      return this.tradeGui;
   }

   public Integer getConfirmed() {
      return this.confirmed;
   }

   public boolean hasConfirmed() {
      return this.confirmed != null;
   }

   public void confirm() {
      for (CurrencyHook currencyHook : HookManager.getCurrency()) {
         Number minimum = (Number)currencyHook.getSettings().getOrDefault("required", 0);
         if (!(minimum.doubleValue() <= 0.0)
            && this.getCurrency(currencyHook.getName()) + this.otherPlayer.getCurrency(currencyHook.getName()) < minimum.doubleValue()) {
            AxTrade.MESSAGEUTILS
               .sendLang(
                  this.player,
                  "request.below-required-amount",
                  Map.of("%amount%", NumberUtils.formatNumber(minimum.doubleValue()), "%currency%", Utils.getFormattedCurrency(currencyHook))
               );
            return;
         }
      }

      this.confirmed = AxTrade.CONFIG.getInt("trade-confirm-seconds", 10);
      this.trade.update();
      SoundUtils.playSound(this.player, "accept");
      SoundUtils.playSound(this.otherPlayer.getPlayer(), "accept");
      this.getTradeGui().updateTitle();
      this.otherPlayer.getTradeGui().updateTitle();
   }

   public void cancel() {
      if (this.confirmed != null) {
         this.confirmed = null;
         this.otherPlayer.setConfirmed(null);
         SoundUtils.playSound(this.player, "cancel");
         SoundUtils.playSound(this.otherPlayer.getPlayer(), "cancel");
         this.getTradeGui().updateTitle();
         this.otherPlayer.getTradeGui().updateTitle();
      } else {
         if (this.otherPlayer.setConfirmed(null)) {
            this.getTradeGui().updateTitle();
         }
      }
   }

   public boolean setConfirmed(Integer confirmed) {
      if (Objects.equals(this.confirmed, confirmed)) {
         return false;
      } else {
         this.confirmed = confirmed;
         this.getTradeGui().updateTitle();
         return true;
      }
   }

   public void tick() {
      this.confirmed = this.confirmed - 1;
      this.trade.update();
      SoundUtils.playSound(this.player, "countdown");
   }

   public ConcurrentHashMap<CurrencyHook, Double> getCurrencies() {
      return this.currencies;
   }

   public double getCurrency(String currency) {
      CurrencyHook currencyHook = HookManager.getCurrencyHook(currency);
      if (currencyHook == null) {
         return 0.0;
      } else {
         Double amount = this.currencies.get(currencyHook);
         return amount == null ? 0.0 : amount;
      }
   }

   public TradePlayer.Result setCurrency(String currency, String am) {
      Double unformatted = NumberUtils.parseNumber(am);
      if (unformatted == null && !com.artillexstudios.axtrade.libs.axapi.utils.NumberUtils.isDouble(am)) {
         return TradePlayer.Result.NOT_A_NUMBER;
      } else {
         double amount = unformatted == null ? Double.parseDouble(am) : unformatted;
         if (Double.isNaN(amount)) {
            return TradePlayer.Result.NOT_A_NUMBER;
         } else {
            CurrencyHook currencyHook = HookManager.getCurrencyHook(currency);
            if (currencyHook == null) {
               return TradePlayer.Result.CURRENCY_NOT_FOUND;
            } else {
               amount = currencyHook.usesDouble() ? amount : Math.round(amount);
               if (amount == 0.0) {
                  this.currencies.remove(currencyHook);
                  return TradePlayer.Result.SUCCESS;
               } else if (amount < 0.1) {
                  return TradePlayer.Result.TOO_LOW_VALUE;
               } else if (currencyHook.getBalance(this.player.getUniqueId()) < amount) {
                  return TradePlayer.Result.NOT_ENOUGH_CURRENCY;
               } else {
                  this.currencies.put(currencyHook, amount);
                  return TradePlayer.Result.SUCCESS;
               }
            }
         }
      }
   }

   public int getEmptySlots() {
      int am = 0;

      for (ItemStack item : this.player.getInventory().getStorageContents()) {
         if (item == null) {
            am++;
         }
      }

      return am;
   }

   public static enum Result {
      NOT_A_NUMBER,
      CURRENCY_NOT_FOUND,
      TOO_LOW_VALUE,
      NOT_ENOUGH_CURRENCY,
      SUCCESS;
   }
}
