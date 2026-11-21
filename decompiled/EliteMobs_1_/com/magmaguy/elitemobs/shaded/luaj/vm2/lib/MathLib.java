package com.magmaguy.elitemobs.shaded.luaj.vm2.lib;

import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaDouble;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.Varargs;
import java.util.Random;

public class MathLib extends TwoArgFunction {
   public static MathLib MATHLIB = null;

   public MathLib() {
      MATHLIB = this;
   }

   public LuaValue call(LuaValue var1, LuaValue var2) {
      LuaTable var3 = new LuaTable(0, 30);
      var3.set("abs", new MathLib.abs());
      var3.set("ceil", new MathLib.ceil());
      var3.set("cos", new MathLib.cos());
      var3.set("deg", new MathLib.deg());
      var3.set("exp", new MathLib.exp(this));
      var3.set("floor", new MathLib.floor());
      var3.set("fmod", new MathLib.fmod());
      var3.set("frexp", new MathLib.frexp());
      var3.set("huge", LuaDouble.POSINF);
      var3.set("ldexp", new MathLib.ldexp());
      var3.set("max", new MathLib.max());
      var3.set("min", new MathLib.min());
      var3.set("modf", new MathLib.modf());
      var3.set("pi", Math.PI);
      var3.set("pow", new MathLib.pow());
      MathLib.random var4;
      var3.set("random", var4 = new MathLib.random());
      var3.set("randomseed", new MathLib.randomseed(var4));
      var3.set("rad", new MathLib.rad());
      var3.set("sin", new MathLib.sin());
      var3.set("sqrt", new MathLib.sqrt());
      var3.set("tan", new MathLib.tan());
      var2.set("math", var3);
      var2.get("package").get("loaded").set("math", var3);
      return var3;
   }

   public static LuaValue dpow(double var0, double var2) {
      return LuaDouble.valueOf(MATHLIB != null ? MATHLIB.dpow_lib(var0, var2) : dpow_default(var0, var2));
   }

   public static double dpow_d(double var0, double var2) {
      return MATHLIB != null ? MATHLIB.dpow_lib(var0, var2) : dpow_default(var0, var2);
   }

   public double dpow_lib(double var1, double var3) {
      return dpow_default(var1, var3);
   }

   protected static double dpow_default(double var0, double var2) {
      if (var2 < 0.0) {
         return 1.0 / dpow_default(var0, -var2);
      } else {
         double var4 = 1.0;
         int var6 = (int)var2;

         for (double var7 = var0; var6 > 0; var7 *= var7) {
            if ((var6 & 1) != 0) {
               var4 *= var7;
            }

            var6 >>= 1;
         }

         if ((var2 = var2 - var6) > 0.0) {
            for (int var10 = (int)(65536.0 * var2); (var10 & 65535) != 0; var10 <<= 1) {
               var0 = Math.sqrt(var0);
               if ((var10 & 32768) != 0) {
                  var4 *= var0;
               }
            }
         }

         return var4;
      }
   }

   protected abstract static class BinaryOp extends TwoArgFunction {
      public LuaValue call(LuaValue var1, LuaValue var2) {
         return valueOf(this.call(var1.checkdouble(), var2.checkdouble()));
      }

      protected abstract double call(double var1, double var3);
   }

   protected abstract static class UnaryOp extends OneArgFunction {
      public LuaValue call(LuaValue var1) {
         return valueOf(this.call(var1.checkdouble()));
      }

      protected abstract double call(double var1);
   }

   static final class abs extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.abs(var1);
      }
   }

   static final class ceil extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.ceil(var1);
      }
   }

   static final class cos extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.cos(var1);
      }
   }

   static final class deg extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.toDegrees(var1);
      }
   }

   static final class exp extends MathLib.UnaryOp {
      final MathLib mathlib;

      exp(MathLib var1) {
         this.mathlib = var1;
      }

      protected double call(double var1) {
         return this.mathlib.dpow_lib(Math.E, var1);
      }
   }

   static final class floor extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.floor(var1);
      }
   }

   static final class fmod extends MathLib.BinaryOp {
      protected double call(double var1, double var3) {
         double var5 = var1 / var3;
         return var1 - var3 * (var5 >= 0.0 ? Math.floor(var5) : Math.ceil(var5));
      }
   }

   static class frexp extends VarArgFunction {
      public Varargs invoke(Varargs var1) {
         double var2 = var1.checkdouble(1);
         if (var2 == 0.0) {
            return varargsOf(ZERO, ZERO);
         } else {
            long var4 = Double.doubleToLongBits(var2);
            double var6 = ((var4 & 4503599627370495L) + 4503599627370496L) * (var4 >= 0L ? 1.110223E-16F : -1.110223E-16F);
            double var8 = ((int)(var4 >> 52) & 2047) - 1022;
            return varargsOf(valueOf(var6), valueOf(var8));
         }
      }
   }

   static final class ldexp extends MathLib.BinaryOp {
      protected double call(double var1, double var3) {
         return var1 * Double.longBitsToDouble((long)var3 + 1023L << 52);
      }
   }

   static class max extends VarArgFunction {
      public Varargs invoke(Varargs var1) {
         double var2 = var1.checkdouble(1);
         int var4 = 2;

         for (int var5 = var1.narg(); var4 <= var5; var4++) {
            var2 = Math.max(var2, var1.checkdouble(var4));
         }

         return valueOf(var2);
      }
   }

   static class min extends VarArgFunction {
      public Varargs invoke(Varargs var1) {
         double var2 = var1.checkdouble(1);
         int var4 = 2;

         for (int var5 = var1.narg(); var4 <= var5; var4++) {
            var2 = Math.min(var2, var1.checkdouble(var4));
         }

         return valueOf(var2);
      }
   }

   static class modf extends VarArgFunction {
      public Varargs invoke(Varargs var1) {
         double var2 = var1.checkdouble(1);
         double var4 = var2 > 0.0 ? Math.floor(var2) : Math.ceil(var2);
         double var6 = var2 - var4;
         return varargsOf(valueOf(var4), valueOf(var6));
      }
   }

   static final class pow extends MathLib.BinaryOp {
      protected double call(double var1, double var3) {
         return MathLib.dpow_default(var1, var3);
      }
   }

   static final class rad extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.toRadians(var1);
      }
   }

   static class random extends LibFunction {
      Random random = new Random();

      public LuaValue call() {
         return valueOf(this.random.nextDouble());
      }

      public LuaValue call(LuaValue var1) {
         int var2 = var1.checkint();
         if (var2 < 1) {
            argerror(1, "interval is empty");
         }

         return valueOf(1 + this.random.nextInt(var2));
      }

      public LuaValue call(LuaValue var1, LuaValue var2) {
         int var3 = var1.checkint();
         int var4 = var2.checkint();
         if (var4 < var3) {
            argerror(2, "interval is empty");
         }

         return valueOf(var3 + this.random.nextInt(var4 + 1 - var3));
      }
   }

   static class randomseed extends OneArgFunction {
      final MathLib.random random;

      randomseed(MathLib.random var1) {
         this.random = var1;
      }

      public LuaValue call(LuaValue var1) {
         long var2 = var1.checklong();
         this.random.random = new Random(var2);
         return NONE;
      }
   }

   static final class sin extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.sin(var1);
      }
   }

   static final class sqrt extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.sqrt(var1);
      }
   }

   static final class tan extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.tan(var1);
      }
   }
}
