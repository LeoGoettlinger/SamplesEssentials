package com.magmaguy.elitemobs.magmacore.enchantments;

import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import org.bukkit.plugin.Plugin;

public final class EnchantmentDefinitions {
   private static final Map<String, EnchantmentDefinitions.ResolvedCatalog> RESOLVED = new LinkedHashMap<>();

   private EnchantmentDefinitions() {
   }

   public static EnchantmentDefinitions.HostedCatalog publish(
      Plugin owner, EnchantmentCatalog catalog, Set<String> capabilities, EnchantmentProviders.Handler domainHandler
   ) {
      EnchantmentProviders.requireServerThread();
      return new EnchantmentDefinitions.HostedCatalog(owner, catalog, capabilities, domainHandler, null, null);
   }

   public static EnchantmentDefinitions.HostedCatalog publishQueries(
      Plugin owner, EnchantmentCatalog catalog, Set<String> capabilities, Set<ScriptHook> queryHooks, EnchantmentProviders.Handler domainHandler
   ) {
      EnchantmentProviders.requireServerThread();
      return new EnchantmentDefinitions.HostedCatalog(owner, catalog, capabilities, domainHandler, Set.copyOf(queryHooks), null);
   }

   public static EnchantmentDefinitions.HostedCatalog publishActions(
      Plugin owner,
      EnchantmentCatalog catalog,
      Set<String> capabilities,
      Set<ScriptHook> queryHooks,
      Set<ScriptHook> actionHooks,
      EnchantmentProviders.Handler domainHandler
   ) {
      EnchantmentProviders.requireServerThread();
      return new EnchantmentDefinitions.HostedCatalog(owner, catalog, capabilities, domainHandler, Set.copyOf(queryHooks), Set.copyOf(actionHooks));
   }

   public static EnchantmentItems.Resolved resolve(String id) {
      EnchantmentProviders.requireServerThread();
      EnchantmentDefinition.requireId(id);
      String namespace = id.substring(0, id.indexOf(58));
      if (namespace.equals("minecraft")) {
         throw new IllegalArgumentException("Native enchantments use the native registry");
      } else {
         EnchantmentProviders.Provider provider = EnchantmentProviders.find(namespace).orElse(null);
         if (provider != null && provider.compatible()) {
            EnchantmentDefinitions.ResolvedCatalog cached = RESOLVED.get(namespace);
            if (cached == null || !cached.provider().equals(provider)) {
               cached = new EnchantmentDefinitions.ResolvedCatalog(provider, new LinkedHashMap<>());
               RESOLVED.put(namespace, cached);
            }

            EnchantmentItems.Resolved existing = cached.definitions().get(id);
            if (existing != null) {
               return existing;
            } else {
               EnchantmentProviders.Result response = EnchantmentProviders.call(provider, EnchantmentProviders.Operation.RESOLVE, Map.of("id", id));
               if (response.status() != EnchantmentProviders.Status.OK) {
                  return null;
               } else {
                  Map<String, Object> payload = response.payload();
                  if (payload.equals(Map.of("found", false))) {
                     return null;
                  } else if (payload.keySet().equals(Set.of("found", "definition"))
                     && Boolean.TRUE.equals(payload.get("found"))
                     && payload.get("definition") instanceof Map<?, ?> raw) {
                     EnchantmentDefinition definition = decode(raw);
                     if (!definition.id().equals(id)) {
                        throw new IllegalArgumentException("Provider returned a different enchantment identity");
                     } else {
                        EnchantmentItems.Resolved resolved = new EnchantmentItems.Resolved(definition, provider);
                        cached.definitions().put(id, resolved);
                        return resolved;
                     }
                  } else {
                     throw new IllegalArgumentException("Malformed enchantment definition response from " + namespace);
                  }
               }
            }
         } else {
            RESOLVED.remove(namespace);
            return null;
         }
      }
   }

   private static Map<String, Object> encode(EnchantmentDefinition definition) {
      Map<String, Object> data = new LinkedHashMap<>();
      data.put("version", 1);
      data.put("id", definition.id());
      data.put("name", definition.name());
      data.put("description", definition.description());
      data.put("maxLevel", definition.maxLevel());
      data.put("curse", definition.curse());
      data.put("validSlots", names(definition.validSlots()));
      data.put("itemTypes", names(definition.itemTypes()));
      data.put("attackKinds", definition.attackKinds().stream().sorted().toList());
      data.put("requires", definition.requires().stream().sorted().toList());
      data.put("conflicts", definition.conflicts().stream().sorted().toList());
      data.put("stacking", definition.stacking().name());
      data.put("script", definition.script());
      data.put("parameters", definition.parameters());
      return Map.copyOf(data);
   }

   private static EnchantmentDefinition decode(Map<?, ?> data) {
      if (data.keySet()
            .equals(
               Set.of(
                  "version",
                  "id",
                  "name",
                  "description",
                  "maxLevel",
                  "curse",
                  "validSlots",
                  "itemTypes",
                  "attackKinds",
                  "requires",
                  "conflicts",
                  "stacking",
                  "script",
                  "parameters"
               )
            )
         && Integer.valueOf(1).equals(data.get("version"))) {
         if (data.get("maxLevel") instanceof Integer maximum
            && data.get("curse") instanceof Boolean curse
            && data.get("parameters") instanceof Map<?, ?> parameters) {
            return new EnchantmentDefinition(
               string(data, "id"),
               string(data, "name"),
               string(data, "description"),
               maximum,
               curse,
               strings(data, "validSlots", EnchantmentDefinition.Slot::valueOf),
               strings(data, "itemTypes", EnchantmentDefinition.ItemType::valueOf),
               strings(data, "attackKinds", Function.identity()),
               strings(data, "requires", Function.identity()),
               strings(data, "conflicts", Function.identity()),
               EnchantmentDefinition.Stacking.valueOf(string(data, "stacking")),
               string(data, "script"),
               EnchantmentValues.copy(parameters)
            );
         } else {
            throw new IllegalArgumentException("Invalid definition field types");
         }
      } else {
         throw new IllegalArgumentException("Unknown definition record");
      }
   }

