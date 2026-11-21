package com.magmaguy.elitemobs.magmacore.enchantments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

public final class EnchantmentProviders {
   private static final Pattern NAMESPACE = Pattern.compile("[a-z0-9._-]{1,64}");
   private static final Pattern CAPABILITY = Pattern.compile("[a-z0-9._:-]{1,128}");

   private EnchantmentProviders() {
   }

   public static EnchantmentProviders.Registration register(Plugin owner, String namespace, Set<String> capabilities, EnchantmentProviders.Handler handler) {
      requireServerThread();
      Objects.requireNonNull(owner, "owner");
      if (!owner.isEnabled()) {
         throw new IllegalStateException("Provider plugin is disabled");
      } else {
         validateNamespace(namespace);
         Objects.requireNonNull(capabilities, "capabilities");
         if (capabilities.size() <= 128 && !capabilities.stream().anyMatch(c -> c == null || !CAPABILITY.matcher(c).matches())) {
            Objects.requireNonNull(handler, "handler");
            if (entries().stream().anyMatch(e -> e.provider.namespace().equals(namespace))) {
               throw new IllegalStateException("Enchantment namespace already owned: " + namespace);
            } else {
               EnchantmentProviderEndpoint endpoint = new EnchantmentProviderEndpoint(owner, namespace, capabilities, handler);
               Bukkit.getPluginManager().registerEvents(endpoint, owner);

               try {
                  Bukkit.getServicesManager().register(BiFunction.class, endpoint, owner, ServicePriority.Normal);
               } catch (Error | RuntimeException var6) {
                  endpoint.deactivate();
                  throw var6;
               }

               return new EnchantmentProviders.Registration(endpoint);
            }
         } else {
            throw new IllegalArgumentException("Invalid capabilities");
         }
      }
   }

   public static List<EnchantmentProviders.Provider> providers() {
      requireServerThread();
      return entries()
         .stream()
         .map(EnchantmentProviders.Entry::provider)
         .sorted(Comparator.comparing(EnchantmentProviders.Provider::namespace).thenComparing(EnchantmentProviders.Provider::plugin))
         .toList();
   }

   public static Optional<EnchantmentProviders.Provider> find(String namespace) {
      requireServerThread();
      validateNamespace(namespace);
      List<EnchantmentProviders.Entry> matches = matching(namespace);
      if (matches.size() > 1) {
         throw new IllegalStateException("Conflicting enchantment namespace: " + namespace);
      } else {
         return matches.isEmpty() ? Optional.empty() : Optional.of(matches.getFirst().provider);
      }
   }

   public static EnchantmentProviders.Result call(EnchantmentProviders.Provider expected, EnchantmentProviders.Operation operation, Map<String, Object> payload) {
      requireServerThread();
      Objects.requireNonNull(expected, "expected");
      Objects.requireNonNull(operation, "operation");
      List<EnchantmentProviders.Entry> matches = matching(expected.namespace());
      if (matches.isEmpty()) {
         return result(EnchantmentProviders.Status.UNAVAILABLE);
      } else if (matches.size() > 1) {
         return result(EnchantmentProviders.Status.CONFLICT);
      } else {
         EnchantmentProviders.Entry entry = matches.getFirst();
         if (!expected.compatible() || !entry.provider.compatible()) {
            return result(EnchantmentProviders.Status.INCOMPATIBLE);
         } else if (!entry.provider.equals(expected)) {
            return result(EnchantmentProviders.Status.STALE);
         } else {
            Map<String, Object> copied;
            try {
               copied = EnchantmentValues.copy(payload);
            } catch (IllegalArgumentException var13) {
               return result(EnchantmentProviders.Status.INVALID_REQUEST);
            }

            try {
               Map<String, Object> response = entry.endpoint
                  .apply(
                     operation.name(),
                     Map.of(
                        "protocol",
                        "magmacore.enchantments",
                        "version",
                        1,
                        "generation",
                        expected.generation(),
                        "revision",
                        expected.revision(),
                        "payload",
                        copied
                     )
                  );
               if (response != null && response.get("status") instanceof String statusText) {
                  EnchantmentProviders.Status status = EnchantmentProviders.Status.valueOf(statusText);
                  if (status != EnchantmentProviders.Status.OK) {
                     return result(status);
                  } else if (response.keySet().equals(Set.of("status", "payload")) && response.get("payload") instanceof Map<?, ?> output) {
                     List<EnchantmentProviders.Entry> current = matching(expected.namespace());
                     if (current.size() > 1) {
                        return result(EnchantmentProviders.Status.CONFLICT);
                     } else {
                        return !current.isEmpty() && current.getFirst().endpoint == entry.endpoint && current.getFirst().provider.equals(expected)
                           ? new EnchantmentProviders.Result(EnchantmentProviders.Status.OK, (Map<String, Object>)output)
                           : result(EnchantmentProviders.Status.STALE);
                     }
                  } else {
                     return result(EnchantmentProviders.Status.FAILED);
                  }
               } else {
                  return result(EnchantmentProviders.Status.FAILED);
               }
            } catch (LinkageError | RuntimeException var12) {
               return result(EnchantmentProviders.Status.FAILED);
            }
         }
      }
   }

