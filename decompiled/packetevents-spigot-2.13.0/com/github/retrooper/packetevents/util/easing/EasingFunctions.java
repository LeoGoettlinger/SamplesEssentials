package com.github.retrooper.packetevents.util.easing;

import com.github.retrooper.packetevents.util.MathUtil;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class EasingFunctions {
   private EasingFunctions() {
   }

   public static float constant(float ignoredX) {
      return 0.0F;
   }

   public static float linear(float x) {
      return x;
   }

   public static float inBack(float x) {
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      return x * x * (c3 * x - c1);
   }

   public static float inBounce(float x) {
      return 1.0F - outBounce(1.0F - x);
   }

   public static float inCubic(float x) {
      return x * x * x;
   }

   public static float inElastic(float x) {
      if (x == 0.0F) {
         return 0.0F;
      } else if (x == 1.0F) {
         return 1.0F;
      } else {
         float c4 = (float) (Math.PI * 2.0 / 3.0);
         return (float)(-Math.pow(2.0, 10.0 * x - 10.0) * Math.sin((x * 10.0 - 10.75) * c4));
      }
   }

   public static float inExpo(float x) {
      return x == 0.0F ? 0.0F : (float)Math.pow(2.0, 10.0 * x - 10.0);
   }

   public static float inQuart(float x) {
      return x * x * (x * x);
   }

   public static float inQuint(float x) {
      return x * x * (x * x) * x;
   }

   public static float inSine(float x) {
      return 1.0F - (float)Math.cos(x * (float) (Math.PI / 2));
   }

   public static float inOutBounce(float x) {
      return x < 0.5F ? (1.0F - outBounce(1.0F - 2.0F * x)) / 2.0F : (1.0F + outBounce(2.0F * x - 1.0F)) / 2.0F;
   }

   public static float inOutCirc(float x) {
      return x < 0.5F ? (float)((1.0 - Math.sqrt(1.0 - Math.pow(2.0 * x, 2.0))) / 2.0) : (float)((Math.sqrt(1.0 - Math.pow(-2.0 * x + 2.0, 2.0)) + 1.0) / 2.0);
   }

   public static float inOutCubic(float x) {
      return x < 0.5F ? 4.0F * (x * x * x) : (float)(1.0 - Math.pow(-2.0 * x + 2.0, 3.0) / 2.0);
   }

   public static float inOutQuad(float x) {
      return x < 0.5F ? 2.0F * (x * x) : (float)(1.0 - Math.pow(-2.0 * x + 2.0, 2.0) / 2.0);
   }

   public static float inOutQuart(float x) {
      return x < 0.5F ? 8.0F * (x * x * (x * x)) : (float)(1.0 - Math.pow(-2.0 * x + 2.0, 4.0) / 2.0);
   }

   public static float inOutQuint(float x) {
      return x < 0.5 ? 16.0F * x * x * x * x * x : (float)(1.0 - Math.pow(-2.0 * x + 2.0, 5.0) / 2.0);
   }

   public static float outBounce(float x) {
      float n1 = 7.5625F;
      float d1 = 2.75F;
      if (x < 1.0F / d1) {
         return n1 * (x * x);
      } else if (x < 2.0F / d1) {
         return n1 * MathUtil.square(x - 1.5F / d1) + 0.75F;
      } else {
         return x < 2.5F / d1 ? n1 * MathUtil.square(x - 2.25F / d1) + 0.9375F : n1 * MathUtil.square(x - 2.625F / d1) + 0.984375F;
      }
   }

   public static float outElastic(float x) {
      float c4 = (float) (Math.PI * 2.0 / 3.0);
      if (x == 0.0F) {
         return 0.0F;
      } else {
         return x == 1.0F ? 1.0F : (float)(Math.pow(2.0, -10.0 * x) * Math.sin((x * 10.0 - 0.75) * c4) + 1.0);
      }
   }

   public static float outExpo(float x) {
      return x == 1.0F ? 1.0F : 1.0F - (float)Math.pow(2.0, -10.0 * x);
   }

   public static float outQuad(float x) {
      return 1.0F - MathUtil.square(1.0F - x);
   }

   public static float outQuint(float x) {
      return 1.0F - (float)Math.pow(1.0 - x, 5.0);
   }

   public static float outSine(float x) {
      return (float)Math.sin(x * (float) (Math.PI / 2));
   }

   public static float inOutSine(float x) {
      return -((float)Math.cos((float) Math.PI * x) - 1.0F) / 2.0F;
   }

   public static float outBack(float x) {
      float c1 = 1.70158F;
      float c3 = 2.70158F;
      return 1.0F + c3 * MathUtil.cube(x - 1.0F) + c1 * MathUtil.square(x - 1.0F);
   }

   public static float outQuart(float x) {
      return 1.0F - MathUtil.square(MathUtil.square(1.0F - x));
   }

   public static float outCubic(float x) {
      return 1.0F - MathUtil.cube(1.0F - x);
   }

   public static float inOutExpo(float x) {
      if (x == 0.0F) {
         return 0.0F;
      } else if (x == 1.0F) {
         return 1.0F;
      } else {
         return x < 0.5F ? (float)(Math.pow(2.0, 20.0 * x - 10.0) / 2.0) : (float)((2.0 - Math.pow(2.0, -20.0 * x + 10.0)) / 2.0);
      }
   }

   public static float inQuad(float x) {
      return x * x;
   }

   public static float outCirc(float x) {
      return (float)Math.sqrt(1.0F - MathUtil.square(x - 1.0F));
   }

   public static float inOutElastic(float x) {
      float c5 = (float) Math.PI * 4.0F / 9.0F;
      if (x == 0.0F) {
         return 0.0F;
      } else if (x == 1.0F) {
         return 1.0F;
      } else {
         double sin = Math.sin((20.0 * x - 11.125) * c5);
         return x < 0.5F ? (float)(-(Math.pow(2.0, 20.0 * x - 10.0) * sin) / 2.0) : (float)(Math.pow(2.0, -20.0 * x + 10.0) * sin / 2.0 + 1.0);
      }
   }

   public static float inCirc(float x) {
      return (float)(-Math.sqrt(1.0F - x * x)) + 1.0F;
   }

   public static float inOutBack(float x) {
      float c1 = 1.70158F;
      float c2 = c1 * 1.525F;
      if (x < 0.5F) {
         return 4.0F * x * x * (2.0F * (c2 + 1.0F) * x - c2) / 2.0F;
      } else {
         float dt = 2.0F * x - 2.0F;
         return (dt * dt * ((c2 + 1.0F) * dt + c2) + 2.0F) / 2.0F;
      }
   }
}
