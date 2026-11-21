package com.skyblockexp.ezrtp.pvptag;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

public final class PvpTagService {
   private final List<PvpTagProvider> providers = new ArrayList<>();

   public void registerProvider(PvpTagProvider provider) {
      this.providers.add(provider);
   }

   public boolean isEnabled() {
      return !this.providers.isEmpty();
   }

   public boolean isInCombat(Player player) {
      if (player == null) {
         return false;
      } else {
         for (PvpTagProvider provider : this.providers) {
            if (provider.isAvailable() && provider.isInCombat(player)) {
               return true;
            }
         }

         return false;
      }
   }
}
