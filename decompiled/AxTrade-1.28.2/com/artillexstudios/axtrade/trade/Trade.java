package com.artillexstudios.axtrade.trade;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.api.events.AxTradeAbortEvent;
import com.artillexstudios.axtrade.api.events.AxTradeCompleteEvent;
import com.artillexstudios.axtrade.currency.CurrencyProcessor;
import com.artillexstudios.axtrade.hooks.currency.CurrencyHook;
import com.artillexstudios.axtrade.libs.axapi.scheduler.Scheduler;
import com.artillexstudios.axtrade.libs.axapi.utils.PaperUtils;
import com.artillexstudios.axtrade.utils.HistoryUtils;
import com.artillexstudios.axtrade.utils.NumberUtils;
import com.artillexstudios.axtrade.utils.SoundUtils;
import com.artillexstudios.axtrade.utils.TaxUtils;
import com.artillexstudios.axtrade.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Trade {
   protected final TradePlayer player1;
   protected final TradePlayer player2;
   private volatile boolean ended = false;
   protected long prepTime = System.currentTimeMillis();

   public Trade(Player p1, Player p2) {
      this.player1 = new TradePlayer(this, p1);
      this.player2 = new TradePlayer(this, p2);
      this.player1.setOtherPlayer(this.player2);
      this.player2.setOtherPlayer(this.player1);
      HistoryUtils.writeToHistory(String.format("Started: %s - %s", this.player1.getPlayer().getName(), this.player2.getPlayer().getName()));
   }

   public void update() {
      this.player1.getTradeGui().update();
      this.player2.getTradeGui().update();
   }

   public CompletableFuture<Void> end() {
      this.ended = true;
      CompletableFuture<Void> task1 = new CompletableFuture<>();
      Player player = this.player1.getPlayer();
      this.execute(player, () -> {
         this.closeAndUpdate(player);
         task1.complete(null);
      });
      CompletableFuture<Void> task2 = new CompletableFuture<>();
      Player playerx = this.player2.getPlayer();
      this.execute(playerx, () -> {
         this.closeAndUpdate(player);
         task2.complete(null);
      });
      return CompletableFuture.allOf(task1, task2);
   }

   private void closeAndUpdate(Player player) {
      player.closeInventory();
      player.updateInventory();
   }

   public void abort() {
      this.abort(false);
   }

   public void abort(boolean force) {
      if (force || !this.ended) {
         AxTradeAbortEvent event = new AxTradeAbortEvent(this);
         Bukkit.getPluginManager().callEvent(event);
         this.end().thenRun(() -> {
            this.player1.getTradeGui().getItems(false).forEach(itemStack -> {
               if (itemStack != null) {
                  this.addOrDrop(this.player1.getPlayer().getInventory(), List.of(itemStack), this.player1.getPlayer().getLocation());
               }
            });
            if (this.player2.getTradeGui() != null) {
               this.player2.getTradeGui().getItems(false).forEach(itemStack -> {
                  if (itemStack != null) {
                     this.addOrDrop(this.player2.getPlayer().getInventory(), List.of(itemStack), this.player2.getPlayer().getLocation());
                  }
               });
            }

            HistoryUtils.writeToHistory(String.format("Aborted: %s - %s", this.player1.getPlayer().getName(), this.player2.getPlayer().getName()));
            AxTrade.MESSAGEUTILS.sendLang(this.player1.getPlayer(), "trade.aborted", Map.of("%player%", this.player2.getPlayer().getName()));
            AxTrade.MESSAGEUTILS.sendLang(this.player2.getPlayer(), "trade.aborted", Map.of("%player%", this.player1.getPlayer().getName()));
            SoundUtils.playSound(this.player1.getPlayer(), "aborted");
            SoundUtils.playSound(this.player2.getPlayer(), "aborted");
            Scheduler.get().runLater(task -> Trades.removeTrade(this), 1L);
         }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
         });
      }
   }

   public void complete() {
      this.end()
         .thenRun(
            () -> {
               for (Entry<CurrencyHook, Double> entry : this.player1.getCurrencies().entrySet()) {
                  if (entry.getKey().getBalance(this.player1.getPlayer().getUniqueId()) < entry.getValue()) {
                     this.abort(true);
                     return;
                  }
               }

               for (Entry<CurrencyHook, Double> entryx : this.player2.getCurrencies().entrySet()) {
                  if (entryx.getKey().getBalance(this.player2.getPlayer().getUniqueId()) < entryx.getValue()) {
                     this.abort(true);
                     return;
                  }
               }

               AxTradeCompleteEvent event = new AxTradeCompleteEvent(this);
               Bukkit.getPluginManager().callEvent(event);
               if (event.isCancelled()) {
                  this.abort(true);
               } else {
                  CurrencyProcessor currencyProcessor1 = new CurrencyProcessor(this.player1.getPlayer(), this.player1.getCurrencies().entrySet());
                  currencyProcessor1.run()
                     .thenAccept(
                        success1 -> {
                           if (!success1) {
                              this.abort(true);
                           } else {
                              CurrencyProcessor currencyProcessor2 = new CurrencyProcessor(this.player2.getPlayer(), this.player2.getCurrencies().entrySet());
                              currencyProcessor2.run()
                                 .thenAccept(
                                    success2 -> {
                                       if (!success2) {
                                          this.abort(true);
                                          currencyProcessor1.reverse();
                                       } else {
                                          AxTrade.MESSAGEUTILS
                                             .sendLang(this.player1.getPlayer(), "trade.completed", Map.of("%player%", this.player2.getPlayer().getName()));
                                          AxTrade.MESSAGEUTILS
                                             .sendLang(this.player2.getPlayer(), "trade.completed", Map.of("%player%", this.player1.getPlayer().getName()));
                                          SoundUtils.playSound(this.player1.getPlayer(), "completed");
                                          SoundUtils.playSound(this.player2.getPlayer(), "completed");
                                          List<String> player1Currencies = new ArrayList<>();

                                          for (Entry<CurrencyHook, Double> entryxx : this.player1.getCurrencies().entrySet()) {
                                             double amountAfterTax = TaxUtils.getTotalAfterTax(entryxx.getValue(), entryxx.getKey());
                                             entryxx.getKey().giveBalance(this.player2.getPlayer().getUniqueId(), amountAfterTax);
                                             String currencyName = Utils.getFormattedCurrency(entryxx.getKey());
                                             String fullCurrencyAmount = NumberUtils.formatNumber(entryxx.getValue());
                                             String taxedCurrencyAmount = NumberUtils.formatNumber(amountAfterTax);
                                             if (amountAfterTax == entryxx.getValue()) {
                                                player1Currencies.add(currencyName + ": " + taxedCurrencyAmount);
                                             } else {
                                                player1Currencies.add(
                                                   currencyName
                                                      + ": "
                                                      + taxedCurrencyAmount
                                                      + " (+ tax: "
                                                      + NumberUtils.formatNumber(entryxx.getValue() - amountAfterTax)
                                                      + ")"
                                                );
                                             }

                                             if (AxTrade.CONFIG.getBoolean("enable-trade-summaries")) {
                                                AxTrade.MESSAGEUTILS
                                                   .sendFormatted(
                                                      this.player2.getPlayer(),
                                                      AxTrade.LANG.getString("summary.get.currency"),
                                                      Map.of("%amount%", taxedCurrencyAmount, "%currency%", currencyName)
                                                   );
                                                AxTrade.MESSAGEUTILS
                                                   .sendFormatted(
                                                      this.player1.getPlayer(),
                                                      AxTrade.LANG.getString("summary.give.currency"),
                                                      Map.of("%amount%", fullCurrencyAmount, "%currency%", currencyName)
                                                   );
                                             }
                                          }

                                          List<String> player2Currencies = new ArrayList<>();

                                          for (Entry<CurrencyHook, Double> entryxx : this.player2.getCurrencies().entrySet()) {
                                             double amountAfterTaxx = TaxUtils.getTotalAfterTax(entryxx.getValue(), entryxx.getKey());
                                             entryxx.getKey().giveBalance(this.player1.getPlayer().getUniqueId(), amountAfterTaxx);
                                             String currencyNamex = Utils.getFormattedCurrency(entryxx.getKey());
                                             String fullCurrencyAmountx = NumberUtils.formatNumber(entryxx.getValue());
                                             String taxedCurrencyAmountx = NumberUtils.formatNumber(amountAfterTaxx);
                                             if (amountAfterTaxx == entryxx.getValue()) {
                                                player2Currencies.add(currencyNamex + ": " + taxedCurrencyAmountx);
                                             } else {
                                                player2Currencies.add(
                                                   currencyNamex
                                                      + ": "
                                                      + taxedCurrencyAmountx
                                                      + " (+ tax: "
                                                      + NumberUtils.formatNumber(entryxx.getValue() - amountAfterTaxx)
                                                      + ")"
                                                );
                                             }

                                             if (AxTrade.CONFIG.getBoolean("enable-trade-summaries")) {
                                                AxTrade.MESSAGEUTILS
                                                   .sendFormatted(
                                                      this.player2.getPlayer(),
                                                      AxTrade.LANG.getString("summary.give.currency"),
                                                      Map.of("%amount%", fullCurrencyAmountx, "%currency%", currencyNamex)
                                                   );
                                                AxTrade.MESSAGEUTILS
                                                   .sendFormatted(
                                                      this.player1.getPlayer(),
                                                      AxTrade.LANG.getString("summary.get.currency"),
                                                      Map.of("%amount%", taxedCurrencyAmountx, "%currency%", currencyNamex)
                                                   );
                                             }
                                          }

                                          List<String> player1Items = new ArrayList<>();
                                          this.player1
                                             .getTradeGui()
                                             .getItems(false)
                                             .forEach(
                                                itemStack -> {
                                                   if (itemStack != null) {
                                                      String itemName = Utils.getFormattedItemName(itemStack);
                                                      int itemAm = itemStack.getAmount();
                                                      this.addOrDrop(
                                                         this.player2.getPlayer().getInventory(), List.of(itemStack), this.player2.getPlayer().getLocation()
                                                      );
                                                      player1Items.add(itemAm + "x " + itemName);
                                                      if (AxTrade.CONFIG.getBoolean("enable-trade-summaries")) {
                                                         AxTrade.MESSAGEUTILS
                                                            .sendFormatted(
                                                               this.player1.getPlayer(),
                                                               AxTrade.LANG.getString("summary.give.item"),
                                                               Map.of("%amount%", itemAm + "", "%item%", itemName)
                                                            );
                                                         AxTrade.MESSAGEUTILS
                                                            .sendFormatted(
                                                               this.player2.getPlayer(),
                                                               AxTrade.LANG.getString("summary.get.item"),
                                                               Map.of("%amount%", itemAm + "", "%item%", itemName)
                                                            );
                                                      }
                                                   }
                                                }
                                             );
                                          List<String> player2Items = new ArrayList<>();
                                          this.player2
                                             .getTradeGui()
                                             .getItems(false)
                                             .forEach(
                                                itemStack -> {
                                                   if (itemStack != null) {
                                                      String itemName = Utils.getFormattedItemName(itemStack);
                                                      int itemAm = itemStack.getAmount();
                                                      this.addOrDrop(
                                                         this.player1.getPlayer().getInventory(), List.of(itemStack), this.player1.getPlayer().getLocation()
                                                      );
                                                      player2Items.add(itemAm + "x " + itemName);
                                                      if (AxTrade.CONFIG.getBoolean("enable-trade-summaries")) {
                                                         AxTrade.MESSAGEUTILS
                                                            .sendFormatted(
                                                               this.player2.getPlayer(),
                                                               AxTrade.LANG.getString("summary.give.item"),
                                                               Map.of("%amount%", itemAm + "", "%item%", itemName)
                                                            );
                                                         AxTrade.MESSAGEUTILS
                                                            .sendFormatted(
                                                               this.player1.getPlayer(),
                                                               AxTrade.LANG.getString("summary.get.item"),
                                                               Map.of("%amount%", itemAm + "", "%item%", itemName)
                                                            );
                                                      }
                                                   }
                                                }
                                             );
                                          HistoryUtils.writeToHistory(
                                             String.format(
                                                "%s: [Currencies: %s] [Items: %s] | %s: [Currencies: %s] [Items: %s]",
                                                this.player1.getPlayer().getName(),
                                                player1Currencies.isEmpty() ? "---" : String.join(", ", player1Currencies),
                                                player1Items.isEmpty() ? "---" : String.join(", ", player1Items),
                                                this.player2.getPlayer().getName(),
                                                player2Currencies.isEmpty() ? "---" : String.join(", ", player2Currencies),
                                                player2Items.isEmpty() ? "---" : String.join(", ", player2Items)
                                             )
                                          );
                                          Scheduler.get().runLater(task -> Trades.removeTrade(this), 1L);
                                       }
                                    }
                                 )
                                 .exceptionally(throwable -> {
                                    throwable.printStackTrace();
                                    return null;
                                 });
                           }
                        }
                     )
                     .exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                     });
               }
            }
         )
         .exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
         });
   }

   public long getPrepTime() {
      return this.prepTime;
   }

   public TradePlayer getPlayer1() {
      return this.player1;
   }

   public TradePlayer getPlayer2() {
      return this.player2;
   }

   public Player getOtherPlayer(Player player) {
      return this.player1.getPlayer().equals(player) ? this.player2.getPlayer() : this.player1.getPlayer();
   }

   public boolean isEnded() {
      return this.ended;
   }

   private void addOrDrop(Inventory inventory, List<ItemStack> items, Location location) {
      boolean folia = PaperUtils.isFolia();
      boolean sync = folia ? Scheduler.get().isOwnedByCurrentRegion(location) : Bukkit.isPrimaryThread();
      Location copy = location.clone();
      Runnable runnable = () -> {
         for (ItemStack key : items) {
            HashMap<Integer, ItemStack> remaining = inventory.addItem(new ItemStack[]{key});
            remaining.forEach((k, v) -> copy.getWorld().dropItem(copy, v));
         }
      };
      if (sync) {
         runnable.run();
      } else {
         Scheduler.get().runAt(copy, runnable);
      }
   }

   private void execute(Player player, Runnable runnable) {
      boolean folia = PaperUtils.isFolia();
      boolean sync = folia ? Scheduler.get().isOwnedByCurrentRegion(player.getLocation()) : Bukkit.isPrimaryThread();
      if (sync) {
         runnable.run();
      } else {
         Scheduler.get().run(player, task -> runnable.run(), runnable);
      }
   }
}
