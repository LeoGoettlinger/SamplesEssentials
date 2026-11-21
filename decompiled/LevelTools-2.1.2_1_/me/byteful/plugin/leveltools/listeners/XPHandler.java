package me.byteful.plugin.leveltools.listeners;

import java.util.List;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.event.LevelToolsLevelIncreaseEvent;
import me.byteful.plugin.leveltools.api.event.LevelToolsXPIncreaseEvent;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.api.trigger.TriggerSlot;
import me.byteful.plugin.leveltools.libs.xseries.XSound;
import me.byteful.plugin.leveltools.profile.ProfileManager;
import me.byteful.plugin.leveltools.profile.display.DisplayProfile;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.profile.reward.RewardEntry;
import me.byteful.plugin.leveltools.profile.reward.RewardProfile;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import me.byteful.plugin.leveltools.util.Text;
import me.byteful.plugin.leveltools.util.XPBooster;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class XPHandler {
   private final ProfileManager profileManager;

   public XPHandler(@NotNull ProfileManager profileManager) {
      this.profileManager = profileManager;
   }

   public void handle(@NotNull Player player, @NotNull ItemProfile itemProfile, @Nullable TriggerSlot slot, @NotNull LevelToolsItem tool, double modifier) {
      List<String> disabledWorlds = LevelToolsPlugin.getInstance().getConfig().getStringList("disabled_worlds");
      if (!disabledWorlds.contains(player.getWorld().getName())) {
         modifier = Math.max(0.0, XPBooster.apply(player, modifier));
         double newXp = LevelToolsUtil.round(tool.getXp() + modifier, 1);
         LevelToolsXPIncreaseEvent xpEvent = new LevelToolsXPIncreaseEvent(tool, player, newXp, newXp, false);
         Bukkit.getPluginManager().callEvent(xpEvent);
         if (!xpEvent.isCancelled()) {
            tool.setXp(xpEvent.getNewXp());
            if (tool.getXp() >= tool.getMaxXp()) {
               this.handleLevelUp(tool, player, itemProfile, slot);
            }

            this.updateItem(player, slot, tool.getItemStack());
            this.showActionBar(tool, player, itemProfile);
            this.handleReward(tool, player, itemProfile, slot);
         }
      }
   }

   private void handleLevelUp(@NotNull LevelToolsItem tool, @NotNull Player player, @NotNull ItemProfile itemProfile, @Nullable TriggerSlot slot) {
      int newLevel = tool.getLevel() + 1;
      int maxLevel = itemProfile.getMaxLevel();
      if (newLevel > maxLevel) {
         if (tool.getXp() != tool.getMaxXp()) {
            tool.setXp(tool.getMaxXp());
            this.updateItem(player, slot, tool.getItemStack());
         }
      } else {
         LevelToolsLevelIncreaseEvent levelEvent = new LevelToolsLevelIncreaseEvent(tool, player, newLevel, false);
         Bukkit.getPluginManager().callEvent(levelEvent);
         if (!levelEvent.isCancelled()) {
            tool.setXp(LevelToolsUtil.round(Math.abs(tool.getXp() - tool.getMaxXp()), 1));
            tool.setLevel(levelEvent.getNewLevel());
            if (levelEvent.getNewLevel() == maxLevel) {
               tool.setXp(0.0);
               tool.setLevel(maxLevel);
            }

            this.playLevelUpSound(player);
         }
      }
   }

   private void playLevelUpSound(@NotNull Player player) {
      ConfigurationSection soundCs = LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("level_up_sound");
      if (soundCs != null) {
         String sound = soundCs.getString("sound", null);
         if (sound != null) {
            XSound parsed = XSound.matchXSound(sound).orElse(null);
            if (parsed != null && parsed.isSupported() && parsed.parseSound() != null) {
               player.playSound(player.getLocation(), parsed.parseSound(), (float)soundCs.getDouble("pitch"), (float)soundCs.getDouble("volume"));
            }
         }
      }
   }

   private void showActionBar(@NotNull LevelToolsItem tool, @NotNull Player player, @NotNull ItemProfile itemProfile) {
      DisplayProfile displayProfile = this.profileManager.getDisplayProfileFor(itemProfile);
      if (displayProfile != null) {
         DisplayProfile.ActionBarDisplay actionBar = displayProfile.getActionBarDisplay();
         if (actionBar.isEnabled()) {
            String progressBar = displayProfile.getProgressBar().buildProgressBar(tool.getXp(), tool.getMaxXp());
            String text = Text.colorize(
               actionBar.getText()
                  .replace("{progress_bar}", progressBar)
                  .replace("{xp}", String.valueOf(tool.getXp()))
                  .replace("{max_xp}", String.valueOf(tool.getMaxXp()))
                  .replace("{level}", String.valueOf(tool.getLevel()))
                  .replace("{max_xp_formatted}", Text.formatMoney(tool.getMaxXp()))
                  .replace("{xp_formatted}", Text.formatMoney(tool.getXp()))
            );
            LevelToolsUtil.sendActionBar(player, text);
         }
      }
   }

   private void handleReward(@NotNull LevelToolsItem tool, @NotNull Player player, @NotNull ItemProfile itemProfile, @Nullable TriggerSlot slot) {
      RewardProfile rewardProfile = this.profileManager.getRewardProfileFor(itemProfile);
      if (rewardProfile != null) {
         int level = tool.getLevel();
         if (rewardProfile.hasRewardsForLevel(level)) {
            if (tool.getLastHandledReward() != level) {
               tool.setLastHandledReward(level);
               this.updateItem(player, slot, tool.getItemStack());

               for (RewardEntry entry : rewardProfile.getRewardsForLevel(level)) {
                  entry.apply(tool, player);
                  if (entry.shouldUpdateItem()) {
                     this.updateItem(player, slot, tool.getItemStack());
                  }
               }
            }
         }
      }
   }

   private void updateItem(@NotNull Player player, @Nullable TriggerSlot slot, @NotNull ItemStack itemStack) {
      LevelToolsUtil.setItemInSlot(player, slot, itemStack);
   }
}
