package com.magmaguy.elitemobs.items.upgradesystem;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import com.magmaguy.elitemobs.items.ItemConsumables;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentDefinition;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentDefinitions;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentItemProfile;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentItems;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.elitemobs.skills.WeaponResolution;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class EliteEnchantmentItems {
   public static final EnchantmentItems ITEMS = new EnchantmentItems(EnchantmentDefinitions::resolve, EliteEnchantmentItems::profile);

   private EliteEnchantmentItems() {
   }

   private static EnchantmentItemProfile profile(ItemStack item) {
      if (WeaponIdentityResolver.resolve(item) instanceof WeaponResolution.InvalidExplicit) {
         throw new IllegalArgumentException("Authored item provider or definition is unavailable");
      } else {
         SkillType skill = WeaponIdentityResolver.progressionSkill(item);
         if (skill == SkillType.WANDS) {
            return new EnchantmentItemProfile(EnchantmentDefinition.ItemType.WAND, Set.of(EnchantmentDefinition.Slot.MAINHAND), Set.of("WAND_MISSILE"));
         } else {
            return skill == SkillType.STAVES
               ? new EnchantmentItemProfile(
                  EnchantmentDefinition.ItemType.STAFF, Set.of(EnchantmentDefinition.Slot.MAINHAND), Set.of("STAFF_FIREBALL", "STAFF_MELEE")
               )
               : EnchantmentItemProfile.vanilla(item);
         }
      }
   }

   public static Map<String, Integer> custom(ItemStack item) {
      LinkedHashMap<String, Integer> result = new LinkedHashMap<>(ITEMS.inspect(item));
      result.keySet().removeIf(id -> id.startsWith("minecraft:"));
      return result;
   }

   public static Map<Enchantment, Integer> nativeLevels(ItemStack item) {
      ItemMeta meta = Objects.requireNonNull(item.getItemMeta(), "item metadata");
      Map<Enchantment, Integer> natives = meta instanceof EnchantmentStorageMeta book ? book.getStoredEnchants() : meta.getEnchants();
      if (natives.keySet().stream().anyMatch(enchantmentx -> !enchantmentx.getKey().getNamespace().equals("minecraft"))) {
         throw new IllegalArgumentException("Unsupported native registry enchantment");
      } else {
         LinkedHashMap<Enchantment, Integer> result = new LinkedHashMap<>();

         for (Enchantment enchantment : Enchantment.values()) {
            int level = ItemTagger.getEnchantment(meta, enchantment.getKey());
            if (level <= 0 && (natives.containsKey(enchantment) || meta.getPersistentDataContainer().has(enchantment.getKey(), PersistentDataType.INTEGER))) {
               throw new IllegalArgumentException("Native enchantment levels must be positive");
            }

            if (level > 0) {
               result.put(enchantment, level);
            }
         }

         return result;
      }
   }

   public static Map<String, Integer> readBook(ItemStack item) {
      if (item != null && item.getType() == Material.ENCHANTED_BOOK && item.getAmount() >= 1) {
         LinkedHashMap<String, Integer> result = new LinkedHashMap<>(custom(item));
         ITEMS.previewCustom(item, result);

         for (Entry<Enchantment, Integer> entry : nativeLevels(item).entrySet()) {
            EnchantmentsConfigFields config = EnchantmentsConfig.getEnchantment(entry.getKey());
            if (config == null || !config.isEnabled()) {
               throw new IllegalArgumentException("Native book enchantment is outside EM's configured policy");
            }

            if (entry.getValue() > config.getMaxEnchantmentLevel()) {
               throw new EnchantmentItems.LimitExceededException("Native book enchantment exceeds EM's configured limit");
            }

            result.put(entry.getKey().getKey().toString(), entry.getValue());
         }

         if (result.isEmpty()) {
            throw new IllegalArgumentException("Book has no transferable enchantments");
         } else {
            return Map.copyOf(result);
         }
      } else {
         throw new IllegalArgumentException("An actual enchanted book is required");
      }
   }

   public static boolean isEliteEnchantmentBook(ItemStack item) {
      try {
         return !readBook(item).isEmpty();
      } catch (IllegalStateException | IllegalArgumentException var2) {
         return false;
      }
   }

   public static boolean isEliteLuckyTicket(ItemStack item) {
      return ItemConsumables.is(item, ItemConsumables.Type.LUCKY_TICKET);
   }
}
