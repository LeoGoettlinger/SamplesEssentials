package com.magmaguy.elitemobs.magmacore.enchantments;

import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

public final class EnchantmentQueries {
   public static final String CAPABILITY = "enchantment.numeric_queries.v1";

   private EnchantmentQueries() {
   }

   public static EnchantmentQueries.Batch evaluate(List<EnchantmentQueries.Query> requests, EnchantmentQueries.Limits limits) {
      EnchantmentProviders.requireServerThread();
      Objects.requireNonNull(limits, "limits");
      if (requests != null && requests.size() <= limits.maximumQueries()) {
         List<EnchantmentQueries.Query> queries = List.copyOf(requests);
         List<EnchantmentQueries.Contribution> values = new ArrayList<>();
         long nanos = 0L;
         long instructions = 0L;

         for (EnchantmentQueries.Query query : queries) {
            if (nanos < limits.cpuNanos() && instructions < limits.instructions()) {
               if (!query.provider().capabilities().contains("enchantment.numeric_queries.v1")) {
                  return failed(EnchantmentQueries.Status.UNAVAILABLE, values.size(), nanos, instructions);
               }

               EnchantmentProviders.Result result = EnchantmentProviders.call(
                  query.provider(),
                  EnchantmentProviders.Operation.EVALUATE,
                  Map.of(
                     "kind",
                     "enchantment.numeric_queries.v1",
                     "id",
                     query.id(),
                     "hook",
                     query.hook().getKey(),
                     "level",
                     query.level(),
                     "input",
                     query.input(),
                     "actor",
                     query.actor() == null ? "" : query.actor().toString(),
                     "target",
                     query.target() == null ? "" : query.target().toString(),
                     "cpuNanos",
                     limits.cpuNanos() - nanos,
                     "instructions",
                     limits.instructions() - instructions
                  )
               );
               if (result.status() != EnchantmentProviders.Status.OK) {
                  EnchantmentQueries.Status status = switch (result.status()) {
                     case STALE -> EnchantmentQueries.Status.STALE;
                     case UNAVAILABLE, INCOMPATIBLE, CONFLICT, UNSUPPORTED -> EnchantmentQueries.Status.UNAVAILABLE;
                     case INVALID_REQUEST -> EnchantmentQueries.Status.INVALID;
                     default -> EnchantmentQueries.Status.FAILED;
                  };
                  return failed(status, values.size(), nanos, instructions);
               }

               try {
                  Map<String, Object> data = result.payload();
                  if (data.keySet().equals(Set.of("kind", "id", "hook", "status", "value", "hasValue", "chargedNanos", "instructions"))
                     && "enchantment.numeric_queries.v1".equals(data.get("kind"))
                     && query.id().equals(data.get("id"))
                     && query.hook().getKey().equals(data.get("hook"))
                     && data.get("chargedNanos") instanceof Long usedNanos
                     && usedNanos >= 0L
                     && data.get("instructions") instanceof Long usedInstructions
                     && usedInstructions >= 0L
                     && data.get("hasValue") instanceof Boolean hasValue
                     && data.get("value") instanceof Double value
                     && Double.isFinite(value)
                     && data.get("status") instanceof String statusText) {
                     EnchantmentQueries.Status status = EnchantmentQueries.Status.valueOf(statusText);
                     nanos += Math.min(usedNanos, limits.cpuNanos() - nanos);
                     instructions += Math.min(usedInstructions, limits.instructions() - instructions);
                     if (status != EnchantmentQueries.Status.OK) {
                        return failed(status, values.size(), nanos, instructions);
                     }

                     if (nanos < limits.cpuNanos() && instructions < limits.instructions()) {
                        if (!hasValue && value != 0.0) {
                           return failed(EnchantmentQueries.Status.FAILED, values.size(), nanos, instructions);
                        }

                        values.add(new EnchantmentQueries.Contribution(query.id(), query.hook().getKey(), hasValue ? value : null));
                        continue;
                     }

                     return failed(EnchantmentQueries.Status.BUDGET_EXHAUSTED, values.size(), nanos, instructions);
                  }

                  return failed(EnchantmentQueries.Status.FAILED, values.size(), nanos, instructions);
               } catch (IllegalArgumentException var18) {
                  return failed(EnchantmentQueries.Status.FAILED, values.size(), nanos, instructions);
               }
            }

            return failed(EnchantmentQueries.Status.BUDGET_EXHAUSTED, values.size(), nanos, instructions);
         }

         return new EnchantmentQueries.Batch(EnchantmentQueries.Status.OK, values, values.size(), nanos, instructions);
      } else {
         throw new IllegalArgumentException("Too many enchantment queries");
      }
   }

