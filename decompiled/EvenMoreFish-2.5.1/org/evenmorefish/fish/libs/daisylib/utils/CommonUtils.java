package org.evenmorefish.fish.libs.daisylib.utils;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CommonUtils {
   public static boolean classExists(@NonNull String className) {
      try {
         Class.forName(className);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }

   public static boolean isInt(@NonNull String str) {
      try {
         Integer.parseInt(str);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   @Nullable
   public static Integer getInt(@NonNull String str) {
      try {
         return Integer.parseInt(str);
      } catch (NumberFormatException var2) {
         return null;
      }
   }

   public static int getIntOrDefault(@Nullable String str, int defaultInt) {
      if (str == null) {
         return defaultInt;
      } else {
         Integer value = getInt(str);
         return value == null ? defaultInt : value;
      }
   }

   public static boolean isLong(@NonNull String str) {
      try {
         Long.parseLong(str);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   @Nullable
   public static Long getLong(@NonNull String str) {
      try {
         return Long.parseLong(str);
      } catch (NumberFormatException var2) {
         return null;
      }
   }

   public static long getLongOrDefault(@Nullable String str, long defaultLong) {
      if (str == null) {
         return defaultLong;
      } else {
         Long value = getLong(str);
         return value == null ? defaultLong : value;
      }
   }

   public static boolean isDouble(@NonNull String str) {
      try {
         Double.parseDouble(str);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   @Nullable
   public static Double getDouble(@NonNull String str) {
      try {
         return Double.parseDouble(str);
      } catch (NumberFormatException var2) {
         return null;
      }
   }

   public static double getDoubleOrDefault(@Nullable String str, double defaultDouble) {
      if (str == null) {
         return defaultDouble;
      } else {
         Double value = getDouble(str);
         return value == null ? defaultDouble : value;
      }
   }

   public static boolean isFloat(@NonNull String str) {
      try {
         Float.parseFloat(str);
         return true;
      } catch (NumberFormatException var2) {
         return false;
      }
   }

   @Nullable
   public static Float getFloat(@NonNull String str) {
      try {
         return Float.parseFloat(str);
      } catch (NumberFormatException var2) {
         return null;
      }
   }

   public static float getFloatOrDefault(@Nullable String str, float defaultFloat) {
      if (str == null) {
         return defaultFloat;
      } else {
         Float value = getFloat(str);
         return value == null ? defaultFloat : value;
      }
   }

   public static <T> T getOrDefault(T[] array, int index, T def) {
      try {
         return array[index];
      } catch (ArrayIndexOutOfBoundsException var4) {
         return def;
      }
   }

   public static <T> T getOrDefault(@NonNull List<T> list, int index, T def) {
      try {
         return list.get(index);
      } catch (IndexOutOfBoundsException var4) {
         return def;
      }
   }

   @NonNull
   public static <E extends Enum<E>> E getEnumValue(@NonNull Class<E> enumClass, @Nullable String value, @NonNull E def) {
      E enumValue = getEnumValue(enumClass, value);
      return enumValue == null ? def : enumValue;
   }

   @Nullable
   public static <E extends Enum<E>> E getEnumValue(@NonNull Class<E> enumClass, @Nullable String value) {
      if (value != null && enumClass.isEnum()) {
         try {
            return Enum.valueOf(enumClass, value.toUpperCase());
         } catch (IllegalArgumentException var3) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static boolean randomChance(double chance) {
      if (chance < 0.0) {
         return false;
      } else {
         return chance >= 100.0 ? true : chance > ThreadLocalRandom.current().nextDouble(100.0);
      }
   }

   public static char getCharFromString(@NonNull String string, char defaultChar) {
      try {
         return string.toCharArray()[0];
      } catch (ArrayIndexOutOfBoundsException var3) {
         return defaultChar;
      }
   }

   @Nullable
   public static Material getMaterial(@Nullable String itemName) {
      if (itemName == null) {
         return null;
      } else {
         try {
            return Material.valueOf(itemName.toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var2) {
            return null;
         }
      }
   }

   @NonNull
   public static Material getMaterial(@Nullable String itemName, @NonNull Material defaultMaterial) {
      Material material = getMaterial(itemName);
      return material == null ? defaultMaterial : material;
   }

   @Nullable
   public static ItemType getItemType(@Nullable String itemName) {
      Material material = getMaterial(itemName);
      return material == null ? null : material.asItemType();
   }

   @NonNull
   public static ItemType getItemType(@Nullable String itemName, @NonNull ItemType defaultType) {
      ItemType type = getItemType(itemName);
      return type == null ? defaultType : type;
   }

   @Nullable
   public static Enchantment getEnchantment(@Nullable String enchantmentName) {
      return getFromRegistry(enchantmentName, RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT));
   }

   @Nullable
   public static <T extends Keyed> T getFromRegistry(@Nullable String name, @NonNull Registry<T> registry) {
      if (name != null && !name.isEmpty()) {
         NamespacedKey key = NamespacedKey.fromString(name.toLowerCase());
         return (T)(key == null ? null : registry.get(key));
      } else {
         return null;
      }
   }

   public static <E extends Entity> void modifyEntity(@NonNull Entity entity, @NonNull Class<E> classCheck, @NonNull Consumer<? super E> consumer) {
      if (classCheck.isInstance(entity)) {
         E checked = (E)classCheck.cast(entity);
         consumer.accept(checked);
      }
   }

   @Nullable
   public static BlockType getBlockType(@Nullable String blockName) {
      return getFromRegistry(blockName, RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK));
   }

   @NonNull
   public static BlockType getBlockType(@Nullable String blockName, @NonNull BlockType defaultType) {
      BlockType type = getBlockType(blockName);
      return type == null ? defaultType : type;
   }

   public static boolean isSpawnable(@NonNull Location location) {
      return location.getBlock().isPassable() && getAbove(location).isPassable() && getBelow(location).getType().isSolid();
   }

   public static Block getBelow(@NonNull Block block) {
      return block.getRelative(0, -1, 0);
   }

   public static Block getBelow(@NonNull Location location) {
      return getBelow(location.getBlock());
   }

   public static Block getAbove(@NonNull Block block) {
      return block.getRelative(0, 1, 0);
   }

   public static Block getAbove(@NonNull Location location) {
      return getAbove(location.getBlock());
   }

   public static void addLocationToConfig(@NonNull YamlConfiguration config, @NonNull String path, @NonNull Location location) {
      config.set(path, location);
   }

   @Nullable
   public static Location getLocationFromConfig(@NonNull YamlConfiguration config, @NonNull String path) {
      return config.getLocation(path);
   }

   public static boolean removeFromInventory(@NonNull Inventory inventory, @NonNull Predicate<ItemStack> predicate) {
      List<Integer> removalSlots = new ArrayList<>();

      for (int slot = 0; slot < inventory.getSize(); slot++) {
         ItemStack item = inventory.getItem(slot);
         if (item != null && !item.isEmpty() && predicate.test(item)) {
            removalSlots.add(slot);
         }
      }

      if (removalSlots.isEmpty()) {
         return false;
      } else {
         removalSlots.forEach(slotx -> inventory.setItem(slotx, null));
         return true;
      }
   }

   public static void giveItems(@NonNull List<ItemStack> items, @NonNull Player player) {
      if (!items.isEmpty()) {
         List<ItemStack> filteredItems = items.stream().filter(Objects::nonNull).toList();
         if (!filteredItems.isEmpty()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5F, 1.5F);
            Map<Integer, ItemStack> leftoverItems = player.getInventory().addItem(filteredItems.toArray(ItemStack[]::new));
            leftoverItems.values().forEach(item -> player.getWorld().dropItem(player.getLocation(), item));
         }
      }
   }

   public static void giveItems(@NonNull ItemStack[] items, @NonNull Player player) {
      giveItems(Arrays.asList(items), player);
   }

   public static void giveItem(@NonNull ItemStack item, @NonNull Player player) {
      giveItems(List.of(item), player);
   }
}
