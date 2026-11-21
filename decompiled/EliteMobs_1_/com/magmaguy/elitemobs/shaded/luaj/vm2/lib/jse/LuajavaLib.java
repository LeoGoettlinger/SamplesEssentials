package com.magmaguy.elitemobs.shaded.luaj.vm2.lib.jse;

import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaError;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.VarArgFunction;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class LuajavaLib extends VarArgFunction {
   static final int INIT = 0;
   static final int BINDCLASS = 1;
   static final int NEWINSTANCE = 2;
   static final int NEW = 3;
   static final int CREATEPROXY = 4;
   static final int LOADLIB = 5;
   static final String[] NAMES = new String[]{"bindClass", "newInstance", "new", "createProxy", "loadLib"};
   static final int METHOD_MODIFIERS_VARARGS = 128;

   public Varargs invoke(Varargs var1) {
      try {
         switch (this.opcode) {
            case 0:
               LuaValue var13 = var1.arg(2);
               LuaTable var16 = new LuaTable();
               this.bind(var16, this.getClass(), NAMES, 1);
               var13.set("luajava", var16);
               var13.get("package").get("loaded").set("luajava", var16);
               return var16;
            case 1:
               Class var12 = this.classForName(var1.checkjstring(1));
               return JavaClass.forClass(var12);
            case 2:
            case 3:
               LuaValue var11 = var1.checkvalue(1);
               Class var15 = this.opcode == 2
                  ? this.classForName(var11.tojstring())
                  : (Class)var11.checkuserdata(class$java$lang$Class == null ? (class$java$lang$Class = class$("java.lang.Class")) : class$java$lang$Class);
               Varargs var18 = var1.subargs(2);
               return JavaClass.forClass(var15).getConstructor().invoke(var18);
            case 4:
               int var10 = var1.narg() - 1;
               if (var10 <= 0) {
                  throw new LuaError("no interfaces");
               }

               LuaTable var14 = var1.checktable(var10 + 1);
               Class[] var17 = new Class[var10];

               for (int var19 = 0; var19 < var10; var19++) {
                  var17[var19] = this.classForName(var1.checkjstring(var19 + 1));
               }

               LuajavaLib.ProxyInvocationHandler var20 = new LuajavaLib.ProxyInvocationHandler(var14);
               Object var21 = Proxy.newProxyInstance(this.getClass().getClassLoader(), var17, var20);
               return LuaValue.userdataOf(var21);
            case 5:
               String var2 = var1.checkjstring(1);
               String var3 = var1.checkjstring(2);
               Class var4 = this.classForName(var2);
               Method var5 = var4.getMethod(var3);
               Object var6 = var5.invoke(var4);
               if (var6 instanceof LuaValue) {
                  return (LuaValue)var6;
               }

               return NIL;
            default:
               throw new LuaError("not yet supported: " + this);
         }
      } catch (LuaError var7) {
         throw var7;
      } catch (InvocationTargetException var8) {
         throw new LuaError(var8.getTargetException());
      } catch (Exception var9) {
         throw new LuaError(var9);
      }
   }

   protected Class classForName(String var1) throws ClassNotFoundException {
      return Class.forName(var1, true, ClassLoader.getSystemClassLoader());
   }

   private static final class ProxyInvocationHandler implements InvocationHandler {
      private final LuaValue lobj;

      private ProxyInvocationHandler(LuaValue var1) {
         this.lobj = var1;
      }

      public Object invoke(Object var1, Method var2, Object[] var3) throws Throwable {
         String var4 = var2.getName();
         LuaValue var5 = this.lobj.get(var4);
         if (var5.isnil()) {
            return null;
         } else {
            boolean var6 = (var2.getModifiers() & 128) != 0;
            int var7 = var3 != null ? var3.length : 0;
            LuaValue[] var8;
            if (var6) {
               Object var9 = var3[--var7];
               int var10 = Array.getLength(var9);
               var8 = new LuaValue[var7 + var10];

               for (int var11 = 0; var11 < var7; var11++) {
                  var8[var11] = CoerceJavaToLua.coerce(var3[var11]);
               }

               for (int var15 = 0; var15 < var10; var15++) {
                  var8[var15 + var7] = CoerceJavaToLua.coerce(Array.get(var9, var15));
               }
            } else {
               var8 = new LuaValue[var7];

               for (int var13 = 0; var13 < var7; var13++) {
                  var8[var13] = CoerceJavaToLua.coerce(var3[var13]);
               }
            }

            LuaValue var14 = var5.invoke(var8).arg1();
            return CoerceLuaToJava.coerce(var14, var2.getReturnType());
         }
      }
   }
}
