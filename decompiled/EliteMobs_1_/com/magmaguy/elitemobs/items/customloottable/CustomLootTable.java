package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomLootTable implements Serializable {
   private static final long serialVersionUID = -3145961375108085416L;
   private final List<CustomLootEntry> entries = new ArrayList<>();
   private final Map<Integer, List<CustomLootEntry>> waveRewards = new HashMap<>();

   public CustomLootTable() {
   }

   public CustomLootTable(CustomBossesConfigFields customBossesConfigFields) {
      List<Object> rawStrings = customBossesConfigFields.getUniqueLootList();
      this.parseConfig(rawStrings, customBossesConfigFields.getFilename());
   }

   public CustomLootTable(CustomTreasureChestConfigFields treasureChestConfigFields) {
      List<Object> rawStrings = treasureChestConfigFields.getLootList();
      this.parseConfig(rawStrings, treasureChestConfigFields.getFilename());
   }

   public CustomLootTable(CustomQuestsConfigFields questsConfigFields) {
      List<String> rawStrings = questsConfigFields.getCustomRewardsList();
      this.parseConfig(rawStrings, questsConfigFields.getFilename());
   }

   public CustomLootTable(CustomArenasConfigFields customArenasConfigFields) {
      List<Object> rawStrings = customArenasConfigFields.getRawArenaRewards();
      this.parseConfig(rawStrings, customArenasConfigFields.getFilename());
   }

   private void parseConfig(List<?> lootTable, String filename) {
      if (lootTable != null) {
         for (Object raw : lootTable) {
            try {
               if (raw instanceof String value && !value.contains("=")) {
                  new EliteCustomLootEntry(this.entries, value, filename);
               } else {
                  Map<String, Object> definition = CustomLootEntry.fields(raw);
                  if (definition.containsKey("currencyamount")) {
                     new CurrencyCustomLootEntry(this.entries, definition, filename);
                  } else if (definition.containsKey("material") || definition.containsKey("type")) {
                     new VanillaCustomLootEntry(this.entries, definition, filename);
                  } else if (definition.containsKey("command")) {
                     new CommandLootTable(this.entries, definition, filename);
                  } else {
                     new EliteCustomLootEntry(this.entries, definition, filename);
                  }
               }
            } catch (RuntimeException var7) {
               CustomLootEntry.errorMessage(String.valueOf(raw), filename, var7.getMessage());
            }
         }

         for (CustomLootEntry customLootEntry : this.entries) {
            if (customLootEntry.getWave() > 0) {
               if (this.waveRewards.get(customLootEntry.getWave()) != null) {
                  List<CustomLootEntry> rewards = this.waveRewards.get(customLootEntry.getWave());
                  rewards.add(customLootEntry);
                  this.waveRewards.put(customLootEntry.getWave(), rewards);
               } else {
                  this.waveRewards.put(customLootEntry.getWave(), new ArrayList<>(List.of(customLootEntry)));
               }
            }
         }
      }
   }

   public void generateCurrencyEntry(int currencyAmount) {
      new CurrencyCustomLootEntry(this.entries, currencyAmount);
   }

   public void generateEliteEntry(ItemStack itemStack) {
      new ItemStackCustomLootEntry(this.entries, itemStack);
   }

   public void bossDrop(Player player, int level, Location dropLocation, EliteEntity eliteEntity) {
      this.bossDrop(player, level, dropLocation, eliteEntity, false);
   }

   public void bossDrop(Player player, int level, Location dropLocation, EliteEntity eliteEntity, boolean materialsOnly) {
      for (CustomLootEntry customLootEntry : this.entries) {
         if (!(customLootEntry instanceof EliteCustomLootEntry customxx && customxx.isClassLoot())
            && (!materialsOnly || customLootEntry instanceof EliteCustomLootEntry customx && !customx.isEquipment())
            && !(customLootEntry instanceof EliteCustomLootEntry custom ? !custom.willDrop(player, eliteEntity) : !customLootEntry.willDrop(player))) {
            if (!ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()
               || eliteEntity instanceof InstancedBossEntity instance && instance.getDungeonInstance() == null) {
               customLootEntry.locationDrop(level, player, dropLocation, eliteEntity);
            } else {
               customLootEntry.directDrop(level, player, eliteEntity);
            }
         }
      }
   }

   public void treasureChestDrop(Player player, int chestLevel, Location dropLocation) {
      boolean anythingDropped = false;

      for (CustomLootEntry customLootEntry : this.entries) {
         if (customLootEntry.willDrop(player)) {
            if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) {
               anythingDropped |= customLootEntry.directDrop(chestLevel * 10, player);
            } else {
               anythingDropped |= customLootEntry.locationDrop(chestLevel * 10, player, dropLocation);
            }
         }
      }

      if (!anythingDropped) {
         player.sendMessage(DefaultConfig.getTreasureChestNoDropMessage());
      }
   }

   public void treasureChestDropAtLevel(Player player, int level, Location dropLocation) {
      boolean anythingDropped = false;

      for (CustomLootEntry customLootEntry : this.entries) {
         if (customLootEntry.willDrop(player)) {
            if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) {
               if (customLootEntry instanceof EliteCustomLootEntry eliteCustomLootEntry) {
                  int exactLevel = level;
                  if (eliteCustomLootEntry.isScalableItem()) {
                     exactLevel = Math.max(1, level + ThreadLocalRandom.current().nextInt(-1, 2));
                  }

                  anythingDropped |= eliteCustomLootEntry.directDropExactLevel(exactLevel, player);
               } else {
                  anythingDropped |= customLootEntry.directDrop(level, player);
               }
            } else if (customLootEntry instanceof EliteCustomLootEntry eliteCustomLootEntry) {
               int exactLevel = level;
               if (eliteCustomLootEntry.isScalableItem()) {
                  exactLevel = Math.max(1, level + ThreadLocalRandom.current().nextInt(-1, 2));
               }

               anythingDropped |= eliteCustomLootEntry.locationDropExactLevel(exactLevel, player, dropLocation);
            } else {
               anythingDropped |= customLootEntry.locationDrop(level, player, dropLocation);
            }
         }
      }

      if (!anythingDropped) {
         player.sendMessage(DefaultConfig.getTreasureChestNoDropMessage());
      }
   }

   public void treasureChestDropScalableToPlayerLevel(Player player, int chestLevel, int playerLevel, Location dropLocation) {
      boolean anythingDropped = false;
      int defaultLevel = chestLevel * 10;

      for (CustomLootEntry customLootEntry : this.entries) {
         if (customLootEntry.willDrop(player)) {
            if (customLootEntry instanceof EliteCustomLootEntry eliteCustomLootEntry && eliteCustomLootEntry.isScalableItem()) {
               int scalableLevel = Math.max(1, playerLevel + ThreadLocalRandom.current().nextInt(-1, 2));
               if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) {
                  anythingDropped |= eliteCustomLootEntry.directDropExactLevel(scalableLevel, player);
               } else {
                  anythingDropped |= eliteCustomLootEntry.locationDropExactLevel(scalableLevel, player, dropLocation);
               }
            } else if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) {
               anythingDropped |= customLootEntry.directDrop(defaultLevel, player);
            } else {
               anythingDropped |= customLootEntry.locationDrop(defaultLevel, player, dropLocation);
            }
         }
      }

      if (!anythingDropped) {
         player.sendMessage(DefaultConfig.getTreasureChestNoDropMessage());
      }
   }

   public void questDrop(Player player, int questRewardLevel) {
      for (CustomLootEntry customLootEntry : this.entries) {
         if (customLootEntry.willDrop(player)) {
            customLootEntry.directDrop(questRewardLevel, player);
         }
      }
   }

   public List<ItemStack> previewQuestDrop(Player player, int questRewardLevel) {
      List<ItemStack> items = new ArrayList<>();

      for (CustomLootEntry customLootEntry : this.entries) {
         if (customLootEntry.willDrop(player)) {
            items.add(customLootEntry.previewDrop(questRewardLevel, player));
         }
      }

      return items;
   }

   public void arenaReward(Player player, int wave) {
      if (this.waveRewards.get(wave) != null) {
         this.waveRewards.get(wave).forEach(reward -> {
            if (reward.willDrop(player)) {
               reward.directDrop(reward.getItemLevel(), player);
            }
         });
      }
   }

   @Generated
   public List<CustomLootEntry> getEntries() {
      return this.entries;
   }
}
