package com.magmaguy.elitemobs.magmacore.enchantments;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

final class EnchantmentAnvilEndpoint implements BiFunction<String, ItemStack, Map<String, Object>> {
   static final String PROTOCOL = "nightbreak.enchantment-anvil.v1";
   final Plugin owner;
   private final Function<ItemStack, EnchantmentItemProfile> classifier;
   private final Function<ItemStack, String> veto;
   boolean active = true;

   EnchantmentAnvilEndpoint(Plugin owner, Function<ItemStack, EnchantmentItemProfile> classifier, Function<ItemStack, String> veto) {
      this.owner = owner;
      this.classifier = classifier;
      this.veto = veto;
   }

   public Map<String, Object> apply(String operation, ItemStack item) {
      EnchantmentProviders.requireServerThread();
      if (!this.active || !this.owner.isEnabled()) {
         return Map.of();
      } else if (operation.equals("describe")) {
         return Map.of("protocol", "nightbreak.enchantment-anvil.v1", "owner", this.owner.getName());
      } else if ((operation.equals("policy") || operation.equals("classify")) && item != null) {
         String denied = operation.equals("policy") ? this.veto.apply(item.clone()) : null;
         if (denied != null) {
            return Map.of("denied", denied);
         } else {
            EnchantmentItemProfile profile = this.classifier.apply(item.clone());
            return profile == null
               ? Map.of("accepted", true)
               : Map.of(
                  "accepted",
                  true,
                  "type",
                  profile.type().name(),
                  "slots",
                  profile.slots().stream().map(Enum::name).sorted().toList(),
                  "attacks",
                  profile.attackKinds().stream().sorted().toList()
               );
         }
      } else {
         throw new IllegalArgumentException("Invalid item policy call");
      }
   }
}
