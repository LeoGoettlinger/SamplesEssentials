package com.magmaguy.elitemobs.items.customitems;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.weapons.AdvancedMagicWeaponItems;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.config.AdvancedCombatSystemConfig;
import com.magmaguy.elitemobs.config.ClassLootSettingsConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfig;
import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import com.magmaguy.elitemobs.items.ClassLootCoverage;
import com.magmaguy.elitemobs.items.ScalableItemConstructor;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.items.itemconstructor.ClassLootItemConstructor;
import com.magmaguy.elitemobs.items.itemconstructor.EnchantmentGenerator;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructionContext;
import com.magmaguy.elitemobs.items.itemconstructor.ItemConstructor;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentDefinition;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class CustomItem {
   private static final HashMap<String, CustomItem> customItems = new HashMap<>();
   private static final ArrayList<ItemStack> customItemStackList = new ArrayList<>();
   private static final ArrayList<ItemStack> customItemStackShopList = new ArrayList<>();
   private static final HashMap<ItemStack, Double> weighedFixedItems = new HashMap<>();
   private static final HashMap<Integer, ArrayList<ItemStack>> tieredLoot = new HashMap<>();
   private static final HashMap<Integer, ArrayList<CustomItem>> fixedItems = new HashMap<>();
   private static final List<CustomItem> scalableItems = new ArrayList<>();
   private static final HashMap<Integer, ArrayList<CustomItem>> limitedItems = new HashMap<>();
   private static final CacheRegenerationLifecycle cacheRegenerationLifecycle = new CacheRegenerationLifecycle();
   private static final Object cacheTaskLock = new Object();
   private static CustomItem.TrackedCacheTask cacheRegenerationTask;
   private static CustomItem.TrackedCacheTask cacheSwapTask;
   private final CustomItemsConfigFields customItemsConfigFields;
   private final HashMap<Enchantment, Integer> enchantments = new HashMap<>();
   private final HashMap<String, Integer> customEnchantments = new HashMap<>();
   private final String permission;
   private List<String> potionEffects = new ArrayList<>();
   private double dropWeight = 0.0;
   private CustomItem.Scalability scalability;
   private CustomItem.ItemType itemType;
   private int itemLevel = -1;

   public CustomItem(CustomItemsConfigFields customItemsConfigFields) {
      this(customItemsConfigFields, null);
   }

   public CustomItem(CustomItemsConfigFields customItemsConfigFields, ItemConstructionContext construction) {
      this.customItemsConfigFields = customItemsConfigFields;
      this.itemLevel = customItemsConfigFields.getLevel();
      if (this.itemLevel == 0 && customItemsConfigFields.getItemType() != CustomItem.ItemType.CLASS_LOOT) {
         this.itemLevel = (int)EliteItemManager.getItemLevel(new ItemStack(customItemsConfigFields.getMaterial()));
      }

      this.permission = customItemsConfigFields.getPermission();
      if (construction == null
         ? !isUnavailableWithoutModels(customItemsConfigFields)
         : construction.modelsEnabled() || !requiresModels(customItemsConfigFields)) {
         if (customItemsConfigFields.isEnabled()) {
            if (customItemsConfigFields.getMaterial() != null) {
               if (this.parseEnchantments()) {
                  this.parsePotionEffects();
                  this.parseItemType();
                  if (this.itemType != CustomItem.ItemType.CLASS_LOOT) {
                     int sampleLevel = this.itemLevel;
                     ItemStack sample = this.generateDefaultsItemStack(null, false, null, false, construction);
                     if (sample != null) {
                        this.itemLevel = (int)Math.round(EliteItemManager.getItemLevel(sample));
                        if (this.parseDropWeight()) {
                           this.scalability = CustomItem.Scalability.FIXED;
                           if (this.itemType != CustomItem.ItemType.UNIQUE) {
                              addFixedItem(this);
                           }
                        } else {
                           this.parseScalability();
                        }

                        addCustomItem(customItemsConfigFields.getFilename(), this);
                        ItemStack reusable = sampleLevel == this.itemLevel && !customItemsConfigFields.isProceduralEnchantments() ? sample : null;
                        appendCachedItemStacks(this, customItemStackList, customItemStackShopList, tieredLoot, weighedFixedItems, construction, reusable);
                     }
                  } else if (customItemsConfigFields.getClassLootFamily() != null) {
                     this.scalability = CustomItem.Scalability.SCALABLE;
                     this.itemLevel = Math.max(1, customItemsConfigFields.getLevel());
                     addCustomItem(customItemsConfigFields.getFilename(), this);
                     addCustomItem(this, construction);
                  }
               }
            }
         }
      }
   }

   public static CustomItem getCustomItem(String fileName) {
      if (!fileName.contains(".yml")) {
         fileName = fileName + ".yml";
      }

      return !customItems.containsKey(fileName) ? null : customItems.get(fileName);
   }

   public static boolean isUnavailableWithoutModels(String filename) {
      if (filename == null) {
         return false;
      } else {
         String key = filename.endsWith(".yml") ? filename : filename + ".yml";
         return isUnavailableWithoutModels(CustomItemsConfig.getCustomItems().get(key));
      }
   }

   private static boolean isUnavailableWithoutModels(CustomItemsConfigFields config) {
      return config != null && !Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels") ? requiresModels(config) : false;
   }

   private static boolean requiresModels(CustomItemsConfigFields config) {
      return config.getWeaponType() != SkillType.STAVES && config.getWeaponType() != SkillType.WANDS
         ? config.getEnchantments().stream().anyMatch(entry -> entry.startsWith("freeminecraftmodels:"))
         : true;
   }

   private static void addCustomItem(String fileName, CustomItem customItem) {
      customItems.put(fileName, customItem);
   }

   private static void addCustomItem(CustomItem customItem, ItemConstructionContext construction) {
      ItemStack sample = customItem.generateDefaultsItemStack(null, false, null, false, construction);
      if (sample != null) {
         customItemStackList.add(sample);
         if (!isShopExcluded(customItem.getItemType())) {
            customItemStackShopList.add(customItem.generateDefaultsItemStack(null, true, null, false, construction));
         }
      }
   }

   public static void addTieredLoot(CustomItem customItem) {
      addTieredLoot(customItem, null);
   }

   private static void addTieredLoot(CustomItem customItem, ItemConstructionContext construction) {
      ItemStack itemStack = customItem.generateDefaultsItemStack(null, false, null, false, construction);
      int itemTier = customItem.getItemLevel();
      if (tieredLoot.get(itemTier) == null) {
         tieredLoot.put(itemTier, new ArrayList<>(Collections.singletonList(itemStack)));
      } else {
         ArrayList<ItemStack> list = tieredLoot.get(itemTier);
         list.add(itemStack);
         tieredLoot.put(itemTier, list);
      }
   }

   private static void addFixedItem(CustomItem customItem) {
      if (fixedItems.get(customItem.getItemLevel()) != null && !fixedItems.get(customItem.getItemLevel()).isEmpty()) {
         fixedItems.get(customItem.getItemLevel()).add(customItem);
      } else {
         fixedItems.put(customItem.getItemLevel(), new ArrayList<>(Collections.singletonList(customItem)));
      }
   }

   private static void addLimitedItem(CustomItem customItem) {
      if (limitedItems.get(customItem.getItemLevel()) != null && !limitedItems.get(customItem.getItemLevel()).isEmpty()) {
         limitedItems.get(customItem.getItemLevel()).add(customItem);
      } else {
         limitedItems.put(customItem.getItemLevel(), new ArrayList<>(Collections.singletonList(customItem)));
      }
   }

   public static void initializeCustomItems(BooleanSupplier cancelled) {
      List<CustomItemsConfigFields> configs = List.copyOf(CustomItemsConfig.getCustomItems().values());
      ItemConstructionContext construction = prepareConstruction(configs, cancelled);

      for (CustomItemsConfigFields config : configs) {
         if (cancelled.getAsBoolean()) {
            throw new CancellationException("Item construction cancelled");
         }

         try {
            new CustomItem(config, construction);
         } catch (Exception var6) {
            Logger.warn("Failed to generate custom item " + config.getFilename() + ": " + var6.getMessage());
            var6.printStackTrace();
         }
      }

      if (AdvancedCombatSystemConfig.isEnabled()) {
         AdvancedMagicWeaponItems.register(construction);
      }

      onServerThread(construction::requireCurrent, cancelled);
   }

   private static ItemConstructionContext prepareConstruction(Collection<CustomItemsConfigFields> configs, BooleanSupplier cancelled) {
      AtomicReference<ItemConstructionContext> result = new AtomicReference<>();
      onServerThread(() -> result.set(ItemConstructionContext.capture(configs)), cancelled);
      return result.get();
   }

   private static void onServerThread(Runnable construction, BooleanSupplier cancelled) {
      Runnable guarded = () -> {
         if (!cancelled.getAsBoolean() && !MetadataHandler.shutdownRequested) {
            construction.run();
         } else {
            throw new CancellationException("Item construction was cancelled");
         }
      };
      if (Bukkit.isPrimaryThread()) {
         guarded.run();
      } else {
         Future<?> pending = Bukkit.getScheduler().callSyncMethod(MetadataHandler.PLUGIN, () -> {
            guarded.run();
            return null;
         });

         try {
            while (!cancelled.getAsBoolean() && !MetadataHandler.shutdownRequested) {
               try {
                  pending.get(1L, TimeUnit.SECONDS);
                  return;
               } catch (TimeoutException var12) {
               }
            }

            throw new CancellationException("Item construction was cancelled");
         } catch (InterruptedException var13) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Item construction worker was interrupted");
         } catch (ExecutionException var14) {
            if (var14.getCause() instanceof RuntimeException runtime) {
               throw runtime;
            } else if (var14.getCause() instanceof Error error) {
               throw error;
            } else {
               throw new IllegalStateException("Item construction failed", var14.getCause());
            }
         } finally {
            if (!pending.isDone()) {
               pending.cancel(false);
            }
         }
      }
   }

   public static void regenerateCachedItemStacks() {
      CacheRegenerationLifecycle.Attempt<CustomItem> attempt = cacheRegenerationLifecycle.beginIf(
         () -> !MetadataHandler.shutdownRequested && MetadataHandler.PLUGIN != null && MetadataHandler.PLUGIN.isEnabled(), () -> customItems.values()
      );
      if (attempt != null) {
         cancelTrackedCacheTasks();

         BukkitTask scheduledTask;
         try {
            scheduledTask = Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, () -> rebuildCachedItemStacks(attempt));
         } catch (RuntimeException var3) {
            if (cacheRegenerationLifecycle.isCurrent(attempt.generation()) && !MetadataHandler.shutdownRequested) {
               Logger.warn("Failed to schedule the cached item stack refresh.");
               var3.printStackTrace();
            }

            return;
         }

         if (!trackCacheTask(attempt.generation(), scheduledTask, true)) {
            scheduledTask.cancel();
         }
      }
   }

   private static void rebuildCachedItemStacks(CacheRegenerationLifecycle.Attempt<CustomItem> attempt) {
      try {
         try {
            ItemConstructionContext construction = prepareConstruction(
               attempt.snapshot().stream().map(CustomItem::getCustomItemsConfigFields).toList(),
               () -> !cacheRegenerationLifecycle.isCurrent(attempt.generation())
            );
            ArrayList<ItemStack> rebuiltItemStackList = new ArrayList<>();
            ArrayList<ItemStack> rebuiltItemStackShopList = new ArrayList<>();
            HashMap<Integer, ArrayList<ItemStack>> rebuiltTieredLoot = new HashMap<>();
            HashMap<ItemStack, Double> rebuiltWeighedFixedItems = new HashMap<>();

            try {
               if (!buildCachedItemStacks(attempt, rebuiltItemStackList, rebuiltItemStackShopList, rebuiltTieredLoot, rebuiltWeighedFixedItems, construction)) {
                  return;
               }
            } catch (Exception var13) {
               if (cacheRegenerationLifecycle.isCurrent(attempt.generation()) && !MetadataHandler.shutdownRequested) {
                  Logger.warn("Failed to refresh the cached item stacks; items will keep the appearance they were first built with.");
                  var13.printStackTrace();
               }

               return;
            }

            if (!cacheRegenerationLifecycle.isCurrent(attempt.generation())) {
               return;
            }

            BukkitTask scheduledSwap;
            try {
               scheduledSwap = Bukkit.getScheduler()
                  .runTask(
                     MetadataHandler.PLUGIN,
                     () -> applyRebuiltItemStacks(
                        attempt.generation(), rebuiltItemStackList, rebuiltItemStackShopList, rebuiltTieredLoot, rebuiltWeighedFixedItems, construction
                     )
                  );
            } catch (RuntimeException var14) {
               if (cacheRegenerationLifecycle.isCurrent(attempt.generation()) && !MetadataHandler.shutdownRequested) {
                  Logger.warn("Failed to apply the refreshed cached item stacks.");
                  var14.printStackTrace();
               }

               return;
            }

            if (!trackCacheTask(attempt.generation(), scheduledSwap, false)) {
               scheduledSwap.cancel();
               return;
            }
         } catch (Exception var15) {
            if (cacheRegenerationLifecycle.isCurrent(attempt.generation()) && !MetadataHandler.shutdownRequested) {
               Logger.warn("Failed to prepare cached item construction; existing caches were retained.");
               var15.printStackTrace();
               return;
            }
         }
      } finally {
         clearTrackedCacheTask(attempt.generation(), true);
      }
   }

   private static boolean buildCachedItemStacks(
      CacheRegenerationLifecycle.Attempt<CustomItem> attempt,
      ArrayList<ItemStack> itemStackList,
      ArrayList<ItemStack> itemStackShopList,
      HashMap<Integer, ArrayList<ItemStack>> tieredLootTarget,
      HashMap<ItemStack, Double> weighedFixedItemsTarget,
      ItemConstructionContext construction
   ) {
      for (CustomItem item : attempt.snapshot()) {
         if (!cacheRegenerationLifecycle.runIfCurrent(attempt.generation(), () -> {
            try {
               appendCachedItemStacks(item, itemStackList, itemStackShopList, tieredLootTarget, weighedFixedItemsTarget, construction);
            } catch (Exception var7x) {
               throw new IllegalStateException("Failed to rebuild custom item " + item.getCustomItemsConfigFields().getFilename(), var7x);
            }
         })) {
            return false;
         }
      }

      return cacheRegenerationLifecycle.isCurrent(attempt.generation());
   }

   private static void appendCachedItemStacks(
      CustomItem customItem,
      ArrayList<ItemStack> itemStackList,
      ArrayList<ItemStack> itemStackShopList,
      HashMap<Integer, ArrayList<ItemStack>> tieredLootTarget,
      HashMap<ItemStack, Double> weighedFixedItemsTarget,
      ItemConstructionContext construction
   ) {
      appendCachedItemStacks(customItem, itemStackList, itemStackShopList, tieredLootTarget, weighedFixedItemsTarget, construction, null);
   }

   private static void appendCachedItemStacks(
      CustomItem customItem,
      ArrayList<ItemStack> itemStackList,
      ArrayList<ItemStack> itemStackShopList,
      HashMap<Integer, ArrayList<ItemStack>> tieredLootTarget,
      HashMap<ItemStack, Double> weighedFixedItemsTarget,
      ItemConstructionContext construction,
      ItemStack preparedSample
   ) {
      if (customItem.getCustomItemsConfigFields() != null && customItem.getCustomItemsConfigFields().isEnabled()) {
         if (customItem.getCustomItemsConfigFields().getMaterial() != null) {
            boolean deterministic = !customItem.getCustomItemsConfigFields().isProceduralEnchantments();
            ItemStack defaultsItemStack = preparedSample != null
               ? preparedSample
               : customItem.generateDefaultsItemStack(null, false, null, false, construction);
            if (defaultsItemStack != null) {
               itemStackList.add(defaultsItemStack);
               if (customItem.getItemType() != CustomItem.ItemType.CLASS_LOOT) {
                  if (!isShopExcluded(customItem.getItemType())) {
                     itemStackShopList.add(customItem.generateDefaultsItemStack(null, true, null, false, construction));
                  }

                  ItemStack itemStack = deterministic
                     ? defaultsItemStack.clone()
                     : customItem.generateDefaultsItemStack(null, false, null, false, construction);
                  int itemTier = customItem.getItemLevel();
                  if (tieredLootTarget.get(itemTier) == null) {
                     tieredLootTarget.put(itemTier, new ArrayList<>(Collections.singletonList(itemStack)));
                  } else {
                     tieredLootTarget.get(itemTier).add(itemStack);
                  }

                  if (customItem.getItemType() != CustomItem.ItemType.UNIQUE
                     && customItem.getScalability() == CustomItem.Scalability.FIXED
                     && customItem.getDropWeight() > 0.0) {
                     ItemStack weighedStack = deterministic
                        ? defaultsItemStack.clone()
                        : customItem.generateDefaultsItemStack(null, false, null, false, construction);
                     weighedFixedItemsTarget.put(weighedStack, customItem.getDropWeight());
                  }
               }
            }
         }
      }
   }

   private static void applyRebuiltItemStacks(
      long generation,
      ArrayList<ItemStack> rebuiltItemStackList,
      ArrayList<ItemStack> rebuiltItemStackShopList,
      HashMap<Integer, ArrayList<ItemStack>> rebuiltTieredLoot,
      HashMap<ItemStack, Double> rebuiltWeighedFixedItems,
      ItemConstructionContext construction
   ) {
      try {
         cacheRegenerationLifecycle.runIfCurrent(generation, () -> {
            construction.requireCurrent();
            customItemStackList.clear();
            customItemStackList.addAll(rebuiltItemStackList);
            customItemStackShopList.clear();
            customItemStackShopList.addAll(rebuiltItemStackShopList);
            tieredLoot.clear();
            tieredLoot.putAll(rebuiltTieredLoot);
            weighedFixedItems.clear();
            weighedFixedItems.putAll(rebuiltWeighedFixedItems);
         });
      } catch (Exception var11) {
         Logger.warn("Failed to publish refreshed item stacks; existing caches were retained: " + var11.getMessage());
      } finally {
         clearTrackedCacheTask(generation, false);
      }
   }

   public static void shutdownCacheRegeneration() {
      cacheRegenerationLifecycle.cancel();
      cancelTrackedCacheTasks();
   }

   private static boolean trackCacheTask(long generation, BukkitTask task, boolean regenerationTask) {
      return cacheRegenerationLifecycle.runIfCurrent(generation, () -> {
         synchronized (cacheTaskLock) {
            CustomItem.TrackedCacheTask trackedCacheTask = new CustomItem.TrackedCacheTask(generation, task);
            if (regenerationTask) {
               cacheRegenerationTask = trackedCacheTask;
            } else {
               cacheSwapTask = trackedCacheTask;
            }
         }
      });
   }

   private static void clearTrackedCacheTask(long generation, boolean regenerationTask) {
      synchronized (cacheTaskLock) {
         CustomItem.TrackedCacheTask trackedCacheTask = regenerationTask ? cacheRegenerationTask : cacheSwapTask;
         if (trackedCacheTask != null && trackedCacheTask.generation() == generation) {
            if (regenerationTask) {
               cacheRegenerationTask = null;
            } else {
               cacheSwapTask = null;
            }
         }
      }
   }

   private static void cancelTrackedCacheTasks() {
      CustomItem.TrackedCacheTask regenerationTask;
      CustomItem.TrackedCacheTask swapTask;
      synchronized (cacheTaskLock) {
         regenerationTask = cacheRegenerationTask;
         swapTask = cacheSwapTask;
         cacheRegenerationTask = null;
         cacheSwapTask = null;
      }

      if (regenerationTask != null) {
         regenerationTask.task().cancel();
      }

      if (swapTask != null) {
         swapTask.task().cancel();
      }
   }

   public static int limitItemLevel(Player player, int originalLevel) {
      return originalLevel;
   }

   public Item dropPlayerLoot(Player player, int tier, Location location, EliteEntity eliteEntity) {
      if (this.permission.isEmpty() || player != null && player.hasPermission(this.permission)) {
         int itemTier = limitItemLevel(player, tier);

         ItemStack itemStack = switch (this.getScalability()) {
            case FIXED -> this.generateItemStack(this.itemLevel, player, eliteEntity);
            case LIMITED -> ScalableItemConstructor.constructLimitedItem(itemTier, this, player, eliteEntity);
            case SCALABLE -> ScalableItemConstructor.constructScalableItem(itemTier, this, player, eliteEntity);
         };
         return this.dropConstructedLoot(itemStack, player, location);
      } else {
         return null;
      }
   }

   public Item dropPlayerLootExact(Player player, int level, Location location, EliteEntity eliteEntity) {
      if (this.permission.isEmpty() || player != null && player.hasPermission(this.permission)) {
         ItemStack itemStack = this.generateItemStackExact(level, player, eliteEntity);
         return this.dropConstructedLoot(itemStack, player, location);
      } else {
         return null;
      }
   }

   private Item dropConstructedLoot(ItemStack itemStack, Player player, Location location) {
      if (itemStack != null && !itemStack.getType().isAir() && itemStack.getAmount() > 0) {
         Item loot = location.getWorld().dropItem(location, itemStack);
         if (player != null) {
            loot.setOwner(player.getUniqueId());
         }

         SoulbindEnchantment.addPhysicalDisplay(loot, player);
         loot.setCustomName(loot.getItemStack().getItemMeta().getDisplayName());
         loot.setCustomNameVisible(true);
         return loot;
      } else {
         return null;
      }
   }

   private boolean parseEnchantments() {
      for (String string : this.customItemsConfigFields.getEnchantments()) {
         try {
            String name = string.split(",")[0];
            if (name.contains(":") && !name.startsWith("minecraft:")) {
               EnchantmentDefinition.requireId(name);
               String[] parts = string.split(",", -1);
               if (parts.length != 2) {
                  throw new IllegalArgumentException("Expected namespaced_id,level");
               }

               int sharedLevel = Integer.parseInt(parts[1]);
               if (sharedLevel < 1) {
                  throw new IllegalArgumentException("Custom levels must be positive");
               }

               if (this.customEnchantments.putIfAbsent(name, sharedLevel) != null) {
                  throw new IllegalArgumentException("Duplicate custom enchantment identity " + name);
               }
            } else {
               if (Set.of(
                     "multicast",
                     "blast_radius",
                     "ignition",
                     "repair",
                     "unbind",
                     "lucky_source",
                     "enchanted_source",
                     "loud_strikes",
                     "critical_strikes",
                     "drilling",
                     "ice_breaker",
                     "summon_wolf",
                     "summon_merchant",
                     "flamethrower",
                     "lightning",
                     "hunter",
                     "earthquake",
                     "plasma_boots",
                     "grappling_hook",
                     "meteor_shower"
                  )
                  .contains(name.toLowerCase(Locale.ROOT))) {
                  this.customItemsConfigFields.setEnabled(false);
                  Logger.info(
                     "Custom item "
                        + this.customItemsConfigFields.getFilename()
                        + " disabled: retired custom enchantment format "
                        + name
                        + ". Author the current namespaced enchantment or consumable role; old formats are not converted."
                  );
                  return false;
               }

               int level = 1;

               try {
                  level = Integer.parseInt(string.split(",")[1]);
               } catch (Exception var7) {
                  Logger.warn("Custom Item Entry " + this.customItemsConfigFields.getFilename() + " has an invalid enchantment entry.");
                  Logger.warn("Enchantment " + name + " is missing a level.");
                  Logger.warn("Reminder - The correct format for these is [enchantmentName],[level]");
                  Logger.warn("The name should follow the API names and the level should be above 0.");
                  Logger.warn("Defaulting " + name + " to level 1.");
               }

               Enchantment enchantment;
               try {
                  enchantment = Enchantment.getByName(name);
                  if (enchantment == null) {
                     throw new Exception("Null enchantment");
                  }
               } catch (Exception var8) {
                  Logger.warn("Custom Item Entry " + this.customItemsConfigFields.getFilename() + " has an invalid enchantment entry.");
                  Logger.warn("Enchantment " + name + " is not a valid enchantment. Check the Spigot API for the correct names!");
                  Logger.warn("The invalid entry will be skipped.");
                  continue;
               }

               this.enchantments.put(enchantment, level);
            }
         } catch (Exception var9) {
            if (string.contains(":") && !string.startsWith("minecraft:")) {
               this.customItemsConfigFields.setEnabled(false);
               Logger.warn(
                  "Custom item " + this.customItemsConfigFields.getFilename() + " disabled: invalid shared enchantment " + string + ": " + var9.getMessage()
               );
               return false;
            }

            Logger.warn("Invalid enchantment entry for item " + this.customItemsConfigFields.getFilename());
            Logger.warn("[" + string + "] is not a valid entry and will be ignored.");
            Logger.warn("Reminder - The correct format for these is [enchantmentName],[level]");
            Logger.warn("The name should follow the API names and the level should be above 0.");
         }
      }

      return true;
   }

   private void parsePotionEffects() {
      this.potionEffects = this.customItemsConfigFields.getPotionEffects();
   }

   private void parseItemType() {
      if (this.customItemsConfigFields.getItemType() == null) {
         this.itemType = CustomItem.ItemType.CUSTOM;
      } else {
         this.itemType = this.customItemsConfigFields.getItemType();
      }
   }

   private boolean parseDropWeight() {
      String configured = this.customItemsConfigFields.getDropWeight();
      if (configured != null && !configured.equalsIgnoreCase("dynamic")) {
         this.dropWeight = Double.parseDouble(configured);
         if (Double.isFinite(this.dropWeight) && !(this.dropWeight < 0.0)) {
            return true;
         } else {
            throw new IllegalArgumentException("dropWeight must be finite and nonnegative for " + this.customItemsConfigFields.getFilename());
         }
      } else {
         return false;
      }
   }

   private void parseScalability() {
      if (this.customItemsConfigFields.getScalability() == null) {
         this.scalability = CustomItem.Scalability.SCALABLE;
      } else {
         this.scalability = this.customItemsConfigFields.getScalability();
         switch (this.customItemsConfigFields.getScalability()) {
            case FIXED:
               if (!this.itemType.equals(CustomItem.ItemType.UNIQUE)) {
                  addFixedItem(this);
               }
               break;
            case LIMITED:
               if (!this.itemType.equals(CustomItem.ItemType.UNIQUE)) {
                  addLimitedItem(this);
               }
               break;
            case SCALABLE:
               if (!this.itemType.equals(CustomItem.ItemType.UNIQUE)) {
                  scalableItems.add(this);
               }
               break;
            default:
               this.scalability = CustomItem.Scalability.SCALABLE;
               if (!this.itemType.equals(CustomItem.ItemType.UNIQUE)) {
                  scalableItems.add(this);
               }

               Logger.warn("Item " + this.customItemsConfigFields.getFilename() + " does not have a valid scalability type! Defaulting to scalable.");
         }
      }
   }

   public ItemStack generateDefaultsItemStack(Player player, boolean showItemWorth, EliteEntity eliteEntity) {
      return this.generateDefaultsItemStack(player, showItemWorth, eliteEntity, false);
   }

   public ItemStack generateDefaultsItemStack(Player player, boolean showItemWorth, EliteEntity eliteEntity, boolean bypass) {
      return this.generateDefaultsItemStack(player, showItemWorth, eliteEntity, bypass, null);
   }

   private ItemStack generateDefaultsItemStack(
      Player player, boolean showItemWorth, EliteEntity eliteEntity, boolean bypass, ItemConstructionContext construction
   ) {
      if (!bypass && player != null && !this.permission.isEmpty() && !player.hasPermission(this.permission)) {
         return null;
      } else {
         return this.itemType == CustomItem.ItemType.CLASS_LOOT
            ? this.generateClassLootItem(this.itemLevel, player, eliteEntity, construction)
            : ItemConstructor.constructItem(
               this.itemLevel,
               this.customItemsConfigFields.getName(),
               this.customItemsConfigFields.getMaterial(),
               EnchantmentGenerator.withProceduralEnchantments(this.itemLevel, this.customItemsConfigFields, this.getEnchantments()),
               EnchantmentGenerator.withProceduralCustomEnchantments(this.itemLevel, this.customItemsConfigFields, this.getCustomEnchantments()),
               this.getPotionEffects(),
               this.customItemsConfigFields.getLore(),
               eliteEntity,
               player,
               showItemWorth,
               this.customItemsConfigFields.getCustomModelID(),
               this.customItemsConfigFields.getEquipmentModelID(),
               this.customItemsConfigFields.isSoulbound(),
               this.getCustomItemsConfigFields().getFilename(),
               this.customItemsConfigFields.getScriptedItem(),
               this.customItemsConfigFields.getWeaponType(),
               this.customItemsConfigFields.getFmmItemModel(),
               construction
            );
      }
   }

   public ItemStack generateItemStack(int itemTier, Player player, EliteEntity eliteEntity) {
      if (this.itemType == CustomItem.ItemType.CLASS_LOOT) {
         return this.generateClassLootItem(itemTier, player, eliteEntity);
      } else {
         ItemStack itemStack = null;
         if (player != null) {
            itemTier = limitItemLevel(player, itemTier);
         }

         switch (this.scalability) {
            case FIXED:
               itemStack = this.generateDefaultsItemStack(player, false, eliteEntity);
               break;
            case LIMITED:
               itemStack = ScalableItemConstructor.constructLimitedItem(itemTier, this, player, eliteEntity);
               break;
            case SCALABLE:
               itemStack = ScalableItemConstructor.constructScalableItem(itemTier, this, player, eliteEntity);
         }

         return itemStack;
      }
   }

   public ItemStack generateItemStackExact(int itemTier, Player player, EliteEntity eliteEntity) {
      if (this.itemType == CustomItem.ItemType.CLASS_LOOT) {
         return this.generateClassLootItem(itemTier, player, eliteEntity);
      } else {
         ItemStack itemStack = null;
         switch (this.scalability) {
            case FIXED:
               itemStack = this.generateDefaultsItemStack(player, false, eliteEntity);
               break;
            case LIMITED:
               itemStack = ScalableItemConstructor.constructLimitedItem(itemTier, this, player, eliteEntity);
               break;
            case SCALABLE:
               itemStack = ScalableItemConstructor.constructScalableItem(itemTier, this, player, eliteEntity);
         }

         return itemStack;
      }
   }

   private ItemStack generateClassLootItem(int level, Player player, EliteEntity entity) {
      return this.generateClassLootItem(level, player, entity, null);
   }

   private ItemStack generateClassLootItem(int level, Player player, EliteEntity entity, ItemConstructionContext construction) {
      CustomBossEntity boss = entity instanceof CustomBossEntity custom ? custom : null;
      ClassLootSettingsConfig.Difficulty difficulty = boss == null ? ClassLootSettingsConfig.defaultDifficulty() : ClassLootCoverage.difficulty(boss);
      ClassLootSettingsConfig.Rank rank = boss == null ? ClassLootSettingsConfig.Rank.TRASH : ClassLootCoverage.rank(boss);
      return ClassLootItemConstructor.construct(this, level, difficulty, rank, entity, player, entity == null ? "Elite" : entity.getName(), construction);
   }

   private static boolean isShopExcluded(CustomItem.ItemType type) {
      return type == CustomItem.ItemType.UNIQUE || type == CustomItem.ItemType.DROPPABLE || type == CustomItem.ItemType.CLASS_LOOT;
   }

   @Generated
   public static HashMap<String, CustomItem> getCustomItems() {
      return customItems;
   }

   @Generated
   public static ArrayList<ItemStack> getCustomItemStackList() {
      return customItemStackList;
   }

   @Generated
   public static ArrayList<ItemStack> getCustomItemStackShopList() {
      return customItemStackShopList;
   }

   @Generated
   public static HashMap<ItemStack, Double> getWeighedFixedItems() {
      return weighedFixedItems;
   }

   @Generated
   public static HashMap<Integer, ArrayList<ItemStack>> getTieredLoot() {
      return tieredLoot;
   }

   @Generated
   public static HashMap<Integer, ArrayList<CustomItem>> getFixedItems() {
      return fixedItems;
   }

   @Generated
   public static List<CustomItem> getScalableItems() {
      return scalableItems;
   }

   @Generated
   public static HashMap<Integer, ArrayList<CustomItem>> getLimitedItems() {
      return limitedItems;
   }

   @Generated
   public CustomItemsConfigFields getCustomItemsConfigFields() {
      return this.customItemsConfigFields;
   }

   @Generated
   public HashMap<Enchantment, Integer> getEnchantments() {
      return this.enchantments;
   }

   @Generated
   public HashMap<String, Integer> getCustomEnchantments() {
      return this.customEnchantments;
   }

   @Generated
   public String getPermission() {
      return this.permission;
   }

   @Generated
   public List<String> getPotionEffects() {
      return this.potionEffects;
   }

   @Generated
   public double getDropWeight() {
      return this.dropWeight;
   }

   @Generated
   public CustomItem.Scalability getScalability() {
      return this.scalability;
   }

   @Generated
   public CustomItem.ItemType getItemType() {
      return this.itemType;
   }

   @Generated
   public int getItemLevel() {
      return this.itemLevel;
   }

   public static enum ItemType {
      CUSTOM,
      UNIQUE,
      DROPPABLE,
      CLASS_LOOT;
   }

   public static enum Scalability {
      FIXED,
      LIMITED,
      SCALABLE;
   }

   private record TrackedCacheTask(long generation, BukkitTask task) {
   }
}
