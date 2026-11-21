package com.oheers.fish;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.config.serializer.BossBarOverlaySerializer;
import com.oheers.fish.api.config.serializer.ItemSerializer;
import com.oheers.fish.api.config.serializer.PotionEffectSerializer;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.economy.selling.SellHelper;
import com.oheers.fish.api.economy.selling.SoldFish;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.utils.DurationFormatter;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.sound.Sound.Type;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.daisylib.utils.MessageUtils;
import org.evenmorefish.fish.libs.inventorygui.GuiStorageElement;
import org.evenmorefish.fish.libs.inventorygui.InventoryGui;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public class FishUtils {
   private static final DurationFormatter durationFormatter = new DurationFormatter(TimeUnit.SECONDS);
   public static final UUID B64_SKULL_UUID = UUID.fromString("07cd5534-e542-4fbf-861c-67a144ecf776");

   private FishUtils() {
      throw new UnsupportedOperationException();
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

   @Nullable
   public static String getRegionName(@NonNull Location location) {
      if (!MainConfig.getInstance().isRegionBoostsEnabled()) {
         EvenMoreFish.getInstance().debug("Region boosts are disabled.");
         return null;
      } else {
         EvenMoreFish plugin = EvenMoreFish.getInstance();
         PluginManager pluginManager = plugin.getServer().getPluginManager();
         Plugin worldGuard = pluginManager.getPlugin("WorldGuard");
         if (worldGuard != null && worldGuard.isEnabled()) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            ApplicableRegionSet set = container.createQuery().getApplicableRegions(BukkitAdapter.adapt(location));
            if (set.getRegions().isEmpty()) {
               EvenMoreFish.getInstance().debug("Could not find any regions with WorldGuard");
               return null;
            } else {
               return ((ProtectedRegion)set.iterator().next()).getId();
            }
         } else if (pluginManager.isPluginEnabled("RedProtect")) {
            Region region = RedProtect.get().getAPI().getRegion(location);
            if (region == null) {
               EvenMoreFish.getInstance().debug("Could not find any regions with RedProtect");
               return null;
            } else {
               return region.getName();
            }
         } else {
            return null;
         }
      }
   }

   @NonNull
   public static EMFMessage timeFormat(long timeLeft) {
      return EMFSingleMessage.of(durationFormatter.format(timeLeft));
   }

   @NonNull
   public static String timeRaw(long timeLeft) {
      String returning = "";
      long hours = timeLeft / 3600L;
      if (timeLeft >= 3600L) {
         returning = returning + hours + ":";
      }

      if (timeLeft >= 60L) {
         returning = returning + timeLeft % 3600L / 60L + ":";
      }

      return returning + timeLeft % 60L;
   }

   public static char getCharFromString(@NonNull String string, char defaultChar) {
      try {
         return string.toCharArray()[0];
      } catch (ArrayIndexOutOfBoundsException var3) {
         return defaultChar;
      }
   }

   @Nullable
   public static Biome getBiome(@NonNull String keyString) {
      Biome biome = getFromPaperRegistry(keyString, RegistryKey.BIOME);
      if (biome == null) {
         EvenMoreFish.getInstance().getLogger().severe(keyString + " is not a valid biome.");
      }

      return biome;
   }

   @Nullable
   public static DayOfWeek getDay(@NonNull String day) {
      return getEnumValue(DayOfWeek.class, day);
   }

   @Nullable
   public static Integer getInteger(@NonNull String intString) {
      try {
         return Integer.parseInt(intString);
      } catch (NumberFormatException var2) {
         return null;
      }
   }

   @NonNull
   public static String getPlayerNameOrDefault(@Nullable OfflinePlayer player, @NonNull String def) {
      return Optional.ofNullable(player).<String>map(OfflinePlayer::getName).orElse(def);
   }

   @Nullable
   public static String getPlayerName(@Nullable OfflinePlayer player) {
      return Optional.ofNullable(player).<String>map(OfflinePlayer::getName).orElse(null);
   }

   @Nullable
   public static String getPlayerName(@Nullable UUID uuid) {
      return uuid == null ? null : getPlayerName(Bukkit.getOfflinePlayer(uuid));
   }

   @Nullable
   public static String getPlayerName(@Nullable String uuidString) {
      if (uuidString == null) {
         return null;
      } else {
         try {
            UUID uuid = UUID.fromString(uuidString);
            return getPlayerName(uuid);
         } catch (IllegalArgumentException var2) {
            return null;
         }
      }
   }

   @NonNull
   public static ItemStack getSkullFromBase64(@NonNull String base64) {
      return EvenMoreFish.getInstance().getVersionProvider().getSkullFromBase64(base64);
   }

   @NonNull
   public static ItemStack getSkullFromUUID(@NonNull UUID uuid) {
      return EvenMoreFish.getInstance().getVersionProvider().getSkullFromUUID(uuid);
   }

   @NonNull
   public static ItemStack getSkullFromUUIDString(@NonNull String uuidString) {
      try {
         return getSkullFromUUID(UUID.fromString(uuidString));
      } catch (IllegalArgumentException var2) {
         return new ItemStack(Material.PLAYER_HEAD);
      }
   }

   public static double roundDouble(final double value, final int places) {
      return places < 0 ? value : BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
   }

   public static float roundFloat(final float value, int places) {
      return places < 0 ? value : BigDecimal.valueOf((double)value).setScale(places, RoundingMode.HALF_UP).floatValue();
   }

   @NonNull
   public static String getFormat(@NonNull String colour) {
      return MessageUtils.containsLegacy(colour) ? colour + "{name}" : getMiniMessageFormat(colour);
   }

   @NonNull
   private static String getMiniMessageFormat(@NonNull String colour) {
      int openingTagEnd = colour.indexOf(">");
      if (openingTagEnd == -1) {
         return colour + "{name}";
      } else {
         return colour.contains("</")
            ? colour.substring(0, openingTagEnd + 1) + "{name}" + colour.substring(openingTagEnd + 1)
            : colour.substring(0, openingTagEnd + 1) + "{name}";
      }
   }

   @Nullable
   public static Enchantment getEnchantment(@NonNull String namespace) {
      return getFromPaperRegistry(namespace, RegistryKey.ENCHANTMENT);
   }

   @NonNull
   public static <E extends Enum<E>> E getEnumValue(@NonNull Class<E> enumClass, @Nullable String value, @NonNull E def) {
      E enumValue = getEnumValue(enumClass, value);
      return enumValue == null ? def : enumValue;
   }

   @Nullable
   public static <E extends Enum<E>> E getEnumValue(@NonNull Class<E> enumClass, @Nullable String value) {
      if (!enumClass.isEnum()) {
         throw new IllegalArgumentException(enumClass.getName() + " cannot be used in FishUtils#getEnumValue.");
      } else if (value == null) {
         return null;
      } else {
         try {
            return Enum.valueOf(enumClass, value.toUpperCase());
         } catch (IllegalArgumentException var3) {
            return null;
         }
      }
   }

   @Nullable
   private static <T extends Keyed> T getFromPaperRegistry(@NonNull String namespace, @NonNull RegistryKey<T> registryKey) {
      NamespacedKey key = NamespacedKey.fromString(namespace.toLowerCase(Locale.ROOT));
      return (T)(key == null ? null : RegistryAccess.registryAccess().getRegistry(registryKey).get(key));
   }

   public static boolean inventoryHasSpace(@Nullable Inventory inventory) {
      return inventory != null && inventory.firstEmpty() != -1;
   }

   public static boolean classExists(@NonNull String className) {
      try {
         Class.forName(className);
         return true;
      } catch (ClassNotFoundException var2) {
         return false;
      }
   }

   public static Double getDoubleOrNull(@Nullable Object value) {
      if (value == null) {
         return null;
      } else {
         try {
            return Double.parseDouble(value.toString());
         } catch (NumberFormatException var2) {
            return null;
         }
      }
   }

   @Nullable
   public static Double fetchSize(@NonNull Section section, @NonNull String key, @Nullable OfflinePlayer player) {
      Object value = section.get(key);
      if (value != null && !(value instanceof Section)) {
         Double parsed = getDoubleOrNull(value);
         if (parsed != null) {
            return parsed;
         } else if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return null;
         } else {
            String papi = PlaceholderAPI.setPlaceholders(player, value.toString());
            Double papiParsed = getDoubleOrNull(papi);
            if (papiParsed == null) {
               Logging.warn("Invalid size placeholder " + value + " in config " + section.getRouteAsString());
               return null;
            } else {
               return papiParsed;
            }
         }
      } else {
         return null;
      }
   }

   public static void sellInventoryGui(@NonNull InventoryGui gui, @NonNull HumanEntity humanEntity) {
      if (humanEntity instanceof Player player) {
         if (!Economy.getInstance().isEnabled()) {
            ConfigMessage.ECONOMY_DISABLED.getMessage().send(player);
         } else {
            gui.getElements().forEach(element -> {
               if (element instanceof GuiStorageElement storageElement) {
                  SellHelper.get().sell(storageElement.getStorage(), player);
               }
            });
         }
      }
   }

   public static double calculateInventoryWorth(@NonNull Inventory inventory) {
      double worth = 0.0;

      for (ItemStack itemStack : inventory) {
         SoldFish fish = SoldFish.get(null, itemStack);
         if (fish != null) {
            worth += fish.getFinalValue();
         }
      }

      return worth;
   }

   @Nullable
   public static Double parseDoubleOrRange(@Nullable String string) {
      if (string == null) {
         return null;
      } else {
         Double parsed = getDoubleOrNull(string);
         if (parsed != null) {
            return parsed;
         } else {
            String[] split = string.split("-", 2);
            if (split.length != 2) {
               Logging.debug(string + " is not a valid set-worth range.");
               return null;
            } else {
               Double min = getDoubleOrNull(split[0]);
               Double max = getDoubleOrNull(split[1]);
               if (min != null && max != null) {
                  return EvenMoreFish.RANDOM.nextDouble(min, max);
               } else {
                  Logging.debug(split[0] + " or " + split[1] + " are not valid doubles.");
                  return null;
               }
            }
         }
      }
   }

   @Nullable
   public static Method getMethodOrNull(@NonNull Class<?> clazz, @NonNull String method, @NonNull Class<?>... parameterTypes) {
      try {
         return clazz.getDeclaredMethod(method, parameterTypes);
      } catch (NoSuchMethodException var4) {
         return null;
      }
   }

   @Deprecated
   @Nullable
   public static PotionEffect getPotionEffect(@NonNull String effectString, @NonNull String separator) {
      return PotionEffectSerializer.get().deserialize(effectString, separator);
   }

   @Deprecated
   @Nullable
   public static PotionEffect getPotionEffect(@NonNull String effectString) {
      return PotionEffectSerializer.get().deserialize(effectString);
   }

   @Deprecated
   @Nullable
   public static Type getSound(@Nullable String name) {
      try {
         return Sound.valueOf(name);
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   @Deprecated
   public static Overlay fetchBarStyle(@Nullable String styleStr) {
      return BossBarOverlaySerializer.get().deserialize(styleStr);
   }

   @Deprecated
   public static boolean isFish(@Nullable ItemStack item) {
      return FishManager.getInstance().isFish(item);
   }

   @Deprecated
   public static boolean isFish(@Nullable Skull skull) {
      return FishManager.getInstance().isFish(skull);
   }

   @Deprecated
   @Nullable
   public static Fish getFish(@Nullable ItemStack item) {
      return FishManager.getInstance().getFish(item) instanceof Fish fish ? fish : null;
   }

   @Deprecated
   @Nullable
   public static Fish getFish(@Nullable Skull skull, @Nullable Player fisher) {
      return FishManager.getInstance().getFish(skull, fisher) instanceof Fish fish ? fish : null;
   }

   @Deprecated
   public static boolean isBaitObject(@NonNull ItemStack item) {
      return BaitManager.getInstance().isBait(item);
   }

   @Deprecated
   public static boolean checkWorld(@NonNull Location l) {
      return Checks.canFishInWorld(l);
   }

   @Deprecated
   public static boolean checkRegion(@NonNull Location location, @NonNull List<String> whitelistedRegions) {
      return Checks.canUseRegion(location, whitelistedRegions);
   }

   @Deprecated
   @Nullable
   public static ItemStack getItem(@Nullable final String materialString) {
      return ItemSerializer.get().deserialize(materialString, true);
   }

   @Deprecated
   @Nullable
   public static ItemStack getCustomItem(@NonNull String materialString) {
      return ItemSerializer.get().deserializeItemAddon(materialString);
   }
}
