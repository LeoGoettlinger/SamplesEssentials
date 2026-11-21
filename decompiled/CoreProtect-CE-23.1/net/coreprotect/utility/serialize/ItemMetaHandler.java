package net.coreprotect.utility.serialize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.ItemUtils;
import net.coreprotect.utility.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;

public class ItemMetaHandler {
   public static String getEnchantmentName(Enchantment enchantment, int level) {
      String name = enchantment.getKey().getKey();
      switch (name) {
         case "vanishing_curse":
            name = "Curse of Vanishing";
            break;
         case "binding_curse":
            name = "Curse of Binding";
            break;
         default:
            name = StringUtils.capitalize(name.replace("_", " "), true);
      }

      if (enchantment.getMaxLevel() > 1) {
         name = name + " " + getEnchantmentLevel(level);
      }

      return name;
   }

   private static String getEnchantmentLevel(int level) {
      switch (level) {
         case 1:
            return "I";
         case 2:
            return "II";
         case 3:
            return "III";
         case 4:
            return "IV";
         case 5:
            return "V";
         case 6:
            return "VI";
         case 7:
            return "VII";
         case 8:
            return "VIII";
         case 9:
            return "IX";
         case 10:
            return "X";
         default:
            return Integer.toString(level);
      }
   }

   private static Map<Enchantment, Integer> getEnchantments(ItemMeta itemMeta) {
      if (itemMeta == null) {
         return null;
      } else if (itemMeta instanceof EnchantmentStorageMeta) {
         EnchantmentStorageMeta enchantmentStorageEngine = (EnchantmentStorageMeta)itemMeta;
         return enchantmentStorageEngine.getStoredEnchants();
      } else {
         return itemMeta.getEnchants();
      }
   }

   public static List<String> getEnchantments(ItemStack item, String displayName) {
      List<String> result = new ArrayList<>();
      ItemMeta itemMeta = item.getItemMeta();
      Map<Enchantment, Integer> enchantments = getEnchantments(itemMeta);

      for (Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
         Enchantment enchantment = entry.getKey();
         Integer level = entry.getValue();
         result.add(getEnchantmentName(enchantment, level));
      }

      if (itemMeta.hasLore()) {
         for (String lore : itemMeta.getLore()) {
            result.add(Color.DARK_PURPLE + "§o" + lore);
         }
      }

      return result;
   }

