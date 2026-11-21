package com.oheers.fish.addons.internal.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.config.serializer.SoundSerializer;
import com.oheers.fish.api.reward.RewardType;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Emitter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class SoundRewardType extends RewardType {
   @Override
   public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
      Sound sound = SoundSerializer.get().deserialize(value);
      player.playSound(sound, Emitter.self());
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "SOUND";
   }

   @NonNull
   @Override
   public String getAuthor() {
      return "FireML";
   }

   @NonNull
   @Override
   public Plugin getPlugin() {
      return EvenMoreFish.getInstance();
   }
}
