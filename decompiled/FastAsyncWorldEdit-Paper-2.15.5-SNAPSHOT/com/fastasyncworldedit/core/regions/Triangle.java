package com.fastasyncworldedit.core.regions;

import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.StringMan;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.polyhedron.Edge;

public class Triangle implements Cloneable {
   public static double RADIUS = 0.5;
   private final BlockVector3 pos1;
   private final BlockVector3 pos2;
   private final BlockVector3 pos3;
   private final double[][] verts = new double[3][3];
   private final double[] center = new double[3];
   private final double[] radius = new double[3];
   private final double[] v0 = new double[3];
   private final double[] v1 = new double[3];
   private final double[] v2 = new double[3];
   private final double[] normal = new double[3];
   private final double[] e0 = new double[3];
   private final double[] e1 = new double[3];
   private final double[] e2 = new double[3];
   private final double[] vmin = new double[3];
   private final double[] vmax = new double[3];
   private final BlockVector3 normalVec;
   private final double b;

   public Triangle(BlockVector3 pos1, BlockVector3 pos2, BlockVector3 pos3) {
      this.pos1 = pos1;
      this.pos2 = pos2;
      this.pos3 = pos3;
      this.verts[0] = new double[]{pos1.x(), pos1.y(), pos1.z()};
      this.verts[1] = new double[]{pos2.x(), pos2.y(), pos2.z()};
      this.verts[2] = new double[]{pos3.x(), pos3.y(), pos3.z()};
      this.radius[0] = RADIUS;
      this.radius[1] = RADIUS;
      this.radius[2] = RADIUS;
      this.normalVec = pos2.subtract(pos1).cross(pos3.subtract(pos1)).normalize();
      this.b = Math.max(Math.max(this.normalVec.dot(pos1), this.normalVec.dot(pos2)), this.normalVec.dot(pos3));
   }

   public boolean above(BlockVector3 pt) {
      Preconditions.checkNotNull(pt);
      return this.normalVec.dot(pt) > this.b;
   }

   public Edge getEdge(int index) {
      return index == this.verts.length - 1
         ? new Edge(
            Vector3.at(this.verts[index][0], this.verts[index][1], this.verts[index][2]), Vector3.at(this.verts[0][0], this.verts[0][1], this.verts[0][2])
         )
         : new Edge(
            Vector3.at(this.verts[index][0], this.verts[index][1], this.verts[index][2]),
            Vector3.at(this.verts[index + 1][0], this.verts[index + 1][1], this.verts[index + 1][2])
         );
   }

   @Override
   public String toString() {
      return StringMan.getString(this.verts);
   }

   public BlockVector3 getVertex(int index) {
      return BlockVector3.at(this.verts[index][0], this.verts[index][1], this.verts[index][2]);
   }

   public boolean contains(BlockVector3 pos) {
      this.center[0] = pos.x() + RADIUS;
      this.center[1] = pos.y() + RADIUS;
      this.center[2] = pos.z() + RADIUS;
      return this.overlaps(this.center, this.radius, this.verts);
   }

   private void sub(double[] dest, double[] v1, double[] v2) {
      dest[0] = v1[0] - v2[0];
      dest[1] = v1[1] - v2[1];
      dest[2] = v1[2] - v2[2];
   }

   private void cross(double[] dest, double[] v1, double[] v2) {
      dest[0] = v1[1] * v2[2] - v1[2] * v2[1];
      dest[1] = v1[2] * v2[0] - v1[0] * v2[2];
      dest[2] = v1[0] * v2[1] - v1[1] * v2[0];
   }

   private double dot(double[] v1, double[] v2) {
      return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
   }

   private boolean axisTestX01(double a, double b, double fa, double fb) {
      double p0 = a * this.v0[1] - b * this.v0[2];
      double p2 = a * this.v2[1] - b * this.v2[2];
      double min;
      double max;
      if (p0 < p2) {
         min = p0;
         max = p2;
      } else {
         min = p2;
         max = p0;
      }

      double rad = fa * this.radius[1] + fb * this.radius[2];
      return !(min > rad) && !(max < -rad);
   }

   private boolean axisTestX2(double a, double b, double fa, double fb) {
      double p0 = a * this.v0[1] - b * this.v0[2];
      double p1 = a * this.v1[1] - b * this.v1[2];
      double min;
      double max;
      if (p0 < p1) {
         min = p0;
         max = p1;
      } else {
         min = p1;
         max = p0;
      }

      double rad = fa * this.radius[1] + fb * this.radius[2];
      return !(min > rad) && !(max < -rad);
   }

   private boolean axisTestY02(double a, double b, double fa, double fb) {
      double p0 = -a * this.v0[0] + b * this.v0[2];
      double p2 = -a * this.v2[0] + b * this.v2[2];
      double min;
      double max;
      if (p0 < p2) {
         min = p0;
         max = p2;
      } else {
         min = p2;
         max = p0;
      }

      double rad = fa * this.radius[0] + fb * this.radius[2];
      return !(min > rad) && !(max < -rad);
   }

