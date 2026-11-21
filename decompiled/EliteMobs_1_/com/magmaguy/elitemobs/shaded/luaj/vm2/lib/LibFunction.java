package com.magmaguy.elitemobs.shaded.luaj.vm2.lib;

import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaError;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaFunction;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;

public abstract class LibFunction extends LuaFunction {
   protected int opcode;
   protected String name;

   protected LibFunction() {
   }

   public String tojstring() {
      return this.name != null ? this.name : super.tojstring();
   }

   protected void bind(LuaValue var1, Class var2, String[] var3) {
      this.bind(var1, var2, var3, 0);
   }

   protected void bind(LuaValue var1, Class var2, String[] var3, int var4) {
      try {
         int var5 = 0;

         for (int var6 = var3.length; var5 < var6; var5++) {
            LibFunction var7 = (LibFunction)var2.newInstance();
            var7.opcode = var4 + var5;
            var7.name = var3[var5];
            var1.set(var7.name, var7);
         }
      } catch (Exception var8) {
         throw new LuaError("bind failed: " + var8);
      }
   }

   protected static LuaValue[] newupe() {
      return new LuaValue[1];
   }

   protected static LuaValue[] newupn() {
      return new LuaValue[]{NIL};
   }

   protected static LuaValue[] newupl(LuaValue var0) {
      return new LuaValue[]{var0};
   }

   public LuaValue call() {
      return argerror(1, "value");
   }

   public LuaValue call(LuaValue var1) {
      return this.call();
   }

   public LuaValue call(LuaValue var1, LuaValue var2) {
      return this.call(var1);
   }

   public LuaValue call(LuaValue var1, LuaValue var2, LuaValue var3) {
      return this.call(var1, var2);
   }

   public LuaValue call(LuaValue var1, LuaValue var2, LuaValue var3, LuaValue var4) {
      return this.call(var1, var2, var3);
   }

   public Varargs invoke(Varargs var1) {
      switch (var1.narg()) {
         case 0:
            return this.call();
         case 1:
            return this.call(var1.arg1());
         case 2:
            return this.call(var1.arg1(), var1.arg(2));
         case 3:
            return this.call(var1.arg1(), var1.arg(2), var1.arg(3));
         default:
            return this.call(var1.arg1(), var1.arg(2), var1.arg(3), var1.arg(4));
      }
   }
}
