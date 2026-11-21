package com.magmaguy.elitemobs.shaded.luaj.vm2.lib.jse;

import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaDouble;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaInteger;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaString;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import java.util.HashMap;
import java.util.Map;

public class CoerceJavaToLua {
   static final Map COERCIONS = new HashMap();
   static final CoerceJavaToLua.Coercion instanceCoercion = new CoerceJavaToLua.InstanceCoercion();
   static final CoerceJavaToLua.Coercion arrayCoercion = new CoerceJavaToLua.ArrayCoercion();
   static final CoerceJavaToLua.Coercion luaCoercion = new CoerceJavaToLua.LuaCoercion();

   public static LuaValue coerce(Object var0) {
      if (var0 == null) {
         return LuaValue.NIL;
      } else {
         Class var1 = var0.getClass();
         CoerceJavaToLua.Coercion var2 = (CoerceJavaToLua.Coercion)COERCIONS.get(var1);
         if (var2 == null) {
            var2 = var1.isArray() ? arrayCoercion : (var0 instanceof LuaValue ? luaCoercion : instanceCoercion);
            COERCIONS.put(var1, var2);
         }

         return var2.coerce(var0);
      }
   }

   static {
      CoerceJavaToLua.BoolCoercion var0 = new CoerceJavaToLua.BoolCoercion();
      CoerceJavaToLua.IntCoercion var1 = new CoerceJavaToLua.IntCoercion();
      CoerceJavaToLua.CharCoercion var2 = new CoerceJavaToLua.CharCoercion();
      CoerceJavaToLua.DoubleCoercion var3 = new CoerceJavaToLua.DoubleCoercion();
      CoerceJavaToLua.StringCoercion var4 = new CoerceJavaToLua.StringCoercion();
      CoerceJavaToLua.BytesCoercion var5 = new CoerceJavaToLua.BytesCoercion();
      CoerceJavaToLua.ClassCoercion var6 = new CoerceJavaToLua.ClassCoercion();
      COERCIONS.put(class$java$lang$Boolean == null ? (class$java$lang$Boolean = class$("java.lang.Boolean")) : class$java$lang$Boolean, var0);
      COERCIONS.put(class$java$lang$Byte == null ? (class$java$lang$Byte = class$("java.lang.Byte")) : class$java$lang$Byte, var1);
      COERCIONS.put(class$java$lang$Character == null ? (class$java$lang$Character = class$("java.lang.Character")) : class$java$lang$Character, var2);
      COERCIONS.put(class$java$lang$Short == null ? (class$java$lang$Short = class$("java.lang.Short")) : class$java$lang$Short, var1);
      COERCIONS.put(class$java$lang$Integer == null ? (class$java$lang$Integer = class$("java.lang.Integer")) : class$java$lang$Integer, var1);
      COERCIONS.put(class$java$lang$Long == null ? (class$java$lang$Long = class$("java.lang.Long")) : class$java$lang$Long, var3);
      COERCIONS.put(class$java$lang$Float == null ? (class$java$lang$Float = class$("java.lang.Float")) : class$java$lang$Float, var3);
      COERCIONS.put(class$java$lang$Double == null ? (class$java$lang$Double = class$("java.lang.Double")) : class$java$lang$Double, var3);
      COERCIONS.put(class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String, var4);
      COERCIONS.put(array$B == null ? (array$B = class$("[B")) : array$B, var5);
      COERCIONS.put(class$java$lang$Class == null ? (class$java$lang$Class = class$("java.lang.Class")) : class$java$lang$Class, var6);
   }

   private static final class ArrayCoercion implements CoerceJavaToLua.Coercion {
      private ArrayCoercion() {
      }

      public LuaValue coerce(Object var1) {
         return new JavaArray(var1);
      }
   }

   private static final class BoolCoercion implements CoerceJavaToLua.Coercion {
      private BoolCoercion() {
      }

      public LuaValue coerce(Object var1) {
         Boolean var2 = (Boolean)var1;
         return var2 ? LuaValue.TRUE : LuaValue.FALSE;
      }
   }

   private static final class BytesCoercion implements CoerceJavaToLua.Coercion {
      private BytesCoercion() {
      }

      public LuaValue coerce(Object var1) {
         return LuaValue.valueOf((byte[])var1);
      }
   }

   private static final class CharCoercion implements CoerceJavaToLua.Coercion {
      private CharCoercion() {
      }

      public LuaValue coerce(Object var1) {
         Character var2 = (Character)var1;
         return LuaInteger.valueOf(var2);
      }
   }

   private static final class ClassCoercion implements CoerceJavaToLua.Coercion {
      private ClassCoercion() {
      }

      public LuaValue coerce(Object var1) {
         return JavaClass.forClass((Class)var1);
      }
   }

   interface Coercion {
      LuaValue coerce(Object var1);
   }

   private static final class DoubleCoercion implements CoerceJavaToLua.Coercion {
      private DoubleCoercion() {
      }

      public LuaValue coerce(Object var1) {
         Number var2 = (Number)var1;
         return LuaDouble.valueOf(var2.doubleValue());
      }
   }

   private static final class InstanceCoercion implements CoerceJavaToLua.Coercion {
      private InstanceCoercion() {
      }

      public LuaValue coerce(Object var1) {
         return new JavaInstance(var1);
      }
   }

   private static final class IntCoercion implements CoerceJavaToLua.Coercion {
      private IntCoercion() {
      }

      public LuaValue coerce(Object var1) {
         Number var2 = (Number)var1;
         return LuaInteger.valueOf(var2.intValue());
      }
   }

   private static final class LuaCoercion implements CoerceJavaToLua.Coercion {
      private LuaCoercion() {
      }

      public LuaValue coerce(Object var1) {
         return (LuaValue)var1;
      }
   }

   private static final class StringCoercion implements CoerceJavaToLua.Coercion {
      private StringCoercion() {
      }

      public LuaValue coerce(Object var1) {
         return LuaString.valueOf(var1.toString());
      }
   }
}
