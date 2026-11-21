package de.theredend2000.advancedhunt.model;

import de.theredend2000.advancedhunt.Main;
import java.util.ArrayList;
import java.util.List;

public class PresetRewardHolder implements RewardHolder {
   private final Main plugin;
   private final RewardPreset preset;

   public PresetRewardHolder(Main plugin, RewardPreset preset) {
      this.plugin = plugin;
      this.preset = preset;
   }

   public RewardPreset getPreset() {
      return this.preset;
   }

   @Override
   public List<Reward> getRewards() {
      return this.preset.getRewards() != null ? new ArrayList<>(this.preset.getRewards()) : new ArrayList<>();
   }

   @Override
   public void saveRewards(List<Reward> rewards) {
      this.preset.setRewards(rewards);
      this.plugin.getRewardPresetManager().savePreset(this.preset);
   }

   @Override
   public String getRewardsTitleKey() {
      return "gui.rewards.preset_title";
   }
}
