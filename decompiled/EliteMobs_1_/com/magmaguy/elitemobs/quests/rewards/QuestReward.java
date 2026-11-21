package com.magmaguy.elitemobs.quests.rewards;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.instanced.dungeons.DynamicDungeonInstance;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.items.customloottable.CustomLootEntry;
import com.magmaguy.elitemobs.items.customloottable.CustomLootTable;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.DynamicQuestLevel;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class QuestReward implements Serializable {
   private static final long serialVersionUID = -2611659941991762555L;
   private final int rewardLevel;
   private final UUID playerUUID;
   private CustomLootTable customLootTable = null;
   private boolean generateProceduralReward = false;

   public QuestReward(int rewardLevel, QuestObjectives questObjectives, Player player) {
      this.rewardLevel = DynamicQuestLevel.clamp(rewardLevel);
      this.playerUUID = player.getUniqueId();
      int killAmount = questObjectives.getObjectives().stream().mapToInt(Objective::getTargetAmount).sum();
      int baselineReward = (int)(this.rewardLevel / 2.0 * killAmount);
      this.customLootTable = new CustomLootTable();
      this.customLootTable.generateCurrencyEntry(baselineReward);
      this.generateProceduralReward = true;
   }

   public QuestReward(CustomQuestsConfigFields customQuestsConfigFields, Player player) {
      this.rewardLevel = customQuestsConfigFields.getQuestLevel() * 10;
      this.playerUUID = player.getUniqueId();
      this.customLootTable = new CustomLootTable(customQuestsConfigFields);
   }

   private int getEffectiveRewardLevel(Player player) {
      if (player != null && PlayerData.getMatchInstance(player) instanceof DynamicDungeonInstance dynamicDungeonInstance) {
         int level = dynamicDungeonInstance.getSelectedLevel();
         if (ThreadLocalRandom.current().nextDouble() < 0.1) {
            level++;
         }

         return this.generateProceduralReward ? DynamicQuestLevel.clamp(level) : Math.max(1, level);
      } else {
         int level = CombatLevelCalculator.calculateCombatLevel(this.playerUUID);
         if (ThreadLocalRandom.current().nextDouble() < 0.1) {
            level++;
         }

         return this.generateProceduralReward ? DynamicQuestLevel.clamp(level) : Math.max(1, level);
      }
   }

   public void doRewards() {
      Player player = Bukkit.getPlayer(this.playerUUID);
      this.doRewards(player);
   }

   public void doRewards(Player player) {
      if (player == null) {
         Logger.warn("Tried to give quest rewards to an offline or missing player with UUID " + this.playerUUID + ". Rewards were skipped to prevent an error.");
      } else {
         int effectiveLevel = this.getEffectiveRewardLevel(player);
         this.customLootTable.questDrop(player, effectiveLevel);
         if (this.generateProceduralReward) {
            ItemStack itemReward = LootTables.generateItemStack(effectiveLevel, player, null);
            if (itemReward != null) {
               HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(new ItemStack[]{itemReward});
               leftOvers.values().forEach(leftOver -> player.getWorld().dropItem(player.getLocation(), leftOver));
            }
         }
      }
   }

   public List<ItemStack> previewRewards() {
      return this.previewRewardEntries().stream().map(QuestReward.PreviewEntry::item).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
   }

   public List<QuestReward.PreviewEntry> previewRewardEntries() {
      Player player = Bukkit.getPlayer(this.playerUUID);
      int effectiveLevel = this.getEffectiveRewardLevel(player);
      List<QuestReward.PreviewEntry> items = new ArrayList<>();
      if (this.customLootTable != null) {
         for (CustomLootEntry entry : this.customLootTable.getEntries()) {
            if (entry.willDrop(player)) {
               items.add(new QuestReward.PreviewEntry(entry, entry.previewDrop(effectiveLevel, player)));
            }
         }
      }

      if (this.generateProceduralReward) {
         ItemStack itemReward = LootTables.generateItemStack(effectiveLevel, player, null);
         if (itemReward != null) {
            items.add(new QuestReward.PreviewEntry(null, itemReward));
         }
      }

      return items;
   }

   @Generated
   public int getRewardLevel() {
      return this.rewardLevel;
   }

   @Generated
   public UUID getPlayerUUID() {
      return this.playerUUID;
   }

   @Generated
   public CustomLootTable getCustomLootTable() {
      return this.customLootTable;
   }

   public record PreviewEntry(CustomLootEntry source, ItemStack item) {
   }
}
