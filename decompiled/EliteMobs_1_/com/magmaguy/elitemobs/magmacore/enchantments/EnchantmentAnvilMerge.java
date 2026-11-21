package com.magmaguy.elitemobs.magmacore.enchantments;

import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public final class EnchantmentAnvilMerge {
   private final EnchantmentItems items;

   public EnchantmentAnvilMerge(EnchantmentItems items) {
      this.items = Objects.requireNonNull(items);
   }

   public EnchantmentAnvilMerge.Preview prepare(ItemStack source, ItemStack book, ItemStack nativeResult, int baseCost) {
      if (source != null && book != null && book.getType() == Material.ENCHANTED_BOOK && baseCost >= 0) {
         Map<String, Integer> original = EnchantmentItems.inspectCustom(source.getItemMeta());
         Map<String, Integer> additions = EnchantmentItems.inspectCustom(book.getItemMeta());
         EnchantmentItems.validateAnvilNatives(source);
         EnchantmentItems.validateAnvilNatives(book);
         EnchantmentItems.Preview sourceValidation = this.items.previewCustom(source, original);
         EnchantmentItems.Preview bookValidation = this.items.previewCustom(book, additions);
         Map<Enchantment, Integer> nativeAdditions = EnchantmentItems.nativeEnchantments(book.getItemMeta());
         if (additions.isEmpty() && nativeAdditions.isEmpty()) {
            throw new IllegalArgumentException("Book has no transferable enchantments");
         } else {
            LinkedHashMap<String, Integer> custom = new LinkedHashMap<>(original);
            boolean containsCustom = !custom.isEmpty() || !additions.isEmpty();
            if (!containsCustom) {
               throw new IllegalArgumentException("Native-only operation belongs to the server");
            } else {
               int additionalCost = 0;

               for (Entry<String, Integer> entry : additions.entrySet()) {
                  int previous = custom.getOrDefault(entry.getKey(), 0);
                  int next = previous == entry.getValue() ? Math.addExact(previous, 1) : Math.max(previous, entry.getValue());
                  custom.put(entry.getKey(), next);
                  if (next != previous) {
                     additionalCost = Math.addExact(additionalCost, next);
                  }
               }

               ItemStack draft = nativeResult == null ? source.clone() : nativeResult.clone();
               draft.setAmount(1);
               Map<Enchantment, Integer> resultNatives = EnchantmentItems.nativeEnchantments(draft.getItemMeta());
               Map<Enchantment, Integer> sourceNatives = EnchantmentItems.nativeEnchantments(source.getItemMeta());

               for (Entry<Enchantment, Integer> entryx : nativeAdditions.entrySet()) {
                  int old = sourceNatives.getOrDefault(entryx.getKey(), 0);
                  int expected = old == entryx.getValue() ? Math.addExact(old, 1) : Math.max(old, entryx.getValue());
                  Enchantment nativeEnchantment = entryx.getKey();
                  expected = Math.min(expected, nativeEnchantment.getMaxLevel());
                  int actual = resultNatives.getOrDefault(entryx.getKey(), 0);
                  boolean accepted = nativeEnchantment.getKey().getNamespace().equals("minecraft")
                     ? actual == expected
                     : actual >= Math.min(Math.max(old, entryx.getValue()), nativeEnchantment.getMaxLevel());
                  if (nativeResult == null || !accepted) {
                     throw new IllegalArgumentException("The server rejected a native book entry: " + entryx.getKey());
                  }
               }

               EnchantmentItems.validateAnvilNatives(draft);
               EnchantmentItems.Preview result = this.items.previewCustom(draft, custom);
               if (additionalCost == 0 && nativeResult == null) {
                  throw new IllegalArgumentException("Book makes no transferable change");
               } else {
                  int cost = Math.addExact(baseCost, additionalCost);
                  return new EnchantmentAnvilMerge.Preview(source.clone(), book.clone(), draft, sourceValidation, bookValidation, result, cost);
               }
            }
         }
      } else {
         throw new IllegalArgumentException("Expected an item, actual enchanted book and nonnegative native cost");
      }
   }

   public static final class Preview {
      private final ItemStack source;
      private final ItemStack book;
      private final ItemStack draft;
      private final EnchantmentItems.Preview sourceValidation;
      private final EnchantmentItems.Preview bookValidation;
      private final EnchantmentItems.Preview result;
      private final int cost;

      private Preview(
         ItemStack source,
         ItemStack book,
         ItemStack draft,
         EnchantmentItems.Preview sourceValidation,
         EnchantmentItems.Preview bookValidation,
         EnchantmentItems.Preview result,
         int cost
      ) {
         this.source = source;
         this.book = book;
         this.draft = draft;
         this.sourceValidation = sourceValidation;
         this.bookValidation = bookValidation;
         this.result = result;
         this.cost = cost;
      }

      public int cost() {
         return this.cost;
      }

      public ItemStack item() {
         return this.result.previewItem();
      }

      public ItemStack apply(ItemStack source, ItemStack book) {
         if (this.source.equals(source) && this.book.equals(book)) {
            this.sourceValidation.apply(source);
            this.bookValidation.apply(book);
            return this.result.apply(this.draft);
         } else {
            throw new ConcurrentModificationException("Anvil inputs changed after preview");
         }
      }
   }
}
