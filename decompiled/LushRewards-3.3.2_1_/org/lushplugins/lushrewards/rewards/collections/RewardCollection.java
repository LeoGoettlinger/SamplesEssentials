package org.lushplugins.lushrewards.rewards.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.libraries.lushlib.utils.DisplayItemStack;
import org.lushplugins.lushrewards.libraries.lushlib.utils.StringUtils;
import org.lushplugins.lushrewards.libraries.lushlib.utils.converter.MapConverter;
import org.lushplugins.lushrewards.libraries.lushlib.utils.converter.YamlConverter;
import org.lushplugins.lushrewards.rewards.Reward;
import org.lushplugins.lushrewards.utils.Debugger;

public class RewardCollection {
   protected final Collection<Reward> rewards;
   protected final int priority;
   protected final String category;
   protected final DisplayItemStack displayItem;
   protected final Sound sound;

   public RewardCollection(
      @Nullable Collection<Reward> rewards, int priority, @Nullable String category, @Nullable DisplayItemStack displayItem, @Nullable Sound sound
   ) {
      this.rewards = (Collection<Reward>)(rewards != null ? rewards : Collections.emptyList());
      this.priority = priority;
      this.category = category;
      this.displayItem = displayItem;
      this.sound = sound;
   }

   public Collection<Reward> getRewards() {
      return this.rewards;
   }

   public int getPriority() {
      return this.priority;
   }

   public String getCategory() {
      return this.category;
   }

   public DisplayItemStack getDisplayItem() {
      return this.displayItem != null ? this.displayItem : LushRewards.getInstance().getConfigManager().getCategoryTemplate(this.category);
   }

   public Sound getSound() {
      return this.sound;
   }

   public int getRewardCount() {
      return this.rewards.size();
   }

   public boolean isEmpty() {
      return this.rewards.isEmpty();
   }

   public void giveAll(Player player) {
      for (Reward reward : this.rewards) {
         try {
            reward.giveReward(player);
         } catch (Exception var5) {
            LushRewards.getInstance().getLogger().severe("Error occurred when giving reward (" + reward.toString() + ") to " + player.getName());
            var5.printStackTrace();
         }
      }
   }

   @NotNull
   public static RewardCollection from(ConfigurationSection rewardCollectionSection) {
      Debugger.DebugMode debugMode = Debugger.DebugMode.DAILY;
      Debugger.sendDebugMessage("Attempting to load reward collection at '" + rewardCollectionSection.getCurrentPath() + "'", debugMode);
      int priority = rewardCollectionSection.getInt("priority", 0);
      Debugger.sendDebugMessage("Reward collection priority set to " + priority, debugMode);
      String category = rewardCollectionSection.getString("category", "small");
      Debugger.sendDebugMessage("Reward collection category set to " + category, debugMode);
      ConfigurationSection itemSection = rewardCollectionSection.getConfigurationSection("display-item");
      DisplayItemStack displayItem = itemSection != null ? YamlConverter.getDisplayItem(itemSection) : DisplayItemStack.empty();
      Debugger.sendDebugMessage("Reward collection item set to: " + displayItem, debugMode);
      Sound redeemSound = (Sound)StringUtils.getEnum(rewardCollectionSection.getString("redeem-sound", "ENTITY_EXPERIENCE_ORB_PICKUP"), Sound.class)
         .orElse(null);
      Debugger.sendDebugMessage("Attempting to load rewards", debugMode);
      List<Map<?, ?>> rewardMaps = rewardCollectionSection.getMapList("rewards");
      List<Reward> rewardList = !rewardMaps.isEmpty() ? Reward.loadRewards(rewardMaps, rewardCollectionSection.getCurrentPath() + ".rewards") : null;
      Debugger.sendDebugMessage(
         "Successfully loaded " + (rewardList != null ? rewardList.size() : 0) + " rewards from '" + rewardCollectionSection.getCurrentPath() + "'", debugMode
      );
      return rewardList != null ? new RewardCollection(rewardList, 0, category, displayItem, redeemSound) : empty();
   }

   @NotNull
   public static RewardCollection from(Map<?, ?> rewardCollectionMap) {
      Debugger.DebugMode debugMode = Debugger.DebugMode.DAILY;
      Debugger.sendDebugMessage("Attempting to load reward collection at '" + rewardCollectionMap + "'", debugMode);
      int priority = rewardCollectionMap.containsKey("priority") ? (Integer)rewardCollectionMap.get("priority") : 0;
      Debugger.sendDebugMessage("Reward collection priority set to " + priority, debugMode);
      String category = rewardCollectionMap.containsKey("category") ? (String)rewardCollectionMap.get("category") : "small";
      Debugger.sendDebugMessage("Reward collection category set to " + category, debugMode);
      Map<?, ?> itemMap = (Map<?, ?>)rewardCollectionMap.get("display-item");
      DisplayItemStack itemStack = itemMap != null ? MapConverter.getDisplayItemBuilder(itemMap).build() : DisplayItemStack.empty();
      Debugger.sendDebugMessage("Reward collection item set to: " + itemStack, debugMode);
      Sound redeemSound = rewardCollectionMap.containsKey("redeem-sound")
         ? (Sound)StringUtils.getEnum((String)rewardCollectionMap.get("redeem-sound"), Sound.class).orElse(null)
         : Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
      Debugger.sendDebugMessage("Attempting to load rewards", debugMode);
      List<Map<?, ?>> rewardMaps = (List<Map<?, ?>>)rewardCollectionMap.get("rewards");
      List<Reward> rewardList = !rewardMaps.isEmpty() ? Reward.loadRewards(rewardMaps, rewardCollectionMap.toString()) : null;
      Debugger.sendDebugMessage(
         "Successfully loaded " + (rewardList != null ? rewardList.size() : 0) + " rewards from '" + rewardCollectionMap + "'", debugMode
      );
      return rewardList != null ? new RewardCollection(rewardList, priority, category, itemStack, redeemSound) : empty();
   }

   public static RewardCollection empty() {
      return new RewardCollection(null, 0, null, null, null);
   }
}
