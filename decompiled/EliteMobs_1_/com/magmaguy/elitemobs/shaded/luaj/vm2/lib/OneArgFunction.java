package com.magmaguy.elitemobs.shaded.luaj.vm2.lib;

import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;

public abstract class OneArgFunction extends LibFunction {
   public abstract LuaValue call(LuaValue var1);

   public final LuaValue call() {
      return this.call(NIL);
   }

   public final LuaValue call(LuaValue var1, LuaValue var2) {
      return this.call(var1);
   }

   public LuaValue call(LuaValue var1, LuaValue var2, LuaValue var3) {
      return this.call(var1);
   }

   public Varargs invoke(Varargs var1) {
      return this.call(var1.arg1());
   }
}
