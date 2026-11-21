package net.coreprotect.database.rollback;

import java.io.ByteArrayInputStream;
import java.util.List;
import net.coreprotect.config.ConfigHandler;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;

public class RollbackItemHandler {
   public static Object[] populateItemStack(ItemStack itemstack, byte[] metadata) {
      if (metadata != null) {
         try {
            ByteArrayInputStream metaByteStream = new ByteArrayInputStream(metadata);
            BukkitObjectInputStream metaObjectStream = new BukkitObjectInputStream(metaByteStream);
            Object metaList = metaObjectStream.readObject();
            metaObjectStream.close();
            metaByteStream.close();
            return RollbackUtil.populateItemStack(itemstack, metaList);
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }

      return new Object[]{0, "", itemstack};
   }

   public static void sortContainerItems(PlayerInventory inventory, List<Integer> slots) {
      if (slots.contains(0)) {
         ItemStack boots = inventory.getBoots();
         if (boots != null && !boots.getType().equals(Material.AIR) && !boots.getType().name().contains("BOOTS")) {
            inventory.setBoots(new ItemStack(Material.AIR));
            inventory.addItem(new ItemStack[]{boots});
         }
      }

      if (slots.contains(1)) {
         ItemStack leggings = inventory.getLeggings();
         if (leggings != null && !leggings.getType().equals(Material.AIR) && !leggings.getType().name().contains("LEGGINGS")) {
            inventory.setLeggings(new ItemStack(Material.AIR));
            inventory.addItem(new ItemStack[]{leggings});
         }
      }

      if (slots.contains(2)) {
         ItemStack chestplate = inventory.getChestplate();
         if (chestplate != null && !chestplate.getType().equals(Material.AIR) && !chestplate.getType().name().contains("CHESTPLATE")) {
            inventory.setChestplate(new ItemStack(Material.AIR));
            inventory.addItem(new ItemStack[]{chestplate});
         }
      }

      if (slots.contains(3)) {
         ItemStack helmet = inventory.getHelmet();
         if (helmet != null && !helmet.getType().equals(Material.AIR)) {
            String materialName = helmet.getType().name();
            if (!materialName.contains("HELMET") && !materialName.contains("SKULL") && !materialName.endsWith("_HEAD")) {
               inventory.setHelmet(new ItemStack(Material.AIR));
               inventory.addItem(new ItemStack[]{helmet});
            }
         }
      }
   }

   public static void updateItemCount(String userString, int increment) {
      int[] rollbackHashData = ConfigHandler.rollbackHash.get(userString);
      int itemCount = rollbackHashData[0];
      int blockCount = rollbackHashData[1];
      int entityCount = rollbackHashData[2];
      int scannedWorlds = rollbackHashData[4];
      itemCount += increment;
      ConfigHandler.rollbackHash.put(userString, new int[]{itemCount, blockCount, entityCount, 0, scannedWorlds});
   }
}
