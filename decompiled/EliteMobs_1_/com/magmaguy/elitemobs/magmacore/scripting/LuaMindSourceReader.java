package com.magmaguy.elitemobs.magmacore.scripting;

import com.magmaguy.elitemobs.shaded.luaj.vm2.Globals;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class LuaMindSourceReader {
   private LuaMindSourceReader() {
   }

   static LuaTable evaluateTable(Globals globals, String sourceName, String source, String expectedKind) {
      LuaTable environment = isolatedEnvironment(globals);
      LuaValue result = LuaExecutionBudget.run(() -> globals.load(source, sourceName, environment).call());
      if (result instanceof LuaTable table) {
         LuaMindProgramCompiler.requireKind(table, expectedKind);
         return table;
      } else {
         throw new IllegalArgumentException("Mind script " + sourceName + " must return ai." + expectedKind + " { ... }");
      }
   }

   private static LuaTable isolatedEnvironment(Globals globals) {
      LuaTable environment = new LuaTable();
      LuaTable metatable = new LuaTable();
      metatable.set("__index", globals);
      environment.setmetatable(metatable);
      environment.set("_G", environment);
      return environment;
   }

   static LuaMindProgramMetadata inspectProgram(String sourceName, String source) {
      Globals globals = LuaEnvironmentFactory.createGlobals();
      LuaTable table = evaluateTable(globals, sourceName, source, "program");
      return programMetadata(table);
   }

   static LuaMindProgramMetadata programMetadata(LuaTable table) {
      LuaMindProgramCompiler.requireKind(table, "program");
      return new LuaMindProgramMetadata(
         LuaMindProgramCompiler.requiredString(table, "id"),
         LuaMindProgramCompiler.requiredPositiveLong(table, "revision"),
         stringArray(table.get("modules"), "modules")
      );
   }

   static List<String> stringArray(LuaValue value, String field) {
      if (value.isnil()) {
         return List.of();
      } else {
         LuaTable table = value.checktable();
         int length = table.length();
         List<String> values = new ArrayList<>(length);
         Set<String> unique = new HashSet<>();

         for (int index = 1; index <= length; index++) {
            LuaValue entry = table.get(index);
            if (!entry.isstring()) {
               throw new IllegalArgumentException("Mind field '" + field + "' must be an array of module identifiers");
            }

            String identifier = entry.checkjstring();
            if (!unique.add(identifier)) {
               throw new IllegalArgumentException("Mind field '" + field + "' contains duplicate module " + identifier);
            }

            values.add(identifier);
         }

         LuaValue key = LuaValue.NIL;
         int entries = 0;

         do {
            Varargs next = table.next(key);
            key = next.arg1();
            if (key.isnil()) {
               if (entries != length) {
                  throw new IllegalArgumentException("Mind field '" + field + "' must be a dense array");
               }

               return List.copyOf(values);
            }

            entries++;
         } while (key.isint() && key.checkint() >= 1 && key.checkint() <= length);

         throw new IllegalArgumentException("Mind field '" + field + "' must be a dense array");
      }
   }
}
