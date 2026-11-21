package com.magmaguy.elitemobs.magmacore.scripting;

import com.magmaguy.elitemobs.shaded.luaj.vm2.Globals;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import java.util.List;
import java.util.Objects;

record LuaMindModuleSource(String sourceName, String source, String identifier, long revision, List<String> dependencies) {
   LuaMindModuleSource(String sourceName, String source, String identifier, long revision, List<String> dependencies) {
      Objects.requireNonNull(sourceName, "sourceName");
      Objects.requireNonNull(source, "source");
      Objects.requireNonNull(identifier, "identifier");
      if (revision < 1L) {
         throw new IllegalArgumentException("revision must be positive");
      } else {
         dependencies = List.copyOf(dependencies);
         this.sourceName = sourceName;
         this.source = source;
         this.identifier = identifier;
         this.revision = revision;
         this.dependencies = dependencies;
      }
   }

   static LuaMindModuleSource inspect(String sourceName, String source) {
      Objects.requireNonNull(sourceName, "sourceName");
      Objects.requireNonNull(source, "source");
      Globals globals = LuaEnvironmentFactory.createGlobals();
      LuaTable table = LuaMindSourceReader.evaluateTable(globals, sourceName, source, "module");
      return new LuaMindModuleSource(
         sourceName,
         source,
         LuaMindProgramCompiler.requiredString(table, "id"),
         LuaMindProgramCompiler.requiredPositiveLong(table, "revision"),
         LuaMindSourceReader.stringArray(table.get("dependencies"), "dependencies")
      );
   }

   LuaTable evaluate(Globals globals) {
      LuaTable table = LuaMindSourceReader.evaluateTable(globals, this.sourceName, this.source, "module");
      String evaluatedIdentifier = LuaMindProgramCompiler.requiredString(table, "id");
      long evaluatedRevision = LuaMindProgramCompiler.requiredPositiveLong(table, "revision");
      List<String> evaluatedDependencies = LuaMindSourceReader.stringArray(table.get("dependencies"), "dependencies");
      if (this.identifier.equals(evaluatedIdentifier) && this.revision == evaluatedRevision && this.dependencies.equals(evaluatedDependencies)) {
         return table;
      } else {
         throw new IllegalArgumentException("Mind module " + this.sourceName + " produced non-deterministic metadata");
      }
   }

   LuaMindModuleDescriptor descriptor() {
      return new LuaMindModuleDescriptor(this.identifier, this.revision, this.dependencies);
   }
}
