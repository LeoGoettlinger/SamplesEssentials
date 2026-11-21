package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeItem;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.Round;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.HarvesterSkill;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import com.magmaguy.elitemobs.utils.MessageThrottler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ItemLootShower {
   public static HashMap<UUID, ItemLootShower.LegacyCoin> coinValues = new HashMap<>();
   private static Boolean useFakeItems = null;
   private final Player player;

   public ItemLootShower(double itemLevel, final double mobLevel, Location location, final Player player) {
      this.player = player;
      if (ElitePlayerInventory.playerInventories.get(player.getUniqueId()) != null) {
         if (EconomySettingsConfig.isEnableCurrencyShower()) {
            if (useFakeItems() || SoulbindEnchantment.isEnabled) {
               if (Math.abs(mobLevel - ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(false))
                  > ItemSettingsConfig.getLootLevelDifferenceLockout()) {
                  (new BukkitRunnable() {
                        int counter = 0;

                        public void run() {
                           this.counter++;
                           if (player.isValid() && this.counter <= 100) {
                              ActionBarCompositor.show(
                                 player,
                                 ActionBarCompositor.Source.CRITICAL_WARNING,
                                 ItemSettingsConfig.getLevelRangeTooDifferent()
                                    .replace("$playerLevel", ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getFullPlayerTier(false) + "")
                                    .replace("$bossLevel", (int)mobLevel + "")
                              );
                           } else {
                              this.cancel();
                           }
                        }
                     })
                     .runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
               } else {
                  if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) {
                     this.addDirectly(getCurrencyAmount(itemLevel));
                  } else {
                     this.addIndirectly(location, HarvesterSkill.applyEliteCoinBonus(player, getCurrencyAmount(itemLevel)));
                  }
               }
            }
         }
      }
   }

   public static void shutdown() {
      coinValues.clear();
      useFakeItems = null;
   }

   private static boolean useFakeItems() {
      if (useFakeItems == null) {
         useFakeItems = NMSManager.getAdapter().supportsFakeItems();
      }

      return useFakeItems;
   }

   public ItemLootShower(Location location, Player player, int amount) {
      this.player = player;
      this.addIndirectly(location, HarvesterSkill.applyEliteCoinBonus(player, amount));
   }

   private void addIndirectly(final Location location, final int currencyAmount2) {
      (new BukkitRunnable() {
         int currencyAmount = currencyAmount2;

         public void run() {
            if (this.currencyAmount <= 0) {
               this.cancel();
            } else if (this.currencyAmount >= 1000 && ThreadLocalRandom.current().nextDouble() < 0.65) {
               ItemLootShower.this.dropOneThousand(location);
               this.currencyAmount -= 1000;
            } else if (this.currencyAmount >= 500 && ThreadLocalRandom.current().nextDouble() < 0.65) {
               ItemLootShower.this.dropFiveHundred(location);
               this.currencyAmount -= 500;
            } else {
               if (this.currencyAmount >= 100) {
                  if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                     ItemLootShower.this.dropOneHundred(location);
                     this.currencyAmount -= 100;
                  }
               } else if (this.currencyAmount >= 50) {
                  if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                     ItemLootShower.this.dropFifty(location);
                     this.currencyAmount -= 50;
                  }
               } else if (this.currencyAmount >= 20) {
                  if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                     ItemLootShower.this.dropTwenty(location);
                     this.currencyAmount -= 20;
                  }
               } else if (this.currencyAmount >= 10) {
                  if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                     ItemLootShower.this.dropTen(location);
                     this.currencyAmount -= 10;
                  }
               } else if (this.currencyAmount >= 5) {
                  if (ThreadLocalRandom.current().nextDouble() < 0.65) {
                     ItemLootShower.this.dropFive(location);
                     this.currencyAmount -= 5;
                  }
               } else {
                  ItemLootShower.this.dropOne(location);
                  this.currencyAmount--;
               }
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 2L, 2L);
   }

   public static int getCurrencyAmount(double eliteMobTier) {
      return (int)(eliteMobTier / 2.0 * EconomySettingsConfig.getCurrencyShowerMultiplier());
   }

   private void addDirectly(int baseCurrencyAmount) {
      int currencyAmount = HarvesterSkill.applyEliteCoinBonus(this.player, baseCurrencyAmount);
      EconomyHandler.addCurrency(this.player.getUniqueId(), currencyAmount);
      this.player
         .sendMessage(
            EconomySettingsConfig.getChatCurrencyShowerMessage()
               .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
               .replace("$amount", currencyAmount + "")
         );
   }

   private void generateCurrencyItem(Material material, Location location, double value, String colorCode) {
      if (useFakeItems()) {
         this.generateFakeCurrencyItem(material, location, value, colorCode);
      } else {
         this.generateLegacyCurrencyItem(material, location, value, colorCode);
      }
   }

   private void generateFakeCurrencyItem(Material material, Location location, double value, String colorCode) {
      ItemStack currencyItemStack = ItemStackGenerator.generateItemStack(
         material, "", new ArrayList<>(List.of("EliteMobsCurrencyItem", value + "", ThreadLocalRandom.current().nextDouble() + ""))
      );
      String model = null;

      try {
         model = EconomySettingsConfig.getThisConfiguration().getString("lootShowerDataV2." + (int)value);
      } catch (Exception var10) {
         Logger.warn("Failed to get coin model for value " + value + " !");
         var10.printStackTrace();
      }

      if (model == null) {
         Logger.warn("No model found for value " + value + " !");
      } else {
         this.setCoinModel(currencyItemStack, model);
      }

      Location spawnLocation = location.clone().add(0.0, 1.0, 0.0);
      FakeItem fakeItem = NMSManager.getAdapter()
         .fakeItemBuilder()
         .itemStack(currencyItemStack)
         .billboard(Billboard.CENTER)
         .scale(0.6F)
         .customName(ChatColorConverter.convert(colorCode + (int)value + " " + EconomySettingsConfig.getCurrencyName()))
         .customNameVisible(true)
         .build(spawnLocation);
      fakeItem.displayTo(this.player);
      new ItemLootShower.FakeCoin(value, this.player, fakeItem, spawnLocation);
   }

   private void generateLegacyCurrencyItem(Material material, Location location, double value, String colorCode) {
      ItemStack currencyItemStack = SoulbindEnchantment.addEnchantment(
         ItemStackGenerator.generateItemStack(
            material, "", new ArrayList<>(List.of("EliteMobsCurrencyItem", value + "", ThreadLocalRandom.current().nextDouble() + ""))
         ),
         this.player
      );
      String model = null;

      try {
         model = EconomySettingsConfig.getThisConfiguration().getString("lootShowerDataV2." + (int)value);
      } catch (Exception var9) {
         Logger.warn("Failed to get coin model for value " + value + " !");
         var9.printStackTrace();
      }

      if (model == null) {
         Logger.warn("No model found for value " + value + " !");
      } else {
         this.setCoinModel(currencyItemStack, model);
      }

      Item currencyItem = location.getWorld().dropItem(location.clone().add(new Vector(0, 1, 0)), currencyItemStack);
      EntityTracker.registerVisualEffects(currencyItem);
      currencyItem.setInvulnerable(true);
      currencyItem.setVelocity(new Vector((ThreadLocalRandom.current().nextDouble() - 0.5) / 2.0, 0.5, (ThreadLocalRandom.current().nextDouble() - 0.5) / 2.0));
      SoulbindEnchantment.addPhysicalDisplay(currencyItem, this.player);
      currencyItem.setCustomName(ChatColorConverter.convert(colorCode + (int)value + " " + EconomySettingsConfig.getCurrencyName()));
      currencyItem.setCustomNameVisible(true);
      new ItemLootShower.LegacyCoin(value, this.player, currencyItem);
   }

   private void dropOne(Location location) {
      try {
         this.generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial1()), location, 1.0, "&7");
      } catch (Exception var3) {
         Logger.warn("Material for EliteMob shower 1 is invalid. Defaulting to gold nugget.");
         this.generateCurrencyItem(Material.GOLD_NUGGET, location, 1.0, "&7");
      }
   }

   private void dropFive(Location location) {
      try {
         this.generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial5()), location, 5.0, "&f");
      } catch (Exception var3) {
         Logger.warn("Material for EliteMob shower 5 is invalid. Defaulting to gold ingot.");
         this.generateCurrencyItem(Material.GOLD_INGOT, location, 5.0, "&f");
      }
   }

   private void dropTen(Location location) {
      try {
         this.generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial10()), location, 10.0, "&a");
      } catch (Exception var3) {
         Logger.warn("Material for EliteMob shower 10 is invalid. Defaulting to Gold block.");
         this.generateCurrencyItem(Material.GOLD_BLOCK, location, 10.0, "&a");
      }
   }

   private void dropTwenty(Location location) {
      try {
         this.generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial20()), location, 20.0, "&2");
      } catch (Exception var3) {
         Logger.warn("Material for EliteMob shower 20 is invalid. Defaulting to emerald.");
         this.generateCurrencyItem(Material.EMERALD, location, 20.0, "&2");
      }
   }

   private void dropFifty(Location location) {
      try {
         this.generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial50()), location, 50.0, "&2");
      } catch (Exception var3) {
         Logger.warn("Material for EliteMob shower 50 is invalid. Defaulting to emerald block.");
         this.generateCurrencyItem(Material.EMERALD_BLOCK, location, 50.0, "&2");
      }
   }

   private void dropOneHundred(Location location) {
      try {
         this.generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial100()), location, 100.0, "&2");
      } catch (Exception var3) {
         Logger.warn("Material for EliteMob shower 100 is invalid. Defaulting to diamond.");
         this.generateCurrencyItem(Material.DIAMOND, location, 100.0, "&2");
      }
   }

   private void dropFiveHundred(Location location) {
      try {
         this.generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial500()), location, 500.0, "&2");
      } catch (Exception var3) {
         Logger.warn("Material for EliteMob shower 500 is invalid. Defaulting to diamond block.");
         this.generateCurrencyItem(Material.DIAMOND_BLOCK, location, 500.0, "&2");
      }
   }

   private void dropOneThousand(Location location) {
      try {
         this.generateCurrencyItem(Material.getMaterial(EconomySettingsConfig.getLootShowerMaterial1000()), location, 1000.0, "&2");
      } catch (Exception var3) {
         Logger.warn("Material for EliteMob shower 1000 is invalid. Defaulting to nether star.");
         this.generateCurrencyItem(Material.NETHER_STAR, location, 1000.0, "&2");
      }
   }

   private ItemStack setCoinModel(ItemStack itemStack, String data) {
      CustomModelAdder.addCustomModel(itemStack, data);
      return itemStack;
   }

   private class FakeCoin {
      private final double value;
      private final FakeItem fakeItem;
      private Location currentLocation;
      private Vector velocity;

      public FakeCoin(double value, Player player, FakeItem fakeItem, Location spawnLocation) {
         this.value = value;
         this.fakeItem = fakeItem;
         this.currentLocation = spawnLocation.clone();
         this.velocity = new Vector((ThreadLocalRandom.current().nextDouble() - 0.5) * 0.3, 0.4, (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.3);
         (new BukkitRunnable() {
            int counter = 0;
            final int LAUNCH_TICKS = 15;
            final float ROTATION_PER_TICK = 9.0F;
            boolean attracting = false;
            float rotation = 0.0F;

            public void run() {
               if (this.counter >= 5999) {
                  this.cancel();
                  fakeItem.remove();
               } else {
                  Player targetPlayer = player.isOnline() ? player : null;
                  if (targetPlayer != null && targetPlayer.isValid()) {
                     if (!targetPlayer.getWorld().equals(FakeCoin.this.currentLocation.getWorld())) {
                        this.cancel();
                        fakeItem.remove();
                     } else {
                        double distanceSquared = targetPlayer.getLocation().add(0.0, 1.0, 0.0).distanceSquared(FakeCoin.this.currentLocation);
                        if (distanceSquared <= 1.5) {
                           this.cancel();
                           FakeCoin.this.collectCoin(targetPlayer);
                        } else if (this.counter > 200 && distanceSquared > 900.0) {
                           this.cancel();
                           fakeItem.remove();
                        } else {
                           this.rotation += 9.0F;
                           if (this.rotation >= 360.0F) {
                              this.rotation -= 360.0F;
                           }

                           fakeItem.setYawRotation(this.rotation);
                           if (this.counter < 15) {
                              FakeCoin.this.velocity.setY(FakeCoin.this.velocity.getY() - 0.04);
                              FakeCoin.this.currentLocation.add(FakeCoin.this.velocity);
                              fakeItem.teleport(FakeCoin.this.currentLocation);
                           } else {
                              if (!this.attracting) {
                                 this.attracting = true;
                                 FakeCoin.this.velocity = new Vector(0, 0, 0);
                              }

                              Location targetLoc = targetPlayer.getLocation().add(0.0, 1.0, 0.0);
                              Vector direction = targetLoc.clone().subtract(FakeCoin.this.currentLocation).toVector();
                              if (direction.lengthSquared() > 0.01) {
                                 direction.normalize().multiply(0.35);
                                 FakeCoin.this.currentLocation.add(direction);
                                 fakeItem.teleport(FakeCoin.this.currentLocation);
                              }
                           }

                           this.counter++;
                        }
                     }
                  } else {
                     this.cancel();
                     fakeItem.remove();
                  }
               }
            }
         }).runTaskTimer(MetadataHandler.PLUGIN, 1L, 1L);
      }

      private void collectCoin(Player targetPlayer) {
         this.fakeItem.remove();
         EconomyHandler.addCurrency(targetPlayer.getUniqueId(), this.value);
         double runningTotal = MessageThrottler.pushCurrency(targetPlayer, this.value);
         ActionBarCompositor.show(
            targetPlayer,
            ActionBarCompositor.Source.ECONOMY,
            EconomySettingsConfig.getActionBarCurrencyShowerMessage()
               .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
               .replace("$amount", Round.twoDecimalPlaces(runningTotal) + "")
         );
      }
   }

   public static class ItemLootShowerEvents implements Listener {
      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public static void onItemPickup(EntityPickupItemEvent event) {
         if (ItemLootShower.coinValues.containsKey(event.getItem().getUniqueId())) {
            event.setCancelled(true);
            if (event.getEntity().getType().equals(EntityType.PLAYER)) {
               ItemLootShower.LegacyCoin coin = ItemLootShower.coinValues.get(event.getItem().getUniqueId());
               if (coin.pickupable) {
                  ItemLootShower.coinValues.remove(event.getItem().getUniqueId());
                  double amountIncremented = coin.value;
                  Player player = (Player)event.getEntity();
                  event.getItem().remove();
                  EconomyHandler.addCurrency(player.getUniqueId(), amountIncremented);
                  double runningTotal = MessageThrottler.pushCurrency(player, amountIncremented);
                  ActionBarCompositor.show(
                     player,
                     ActionBarCompositor.Source.ECONOMY,
                     EconomySettingsConfig.getActionBarCurrencyShowerMessage()
                        .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                        .replace("$amount", Round.twoDecimalPlaces(runningTotal) + "")
                  );
               }
            }
         }
      }

      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public static void onItemPickup(InventoryPickupItemEvent event) {
         if (ItemLootShower.coinValues.containsKey(event.getItem().getUniqueId())) {
            event.setCancelled(true);
         }
      }
   }

   private class LegacyCoin {
      UUID player;
      UUID item;
      double value;
      boolean pickupable;

      public LegacyCoin(double value, Player player, Item item) {
         this.player = player.getUniqueId();
         this.value = value;
         this.item = item.getUniqueId();
         ItemLootShower.coinValues.put(item.getUniqueId(), this);
         this.pickupable = false;
         item.setGravity(false);
         (new BukkitRunnable() {
            public void run() {
               if (ItemLootShower.coinValues.containsKey(item.getUniqueId())) {
                  if (Bukkit.getEntity(item.getUniqueId()) != null) {
                     Bukkit.getEntity(item.getUniqueId()).remove();
                  }

                  ItemLootShower.coinValues.remove(item.getUniqueId());
               }
            }
         }).runTaskLater(MetadataHandler.PLUGIN, 6000L);
         (new BukkitRunnable() {
               int counter = 0;

               public void run() {
                  if (item.isValid()
                     && player.isValid()
                     && player.getWorld().equals(item.getWorld())
                     && this.counter <= 80
                     && !(item.getLocation().distanceSquared(player.getLocation()) > 900.0)) {
                     item.setVelocity(player.getLocation().clone().subtract(item.getLocation()).toVector().normalize().multiply(0.2));
                     if (player.getLocation().distanceSquared(item.getLocation()) <= 1.0) {
                        item.remove();
                        EconomyHandler.addCurrency(player.getUniqueId(), value);
                        double runningTotal = MessageThrottler.pushCurrency(player, value);
                        ActionBarCompositor.show(
                           player,
                           ActionBarCompositor.Source.ECONOMY,
                           EconomySettingsConfig.getActionBarCurrencyShowerMessage()
                              .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                              .replace("$amount", Round.twoDecimalPlaces(runningTotal) + "")
                        );
                        ItemLootShower.coinValues.remove(item.getUniqueId());
                        this.cancel();
                     } else {
                        this.counter++;
                     }
                  } else {
                     this.cancel();
                     LegacyCoin.this.pickupable = true;
                     item.setGravity(true);
                  }
               }
            })
            .runTaskTimer(MetadataHandler.PLUGIN, 1L, 1L);
      }
   }
}
