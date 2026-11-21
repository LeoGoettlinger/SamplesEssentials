package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.antiexploit.FarmingProtection;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.combatsystem.ScaledCombatRewardResolver;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.ProceduralItemGenerationSettingsConfig;
import com.magmaguy.elitemobs.config.SpecialItemSystemsConfig;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.customloottable.SharedLootTable;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.WeightedProbability;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class LootTables implements Listener {
   private static boolean proceduralItemsOn;
   private static boolean customItemsOn;
   private static boolean weighedItemsExist;
   private static boolean fixedItemsExist;
   private static boolean limitedItemsExist;
   private static boolean scalableItemsExist;

   public static void generatePlayerLoot(EliteEntity eliteEntity) {
      if (!eliteEntity.isTriggeredAntiExploit()) {
         for (Player player : eliteEntity.getDamagers().keySet()) {
            if (!player.hasMetadata("NPC")
               && PlayerData.isInMemory(player.getUniqueId())
               && !(eliteEntity instanceof InstancedBossEntity instancedBossEntity && instancedBossEntity.getLockoutPlayers().contains(player))
               && !(eliteEntity.getDamagers().get(player) / eliteEntity.getMaxHealth() < 0.1)
               && eliteEntity.isEliteLoot()) {
               int rewardLevel = ScaledCombatRewardResolver.getRewardLevel(eliteEntity, player);
               int effectiveLootLevel = rewardLevel;
               if (eliteEntity instanceof CustomBossEntity || FarmingProtection.shouldDropLoot(player, rewardLevel)) {
                  if (!(eliteEntity instanceof CustomBossEntity)) {
                     effectiveLootLevel = FarmingProtection.getEffectiveMobLevelForLoot(player, rewardLevel);
                  }

                  if (eliteEntity instanceof CustomBossEntity || !FarmingProtection.isPlayerCapped(player)) {
                     double itemLevel = setItemTier(effectiveLootLevel);
                     double eliteLevel = effectiveLootLevel;
                     if (eliteEntity.getPower("bonus_coins.yml") == null) {
                        new ItemLootShower(itemLevel, eliteLevel, eliteEntity.getUnsyncedLivingEntity().getLocation(), player);
                     }

                     if (eliteEntity.isRandomLoot()) {
                        if (PartyManager.shouldUsePartyLoot(player, eliteEntity)) {
                           int partyRewardLevel = eliteEntity instanceof CustomBossEntity
                              ? (int)MobTierCalculator.findMobTier(eliteEntity)
                              : effectiveLootLevel;
                           int partyItemTier = (int)setItemTier(partyRewardLevel);
                           ItemStack partyItem = rollUndeliveredLoot(partyItemTier, eliteEntity);
                           if (partyItem != null && !SharedLootTable.addPartyLoot(eliteEntity, player, partyItem)) {
                              deliverGeneratedItem(player, eliteEntity, partyItem);
                           }
                        } else if (eliteEntity instanceof CustomBossEntity) {
                           generateLoot(eliteEntity, player);
                        } else {
                           generateLoot(eliteEntity, player, effectiveLootLevel);
                        }

                        if (SpecialItemSystemsConfig.isDropSpecialLoot()) {
                           boolean shouldDropSpecialLoot = false;
                           if (eliteEntity instanceof CustomBossEntity customBossEntity
                              && customBossEntity.getCustomBossesConfigFields().getHealthMultiplier() > 1.0
                              && ThreadLocalRandom.current().nextDouble() < SpecialItemSystemsConfig.getBossChanceToDrop()) {
                              shouldDropSpecialLoot = true;
                           } else if (eliteEntity instanceof CustomBossEntity
                              && ThreadLocalRandom.current().nextDouble() < SpecialItemSystemsConfig.getNonEliteChanceToDrop()) {
                              shouldDropSpecialLoot = true;
                           }

                           if (shouldDropSpecialLoot) {
                              if (PartyManager.shouldUsePartyLoot(player, eliteEntity)) {
                                 ItemStack specialItem = generateSpecialLootItem(null, 0, eliteEntity);
                                 if (specialItem != null && !SharedLootTable.addPartyLoot(eliteEntity, player, specialItem)) {
                                    deliverGeneratedItem(player, eliteEntity, specialItem);
                                 }
                              } else {
                                 generateSpecialLoot(player, 0, eliteEntity);
                              }
                           }
                        }

                        if (ItemSettingsConfig.isUseEliteItemScrolls()
                           && ThreadLocalRandom.current().nextDouble() < ItemSettingsConfig.getEliteItemScrollChance()) {
                           ItemStack scrollItem = EliteScroll.generateScroll((int)itemLevel, player);
                           if (deliverDirectly(eliteEntity)) {
                              HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(new ItemStack[]{scrollItem});
                              leftOvers.values().forEach(leftOver -> player.getWorld().dropItem(player.getLocation(), leftOver));
                           } else {
                              Location dropLocation = supplementalDropLocation(eliteEntity, player);
                              dropLocation.getWorld().dropItem(dropLocation, scrollItem);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void initialize() {
      proceduralItemsOn = ProceduralItemGenerationSettingsConfig.isDoProceduralItemDrops();
      customItemsOn = ItemSettingsConfig.isDoEliteMobsLoot() && !CustomItem.getCustomItemStackList().isEmpty();
      weighedItemsExist = CustomItem.getWeighedFixedItems() != null && !CustomItem.getWeighedFixedItems().isEmpty();
      fixedItemsExist = CustomItem.getFixedItems() != null && !CustomItem.getFixedItems().isEmpty();
      limitedItemsExist = CustomItem.getLimitedItems() != null && !CustomItem.getLimitedItems().isEmpty();
      scalableItemsExist = CustomItem.getScalableItems() != null && !CustomItem.getScalableItems().isEmpty();
   }

   private static ItemStack generateLoot(EliteEntity eliteEntity, Player player) {
      return generateLoot(eliteEntity, player, (int)MobTierCalculator.findMobTier(eliteEntity));
   }

   private static ItemStack generateLoot(EliteEntity eliteEntity, Player player, int rewardLevel) {
      int itemTier = (int)setItemTier(rewardLevel);
      return generateLoot(itemTier, eliteEntity, player);
   }

   public static ItemStack generateLoot(int itemTier, EliteEntity eliteEntity, Player player) {
      double baseChance = ItemSettingsConfig.getFlatDropRate();
      if (eliteEntity instanceof RegionalBossEntity) {
         baseChance = ItemSettingsConfig.getRegionalBossNonUniqueDropRate();
      }

      double dropChanceBonus = ItemSettingsConfig.getLevelIncreaseDropRate() * itemTier;
      if (ThreadLocalRandom.current().nextDouble() > baseChance + dropChanceBonus) {
         return null;
      } else {
         List<CustomItem> limited = ScalableItemConstructor.eligibleLimitedItems(itemTier, player);
         HashMap<String, Double> weightedProbability = new HashMap<>();
         if (proceduralItemsOn) {
            weightedProbability.put("procedural", ItemSettingsConfig.getProceduralItemWeight());
         }

         if (customItemsOn) {
            if (weighedItemsExist) {
               weightedProbability.put("weighed", ItemSettingsConfig.getWeighedItemWeight());
            }

            if (fixedItemsExist && CustomItem.getFixedItems().containsKey(itemTier)) {
               weightedProbability.put("fixed", ItemSettingsConfig.getFixedItemWeight());
            }

            if (!limited.isEmpty()) {
               weightedProbability.put("limited", ItemSettingsConfig.getLimitedItemWeight());
            }

            if (scalableItemsExist) {
               weightedProbability.put("scalable", ItemSettingsConfig.getScalableItemWeight());
            }
         }

         String selectedLootSystem = WeightedProbability.pickWeighedProbability(weightedProbability);
         if (selectedLootSystem == null) {
            return null;
         } else {
            switch (selectedLootSystem) {
               case "procedural":
                  return dropProcedurallyGeneratedItem(itemTier, eliteEntity, player);
               case "weighed":
                  return dropWeighedFixedItem(eliteEntity, player);
               case "fixed":
                  return dropFixedItem(eliteEntity, itemTier, player);
               case "limited":
                  return dropLimitedItem(eliteEntity, itemTier, player, limited);
               case "scalable":
                  return dropScalableItem(eliteEntity, itemTier, player);
               default:
                  return null;
            }
         }
      }
   }

   private static ItemStack rollUndeliveredLoot(int itemTier, EliteEntity eliteEntity) {
      double baseChance = ItemSettingsConfig.getFlatDropRate();
      if (eliteEntity instanceof RegionalBossEntity) {
         baseChance = ItemSettingsConfig.getRegionalBossNonUniqueDropRate();
      }

      double dropChanceBonus = ItemSettingsConfig.getLevelIncreaseDropRate() * itemTier;
      if (ThreadLocalRandom.current().nextDouble() > baseChance + dropChanceBonus) {
         return null;
      } else {
         List<CustomItem> limited = ScalableItemConstructor.eligibleLimitedItems(itemTier, null);
         HashMap<String, Double> weightedProbability = new HashMap<>();
         if (proceduralItemsOn) {
            weightedProbability.put("procedural", ItemSettingsConfig.getProceduralItemWeight());
         }

         if (customItemsOn) {
            if (weighedItemsExist) {
               weightedProbability.put("weighed", ItemSettingsConfig.getWeighedItemWeight());
            }

            if (fixedItemsExist && CustomItem.getFixedItems().containsKey(itemTier)) {
               weightedProbability.put("fixed", ItemSettingsConfig.getFixedItemWeight());
            }

            if (!limited.isEmpty()) {
               weightedProbability.put("limited", ItemSettingsConfig.getLimitedItemWeight());
            }

            if (scalableItemsExist) {
               weightedProbability.put("scalable", ItemSettingsConfig.getScalableItemWeight());
            }
         }

         String selectedLootSystem = WeightedProbability.pickWeighedProbability(weightedProbability);
         if (selectedLootSystem == null) {
            return null;
         } else {
            return switch (selectedLootSystem) {
               case "procedural" -> generateProcedurallyGeneratedItem(itemTier, null, eliteEntity);
               case "weighed" -> generateWeighedFixedItemStack(null);
               case "fixed" -> generateFixedItem(itemTier, null, eliteEntity);
               case "limited" -> generateLimitedItem(itemTier, null, eliteEntity, limited);
               case "scalable" -> generateScalableItem(itemTier, null, eliteEntity);
               default -> null;
            };
         }
      }
   }

   public static ItemStack generateLoot(int itemLevel, Location location, Player player) {
      double baseChance = ItemSettingsConfig.getFlatDropRate();
      double dropChanceBonus = ItemSettingsConfig.getLevelIncreaseDropRate() * itemLevel;
      if (ThreadLocalRandom.current().nextDouble() > baseChance + dropChanceBonus) {
         return null;
      } else {
         List<CustomItem> limited = ScalableItemConstructor.eligibleLimitedItems(itemLevel, player);
         HashMap<String, Double> weightedProbability = new HashMap<>();
         if (proceduralItemsOn) {
            weightedProbability.put("procedural", ItemSettingsConfig.getProceduralItemWeight());
         }

         if (customItemsOn) {
            if (weighedItemsExist) {
               weightedProbability.put("weighed", ItemSettingsConfig.getWeighedItemWeight());
            }

            if (fixedItemsExist && CustomItem.getFixedItems().containsKey(itemLevel)) {
               weightedProbability.put("fixed", ItemSettingsConfig.getFixedItemWeight());
            }

            if (!limited.isEmpty()) {
               weightedProbability.put("limited", ItemSettingsConfig.getLimitedItemWeight());
            }

            if (scalableItemsExist) {
               weightedProbability.put("scalable", ItemSettingsConfig.getScalableItemWeight());
            }
         }

         String selectedLootSystem = WeightedProbability.pickWeighedProbability(weightedProbability);
         if (selectedLootSystem == null) {
            return null;
         } else {
            switch (selectedLootSystem) {
               case "procedural":
                  return dropProcedurallyGeneratedItem(itemLevel, location, player);
               case "weighed":
                  return dropWeighedFixedItem(location, player);
               case "fixed":
                  return dropFixedItem(location, itemLevel, player);
               case "limited":
                  return dropLimitedItem(location, itemLevel, player, limited);
               case "scalable":
                  return dropScalableItem(location, itemLevel, player);
               default:
                  return null;
            }
         }
      }
   }

   public static ItemStack generateItemStack(int itemTier, Player player, EliteEntity eliteEntity) {
      List<CustomItem> limited = ScalableItemConstructor.eligibleLimitedItems(itemTier, player);
      HashMap<String, Double> weightedProbability = new HashMap<>();
      if (proceduralItemsOn) {
         weightedProbability.put("procedural", ItemSettingsConfig.getProceduralItemWeight());
      }

      if (customItemsOn) {
         if (weighedItemsExist) {
            weightedProbability.put("weighed", ItemSettingsConfig.getWeighedItemWeight());
         }

         if (fixedItemsExist && CustomItem.getFixedItems().containsKey(itemTier)) {
            weightedProbability.put("fixed", ItemSettingsConfig.getFixedItemWeight());
         }

         if (!limited.isEmpty()) {
            weightedProbability.put("limited", ItemSettingsConfig.getLimitedItemWeight());
         }

         if (scalableItemsExist) {
            weightedProbability.put("scalable", ItemSettingsConfig.getScalableItemWeight());
         }
      }

      String selectedLootSystem = WeightedProbability.pickWeighedProbability(weightedProbability);
      if (selectedLootSystem == null) {
         return null;
      } else {
         switch (selectedLootSystem) {
            case "procedural":
               return generateProcedurallyGeneratedItem(itemTier, player, eliteEntity);
            case "weighed":
               return generateWeighedFixedItemStack(player);
            case "fixed":
               return generateFixedItem(itemTier, player, eliteEntity);
            case "limited":
               return generateLimitedItem(itemTier, player, eliteEntity, limited);
            case "scalable":
               return generateScalableItem(itemTier, player, eliteEntity);
            default:
               return null;
         }
      }
   }

   public static double setItemTier(int mobTier) {
      double chanceToUpgradeTier = 10.0 / mobTier * ItemSettingsConfig.getMaximumItemLevel();
      if (ThreadLocalRandom.current().nextDouble() * 100.0 < chanceToUpgradeTier) {
         return mobTier + 1.0;
      } else {
         double diceRoll = ThreadLocalRandom.current().nextDouble();
         if (diceRoll < 0.1) {
            mobTier -= 2;
         } else if (diceRoll < 0.5) {
            mobTier--;
         }

         if (mobTier < 0) {
            mobTier = 0;
         }

         return mobTier;
      }
   }

   public static ItemStack dropWeighedFixedItem(EliteEntity eliteEntity, Player player) {
      ItemStack itemStack = generateWeighedFixedItemStack(player);
      if (deliverDirectly(eliteEntity)) {
         addToInventoryOrDrop(player, itemStack);
      } else {
         processPhysicalItem(eliteEntity.getLocation(), itemStack, player);
      }

      return itemStack;
   }

   public static ItemStack dropWeighedFixedItem(Location location, Player player) {
      ItemStack itemStack = generateWeighedFixedItemStack(player);
      if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) {
         addToInventoryOrDrop(player, itemStack);
      } else {
         processPhysicalItem(location, itemStack, player);
      }

      return itemStack;
   }

   private static HashMap<ItemStack, Double> eligibleWeightedItems(Player player) {
      HashMap<ItemStack, Double> eligible = new HashMap<>();

      for (Entry<ItemStack, Double> entry : CustomItem.getWeighedFixedItems().entrySet()) {
         String id = ItemTagger.getCustomItemId(entry.getKey());
         CustomItem definition = id == null ? null : CustomItem.getCustomItem(id);
         if (definition != null
            && definition.getItemType() != CustomItem.ItemType.UNIQUE
            && definition.getCustomItemsConfigFields().isEnabled()
            && (player == null || LootItemPolicy.canReceive(definition, player))) {
            eligible.put(entry.getKey(), entry.getValue());
         }
      }

      return eligible;
   }

   private static ItemStack generateWeighedFixedItemStack(Player player) {
      ItemStack selected = WeightedProbability.pick(eligibleWeightedItems(player));
      if (selected == null) {
         return null;
      } else {
         ItemStack result = selected.clone();
         if (LootItemPolicy.shouldSoulbind(result)) {
            SoulbindEnchantment.addEnchantment(result, player);
         }

         return result;
      }
   }

   public static boolean hasAvailableLoot(int tier, Player player) {
      if (proceduralItemsOn && positiveWeight(ItemSettingsConfig.getProceduralItemWeight())) {
         return true;
      } else if (!customItemsOn) {
         return false;
      } else if (weighedItemsExist && positiveWeight(ItemSettingsConfig.getWeighedItemWeight()) && !eligibleWeightedItems(player).isEmpty()) {
         return true;
      } else if (fixedItemsExist
         && positiveWeight(ItemSettingsConfig.getFixedItemWeight())
         && CustomItem.getFixedItems().getOrDefault(tier, new ArrayList<>()).stream().anyMatch(item -> LootItemPolicy.canReceive(item, player))) {
         return true;
      } else {
         return limitedItemsExist
               && positiveWeight(ItemSettingsConfig.getLimitedItemWeight())
               && !ScalableItemConstructor.eligibleLimitedItems(tier, player).isEmpty()
            ? true
            : scalableItemsExist
               && positiveWeight(ItemSettingsConfig.getScalableItemWeight())
               && CustomItem.getScalableItems().stream().anyMatch(item -> LootItemPolicy.canReceive(item, player));
      }
   }

   private static boolean positiveWeight(double weight) {
      return Double.isFinite(weight) && weight > 0.0;
   }

   private static ItemStack dropProcedurallyGeneratedItem(int itemLevel, EliteEntity eliteEntity, Player player) {
      ItemStack itemStack = generateProcedurallyGeneratedItem(itemLevel, player, eliteEntity);
      if (deliverDirectly(eliteEntity)) {
         addToInventoryOrDrop(player, itemStack);
      } else {
         processPhysicalItem(eliteEntity.getLocation(), itemStack, player);
      }

      return itemStack;
   }

   private static ItemStack dropProcedurallyGeneratedItem(int itemLevel, Location location, Player player) {
      ItemStack itemStack = generateProcedurallyGeneratedItem(itemLevel, player, null);
      if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) {
         addToInventoryOrDrop(player, itemStack);
      } else {
         processPhysicalItem(location, itemStack, player);
      }

      return itemStack;
   }

   private static ItemStack generateProcedurallyGeneratedItem(int itemLevel, Player player, EliteEntity eliteEntity) {
      return ItemConstructor.constructItem(itemLevel, eliteEntity, player, false);
   }

   private static ItemStack dropScalableItem(EliteEntity eliteEntity, int itemLevel, Player player) {
      ItemStack itemStack = generateScalableItem(itemLevel, player, eliteEntity);
      if (deliverDirectly(eliteEntity)) {
         addToInventoryOrDrop(player, itemStack);
      } else {
         processPhysicalItem(eliteEntity.getLocation(), itemStack, player);
      }

      return itemStack;
   }

   private static ItemStack dropScalableItem(Location location, int itemLevel, Player player) {
      ItemStack itemStack = generateScalableItem(itemLevel, player, null);
      if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) {
         addToInventoryOrDrop(player, itemStack);
      } else {
         processPhysicalItem(location, itemStack, player);
      }

      return itemStack;
   }

   private static ItemStack generateScalableItem(int itemTier, Player player, EliteEntity eliteEntity) {
      return ScalableItemConstructor.randomizeScalableItem(itemTier, player, eliteEntity);
   }

   private static ItemStack dropLimitedItem(EliteEntity eliteEntity, int itemLevel, Player player, List<CustomItem> limited) {
      ItemStack itemStack = generateLimitedItem(itemLevel, player, eliteEntity, limited);
      if (deliverDirectly(eliteEntity)) {
         addToInventoryOrDrop(player, itemStack);
      } else {
         processPhysicalItem(eliteEntity.getLocation(), itemStack, player);
      }

      return itemStack;
   }

   private static ItemStack dropLimitedItem(Location location, int itemTier, Player player, List<CustomItem> limited) {
      ItemStack itemStack = generateLimitedItem(itemTier, player, null, limited);
      if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) {
         addToInventoryOrDrop(player, itemStack);
      } else {
         processPhysicalItem(location, itemStack, player);
      }

      return itemStack;
   }

   private static ItemStack generateLimitedItem(int itemTier, Player player, EliteEntity eliteEntity, List<CustomItem> limited) {
      return ScalableItemConstructor.randomizeLimitedItem(itemTier, player, eliteEntity, limited);
   }

   private static ItemStack dropFixedItem(EliteEntity eliteEntity, int itemTier, Player player) {
      ItemStack itemStack = generateFixedItem(itemTier, player, eliteEntity);
      if (deliverDirectly(eliteEntity)) {
         addToInventoryOrDrop(player, itemStack);
      } else {
         processPhysicalItem(eliteEntity.getLocation(), itemStack, player);
      }

      return itemStack;
   }

   private static ItemStack dropFixedItem(Location location, int itemTier, Player player) {
      ItemStack itemStack = generateFixedItem(itemTier, player, null);
      if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) {
         addToInventoryOrDrop(player, itemStack);
      } else {
         processPhysicalItem(location, itemStack, player);
      }

      return itemStack;
   }

   private static ItemStack generateFixedItem(int itemTier, Player player, EliteEntity eliteEntity) {
      return CustomItem.getFixedItems()
         .get(itemTier)
         .get(ThreadLocalRandom.current().nextInt(CustomItem.getFixedItems().get(itemTier).size()))
         .generateDefaultsItemStack(player, false, eliteEntity);
   }

   private static void addToInventoryOrDrop(Player player, ItemStack itemStack) {
      if (LootItemPolicy.canReceive(itemStack, player)) {
         HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(new ItemStack[]{itemStack});
         leftOvers.values().forEach(leftOver -> player.getWorld().dropItem(player.getLocation(), leftOver));
      }
   }

   private static void processPhysicalItem(Location location, ItemStack itemStack, Player player) {
      if (LootItemPolicy.canReceive(itemStack, player)) {
         Item item = location.getWorld().dropItem(location, itemStack);
         if (item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta().hasDisplayName()) {
            item.setCustomName(item.getItemStack().getItemMeta().getDisplayName());
            item.setCustomNameVisible(true);
         }

         SoulbindEnchantment.addPhysicalDisplay(item, player);
         RareDropEffect.runEffect(item);
      }
   }

   public static void generateSpecialLoot(Player player, int level, EliteEntity eliteEntity) {
      ItemStack specialItem = generateSpecialLootItem(player, level, eliteEntity);
      if (specialItem != null) {
         if (deliverDirectly(eliteEntity)) {
            HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(new ItemStack[]{specialItem});
            leftOvers.values().forEach(leftOver -> player.getWorld().dropItem(player.getLocation(), leftOver));
         } else {
            Location dropLocation = supplementalDropLocation(eliteEntity, player);
            dropLocation.getWorld().dropItem(dropLocation, specialItem);
         }
      }
   }

   private static ItemStack generateSpecialLootItem(Player player, int level, EliteEntity eliteEntity) {
      CustomItem customItem = WeightedProbability.pickWeighedProbabilityFromCustomItems(SpecialItemSystemsConfig.getSpecialValues());
      return customItem == null ? null : customItem.generateItemStack(level, player, eliteEntity);
   }

   public static void deliverGeneratedItem(Player player, Location location, ItemStack itemStack) {
      deliverGeneratedItem(player, location, itemStack, ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory());
   }

   static void deliverGeneratedItem(Player player, EliteEntity source, ItemStack itemStack) {
      deliverGeneratedItem(player, source.getLocation(), itemStack, deliverDirectly(source));
   }

   private static boolean deliverDirectly(EliteEntity source) {
      return ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory() && !isExternalInstance(source);
   }

   private static Location supplementalDropLocation(EliteEntity source, Player player) {
      return isExternalInstance(source) ? source.getLocation() : player.getLocation();
   }

   private static boolean isExternalInstance(EliteEntity source) {
      return source instanceof InstancedBossEntity instance && instance.getDungeonInstance() == null;
   }

   private static void deliverGeneratedItem(Player player, Location location, ItemStack itemStack, boolean direct) {
      if (LootItemPolicy.canReceive(itemStack, player)) {
         if (LootItemPolicy.shouldSoulbind(itemStack)) {
            SoulbindEnchantment.addEnchantment(itemStack, player);
         }

         if (direct) {
            HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(new ItemStack[]{itemStack});
            leftOvers.values().forEach(leftOver -> player.getWorld().dropItem(player.getLocation(), leftOver));
         } else {
            processPhysicalItem(location, itemStack, player);
         }
      }
   }

   @EventHandler
   public void onDeath(EliteMobDeathEvent event) {
      if (event.getEntityDeathEvent() != null && !event.getEliteEntity().isVanillaLoot()) {
         event.getEntityDeathEvent().getDrops().clear();
      }

      if (event.getEliteEntity().isEliteLoot()) {
         if (event.getEliteEntity().getLevel() >= 1) {
            if (!event.getEliteEntity().getDamagers().isEmpty()) {
               generatePlayerLoot(event.getEliteEntity());
            }
         }
      }
   }
}