   private static List<String> names(Set<? extends Enum<?>> values) {
      return values.stream().map(Enum::name).sorted().toList();
   }

   private static String string(Map<?, ?> data, String key) {
      if (data.get(key) instanceof String value) {
         return value;
      } else {
         throw new IllegalArgumentException("Expected string: " + key);
      }
   }

   private static <T> Set<T> strings(Map<?, ?> data, String key, Function<String, T> parser) {
      if (data.get(key) instanceof List<?> values && values.size() <= 128) {
         Set<T> parsed = new LinkedHashSet<>();
         Iterator var6 = values.iterator();

         while (var6.hasNext()) {
            if (!(var6.next() instanceof String text) || !parsed.add(parser.apply(text))) {
               throw new IllegalArgumentException("Invalid or repeated entry: " + key);
            }
         }

         return Set.copyOf(parsed);
      } else {
         throw new IllegalArgumentException("Expected bounded list: " + key);
      }
   }

   public static final class HostedCatalog implements AutoCloseable {
      private EnchantmentCatalog catalog;
      private final EnchantmentProviders.Registration registration;
      private final EnchantmentQueryExecutor queries;
      private final EnchantmentActionExecutor actions;
      private boolean closed;

      private HostedCatalog(
         Plugin owner,
         EnchantmentCatalog initial,
         Set<String> capabilities,
         EnchantmentProviders.Handler domainHandler,
         Set<ScriptHook> queryHooks,
         Set<ScriptHook> actionHooks
      ) {
         this.catalog = Objects.requireNonNull(initial, "catalog");
         Objects.requireNonNull(domainHandler, "domainHandler");
         Set<String> publishedCapabilities = new LinkedHashSet<>(capabilities);
         if (queryHooks != null) {
            publishedCapabilities.add("enchantment.numeric_queries.v1");
         }

         if (actionHooks != null) {
            publishedCapabilities.add("enchantment.actions.v1");
         }

         Set<ScriptHook> supported = new LinkedHashSet<>();
         if (queryHooks != null) {
            supported.addAll(queryHooks);
         }

         if (actionHooks != null) {
            supported.addAll(actionHooks);
         }

         this.queries = queryHooks == null
            ? null
            : new EnchantmentQueryExecutor(initial, queryHooks, supported, publishedCapabilities, owner.getLogger()::warning);
         this.actions = actionHooks == null ? null : new EnchantmentActionExecutor(owner, initial, actionHooks, publishedCapabilities, domainHandler);

         try {
            this.registration = EnchantmentProviders.register(
               owner,
               initial.namespace(),
               publishedCapabilities,
               (operation, request) -> {
                  if (this.actions != null && operation == EnchantmentProviders.Operation.EVALUATE && "enchantment.actions.v1".equals(request.get("kind"))) {
                     return this.actions.evaluate(request);
                  } else if (this.queries != null
                     && operation == EnchantmentProviders.Operation.EVALUATE
                     && "enchantment.numeric_queries.v1".equals(request.get("kind"))) {
                     return this.queries.evaluate(request);
                  } else if (operation != EnchantmentProviders.Operation.RESOLVE) {
                     return domainHandler.handle(operation, request);
                  } else if (request.keySet().equals(Set.of("id")) && request.get("id") instanceof String id) {
                     EnchantmentDefinition.requireId(id);
                     if (!id.startsWith(this.catalog.namespace() + ":")) {
                        throw new IllegalArgumentException("Foreign enchantment namespace");
                     } else {
                        EnchantmentDefinition definition = this.catalog.definitions().get(id);
                        return definition == null ? Map.of("found", false) : Map.of("found", true, "definition", EnchantmentDefinitions.encode(definition));
                     }
                  } else {
                     throw new IllegalArgumentException("Expected one enchantment id");
                  }
               }
            );
         } catch (LinkageError | RuntimeException var10) {
            if (this.actions != null) {
               this.actions.close();
            }

            if (this.queries != null) {
               this.queries.close();
            }

            throw var10;
         }
      }

      public void reload(EnchantmentCatalog candidate) {
         EnchantmentProviders.requireServerThread();
         Objects.requireNonNull(candidate, "candidate");
         if (this.closed) {
            throw new IllegalStateException("Enchantment catalog is closed");
         } else if (!this.catalog.namespace().equals(candidate.namespace())) {
            throw new IllegalArgumentException("Cannot change catalog ownership");
         } else {
            if (this.queries != null) {
               this.queries.validate(candidate);
            }

            this.registration.advanceRevision();
            this.catalog = candidate;
            if (this.queries != null) {
               this.queries.reload(candidate);
            }

            if (this.actions != null) {
               this.actions.reload(candidate);
            }
         }
      }

      public EnchantmentCatalog catalog() {
         EnchantmentProviders.requireServerThread();
         return this.catalog;
      }

      @Override
      public void close() {
         EnchantmentProviders.requireServerThread();
         this.registration.close();
         if (this.queries != null) {
            this.queries.close();
         }

         if (this.actions != null) {
            this.actions.close();
         }

         this.closed = true;
      }
   }

   private record ResolvedCatalog(EnchantmentProviders.Provider provider, Map<String, EnchantmentItems.Resolved> definitions) {
   }
}
