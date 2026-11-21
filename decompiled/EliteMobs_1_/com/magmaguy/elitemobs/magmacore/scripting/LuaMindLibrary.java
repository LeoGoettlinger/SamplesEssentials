package com.magmaguy.elitemobs.magmacore.scripting;

import com.magmaguy.elitemobs.shaded.luaj.vm2.Globals;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.VarArgFunction;

public final class LuaMindLibrary {
   private LuaMindLibrary() {
   }

   public static void install(Globals globals) {
      LuaTable ai = new LuaTable();
      ai.set("program", taggedConstructor("program"));
      ai.set("module", taggedConstructor("module"));
      ai.set("sensor", taggedConstructor("sensor"));
      ai.set("behavior", taggedConstructor("behavior"));
      LuaTable controls = new LuaTable();
      controls.set("move", "move");
      controls.set("look", "look");
      controls.set("jump", "jump");
      controls.set("target", "target");
      controls.set("attack", "attack");
      controls.set("use_item", "use_item");
      controls.set("action", "action");
      ai.set("controls", controls);
      globals.set("ai", ai);
   }

   private static LuaValue taggedConstructor(final String kind) {
      return new VarArgFunction() {
         @Override
         public Varargs invoke(Varargs args) {
            LuaTable table = args.checktable(1);
            table.set("__mind_kind", kind);
            return table;
         }
      };
   }
}
