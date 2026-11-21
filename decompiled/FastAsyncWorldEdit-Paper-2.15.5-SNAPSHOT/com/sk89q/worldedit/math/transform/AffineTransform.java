package com.sk89q.worldedit.math.transform;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.MathUtils;
import com.sk89q.worldedit.math.Vector3;
import java.io.Serializable;

public class AffineTransform implements Transform, Serializable {
   private final double m00;
   private final double m01;
   private final double m02;
   private final double m03;
   private final double m10;
   private final double m11;
   private final double m12;
   private final double m13;
   private final double m20;
   private final double m21;
   private final double m22;
   private final double m23;

   public AffineTransform() {
      this.m00 = this.m11 = this.m22 = 1.0;
      this.m01 = this.m02 = this.m03 = 0.0;
      this.m10 = this.m12 = this.m13 = 0.0;
      this.m20 = this.m21 = this.m23 = 0.0;
   }

   public AffineTransform(double[] coefs) {
      if (coefs.length == 9) {
         this.m00 = coefs[0];
         this.m01 = coefs[1];
         this.m02 = coefs[2];
         this.m10 = coefs[3];
         this.m11 = coefs[4];
         this.m12 = coefs[5];
         this.m20 = coefs[6];
         this.m21 = coefs[7];
         this.m22 = coefs[8];
         this.m03 = this.m13 = this.m23 = 0.0;
      } else {
         if (coefs.length != 12) {
            throw new IllegalArgumentException("Input array must have 9 or 12 elements");
         }

         this.m00 = coefs[0];
         this.m01 = coefs[1];
         this.m02 = coefs[2];
         this.m03 = coefs[3];
         this.m10 = coefs[4];
         this.m11 = coefs[5];
         this.m12 = coefs[6];
         this.m13 = coefs[7];
         this.m20 = coefs[8];
         this.m21 = coefs[9];
         this.m22 = coefs[10];
         this.m23 = coefs[11];
      }
   }

   public AffineTransform(double xx, double yx, double zx, double tx, double xy, double yy, double zy, double ty, double xz, double yz, double zz, double tz) {
      this.m00 = xx;
      this.m01 = yx;
      this.m02 = zx;
      this.m03 = tx;
      this.m10 = xy;
      this.m11 = yy;
      this.m12 = zy;
      this.m13 = ty;
      this.m20 = xz;
      this.m21 = yz;
      this.m22 = zz;
      this.m23 = tz;
   }

   @Override
   public boolean isIdentity() {
      return this.m00 == this.m11
         && this.m11 == this.m22
         && this.m22 == 1.0
         && this.m01 == this.m02
         && this.m02 == this.m03
         && this.m03 == 0.0
         && this.m10 == this.m12
         && this.m12 == this.m13
         && this.m13 == 0.0
         && this.m20 == this.m21
         && this.m21 == this.m23
         && this.m23 == 0.0;
   }

   public double[] coefficients() {
      return new double[]{this.m00, this.m01, this.m02, this.m03, this.m10, this.m11, this.m12, this.m13, this.m20, this.m21, this.m22, this.m23};
   }

   public boolean isOffAxis() {
      double[] c = this.coefficients();

      for (int i = 0; i < c.length; i++) {
         if ((i + 1) % 4 != 0 && Math.abs(c[i]) != 1.0 && c[i] != 0.0) {
            return true;
         }
      }

      return false;
   }

   private double determinant() {
      return this.m00 * (this.m11 * this.m22 - this.m12 * this.m21)
         - this.m01 * (this.m10 * this.m22 - this.m20 * this.m12)
         + this.m02 * (this.m10 * this.m21 - this.m20 * this.m11);
   }