   private boolean axisTestY1(double a, double b, double fa, double fb) {
      double p0 = -a * this.v0[0] + b * this.v0[2];
      double p1 = -a * this.v1[0] + b * this.v1[2];
      double min;
      double max;
      if (p0 < p1) {
         min = p0;
         max = p1;
      } else {
         min = p1;
         max = p0;
      }

      double rad = fa * this.radius[0] + fb * this.radius[2];
      return !(min > rad) && !(max < -rad);
   }

   private boolean axisTestZ12(double a, double b, double fa, double fb) {
      double p1 = a * this.v1[0] - b * this.v1[1];
      double p2 = a * this.v2[0] - b * this.v2[1];
      double min;
      double max;
      if (p2 < p1) {
         min = p2;
         max = p1;
      } else {
         min = p1;
         max = p2;
      }

      double rad = fa * this.radius[0] + fb * this.radius[1];
      return !(min > rad) && !(max < -rad);
   }

   private boolean axisTestZ0(double a, double b, double fa, double fb) {
      double p0 = a * this.v0[0] - b * this.v0[1];
      double p1 = a * this.v1[0] - b * this.v1[1];
      double min;
      double max;
      if (p0 < p1) {
         min = p0;
         max = p1;
      } else {
         min = p1;
         max = p0;
      }

      double rad = fa * this.radius[0] + fb * this.radius[1];
      return !(min > rad) && !(max < -rad);
   }

   private boolean overlaps(double[] boxcenter, double[] boxhalfsize, double[][] triverts) {
      this.sub(this.v0, triverts[0], boxcenter);
      this.sub(this.v1, triverts[1], boxcenter);
      this.sub(this.v2, triverts[2], boxcenter);
      this.sub(this.e0, this.v1, this.v0);
      this.sub(this.e1, this.v2, this.v1);
      this.sub(this.e2, this.v0, this.v2);
      double fex = Math.abs(this.e0[0]);
      double fey = Math.abs(this.e0[1]);
      double fez = Math.abs(this.e0[2]);
      if (!this.axisTestX01(this.e0[2], this.e0[1], fez, fey)) {
         return false;
      } else if (!this.axisTestY02(this.e0[2], this.e0[0], fez, fex)) {
         return false;
      } else if (!this.axisTestZ12(this.e0[1], this.e0[0], fey, fex)) {
         return false;
      } else {
         fex = Math.abs(this.e1[0]);
         fey = Math.abs(this.e1[1]);
         fez = Math.abs(this.e1[2]);
         if (!this.axisTestX01(this.e1[2], this.e1[1], fez, fey)) {
            return false;
         } else if (!this.axisTestY02(this.e1[2], this.e1[0], fez, fex)) {
            return false;
         } else if (!this.axisTestZ0(this.e1[1], this.e1[0], fey, fex)) {
            return false;
         } else {
            fex = Math.abs(this.e2[0]);
            fey = Math.abs(this.e2[1]);
            fez = Math.abs(this.e2[2]);
            if (!this.axisTestX2(this.e2[2], this.e2[1], fez, fey)) {
               return false;
            } else if (!this.axisTestY1(this.e2[2], this.e2[0], fez, fex)) {
               return false;
            } else if (!this.axisTestZ12(this.e2[1], this.e2[0], fey, fex)) {
               return false;
            } else {
               double max = MathMan.max(this.v0[0], this.v1[0], this.v2[0]);
               double min = MathMan.min(this.v0[0], this.v1[0], this.v2[0]);
               if (!(min > boxhalfsize[0]) && !(max < -boxhalfsize[0])) {
                  max = MathMan.max(this.v0[1], this.v1[1], this.v2[1]);
                  min = MathMan.min(this.v0[1], this.v1[1], this.v2[1]);
                  if (!(min > boxhalfsize[1]) && !(max < -boxhalfsize[1])) {
                     max = MathMan.max(this.v0[2], this.v1[2], this.v2[2]);
                     min = MathMan.min(this.v0[2], this.v1[2], this.v2[2]);
                     if (!(min > boxhalfsize[2]) && !(max < -boxhalfsize[2])) {
                        this.cross(this.normal, this.e0, this.e1);
                        return this.planeBoxOverlap(this.normal, this.v0, boxhalfsize);
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            }
         }
      }
   }

   private boolean planeBoxOverlap(double[] normal, double[] vert, double[] maxbox) {
      for (int q = 0; q <= 2; q++) {
         double v = vert[q];
         if (normal[q] > 0.0) {
            this.vmin[q] = -maxbox[q] - v;
            this.vmax[q] = maxbox[q] - v;
         } else {
            this.vmin[q] = maxbox[q] - v;
            this.vmax[q] = -maxbox[q] - v;
         }
      }

      return this.dot(normal, this.vmin) > 0.0 ? false : this.dot(normal, this.vmax) >= 0.0;
   }

   public Triangle clone() {
      return new Triangle(this.pos1, this.pos2, this.pos3);
   }
}
