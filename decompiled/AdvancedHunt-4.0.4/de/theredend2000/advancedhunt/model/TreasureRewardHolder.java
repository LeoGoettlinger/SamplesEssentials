package de.theredend2000.advancedhunt.model;

import de.theredend2000.advancedhunt.Main;
import java.util.ArrayList;
import java.util.List;

public class TreasureRewardHolder implements RewardHolder {
   private final Main plugin;
   private Treasure treasure;

   public TreasureRewardHolder(Main plugin, Treasure treasure) {
      this.plugin = plugin;
      this.treasure = treasure;
   }

   @Override
   public List<Reward> getRewards() {
      return this.treasure.getRewards() != null ? new ArrayList<>(this.treasure.getRewards()) : new ArrayList<>();
   }

   @Override
   public void saveRewards(List<Reward> rewards) {
      Treasure newTreasure = new Treasure(
         this.treasure.getId(),
         this.treasure.getCollectionId(),
         this.treasure.getLocation(),
         new ArrayList<>(rewards),
         this.treasure.getNbtData(),
         this.treasure.getMaterial(),
         this.treasure.getBlockState()
      );
      this.plugin.getTreasureManager().updateTreasure(this.treasure, newTreasure);
      this.treasure = newTreasure;
   }

   public Treasure getTreasure() {
      return this.treasure;
   }

   @Override
   public String getRewardsTitleKey() {
      return "gui.rewards.title";
   }
}
