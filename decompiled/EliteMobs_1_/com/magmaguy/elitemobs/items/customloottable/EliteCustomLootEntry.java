package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.PluginState;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DifficultyResolver;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.items.LootItemPolicy;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EliteCustomLootEntry extends CustomLootEntry implements Serializable {
   private static final long serialVersionUID = 6544054339653615367L;
   private String filename = null;
   private List<String> difficultyIDs = null;
   private final String configFilename;

   public List<String> getDifficultyIDs() {
      return this.difficultyIDs == null ? null : List.copyOf(this.difficultyIDs);
   }

   public EliteCustomLootEntry(List<CustomLootEntry> entries, String rawString, String configFilename) {
      this.configFilename = configFilename;
      Map<String, Object> definition;
      if (!rawString.contains("=")) {
         String[] legacy = rawString.split(":", -1);
         if (legacy.length > 3) {
            throw new IllegalArgumentException("legacy loot expects filename:chance:permission");
         }

         definition = new LinkedHashMap<>();
         definition.put("filename", legacy[0]);
         if (legacy.length > 1) {
            definition.put("chance", legacy[1]);
         }

         if (legacy.length > 2) {
            definition.put("permission", legacy[2]);
         }
      } else {
         definition = fields(rawString);
      }

      this.parse(definition);
      this.register(entries, rawString);
   }

   public EliteCustomLootEntry(List<CustomLootEntry> entries, Map<?, ?> configMap, String configFilename) {
      this.configFilename = configFilename;
      this.parse(fields(configMap));
      this.register(entries, configMap.toString());
   }

   private void parse(Map<String, Object> definition) {
      this.commonFields(definition, true, new String[]{"filename", "difficultyid"});
      this.filename = text(definition, "filename");
      if (definition.containsKey("difficultyid")) {
         Object raw = definition.get("difficultyid");
         List<?> values = raw instanceof List<?> list ? list : Collections.singletonList(raw);
         if (values.isEmpty() || values.stream().anyMatch(value -> !(value instanceof String) && !(value instanceof Number) || value.toString().isBlank())) {
            throw new IllegalArgumentException("difficultyID must contain nonempty IDs");
         }

         this.difficultyIDs = DifficultyResolver.parseFilter(raw, this.configFilename);
      }
   }

   private void register(List<CustomLootEntry> entries, String raw) {
      if (!CustomItem.isUnavailableWithoutModels(this.filename)) {
         if (CustomItem.getCustomItem(this.filename) == null && MetadataHandler.pluginState != PluginState.INITIALIZING) {
            errorMessage(raw, this.configFilename, "filename");
         }

         entries.add(this);
      }
   }

   private CustomItem generateCustomItem() {
      return CustomItem.getCustomItem(this.filename);
   }

   public ItemStack generateItemStack(int level, Player player, EliteEntity eliteEntity) {
      if (CustomItem.isUnavailableWithoutModels(this.filename)) {
         return null;
      } else {
         CustomItem customItem = this.generateCustomItem();
         if (customItem == null) {
            Logger.warn("Invalid custom item entry! Entry: " + this.filename);
            return null;
         } else {
            return customItem.generateItemStack(level, player, eliteEntity);
         }
      }
   }

   public boolean isScalableItem() {
      CustomItem customItem = this.generateCustomItem();
      return customItem != null && customItem.getScalability() == CustomItem.Scalability.SCALABLE;
   }

   @Override
   public boolean willDrop(Player player) {
      return this.willDrop(player, null);
   }

   public boolean willDrop(Player player, EliteEntity source) {
      return this.matchesDifficulty(player, source) && super.willDrop(player);
   }

   private boolean matchesDifficulty(Player player, EliteEntity source) {
      if (this.difficultyIDs == null) {
         return true;
      } else {
         return source instanceof InstancedBossEntity instanced
            ? instanced.matchesDifficulty(this.difficultyIDs, this.configFilename)
            : !(
               (player == null ? null : PlayerData.getMatchInstance(player)) instanceof DungeonInstance dungeon
                  && !dungeon.matchesDifficulty(this.difficultyIDs, this.configFilename)
            );
      }
   }

   @Override
   public boolean locationDrop(int itemTier, Player player, Location location) {
      return this.dropPhysical(itemTier, player, location, null, false);
   }

   public boolean locationDropExactLevel(int itemTier, Player player, Location location) {
      return this.dropPhysical(itemTier, player, location, null, true);
   }

   @Override
   public boolean locationDrop(int itemTier, Player player, Location location, EliteEntity eliteEntity) {
      EliteCustomLootEntry.GroupDelivery group = this.groupDelivery(itemTier, player, eliteEntity);
      return group != EliteCustomLootEntry.GroupDelivery.PERSONAL
         ? group == EliteCustomLootEntry.GroupDelivery.DELIVERED
         : this.dropPhysical(itemTier, player, location, eliteEntity, false);
   }

   private boolean dropPhysical(int level, Player player, Location location, EliteEntity source, boolean exactLevel) {
      if (CustomItem.isUnavailableWithoutModels(this.filename)) {
         return false;
      } else if (this.matchesDifficulty(player, source) && this.getAmount() > 0) {
         CustomItem item = this.generateCustomItem();
         if (item == null) {
            Logger.warn("Invalid loot entry for " + (source == null ? "treasure chest" : "boss " + source.getName()) + "! Entry: " + this.filename);
            return false;
         } else {
            boolean delivered = false;

            for (int copy = 0; copy < this.getAmount(); copy++) {
               delivered |= (exactLevel ? item.dropPlayerLootExact(player, level, location, source) : item.dropPlayerLoot(player, level, location, source))
                  != null;
            }

            return delivered;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean directDrop(int itemTier, Player player) {
      return this.dropDirect(itemTier, player, null, false);
   }

   public boolean directDropExactLevel(int itemTier, Player player) {
      return this.dropDirect(itemTier, player, null, true);
   }

   @Override
   public boolean directDrop(int itemTier, Player player, EliteEntity eliteEntity) {
      if (eliteEntity instanceof InstancedBossEntity instance && instance.getDungeonInstance() == null) {
         return this.dropPhysical(itemTier, player, eliteEntity.getLocation(), eliteEntity, false);
      } else {
         EliteCustomLootEntry.GroupDelivery group = this.groupDelivery(itemTier, player, eliteEntity);
         return group != EliteCustomLootEntry.GroupDelivery.PERSONAL
            ? group == EliteCustomLootEntry.GroupDelivery.DELIVERED
            : this.dropDirect(itemTier, player, eliteEntity, false);
      }
   }

   private boolean dropDirect(int level, Player player, EliteEntity source, boolean exactLevel) {
      if (CustomItem.isUnavailableWithoutModels(this.filename)) {
         return false;
      } else if (this.matchesDifficulty(player, source) && this.getAmount() > 0) {
         CustomItem item = this.generateCustomItem();
         if (item == null) {
            Logger.warn("Invalid loot entry for " + (source == null ? "direct drop" : "boss " + source.getName()) + "! Entry: " + this.filename);
            return false;
         } else {
            String name = null;
            int stored = 0;
            boolean delivered = false;

            for (int copy = 0; copy < this.getAmount(); copy++) {
               ItemStack stack = exactLevel ? item.generateItemStackExact(level, player, source) : item.generateItemStack(level, player, source);
               if (stack != null && !stack.getType().isAir() && stack.getAmount() > 0) {
                  stored += deliver(player, stack);
                  delivered = true;
                  if (name == null && stack.getItemMeta() != null) {
                     name = stack.getItemMeta().hasDisplayName() ? stack.getItemMeta().getDisplayName() : stack.getType().toString().replace("_", " ");
                  }
               }
            }

            if (name != null && stored > 0) {
               player.sendMessage(ItemSettingsConfig.getDirectDropCustomLootMessage().replace("$itemName", stored + "x " + name));
            }

            return delivered;
         }
      } else {
         return false;
      }
   }

   private static int deliver(Player player, ItemStack item) {
      int amount = item.getAmount();
      HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(new ItemStack[]{item});
      int dropped = 0;

      for (ItemStack leftover : overflow.values()) {
         dropped += leftover.getAmount();
         Item entity = player.getWorld().dropItem(player.getLocation(), leftover);
         entity.setOwner(player.getUniqueId());
      }

      return amount - dropped;
   }

   public boolean isClassLoot() {
      CustomItem item = this.generateCustomItem();
      return item != null && item.getItemType() == CustomItem.ItemType.CLASS_LOOT;
   }

   public boolean eligibleForClassLoot(Player player, Predicate<List<String>> difficultyFilter) {
      CustomItem item = this.generateCustomItem();
      return this.isClassLoot()
         && item.getCustomItemsConfigFields().isEnabled()
         && this.getAmount() > 0
         && Double.isFinite(this.getChance())
         && this.getChance() > 0.0
         && (this.getPermission().isEmpty() || player != null && player.hasPermission(this.getPermission()))
         && (item.getPermission().isEmpty() || player != null && player.hasPermission(item.getPermission()))
         && (this.difficultyIDs == null || difficultyFilter.test(this.difficultyIDs));
   }

   public boolean isEquipment() {
      return LootItemPolicy.isEquipment(this.generateCustomItem());
   }

   private EliteCustomLootEntry.GroupDelivery groupDelivery(int itemTier, Player player, EliteEntity eliteEntity) {
      if (CustomItem.isUnavailableWithoutModels(this.filename)) {
         return EliteCustomLootEntry.GroupDelivery.SKIPPED;
      } else if (this.matchesDifficulty(player, eliteEntity) && this.getAmount() > 0) {
         if (eliteEntity instanceof InstancedBossEntity instance && instance.getDungeonInstance() == null) {
            return EliteCustomLootEntry.GroupDelivery.PERSONAL;
         } else {
            if (this.difficultyIDs != null) {
               MatchInstance matchInstance = player == null ? null : PlayerData.getMatchInstance(player);
               if (matchInstance instanceof DungeonInstance) {
                  if (!this.isEquipment()) {
                     return EliteCustomLootEntry.GroupDelivery.PERSONAL;
                  }

                  return this.addGroupLoot(CustomItem.limitItemLevel(player, itemTier), eliteEntity)
                     ? EliteCustomLootEntry.GroupDelivery.DELIVERED
                     : EliteCustomLootEntry.GroupDelivery.SKIPPED;
               }
            }

            return this.isEquipment() && PartyManager.shouldUsePartyLoot(player, eliteEntity)
               ? this.addPartyLoot(CustomItem.limitItemLevel(player, itemTier), player, eliteEntity)
               : EliteCustomLootEntry.GroupDelivery.PERSONAL;
         }
      } else {
         return EliteCustomLootEntry.GroupDelivery.SKIPPED;
      }
   }

   private boolean addGroupLoot(int itemTier, EliteEntity eliteEntity) {
      SharedLootTable sharedLootTable = SharedLootTable.getSharedLootTables().get(eliteEntity);
      boolean delivered = false;

      for (int i = 0; i < this.getAmount(); i++) {
         CustomItem customItem = this.generateCustomItem();
         if (customItem == null) {
            Logger.warn(
               "Failed to generate a custom item for the boss "
                  + eliteEntity.getName()
                  + "! The configuration file for one of its loot items is not correctly configured."
            );
            return delivered;
         }

         ItemStack itemStack = customItem.generateItemStack(itemTier, null, eliteEntity);
         if (itemStack == null) {
            Logger.warn("A custom item for boss " + eliteEntity.getName() + " was null! This item will be skipped.");
            return delivered;
         }

         if (sharedLootTable == null) {
            sharedLootTable = new SharedLootTable(eliteEntity);
         }

         delivered |= sharedLootTable.addLoot(itemStack, this.getPermission());
      }

      return delivered;
   }

   private EliteCustomLootEntry.GroupDelivery addPartyLoot(int itemTier, Player contributor, EliteEntity eliteEntity) {
      boolean pooledAnyItem = false;

      for (int i = 0; i < this.getAmount(); i++) {
         CustomItem customItem = this.generateCustomItem();
         if (customItem == null) {
            Logger.warn(
               "Failed to generate a custom item for the boss "
                  + eliteEntity.getName()
                  + "! The configuration file for one of its loot items is not correctly configured."
            );
            return pooledAnyItem ? EliteCustomLootEntry.GroupDelivery.DELIVERED : EliteCustomLootEntry.GroupDelivery.SKIPPED;
         }

         ItemStack itemStack = customItem.generateItemStack(itemTier, null, eliteEntity);
         if (itemStack == null) {
            Logger.warn("A custom item for boss " + eliteEntity.getName() + " was null! This item will be skipped.");
            return pooledAnyItem ? EliteCustomLootEntry.GroupDelivery.DELIVERED : EliteCustomLootEntry.GroupDelivery.SKIPPED;
         }

         if (!SharedLootTable.addPartyLoot(eliteEntity, contributor, itemStack, this.getPermission(), null)) {
            return pooledAnyItem ? EliteCustomLootEntry.GroupDelivery.DELIVERED : EliteCustomLootEntry.GroupDelivery.PERSONAL;
         }

         pooledAnyItem = true;
      }

      return pooledAnyItem ? EliteCustomLootEntry.GroupDelivery.DELIVERED : EliteCustomLootEntry.GroupDelivery.SKIPPED;
   }

   @Override
   public ItemStack previewDrop(int itemTier, Player player) {
      return this.generateItemStack(itemTier, player, null);
   }

   public ItemStack previewDropAtLevel(int level, Player player) {
      CustomItem item = this.generateCustomItem();
      return item == null ? null : item.generateItemStackExact(level, player, null);
   }

   @Generated
   public String getFilename() {
      return this.filename;
   }

   private static enum GroupDelivery {
      PERSONAL,
      SKIPPED,
      DELIVERED;
   }
}
