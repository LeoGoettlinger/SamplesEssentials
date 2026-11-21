package com.oheers.fish.addons.internal.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import com.oheers.fish.api.utils.Scheduling;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class CommandRewardType extends RewardType {
   @Override
   public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
      String inputCommand = value.replace("{player}", player.getName());
      if (EvenMoreFish.getInstance().getDependencyManager().isUsingPAPI()) {
         inputCommand = PlaceholderAPI.setPlaceholders(player, inputCommand);
      }

      if (hookLocation != null) {
         String worldName = hookLocation.getWorld() == null ? "N/A" : hookLocation.getWorld().getName();
         inputCommand = inputCommand.replace("{x}", Double.toString(hookLocation.getX()))
            .replace("{y}", Double.toString(hookLocation.getY()))
            .replace("{z}", Double.toString(hookLocation.getZ()))
            .replace("{world}", worldName);
      }

      String finalCommand = inputCommand;
      Scheduling.getInstance().runTask(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand));
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "COMMAND";
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