   public AffineTransform inverse() {
      double det = this.determinant();
      return new AffineTransform(
         (this.m11 * this.m22 - this.m21 * this.m12) / det,
         (this.m21 * this.m02 - this.m01 * this.m22) / det,
         (this.m01 * this.m12 - this.m11 * this.m02) / det,
         (
               this.m01 * (this.m22 * this.m13 - this.m12 * this.m23)
                  + this.m02 * (this.m11 * this.m23 - this.m21 * this.m13)
                  - this.m03 * (this.m11 * this.m22 - this.m21 * this.m12)
            )
            / det,
         (this.m20 * this.m12 - this.m10 * this.m22) / det,
         (this.m00 * this.m22 - this.m20 * this.m02) / det,
         (this.m10 * this.m02 - this.m00 * this.m12) / det,
         (
               this.m00 * (this.m12 * this.m23 - this.m22 * this.m13)
                  - this.m02 * (this.m10 * this.m23 - this.m20 * this.m13)
                  + this.m03 * (this.m10 * this.m22 - this.m20 * this.m12)
            )
            / det,
         (this.m10 * this.m21 - this.m20 * this.m11) / det,
         (this.m20 * this.m01 - this.m00 * this.m21) / det,
         (this.m00 * this.m11 - this.m10 * this.m01) / det,
         (
               this.m00 * (this.m21 * this.m13 - this.m11 * this.m23)
                  + this.m01 * (this.m10 * this.m23 - this.m20 * this.m13)
                  - this.m03 * (this.m10 * this.m21 - this.m20 * this.m11)
            )
            / det
      );
   }

   public AffineTransform concatenate(
      double o00, double o01, double o02, double o03, double o10, double o11, double o12, double o13, double o20, double o21, double o22, double o23
   ) {
      double n00 = this.m00 * o00 + this.m01 * o10 + this.m02 * o20;
      double n01 = this.m00 * o01 + this.m01 * o11 + this.m02 * o21;
      double n02 = this.m00 * o02 + this.m01 * o12 + this.m02 * o22;
      double n03 = this.m00 * o03 + this.m01 * o13 + this.m02 * o23 + this.m03;
      double n10 = this.m10 * o00 + this.m11 * o10 + this.m12 * o20;
      double n11 = this.m10 * o01 + this.m11 * o11 + this.m12 * o21;
      double n12 = this.m10 * o02 + this.m11 * o12 + this.m12 * o22;
      double n13 = this.m10 * o03 + this.m11 * o13 + this.m12 * o23 + this.m13;
      double n20 = this.m20 * o00 + this.m21 * o10 + this.m22 * o20;
      double n21 = this.m20 * o01 + this.m21 * o11 + this.m22 * o21;
      double n22 = this.m20 * o02 + this.m21 * o12 + this.m22 * o22;
      double n23 = this.m20 * o03 + this.m21 * o13 + this.m22 * o23 + this.m23;
      return new AffineTransform(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23);
   }

   public AffineTransform concatenate(AffineTransform that) {
      return this.concatenate(that.m00, that.m01, that.m02, that.m03, that.m10, that.m11, that.m12, that.m13, that.m20, that.m21, that.m22, that.m23);
   }

   public AffineTransform preConcatenate(AffineTransform that) {
      double n00 = that.m00 * this.m00 + that.m01 * this.m10 + that.m02 * this.m20;
      double n01 = that.m00 * this.m01 + that.m01 * this.m11 + that.m02 * this.m21;
      double n02 = that.m00 * this.m02 + that.m01 * this.m12 + that.m02 * this.m22;
      double n03 = that.m00 * this.m03 + that.m01 * this.m13 + that.m02 * this.m23 + that.m03;
      double n10 = that.m10 * this.m00 + that.m11 * this.m10 + that.m12 * this.m20;
      double n11 = that.m10 * this.m01 + that.m11 * this.m11 + that.m12 * this.m21;
      double n12 = that.m10 * this.m02 + that.m11 * this.m12 + that.m12 * this.m22;
      double n13 = that.m10 * this.m03 + that.m11 * this.m13 + that.m12 * this.m23 + that.m13;
      double n20 = that.m20 * this.m00 + that.m21 * this.m10 + that.m22 * this.m20;
      double n21 = that.m20 * this.m01 + that.m21 * this.m11 + that.m22 * this.m21;
      double n22 = that.m20 * this.m02 + that.m21 * this.m12 + that.m22 * this.m22;
      double n23 = that.m20 * this.m03 + that.m21 * this.m13 + that.m22 * this.m23 + that.m23;
      return new AffineTransform(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23);
   }

   public AffineTransform translate(Vector3 vec) {
      return this.translate(vec.x(), vec.y(), vec.z());
   }

