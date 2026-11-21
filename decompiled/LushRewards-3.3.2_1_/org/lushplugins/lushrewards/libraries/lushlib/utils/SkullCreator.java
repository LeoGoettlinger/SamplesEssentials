package org.lushplugins.lushrewards.libraries.lushlib.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.lushplugins.lushrewards.libraries.lushlib.utils.skullcreator.LegacySkullCreator;
import org.lushplugins.lushrewards.libraries.lushlib.utils.skullcreator.NewSkullCreator;

public class SkullCreator {
   private static final SkullCreator.Interface skullCreator;

   public static ItemStack getCustomSkull(String texture) {
      return skullCreator.getCustomSkull(texture);
   }

   public static void mutateItemMeta(SkullMeta meta, String b64) {
      skullCreator.mutateItemMeta(meta, b64);
   }

   public static String getB64(ItemStack itemStack) {
      return skullCreator.getB64(itemStack);
   }

   public static String getTexture(OfflinePlayer player) {
      return skullCreator.getTexture(player);
   }

   static {
      String version = Bukkit.getBukkitVersion();
      if (!version.contains("1.16") && !version.contains("1.17") && !version.contains("1.18")) {
         skullCreator = new NewSkullCreator();
      } else {
         skullCreator = new LegacySkullCreator();
      }
   }

   public interface Interface {
      ItemStack getCustomSkull(String var1);

      void mutateItemMeta(SkullMeta var1, String var2);

      String getB64(ItemStack var1);

      String getTexture(OfflinePlayer var1);
   }
}
