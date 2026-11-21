package com.amethysttools;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class AmethystToolService {
   private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
   private final AmethystToolsPlugin plugin;
   private final NamespacedKey toolKey;
   private final NamespacedKey expiresAtKey;
   private final NamespacedKey itemTypeKey;
   private final Set<Material> blockedMaterials = new HashSet<>();

   public AmethystToolService(AmethystToolsPlugin plugin, NamespacedKey toolKey, NamespacedKey expiresAtKey, NamespacedKey itemTypeKey) {
      this.plugin = plugin;
      this.toolKey = toolKey;
      this.expiresAtKey = expiresAtKey;
      this.itemTypeKey = itemTypeKey;
      this.reloadBlockedMaterials();
   }

   public void reloadBlockedMaterials() {
      this.blockedMaterials.clear();

      for (String name : this.plugin.getConfig().getStringList("blocked-materials")) {
         Material material = Material.matchMaterial(name);
         if (material != null) {
            this.blockedMaterials.add(material);
         } else {
            this.plugin.getLogger().warning("Unknown material in blocked-materials: " + name);
         }
      }
   }

   public ItemStack createItem(String itemKey, int amount) {
      ConfigurationSection itemSection = this.getItemSection(itemKey);
      String materialName = itemSection != null ? itemSection.getString("material", "NETHERITE_PICKAXE") : "NETHERITE_PICKAXE";
      Material material = Material.matchMaterial(materialName);
      if (material == null) {
         material = Material.NETHERITE_PICKAXE;
      }

      ItemStack item = new ItemStack(material, Math.max(1, amount));
      ItemMeta meta = item.getItemMeta();
      if (meta == null) {
         return item;
      } else {
         long expiresAt = Instant.now().plus(Duration.ofDays(this.getExpiryDays())).toEpochMilli();
         meta.displayName(
            this.color(itemSection != null ? itemSection.getString("name", "&dAmethyst Pickaxe") : "&dAmethyst Pickaxe")
               .decoration(TextDecoration.ITALIC, false)
         );
         if (itemSection != null && itemSection.getBoolean("unbreakable", false)) {
            meta.setUnbreakable(true);
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_UNBREAKABLE});
         }

         boolean hasConfiguredEnchants = this.applyConfiguredEnchants(meta, itemSection, itemKey);
         if (!hasConfiguredEnchants && itemSection != null && itemSection.getBoolean("glow", true)) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
         }

         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         pdc.set(this.toolKey, PersistentDataType.BYTE, (byte)1);
         pdc.set(this.expiresAtKey, PersistentDataType.LONG, expiresAt);
         pdc.set(this.itemTypeKey, PersistentDataType.STRING, itemKey);
         meta.lore(this.buildLore(expiresAt, itemSection));
         item.setItemMeta(meta);
         return item;
      }
   }

   public String resolveItemKey(String inputKey) {
      String normalizedInput = this.normalizeKey(inputKey != null && !inputKey.isBlank() ? inputKey : "amethyst_pickaxe");
      ConfigurationSection itemsSection = this.plugin.getConfig().getConfigurationSection("items");
      if (itemsSection == null) {
         if (!this.plugin.getConfig().isConfigurationSection("item")) {
            return null;
         } else {
            return !normalizedInput.equals("pickaxe") && !normalizedInput.equals("amethystpickaxe") ? null : "pickaxe";
         }
      } else {
         for (String key : itemsSection.getKeys(false)) {
            if (this.normalizeKey(key).equals(normalizedInput)) {
               return key;
            }

            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection != null) {
               for (String alias : itemSection.getStringList("aliases")) {
                  if (this.normalizeKey(alias).equals(normalizedInput)) {
                     return key;
                  }
               }
            }
         }

         return null;
      }
   }

   public List<String> getItemInputOptions() {
      Set<String> options = new LinkedHashSet<>();
      ConfigurationSection itemsSection = this.plugin.getConfig().getConfigurationSection("items");
      if (itemsSection != null) {
         for (String key : itemsSection.getKeys(false)) {
            options.add(key.toLowerCase(Locale.ROOT));
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection != null) {
               for (String alias : itemSection.getStringList("aliases")) {
                  if (alias != null && !alias.isBlank()) {
                     options.add(alias.toLowerCase(Locale.ROOT));
                  }
               }
            }
         }
      }

      if (options.isEmpty() && this.plugin.getConfig().isConfigurationSection("item")) {
         options.add("pickaxe");
         options.add("amethystpickaxe");
         options.add("amethyst_pickaxe");
      }

      return new ArrayList<>(options);
   }

   public boolean isAmethystTool(ItemStack item) {
      if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         Byte marker = (Byte)meta.getPersistentDataContainer().get(this.toolKey, PersistentDataType.BYTE);
         return marker != null && marker == 1;
      } else {
         return false;
      }
   }

   public boolean isExpired(ItemStack item) {
      long expiresAt = this.getExpiresAt(item);
      return expiresAt > 0L && Instant.now().toEpochMilli() >= expiresAt;
   }

   public String getToolItemKey(ItemStack item) {
      if (!this.isAmethystTool(item)) {
         return null;
      } else {
         ItemMeta meta = item.getItemMeta();
         if (meta == null) {
            return null;
         } else {
            String stored = (String)meta.getPersistentDataContainer().get(this.itemTypeKey, PersistentDataType.STRING);
            return stored != null && !stored.isBlank() ? stored : this.resolveItemKey("amethyst_pickaxe");
         }
      }
   }

   public boolean isToolItem(ItemStack item, String expectedKey) {
      String actualKey = this.getToolItemKey(item);
      if (actualKey != null && expectedKey != null && !expectedKey.isBlank()) {
         String resolvedExpected = this.resolveItemKey(expectedKey);
         if (resolvedExpected == null) {
            resolvedExpected = expectedKey;
         }

         return this.normalizeKey(actualKey).equals(this.normalizeKey(resolvedExpected));
      } else {
         return false;
      }
   }

   public long getExpiresAt(ItemStack item) {
      if (!this.isAmethystTool(item)) {
         return -1L;
      } else {
         ItemMeta meta = item.getItemMeta();
         Long expires = (Long)meta.getPersistentDataContainer().get(this.expiresAtKey, PersistentDataType.LONG);
         return expires == null ? -1L : expires;
      }
   }

   public ItemStack updateOrExpire(ItemStack item, Player owner) {
      if (!this.isAmethystTool(item)) {
         return item;
      } else if (this.isExpired(item)) {
         return null;
      } else {
         ItemMeta meta = item.getItemMeta();
         if (meta == null) {
            return item;
         } else {
            meta.lore(this.buildLore(this.getExpiresAt(item), null));
            item.setItemMeta(meta);
            return item;
         }
      }
   }

   public boolean isBlocked(Material material) {
      return this.blockedMaterials.contains(material);
   }

   public int getRadius() {
      return Math.max(0, this.plugin.getConfig().getInt("mining.radius", 1));
   }

   public int getDepth() {
      return Math.max(1, this.plugin.getConfig().getInt("mining.depth", 1));
   }

   public long getExpiryDays() {
      return Math.max(1L, this.plugin.getConfig().getLong("expiry.days", 3L));
   }

   public String msg(String key) {
      String raw = this.plugin.getMessagesConfig().getString("messages." + key, key);
      return LEGACY.serialize(this.color(raw));
   }

   private boolean applyConfiguredEnchants(ItemMeta meta, ConfigurationSection itemSection, String itemKey) {
      if (itemSection == null) {
         return false;
      } else {
         ConfigurationSection enchants = itemSection.getConfigurationSection("enchantments");
         if (enchants == null) {
            return false;
         } else {
            boolean addedAny = false;

            for (String enchantName : enchants.getKeys(false)) {
               Enchantment enchantment = this.resolveEnchantment(enchantName);
               if (enchantment == null) {
                  this.plugin.getLogger().warning("Unknown enchantment in items." + itemKey + ".enchantments: " + enchantName);
               } else {
                  int level = Math.max(1, enchants.getInt(enchantName, 1));
                  meta.addEnchant(enchantment, level, true);
                  addedAny = true;
               }
            }

            return addedAny;
         }
      }
   }

   private Enchantment resolveEnchantment(String enchantName) {
      String normalized = enchantName.toUpperCase(Locale.ROOT);
      if (normalized.equals("SILKTOUCH")) {
         normalized = "SILK_TOUCH";
      }

      return Enchantment.getByName(normalized);
   }

   private List<Component> buildLore(long expiresAt, ConfigurationSection itemSection) {
      List<Component> lore = new ArrayList<>();
      List<String> configured = itemSection != null ? itemSection.getStringList("lore") : this.plugin.getConfig().getStringList("item.lore");
      if (configured.isEmpty()) {
         configured = List.of("&7Expires in: &e%time%");
      }

      String remaining = this.formatRemaining(expiresAt);

      for (String line : configured) {
         lore.add(this.color(line.replace("%time%", remaining)));
      }

      return lore;
   }

   private ConfigurationSection getItemSection(String itemKey) {
      ConfigurationSection itemsSection = this.plugin.getConfig().getConfigurationSection("items");
      if (itemsSection != null) {
         ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemKey);
         if (itemSection != null) {
            return itemSection;
         } else {
            String resolved = this.resolveItemKey(itemKey);
            return resolved != null ? itemsSection.getConfigurationSection(resolved) : null;
         }
      } else {
         return this.plugin.getConfig().getConfigurationSection("item");
      }
   }

   private String normalizeKey(String input) {
      return input.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
   }

   private String formatRemaining(long expiresAt) {
      long millisLeft = Math.max(0L, expiresAt - Instant.now().toEpochMilli());
      Duration duration = Duration.ofMillis(millisLeft);
      long days = duration.toDays();
      long hours = duration.minusDays(days).toHours();
      long minutes = duration.minusDays(days).minusHours(hours).toMinutes();
      return String.format(Locale.ROOT, "%dd %dh %dm", days, hours, minutes);
   }

   private Component color(String input) {
      return LEGACY.deserialize(input);
   }
}
