package com.magmaguy.elitemobs.magmacore.enchantments;

import com.magmaguy.elitemobs.magmacore.scripting.LuaExecutionBudget;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptDefinition;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptInstance;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptQueryResult;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptableEntity;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaEntityTable;
import com.magmaguy.elitemobs.magmacore.scripting.tables.LuaLivingEntityTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

final class EnchantmentQueryExecutor {
   private EnchantmentCatalog catalog;
   private final Set<ScriptHook> hooks;
   private final Set<ScriptHook> catalogHooks;
   private final Consumer<String> warning;
   private final Set<String> capabilities;
   private final Set<String> quarantined = new HashSet<>();
   private final Set<String> warned = new HashSet<>();
   private boolean closed;

   EnchantmentQueryExecutor(EnchantmentCatalog catalog, Set<ScriptHook> hooks, Set<String> capabilities, Consumer<String> warning) {
      this(catalog, hooks, hooks, capabilities, warning);
   }

   EnchantmentQueryExecutor(EnchantmentCatalog catalog, Set<ScriptHook> hooks, Set<ScriptHook> catalogHooks, Set<String> capabilities, Consumer<String> warning) {
      this.hooks = Set.copyOf(hooks);
      this.catalogHooks = Set.copyOf(catalogHooks);
      this.hooks.forEach(EnchantmentQueries::requireHook);
      this.warning = warning;
      this.capabilities = Set.copyOf(capabilities);
      this.validate(catalog);
      this.catalog = catalog;
   }

   void validate(EnchantmentCatalog candidate) {
      for (String id : candidate.definitions().keySet()) {
         ScriptDefinition script = candidate.script(id).orElseThrow();
         if (!this.catalogHooks.containsAll(script.getHooks())) {
            throw new IllegalArgumentException("Catalog declares unsupported query hooks: " + id);
         }
      }
   }

   void reload(EnchantmentCatalog candidate) {
      this.catalog = candidate;
      this.quarantined.clear();
      this.warned.clear();
   }

   void close() {
      this.closed = true;
      this.quarantined.clear();
      this.warned.clear();
   }

   Map<String, Object> evaluate(Map<String, Object> request) {
      if (request.keySet().equals(Set.of("kind", "id", "hook", "level", "input", "actor", "target", "cpuNanos", "instructions"))
         && "enchantment.numeric_queries.v1".equals(request.get("kind"))
         && request.get("id") instanceof String id
         && request.get("hook") instanceof String hookName
         && request.get("level") instanceof Integer level
         && request.get("input") instanceof Map<?, ?> rawInput
         && request.get("actor") instanceof String actorId
         && request.get("target") instanceof String targetId
         && request.get("cpuNanos") instanceof Long nanos
         && request.get("instructions") instanceof Long instructions) {
         new EnchantmentQueries.Limits(1, nanos, instructions);
         EnchantmentDefinition.requireId(id);
         ScriptHook hook = new ScriptHook(hookName);
         EnchantmentQueries.requireHook(hook);
         Map input = EnchantmentQueries.copyInput(rawInput);
         Entity actor = resolveEntity(actorId);
         Entity target = resolveEntity(targetId);
         if (!actorId.isEmpty() && actor == null || !targetId.isEmpty() && target == null) {
            return response(id, hookName, EnchantmentQueries.Status.UNAVAILABLE, null, 0L, 0L);
         } else if (this.closed) {
            return response(id, hookName, EnchantmentQueries.Status.UNAVAILABLE, null, 0L, 0L);
         } else {
            EnchantmentDefinition definition = this.catalog.definitions().get(id);
            ScriptDefinition script = this.catalog.script(id).orElse(null);
            if (definition == null || script == null || level < 1) {
               return response(id, hookName, EnchantmentQueries.Status.INVALID, null, 0L, 0L);
            } else if (!this.hooks.contains(hook) || !script.supportsHook(hook)) {
               return response(id, hookName, EnchantmentQueries.Status.OK, null, 0L, 0L);
            } else if (this.quarantined.contains(id)) {
               return response(id, hookName, EnchantmentQueries.Status.QUARANTINED, null, 0L, 0L);
            } else if (!this.capabilities.containsAll(definition.requires())) {
               return response(id, hookName, EnchantmentQueries.Status.UNAVAILABLE, null, 0L, 0L);
            } else {
               LuaExecutionBudget.Measurement<EnchantmentQueryExecutor.Invocation> measured = LuaExecutionBudget.measure(
                  () -> {
                     EnchantmentQueryExecutor.QueryOwner owner = new EnchantmentQueryExecutor.QueryOwner(
                        id, level, definition.parametersAt(level), input, this.hooks, actor, target
                     );
                     ScriptInstance instance = new ScriptInstance(script, owner);

                     EnchantmentQueryExecutor.Invocation var16x;
                     try {
                        var16x = new EnchantmentQueryExecutor.Invocation(
                           instance.handleQuery(
                              hook, null, target instanceof LivingEntity livingx ? livingx : null, actor instanceof LivingEntity living ? living : null
                           ),
                           owner.problem
                        );
                     } finally {
                        instance.shutdown();
                     }

                     return var16x;
                  },
                  nanos,
                  instructions
               );
               ScriptQueryResult result = measured.value() == null ? null : measured.value().result();
               Double value = null;
               EnchantmentQueries.Status status;
               if (measured.exhausted()) {
                  status = EnchantmentQueries.Status.BUDGET_EXHAUSTED;
               } else if (measured.failure() == null && result != null) {
                  if (result.kind() == ScriptQueryResult.Kind.NIL) {
                     status = EnchantmentQueries.Status.OK;
                  } else if (result.kind() == ScriptQueryResult.Kind.NUMBER && Double.isFinite(result.number())) {
                     status = EnchantmentQueries.Status.OK;
                     value = result.number();
                  } else {
                     status = EnchantmentQueries.Status.FAILED;
                  }
               } else {
                  status = EnchantmentQueries.Status.FAILED;
               }

               if (status != EnchantmentQueries.Status.OK) {
                  if (status != EnchantmentQueries.Status.BUDGET_EXHAUSTED) {
                     this.quarantined.add(id);
                  }

                  String detail = measured.failure() != null
                     ? measured.failure().getMessage()
                     : (measured.value() == null ? "No query result" : measured.value().problem());
                  if (detail == null) {
                     detail = "Expected nil or one finite numeric contribution";
                  }

                  detail = detail.replaceAll("[\\r\\n\\p{Cntrl}]", " ");
                  if (detail.length() > 512) {
                     detail = detail.substring(0, 512);
                  }

                  if (this.warned.add(id)) {
                     this.warning.accept("Enchantment " + id + " hook " + hookName + " failed: " + status + ": " + detail);
                  }
               }

               return response(id, hookName, status, value, measured.chargedNanos(), measured.instructions());
            }
         }
      } else {
         throw new IllegalArgumentException("Invalid numeric query payload");
      }
   }

