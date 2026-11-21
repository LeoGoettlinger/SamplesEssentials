package de.mistrx.buildpaste.items;

import de.mistrx.buildpaste.firebase.Firebase;
import de.mistrx.buildpaste.util.Debug;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PastePaperItem {
   public static ItemStack GetPastePaper(String buildId) {
      ItemStack item = new ItemStack(Material.PAPER);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName("(" + buildId + ")");
      item.setItemMeta(meta);
      return item;
   }

   public static ItemStack GetPastePaper(String name, String buildId) {
      String buildName = name;
      if (name.equals("") || name == null) {
         buildName = Firebase.getBuildName(buildId);
      }

      ItemStack item = new ItemStack(Material.PAPER);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(buildName + " " + "(" + buildId + ")");
      item.setItemMeta(meta);
      return item;
   }

   public static String GetPastePaperBuildId(ItemStack item) {
      if (!item.getType().equals(Material.PAPER)) {
         return null;
      } else {
         String name = item.getItemMeta().getDisplayName();
         if (name.contains("(") && name.contains(")")) {
            String id = name.substring(name.lastIndexOf("(") + 1, name.lastIndexOf(")"));
            Debug.Warning(id);
            return id;
         } else {
            return null;
         }
      }
   }

   public static boolean IsPastePaper(ItemStack item) {
      return GetPastePaperBuildId(item) != null;
   }
}
