package com.magmaguy.elitemobs.shaded.luaj.vm2.lib.jse;

import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import com.magmaguy.elitemobs.shaded.luaj.vm2.lib.MathLib;

public class JseMathLib extends MathLib {
   public LuaValue call(LuaValue var1, LuaValue var2) {
      super.call(var1, var2);
      LuaValue var3 = var2.get("math");
      var3.set("acos", new JseMathLib.acos());
      var3.set("asin", new JseMathLib.asin());
      var3.set("atan", new JseMathLib.atan());
      var3.set("atan2", new JseMathLib.atan2());
      var3.set("cosh", new JseMathLib.cosh());
      var3.set("exp", new JseMathLib.exp());
      var3.set("log", new JseMathLib.log());
      var3.set("pow", new JseMathLib.pow());
      var3.set("sinh", new JseMathLib.sinh());
      var3.set("tanh", new JseMathLib.tanh());
      return var3;
   }

   public double dpow_lib(double var1, double var3) {
      return Math.pow(var1, var3);
   }

   static final class acos extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.acos(var1);
      }
   }

   static final class asin extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.asin(var1);
      }
   }

   static final class atan extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.atan(var1);
      }
   }

   static final class atan2 extends MathLib.BinaryOp {
      protected double call(double var1, double var3) {
         return Math.atan2(var1, var3);
      }
   }

   static final class cosh extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.cosh(var1);
      }
   }

   static final class exp extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.exp(var1);
      }
   }

   static final class log extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.log(var1);
      }
   }

   static final class pow extends MathLib.BinaryOp {
      protected double call(double var1, double var3) {
         return Math.pow(var1, var3);
      }
   }

   static final class sinh extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.sinh(var1);
      }
   }

   static final class tanh extends MathLib.UnaryOp {
      protected double call(double var1) {
         return Math.tanh(var1);
      }
   }
}
