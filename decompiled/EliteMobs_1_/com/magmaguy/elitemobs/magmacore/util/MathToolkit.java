package com.magmaguy.elitemobs.magmacore.util;

public class MathToolkit {
   public static float lerp(float start, float end, float t) {
      return (1.0F - t) * start + t * end;
   }

   public static float smoothLerp(float start, float end, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      float smoothT = t * t * (3.0F - 2.0F * t);
      return (1.0F - smoothT) * start + smoothT * end;
   }

   public static float bezierLerp(float start, float end, float t, float cp1, float cp2) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      float oneMinusT = 1.0F - t;
      float bezierT = 3.0F * oneMinusT * oneMinusT * t * cp1 + 3.0F * oneMinusT * t * t * cp2 + t * t * t;
      return (1.0F - bezierT) * start + bezierT * end;
   }

   public static float stepLerp(float start, float end, float t, float threshold) {
      return t < threshold ? start : end;
   }

   public static float stepLerp(float start, float end, float t) {
      return stepLerp(start, end, t, 0.5F);
   }
}
