package com.magmaguy.elitemobs.items.upgradesystem;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.items.itemconstructor.EnchantmentGenerator;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentItems;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class UpgradeSystem {
   private UpgradeSystem() {
   }

   public static UpgradeSystem.Preview preview(ItemStack source, ItemStack book) {
      if (source != null && source.getAmount() >= 1 && ItemTagger.isEliteItem(source)) {
         Map<String, Integer> additions = EliteEnchantmentItems.readBook(book);
         Map<String, Integer> custom = EliteEnchantmentItems.custom(source);
         HashMap<Enchantment, Integer> natives = new HashMap<>(EliteEnchantmentItems.nativeLevels(source));

         for (Entry<String, Integer> entry : additions.entrySet()) {
            if (entry.getKey().startsWith("minecraft:")) {
               Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(entry.getKey()));
               if (enchantment == null) {
                  throw new IllegalArgumentException("Unknown native enchantment");
               }

               if (WeaponIdentityResolver.isMagicWeapon(source) && !EnchantmentGenerator.supportedMagicEnchantments().contains(enchantment)) {
                  throw new IllegalArgumentException("Native enchantment is incompatible with this magic weapon");
               }

               natives.merge(enchantment, entry.getValue(), UpgradeSystem::addLevels);
            } else {
               custom.merge(entry.getKey(), entry.getValue(), UpgradeSystem::addLevels);
            }
         }

         for (Entry<Enchantment, Integer> entryx : natives.entrySet()) {
            EnchantmentsConfigFields config = EnchantmentsConfig.getEnchantment(entryx.getKey());
            if (config == null || !config.isEnabled()) {
               throw new IllegalArgumentException("Native upgrade is disabled or unavailable");
            }

            if (entryx.getValue() > config.getMaxEnchantmentLevel()) {
               throw new EnchantmentItems.LimitExceededException("Native upgrade exceeds EM's configured limit");
            }
         }

         ItemStack nativeDraft = source.clone();
         nativeDraft.setAmount(1);
         ItemMeta meta = nativeDraft.getItemMeta();
         EnchantmentGenerator.generateEnchantments(meta, natives);
         ItemTagger.registerEnchantments(meta, natives);
         nativeDraft.setItemMeta(meta);
         EnchantmentItems.Preview shared = EliteEnchantmentItems.ITEMS.previewCustom(nativeDraft, custom);
         ItemStack rendered = shared.previewItem();
         new EliteItemLore(rendered, false);
         return new UpgradeSystem.Preview(source.clone(), book.clone(), nativeDraft, shared, rendered);
      } else {
         throw new IllegalArgumentException("An eligible elite target is required");
      }
   }

   public static ItemStack upgrade(ItemStack source, ItemStack book) {
      return preview(source, book).apply(source, book);
   }

   private static int addLevels(int first, int second) {
      try {
         return Math.addExact(first, second);
      } catch (ArithmeticException var3) {
         throw new EnchantmentItems.LimitExceededException("Enchantment level exceeds the supported integer range");
      }
   }

   public static final class Preview {
      private final ItemStack source;
      private final ItemStack book;
      private final ItemStack nativeDraft;
      private final ItemStack result;
      private final EnchantmentItems.Preview shared;

      private Preview(ItemStack source, ItemStack book, ItemStack nativeDraft, EnchantmentItems.Preview shared, ItemStack result) {
         this.source = source;
         this.book = book;
         this.nativeDraft = nativeDraft;
         this.shared = shared;
         this.result = result;
      }

      public ItemStack apply(ItemStack currentSource, ItemStack currentBook) {
         if (this.source.equals(currentSource) && this.book.equals(currentBook)) {
            this.shared.apply(this.nativeDraft);
            return this.result.clone();
         } else {
            throw new ConcurrentModificationException("Enchantment inputs changed after preview");
         }
      }
   }
}