   private static EnchantmentProviders.Result result(EnchantmentProviders.Status status) {
      return new EnchantmentProviders.Result(status, Map.of());
   }

   static void requireServerThread() {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("Enchantment providers require the server thread");
      }
   }

   private static void validateNamespace(String namespace) {
      if (namespace == null || !NAMESPACE.matcher(namespace).matches()) {
         throw new IllegalArgumentException("Namespace must be a lowercase identifier");
      }
   }

   private static List<EnchantmentProviders.Entry> matching(String namespace) {
      return entries().stream().filter(e -> e.provider.namespace().equals(namespace)).toList();
   }

   private static List<EnchantmentProviders.Entry> entries() {
      List<EnchantmentProviders.Entry> result = new ArrayList<>();

      for (RegisteredServiceProvider<BiFunction> service : Bukkit.getServicesManager().getRegistrations(BiFunction.class)) {
         Object candidate = service.getProvider();
         if (service.getPlugin().isEnabled() && candidate.getClass().getSimpleName().equals("EnchantmentProviderEndpoint")) {
            try {
               BiFunction<String, Map<String, Object>, Map<String, Object>> endpoint = (BiFunction<String, Map<String, Object>, Map<String, Object>>)candidate;
               if (endpoint.apply("describe", Map.of()) instanceof Map<?, ?> data
                  && "OK".equals(data.get("status"))
                  && "magmacore.enchantments".equals(data.get("protocol"))
                  && data.get("namespace") instanceof String namespace
                  && data.get("plugin") instanceof String plugin
                  && plugin.equals(service.getPlugin().getName())
                  && data.get("generation") instanceof UUID generation
                  && data.get("revision") instanceof Long revision
                  && revision >= 1L
                  && data.get("version") instanceof Integer version
                  && version >= 1
                  && data.get("capabilities") instanceof List<?> capabilities
                  && capabilities.size() <= 128) {
                  validateNamespace(namespace);
                  Set<String> verified = new HashSet<>();
                  Iterator var14 = capabilities.iterator();

                  while (var14.hasNext()) {
                     if (!(var14.next() instanceof String name) || !CAPABILITY.matcher(name).matches() || !verified.add(name)) {
                        throw new IllegalArgumentException("Malformed capabilities");
                     }
                  }

                  result.add(
                     new EnchantmentProviders.Entry(new EnchantmentProviders.Provider(namespace, plugin, generation, revision, version, verified), endpoint)
                  );
               }
            } catch (LinkageError | RuntimeException var17) {
            }
         }
      }

      return result;
   }

   private record Entry(EnchantmentProviders.Provider provider, BiFunction<String, Map<String, Object>, Map<String, Object>> endpoint) {
   }

   @FunctionalInterface
   public interface Handler {
      Map<String, Object> handle(EnchantmentProviders.Operation var1, Map<String, Object> var2);
   }

   public static enum Operation {
      RESOLVE,
      VALIDATE,
      EVALUATE;
   }

   public record Provider(String namespace, String plugin, UUID generation, long revision, int protocolVersion, Set<String> capabilities) {
      public Provider(String namespace, String plugin, UUID generation, long revision, int protocolVersion, Set<String> capabilities) {
         capabilities = Set.copyOf(capabilities);
         this.namespace = namespace;
         this.plugin = plugin;
         this.generation = generation;
         this.revision = revision;
         this.protocolVersion = protocolVersion;
         this.capabilities = capabilities;
      }

      public boolean compatible() {
         return this.protocolVersion == 1;
      }
   }

   public static final class Registration implements AutoCloseable {
      private final EnchantmentProviderEndpoint endpoint;
      private boolean closed;

      private Registration(EnchantmentProviderEndpoint endpoint) {
         this.endpoint = endpoint;
      }

      public long advanceRevision() {
         return this.endpoint.advanceRevision();
      }

      @Override
      public void close() {
         EnchantmentProviders.requireServerThread();
         if (!this.closed) {
            this.closed = true;
            this.endpoint.deactivate();
            Bukkit.getServicesManager().unregister(BiFunction.class, this.endpoint);
         }
      }
   }

   public record Result(EnchantmentProviders.Status status, Map<String, Object> payload) {
      public Result(EnchantmentProviders.Status status, Map<String, Object> payload) {
         payload = EnchantmentValues.copy(payload);
         this.status = status;
         this.payload = payload;
      }

      public Map<String, Object> payload() {
         return EnchantmentValues.copy(this.payload);
      }
   }

   public static enum Status {
      OK,
      UNAVAILABLE,
      INCOMPATIBLE,
      CONFLICT,
      STALE,
      UNSUPPORTED,
      INVALID_REQUEST,
      WRONG_THREAD,
      FAILED;
   }
}
