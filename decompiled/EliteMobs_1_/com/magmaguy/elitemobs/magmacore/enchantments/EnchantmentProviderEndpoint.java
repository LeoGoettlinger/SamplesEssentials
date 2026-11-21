package com.magmaguy.elitemobs.magmacore.enchantments;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.Plugin;

public final class EnchantmentProviderEndpoint implements BiFunction<String, Map<String, Object>, Map<String, Object>>, Listener {
   static final String PROTOCOL = "magmacore.enchantments";
   static final int VERSION = 1;
   private final Plugin owner;
   private final String namespace;
   private final Set<String> capabilities;
   private final EnchantmentProviders.Handler handler;
   private final UUID generation = UUID.randomUUID();
   private long revision = 1L;
   private boolean active = true;
   private boolean failureWarned;
   private Map<String, Object> descriptor;

   EnchantmentProviderEndpoint(Plugin owner, String namespace, Set<String> capabilities, EnchantmentProviders.Handler handler) {
      this.owner = owner;
      this.namespace = namespace;
      this.capabilities = Set.copyOf(capabilities);
      this.handler = handler;
      this.refreshDescriptor();
   }

   public Map<String, Object> apply(String operation, Map<String, Object> request) {
      if (!Bukkit.isPrimaryThread()) {
         return failure("WRONG_THREAD");
      } else if (!this.available()) {
         return failure("UNAVAILABLE");
      } else if ("describe".equals(operation)) {
         return this.descriptor;
      } else {
         try {
            if (request == null || !request.keySet().equals(Set.of("protocol", "version", "generation", "revision", "payload"))) {
               return failure("INVALID_REQUEST");
            } else if (!"magmacore.enchantments".equals(request.get("protocol")) || !Integer.valueOf(1).equals(request.get("version"))) {
               return failure("INCOMPATIBLE");
            } else if (this.generation.equals(request.get("generation")) && Long.valueOf(this.revision).equals(request.get("revision"))) {
               EnchantmentProviders.Operation kind;
               try {
                  kind = EnchantmentProviders.Operation.valueOf(operation);
               } catch (NullPointerException | IllegalArgumentException var8) {
                  return failure("UNSUPPORTED");
               }

               if (request.get("payload") instanceof Map<?, ?> payload) {
                  Map<String, Object> input;
                  try {
                     input = EnchantmentValues.copy(payload);
                  } catch (IllegalArgumentException var7) {
                     return failure("INVALID_REQUEST");
                  }

                  Map<String, Object> output = EnchantmentValues.copy(this.handler.handle(kind, input));
                  if (!this.available()) {
                     return failure("UNAVAILABLE");
                  } else {
                     return !Long.valueOf(this.revision).equals(request.get("revision")) ? failure("STALE") : Map.of("status", "OK", "payload", output);
                  }
               } else {
                  return failure("INVALID_REQUEST");
               }
            } else {
               return failure("STALE");
            }
         } catch (LinkageError | RuntimeException var9) {
            if (!this.failureWarned) {
               this.failureWarned = true;
               this.owner
                  .getLogger()
                  .log(
                     Level.WARNING,
                     "Enchantment provider " + this.namespace + " failed " + operation + "; further failures suppressed until its revision changes",
                     (Throwable)var9
                  );
            }

            return failure("FAILED");
         }
      }
   }

   long advanceRevision() {
      EnchantmentProviders.requireServerThread();
      if (!this.available()) {
         throw new IllegalStateException("Provider is unavailable");
      } else {
         this.revision = Math.incrementExact(this.revision);
         this.failureWarned = false;
         this.refreshDescriptor();
         return this.revision;
      }
   }

   private void refreshDescriptor() {
      this.descriptor = Map.of(
         "status",
         "OK",
         "protocol",
         "magmacore.enchantments",
         "version",
         1,
         "namespace",
         this.namespace,
         "plugin",
         this.owner.getName(),
         "generation",
         this.generation,
         "revision",
         this.revision,
         "capabilities",
         this.capabilities.stream().sorted().toList()
      );
   }

   @EventHandler
   public void onUnregister(ServiceUnregisterEvent event) {
      if (event.getProvider().getProvider() == this) {
         this.deactivate();
      }
   }

   void deactivate() {
      this.active = false;
      HandlerList.unregisterAll(this);
   }

   private boolean available() {
      return this.active && this.owner.isEnabled();
   }

   static Map<String, Object> failure(String status) {
      return Map.of("status", status);
   }
}