   public AffineTransform translate(BlockVector3 vec) {
      return this.translate(vec.x(), vec.y(), vec.z());
   }

   public AffineTransform translate(double x, double y, double z) {
      return this.concatenate(1.0, 0.0, 0.0, x, 0.0, 1.0, 0.0, y, 0.0, 0.0, 1.0, z);
   }

   public AffineTransform rotateX(double theta) {
      double cot = MathUtils.dCos(theta);
      double sit = MathUtils.dSin(theta);
      return this.concatenate(1.0, 0.0, 0.0, 0.0, 0.0, cot, -sit, 0.0, 0.0, sit, cot, 0.0);
   }

   public AffineTransform rotateY(double theta) {
      double cot = MathUtils.dCos(theta);
      double sit = MathUtils.dSin(theta);
      return this.concatenate(cot, 0.0, sit, 0.0, 0.0, 1.0, 0.0, 0.0, -sit, 0.0, cot, 0.0);
   }

   public AffineTransform rotateZ(double theta) {
      double cot = MathUtils.dCos(theta);
      double sit = MathUtils.dSin(theta);
      return this.concatenate(cot, -sit, 0.0, 0.0, sit, cot, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
   }

   public AffineTransform scale(double s) {
      return this.scale(s, s, s);
   }

   public AffineTransform scale(double sx, double sy, double sz) {
      return this.concatenate(sx, 0.0, 0.0, 0.0, 0.0, sy, 0.0, 0.0, 0.0, 0.0, sz, 0.0);
   }

   public AffineTransform scale(Vector3 vec) {
      return this.scale(vec.x(), vec.y(), vec.z());
   }

   public boolean isScaled(Vector3 vector) {
      boolean flip = vector.x() != 0.0 && this.m00 < 0.0;
      if (vector.y() != 0.0 && this.m11 < 0.0) {
         flip = !flip;
      }

      if (vector.z() != 0.0 && this.m22 < 0.0) {
         flip = !flip;
      }

      if (flip) {
         return true;
      } else {
         if (vector.x() != 0.0 && vector.y() != 0.0 && (this.m01 < 0.0 && this.m10 < 0.0 || this.m01 > 0.0 && this.m10 > 0.0)) {
            flip = true;
         }

         if (vector.x() != 0.0 && vector.z() != 0.0 && (this.m02 < 0.0 && this.m20 < 0.0 || this.m02 > 0.0 && this.m20 > 0.0)) {
            flip = !flip;
         }

         if (vector.y() != 0.0 && vector.z() != 0.0 && (this.m12 < 0.0 && this.m21 < 0.0 || this.m12 > 0.0 && this.m21 > 0.0)) {
            flip = !flip;
         }

         return flip;
      }
   }

   @Override
   public Vector3 apply(Vector3 vector) {
      double x = vector.x() * this.m00 + vector.y() * this.m01 + vector.z() * this.m02 + this.m03;
      double y = vector.x() * this.m10 + vector.y() * this.m11 + vector.z() * this.m12 + this.m13;
      double z = vector.x() * this.m20 + vector.y() * this.m21 + vector.z() * this.m22 + this.m23;
      Vector3 var8 = vector.mutX(x);
      Vector3 var9 = var8.mutY(y);
      return var9.mutZ(z);
   }

   public AffineTransform combine(AffineTransform other) {
      return this.concatenate(other);
   }

   @Override
   public Transform combine(Transform other) {
      if (other instanceof Identity || other.isIdentity()) {
         return this;
      } else {
         return (Transform)(other instanceof AffineTransform otherTransform ? this.concatenate(otherTransform) : new CombinedTransform(this, other));
      }
   }

   public boolean isHorizontalFlip() {
      return this.m00 * this.m22 - this.m02 * this.m20 < 0.0;
   }

   public boolean isVerticalFlip() {
      return this.m11 < 0.0;
   }

   @Override
   public String toString() {
      return String.format(
         "Affine[%g %g %g %g, %g %g %g %g, %g %g %g %g]}",
         this.m00,
         this.m01,
         this.m02,
         this.m03,
         this.m10,
         this.m11,
         this.m12,
         this.m13,
         this.m20,
         this.m21,
         this.m22,
         this.m23
      );
   }
}
