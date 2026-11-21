package de.theredend2000.advancedhunt.model;

import java.util.List;

public interface RewardHolder {
   List<Reward> getRewards();

   void saveRewards(List<Reward> var1);

   String getRewardsTitleKey();
}
