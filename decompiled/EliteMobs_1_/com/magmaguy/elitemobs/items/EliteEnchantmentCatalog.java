package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.combatsystem.EnchantmentDamage;
import com.magmaguy.elitemobs.items.customenchantments.SoulbindEnchantment;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentActions;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentCatalog;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentDefinition;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentDefinitions;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentInputs;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentItemProfile;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentItems;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentProviders;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentQueries;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.PlayerItem;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class EliteEnchantmentCatalog {
   public static final String LOUD_STRIKES = "elitemobs:loud_strikes";
   public static final String CRITICAL_STRIKES = "elitemobs:critical_strikes";
   public static final ScriptHook THREAT_BONUS = new ScriptHook("on_threat_bonus");
   public static final ScriptHook CRITICAL_CHANCE = new ScriptHook("on_critical_chance");
   public static final ScriptHook HUNTER_BONUS = new ScriptHook("on_natural_spawn_bonus");
   private static final Set<ScriptHook> HOOKS = Set.of(THREAT_BONUS, CRITICAL_CHANCE, HUNTER_BONUS);
   private static final Set<ScriptHook> ALL_HOOKS = Stream.concat(HOOKS.stream(), EnchantmentInputs.HOOKS.stream()).collect(Collectors.toUnmodifiableSet());
   private static final Set<String> SHARED = Set.of(
      "loud_strikes",
      "critical_strikes",
      "drilling",
      "ice_breaker",
      "summon_wolf",
      "summon_merchant",
      "flamethrower",
      "lightning",
      "hunter",
      "earthquake",
      "plasma_boots",
      "grappling_hook",
      "meteor_shower"
   );
   public static final Set<String> HOST_SETTINGS = Set.of("soulbind", "multicast", "blast_radius", "ignition");
   private static volatile EnchantmentCatalog catalog;
   private static volatile Set<String> sharedNames = SHARED;
   private static EnchantmentDefinitions.HostedCatalog hosted;
   private static long revision;

   private EliteEnchantmentCatalog() {
   }

   public static boolean ownsFilename(String filename) {
      String stem = filename.toLowerCase(Locale.ROOT).replaceFirst("\\.ya?ml$", "");
      return sharedNames.contains(stem);
   }

   public static void prepare() {
      JavaPlugin plugin = MetadataHandler.PLUGIN;
      Path directory = plugin.getDataFolder().toPath().resolve("enchantments");

      try {
         EnchantmentCatalog.initializeDefaults(plugin, directory, SHARED);
         Set<String> selectedNames = new HashSet<>(SHARED);
         EnchantmentCatalog candidate = EnchantmentCatalog.load("elitemobs", directory, ALL_HOOKS, path -> {
            String stem = path.getFileName().toString().toLowerCase(Locale.ROOT).replaceFirst("\\.ya?ml$", "");
            if (!HOST_SETTINGS.contains(stem) && Enchantment.getByKey(NamespacedKey.minecraft(stem)) == null) {
               selectedNames.add(stem);
               return true;
            } else {
               return false;
            }
         });
         catalog = candidate;
         sharedNames = Set.copyOf(selectedNames);
      } catch (IOException var4) {
         throw new UncheckedIOException(var4);
      }
   }

   public static void publish() {
      if (hosted == null) {
         hosted = EnchantmentDefinitions.publishActions(
            MetadataHandler.PLUGIN,
            catalog,
            Set.of("enchantment.source_facts.v1"),
            HOOKS,
            EnchantmentInputs.HOOKS,
            (operation, request) -> {
               if (operation == EnchantmentProviders.Operation.EVALUATE && "enchantment.source_facts.v1".equals(request.get("kind"))) {
                  return captureCombatFacts(request);
               } else if (operation == EnchantmentProviders.Operation.EVALUATE && "attributed_damage".equals(request.get("kind"))) {
                  EnchantmentActions.DamageInput input = EnchantmentActions.DamageInput.read(request);
                  return Map.of(
                     "applied",
                     input != null
                        && EnchantmentInputs.applyExplicitDamage(
                           MetadataHandler.PLUGIN,
                           input.actor(),
                           input.target(),
                           () -> EnchantmentDamage.apply(input.attackId(), input.providerFacts(), input::applyDamage)
                        )
                  );
               } else {
                  return Map.of("supported", false);
               }
            }
         );
      } else {
         hosted.reload(catalog);
      }

      revision++;
   }

   public static void close() {
      if (hosted != null) {
         hosted.close();
      }

      hosted = null;
      catalog = null;
      revision++;
   }

   public static long revision() {
      return revision;
   }

   public static double huntingGearBonus(List<Player> players) {
      double bonus = 0.0;

      for (Player player : players) {
         ElitePlayerInventory inventory = ElitePlayerInventory.playerInventories.get(player.getUniqueId());
         if (inventory != null) {
            bonus += inventory.getHunterChance(true);
         }
      }

      return bonus;
   }

   private static Map<String, Object> captureCombatFacts(Map<String, Object> request) {
      if (request.keySet().equals(Set.of("kind", "actor", "equipment"))
         && request.get("actor") instanceof UUID actorId
         && request.get("equipment") instanceof Map<?, ?> equipment) {
         Player player = Bukkit.getPlayer(actorId);
         if (player != null && player.isOnline()) {
            double threat = 0.0;
            int weaponLevel = 0;
            Map<String, Boolean> usableSlots = new LinkedHashMap<>();
            Map<String, Integer> equippedLevels = new TreeMap<>();

            for (Entry<?, ?> entry : equipment.entrySet()) {
               EnchantmentDefinition.Slot slot = EnchantmentDefinition.Slot.valueOf((String)entry.getKey());
               if (!(entry.getValue() instanceof ItemStack item)) {
                  throw new IllegalArgumentException("Invalid source item");
               }

               if (!EliteItemManager.isOnLastDamage(item)
                  && SoulbindEnchantment.isValidSoulbindUser(item.getItemMeta(), player)
                  && GearRestrictionHandler.canEquip(player, item)) {
                  usableSlots.put(slot.name(), true);
                  EnchantmentItemProfile profile = EnchantmentItems.classify(item);

                  for (Entry<String, Integer> enchantment : EnchantmentItems.inspectCustom(item.getItemMeta()).entrySet()) {
                     EnchantmentDefinition definition = definition(enchantment.getKey());
                     if (definition != null
                        && enchantment.getValue() >= 1
                        && (definition.validSlots().isEmpty() || definition.validSlots().contains(slot))
                        && (definition.itemTypes().isEmpty() || definition.itemTypes().contains(profile.type()))) {
                        equippedLevels.merge(enchantment.getKey(), enchantment.getValue(), Math::addExact);
                     }
                  }

                  if (slot == EnchantmentDefinition.Slot.MAINHAND) {
                     weaponLevel = PlayerItem.readWeaponTier(player, item);
                  }

                  threat += contribution(item, THREAT_BONUS, actorId, slot);
               }
            }

            if (!Double.isFinite(threat)) {
               throw new IllegalArgumentException("Invalid captured threat bonus");
            } else {
               return Map.of("weapon_level", weaponLevel, "loud_strikes", threat, "usable_slots", usableSlots, "equipped_levels", equippedLevels);
            }
         } else {
            throw new IllegalArgumentException("Missing source player");
         }
      } else {
         throw new IllegalArgumentException("Invalid EM source facts request");
      }
   }

   public static EnchantmentDefinition definition(String id) {
      EnchantmentCatalog current = catalog;
      return current == null ? null : current.definitions().get(id);
   }

   public static Set<String> definitionIds() {
      EnchantmentCatalog current = catalog;
      return current == null ? Set.of() : current.definitions().keySet();
   }

   public static String text(String id, String parameter, String fallback) {
      EnchantmentDefinition definition = definition(id);
      return (definition == null ? null : definition.parameters().get(parameter)) instanceof String text ? text : fallback;
   }

   public static double contribution(ItemStack item, ScriptHook hook, UUID actor, EnchantmentDefinition.Slot slot) {
      if (item != null && item.hasItemMeta() && !EliteItemManager.isOnLastDamage(item)) {
         Map<String, Integer> levels;
         try {
            levels = EnchantmentItems.inspectCustom(item.getItemMeta());
         } catch (IllegalArgumentException var15) {
            return 0.0;
         }

         if (levels.isEmpty()) {
            return 0.0;
         } else {
            ArrayList<EnchantmentQueries.Query> requests = new ArrayList<>();

            EnchantmentItemProfile profile;
            try {
               profile = EnchantmentItems.classify(item);
            } catch (IllegalArgumentException var14) {
               return 0.0;
            }

            for (Entry<String, Integer> entry : new TreeMap<>(levels).entrySet()) {
               EnchantmentDefinition definition = definition(entry.getKey());
               if (definition != null
                  && entry.getValue() >= 1
                  && (definition.validSlots().isEmpty() || definition.validSlots().contains(slot))
                  && (definition.itemTypes().isEmpty() || definition.itemTypes().contains(profile.type()))
                  && (definition.attackKinds().isEmpty() || !Collections.disjoint(definition.attackKinds(), profile.attackKinds()))) {
                  EnchantmentItems.Resolved resolved = EnchantmentDefinitions.resolve(entry.getKey());
                  if (resolved != null && resolved.available()) {
                     requests.add(
                        new EnchantmentQueries.Query(resolved.provider(), entry.getKey(), hook, entry.getValue(), Map.of("slot", slot.name()), actor, null)
                     );
                  }
               }
            }

            if (requests.isEmpty()) {
               return 0.0;
            } else {
               EnchantmentQueries.Batch result = EnchantmentQueries.evaluate(requests, new EnchantmentQueries.Limits(64, 50000000L, 250000L));
               if (result.status() != EnchantmentQueries.Status.OK) {
                  return 0.0;
               } else {
                  double total = 0.0;

                  for (EnchantmentQueries.Contribution contribution : result.contributions()) {
                     if (contribution.value() != null) {
                        double value = contribution.value();
                        if (!Double.isFinite(value) || value < 0.0) {
                           throw new IllegalArgumentException("Invalid EM contribution from " + contribution.id());
                        }

                        total += value;
                     }
                  }

                  if (!Double.isFinite(total)) {
                     throw new IllegalArgumentException("EM contribution overflow");
                  } else {
                     return total;
                  }
               }
            }
         }
      } else {
         return 0.0;
      }
   }

   public static Map<String, Integer> procedural(String id) {
      EnchantmentDefinition definition = definition(id);
      if (definition != null && Boolean.TRUE.equals(definition.parameters().get("procedural"))) {
         if (definition.parameters().get("procedural_max_level") instanceof Integer maximum && maximum >= 1) {
            return Map.of(id, ThreadLocalRandom.current().nextInt(maximum) + 1);
         } else {
            throw new IllegalArgumentException("Invalid procedural limit for " + id);
         }
      } else {
         return Map.of();
      }
   }
}