   public static List<List<Map<String, Object>>> serialize(ItemStack item, Material type, String faceData, int slot) {
      List<List<Map<String, Object>>> metadata = new ArrayList<>();
      List<Map<String, Object>> list = new ArrayList<>();
      List<Object> modifiers = new ArrayList<>();
      if (item != null && item.hasItemMeta() && item.getItemMeta() != null) {
         ItemMeta itemMeta = item.getItemMeta().clone();
         if (itemMeta.hasAttributeModifiers()) {
            for (Entry<Attribute, AttributeModifier> entry : itemMeta.getAttributeModifiers().entries()) {
               Map<Object, Map<String, Object>> attributeList = new HashMap<>();
               Attribute attribute = entry.getKey();
               AttributeModifier modifier = entry.getValue();
               itemMeta.removeAttributeModifier(attribute, modifier);
               attributeList.put(BukkitAdapter.ADAPTER.getRegistryKey(attribute), modifier.serialize());
               modifiers.add(attributeList);
            }
         }

         if (itemMeta instanceof LeatherArmorMeta) {
            LeatherArmorMeta meta = (LeatherArmorMeta)itemMeta;
            LeatherArmorMeta subMeta = meta.clone();
            meta.setColor(Bukkit.getServer().getItemFactory().getDefaultLeatherColor());
            list.add(meta.serialize());
            metadata.add(list);
            list = new ArrayList<>();
            list.add(subMeta.getColor().serialize());
            metadata.add(list);
         } else if (itemMeta instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta)itemMeta;
            PotionMeta subMeta = meta.clone();
            meta.setColor(null);
            meta.clearCustomEffects();
            list.add(meta.serialize());
            if (subMeta.hasColor()) {
               list.add(subMeta.getColor().serialize());
            }

            metadata.add(list);
            if (subMeta.hasCustomEffects()) {
               for (PotionEffect effect : subMeta.getCustomEffects()) {
                  List<Map<String, Object>> var14 = new ArrayList();
                  var14.add(effect.serialize());
                  metadata.add(var14);
               }
            }
         } else if (itemMeta instanceof FireworkMeta) {
            FireworkMeta metax = (FireworkMeta)itemMeta;
            FireworkMeta subMetax = metax.clone();
            metax.clearEffects();
            list.add(metax.serialize());
            metadata.add(list);
            if (subMetax.hasEffects()) {
               for (FireworkEffect effect : subMetax.getEffects()) {
                  deserializeFireworkEffect(effect, metadata);
               }
            }
         } else if (itemMeta instanceof FireworkEffectMeta) {
            FireworkEffectMeta metax = (FireworkEffectMeta)itemMeta;
            FireworkEffectMeta subMetax = metax.clone();
            metax.setEffect(null);
            list.add(metax.serialize());
            metadata.add(list);
            if (subMetax.hasEffect()) {
               FireworkEffect effect = subMetax.getEffect();
               deserializeFireworkEffect(effect, metadata);
            }
         } else if (itemMeta instanceof BannerMeta) {
            BannerMeta metax = (BannerMeta)itemMeta;
            BannerMeta subMetax = (BannerMeta)metax.clone();
            metax.setPatterns(new ArrayList());
            list.add(metax.serialize());
            metadata.add(list);

            for (Pattern pattern : subMetax.getPatterns()) {
               List<Map<String, Object>> var15 = new ArrayList();
               var15.add(pattern.serialize());
               metadata.add(var15);
            }
         } else if (itemMeta instanceof CrossbowMeta) {
            CrossbowMeta metax = (CrossbowMeta)itemMeta;
            CrossbowMeta subMetax = (CrossbowMeta)metax.clone();
            metax.setChargedProjectiles(null);
            list.add(metax.serialize());
            metadata.add(list);
            if (subMetax.hasChargedProjectiles()) {
               List<Map<String, Object>> var16 = new ArrayList();

               for (ItemStack chargedProjectile : subMetax.getChargedProjectiles()) {
                  Map<String, Object> itemMap = ItemUtils.serializeItemStack(chargedProjectile, null, slot);
                  if (itemMap.size() > 0) {
                     var16.add(itemMap);
                  }
               }

               metadata.add(var16);
            }
         } else if (itemMeta instanceof MapMeta) {
            MapMeta metax = (MapMeta)itemMeta;
            MapMeta subMetax = metax.clone();
            metax.setColor(null);
            list.add(metax.serialize());
            metadata.add(list);
            if (subMetax.hasColor()) {
               List<Map<String, Object>> var17 = new ArrayList();
               var17.add(subMetax.getColor().serialize());
               metadata.add(var17);
            }
         } else if (itemMeta instanceof SuspiciousStewMeta) {
            SuspiciousStewMeta metax = (SuspiciousStewMeta)itemMeta;
            SuspiciousStewMeta subMetax = metax.clone();
            metax.clearCustomEffects();
            list.add(metax.serialize());
            metadata.add(list);
            if (subMetax.hasCustomEffects()) {
               for (PotionEffect effect : subMetax.getCustomEffects()) {
                  List<Map<String, Object>> var18 = new ArrayList();
                  var18.add(effect.serialize());
                  metadata.add(var18);
               }
            }
         } else if (!BukkitAdapter.ADAPTER.getItemMeta(itemMeta, list, metadata, slot)) {
            list.add(itemMeta.serialize());
            metadata.add(list);
         }
      }

      if (type != null && type.equals(Material.ARMOR_STAND)) {
         Map<String, Object> metax = new HashMap<>();
         metax.put("slot", slot);
         List<Map<String, Object>> var19 = new ArrayList();
         var19.add(metax);
         metadata.add(var19);
      }

      if (faceData != null && faceData.length() > 0) {
         Map<String, Object> metax = new HashMap<>();
         metax.put("facing", faceData);
         List<Map<String, Object>> var20 = new ArrayList();
         var20.add(metax);
         metadata.add(var20);
      }

      if (modifiers.size() > 0) {
         Map<String, Object> metax = new HashMap<>();
         metax.put("modifiers", modifiers);
         List<Map<String, Object>> var21 = new ArrayList();
         var21.add(metax);
         metadata.add(var21);
      }

      return metadata;
   }

   private static void deserializeFireworkEffect(FireworkEffect effect, List<List<Map<String, Object>>> metadata) {
      List<Map<String, Object>> colorList = new ArrayList<>();
      List<Map<String, Object>> fadeList = new ArrayList<>();
      List<Map<String, Object>> list = new ArrayList<>();

      for (org.bukkit.Color color : effect.getColors()) {
         colorList.add(color.serialize());
      }

      for (org.bukkit.Color color : effect.getFadeColors()) {
         fadeList.add(color.serialize());
      }

      Map<String, Object> hasCheck = new HashMap<>();
      hasCheck.put("type", effect.getType());
      hasCheck.put("flicker", effect.hasFlicker());
      hasCheck.put("trail", effect.hasTrail());
      list.add(hasCheck);
      metadata.add(list);
      metadata.add(colorList);
      metadata.add(fadeList);
   }
}