   private static EnchantmentQueries.Batch failed(EnchantmentQueries.Status status, int completed, long nanos, long instructions) {
      return new EnchantmentQueries.Batch(status, List.of(), completed, nanos, instructions);
   }

   static void requireHook(ScriptHook hook) {
      if (hook == null
         || !hook.getKey().matches("on_[a-z0-9_]{1,100}")
         || hook.equals(ScriptHook.ON_TICK)
         || hook.equals(ScriptHook.ON_ZONE_ENTER)
         || hook.equals(ScriptHook.ON_ZONE_LEAVE)) {
         throw new IllegalArgumentException("Expected a synchronous query hook");
      }
   }

   static Map<String, Object> copyInput(Map<?, ?> input) {
      Map<String, Object> result = EnchantmentValues.copy(input);

      for (Entry<String, Object> entry : result.entrySet()) {
         if (!entry.getKey().matches("[a-zA-Z_][a-zA-Z0-9_]{0,127}")
            || !(entry.getValue() instanceof String) && !(entry.getValue() instanceof Number) && !(entry.getValue() instanceof Boolean)) {
            throw new IllegalArgumentException("Query inputs must be named scalar snapshots");
         }
      }

      return result;
   }

   public record Batch(
      EnchantmentQueries.Status status, List<EnchantmentQueries.Contribution> contributions, int completedQueries, long chargedNanos, long instructions
   ) {
      public Batch(
         EnchantmentQueries.Status status, List<EnchantmentQueries.Contribution> contributions, int completedQueries, long chargedNanos, long instructions
      ) {
         contributions = List.copyOf(contributions);
         this.status = status;
         this.contributions = contributions;
         this.completedQueries = completedQueries;
         this.chargedNanos = chargedNanos;
         this.instructions = instructions;
      }
   }

   public record Contribution(String id, String hook, Double value) {
   }

   public record Limits(int maximumQueries, long cpuNanos, long instructions) {
      public Limits(int maximumQueries, long cpuNanos, long instructions) {
         if (maximumQueries >= 1 && maximumQueries <= 128 && cpuNanos >= 1L && cpuNanos <= 50000000L && instructions >= 1L && instructions <= 250000L) {
            this.maximumQueries = maximumQueries;
            this.cpuNanos = cpuNanos;
            this.instructions = instructions;
         } else {
            throw new IllegalArgumentException("Invalid enchantment query allowance");
         }
      }
   }

   public record Query(EnchantmentProviders.Provider provider, String id, ScriptHook hook, int level, Map<String, Object> input, UUID actor, UUID target) {
      public Query(EnchantmentProviders.Provider provider, String id, ScriptHook hook, int level, Map<String, Object> input) {
         this(provider, id, hook, level, input, null, null);
      }

      public Query(EnchantmentProviders.Provider provider, String id, ScriptHook hook, int level, Map<String, Object> input, UUID actor, UUID target) {
         Objects.requireNonNull(provider, "provider");
         EnchantmentDefinition.requireId(id);
         EnchantmentQueries.requireHook(hook);
         if (id.startsWith(provider.namespace() + ":") && level >= 1) {
            input = EnchantmentQueries.copyInput(input);
            this.provider = provider;
            this.id = id;
            this.hook = hook;
            this.level = level;
            this.input = input;
            this.actor = actor;
            this.target = target;
         } else {
            throw new IllegalArgumentException("Invalid query identity or level");
         }
      }
   }

   public static enum Status {
      OK,
      UNAVAILABLE,
      STALE,
      INVALID,
      FAILED,
      QUARANTINED,
      BUDGET_EXHAUSTED;
   }
}
