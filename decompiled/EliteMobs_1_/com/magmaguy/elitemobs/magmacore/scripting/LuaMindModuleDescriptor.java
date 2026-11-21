package com.magmaguy.elitemobs.magmacore.scripting;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public record LuaMindModuleDescriptor(String identifier, long revision, List<String> dependencies) {
   private static final Pattern IDENTIFIER = Pattern.compile("[a-z0-9._-]+:[a-z0-9/._-]+");

   public LuaMindModuleDescriptor(String identifier, long revision, List<String> dependencies) {
      Objects.requireNonNull(identifier, "identifier");
      if (!IDENTIFIER.matcher(identifier).matches()) {
         throw new IllegalArgumentException("Lua Mind module identifiers must be namespaced lower-case keys");
      } else if (revision < 1L) {
         throw new IllegalArgumentException("revision must be positive");
      } else {
         dependencies = List.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
         if (new HashSet<>(dependencies).size() != dependencies.size()) {
            throw new IllegalArgumentException("Lua Mind module dependencies cannot repeat");
         } else {
            for (String dependency : dependencies) {
               if (dependency == null || !IDENTIFIER.matcher(dependency).matches()) {
                  throw new IllegalArgumentException("Lua Mind module dependencies must be namespaced lower-case keys");
               }
            }

            this.identifier = identifier;
            this.revision = revision;
            this.dependencies = dependencies;
         }
      }
   }
}
