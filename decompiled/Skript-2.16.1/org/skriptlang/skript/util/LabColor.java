package org.skriptlang.skript.util;

import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public record LabColor(double L, double a, double b) {
   private static final Matrix3dc XYZ_TRANSFORMATION = new Matrix3d(
         0.4124564, 0.3575761, 0.1804375, 0.2126729, 0.7151522, 0.072175, 0.0193339, 0.119192, 0.9503041
      )
      .transpose();
   private static final Matrix3dc INVERSE_XYZ_TRANSFORMATION = new Matrix3d(
         3.2404542, -1.5371385, -0.4985314, -0.969266, 1.8760108, 0.041556, 0.0556434, -0.2040259, 1.0572252
      )
      .transpose();
   private static final Vector3dc D65 = new Vector3d(0.95047, 1.0, 1.08883);
   private static final double EPSILON = 0.008856;
   private static final double K_0 = 903.3;
   private static final double K_1 = 7.787;
   private static final double POW25_7 = 6.103515625E9;

   public int toRGB() {
      double fy = (this.L + 16.0) / 116.0;
      double fx = this.a / 500.0 + fy;
      double fz = fy - this.b / 200.0;
      double xr = fInverse(fx);
      double yr = this.L > 7.9996247999999985 ? fy * fy * fy : this.L / 903.3;
      double zr = fInverse(fz);
      Vector3d rgb = new Vector3d(xr * D65.x(), yr * D65.y(), zr * D65.z());
      rgb.mul(INVERSE_XYZ_TRANSFORMATION);
      double r = inverseGammaCorrection(rgb.x());
      double g = inverseGammaCorrection(rgb.y());
      double b = inverseGammaCorrection(rgb.z());
      int ri = (int)Math.round(Math.clamp(r, 0.0, 1.0) * 255.0);
      int gi = (int)Math.round(Math.clamp(g, 0.0, 1.0) * 255.0);
      int bi = (int)Math.round(Math.clamp(b, 0.0, 1.0) * 255.0);
      return ri << 16 | gi << 8 | bi;
   }

   public double euclideanDistanceSquared(LabColor other) {
      double L = other.L() - this.L();
      double a = other.a() - this.a();
      double b = other.b() - this.b();
      return L * L + a * a + b * b;
   }

   public double euclideanDistance(LabColor other) {
      return Math.sqrt(this.euclideanDistanceSquared(other));
   }

   private double c() {
      return Math.sqrt(this.a * this.a + this.b * this.b);
   }

   public static LabColor fromRGB(int rgb) {
      return fromRGB(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
   }

   public static LabColor fromRGB(int r, int g, int b) {
      return fromRGB(r / 255.0, g / 255.0, b / 255.0);
   }

   public static LabColor fromRGB(double r, double g, double b) {
      double rLinear = gammaCorrection(r);
      double gLinear = gammaCorrection(g);
      double bLinear = gammaCorrection(b);
      Vector3d xyz = new Vector3d(rLinear, gLinear, bLinear);
      xyz.mul(XYZ_TRANSFORMATION);
      double Y = f(xyz.y() / D65.y());
      double LStar = 116.0 * Y - 16.0;
      double aStar = 500.0 * (f(xyz.x() / D65.x()) - Y);
      double bStar = 200.0 * (Y - f(xyz.z() / D65.z()));
      return new LabColor(LStar, aStar, bStar);
   }

   private static double gammaCorrection(double value) {
      return value > 0.04045 ? Math.pow((value + 0.055) / 1.055, 2.4) : value / 12.92;
   }

   private static double inverseGammaCorrection(double value) {
      return value > 0.0031308 ? 1.055 * Math.pow(value, 0.4166666666666667) - 0.055 : 12.92 * value;
   }

   private static double f(double t) {
      return t > 0.008856 ? Math.cbrt(t) : 7.787 * t + 0.13793103448275862;
   }

   private static double fInverse(double t) {
      double n3 = t * t * t;
      return n3 > 0.008856 ? n3 : (t - 0.13793103448275862) / 7.787;
   }
}
