package com.magmaguy.elitemobs.advancedcombat;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

final class AdvancedFoodItems {
   private static final String MARKER = "advanced_combat_food";
   private static final String DEFAULT_COMPONENT = "advanced_combat_food_default";
   private static final String NUTRITION = "advanced_combat_food_nutrition";
   private static final String SATURATION = "advanced_combat_food_saturation";
   private static final String ALWAYS_EDIBLE = "advanced_combat_food_always_edible";
   private static final String CONSUMPTION_SECONDS = "advanced_combat_food_consumption_seconds";
   private static final float CONSUMPTION_TIME_MULTIPLIER = 2.0F;
   private static final Map<Material, AdvancedFoodItems.FoodSnapshot> VANILLA_FOOD = vanillaFoodProperties();

   private AdvancedFoodItems() {
   }

   static void preparePlayerInventory(Player player) {
      if (!AdvancedCombatRuntime.isActive(player)) {
         restorePlayerInventory(player);
      } else {
         adaptInventory(player.getInventory());
      }
   }

   static void restorePlayerInventory(Player player) {
      restoreInventory(player.getInventory());
      restoreItem(player.getItemOnCursor());
   }

   static void restoreInventory(Inventory inventory) {
      for (int slot = 0; slot < inventory.getSize(); slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (restoreItem(stack)) {
            inventory.setItem(slot, stack);
         }
      }
   }

   static void restoreDroppedItem(Item item) {
      ItemStack stack = item.getItemStack();
      if (restoreItem(stack)) {
         item.setItemStack(stack);
      }
   }

