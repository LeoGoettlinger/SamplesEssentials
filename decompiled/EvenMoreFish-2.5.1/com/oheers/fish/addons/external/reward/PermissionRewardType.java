package com.oheers.fish.addons.external.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.evenmorefish.fish.libs.daisylib.external.vault.VaultWrapper;
import org.jspecify.annotations.NonNull;

public class PermissionRewardType extends RewardType {
   @Override
   public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
      Permission permission = VaultWrapper.get().getPermissionOrNull();
      if (permission != null) {
         permission.playerAdd(player.getPlayer(), value);
      }
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "PERMISSION";
   }

   @NonNull
   @Override
   public String getAuthor() {
      return "FireML";
   }

   @NonNull
   public JavaPlugin getPlugin() {
      return EvenMoreFish.getInstance();
   }
}
