package com.magmaguy.elitemobs.shaded.luaj.vm2.lib;

import com.magmaguy.elitemobs.shaded.luaj.vm2.Globals;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaFunction;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaThread;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;

public class CoroutineLib extends TwoArgFunction {
   static int coroutine_count = 0;
   Globals globals;

   public LuaValue call(LuaValue var1, LuaValue var2) {
      this.globals = var2.checkglobals();
      LuaTable var3 = new LuaTable();
      var3.set("create", new CoroutineLib.create());
      var3.set("resume", new CoroutineLib.resume());
      var3.set("running", new CoroutineLib.running());
      var3.set("status", new CoroutineLib.status());
      var3.set("yield", new CoroutineLib.yield());
      var3.set("wrap", new CoroutineLib.wrap());
      var2.set("coroutine", var3);
      var2.get("package").get("loaded").set("coroutine", var3);
      return var3;
   }

   final class create extends LibFunction {
      public LuaValue call(LuaValue var1) {
         return new LuaThread(CoroutineLib.this.globals, var1.checkfunction());
      }
   }

   final class resume extends VarArgFunction {
      public Varargs invoke(Varargs var1) {
         LuaThread var2 = var1.checkthread(1);
         return var2.resume(var1.subargs(2));
      }
   }

   final class running extends VarArgFunction {
      public Varargs invoke(Varargs var1) {
         LuaThread var2 = CoroutineLib.this.globals.running;
         return varargsOf(var2, valueOf(var2.isMainThread()));
      }
   }

   static final class status extends LibFunction {
      public LuaValue call(LuaValue var1) {
         LuaThread var2 = var1.checkthread();
         return valueOf(var2.getStatus());
      }
   }

   final class wrap extends LibFunction {
      public LuaValue call(LuaValue var1) {
         LuaFunction var2 = var1.checkfunction();
         LuaThread var3 = new LuaThread(CoroutineLib.this.globals, var2);
         return CoroutineLib.this.new wrapper(var3);
      }
   }

   final class wrapper extends VarArgFunction {
      final LuaThread luathread;

      wrapper(LuaThread var2) {
         this.luathread = var2;
      }

      public Varargs invoke(Varargs var1) {
         Varargs var2 = this.luathread.resume(var1);
         return (Varargs)(var2.arg1().toboolean() ? var2.subargs(2) : error(var2.arg(2).tojstring()));
      }
   }

   final class yield extends VarArgFunction {
      public Varargs invoke(Varargs var1) {
         return CoroutineLib.this.globals.yield(var1);
      }
   }
}
