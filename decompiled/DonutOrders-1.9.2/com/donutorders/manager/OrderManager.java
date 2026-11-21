package com.donutorders.manager;

import com.donutorders.DonutOrders;
import com.donutorders.model.Order;
import com.donutorders.model.OrderStatus;
import com.donutorders.scheduler.FoliaScheduler;
import com.donutorders.storage.StorageManager;
import com.donutorders.util.DeliveryItemUtils;
import com.donutorders.util.ItemUtils;
import com.donutorders.util.MessageHelper;
import com.donutorders.util.NumberFormatter;
import com.donutorders.util.OrderBroadcast;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class OrderManager {
   private static final Logger LOG = DonutOrders.getInstance().getLogger();
   private final StorageManager storage;
   private final Economy economy;

   public OrderManager(StorageManager storage, Economy economy) {
      this.storage = storage;
      this.economy = economy;
   }

   public void createOrder(Player buyer, ItemStack itemTemplate, int amount, double pricePerItem, BiConsumer<Boolean, String> callback) {
      int maxOrders = DonutOrders.getInstance().getOrderLimitManager().getLimitValue(buyer);
      List<Order> existing = this.storage.getPlayerOrders(buyer.getUniqueId());
      long activeCount = existing.stream().filter(o -> o.getStatus() == OrderStatus.ACTIVE).count();
      if (activeCount >= maxOrders) {
         String msg = MessageHelper.get("max-orders-reached", "&cʏᴏᴜ ʜᴀᴠᴇ ʀᴇᴀᴄʜᴇᴅ ᴛʜᴇ ᴍᴀxɪᴍᴜᴍ ɴᴜᴍʙᴇʀ ᴏꜰ ᴀᴄᴛɪᴠᴇ ᴏʀᴅᴇʀꜱ ({0}).", maxOrders);
         FoliaScheduler.runAtEntity(buyer, () -> callback.accept(false, msg), () -> callback.accept(false, msg));
      } else {
         AllowedItemsManager itemsManager = DonutOrders.getInstance().getAllowedItemsManager();
         if (itemsManager != null && itemTemplate != null && itemsManager.isAllowed(itemTemplate.getType())) {
            double total = pricePerItem * amount;
            double tax = DonutOrders.getInstance().getConfig().getDouble("orders.tax-percent", 0.0) / 100.0;
            double taxedTotal = total * (1.0 + tax);
            FoliaScheduler.runAtEntity(
               buyer,
               () -> {
                  if (!this.economy.has(buyer, taxedTotal)) {
                     callback.accept(
                        false,
                        MessageHelper.get(
                           "insufficient-funds",
                           "&cɪɴꜱᴜꜰꜰɪᴄɪᴇɴᴛ ꜰᴜɴᴅꜱ. ʏᴏᴜ ɴᴇᴇᴅ &f{0}&c ʙᴜᴛ ᴏɴʟʏ ʜᴀᴠᴇ &f{1}&c.",
                           NumberFormatter.formatPrice(taxedTotal),
                           NumberFormatter.formatPrice(this.economy.getBalance(buyer))
                        )
                     );
                  } else {
                     this.economy.withdrawPlayer(buyer, taxedTotal);
                     long now = System.currentTimeMillis();
                     int expiryDays = DonutOrders.getInstance().getConfig().getInt("orders.default-expiry-days", 7);
                     long expiresAt = now + expiryDays * 86400000L;
                     Order order = new Order(
                        UUID.randomUUID(),
                        buyer.getUniqueId(),
                        buyer.getName(),
                        itemTemplate.clone(),
                        amount,
                        0,
                        pricePerItem,
                        total,
                        now,
                        expiresAt,
                        OrderStatus.ACTIVE
                     );
                     this.storage.saveOrder(order, () -> FoliaScheduler.runAtEntity(buyer, () -> {
                        this.broadcastOrderCreated(buyer, order);
                        callback.accept(true, null);
                     }, () -> {
                        this.broadcastOrderCreated(buyer, order);
                        callback.accept(true, null);
                     }));
                  }
               },
               () -> callback.accept(false, MessageHelper.get("player-retired", "&cᴘʟᴀʏᴇʀ ɪꜱ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴠᴀɪʟᴀʙʟᴇ."))
            );
         } else {
            String msg = MessageHelper.get("item-not-allowed", "&cᴛʜᴀᴛ ɪᴛᴇᴍ ɪꜱ ɴᴏᴛ ᴀʟʟᴏᴡᴇᴅ ꜰᴏʀ ᴏʀᴅᴇʀꜱ.");
            FoliaScheduler.runAtEntity(buyer, () -> callback.accept(false, msg), () -> callback.accept(false, msg));
         }
      }
   }

   public void fulfillOrder(Player seller, UUID orderId, ItemStack[] items, BiConsumer<Boolean, String> callback) {
      this.storage
         .refreshOrderFromDb(
            orderId,
            refreshed -> {
               if (refreshed == null) {
                  String msg = MessageHelper.get("order-not-found", "&cᴏʀᴅᴇʀ ɴᴏᴛ ꜰᴏᴜɴᴅ.");
                  this.failAndReturnItems(seller, items, msg, callback);
               } else if (refreshed.getStatus() != OrderStatus.ACTIVE) {
                  String msg = MessageHelper.get("order-no-longer-active", "&cᴏʀᴅᴇʀ ɪꜱ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴄᴛɪᴠᴇ.");
                  this.failAndReturnItems(seller, items, msg, callback);
               } else if (refreshed.getBuyerUUID().equals(seller.getUniqueId())) {
                  String msg = MessageHelper.get("delivery-own-order", "&cʏᴏᴜ ᴄᴀɴɴᴏᴛ ꜰᴜʟꜰɪʟʟ ʏᴏᴜʀ ᴏᴡɴ ᴏʀᴅᴇʀ.");
                  this.failAndReturnItems(seller, items, msg, callback);
               } else {
                  int amountNeeded = refreshed.getAmountRemaining();
                  ItemStack template = refreshed.getItemTemplate();
                  int validCount = Math.min(DeliveryItemUtils.countAvailable(seller, items, template), amountNeeded);
                  if (validCount == 0) {
                     String msg = MessageHelper.get("delivery-no-items", "&cʏᴏᴜ ʜᴀᴠᴇ ɴᴏ ᴠᴀʟɪᴅ ɪᴛᴇᴍꜱ ᴛᴏ ᴅᴇʟɪᴠᴇʀ.");
                     this.failAndReturnItems(seller, items, msg, callback);
                  } else if (!refreshed.tryLockDelivery()) {
                     String msg = MessageHelper.get("delivery-in-progress", "&cᴅᴇʟɪᴠᴇʀʏ ᴀʟʀᴇᴀᴅʏ ɪɴ ᴘʀᴏɢʀᴇꜱꜱ.");
                     this.failAndReturnItems(seller, items, msg, callback);
                  } else {
                     int expectedFilled = refreshed.getAmountFulfilled();
                     FoliaScheduler.runAtEntity(
                        seller,
                        () -> {
                           try {
                              int deliverCount = Math.min(DeliveryItemUtils.countAvailable(seller, items, template), amountNeeded);
                              if (deliverCount == 0) {
                                 ItemUtils.giveOrDropAll(seller, items, true);
                                 callback.accept(false, MessageHelper.get("delivery-no-items", "&cʏᴏᴜ ʜᴀᴠᴇ ɴᴏ ᴠᴀʟɪᴅ ɪᴛᴇᴍꜱ ᴛᴏ ᴅᴇʟɪᴠᴇʀ."));
                                 return;
                              }

                              ItemStack[] stashItems = DeliveryItemUtils.extract(seller, items, template, deliverCount);
                              int extractedCount = 0;

                              for (ItemStack stashItem : stashItems) {
                                 if (stashItem != null) {
                                    extractedCount += stashItem.getAmount();
                                 }
                              }

                              if (extractedCount != 0) {
                                 double payout = refreshed.getPricePerItem() * extractedCount;
                                 this.economy.depositPlayer(seller, payout);
                                 refreshed.setAmountFulfilled(refreshed.getAmountFulfilled() + extractedCount);
                                 refreshed.setRemainingFunds(refreshed.getRemainingFunds() - payout);
                                 if (refreshed.isFullyFulfilled()) {
                                    refreshed.setStatus(OrderStatus.PENDING);
                                 }

                                 DeliveryItemUtils.returnSnapshotShulkers(seller, items);
                                 this.storage
                                    .updateOrderOptimistic(
                                       refreshed,
                                       expectedFilled,
                                       OrderStatus.ACTIVE,
                                       success -> {
                                          if (!success) {
                                             FoliaScheduler.runAtEntity(
                                                seller,
                                                () -> callback.accept(false, MessageHelper.get("order-no-longer-active", "&cᴏʀᴅᴇʀ ɪꜱ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴄᴛɪᴠᴇ.")),
                                                () -> callback.accept(false, MessageHelper.get("order-no-longer-active", "&cᴏʀᴅᴇʀ ɪꜱ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴄᴛɪᴠᴇ."))
                                             );
                                          } else {
                                             this.storage
                                                .loadStash(
                                                   refreshed.getOrderId(),
                                                   existingStash -> {
                                                      ItemStack[] mergedStash = this.mergeStash(existingStash, stashItems);
                                                      this.storage
                                                         .saveStash(
                                                            refreshed.getOrderId(),
                                                            mergedStash,
                                                            () -> FoliaScheduler.runAtEntity(
                                                               seller, () -> callback.accept(true, null), () -> callback.accept(true, null)
                                                            )
                                                         );
                                                   }
                                                );
                                          }
                                       }
                                    );
                                 return;
                              }

                              ItemUtils.giveOrDropAll(seller, items, true);
                              callback.accept(false, MessageHelper.get("delivery-no-items", "&cʏᴏᴜ ʜᴀᴠᴇ ɴᴏ ᴠᴀʟɪᴅ ɪᴛᴇᴍꜱ ᴛᴏ ᴅᴇʟɪᴠᴇʀ."));
                           } finally {
                              refreshed.unlockDelivery();
                           }
                        },
                        () -> {
                           refreshed.unlockDelivery();
                           ItemUtils.giveOrDropAll(seller, items, true);
                           callback.accept(false, MessageHelper.get("player-retired", "&cᴘʟᴀʏᴇʀ ɪꜱ ɴᴏ ʟᴏɴɢᴇʀ ᴀᴠᴀɪʟᴀʙʟᴇ."));
                        }
                     );
                  }
               }
            }
         );
   }

   public void cancelOrder(Player buyer, UUID orderId, Consumer<Boolean> callback) {
      Order order = this.storage.getOrder(orderId);
      if (order == null || !order.getBuyerUUID().equals(buyer.getUniqueId())) {
         FoliaScheduler.runAtEntity(buyer, () -> callback.accept(false), () -> callback.accept(false));
      } else if (order.getStatus() != OrderStatus.ACTIVE) {
         FoliaScheduler.runAtEntity(buyer, () -> callback.accept(false), () -> callback.accept(false));
      } else {
         FoliaScheduler.runAtEntity(buyer, () -> {
            double refund = order.getRemainingFunds();
            if (refund > 0.0) {
               this.economy.depositPlayer(buyer, refund);
            }

            order.setRemainingFunds(0.0);
            order.setStatus(OrderStatus.PENDING);
            this.storage.updateOrder(order, () -> FoliaScheduler.runAtEntity(buyer, () -> callback.accept(true), () -> callback.accept(true)));
         }, () -> callback.accept(false));
      }
   }

   public void collectStash(Player buyer, UUID orderId, Consumer<Boolean> callback) {
      this.storage
         .refreshOrderFromDb(
            orderId,
            order -> {
               if (order == null) {
                  FoliaScheduler.runAtEntity(buyer, () -> callback.accept(false), () -> callback.accept(false));
               } else if (!order.getBuyerUUID().equals(buyer.getUniqueId())) {
                  LOG.log(
                     Level.WARNING,
                     "[Security] Player {0} attempted to claim stash of order {1} owned by {2} ({3}).",
                     new Object[]{buyer.getName(), orderId, order.getBuyerName(), order.getBuyerUUID()}
                  );
                  FoliaScheduler.runAtEntity(buyer, () -> callback.accept(false), () -> callback.accept(false));
               } else if (order.getStatus() == OrderStatus.CLAIMED) {
                  LOG.log(
                     Level.WARNING,
                     "[Security] Player {0} tried to collect order {1} which was ALREADY CLAIMED (claimed at: {2}, claimed by: {3}). Replay attack blocked.",
                     new Object[]{buyer.getName(), orderId, order.getClaimedAt(), order.getClaimedBy()}
                  );
                  FoliaScheduler.runAtEntity(buyer, () -> callback.accept(false), () -> callback.accept(false));
               } else if (order.getStatus() != OrderStatus.PENDING) {
                  FoliaScheduler.runAtEntity(buyer, () -> callback.accept(false), () -> callback.accept(false));
               } else if (!order.tryLockClaim()) {
                  LOG.log(
                     Level.WARNING,
                     "[Security] Player {0} triggered rapid concurrent collection for order {1}. Lock acquisition blocked.",
                     new Object[]{buyer.getName(), orderId}
                  );
                  FoliaScheduler.runAtEntity(buyer, () -> callback.accept(false), () -> callback.accept(false));
               } else if (order.getStatus() != OrderStatus.PENDING) {
                  order.unlockClaim();
                  FoliaScheduler.runAtEntity(buyer, () -> callback.accept(false), () -> callback.accept(false));
               } else {
                  order.setStatus(OrderStatus.CLAIMED);
                  order.setClaimedAt(System.currentTimeMillis());
                  order.setClaimedBy(buyer.getUniqueId());
                  this.storage.loadStash(orderId, stash -> this.storage.updateOrderOptimistic(order, null, OrderStatus.PENDING, success -> {
                     if (!success) {
                        order.unlockClaim();
                        FoliaScheduler.runAtEntity(buyer, () -> callback.accept(false), () -> callback.accept(false));
                     } else {
                        this.storage.clearStash(orderId, () -> FoliaScheduler.runAtEntity(buyer, () -> {
                           try {
                              for (ItemStack item : stash) {
                                 if (item != null && item.getType().isItem()) {
                                    Map<Integer, ItemStack> overflow = buyer.getInventory().addItem(new ItemStack[]{item});
                                    if (!overflow.isEmpty()) {
                                       overflow.values().forEach(drop -> buyer.getWorld().dropItemNaturally(buyer.getLocation(), drop));
                                    }
                                 }
                              }

                              double refund = order.getRemainingFunds();
                              if (refund > 0.0) {
                                 this.economy.depositPlayer(buyer, refund);
                                 order.setRemainingFunds(0.0);
                                 this.storage.updateOrder(order, null);
                              }

                              callback.accept(true);
                           } finally {
                              order.unlockClaim();
                           }
                        }, () -> {
                           order.unlockClaim();
                           callback.accept(false);
                        }));
                     }
                  }));
               }
            }
         );
   }

   public void runExpiryCheck() {
      long now = System.currentTimeMillis();

      for (Order order : this.storage.getAllOrders()) {
         if (order.getStatus() == OrderStatus.ACTIVE && order.getExpiresAt() <= now) {
            order.setStatus(OrderStatus.PENDING);
            this.storage.updateOrder(order, null);
            Player buyer = Bukkit.getPlayer(order.getBuyerUUID());
            if (buyer != null && buyer.isOnline()) {
               FoliaScheduler.runAtEntity(
                  buyer,
                  () -> MessageHelper.sendPrefixed(
                     buyer, "order-expired", "&7ʏᴏᴜʀ ᴏʀᴅᴇʀ ʜᴀꜱ ᴇxᴘɪʀᴇᴅ. &f{0} &7ʀᴇꜰᴜɴᴅᴇᴅ.", NumberFormatter.formatPrice(order.getRemainingFunds())
                  ),
                  null
               );
            }
         }
      }
   }

   private void broadcastOrderCreated(Player buyer, Order order) {
      DonutOrders plugin = DonutOrders.getInstance();
      if (plugin != null && OrderBroadcast.isEnabled(plugin.getConfig())) {
         String item = ItemUtils.describeOrderItem(order.getItemTemplate());
         String price = NumberFormatter.formatPrice(order.getPricePerItem());
         String buyerName = buyer != null ? buyer.getName() : order.getBuyerName();
         String message = MessageHelper.prefix() + OrderBroadcast.formatBody(buyerName, item, order.getAmountRequested(), price);
         UUID skip = buyer != null ? buyer.getUniqueId() : order.getBuyerUUID();
         FoliaScheduler.runGlobal(() -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
               if (OrderBroadcast.shouldNotify(online.getUniqueId(), skip)) {
                  FoliaScheduler.runAtEntity(online, () -> online.sendMessage(message));
               }
            }
         });
      }
   }

   private void failAndReturnItems(Player seller, ItemStack[] items, String msg, BiConsumer<Boolean, String> callback) {
      FoliaScheduler.runAtEntity(seller, () -> {
         ItemUtils.giveOrDropAll(seller, items, true);
         callback.accept(false, msg);
      }, () -> {
         ItemUtils.giveOrDropAll(seller, items, true);
         callback.accept(false, msg);
      });
   }

   private ItemStack[] mergeStash(ItemStack[] existing, ItemStack[] additions) {
      ItemStack[] stash = Arrays.copyOf(existing, 54);

      for (ItemStack add : additions) {
         if (add != null) {
            int leftOver = add.getAmount();

            for (int i = 0; i < stash.length && leftOver > 0; i++) {
               if (stash[i] != null && stash[i].isSimilar(add) && stash[i].getAmount() < stash[i].getMaxStackSize()) {
                  int canAdd = stash[i].getMaxStackSize() - stash[i].getAmount();
                  int adding = Math.min(canAdd, leftOver);
                  stash[i].setAmount(stash[i].getAmount() + adding);
                  leftOver -= adding;
               }
            }

            for (int ix = 0; ix < stash.length && leftOver > 0; ix++) {
               if (stash[ix] == null) {
                  ItemStack copy = add.clone();
                  copy.setAmount(Math.min(leftOver, add.getMaxStackSize()));
                  stash[ix] = copy;
                  leftOver -= copy.getAmount();
               }
            }
         }
      }

      return stash;
   }
}
