package cm.chunkManager.gui;

import cm.chunkManager.utils.Scheduler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class GuiUtils {
   private static final Enchantment GLOW_ENCHANT = (Enchantment)Registry.ENCHANTMENT.get(NamespacedKey.minecraft("luck_of_the_sea"));

   private GuiUtils() {
   }

   public static ItemStack item(Material material, String name, String... lore) {
      ItemStack stack = new ItemStack(material);
      ItemMeta meta = stack.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(name);
         if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
         }

         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         stack.setItemMeta(meta);
      }

      return stack;
   }

   public static ItemStack item(Material material, String name, List<String> lore) {
      ItemStack stack = new ItemStack(material);
      ItemMeta meta = stack.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(name);
         if (!lore.isEmpty()) {
            meta.setLore(lore);
         }

         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         stack.setItemMeta(meta);
      }

      return stack;
   }

   public static ItemStack filler() {
      return item(Material.GRAY_STAINED_GLASS_PANE, " ");
   }

   public static ItemStack border() {
      return item(Material.BLACK_STAINED_GLASS_PANE, " ");
   }

   public static ItemStack back() {
      return item(Material.ARROW, ChatColor.YELLOW + "Back");
   }

   public static ItemStack close() {
      return item(Material.BARRIER, ChatColor.RED + "Close");
   }

   public static ItemStack toggle(String name, boolean on, Material icon, String... desc) {
      ItemStack stack = new ItemStack(icon);
      ItemMeta meta = stack.getItemMeta();
      if (meta != null) {
         String status = on ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF";
         meta.setDisplayName(ChatColor.WHITE + name + " " + status);
         List<String> lore = new ArrayList<>();
         lore.add("");

         for (String line : desc) {
            lore.add(ChatColor.GRAY + line);
         }

         lore.add("");
         lore.add(ChatColor.DARK_GRAY + "▶ Click to toggle");
         meta.setLore(lore);
         if (on) {
            if (GLOW_ENCHANT != null) {
               meta.addEnchant(GLOW_ENCHANT, 1, true);
            }

            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         }

         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         stack.setItemMeta(meta);
      }

      return stack;
   }

   public static ItemStack action(String name, Material icon, ChatColor color, String... desc) {
      ItemStack stack = new ItemStack(icon);
      ItemMeta meta = stack.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(color + name);
         List<String> lore = new ArrayList<>();
         lore.add("");

         for (String line : desc) {
            lore.add(ChatColor.GRAY + line);
         }

         lore.add("");
         lore.add(ChatColor.DARK_GRAY + "▶ Click to open");
         meta.setLore(lore);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         stack.setItemMeta(meta);
      }

      return stack;
   }

   public static ItemStack header(String name, String... lore) {
      ItemStack stack = new ItemStack(Material.NETHER_STAR);
      ItemMeta meta = stack.getItemMeta();
      if (meta != null) {
         meta.setDisplayName("" + ChatColor.GOLD + ChatColor.BOLD + name);
         List<String> lines = new ArrayList<>();
         lines.add("");

         for (String line : lore) {
            lines.add(ChatColor.GRAY + line);
         }

         meta.setLore(lines);
         if (GLOW_ENCHANT != null) {
            meta.addEnchant(GLOW_ENCHANT, 1, true);
         }

         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES});
         stack.setItemMeta(meta);
      }

      return stack;
   }

   public static void fill(Inventory inv, int size) {
      ItemStack pane = filler();

      for (int i = 0; i < size; i++) {
         if (inv.getItem(i) == null) {
            inv.setItem(i, pane);
         }
      }
   }

   public static void applyBorder(Inventory inv, int size) {
      ItemStack edge = border();
      int[] borderSlots = new int[]{0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44};

      for (int slot : borderSlots) {
         if (slot < size && inv.getItem(slot) == null) {
            inv.setItem(slot, edge);
         }
      }

      fill(inv, size);
   }

   public static void click(Player player) {
      player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1.0F);
   }

   public static ChatColor perfColor(double value, double good, double warn, double poor) {
      if (value >= good) {
         return ChatColor.GREEN;
      } else if (value >= warn) {
         return ChatColor.YELLOW;
      } else {
         return value >= poor ? ChatColor.GOLD : ChatColor.RED;
      }
   }

   public static ChatColor memColor(double percentage) {
      if (percentage > 80.0) {
         return ChatColor.RED;
      } else {
         return percentage > 60.0 ? ChatColor.YELLOW : ChatColor.GREEN;
      }
   }

   public static boolean isAdvancedEnabled(JavaPlugin plugin) {
      try {
         return plugin.getConfig().getBoolean("advanced-features.enabled", true);
      } catch (Exception var2) {
         return false;
      }
   }

   public static boolean canAccessEntities() {
      return !Scheduler.isFolia();
   }

   public static String title(String name) {
      return ChatColor.DARK_GRAY + "« " + ChatColor.RESET + name + ChatColor.DARK_GRAY + " »";
   }
}
