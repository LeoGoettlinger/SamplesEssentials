package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerProgram;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerType;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.powers.lua.LuaPowerManager;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

final class EliteLuaPowerCatalog {
   private final Map<NamespacedKey, EliteLuaPowerCatalog.Entry> entries = new LinkedHashMap<>();

   EliteLuaPowerProgram register(Plugin owner, NamespacedKey key, long revision, String source, EliteLuaPowerType type, String effect) {
      Objects.requireNonNull(owner, "owner");
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(source, "source");
      Objects.requireNonNull(type, "type");
      if (revision < 1L) {
         throw new IllegalArgumentException("revision must be positive");
      } else {
         requireOwnerNamespace(owner, key);
         String normalizedSource = source.replace("\r", "");
         EliteLuaPowerCatalog.Entry previous = this.entries.get(key);
         if (previous != null) {
            if (previous.owner() != owner) {
               throw new IllegalArgumentException("Plugin " + owner.getName() + " does not own Lua power " + key);
            }

            if (revision == previous.revision()) {
               if (previous.matches(normalizedSource, type, effect)) {
                  return previous.descriptor();
               }

               throw new IllegalArgumentException("Lua power " + key + " revision " + revision + " is already registered with different content");
            }

            if (revision < previous.revision()) {
               throw new IllegalArgumentException("Lua power " + key + " revision " + revision + " is older than registered revision " + previous.revision());
            }
         }

         File sourceFile = syntheticSourceFile(key);
         LuaPowerManager.Registration registration = LuaPowerManager.registerLuaPower(key.toString(), sourceFile, normalizedSource, effect, toNativeType(type));
         EliteLuaPowerProgram descriptor = new EliteLuaPowerProgram(key, revision, registration.executionPriority(), registration.hookKeys(), type, effect);
         EliteLuaPowerCatalog.Entry replacement = new EliteLuaPowerCatalog.Entry(owner, key, revision, normalizedSource, type, effect, registration, descriptor);
         this.entries.put(key, replacement);
         if (previous != null) {
            previous.registration().close();
         }

         return descriptor;
      }
   }

   EliteLuaPowerCatalog.Entry require(NamespacedKey key) {
      Objects.requireNonNull(key, "key");
      EliteLuaPowerCatalog.Entry entry = this.entries.get(key);
      if (entry == null) {
         throw new IllegalArgumentException("Unknown EliteMobs Lua power " + key);
      } else {
         return entry;
      }
   }

   List<EliteLuaPowerCatalog.Entry> resolveOwned(Plugin owner, List<NamespacedKey> orderedKeys) {
      Objects.requireNonNull(owner, "owner");
      Objects.requireNonNull(orderedKeys, "orderedKeys");
      if (new LinkedHashSet<>(orderedKeys).size() != orderedKeys.size()) {
         throw new IllegalArgumentException("A Lua power loadout cannot contain duplicate keys");
      } else {
         ArrayList<EliteLuaPowerCatalog.Entry> resolved = new ArrayList<>(orderedKeys.size());

         for (NamespacedKey key : orderedKeys) {
            EliteLuaPowerCatalog.Entry entry = this.require(Objects.requireNonNull(key, "power key"));
            if (entry.owner() != owner) {
               throw new IllegalArgumentException("Plugin " + owner.getName() + " does not own Lua power " + key);
            }

            resolved.add(entry);
         }

         return List.copyOf(resolved);
      }
   }

   void unregisterOwner(Plugin owner) {
      Objects.requireNonNull(owner, "owner");
      ArrayList<EliteLuaPowerCatalog.Entry> removed = new ArrayList<>();
      this.entries.values().removeIf(entryx -> {
         if (entryx.owner() != owner) {
            return false;
         } else {
            removed.add(entryx);
            return true;
         }
      });

      for (EliteLuaPowerCatalog.Entry entry : removed) {
         entry.registration().close();
      }
   }

   void clear() {
      ArrayList<EliteLuaPowerCatalog.Entry> removed = new ArrayList<>(this.entries.values());
      this.entries.clear();

      for (EliteLuaPowerCatalog.Entry entry : removed) {
         entry.registration().close();
      }
   }

   private static void requireOwnerNamespace(Plugin owner, NamespacedKey key) {
      String ownerNamespace = new NamespacedKey(owner, "ownership_probe").getNamespace();
      if (!ownerNamespace.equals(key.getNamespace())) {
         throw new IllegalArgumentException("Lua power key " + key + " is outside plugin namespace " + ownerNamespace);
      }
   }

   private static File syntheticSourceFile(NamespacedKey key) {
      String safeName = (key.getNamespace() + "__" + key.getKey()).replaceAll("[^a-z0-9._-]", "_");
      return new File(new File(MetadataHandler.PLUGIN.getDataFolder(), "api-lua-powers"), safeName + ".lua");
   }

   private static PowersConfigFields.PowerType toNativeType(EliteLuaPowerType type) {
      return PowersConfigFields.PowerType.valueOf(type.name());
   }

   record Entry(
      Plugin owner,
      NamespacedKey key,
      long revision,
      String source,
      EliteLuaPowerType type,
      String effect,
      LuaPowerManager.Registration registration,
      EliteLuaPowerProgram descriptor
   ) {
      private boolean matches(String candidateSource, EliteLuaPowerType candidateType, String candidateEffect) {
         return this.source.equals(candidateSource) && this.type == candidateType && Objects.equals(this.effect, candidateEffect);
      }
   }
}
