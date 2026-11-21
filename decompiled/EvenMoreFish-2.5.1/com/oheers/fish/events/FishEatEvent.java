package com.oheers.fish.events;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.fishing.items.FishManager;
import java.util.List;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class FishEatEvent {
   private static final FishEatEvent eatEvent = new FishEatEvent();

   private FishEatEvent() {
   }

   public static FishEatEvent getInstance() {
      return eatEvent;
   }

   public boolean checkEatEvent(PlayerItemConsumeEvent event) {
      ItemStack item = event.getItem();
      if (FishManager.getInstance().isFish(item)) {
         IFish fish = FishManager.getInstance().getFish(item);
         if (fish == null) {
            return false;
         }

         List<Reward> rewards = fish.getEatRewards();
         if (!rewards.isEmpty()) {
            rewards.forEach(r -> r.give(event.getPlayer(), event.getPlayer().getLocation()));
            return true;
         }
      }

      return false;
   }
}
