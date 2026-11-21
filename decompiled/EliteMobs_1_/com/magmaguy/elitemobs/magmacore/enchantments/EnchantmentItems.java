package com.magmaguy.elitemobs.magmacore.enchantments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public final class EnchantmentItems {
   private final Function<String, EnchantmentItems.Resolved> resolver;
   private final Function<ItemStack, EnchantmentItemProfile> classifier;

   public static EnchantmentItemProfile classify(ItemStack item) {
      EnchantmentProviders.requireServerThread();
      return EnchantmentAnvil.profile(item, false);
   }

   public EnchantmentItems(Function<String, EnchantmentItems.Resolved> resolver, Function<ItemStack, EnchantmentItemProfile> classifier) {
      this.resolver = Objects.requireNonNull(resolver, "resolver");
      this.classifier = Objects.requireNonNull(classifier, "classifier");
   }

   public Map<String, Integer> inspect(ItemStack item) {
      EnchantmentProviders.requireServerThread();
      ItemMeta meta = requireMeta(item);
      Map<String, Integer> result = new LinkedHashMap<>();
      nativeEnchantments(meta).forEach((enchantment, level) -> result.put(enchantment.getKey().toString(), level));
      EnchantmentItemData.read(meta).forEach((id, level) -> {
         if (result.putIfAbsent(id, level) != null) {
            throw new IllegalArgumentException("Native/custom identity collision: " + id);
         }
      });
      return Map.copyOf(result);
   }

   public EnchantmentItems.Preview preview(ItemStack source, Map<String, Integer> proposed) {
      return this.preview(source, proposed, false, false);
   }

   public static Map<String, Integer> inspectCustom(ItemMeta meta) {
      return EnchantmentItemData.read(Objects.requireNonNull(meta, "item metadata"));
   }

   public static EnchantmentItems.Construction prepareConstruction(Collection<String> ids) {
      EnchantmentProviders.requireServerThread();
      List<EnchantmentProviders.Provider> providers = EnchantmentProviders.providers();
      Map<String, EnchantmentItems.Resolved> definitions = new LinkedHashMap<>();
      Map<String, RuntimeException> failures = new LinkedHashMap<>();

      for (String id : new LinkedHashSet<>(ids)) {
         try {
            EnchantmentItems.Resolved resolved = EnchantmentDefinitions.resolve(id);
            if (resolved != null && resolved.available()) {
               definitions.put(id, resolved);
            }
         } catch (RuntimeException var7) {
            failures.put(id, var7);
         }
      }

      EnchantmentItems.Construction result = new EnchantmentItems.Construction(definitions, providers, failures);
      result.requireCurrent();
      return result;
   }

   public EnchantmentItems.Preview previewAuthored(ItemStack source, Map<String, Integer> proposed) {
      return this.preview(source, proposed, false, true);
   }

   public EnchantmentItems.Preview previewCustom(ItemStack source, Map<String, Integer> proposedCustom) {
      return this.preview(source, proposedCustom, true, false);
   }

   public EnchantmentItems.Preview previewAuthoredCustom(ItemStack source, Map<String, Integer> proposedCustom) {
      return this.preview(source, proposedCustom, true, true);
   }

   private EnchantmentItems.Preview preview(ItemStack source, Map<String, Integer> proposed, boolean preserveNative, boolean authored) {
      EnchantmentProviders.requireServerThread();
      ItemStack snapshot = source.clone();
      ItemMeta originalMeta = requireMeta(snapshot);
      this.inspect(snapshot);
      Map<String, Integer> requested = Map.copyOf(proposed);
      Set<String> nativeIds = new HashSet<>();
      nativeEnchantments(originalMeta).keySet().forEach(enchantment -> nativeIds.add(enchantment.getKey().toString()));
      EnchantmentItemProfile profile = Objects.requireNonNull(this.classifier.apply(snapshot.clone()), "item profile");
      Map<String, EnchantmentItems.Resolved> pinned = new LinkedHashMap<>();

      for (String id : EnchantmentItemData.read(originalMeta).keySet()) {
         this.pin(id, pinned);
      }

      Map<String, Integer> custom = new LinkedHashMap<>();
      Map<Enchantment, Integer> vanilla = new LinkedHashMap<>();

      for (Entry<String, Integer> entry : requested.entrySet()) {
         String id = EnchantmentDefinition.requireId(entry.getKey());
         Integer level = entry.getValue();
         if (level != null && level >= 1) {
            if (!preserveNative || !id.startsWith("minecraft:") && !nativeIds.contains(id)) {
               if (id.startsWith("minecraft:")) {
                  Enchantment enchantment = (Enchantment)Registry.ENCHANTMENT.get(Objects.requireNonNull(NamespacedKey.fromString(id)));
                  if (enchantment == null) {
                     throw new IllegalArgumentException("Unknown native enchantment: " + id);
                  }

                  if (!authored && level > enchantment.getMaxLevel()) {
                     throw new EnchantmentItems.LimitExceededException("Native level exceeds its limit: " + id);
                  }

                  if (!authored && !isBook(snapshot) && !enchantment.canEnchantItem(snapshot)) {
                     throw new IllegalArgumentException("Native enchantment does not support this item: " + id);
                  }

                  vanilla.put(enchantment, level);
               } else {
                  this.pin(id, pinned);
                  custom.put(id, level);
               }
               continue;
            }

            throw new IllegalArgumentException("Custom-only updates cannot write a native enchantment: " + id);
         }

         throw new IllegalArgumentException("Enchantment levels must be positive integers");
      }

      if (custom.size() > 64) {
         throw new EnchantmentItems.LimitExceededException("Too many custom enchantments");
      } else {
         Set<String> resultingIds = new HashSet<>(requested.keySet());
         if (preserveNative) {
            resultingIds.addAll(nativeIds);
         }

         List<String> problems = authored ? List.of() : validateCustom(profile, custom, resultingIds, pinned, isBook(snapshot));
         if (!problems.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", problems));
         } else {
            label127:
            if (!preserveNative) {
               for (Enchantment first : vanilla.keySet()) {
                  for (Enchantment second : vanilla.keySet()) {
                     if (!authored && first != second && (first.conflictsWith(second) || second.conflictsWith(first))) {
                        throw new IllegalArgumentException("Conflicting native enchantments: " + first.getKey() + " / " + second.getKey());
                     }
                  }
               }

               Iterator var25 = nativeEnchantments(originalMeta).keySet().iterator();

               Enchantment existing;
               do {
                  if (!var25.hasNext()) {
                     break label127;
                  }

                  existing = (Enchantment)var25.next();
               } while (existing.getKey().getNamespace().equals("minecraft"));

               throw new IllegalArgumentException("Updating third-party native registry enchantments is not supported: " + existing.getKey());
            }

            ItemStack draft = snapshot.clone();
            ItemMeta meta = requireMeta(draft);
            if (!preserveNative) {
               if (meta instanceof EnchantmentStorageMeta book) {
                  for (Enchantment existing : List.copyOf(book.getStoredEnchants().keySet())) {
                     book.removeStoredEnchant(existing);
                  }

                  for (Entry<Enchantment, Integer> entry : vanilla.entrySet()) {
                     if (!book.addStoredEnchant(entry.getKey(), entry.getValue(), authored)) {
                        throw new IllegalArgumentException("Native book enchantment rejected");
                     }
                  }
               } else {
                  meta.removeEnchantments();

                  for (Entry<Enchantment, Integer> entryx : vanilla.entrySet()) {
                     if (!meta.addEnchant(entryx.getKey(), entryx.getValue(), authored)) {
                        throw new IllegalArgumentException("Native enchantment rejected");
                     }
                  }
               }
            }

            EnchantmentItemData.write(meta, custom);
            EnchantmentPresentation.render(meta, custom, pinned::get);
            if (!draft.setItemMeta(meta)) {
               throw new IllegalArgumentException("Item rejected its metadata");
            } else {
               this.requireCurrent(pinned);
               return new EnchantmentItems.Preview(snapshot, draft, profile, Map.copyOf(pinned));
            }
         }
      }
   }

   public ItemStack refreshPresentation(ItemStack source) {
      return this.refreshPresentation(source, UnaryOperator.identity());
   }

   public ItemStack refreshPresentation(ItemStack source, UnaryOperator<List<String>> rebuildHostLore) {
      return this.refreshPresentation(source, rebuildHostLore, null);
   }

   public ItemStack refreshPresentation(ItemStack source, UnaryOperator<List<String>> rebuildHostLore, ToIntFunction<List<String>> enchantmentPosition) {
      EnchantmentProviders.requireServerThread();
      return renderPresentation(source, rebuildHostLore, enchantmentPosition, this.resolver);
   }

   private static ItemStack renderPresentation(
      ItemStack source,
      UnaryOperator<List<String>> rebuildHostLore,
      ToIntFunction<List<String>> enchantmentPosition,
      Function<String, EnchantmentItems.Resolved> resolver
   ) {
      Objects.requireNonNull(rebuildHostLore, "host lore builder");
      ItemStack result = source.clone();
      ItemMeta meta = requireMeta(result);
      EnchantmentPresentation.render(meta, EnchantmentItemData.read(meta), resolver, rebuildHostLore, enchantmentPosition);
      if (!result.setItemMeta(meta)) {
         throw new IllegalArgumentException("Item rejected its metadata");
      } else {
         return result;
      }
   }

   private void pin(String id, Map<String, EnchantmentItems.Resolved> pinned) {
      EnchantmentItems.Resolved current = this.resolver.apply(id);
      if (current != null && current.available() && current.definition().id().equals(id)) {
         EnchantmentItems.Resolved previous = pinned.putIfAbsent(id, current);
         if (previous != null && !previous.equals(current)) {
            throw new ConcurrentModificationException("Provider changed during preview");
         }
      } else {
         throw new IllegalArgumentException("Unavailable enchantment: " + id);
      }
   }

   private void requireCurrent(Map<String, EnchantmentItems.Resolved> pinned) {
      for (Entry<String, EnchantmentItems.Resolved> entry : pinned.entrySet()) {
         if (!entry.getValue().equals(this.resolver.apply(entry.getKey()))) {
            throw new ConcurrentModificationException("Enchantment provider changed after preview: " + entry.getKey());
         }
      }
   }

   static List<String> validateCustom(
      EnchantmentItemProfile profile, Map<String, Integer> requested, Set<String> resultingIds, Map<String, EnchantmentItems.Resolved> resolved, boolean book
   ) {
      List<String> problems = new ArrayList<>();
      List<String> limits = new ArrayList<>();

      for (Entry<String, Integer> entry : requested.entrySet()) {
         if (!entry.getKey().startsWith("minecraft:")) {
            EnchantmentItems.Resolved resolution = resolved.get(entry.getKey());
            if (resolution != null && resolution.available()) {
               EnchantmentDefinition definition = resolution.definition();
               if (entry.getValue() < 1 || entry.getValue() > definition.maxLevel()) {
                  limits.add("Level outside limits: " + entry.getKey());
               }

               if (!book) {
                  if (!definition.itemTypes().isEmpty() && !definition.itemTypes().contains(profile.type())) {
                     problems.add("Item type does not support " + entry.getKey());
                  }

                  if (!definition.validSlots().isEmpty() && Collections.disjoint(definition.validSlots(), profile.slots())) {
                     problems.add("No valid equipment slot for " + entry.getKey());
                  }

                  if (!definition.attackKinds().isEmpty() && Collections.disjoint(definition.attackKinds(), profile.attackKinds())) {
                     problems.add("No supported attack for " + entry.getKey());
                  }
               }

               for (String conflict : definition.conflicts()) {
                  if (resultingIds.contains(conflict)) {
                     problems.add("Conflicting enchantments: " + entry.getKey() + " / " + conflict);
                  }
               }
            } else {
               problems.add("Unavailable enchantment: " + entry.getKey());
            }
         }
      }

      if (problems.isEmpty() && !limits.isEmpty()) {
         throw new EnchantmentItems.LimitExceededException(String.join("; ", limits));
      } else {
         problems.addAll(limits);
         return List.copyOf(problems);
      }
   }

   static void validateAnvilNatives(ItemStack item) {
      Map<Enchantment, Integer> natives = nativeEnchantments(requireMeta(item));

      label49:
      for (Entry<Enchantment, Integer> entry : natives.entrySet()) {
         Enchantment enchantment = entry.getKey();
         if (enchantment.getKey().getNamespace().equals("minecraft")) {
            if (entry.getValue() >= 1 && entry.getValue() <= enchantment.getMaxLevel()) {
               if (!isBook(item) && !enchantment.canEnchantItem(item)) {
                  throw new IllegalArgumentException("Native enchantment does not support this item: " + enchantment.getKey());
               }

               Iterator var5 = natives.keySet().iterator();

               Enchantment other;
               do {
                  if (!var5.hasNext()) {
                     continue label49;
                  }

                  other = (Enchantment)var5.next();
               } while (other == enchantment || !enchantment.conflictsWith(other) && !other.conflictsWith(enchantment));

               throw new IllegalArgumentException("Conflicting native enchantments: " + enchantment.getKey() + " / " + other.getKey());
            }

            throw new IllegalArgumentException("Native level exceeds its limit: " + enchantment.getKey());
         }
      }
   }

   static Map<Enchantment, Integer> nativeEnchantments(ItemMeta meta) {
      if (meta instanceof EnchantmentStorageMeta book) {
         if (!meta.getEnchants().isEmpty()) {
            throw new IllegalArgumentException("Book has conflicting native storage forms");
         } else {
            return book.getStoredEnchants();
         }
      } else {
         return meta.getEnchants();
      }
   }

   private static boolean isBook(ItemStack item) {
      return item.getType() == Material.ENCHANTED_BOOK;
   }

   private static ItemMeta requireMeta(ItemStack item) {
      if (item != null && !item.getType().isAir() && item.getAmount() >= 1) {
         return Objects.requireNonNull(item.getItemMeta(), "item metadata");
      } else {
         throw new IllegalArgumentException("Expected a nonempty item");
      }
   }

   public static final class Construction {
      private final Map<String, EnchantmentItems.Resolved> definitions;
      private final List<EnchantmentProviders.Provider> providers;
      private final Map<String, RuntimeException> failures;

      private Construction(
         Map<String, EnchantmentItems.Resolved> definitions, List<EnchantmentProviders.Provider> providers, Map<String, RuntimeException> failures
      ) {
         this.definitions = Map.copyOf(definitions);
         this.providers = List.copyOf(providers);
         this.failures = Map.copyOf(failures);
      }

      public void requireCurrent() {
         if (!this.providers.equals(EnchantmentProviders.providers())) {
            throw new ConcurrentModificationException("Enchantment providers changed during item construction");
         }
      }

      public ItemStack applyCustom(ItemStack source, Map<String, Integer> custom) {
         ItemStack result = source.clone();
         ItemMeta meta = EnchantmentItems.requireMeta(result);

         for (Enchantment nativeEnchantment : EnchantmentItems.nativeEnchantments(meta).keySet()) {
            if (custom.containsKey(nativeEnchantment.getKey().toString())) {
               throw new IllegalArgumentException("Custom enchantment collides with a native enchantment: " + nativeEnchantment.getKey());
            }
         }

         for (String id : custom.keySet()) {
            if (this.failures.containsKey(id)) {
               throw new IllegalArgumentException("Failed to resolve custom enchantment: " + id, this.failures.get(id));
            }

            if (id.startsWith("minecraft:") || !this.definitions.containsKey(id)) {
               throw new IllegalArgumentException("Unavailable custom enchantment: " + id);
            }
         }

         EnchantmentItemData.write(meta, custom);
         EnchantmentPresentation.render(meta, custom, this.definitions::get);
         if (!result.setItemMeta(meta)) {
            throw new IllegalArgumentException("Item rejected its metadata");
         } else {
            return result;
         }
      }

      public ItemStack refreshPresentation(ItemStack source, UnaryOperator<List<String>> rebuildHostLore, ToIntFunction<List<String>> position) {
         return EnchantmentItems.renderPresentation(source, rebuildHostLore, position, this.definitions::get);
      }
   }

   public static final class LimitExceededException extends IllegalArgumentException {
      public LimitExceededException(String message) {
         super(message);
      }
   }

   public final class Preview {
      private final ItemStack source;
      private final ItemStack draft;
      private final EnchantmentItemProfile profile;
      private final Map<String, EnchantmentItems.Resolved> pinned;

      private Preview(ItemStack source, ItemStack draft, EnchantmentItemProfile profile, Map<String, EnchantmentItems.Resolved> pinned) {
         this.source = source;
         this.draft = draft;
         this.profile = profile;
         this.pinned = pinned;
      }

      public ItemStack previewItem() {
         EnchantmentProviders.requireServerThread();
         return this.draft.clone();
      }

      public ItemStack apply(ItemStack current) {
         EnchantmentProviders.requireServerThread();
         if (this.source.equals(current) && this.profile.equals(EnchantmentItems.this.classifier.apply(current.clone()))) {
            EnchantmentItems.this.requireCurrent(this.pinned);
            return this.draft.clone();
         } else {
            throw new ConcurrentModificationException("The source item or its definition changed after preview");
         }
      }
   }

   public record Resolved(EnchantmentDefinition definition, EnchantmentProviders.Provider provider) {
      public Resolved(EnchantmentDefinition definition, EnchantmentProviders.Provider provider) {
         Objects.requireNonNull(definition, "definition");
         Objects.requireNonNull(provider, "provider");
         if (definition.id().startsWith(provider.namespace() + ":") && provider.generation() != null && provider.revision() >= 1L) {
            this.definition = definition;
            this.provider = provider;
         } else {
            throw new IllegalArgumentException("Definition/provider ownership mismatch");
         }
      }

      public boolean available() {
         return this.provider.compatible() && this.provider.capabilities().containsAll(this.definition.requires());
      }
   }
}