   static boolean restoreItem(ItemStack stack) {
      if (stack != null && !stack.getType().isAir()) {
         ItemMeta meta = stack.getItemMeta();
         if (meta == null) {
            return false;
         } else {
            PersistentDataContainer data = meta.getPersistentDataContainer();
            NamespacedKey marker = key("advanced_combat_food");
            if (!data.has(marker, PersistentDataType.BYTE)) {
               return false;
            } else {
               Float originalSeconds = (Float)data.get(key("advanced_combat_food_consumption_seconds"), PersistentDataType.FLOAT);
               if (originalSeconds != null && NMSManager.getAdapter() == null) {
                  return false;
               } else {
                  boolean defaultComponent = byteValue(data, "advanced_combat_food_default") != 0;
                  Integer nutrition = (Integer)data.get(key("advanced_combat_food_nutrition"), PersistentDataType.INTEGER);
                  Float saturation = (Float)data.get(key("advanced_combat_food_saturation"), PersistentDataType.FLOAT);
                  boolean alwaysEdible = byteValue(data, "advanced_combat_food_always_edible") != 0;
                  removeAdapterData(data);
                  if (defaultComponent) {
                     meta.setFood(null);
                  } else if (nutrition != null && saturation != null) {
                     FoodComponent food = meta.getFood();
                     food.setNutrition(nutrition);
                     food.setSaturation(saturation);
                     food.setCanAlwaysEat(alwaysEdible);
                     meta.setFood(food);
                  }

                  ItemStack restored = stack.clone();
                  restored.setItemMeta(meta);
                  if (originalSeconds != null) {
                     restored = NMSManager.getAdapter().withConsumptionSeconds(restored, originalSeconds);
                  }

                  stack.setItemMeta(restored.getItemMeta());
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   private static void adaptInventory(Inventory inventory) {
      for (int slot = 0; slot < inventory.getSize(); slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (adaptItem(stack)) {
            inventory.setItem(slot, stack);
         }
      }
   }

   private static boolean adaptItem(ItemStack stack) {
      if (stack != null && !stack.getType().isAir()) {
         ItemMeta meta = stack.getItemMeta();
         if (meta == null) {
            return false;
         } else {
            AdvancedFoodItems.FoodSnapshot food = effectiveFood(stack, meta);
            if (food != null && NMSManager.getAdapter() != null) {
               PersistentDataContainer data = meta.getPersistentDataContainer();
               if (data.has(key("advanced_combat_food_consumption_seconds"), PersistentDataType.FLOAT)) {
                  return false;
               } else {
                  float originalSeconds = NMSManager.getAdapter().getConsumptionSeconds(stack);
                  if (originalSeconds < 0.0F) {
                     return false;
                  } else {
                     float adaptedSeconds = originalSeconds * 2.0F;
                     if (!Float.isFinite(adaptedSeconds)) {
                        return false;
                     } else {
                        if (!data.has(key("advanced_combat_food"), PersistentDataType.BYTE)) {
                           data.set(key("advanced_combat_food"), PersistentDataType.BYTE, (byte)1);
                           data.set(key("advanced_combat_food_default"), PersistentDataType.BYTE, Byte.valueOf((byte)(meta.hasFood() ? 0 : 1)));
                           data.set(key("advanced_combat_food_nutrition"), PersistentDataType.INTEGER, food.nutrition());
                           data.set(key("advanced_combat_food_saturation"), PersistentDataType.FLOAT, food.saturation());
                           data.set(key("advanced_combat_food_always_edible"), PersistentDataType.BYTE, Byte.valueOf((byte)(food.alwaysEdible() ? 1 : 0)));
                        }

                        data.set(key("advanced_combat_food_consumption_seconds"), PersistentDataType.FLOAT, originalSeconds);
                        FoodComponent adapted = meta.getFood();
                        adapted.setNutrition(food.nutrition());
                        adapted.setSaturation(food.saturation());
                        adapted.setCanAlwaysEat(true);
                        meta.setFood(adapted);
                        ItemStack timed = stack.clone();
                        timed.setItemMeta(meta);
                        timed = NMSManager.getAdapter().withConsumptionSeconds(timed, adaptedSeconds);
                        stack.setItemMeta(timed.getItemMeta());
                        return true;
                     }
                  }
               }
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   static double saturationOf(ItemStack stack) {
      if (stack != null && !stack.getType().isAir()) {
         ItemMeta meta = stack.getItemMeta();
         if (meta == null) {
            return 0.0;
         } else {
            AdvancedFoodItems.FoodSnapshot food = effectiveFood(stack, meta);
            return food != null && Float.isFinite(food.saturation()) ? Math.max(0.0, (double)food.saturation()) : 0.0;
         }
      } else {
         return 0.0;
      }
   }

   static boolean isFood(ItemStack stack) {
      if (stack != null && !stack.getType().isAir()) {
         ItemMeta meta = stack.getItemMeta();
         return meta != null && effectiveFood(stack, meta) != null;
      } else {
         return false;
      }
   }

   static Set<Material> vanillaFoodMaterials() {
      return VANILLA_FOOD.keySet();
   }

   static double vanillaSaturation(Material material) {
      AdvancedFoodItems.FoodSnapshot food = VANILLA_FOOD.get(material);
      return food == null ? 0.0 : food.saturation();
   }

   private static AdvancedFoodItems.FoodSnapshot effectiveFood(ItemStack stack, ItemMeta meta) {
      if (!meta.hasFood()) {
         return VANILLA_FOOD.get(stack.getType());
      } else {
         FoodComponent food = meta.getFood();
         return new AdvancedFoodItems.FoodSnapshot(food.getNutrition(), food.getSaturation(), food.canAlwaysEat());
      }
   }

   private static Map<Material, AdvancedFoodItems.FoodSnapshot> vanillaFoodProperties() {
      Map<Material, AdvancedFoodItems.FoodSnapshot> foods = new EnumMap<>(Material.class);
      add(foods, Material.APPLE, 4, 2.4F);
      add(foods, Material.BAKED_POTATO, 5, 6.0F);
      add(foods, Material.BEEF, 3, 1.8F);
      add(foods, Material.BEETROOT, 1, 1.2F);
      add(foods, Material.BEETROOT_SOUP, 6, 7.2F);
      add(foods, Material.BREAD, 5, 6.0F);
      add(foods, Material.CARROT, 3, 3.6F);
      add(foods, Material.CHICKEN, 2, 1.2F);
      add(foods, Material.CHORUS_FRUIT, 4, 2.4F, true);
      add(foods, Material.COD, 2, 0.4F);
      add(foods, Material.COOKED_BEEF, 8, 12.8F);
      add(foods, Material.COOKED_CHICKEN, 6, 7.2F);
      add(foods, Material.COOKED_COD, 5, 6.0F);
      add(foods, Material.COOKED_MUTTON, 6, 9.6F);
      add(foods, Material.COOKED_PORKCHOP, 8, 12.8F);
      add(foods, Material.COOKED_RABBIT, 5, 6.0F);
      add(foods, Material.COOKED_SALMON, 6, 9.6F);
      add(foods, Material.COOKIE, 2, 0.4F);
      add(foods, Material.DRIED_KELP, 1, 0.6F);
      add(foods, Material.ENCHANTED_GOLDEN_APPLE, 4, 9.6F, true);
      add(foods, Material.GLOW_BERRIES, 2, 0.4F);
      add(foods, Material.GOLDEN_APPLE, 4, 9.6F, true);
      add(foods, Material.GOLDEN_CARROT, 6, 14.4F);
      add(foods, Material.HONEY_BOTTLE, 6, 1.2F, true);
      add(foods, Material.MELON_SLICE, 2, 1.2F);
      add(foods, Material.MUSHROOM_STEW, 6, 7.2F);
      add(foods, Material.MUTTON, 2, 1.2F);
      add(foods, Material.POISONOUS_POTATO, 2, 1.2F);
      add(foods, Material.PORKCHOP, 3, 1.8F);
      add(foods, Material.POTATO, 1, 0.6F);
      add(foods, Material.PUFFERFISH, 1, 0.2F);
      add(foods, Material.PUMPKIN_PIE, 8, 4.8F);
      add(foods, Material.RABBIT, 3, 1.8F);
      add(foods, Material.RABBIT_STEW, 10, 12.0F);
      add(foods, Material.ROTTEN_FLESH, 4, 0.8F);
      add(foods, Material.SALMON, 2, 0.4F);
      add(foods, Material.SPIDER_EYE, 2, 3.2F);
      add(foods, Material.SUSPICIOUS_STEW, 6, 7.2F);
      add(foods, Material.SWEET_BERRIES, 2, 0.4F);
      add(foods, Material.TROPICAL_FISH, 1, 0.2F);
      return Map.copyOf(foods);
   }

   private static void add(Map<Material, AdvancedFoodItems.FoodSnapshot> foods, Material material, int nutrition, float saturation) {
      add(foods, material, nutrition, saturation, false);
   }

   private static void add(Map<Material, AdvancedFoodItems.FoodSnapshot> foods, Material material, int nutrition, float saturation, boolean alwaysEdible) {
      foods.put(material, new AdvancedFoodItems.FoodSnapshot(nutrition, saturation, alwaysEdible));
   }

   private static byte byteValue(PersistentDataContainer data, String key) {
      Byte value = (Byte)data.get(key(key), PersistentDataType.BYTE);
      return value == null ? 0 : value;
   }

   private static void removeAdapterData(PersistentDataContainer data) {
      data.remove(key("advanced_combat_food"));
      data.remove(key("advanced_combat_food_default"));
      data.remove(key("advanced_combat_food_nutrition"));
      data.remove(key("advanced_combat_food_saturation"));
      data.remove(key("advanced_combat_food_always_edible"));
      data.remove(key("advanced_combat_food_consumption_seconds"));
   }

   private static NamespacedKey key(String value) {
      return new NamespacedKey(MetadataHandler.PLUGIN, value);
   }

   private record FoodSnapshot(int nutrition, float saturation, boolean alwaysEdible) {
   }
}
