package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.api.mind.EliteMindModule;
import com.magmaguy.elitemobs.api.mind.EliteMindProgram;
import com.magmaguy.elitemobs.magmacore.ai.MindProgram;
import com.magmaguy.elitemobs.magmacore.scripting.LuaMindDefinition;
import com.magmaguy.elitemobs.magmacore.scripting.LuaMindModuleDescriptor;
import com.magmaguy.elitemobs.magmacore.scripting.LuaMindModuleRegistry;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

final class EliteMindCatalog {
   private final Map<NamespacedKey, EliteMindCatalog.Entry> entries = new LinkedHashMap<>();
   private final Map<String, NamespacedKey> behaviorFiles = new LinkedHashMap<>();
   private final LuaMindModuleRegistry moduleRegistry = new LuaMindModuleRegistry();
   private final Map<Plugin, LuaMindModuleRegistry.OwnerScope> moduleOwners = new IdentityHashMap<>();

   void loadBehaviors(Plugin owner) {
      Path directory = owner.getDataFolder().toPath().resolve("behaviors");

      for (String resource : List.of("basic_melee.lua", "modules/target.lua", "modules/pursuit.lua", "modules/melee.lua", "modules/wander.lua")) {
         if (!Files.exists(directory.resolve(resource))) {
            owner.saveResource("behaviors/" + resource, false);
         }
      }

      Map<Path, String> modules = new LinkedHashMap<>();
      Map<Path, String> programs = new LinkedHashMap<>();

      try (Stream<Path> paths = Files.walk(directory)) {
         Path root = directory.toRealPath();

         for (Path path : paths.filter(x$0 -> Files.isRegularFile(x$0)).sorted().toList()) {
            if (path.getFileName().toString().endsWith(".lua")) {
               try {
                  if (!path.toRealPath().startsWith(root)) {
                     Logger.warn("Behavior file escapes its directory: " + path);
                  } else {
                     (isModule(directory.relativize(path)) ? modules : programs).put(path, Files.readString(path));
                  }
               } catch (IOException var16) {
                  Logger.warn("Could not read behavior " + path + ": " + var16.getMessage());
               }
            }
         }
      } catch (IOException var18) {
         throw new IllegalStateException("Could not load behavior directory " + directory, var18);
      }

      Map<Path, String> failures = new LinkedHashMap<>();
      Map<String, Path> moduleFiles = new LinkedHashMap<>();

      boolean advanced;
      do {
         advanced = false;
         Iterator<Map.Entry<Path, String>> iterator = modules.entrySet().iterator();

         while (iterator.hasNext()) {
            Map.Entry<Path, String> file = iterator.next();

            try {
               LuaMindModuleDescriptor module = this.ownerScope(owner).registerModule(file.getKey().toString(), file.getValue());
               Path previous = moduleFiles.put(module.identifier(), file.getKey());
               if (previous != null) {
                  Logger.warn(
                     "Behavior module "
                        + module.identifier()
                        + " is declared by both "
                        + previous
                        + " and "
                        + file.getKey()
                        + "; the registered revision is "
                        + module.revision()
                  );
               }

               iterator.remove();
               failures.remove(file.getKey());
               advanced = true;
            } catch (RuntimeException var15) {
               failures.put(file.getKey(), var15.getMessage());
            }
         }
      } while (advanced && !modules.isEmpty());

      failures.forEach((filex, error) -> Logger.warn("Could not load behavior module " + filex + ": " + error));

      for (Map.Entry<Path, String> file : programs.entrySet()) {
         String reference = directory.relativize(file.getKey()).toString().replace('\\', '/');

         try {
            LuaMindDefinition definition = this.ownerScope(owner).validateProgram(reference, file.getValue());
            NamespacedKey key = Objects.requireNonNull(NamespacedKey.fromString(definition.programIdentifier()));
            requireOwnerNamespace(owner, key);
            if (this.entries.containsKey(key)) {
               throw new IllegalArgumentException("Duplicate behavior id " + key);
            }

            this.entries.put(key, new EliteMindCatalog.Entry(owner, key, definition.programRevision(), file.getValue(), definition));
            this.behaviorFiles.put(reference, key);
         } catch (RuntimeException var14) {
            Logger.warn("Could not load behavior " + reference + ": " + var14.getMessage());
         }
      }
   }

   private static boolean isModule(Path reference) {
      Path parent = reference.getParent();
      if (parent == null) {
         return false;
      } else {
         for (Path part : parent) {
            if (part.toString().equals("modules")) {
               return true;
            }
         }

         return false;
      }
   }

   EliteMindCatalog.Entry requireBehavior(String filename) {
      NamespacedKey key = this.behaviorFiles.get(filename);
      if (key == null) {
         throw new IllegalArgumentException("Unknown or invalid behavior file: " + filename);
      } else {
         return this.require(key);
      }
   }

   EliteMindModule registerModule(Plugin owner, NamespacedKey key, long revision, String source) {
      Objects.requireNonNull(owner, "owner");
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(source, "source");
      if (revision < 1L) {
         throw new IllegalArgumentException("revision must be positive");
      } else {
         requireOwnerNamespace(owner, key);
         LuaMindModuleDescriptor descriptor = this.ownerScope(owner).registerModule(key.toString(), revision, key + ".lua", source);
         return toPublicModule(descriptor);
      }
   }

