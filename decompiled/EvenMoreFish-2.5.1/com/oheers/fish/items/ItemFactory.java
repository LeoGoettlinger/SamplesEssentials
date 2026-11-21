package com.oheers.fish.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.config.serializer.ItemSerializer;
import com.oheers.fish.api.items.AbstractItemFactory;
import com.oheers.fish.items.config.CustomModelDataItemConfig;
import com.oheers.fish.items.config.DisplayNameItemConfig;
import com.oheers.fish.items.config.DurabilityItemConfig;
import com.oheers.fish.items.config.DyeColorItemConfig;
import com.oheers.fish.items.config.EnchantmentsItemConfig;
import com.oheers.fish.items.config.FireResistantItemConfig;
import com.oheers.fish.items.config.GlowingItemConfig;
import com.oheers.fish.items.config.HideTooltipItemConfig;
import com.oheers.fish.items.config.ItemConfig;
import com.oheers.fish.items.config.ItemModelItemConfig;
import com.oheers.fish.items.config.ItemRarityItemConfig;
import com.oheers.fish.items.config.LoreItemConfig;
import com.oheers.fish.items.config.MaxStackSizeItemConfig;
import com.oheers.fish.items.config.PotionEffectItemConfig;
import com.oheers.fish.items.config.QuantityItemConfig;
import com.oheers.fish.items.config.TooltipStyleItemConfig;
import com.oheers.fish.items.config.UnbreakableItemConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ItemFactory extends AbstractItemFactory {
   @NonNull
   private final Section configuration;
   private boolean rawItem = false;
   private UUID relevantPlayer = null;
   private int randomIndex = -1;
   private Consumer<ItemStack> finalChanges = null;
   @NonNull
   private ItemStack baseItem;
   private boolean usingItemAddon = false;
   private boolean usingFallbackBaseItem = false;
   private final Map<Class<? extends ItemConfig>, ItemConfig<?>> configMap = new HashMap<>();

   private ItemFactory(@NonNull Section initialSection, @Nullable String configLocation, @Nullable String itemPath) {
      Section section = configLocation == null ? initialSection : initialSection.createSection(configLocation);
      if (itemPath != null) {
         new ItemFactoryConversion().performConversions(section);
      }

      this.configuration = itemPath == null ? section : section.createSection(itemPath);
      this.loadConfigMap();
      this.baseItem = this.getBaseItem();
   }

   @NonNull
   public ItemFactory createCopy() {
      ItemFactory newFactory = new ItemFactory(this.configuration, null, null);
      newFactory.relevantPlayer = this.relevantPlayer;
      newFactory.finalChanges = this.finalChanges;
      newFactory.configMap.clear();
      newFactory.configMap.putAll(this.configMap);
      newFactory.setRandomIndex(this.randomIndex);
      return newFactory;
   }

   public static ItemFactory itemFactory(@NonNull Section configuration) {
      return itemFactory(configuration, null);
   }

   public static ItemFactory itemFactory(@NonNull Section configuration, @Nullable String configLocation) {
      return itemFactory(configuration, configLocation, "item");
   }

   public static ItemFactory itemFactory(@NonNull Section configuration, @Nullable String configLocation, @NonNull String itemPath) {
      return new ItemFactory(configuration, configLocation, itemPath);
   }

   @NonNull
   @Override
   public ItemStack createItem(@Nullable Map<String, ?> replacements) {
      ItemStack item = this.baseItem.clone();
      if (this.rawItem) {
         return item;
      } else {
         OfflinePlayer player = this.relevantPlayer == null ? null : Bukkit.getOfflinePlayer(this.relevantPlayer);

         for (ItemConfig<?> config : this.configMap.values()) {
            ItemConfigApplier.apply(item, player, replacements, config, this.usingItemAddon);
         }

         if (this.finalChanges != null) {
            this.finalChanges.accept(item);
         }

         return item;
      }
   }

   @NonNull
   @Override
   public ItemStack createItem(@NonNull UUID relevantPlayer, @Nullable Map<String, ?> replacements) {
      this.relevantPlayer = relevantPlayer;
      return this.createItem(replacements);
   }

   @NonNull
   public ItemStack getBaseItem() {
      ItemStack rawNbt = this.checkRawNbt();
      if (rawNbt != null) {
         this.rawItem = true;
         return rawNbt;
      } else {
         ItemStack material = this.checkMaterial();
         if (material != null) {
            return material;
         } else {
            ItemStack rawMaterial = this.checkRawMaterial();
            if (rawMaterial != null) {
               this.rawItem = true;
               return rawMaterial;
            } else {
               ItemStack randomMaterial = this.checkRandomMaterial();
               if (randomMaterial != null) {
                  return randomMaterial;
               } else {
                  ItemStack randomRawMaterial = this.checkRandomRawMaterial();
                  if (randomRawMaterial != null) {
                     this.rawItem = true;
                     return randomRawMaterial;
                  } else {
                     ItemStack headDB = this.checkHeadDB();
                     if (headDB != null) {
                        return headDB;
                     } else {
                        ItemStack randomHeadDB = this.checkRandomHeadDB();
                        if (randomHeadDB != null) {
                           return randomHeadDB;
                        } else {
                           ItemStack head64 = this.checkHead64();
                           if (head64 != null) {
                              return head64;
                           } else {
                              ItemStack randomHead64 = this.checkRandomHead64();
                              if (randomHead64 != null) {
                                 return randomHead64;
                              } else {
                                 ItemStack headUUID = this.checkHeadUUID();
                                 if (headUUID != null) {
                                    return headUUID;
                                 } else {
                                    ItemStack randomHeadUUID = this.checkRandomHeadUUID();
                                    if (randomHeadUUID != null) {
                                       return randomHeadUUID;
                                    } else {
                                       ItemStack ownHead = this.checkOwnHead();
                                       if (ownHead != null) {
                                          return ownHead;
                                       } else {
                                          Logging.debug(this.configuration.getRouteAsString() + " has no valid item, returning default.");
                                          this.usingFallbackBaseItem = true;
                                          return new ItemStack(Material.COD);
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Nullable
   public <T extends ItemConfig<?>> T getItemConfig(@NonNull Class<T> clazz) {
      return (T)this.configMap.get(clazz);
   }

   @Nullable
   private ItemStack checkRawNbt() {
      String rawValue = this.configuration.getString("raw-nbt");
      return rawValue == null ? null : EvenMoreFish.getInstance().getVersionProvider().deserializeItemStack(rawValue);
   }

   @Nullable
   private ItemStack getItemFromMaterialString(@NonNull String materialString) {
      ItemStack material = ItemSerializer.get().deserializeMaterial(materialString);
      if (material != null) {
         return new ItemStack(material);
      } else {
         Logging.debug(materialString + " is not a valid material, checking for custom item.");
         ItemStack customItem = ItemSerializer.get().deserializeItemAddon(materialString);
         if (customItem != null) {
            Logging.debug(materialString + " was a valid ItemAddon.");
            this.usingItemAddon = true;
            return customItem;
         } else {
            EvenMoreFish.getInstance().getLogger().severe("Could not find material or custom item for: " + materialString);
            return null;
         }
      }
   }

   @Nullable
   private ItemStack checkMaterial() {
      String materialStr = this.configuration.getString("material");
      return materialStr == null ? null : this.getItemFromMaterialString(materialStr);
   }

   @Nullable
   private ItemStack checkRandomMaterial() {
      ArrayList<String> materialStrs = new ArrayList<>(this.configuration.getStringList("materials"));
      if (materialStrs.isEmpty()) {
         return null;
      } else {
         return materialStrs.size() == 1
            ? this.getItemFromMaterialString(materialStrs.getFirst())
            : this.getRandomItem(materialStrs, this::getItemFromMaterialString);
      }
   }

   @Nullable
   private ItemStack checkRawMaterial() {
      String materialStr = this.configuration.getString("raw-material");
      return materialStr == null ? null : this.getItemFromMaterialString(materialStr);
   }

   @Nullable
   private ItemStack checkRandomRawMaterial() {
      ArrayList<String> materialStrs = new ArrayList<>(this.configuration.getStringList("raw-materials"));
      if (materialStrs.isEmpty()) {
         return null;
      } else {
         return materialStrs.size() == 1
            ? this.getItemFromMaterialString(materialStrs.getFirst())
            : this.getRandomItem(materialStrs, this::getItemFromMaterialString);
      }
   }

   @Nullable
   private ItemStack checkHeadDB() {
      if (!EvenMoreFish.getInstance().getDependencyManager().isUsingHeadsDB()) {
         return null;
      } else {
         String materialStr = this.configuration.getString("headdb");
         if (materialStr == null) {
            return null;
         } else {
            HeadDatabaseAPI api = EvenMoreFish.getInstance().getDependencyManager().getHdbapi();
            if (api == null) {
               return null;
            } else {
               ItemStack item = api.getItemHead(materialStr);
               if (item == null) {
                  Logging.debug(this.configuration.getRouteAsString() + " has invalid headdb: " + materialStr);
                  return null;
               } else {
                  return item;
               }
            }
         }
      }
   }

   @Nullable
   private ItemStack checkRandomHeadDB() {
      if (!EvenMoreFish.getInstance().getDependencyManager().isUsingHeadsDB()) {
         return null;
      } else {
         ArrayList<String> materialStrs = new ArrayList<>(this.configuration.getStringList("multiple-headdb"));
         if (materialStrs.isEmpty()) {
            return null;
         } else {
            HeadDatabaseAPI api = EvenMoreFish.getInstance().getDependencyManager().getHdbapi();
            if (api == null) {
               return null;
            } else {
               return materialStrs.size() == 1 ? api.getItemHead(materialStrs.getFirst()) : this.getRandomItem(materialStrs, api::getItemHead);
            }
         }
      }
   }

   @Nullable
   private ItemStack checkHead64() {
      String materialStr = this.configuration.getString("head-64");
      return materialStr == null ? null : FishUtils.getSkullFromBase64(materialStr);
   }

   @Nullable
   private ItemStack checkRandomHead64() {
      ArrayList<String> materialStrs = new ArrayList<>(this.configuration.getStringList("multiple-head-64"));
      if (materialStrs.isEmpty()) {
         return null;
      } else {
         return materialStrs.size() == 1
            ? FishUtils.getSkullFromBase64(materialStrs.getFirst())
            : this.getRandomItem(materialStrs, FishUtils::getSkullFromBase64);
      }
   }

   @Nullable
   private ItemStack checkHeadUUID() {
      String materialStr = this.configuration.getString("head-uuid");
      return materialStr == null ? null : FishUtils.getSkullFromUUIDString(materialStr);
   }

   @Nullable
   private ItemStack checkRandomHeadUUID() {
      ArrayList<String> materialStrs = new ArrayList<>(this.configuration.getStringList("multiple-head-uuid"));
      if (materialStrs.isEmpty()) {
         return null;
      } else {
         return materialStrs.size() == 1
            ? FishUtils.getSkullFromUUIDString(materialStrs.getFirst())
            : this.getRandomItem(materialStrs, FishUtils::getSkullFromUUIDString);
      }
   }

   @Nullable
   private ItemStack checkOwnHead() {
      if (this.relevantPlayer == null) {
         return null;
      } else {
         String materialStr = this.configuration.getString("own-head");
         return materialStr == null ? null : FishUtils.getSkullFromUUID(this.relevantPlayer);
      }
   }

   @Nullable
   private ItemStack getRandomItem(@NonNull List<String> strings, @NonNull Function<String, ItemStack> function) {
      if (this.randomIndex != -1) {
         Logging.debug("Random index is set to " + this.randomIndex + ", trying to use it.");

         try {
            String randomStr = strings.get(this.randomIndex);
            ItemStack randomItem = function.apply(randomStr);
            if (randomItem != null) {
               return randomItem;
            }
         } catch (IndexOutOfBoundsException var8) {
            Logging.debug("Random index " + this.randomIndex + " is out of bounds, getting a new one.");
         }
      }

      ArrayList<String> checkList = new ArrayList<>(strings);
      Random random = EvenMoreFish.RANDOM;

      while (!checkList.isEmpty()) {
         int randomIndex = random.nextInt(checkList.size());
         String randomStr = checkList.remove(randomIndex);
         ItemStack randomItem = function.apply(randomStr);
         if (randomItem != null) {
            this.randomIndex = randomIndex;
            return randomItem;
         }

         Logging.debug(this.configuration.getRouteAsString() + " has an invalid name in its list: " + randomStr);
      }

      Logging.debug(this.configuration.getRouteAsString() + " has no valid items in its list.");
      return null;
   }

   public boolean isRawItem() {
      return this.rawItem;
   }

   @Override
   public void setRandomIndex(int randomIndex) {
      this.randomIndex = randomIndex;
      this.baseItem = this.getBaseItem();
   }

   @Override
   public int getRandomIndex() {
      return this.randomIndex;
   }

   public void setFinalChanges(@Nullable Consumer<ItemStack> finalChanges) {
      this.finalChanges = finalChanges;
   }

   public boolean isUsingFallbackBaseItem() {
      return this.usingFallbackBaseItem;
   }

   private void loadConfigMap() {
      this.loadConfig(new CustomModelDataItemConfig(this.configuration));
      this.loadConfig(new DisplayNameItemConfig(this.configuration));
      this.loadConfig(new DurabilityItemConfig(this.configuration));
      this.loadConfig(new DyeColorItemConfig(this.configuration));
      this.loadConfig(new EnchantmentsItemConfig(this.configuration));
      this.loadConfig(new FireResistantItemConfig(this.configuration));
      this.loadConfig(new GlowingItemConfig(this.configuration));
      this.loadConfig(new HideTooltipItemConfig(this.configuration));
      this.loadConfig(new ItemModelItemConfig(this.configuration));
      this.loadConfig(new ItemRarityItemConfig(this.configuration));
      this.loadConfig(new LoreItemConfig(this.configuration));
      this.loadConfig(new MaxStackSizeItemConfig(this.configuration));
      this.loadConfig(new PotionEffectItemConfig(this.configuration));
      this.loadConfig(new QuantityItemConfig(this.configuration));
      this.loadConfig(new TooltipStyleItemConfig(this.configuration));
      this.loadConfig(new UnbreakableItemConfig(this.configuration));
   }

   private <T extends ItemConfig<?>> void loadConfig(@NonNull T config) {
      this.configMap.put((Class<? extends ItemConfig>)config.getClass(), config);
   }
}
