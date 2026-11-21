package com.magmaguy.elitemobs.magmacore.enchantments;

import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptNativeProjectiles;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public final class EnchantmentActions {
   public static final String CAPABILITY = "enchantment.actions.v1";
   public static final String SOURCE_FACTS = "enchantment.source_facts.v1";

   private EnchantmentActions() {
   }

   public static Map<String, Object> captureProviderFacts(UUID actor, Map<String, ItemStack> equipment) {
      EnchantmentProviders.requireServerThread();
      Map<String, Object> facts = new LinkedHashMap<>();

      for (EnchantmentProviders.Provider provider : EnchantmentProviders.providers()) {
         if (provider.compatible() && provider.capabilities().contains("enchantment.source_facts.v1")) {
            EnchantmentProviders.Result result = EnchantmentProviders.call(
               provider, EnchantmentProviders.Operation.EVALUATE, Map.of("kind", "enchantment.source_facts.v1", "actor", actor, "equipment", equipment)
            );
            if (result.status() != EnchantmentProviders.Status.OK) {
               throw new IllegalArgumentException("Cannot capture source facts for " + provider.namespace());
            }

            facts.put(provider.namespace(), result.payload());
         }
      }

      return EnchantmentValues.copy(facts);
   }

   public static Map<String, ItemStack> captureEquipment(LivingEntity actor) {
      EnchantmentProviders.requireServerThread();
      EntityEquipment equipment = Objects.requireNonNull(actor.getEquipment(), "actor equipment");
      Map<String, ItemStack> captured = new LinkedHashMap<>();

      for (EnchantmentDefinition.Slot slot : EnchantmentDefinition.Slot.values()) {
         ItemStack item = switch (slot) {
            case MAINHAND -> equipment.getItemInMainHand();
            case OFFHAND -> equipment.getItemInOffHand();
            case HEAD -> equipment.getHelmet();
            case CHEST -> equipment.getChestplate();
            case LEGS -> equipment.getLeggings();
            case FEET -> equipment.getBoots();
         };
         if (item != null && !item.getType().isAir() && item.getAmount() > 0) {
            captured.put(slot.name(), item.clone());
         }
      }

      return Map.copyOf(captured);
   }

   public static EnchantmentActions.Action begin(EnchantmentItems.Resolved resolved, int level, EnchantmentActions.Source source) {
      return begin(resolved, level, source, null);
   }

   public static EnchantmentActions.Action begin(EnchantmentItems.Resolved resolved, int level, EnchantmentActions.Source source, ScriptHook initialHook) {
      EnchantmentProviders.requireServerThread();
      Objects.requireNonNull(resolved, "resolved");
      Objects.requireNonNull(source, "source");
      if (!resolved.provider().capabilities().contains("enchantment.actions.v1")) {
         return null;
      } else {
         EnchantmentProviders.Result result = EnchantmentProviders.call(
            resolved.provider(),
            EnchantmentProviders.Operation.EVALUATE,
            Map.of(
               "kind",
               "enchantment.actions.v1",
               "operation",
               "begin",
               "id",
               resolved.definition().id(),
               "level",
               level,
               "attack",
               source.attackId().toString(),
               "actor",
               source.actor().toString(),
               "world",
               source.world().toString(),
               "lifetime",
               source.lifetimeTicks(),
               "facts",
               source.facts(),
               "hook",
               initialHook == null ? "" : initialHook.getKey()
            )
         );
         if (status(result) == EnchantmentActions.Status.OK && result.payload().get("token") instanceof String token) {
            UUID.fromString(token);
            return new EnchantmentActions.Action(resolved.provider(), token);
         } else {
            return null;
         }
      }
   }

   private static EnchantmentActions.Status status(EnchantmentProviders.Result result) {
      if (result.status() != EnchantmentProviders.Status.OK) {
         return switch (result.status()) {
            case STALE -> EnchantmentActions.Status.STALE;
            case INVALID_REQUEST -> EnchantmentActions.Status.INVALID;
            case FAILED -> EnchantmentActions.Status.FAILED;
            default -> EnchantmentActions.Status.UNAVAILABLE;
         };
      } else {
         try {
            return EnchantmentActions.Status.valueOf((String)result.payload().get("status"));
         } catch (RuntimeException var2) {
            return EnchantmentActions.Status.FAILED;
         }
      }
   }

   public static final class Action implements AutoCloseable {
      private final EnchantmentProviders.Provider provider;
      private final String token;
      private boolean closed;
      private boolean cancelledInput;

      private Action(EnchantmentProviders.Provider provider, String token) {
         this.provider = provider;
         this.token = token;
      }

      public EnchantmentActions.Status dispatch(String stage, ScriptHook hook, UUID target) {
         return this.dispatch(stage, hook, target, false);
      }

      public EnchantmentActions.Status dispatchFinal(String stage, ScriptHook hook, UUID target) {
         return this.dispatch(stage, hook, target, true);
      }

      public boolean cancelledInput() {
         return this.cancelledInput;
      }

      private EnchantmentActions.Status dispatch(String stage, ScriptHook hook, UUID target, boolean finalStage) {
         EnchantmentProviders.requireServerThread();
         this.cancelledInput = false;
         if (this.closed) {
            return EnchantmentActions.Status.UNAVAILABLE;
         } else {
            Objects.requireNonNull(hook, "hook");
            if (stage != null && !stage.isBlank()) {
               EnchantmentProviders.Result result = EnchantmentProviders.call(
                  this.provider,
                  EnchantmentProviders.Operation.EVALUATE,
                  Map.of(
                     "kind",
                     "enchantment.actions.v1",
                     "operation",
                     "dispatch",
                     "token",
                     this.token,
                     "stage",
                     stage,
                     "hook",
                     hook.getKey(),
                     "target",
                     target == null ? "" : target.toString(),
                     "final",
                     finalStage
                  )
               );
               this.cancelledInput = Boolean.TRUE.equals(result.payload().get("cancelled"));
               return EnchantmentActions.status(result);
            } else {
               throw new IllegalArgumentException("Missing stage identity");
            }
         }
      }

      @Override
      public void close() {
         EnchantmentProviders.requireServerThread();
         if (!this.closed) {
            this.closed = true;
            EnchantmentProviders.call(
               this.provider, EnchantmentProviders.Operation.EVALUATE, Map.of("kind", "enchantment.actions.v1", "operation", "close", "token", this.token)
            );
         }
      }
   }

   public record DamageInput(
      UUID attackId, Player actor, LivingEntity target, double amount, Map<String, ItemStack> equipment, Map<String, Object> providerFacts
   ) {
      public void applyDamage() {
         ScriptNativeProjectiles.applyAttributedDamage(this.target, this.amount, this.actor);
      }

      public static EnchantmentActions.DamageInput read(Map<String, Object> request) {
         EnchantmentProviders.requireServerThread();
         if (request.keySet().equals(Set.of("kind", "attack", "actor", "world", "target", "amount", "facts"))
            && "attributed_damage".equals(request.get("kind"))
            && request.get("attack") instanceof UUID attack
            && request.get("actor") instanceof UUID actorId
            && request.get("world") instanceof UUID world
            && request.get("target") instanceof UUID targetId
            && request.get("amount") instanceof Double amount
            && Double.isFinite(amount)
            && !(amount <= 0.0)
            && request.get("facts") instanceof Map<?, ?> facts) {
            Entity var23 = Bukkit.getEntity(actorId);
            Entity targetEntity = Bukkit.getEntity(targetId);
            if (var23 instanceof Player actor
               && actor.isOnline()
               && actor.isValid()
               && !actor.isDead()
               && targetEntity instanceof LivingEntity target
               && target.isValid()
               && !target.isDead()
               && actor.getWorld().getUID().equals(world)
               && target.getWorld().getUID().equals(world)) {
               Map<String, ItemStack> equipment = new LinkedHashMap<>();
               if (facts.get("equipment") instanceof Map<?, ?> captured) {
                  for (Entry<?, ?> entry : captured.entrySet()) {
                     if (!(entry.getKey() instanceof String slot && entry.getValue() instanceof ItemStack item)) {
                        throw new IllegalArgumentException("Invalid captured equipment");
                     }

                     EnchantmentDefinition.Slot.valueOf(slot);
                     equipment.put(slot, item.clone());
                  }
               }

               Map<String, Object> providerFacts = facts.get("providers") instanceof Map<?, ?> values ? EnchantmentValues.copy(values) : Map.of();
               return new EnchantmentActions.DamageInput(attack, actor, target, amount, Map.copyOf(equipment), providerFacts);
            } else {
               return null;
            }
         } else {
            throw new IllegalArgumentException("Invalid attributed damage request");
         }
      }
   }

   public record Source(UUID attackId, UUID actor, UUID world, long lifetimeTicks, Map<String, Object> facts) {
      public Source(UUID attackId, UUID actor, UUID world, long lifetimeTicks, Map<String, Object> facts) {
         Objects.requireNonNull(attackId, "attackId");
         Objects.requireNonNull(actor, "actor");
         Objects.requireNonNull(world, "world");
         if (lifetimeTicks < 1L) {
            throw new IllegalArgumentException("An action needs a positive lifetime");
         } else {
            facts = EnchantmentValues.copy(facts);
            this.attackId = attackId;
            this.actor = actor;
            this.world = world;
            this.lifetimeTicks = lifetimeTicks;
            this.facts = facts;
         }
      }

      public Map<String, Object> facts() {
         return EnchantmentValues.copy(this.facts);
      }
   }

   public static enum Status {
      OK,
      DUPLICATE,
      UNAVAILABLE,
      STALE,
      INVALID,
      FAILED;
   }
}
