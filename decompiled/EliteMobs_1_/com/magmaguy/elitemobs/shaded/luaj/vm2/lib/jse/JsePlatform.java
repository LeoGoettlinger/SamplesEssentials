package com.magmaguy.elitemobs.shaded.luaj.vm2.lib.jse;

import com.magmaguy.elitemobs.shaded.luaj.vm2.Globals;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LoadState;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.compiler.LuaC;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.Bit32Lib;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.CoroutineLib;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.DebugLib;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.PackageLib;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.StringLib;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.TableLib;

public class JsePlatform {
   public static Globals standardGlobals() {
      Globals var0 = new Globals();
      var0.load(new JseBaseLib());
      var0.load(new PackageLib());
      var0.load(new Bit32Lib());
      var0.load(new TableLib());
      var0.load(new StringLib());
      var0.load(new CoroutineLib());
      var0.load(new JseMathLib());
      var0.load(new JseIoLib());
      var0.load(new JseOsLib());
      var0.load(new LuajavaLib());
      LoadState.install(var0);
      LuaC.install(var0);
      return var0;
   }

   public static Globals debugGlobals() {
      Globals var0 = standardGlobals();
      var0.load(new DebugLib());
      return var0;
   }

   public static void luaMain(LuaValue var0, String[] var1) {
      Globals var2 = standardGlobals();
      int var3 = var1.length;
      LuaValue[] var4 = new LuaValue[var1.length];

      for (int var5 = 0; var5 < var3; var5++) {
         var4[var5] = LuaValue.valueOf(var1[var5]);
      }

      LuaTable var6 = LuaValue.listOf(var4);
      var6.set("n", var3);
      var2.set("arg", var6);
      var0.initupvalue1(var2);
      var0.invoke(LuaValue.varargsOf(var4));
   }
}
