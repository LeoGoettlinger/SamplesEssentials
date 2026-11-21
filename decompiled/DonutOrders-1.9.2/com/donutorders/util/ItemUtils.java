package com.donutorders.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public final class ItemUtils {
   private static final ItemFlag[] ALL_FLAGS = ItemFlag.values();

   private ItemUtils() {
   }

   public static ItemStack createGuiItem(Material material, String name, List<String> lore) {
      ItemStack item = new ItemStack(material, 1);
      ItemMeta meta = item.getItemMeta();
      if (meta == null) {
         return item;
      } else {
         meta.setDisplayName(name);
         if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
         }

         meta.addItemFlags(ALL_FLAGS);
         item.setItemMeta(meta);
         return item;
      }
   }

   public static ItemStack filler(Material material) {
      return createGuiItem(material, " ", null);
   }

   public static ItemStack cleanItem(ItemStack item) {
      if (item != null && item.getType() != Material.AIR) {
         ItemMeta meta = item.getItemMeta();
         if (meta == null) {
            return item;
         } else {
            meta.addItemFlags(ALL_FLAGS);
            item.setItemMeta(meta);
            return item;
         }
      } else {
         return item;
      }
   }

   public static ItemStack cleanedCopy(ItemStack item) {
      if (item == null) {
         return null;
      } else {
         ItemStack copy = item.clone();
         return cleanItem(copy);
      }
   }

   public static boolean isSameMaterial(ItemStack a, ItemStack b) {
      return a != null && b != null ? a.getType() == b.getType() : false;
   }

   public static String serializeItem(ItemStack item) {
      if (item != null && item.getType() != Material.AIR) {
         try {
            String var3;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
               BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos);

               try {
                  boos.writeObject(item);
                  var3 = Base64.getEncoder().encodeToString(baos.toByteArray());
               } catch (Throwable var7) {
                  try {
                     boos.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }

                  throw var7;
               }

               boos.close();
            }

            return var3;
         } catch (IOException var9) {
            return "";
         }
      } else {
         return "";
      }
   }

   public static ItemStack deserializeItem(String data) {
      if (data != null && !data.isEmpty()) {
         try {
            ItemStack var3;
            try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data))) {
               BukkitObjectInputStream bois = new BukkitObjectInputStream(bais);

               try {
                  var3 = (ItemStack)bois.readObject();
               } catch (Throwable var7) {
                  try {
                     bois.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }

                  throw var7;
               }

               bois.close();
            }

            return var3;
         } catch (ClassNotFoundException | IOException var9) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static String serializeItemArray(ItemStack[] items) {
      try {
         String var12;
         try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos);

            try {
               boos.writeInt(items.length);

               for (ItemStack item : items) {
                  boos.writeObject(item);
               }

               var12 = Base64.getEncoder().encodeToString(baos.toByteArray());
            } catch (Throwable var9) {
               try {
                  boos.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }

               throw var9;
            }

            boos.close();
         }

         return var12;
      } catch (IOException var11) {
         return "";
      }
   }

   public static ItemStack[] deserializeItemArray(String data, int size) {
      if (data != null && !data.isEmpty()) {
         try {
            ItemStack[] var12;
            try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data))) {
               BukkitObjectInputStream bois = new BukkitObjectInputStream(bais);

               try {
                  int length = bois.readInt();
                  ItemStack[] items = new ItemStack[length];

                  for (int i = 0; i < length; i++) {
                     items[i] = (ItemStack)bois.readObject();
                  }

                  var12 = items;
               } catch (Throwable var9) {
                  try {
                     bois.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }

                  throw var9;
               }

               bois.close();
            }

            return var12;
         } catch (ClassNotFoundException | IOException var11) {
            return new ItemStack[size];
         }
      } else {
         return new ItemStack[size];
      }
   }

   public static int countItems(ItemStack[] items) {
      int total = 0;

      for (ItemStack item : items) {
         if (item != null && item.getType() != Material.AIR) {
            total += item.getAmount();
         }
      }

      return total;
   }

   public static int countMatchingItems(ItemStack[] items, ItemStack template) {
      int total = 0;

      for (ItemStack item : items) {
         if (isSameMaterial(item, template) && item != null && item.getType() != Material.AIR) {
            total += item.getAmount();
         }
      }

      return total;
   }

   public static String prettyName(Material material) {
      String raw = material.name().replace('_', ' ').toLowerCase();
      StringBuilder sb = new StringBuilder();
      boolean cap = true;

      for (char c : raw.toCharArray()) {
         sb.append(cap ? Character.toUpperCase(c) : c);
         cap = c == ' ';
      }

      return sb.toString();
   }

   public static String describeOrderItem(ItemStack item) {
      return EnchantOrderUtils.describeOrderItem(item);
   }

   public static void giveOrDrop(Player player, ItemStack item) {
      if (player != null && item != null && item.getType() != Material.AIR) {
         ItemStack toGive = item.clone();
         HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(new ItemStack[]{toGive});
         if (!overflow.isEmpty()) {
            if (player.getWorld() != null) {
               overflow.values().forEach(drop -> player.getWorld().dropItemNaturally(player.getLocation(), drop));
            }
         }
      }
   }

   public static void giveOrDropAll(Player player, ItemStack[] items, boolean clearSource) {
      if (items != null) {
         for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (clearSource) {
               items[i] = null;
            }

            giveOrDrop(player, item);
         }
      }
   }

   public static void giveOrDropAll(Player player, Iterable<ItemStack> items) {
      if (items != null) {
         for (ItemStack item : items) {
            giveOrDrop(player, item);
         }
      }
   }
}
