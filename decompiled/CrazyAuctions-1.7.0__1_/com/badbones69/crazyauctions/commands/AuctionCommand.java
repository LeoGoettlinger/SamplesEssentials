package com.badbones69.crazyauctions.commands;

import com.badbones69.crazyauctions.CrazyAuctions;
import com.badbones69.crazyauctions.Methods;
import com.badbones69.crazyauctions.api.CrazyManager;
import com.badbones69.crazyauctions.api.enums.Category;
import com.badbones69.crazyauctions.api.enums.Files;
import com.badbones69.crazyauctions.api.enums.Messages;
import com.badbones69.crazyauctions.api.enums.Reasons;
import com.badbones69.crazyauctions.api.enums.ShopType;
import com.badbones69.crazyauctions.api.events.AuctionCancelledEvent;
import com.badbones69.crazyauctions.api.events.AuctionListEvent;
import com.badbones69.crazyauctions.controllers.GuiListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import libs.com.ryderbelserion.vital.paper.api.files.FileManager;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;

public class AuctionCommand implements CommandExecutor {
   private final CrazyAuctions plugin = CrazyAuctions.get();
   private final CrazyManager crazyManager = this.plugin.getCrazyManager();
   private final FileManager fileManager = this.plugin.getFileManager();

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
      FileConfiguration config = Files.config.getConfiguration();
      FileConfiguration data = Files.data.getConfiguration();
      if (args.length == 0) {
         if (sender instanceof Player player) {
            if (!Methods.hasPermission(sender, "access")) {
               return true;
            } else if (config.contains("Settings.Category-Page-Opens-First") && config.getBoolean("Settings.Category-Page-Opens-First")) {
               GuiListener.openCategories(player, ShopType.SELL);
               return true;
            } else {
               if (this.crazyManager.isSellingEnabled()) {
                  GuiListener.openShop(player, ShopType.SELL, Category.NONE, 1);
               } else if (this.crazyManager.isBiddingEnabled()) {
                  GuiListener.openShop(player, ShopType.BID, Category.NONE, 1);
               } else {
                  player.sendMessage(
                     Methods.getPrefix() + Methods.color("&cThe bidding and selling options are both disabled. Please contact the admin about this.")
                  );
               }

               return true;
            }
         } else {
            sender.sendMessage(Messages.PLAYERS_ONLY.getMessage(sender));
            return true;
         }
      } else {
         String playerx = args[0].toLowerCase();
         switch (playerx) {
            case "help":
               if (!Methods.hasPermission(sender, "access")) {
                  return true;
               }

               sender.sendMessage(Messages.HELP.getMessage(sender));
               return true;
            case "reload":
               if (!Methods.hasPermission(sender, "reload")) {
                  return true;
               }

               this.fileManager.reloadFiles().init();
               this.crazyManager.load();
               sender.sendMessage(Messages.RELOAD.getMessage(sender));
               return true;
            case "force_end_all":
               if (!Methods.hasPermission(sender, "force-end-all")) {
                  return true;
               } else {
                  if (sender instanceof Player playerxx) {
                     this.forceEndAll(playerxx);
                     return true;
                  }

                  sender.sendMessage(Messages.PLAYERS_ONLY.getMessage(sender));
                  return true;
               }
            case "view":
               if (!Methods.hasPermission(sender, "view")) {
                  return true;
               } else {
                  if (sender instanceof Player playerxx) {
                     if (args.length >= 2) {
                        GuiListener.openViewer(playerxx, args[1], 1);
                        return true;
                     }

                     sender.sendMessage(Messages.CRAZYAUCTIONS_VIEW.getMessage(sender));
                     return true;
                  }

                  sender.sendMessage(Messages.PLAYERS_ONLY.getMessage(sender));
                  return true;
               }
            case "expired":
            case "collect":
               if (!Methods.hasPermission(sender, "access")) {
                  return true;
               } else {
                  if (sender instanceof Player playerxx) {
                     GuiListener.openPlayersExpiredList(playerxx, 1);
                     return true;
                  }

                  sender.sendMessage(Messages.PLAYERS_ONLY.getMessage(sender));
                  return true;
               }
            case "listed":
               if (!Methods.hasPermission(sender, "access")) {
                  return true;
               } else {
                  if (sender instanceof Player playerxx) {
                     GuiListener.openPlayersCurrentList(playerxx, 1);
                     return true;
                  }

                  sender.sendMessage(Messages.PLAYERS_ONLY.getMessage(sender));
                  return true;
               }
            case "sell":
            case "bid":
               if (sender instanceof Player playerxx) {
                  if (args.length < 2) {
                     sender.sendMessage(Messages.CRAZYAUCTIONS_SELL_BID.getMessage(sender));
                     return true;
                  } else {
                     if (args[0].equalsIgnoreCase("sell")) {
                        if (!this.crazyManager.isSellingEnabled()) {
                           playerxx.sendMessage(Messages.SELLING_DISABLED.getMessage(sender));
                           return true;
                        }

                        if (!Methods.hasPermission(playerxx, "sell")) {
                           return true;
                        }
                     }

                     if (args[0].equalsIgnoreCase("bid")) {
                        if (!this.crazyManager.isBiddingEnabled()) {
                           playerxx.sendMessage(Messages.BIDDING_DISABLED.getMessage(sender));
                           return true;
                        }

                        if (!Methods.hasPermission(playerxx, "bid")) {
                           return true;
                        }
                     }

                     ItemStack item = Methods.getItemInHand(playerxx);
                     int amount = item.getAmount();
                     if (args.length >= 3) {
                        if (!Methods.isInt(args[2])) {
                           Map<String, String> placeholders = new HashMap<>();
                           placeholders.put("%Arg%", args[2]);
                           placeholders.put("%arg%", args[2]);
                           playerxx.sendMessage(Messages.NOT_A_NUMBER.getMessage(sender, placeholders));
                           return true;
                        }

                        amount = Integer.parseInt(args[2]);
                        if (amount <= 0) {
                           amount = 1;
                        }

                        if (amount > item.getAmount()) {
                           amount = item.getAmount();
                        }
                     }

                     if (!Methods.isLong(args[1])) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("%Arg%", args[1]);
                        placeholders.put("%arg%", args[1]);
                        playerxx.sendMessage(Messages.NOT_A_NUMBER.getMessage(sender, placeholders));
                        return true;
                     } else if (Methods.getItemInHand(playerxx).getType() == Material.AIR) {
                        playerxx.sendMessage(Messages.DOESNT_HAVE_ITEM_IN_HAND.getMessage(sender));
                        return false;
                     } else {
                        long price = Long.parseLong(args[1]);
                        if (args[0].equalsIgnoreCase("bid")) {
                           if (price < config.getLong("Settings.Minimum-Bid-Price", 100L)) {
                              playerxx.sendMessage(Messages.BID_PRICE_TO_LOW.getMessage(sender));
                              return true;
                           }

                           if (price > config.getLong("Settings.Max-Beginning-Bid-Price", 1000000L)) {
                              playerxx.sendMessage(Messages.BID_PRICE_TO_HIGH.getMessage(sender));
                              return true;
                           }
                        } else {
                           if (price < config.getLong("Settings.Minimum-Sell-Price", 10L)) {
                              playerxx.sendMessage(Messages.SELL_PRICE_TO_LOW.getMessage(sender));
                              return true;
                           }

                           if (price > config.getLong("Settings.Max-Beginning-Sell-Price", 1000000L)) {
                              playerxx.sendMessage(Messages.SELL_PRICE_TO_HIGH.getMessage(sender));
                              return true;
                           }
                        }

                        if (!playerxx.hasPermission("crazyauctions.bypass")) {
                           int SellLimit = 0;
                           int BidLimit = 0;

                           for (PermissionAttachmentInfo permission : playerxx.getEffectivePermissions()) {
                              String perm = permission.getPermission();
                              if (perm.startsWith("crazyauctions.sell.")) {
                                 perm = perm.replace("crazyauctions.sell.", "");
                                 if (Methods.isInt(perm) && Integer.parseInt(perm) > SellLimit) {
                                    SellLimit = Integer.parseInt(perm);
                                 }
                              }

                              if (perm.startsWith("crazyauctions.bid.")) {
                                 perm = perm.replace("crazyauctions.bid.", "");
                                 if (Methods.isInt(perm) && Integer.parseInt(perm) > BidLimit) {
                                    BidLimit = Integer.parseInt(perm);
                                 }
                              }
                           }

                           for (int i = 1; i < 100; i++) {
                              if (SellLimit < i && playerxx.hasPermission("crazyauctions.sell." + i)) {
                                 SellLimit = i;
                              }

                              if (BidLimit < i && playerxx.hasPermission("crazyauctions.bid." + i)) {
                                 BidLimit = i;
                              }
                           }

                           if (args[0].equalsIgnoreCase("sell") && this.crazyManager.getItems(playerxx, ShopType.SELL).size() >= SellLimit) {
                              playerxx.sendMessage(Messages.MAX_ITEMS.getMessage(sender));
                              return true;
                           }

                           if (args[0].equalsIgnoreCase("bid") && this.crazyManager.getItems(playerxx, ShopType.BID).size() >= BidLimit) {
                              playerxx.sendMessage(Messages.MAX_ITEMS.getMessage(sender));
                              return true;
                           }
                        }

                        if (config.getStringList("Settings.BlackList").contains(item.getType().getKey().getKey())) {
                           playerxx.sendMessage(Messages.ITEM_BLACKLISTED.getMessage(sender));
                           return true;
                        } else {
                           if (!config.getBoolean("Settings.Allow-Damaged-Items", false)) {
                              for (Material i : this.getDamageableItems()) {
                                 if (item.getType() == i && item instanceof Damageable damageable && damageable.getDamage() > 0) {
                                    playerxx.sendMessage(Messages.ITEM_DAMAGED.getMessage(sender));
                                    return true;
                                 }
                              }
                           }

                           String seller = playerxx.getUniqueId().toString();
                           int num = 1;

                           while (data.contains("Items." + num)) {
                              num++;
                           }

                           data.set("Items." + num + ".Price", price);
                           data.set("Items." + num + ".Seller", seller);
                           data.set("Items." + num + ".SellerName", playerxx.getName());
                           if (args[0].equalsIgnoreCase("bid")) {
                              data.set("Items." + num + ".Time-Till-Expire", Methods.convertToMill(config.getString("Settings.Bid-Time", "2m 30s")));
                           } else {
                              data.set("Items." + num + ".Time-Till-Expire", Methods.convertToMill(config.getString("Settings.Sell-Time", "2d")));
                           }

                           data.set("Items." + num + ".Full-Time", Methods.convertToMill(config.getString("Settings.Full-Expire-Time", "10d")));
                           int id = ThreadLocalRandom.current().nextInt(999999);

                           for (String ix : data.getConfigurationSection("Items").getKeys(false)) {
                              if (data.getInt("Items." + ix + ".StoreID") == id) {
                                 id = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
                              }
                           }

                           for (String ixx : data.getConfigurationSection("Items").getKeys(false)) {
                              if (data.getInt("Items." + ixx + ".StoreID") == id) {
                                 id = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
                              }
                           }

                           for (String ixxx : data.getConfigurationSection("Items").getKeys(false)) {
                              if (data.getInt("Items." + ixxx + ".StoreID") == id) {
                                 id = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
                              }
                           }

                           data.set("Items." + num + ".StoreID", id);
                           ShopType type = ShopType.SELL;
                           if (args[0].equalsIgnoreCase("bid")) {
                              data.set("Items." + num + ".Biddable", true);
                              type = ShopType.BID;
                           } else {
                              data.set("Items." + num + ".Biddable", false);
                           }

                           data.set("Items." + num + ".TopBidder", "None");
                           data.set("Items." + num + ".TopBidderName", "None");
                           ItemStack stack = item.clone();
                           stack.setAmount(amount);
                           data.set("Items." + num + ".Item", Methods.toBase64(stack));
                           Files.data.save();
                           this.plugin.getServer().getPluginManager().callEvent(new AuctionListEvent(playerxx, type, stack, price));
                           Map<String, String> placeholders = new HashMap<>();
                           placeholders.put("%Price%", String.valueOf(price));
                           placeholders.put("%price%", String.valueOf(price));
                           playerxx.sendMessage(Messages.ADDED_ITEM_TO_AUCTION.getMessage(sender, placeholders));
                           if (item.getAmount() > 1 && item.getAmount() - amount > 0) {
                              item.setAmount(item.getAmount() - amount);
                           } else {
                              Methods.setItemInHand(playerxx, new ItemStack(Material.AIR));
                           }

                           return false;
                        }
                     }
                  }
               } else {
                  sender.sendMessage(Messages.PLAYERS_ONLY.getMessage(sender));
                  return true;
               }
            default:
               sender.sendMessage(Methods.getPrefix("&cPlease do /crazyauctions help for more information."));
               return true;
         }
      }
   }

   private ArrayList<Material> getDamageableItems() {
      ArrayList<Material> ma = new ArrayList<>();
      ma.add(Material.GOLDEN_HELMET);
      ma.add(Material.GOLDEN_CHESTPLATE);
      ma.add(Material.GOLDEN_LEGGINGS);
      ma.add(Material.GOLDEN_BOOTS);
      ma.add(Material.GOLDEN_HOE);
      ma.add(Material.WOODEN_SWORD);
      ma.add(Material.WOODEN_PICKAXE);
      ma.add(Material.WOODEN_AXE);
      ma.add(Material.WOODEN_SHOVEL);
      ma.add(Material.WOODEN_HOE);
      ma.add(Material.STONE_SHOVEL);
      ma.add(Material.IRON_SHOVEL);
      ma.add(Material.DIAMOND_SHOVEL);
      ma.add(Material.CROSSBOW);
      ma.add(Material.TRIDENT);
      ma.add(Material.TURTLE_HELMET);
      ma.add(Material.DIAMOND_HELMET);
      ma.add(Material.DIAMOND_CHESTPLATE);
      ma.add(Material.DIAMOND_LEGGINGS);
      ma.add(Material.DIAMOND_BOOTS);
      ma.add(Material.CHAINMAIL_HELMET);
      ma.add(Material.CHAINMAIL_CHESTPLATE);
      ma.add(Material.CHAINMAIL_LEGGINGS);
      ma.add(Material.CHAINMAIL_BOOTS);
      ma.add(Material.IRON_HELMET);
      ma.add(Material.IRON_CHESTPLATE);
      ma.add(Material.IRON_LEGGINGS);
      ma.add(Material.IRON_BOOTS);
      ma.add(Material.LEATHER_HELMET);
      ma.add(Material.LEATHER_CHESTPLATE);
      ma.add(Material.LEATHER_LEGGINGS);
      ma.add(Material.LEATHER_BOOTS);
      ma.add(Material.BOW);
      ma.add(Material.STONE_SWORD);
      ma.add(Material.IRON_SWORD);
      ma.add(Material.DIAMOND_SWORD);
      ma.add(Material.STONE_AXE);
      ma.add(Material.IRON_AXE);
      ma.add(Material.DIAMOND_AXE);
      ma.add(Material.STONE_PICKAXE);
      ma.add(Material.IRON_PICKAXE);
      ma.add(Material.DIAMOND_PICKAXE);
      ma.add(Material.STONE_AXE);
      ma.add(Material.IRON_AXE);
      ma.add(Material.DIAMOND_AXE);
      ma.add(Material.STONE_HOE);
      ma.add(Material.IRON_HOE);
      ma.add(Material.DIAMOND_HOE);
      ma.add(Material.FLINT_AND_STEEL);
      ma.add(Material.ANVIL);
      ma.add(Material.FISHING_ROD);
      return ma;
   }

   private void forceEndAll(Player player) {
      FileConfiguration data = Files.data.getConfiguration();
      int num = 1;

      for (String i : data.getConfigurationSection("Items").getKeys(false)) {
         while (data.contains("OutOfTime/Cancelled." + num)) {
            num++;
         }

         String seller = data.getString("Items." + i + ".Seller");
         Player sellerPlayer = Methods.getPlayer(seller);
         if (Methods.isOnline(seller) && sellerPlayer != null) {
            sellerPlayer.sendMessage(Messages.ADMIN_FORCE_CANCELLED_TO_PLAYER.getMessage(player));
         }

         AuctionCancelledEvent event = new AuctionCancelledEvent(
            (OfflinePlayer)(sellerPlayer != null ? sellerPlayer : Methods.getOfflinePlayer(seller)),
            Methods.fromBase64(data.getString("Items." + i + ".Item")),
            Reasons.ADMIN_FORCE_CANCEL
         );
         this.plugin.getServer().getPluginManager().callEvent(event);
         data.set("OutOfTime/Cancelled." + num + ".Seller", data.getString("Items." + i + ".Seller"));
         data.set("OutOfTime/Cancelled." + num + ".Full-Time", data.getLong("Items." + i + ".Full-Time"));
         data.set("OutOfTime/Cancelled." + num + ".StoreID", data.getInt("Items." + i + ".StoreID"));
         data.set("OutOfTime/Cancelled." + num + ".Item", data.getString("Items." + i + ".Item"));
         data.set("Items." + i, null);
      }

      Files.data.save();
      player.sendMessage(Messages.ADMIN_FORCE_CANCELLED_ALL.getMessage(player));
   }
}
