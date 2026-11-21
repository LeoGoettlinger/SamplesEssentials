package com.magmaguy.elitemobs.shaded.luaj.vm2.lib.jse;

import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaString;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CoerceLuaToJava {
   static int SCORE_NULL_VALUE = 16;
   static int SCORE_WRONG_TYPE = 256;
   static int SCORE_UNCOERCIBLE = 65536;
   static final Map COERCIONS = Collections.synchronizedMap(new HashMap());

   public static Object coerce(LuaValue var0, Class var1) {
      return getCoercion(var1).coerce(var0);
   }

   static final int inheritanceLevels(Class var0, Class var1) {
      if (var1 == null) {
         return SCORE_UNCOERCIBLE;
      } else if (var0 == var1) {
         return 0;
      } else {
         int var2 = Math.min(SCORE_UNCOERCIBLE, inheritanceLevels(var0, var1.getSuperclass()) + 1);
         Class[] var3 = var1.getInterfaces();

         for (int var4 = 0; var4 < var3.length; var4++) {
            var2 = Math.min(var2, inheritanceLevels(var0, var3[var4]) + 1);
         }

         return var2;
      }
   }

   static CoerceLuaToJava.Coercion getCoercion(Class var0) {
      CoerceLuaToJava.Coercion var1 = (CoerceLuaToJava.Coercion)COERCIONS.get(var0);
      if (var1 != null) {
         return var1;
      } else {
         if (var0.isArray()) {
            Class var2 = var0.getComponentType();
            var1 = new CoerceLuaToJava.ArrayCoercion(var0.getComponentType());
         } else {
            var1 = new CoerceLuaToJava.ObjectCoercion(var0);
         }

         COERCIONS.put(var0, var1);
         return var1;
      }
   }

   static {
      CoerceLuaToJava.BoolCoercion var0 = new CoerceLuaToJava.BoolCoercion();
      CoerceLuaToJava.NumericCoercion var1 = new CoerceLuaToJava.NumericCoercion(0);
      CoerceLuaToJava.NumericCoercion var2 = new CoerceLuaToJava.NumericCoercion(1);
      CoerceLuaToJava.NumericCoercion var3 = new CoerceLuaToJava.NumericCoercion(2);
      CoerceLuaToJava.NumericCoercion var4 = new CoerceLuaToJava.NumericCoercion(3);
      CoerceLuaToJava.NumericCoercion var5 = new CoerceLuaToJava.NumericCoercion(4);
      CoerceLuaToJava.NumericCoercion var6 = new CoerceLuaToJava.NumericCoercion(5);
      CoerceLuaToJava.NumericCoercion var7 = new CoerceLuaToJava.NumericCoercion(6);
      CoerceLuaToJava.StringCoercion var8 = new CoerceLuaToJava.StringCoercion(0);
      CoerceLuaToJava.StringCoercion var9 = new CoerceLuaToJava.StringCoercion(1);
      COERCIONS.put(boolean.class, var0);
      COERCIONS.put(class$java$lang$Boolean == null ? (class$java$lang$Boolean = class$("java.lang.Boolean")) : class$java$lang$Boolean, var0);
      COERCIONS.put(byte.class, var1);
      COERCIONS.put(class$java$lang$Byte == null ? (class$java$lang$Byte = class$("java.lang.Byte")) : class$java$lang$Byte, var1);
      COERCIONS.put(char.class, var2);
      COERCIONS.put(class$java$lang$Character == null ? (class$java$lang$Character = class$("java.lang.Character")) : class$java$lang$Character, var2);
      COERCIONS.put(short.class, var3);
      COERCIONS.put(class$java$lang$Short == null ? (class$java$lang$Short = class$("java.lang.Short")) : class$java$lang$Short, var3);
      COERCIONS.put(int.class, var4);
      COERCIONS.put(class$java$lang$Integer == null ? (class$java$lang$Integer = class$("java.lang.Integer")) : class$java$lang$Integer, var4);
      COERCIONS.put(long.class, var5);
      COERCIONS.put(class$java$lang$Long == null ? (class$java$lang$Long = class$("java.lang.Long")) : class$java$lang$Long, var5);
      COERCIONS.put(float.class, var6);
      COERCIONS.put(class$java$lang$Float == null ? (class$java$lang$Float = class$("java.lang.Float")) : class$java$lang$Float, var6);
      COERCIONS.put(double.class, var7);
      COERCIONS.put(class$java$lang$Double == null ? (class$java$lang$Double = class$("java.lang.Double")) : class$java$lang$Double, var7);
      COERCIONS.put(class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String, var8);
      COERCIONS.put(array$B == null ? (array$B = class$("[B")) : array$B, var9);
   }

   static final class ArrayCoercion implements CoerceLuaToJava.Coercion {
      final Class componentType;
      final CoerceLuaToJava.Coercion componentCoercion;

      public ArrayCoercion(Class var1) {
         this.componentType = var1;
         this.componentCoercion = CoerceLuaToJava.getCoercion(var1);
      }

      public String toString() {
         return "ArrayCoercion(" + this.componentType.getName() + ")";
      }

      public int score(LuaValue var1) {
         switch (var1.type()) {
            case 0:
               return CoerceLuaToJava.SCORE_NULL_VALUE;
            case 5:
               return var1.length() == 0 ? 0 : this.componentCoercion.score(var1.get(1));
            case 7:
               return CoerceLuaToJava.inheritanceLevels(this.componentType, var1.touserdata().getClass().getComponentType());
            default:
               return CoerceLuaToJava.SCORE_UNCOERCIBLE;
         }
      }

      public Object coerce(LuaValue var1) {
         switch (var1.type()) {
            case 0:
               return null;
            case 5:
               int var2 = var1.length();
               Object var3 = Array.newInstance(this.componentType, var2);

               for (int var4 = 0; var4 < var2; var4++) {
                  Array.set(var3, var4, this.componentCoercion.coerce(var1.get(var4 + 1)));
               }

               return var3;
            case 7:
               return var1.touserdata();
            default:
               return null;
         }
      }
   }

   static final class BoolCoercion implements CoerceLuaToJava.Coercion {
      public String toString() {
         return "BoolCoercion()";
      }

      public int score(LuaValue var1) {
         switch (var1.type()) {
            case 1:
               return 0;
            default:
               return 1;
         }
      }

      public Object coerce(LuaValue var1) {
         return var1.toboolean() ? Boolean.TRUE : Boolean.FALSE;
      }
   }

   interface Coercion {
      int score(LuaValue var1);

      Object coerce(LuaValue var1);
   }

   static final class NumericCoercion implements CoerceLuaToJava.Coercion {
      static final int TARGET_TYPE_BYTE = 0;
      static final int TARGET_TYPE_CHAR = 1;
      static final int TARGET_TYPE_SHORT = 2;
      static final int TARGET_TYPE_INT = 3;
      static final int TARGET_TYPE_LONG = 4;
      static final int TARGET_TYPE_FLOAT = 5;
      static final int TARGET_TYPE_DOUBLE = 6;
      static final String[] TYPE_NAMES = new String[]{"byte", "char", "short", "int", "long", "float", "double"};
      final int targetType;

      public String toString() {
         return "NumericCoercion(" + TYPE_NAMES[this.targetType] + ")";
      }

      NumericCoercion(int var1) {
         this.targetType = var1;
      }

      public int score(LuaValue var1) {
         byte var2 = 0;
         if (var1.type() == 4) {
            var1 = var1.tonumber();
            if (var1.isnil()) {
               return CoerceLuaToJava.SCORE_UNCOERCIBLE;
            }

            var2 = 4;
         }

         if (var1.isint()) {
            switch (this.targetType) {
               case 0:
                  int var10 = var1.toint();
                  return var2 + (var10 == (byte)var10 ? 0 : CoerceLuaToJava.SCORE_WRONG_TYPE);
               case 1:
                  int var9 = var1.toint();
                  return var2 + (var9 == (byte)var9 ? 1 : (var9 == (char)var9 ? 0 : CoerceLuaToJava.SCORE_WRONG_TYPE));
               case 2:
                  int var8 = var1.toint();
                  return var2 + (var8 == (byte)var8 ? 1 : (var8 == (short)var8 ? 0 : CoerceLuaToJava.SCORE_WRONG_TYPE));
               case 3:
                  int var7 = var1.toint();
                  return var2 + (var7 == (byte)var7 ? 2 : (var7 != (char)var7 && var7 != (short)var7 ? 0 : 1));
               case 4:
                  return var2 + 1;
               case 5:
                  return var2 + 1;
               case 6:
                  return var2 + 2;
               default:
                  return CoerceLuaToJava.SCORE_WRONG_TYPE;
            }
         } else if (var1.isnumber()) {
            switch (this.targetType) {
               case 0:
                  return CoerceLuaToJava.SCORE_WRONG_TYPE;
               case 1:
                  return CoerceLuaToJava.SCORE_WRONG_TYPE;
               case 2:
                  return CoerceLuaToJava.SCORE_WRONG_TYPE;
               case 3:
                  return CoerceLuaToJava.SCORE_WRONG_TYPE;
               case 4:
                  double var6 = var1.todouble();
                  return var2 + (var6 == (long)var6 ? 0 : CoerceLuaToJava.SCORE_WRONG_TYPE);
               case 5:
                  double var5 = var1.todouble();
                  return var2 + (var5 == (float)var5 ? 0 : CoerceLuaToJava.SCORE_WRONG_TYPE);
               case 6:
                  double var3 = var1.todouble();
                  return var2 + (var3 != (long)var3 && var3 != (float)var3 ? 0 : 1);
               default:
                  return CoerceLuaToJava.SCORE_WRONG_TYPE;
            }
         } else {
            return CoerceLuaToJava.SCORE_UNCOERCIBLE;
         }
      }

      public Object coerce(LuaValue var1) {
         switch (this.targetType) {
            case 0:
               return new Byte((byte)var1.toint());
            case 1:
               return new Character((char)var1.toint());
            case 2:
               return new Short((short)var1.toint());
            case 3:
               return new Integer(var1.toint());
            case 4:
               return new Long((long)var1.todouble());
            case 5:
               return new Float((float)var1.todouble());
            case 6:
               return new Double(var1.todouble());
            default:
               return null;
         }
      }
   }

   static final class ObjectCoercion implements CoerceLuaToJava.Coercion {
      final Class targetType;

      ObjectCoercion(Class var1) {
         this.targetType = var1;
      }

      public String toString() {
         return "ObjectCoercion(" + this.targetType.getName() + ")";
      }

      public int score(LuaValue var1) {
         switch (var1.type()) {
            case 0:
               return CoerceLuaToJava.SCORE_NULL_VALUE;
            case 1:
               return CoerceLuaToJava.inheritanceLevels(
                  this.targetType,
                  CoerceLuaToJava.class$java$lang$Boolean == null
                     ? (CoerceLuaToJava.class$java$lang$Boolean = CoerceLuaToJava.class$("java.lang.Boolean"))
                     : CoerceLuaToJava.class$java$lang$Boolean
               );
            case 2:
            case 5:
            case 6:
            default:
               return CoerceLuaToJava.inheritanceLevels(this.targetType, var1.getClass());
            case 3:
               return CoerceLuaToJava.inheritanceLevels(
                  this.targetType,
                  var1.isint()
                     ? (
                        CoerceLuaToJava.class$java$lang$Integer == null
                           ? (CoerceLuaToJava.class$java$lang$Integer = CoerceLuaToJava.class$("java.lang.Integer"))
                           : CoerceLuaToJava.class$java$lang$Integer
                     )
                     : (
                        CoerceLuaToJava.class$java$lang$Double == null
                           ? (CoerceLuaToJava.class$java$lang$Double = CoerceLuaToJava.class$("java.lang.Double"))
                           : CoerceLuaToJava.class$java$lang$Double
                     )
               );
            case 4:
               return CoerceLuaToJava.inheritanceLevels(
                  this.targetType,
                  CoerceLuaToJava.class$java$lang$String == null
                     ? (CoerceLuaToJava.class$java$lang$String = CoerceLuaToJava.class$("java.lang.String"))
                     : CoerceLuaToJava.class$java$lang$String
               );
            case 7:
               return CoerceLuaToJava.inheritanceLevels(this.targetType, var1.touserdata().getClass());
         }
      }

      public Object coerce(LuaValue var1) {
         switch (var1.type()) {
            case 0:
               return null;
            case 1:
               return var1.toboolean() ? Boolean.TRUE : Boolean.FALSE;
            case 2:
            case 5:
            case 6:
            default:
               return var1;
            case 3:
               return var1.isint() ? new Integer(var1.toint()) : new Double(var1.todouble());
            case 4:
               return var1.tojstring();
            case 7:
               return var1.optuserdata(this.targetType, null);
         }
      }
   }

   static final class StringCoercion implements CoerceLuaToJava.Coercion {
      public static final int TARGET_TYPE_STRING = 0;
      public static final int TARGET_TYPE_BYTES = 1;
      final int targetType;

      public StringCoercion(int var1) {
         this.targetType = var1;
      }

      public String toString() {
         return "StringCoercion(" + (this.targetType == 0 ? "String" : "byte[]") + ")";
      }

      public int score(LuaValue var1) {
         switch (var1.type()) {
            case 0:
               return CoerceLuaToJava.SCORE_NULL_VALUE;
            case 4:
               return var1.checkstring().isValidUtf8() ? (this.targetType == 0 ? 0 : 1) : (this.targetType == 1 ? 0 : CoerceLuaToJava.SCORE_WRONG_TYPE);
            default:
               return this.targetType == 0 ? CoerceLuaToJava.SCORE_WRONG_TYPE : CoerceLuaToJava.SCORE_UNCOERCIBLE;
         }
      }

      public Object coerce(LuaValue var1) {
         if (var1.isnil()) {
            return null;
         } else if (this.targetType == 0) {
            return var1.tojstring();
         } else {
            LuaString var2 = var1.checkstring();
            byte[] var3 = new byte[var2.m_length];
            var2.copyInto(0, var3, 0, var3.length);
            return var3;
         }
      }
   }
}