   private static Entity resolveEntity(String id) {
      return id.isEmpty() ? null : Bukkit.getEntity(UUID.fromString(id));
   }

   private static Map<String, Object> response(String id, String hook, EnchantmentQueries.Status status, Double value, long nanos, long instructions) {
      return Map.of(
         "kind",
         "enchantment.numeric_queries.v1",
         "id",
         id,
         "hook",
         hook,
         "status",
         status.name(),
         "hasValue",
         value != null,
         "value",
         value == null ? 0.0 : value,
         "chargedNanos",
         nanos,
         "instructions",
         instructions
      );
   }

   private record Invocation(ScriptQueryResult result, String problem) {
   }

   private static final class QueryOwner extends ScriptableEntity {
      private final LuaTable enchantment;
      private final LuaTable parameters;
      private final LuaTable input;
      private final Set<ScriptHook> hooks;
      private final Entity actor;
      private final Entity target;
      private String problem;

      QueryOwner(String id, int level, Map<String, Object> parameters, Map<String, Object> input, Set<ScriptHook> hooks, Entity actor, Entity target) {
         this.enchantment = table(Map.of("id", id, "level", level));
         this.parameters = table(parameters);
         this.input = table(input);
         this.hooks = hooks;
         this.actor = actor;
         this.target = target;
      }

      @Override
      public boolean handleScriptError(String context, Exception failure) {
         this.problem = failure.getMessage();
         return true;
      }

      @Override
      public LuaTable buildContextTable(ScriptInstance instance) {
         return this.enchantment;
      }

      @Override
      public String getContextKey() {
         return "enchantment";
      }

      @Override
      public Set<ScriptHook> getSupportedHooks() {
         return this.hooks;
      }

      @Override
      public Entity getBukkitEntity() {
         return this.actor;
      }

      @Override
      public Location getLocation() {
         return this.actor == null ? null : this.actor.getLocation();
      }

      @Override
      public LuaValue resolveExtraContext(String key, ScriptInstance instance) {
         return (LuaValue)(switch (key) {
            case "parameters" -> this.parameters;
            case "input" -> this.input;
            case "target" -> this.target instanceof LivingEntity living
               ? LuaLivingEntityTable.build(living)
               : (this.target == null ? LuaValue.NIL : LuaEntityTable.build(this.target));
            default -> LuaValue.NIL;
         });
      }

      private static LuaTable table(Map<String, Object> values) {
         LuaTable table = new LuaTable();
         values.forEach(
            (key, value) -> table.set(
               key,
               (LuaValue)(value instanceof Boolean flag
                  ? LuaValue.valueOf(flag)
                  : (value instanceof Number number ? LuaValue.valueOf(number.doubleValue()) : LuaValue.valueOf((String)value)))
            )
         );
         return table;
      }
   }
}
