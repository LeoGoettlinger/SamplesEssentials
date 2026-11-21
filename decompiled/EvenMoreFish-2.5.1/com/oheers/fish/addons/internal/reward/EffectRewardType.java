package com.oheers.fish.addons.internal.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.config.serializer.PotionEffectSerializer;
import com.oheers.fish.api.reward.RewardType;
import com.oheers.fish.api.utils.Scheduling;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.jspecify.annotations.NonNull;

public class EffectRewardType extends RewardType {
   @Override
   public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
      PotionEffect effect = PotionEffectSerializer.get().deserialize(value);
      if (effect == null) {
         EvenMoreFish.getInstance().getLogger().warning("Invalid effect specified for RewardType " + this.getIdentifier() + ": " + value);
      } else {
         Scheduling.getInstance().runTask(player, () -> player.addPotionEffect(effect));
      }
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "EFFECT";
   }

   @NonNull
   @Override
   public String getAuthor() {
      return "Oheers";
   }

   @NonNull
   public JavaPlugin getPlugin() {
      return EvenMoreFish.getInstance();
   }
}
