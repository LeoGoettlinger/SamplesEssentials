package de.theredend2000.advancedhunt.model;

import de.theredend2000.advancedhunt.Main;
import java.util.ArrayList;
import java.util.List;

public class CollectionRewardHolder implements RewardHolder {
   private final Main plugin;
   private final Collection collection;

   public CollectionRewardHolder(Main plugin, Collection collection) {
      this.plugin = plugin;
      this.collection = collection;
   }

   @Override
   public List<Reward> getRewards() {
      return this.collection.getCompletionRewards() != null ? new ArrayList<>(this.collection.getCompletionRewards()) : new ArrayList<>();
   }

   @Override
   public void saveRewards(List<Reward> rewards) {
      this.collection.setCompletionRewards(new ArrayList<>(rewards));
      this.plugin.getCollectionManager().saveCollection(this.collection);
   }

   @Override
   public String getRewardsTitleKey() {
      return "gui.rewards.collection_title";
   }

   public Collection getCollection() {
      return this.collection;
   }
}
