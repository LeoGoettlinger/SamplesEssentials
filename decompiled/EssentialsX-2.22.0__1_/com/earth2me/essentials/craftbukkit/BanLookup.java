package com.earth2me.essentials.craftbukkit;

import com.earth2me.essentials.User;
import net.ess3.api.IEssentials;
import org.bukkit.BanEntry;
import org.bukkit.BanList.Type;

public final class BanLookup {
   private BanLookup() {
   }

   public static Boolean isBanned(final IEssentials ess, final User user) {
      return isBanned(ess, user.getName());
   }

   public static Boolean isBanned(final IEssentials ess, final String name) {
      return getBanEntry(ess, name) != null;
   }

   public static BanEntry getBanEntry(final IEssentials ess, final String name) {
      for (Object ob : ess.getServer().getBanList(Type.NAME).getBanEntries()) {
         BanEntry banEnt = (BanEntry)ob;
         if (banEnt.getTarget().equals(name)) {
            return banEnt;
         }
      }

      return null;
   }
}
