package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import java.util.List;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.evenmorefish.fish.libs.daisylib.external.vault.VaultWrapper;
import org.jspecify.annotations.NonNull;

public class GroupRequirementType extends RequirementType {
   @Override
   public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
      Player player = context.getPlayer();
      Permission permission = VaultWrapper.get().getPermissionOrNull();
      if (player != null && permission != null) {
         for (String value : values) {
            if (permission.playerInGroup(player, value)) {
               this.debugLogStatus(true, value);
               return true;
            }
         }

         this.debugLogStatus(false, null);
         return false;
      } else {
         return false;
      }
   }

   @NonNull
   @Override
   public String getIdentifier() {
      return "GROUP";
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
