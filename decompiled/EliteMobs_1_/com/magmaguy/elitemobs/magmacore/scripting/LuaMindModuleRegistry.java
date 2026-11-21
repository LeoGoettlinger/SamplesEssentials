package com.magmaguy.elitemobs.magmacore.scripting;

import com.magmaguy.elitemobs.shaded.luaj.vm2.Globals;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public final class LuaMindModuleRegistry implements AutoCloseable {
   public static final int MAX_MODULES_PER_OWNER = 256;
   public static final int MAX_DEPENDENCIES_PER_MODULE = 32;
   public static final int MAX_RESOLVED_MODULES_PER_PROGRAM = 64;
   public static final int MAX_SOURCE_CHARACTERS = 1000000;
   private static final Pattern NAMESPACE = Pattern.compile("[a-z0-9._-]+");
   private static final Pattern IDENTIFIER = Pattern.compile("[a-z0-9._-]+:[a-z0-9/._-]+");
   private final Map<String, LuaMindModuleRegistry.OwnerScope> owners = new HashMap<>();
   private boolean closed;

   public synchronized LuaMindModuleRegistry.OwnerScope openOwner(String namespace) {
      this.requireOpen();
      requireNamespace(namespace);
      if (this.owners.containsKey(namespace)) {
         throw new IllegalStateException("Lua Mind module namespace already has an active owner: " + namespace);
      } else {
         LuaMindModuleRegistry.OwnerScope owner = new LuaMindModuleRegistry.OwnerScope(namespace);
         this.owners.put(namespace, owner);
         return owner;
      }
   }

   @Override
   public synchronized void close() {
      if (!this.closed) {
         this.closed = true;

         for (LuaMindModuleRegistry.OwnerScope owner : List.copyOf(this.owners.values())) {
            owner.closeFromRegistry();
         }

         this.owners.clear();
      }
   }

   private void requireOpen() {
      if (this.closed) {
         throw new IllegalStateException("Lua Mind module registry is closed");
      }
   }

   private static void requireNamespace(String namespace) {
      if (namespace == null || !NAMESPACE.matcher(namespace).matches()) {
         throw new IllegalArgumentException("Lua Mind module owner namespace must be lower-case and path-safe");
      }
   }

   private static String namespaceOf(String identifier) {
      if (identifier != null && IDENTIFIER.matcher(identifier).matches()) {
         int separator = identifier.indexOf(58);
         return identifier.substring(0, separator);
      } else {
         throw new IllegalArgumentException("Lua Mind module identifiers must be namespaced lower-case keys: " + identifier);
      }
   }

   private static List<LuaMindModuleSource> resolve(Map<String, LuaMindModuleSource> modules, List<String> roots, String consumer) {
      Map<String, LuaMindModuleRegistry.Visit> visits = new HashMap<>();
      List<String> path = new ArrayList<>();
      LinkedHashMap<String, LuaMindModuleSource> resolved = new LinkedHashMap<>();

      for (String root : roots) {
         resolve(root, modules, visits, path, resolved, consumer);
      }

      return List.copyOf(resolved.values());
   }

   private static void resolve(
      String identifier,
      Map<String, LuaMindModuleSource> modules,
      Map<String, LuaMindModuleRegistry.Visit> visits,
      List<String> path,
      LinkedHashMap<String, LuaMindModuleSource> resolved,
      String consumer
   ) {
      LuaMindModuleRegistry.Visit visit = visits.get(identifier);
      if (visit != LuaMindModuleRegistry.Visit.COMPLETE) {
         if (visit == LuaMindModuleRegistry.Visit.ACTIVE) {
            int cycleStart = path.indexOf(identifier);
            List<String> cycle = new ArrayList<>(path.subList(cycleStart, path.size()));
            cycle.add(identifier);
            throw new IllegalArgumentException(consumer + " contains a module dependency cycle: " + String.join(" -> ", cycle));
         } else {
            LuaMindModuleSource module = modules.get(identifier);
            if (module == null) {
               throw new IllegalArgumentException(consumer + " requires missing Lua Mind module " + identifier);
            } else {
               visits.put(identifier, LuaMindModuleRegistry.Visit.ACTIVE);
               path.add(identifier);

               for (String dependency : module.dependencies()) {
                  resolve(dependency, modules, visits, path, resolved, consumer);
               }

               path.removeLast();
               visits.put(identifier, LuaMindModuleRegistry.Visit.COMPLETE);
               resolved.put(identifier, module);
               if (resolved.size() > 64) {
                  throw new IllegalArgumentException(consumer + " exceeds the resolved module limit of 64");
               }
            }
         }
      }
   }

   private static void validateComposition(String namespace, String root, List<LuaMindModuleSource> modules) {
      LuaExecutionBudget.run(() -> {
         Globals globals = LuaEnvironmentFactory.createGlobals();
         List<LuaTable> tables = new ArrayList<>(modules.size());

         for (LuaMindModuleSource module : modules) {
            tables.add(module.evaluate(globals));
         }

         LuaTable syntheticProgram = new LuaTable();
         syntheticProgram.set("__mind_kind", "program");
         syntheticProgram.set("id", namespace + ":module_validation/" + Integer.toUnsignedString(root.hashCode(), 36));
         syntheticProgram.set("revision", 1.0);
         LuaMindProgramCompiler.compile(syntheticProgram, tables);
         return null;
      });
   }

   public final class OwnerScope implements AutoCloseable {
      private final String namespace;
      private Map<String, LuaMindModuleSource> modules = new LinkedHashMap<>();
      private boolean ownerClosed;

      private OwnerScope(String namespace) {
         this.namespace = namespace;
      }

      public String namespace() {
         return this.namespace;
      }

      public LuaMindModuleDescriptor registerModule(String sourceName, String source) {
         return this.registerModule(null, 0L, sourceName, source);
      }

      public LuaMindModuleDescriptor registerModule(String expectedIdentifier, long expectedRevision, String sourceName, String source) {
         synchronized (LuaMindModuleRegistry.this) {
            this.requireOwnerOpen();
            if (expectedIdentifier != null) {
               this.requireOwnedIdentifier(expectedIdentifier);
               if (expectedRevision < 1L) {
                  throw new IllegalArgumentException("Expected module revision must be positive");
               }
            }

            this.requireSourceSize(sourceName, source);
            LuaMindModuleSource candidate = LuaMindModuleSource.inspect(sourceName, source);
            this.requireOwnedIdentifier(candidate.identifier());
            if (expectedIdentifier == null || expectedIdentifier.equals(candidate.identifier()) && expectedRevision == candidate.revision()) {
               if (candidate.dependencies().size() > 32) {
                  throw new IllegalArgumentException("Lua Mind module " + candidate.identifier() + " exceeds the dependency limit of 32");
               } else {
                  for (String dependency : candidate.dependencies()) {
                     this.requireOwnedIdentifier(dependency);
                  }

                  LuaMindModuleSource current = this.modules.get(candidate.identifier());
                  if (current != null) {
                     if (current.revision() == candidate.revision() && current.source().equals(candidate.source())) {
                        return current.descriptor();
                     }

                     if (candidate.revision() <= current.revision()) {
                        throw new IllegalArgumentException("Lua Mind module replacements require a newer revision: " + candidate.identifier());
                     }
                  }

                  Map<String, LuaMindModuleSource> proposed = new LinkedHashMap<>(this.modules);
                  proposed.put(candidate.identifier(), candidate);
                  if (proposed.size() > 256) {
                     throw new IllegalArgumentException("Lua Mind module owner exceeds the module limit of 256");
                  } else {
                     this.validateCatalog(proposed, candidate.identifier());
                     this.modules = proposed;
                     return candidate.descriptor();
                  }
               }
            } else {
               throw new IllegalArgumentException(
                  "Lua Mind module identity does not match the caller catalog entry " + expectedIdentifier + "@" + expectedRevision
               );
            }
         }
      }

      public LuaMindDefinition validateProgram(String sourceName, String source) {
         synchronized (LuaMindModuleRegistry.this) {
            this.requireOwnerOpen();
            this.requireSourceSize(sourceName, source);
            LuaMindProgramMetadata metadata = LuaMindSourceReader.inspectProgram(sourceName, source);
            this.requireOwnedIdentifier(metadata.identifier());

            for (String module : metadata.modules()) {
               this.requireOwnedIdentifier(module);
            }

            List<LuaMindModuleSource> resolved = LuaMindModuleRegistry.resolve(this.modules, metadata.modules(), "Mind program " + metadata.identifier());
            return LuaMindDefinition.validateResolved(sourceName, source, metadata, resolved);
         }
      }

      public Optional<LuaMindModuleDescriptor> module(String identifier) {
         synchronized (LuaMindModuleRegistry.this) {
            this.requireOwnerOpen();
            LuaMindModuleSource module = this.modules.get(identifier);
            return module == null ? Optional.empty() : Optional.of(module.descriptor());
         }
      }

      public List<LuaMindModuleDescriptor> modules() {
         synchronized (LuaMindModuleRegistry.this) {
            this.requireOwnerOpen();
            return this.modules.values().stream().sorted(Comparator.comparing(LuaMindModuleSource::identifier)).map(LuaMindModuleSource::descriptor).toList();
         }
      }

      @Override
      public void close() {
         synchronized (LuaMindModuleRegistry.this) {
            if (!this.ownerClosed) {
               this.ownerClosed = true;
               this.modules = Map.of();
               LuaMindModuleRegistry.this.owners.remove(this.namespace, this);
            }
         }
      }

      private void closeFromRegistry() {
         this.ownerClosed = true;
         this.modules = Map.of();
      }

      private void requireOwnerOpen() {
         LuaMindModuleRegistry.this.requireOpen();
         if (this.ownerClosed) {
            throw new IllegalStateException("Lua Mind module owner is closed: " + this.namespace);
         }
      }

      private void requireOwnedIdentifier(String identifier) {
         if (!this.namespace.equals(LuaMindModuleRegistry.namespaceOf(identifier))) {
            throw new IllegalArgumentException("Lua Mind identifier " + identifier + " does not belong to owner namespace " + this.namespace);
         }
      }

      private void validateCatalog(Map<String, LuaMindModuleSource> proposed, String changedIdentifier) {
         Set<String> affected = new HashSet<>();
         affected.add(changedIdentifier);

         boolean expanded;
         do {
            expanded = false;

            for (LuaMindModuleSource module : proposed.values()) {
               if (!affected.contains(module.identifier()) && module.dependencies().stream().anyMatch(affected::contains)) {
                  affected.add(module.identifier());
                  expanded = true;
               }
            }
         } while (expanded);

         for (String root : affected.stream().sorted().toList()) {
            List<LuaMindModuleSource> composition = LuaMindModuleRegistry.resolve(proposed, List.of(root), "Mind module " + root);
            LuaMindModuleRegistry.validateComposition(this.namespace, root, composition);
         }
      }

      private void requireSourceSize(String sourceName, String source) {
         Objects.requireNonNull(sourceName, "sourceName");
         Objects.requireNonNull(source, "source");
         if (source.length() > 1000000) {
            throw new IllegalArgumentException("Lua Mind source " + sourceName + " exceeds the character limit of 1000000");
         }
      }
   }

   private static enum Visit {
      ACTIVE,
      COMPLETE;
   }
}