   EliteMindProgram register(Plugin owner, NamespacedKey key, long revision, String source) {
      Objects.requireNonNull(owner, "owner");
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(source, "source");
      if (revision < 1L) {
         throw new IllegalArgumentException("revision must be positive");
      } else {
         requireOwnerNamespace(owner, key);
         EliteMindCatalog.Entry current = this.entries.get(key);
         if (current != null) {
            if (current.owner() != owner) {
               throw new IllegalStateException("Mind program key is already owned by " + current.owner().getName() + ": " + key);
            }

            if (current.revision() == revision && current.source().equals(source)) {
               return current.descriptor();
            }

            if (revision <= current.revision()) {
               throw new IllegalArgumentException("Mind program replacements require a newer revision: " + key);
            }
         }

         LuaMindDefinition definition = this.ownerScope(owner).validateProgram(key + ".lua", source);
         if (!definition.programIdentifier().equals(key.toString())) {
            throw new IllegalArgumentException("Lua mind id must match its catalog key " + key);
         } else if (definition.programRevision() != revision) {
            throw new IllegalArgumentException("Lua mind revision must match catalog revision " + revision);
         } else {
            EliteMindCatalog.Entry registered = new EliteMindCatalog.Entry(owner, key, revision, source, definition);
            this.entries.put(key, registered);
            return registered.descriptor();
         }
      }
   }

   EliteMindCatalog.Entry require(NamespacedKey key) {
      Objects.requireNonNull(key, "key");
      EliteMindCatalog.Entry entry = this.entries.get(key);
      if (entry == null) {
         throw new IllegalArgumentException("Unknown EliteMobs mind program: " + key);
      } else {
         return entry;
      }
   }

   void unregisterOwner(Plugin owner) {
      Objects.requireNonNull(owner, "owner");
      Iterator<EliteMindCatalog.Entry> iterator = this.entries.values().iterator();

      while (iterator.hasNext()) {
         if (iterator.next().owner() == owner) {
            iterator.remove();
         }
      }

      LuaMindModuleRegistry.OwnerScope moduleOwner = this.moduleOwners.remove(owner);
      if (moduleOwner != null) {
         moduleOwner.close();
      }

      this.behaviorFiles.values().removeIf(key -> !this.entries.containsKey(key));
   }

   void clear() {
      this.entries.clear();
      this.behaviorFiles.clear();

      for (LuaMindModuleRegistry.OwnerScope owner : new ArrayList<>(this.moduleOwners.values())) {
         owner.close();
      }

      this.moduleOwners.clear();
      this.moduleRegistry.close();
   }

   private LuaMindModuleRegistry.OwnerScope ownerScope(Plugin owner) {
      LuaMindModuleRegistry.OwnerScope current = this.moduleOwners.get(owner);
      if (current != null) {
         return current;
      } else {
         String namespace = new NamespacedKey(owner, "ownership_probe").getNamespace();
         LuaMindModuleRegistry.OwnerScope opened = this.moduleRegistry.openOwner(namespace);
         this.moduleOwners.put(owner, opened);
         return opened;
      }
   }

   private static void requireOwnerNamespace(Plugin owner, NamespacedKey key) {
      String ownerNamespace = new NamespacedKey(owner, "ownership_probe").getNamespace();
      if (!ownerNamespace.equals(key.getNamespace())) {
         throw new IllegalArgumentException("Mind program namespace " + key.getNamespace() + " does not belong to plugin " + owner.getName());
      }
   }

   private static EliteMindModule toPublicModule(LuaMindModuleDescriptor descriptor) {
      NamespacedKey key = NamespacedKey.fromString(descriptor.identifier());
      if (key == null) {
         throw new IllegalStateException("MagmaCore returned an invalid Mind module key: " + descriptor.identifier());
      } else {
         List<NamespacedKey> dependencies = descriptor.dependencies().stream().map(identifier -> {
            NamespacedKey dependency = NamespacedKey.fromString(identifier);
            if (dependency == null) {
               throw new IllegalStateException("MagmaCore returned an invalid Mind module dependency: " + identifier);
            } else {
               return dependency;
            }
         }).toList();
         return new EliteMindModule(key, descriptor.revision(), dependencies);
      }
   }

   record Entry(Plugin owner, NamespacedKey key, long revision, String source, LuaMindDefinition definition) implements EliteMindProgramEntry {
      Entry(Plugin owner, NamespacedKey key, long revision, String source, LuaMindDefinition definition) {
         Objects.requireNonNull(owner, "owner");
         Objects.requireNonNull(key, "key");
         Objects.requireNonNull(source, "source");
         Objects.requireNonNull(definition, "definition");
         this.owner = owner;
         this.key = key;
         this.revision = revision;
         this.source = source;
         this.definition = definition;
      }

      @Override
      public EliteMindProgram descriptor() {
         return new EliteMindProgram(
            this.key,
            this.revision,
            this.definition.resolvedModules().stream().map(EliteMindCatalog::toPublicModule).toList(),
            this.definition.programFingerprint()
         );
      }

      @Override
      public MindProgram instantiate() {
         return this.definition.instantiate();
      }
   }
}
