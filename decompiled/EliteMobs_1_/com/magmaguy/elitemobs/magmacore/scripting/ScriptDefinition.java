package com.magmaguy.elitemobs.magmacore.scripting;

import com.magmaguy.elitemobs.shaded.luaj.vm2.Globals;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaClosure;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaFunction;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Prototype;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import lombok.Generated;

public class ScriptDefinition {
   private final String fileName;
   private final File sourceFile;
   private final String source;
   private final int priority;
   private final Set<ScriptHook> hooks;
   private final Prototype prototype;

   public ScriptDefinition(String fileName, File sourceFile, String source, int priority, Set<ScriptHook> hooks) {
      this(fileName, sourceFile, source, priority, hooks, compile(fileName, source, LuaEnvironmentFactory.createGlobals()));
   }

   private ScriptDefinition(String fileName, File sourceFile, String source, int priority, Set<ScriptHook> hooks, Prototype prototype) {
      this.fileName = fileName;
      this.sourceFile = sourceFile;
      this.source = source;
      this.priority = priority;
      this.hooks = Set.copyOf(hooks);
      this.prototype = prototype;
   }

   public static ScriptDefinition validate(String fileName, File sourceFile, String source, ScriptProvider provider) {
      Globals globals = LuaEnvironmentFactory.createGlobals();
      Prototype prototype = compile(fileName, source, globals);
      LuaTable scriptTable = evaluate(fileName, prototype, globals);
      int apiVersion = extractIntField(scriptTable, "api_version", fileName, true);
      if (apiVersion != 1) {
         throw new IllegalArgumentException("Script " + fileName + " has unsupported api_version " + apiVersion + ". Expected 1.");
      } else {
         int priority = extractIntField(scriptTable, "priority", fileName, false);
         Set<ScriptHook> hooks = new HashSet<>();
         LuaValue currentKey = LuaValue.NIL;

         while (true) {
            Varargs next = scriptTable.next(currentKey);
            currentKey = next.arg1();
            if (currentKey.isnil()) {
               return new ScriptDefinition(fileName, sourceFile, source, priority, hooks, prototype);
            }

            String key = currentKey.checkjstring();
            if (!"api_version".equals(key) && !"priority".equals(key)) {
               ScriptHook hook = provider.resolveHook(key);
               if (hook == null) {
                  throw new IllegalArgumentException("Script " + fileName + " contains unsupported key '" + key + "'.");
               }

               if (!(scriptTable.get(key) instanceof LuaFunction)) {
                  throw new IllegalArgumentException("Script " + fileName + " key '" + key + "' must be a function.");
               }

               hooks.add(hook);
            }
         }
      }
   }

   public LuaTable instantiate() {
      return evaluate(this.fileName, this.prototype, LuaEnvironmentFactory.createGlobals());
   }

   public boolean supportsHook(ScriptHook hook) {
      return hook != null && this.hooks.contains(hook);
   }

   private static Prototype compile(String fileName, String source, Globals globals) {
      try {
         return globals.compilePrototype(new StringReader(source), fileName);
      } catch (IOException var4) {
         throw new IllegalArgumentException("Could not compile script " + fileName, var4);
      }
   }

   private static LuaTable evaluate(String fileName, Prototype prototype, Globals globals) {
      LuaValue chunk = new LuaClosure(prototype, globals);
      LuaValue result = LuaExecutionBudget.run(chunk::call);
      if (result instanceof LuaTable scriptTable) {
         return scriptTable;
      } else {
         throw new IllegalArgumentException("Script " + fileName + " must return a table.");
      }
   }

   private static int extractIntField(LuaTable table, String key, String fileName, boolean required) {
      LuaValue value = table.get(key);
      if (value.isnil()) {
         if (required) {
            throw new IllegalArgumentException("Script " + fileName + " is missing required field '" + key + "'.");
         } else {
            return 0;
         }
      } else if (!value.isnumber()) {
         throw new IllegalArgumentException("Script " + fileName + " field '" + key + "' must be a number.");
      } else {
         return value.toint();
      }
   }

   @Generated
   public String getFileName() {
      return this.fileName;
   }

   @Generated
   public File getSourceFile() {
      return this.sourceFile;
   }

   @Generated
   public String getSource() {
      return this.source;
   }

   @Generated
   public int getPriority() {
      return this.priority;
   }

   @Generated
   public Set<ScriptHook> getHooks() {
      return this.hooks;
   }
}
