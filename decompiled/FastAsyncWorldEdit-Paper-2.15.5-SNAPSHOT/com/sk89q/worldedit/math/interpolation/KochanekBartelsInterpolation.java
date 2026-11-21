package com.sk89q.worldedit.math.interpolation;

import com.fastasyncworldedit.core.math.MutableVector3;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.Vector3;
import java.util.Collections;
import java.util.List;

public class KochanekBartelsInterpolation implements Interpolation {
   private List<Node> nodes;
   private Vector3[] coeffA;
   private Vector3[] coeffB;
   private Vector3[] coeffC;
   private Vector3[] coeffD;
   private double scaling;
   private final MutableVector3 mutable = new MutableVector3();

   public KochanekBartelsInterpolation() {
      this.setNodes(Collections.emptyList());
   }

   @Override
   public void setNodes(List<Node> nodes) {
      Preconditions.checkNotNull(nodes);
      this.nodes = nodes;
      this.recalc();
   }

   private void recalc() {
      int nNodes = this.nodes.size();
      this.coeffA = new Vector3[nNodes];
      this.coeffB = new Vector3[nNodes];
      this.coeffC = new Vector3[nNodes];
      this.coeffD = new Vector3[nNodes];
      if (nNodes != 0) {
         Node nodeB = this.nodes.get(0);
         double tensionB = nodeB.getTension();
         double biasB = nodeB.getBias();
         double continuityB = nodeB.getContinuity();

         for (int i = 0; i < nNodes; i++) {
            double tensionA = tensionB;
            double biasA = biasB;
            double continuityA = continuityB;
            if (i + 1 < nNodes) {
               nodeB = this.nodes.get(i + 1);
               tensionB = nodeB.getTension();
               biasB = nodeB.getBias();
               continuityB = nodeB.getContinuity();
            }

            double ta = (1.0 - tensionA) * (1.0 + biasA) * (1.0 + continuityA) / 2.0;
            double tb = (1.0 - tensionA) * (1.0 - biasA) * (1.0 - continuityA) / 2.0;
            double tc = (1.0 - tensionB) * (1.0 + biasB) * (1.0 - continuityB) / 2.0;
            double td = (1.0 - tensionB) * (1.0 - biasB) * (1.0 + continuityB) / 2.0;
            this.coeffA[i] = this.linearCombination(i, -ta, ta - tb - tc + 2.0, tb + tc - td - 2.0, td);
            this.coeffB[i] = this.linearCombination(i, 2.0 * ta, -2.0 * ta + 2.0 * tb + tc - 3.0, -2.0 * tb - tc + td + 3.0, -td);
            this.coeffC[i] = this.linearCombination(i, -ta, ta - tb, tb, 0.0);
            this.coeffD[i] = this.retrieve(i);
         }

         this.scaling = this.nodes.size() - 1;
      }
   }

   private Vector3 linearCombination(int baseIndex, double f1, double f2, double f3, double f4) {
      Vector3 r1 = this.retrieve(baseIndex - 1).multiply(f1);
      Vector3 r2 = this.retrieve(baseIndex).multiply(f2);
      Vector3 r3 = this.retrieve(baseIndex + 1).multiply(f3);
      Vector3 r4 = this.retrieve(baseIndex + 2).multiply(f4);
      return r1.add(r2).add(r3).add(r4);
   }

   private Vector3 retrieve(int index) {
      if (index < 0) {
         return this.fastRetrieve(0);
      } else {
         return index >= this.nodes.size() ? this.fastRetrieve(this.nodes.size() - 1) : this.fastRetrieve(index);
      }
   }

   private Vector3 fastRetrieve(int index) {
      return this.nodes.get(index).getPosition();
   }

   @Override
   public Vector3 getPosition(double position) {
      if (this.coeffA == null) {
         throw new IllegalStateException("Must call setNodes first.");
      } else if (position > 1.0) {
         return null;
      } else {
         position *= this.scaling;
         int index = (int)Math.floor(position);
         double remainder = position - index;
         Vector3 a = this.coeffA[index];
         Vector3 b = this.coeffB[index];
         Vector3 c = this.coeffC[index];
         Vector3 d = this.coeffD[index];
         double r2 = remainder * remainder;
         double r3 = r2 * remainder;
         this.mutable.mutX(a.x() * r3 + b.x() * r2 + c.x() * remainder + d.x());
         this.mutable.mutY(a.y() * r3 + b.y() * r2 + c.y() * remainder + d.y());
         this.mutable.mutZ(a.z() * r3 + b.z() * r2 + c.z() * remainder + d.z());
         return Vector3.at(this.mutable.x(), this.mutable.y(), this.mutable.z());
      }
   }

   @Override
   public Vector3 get1stDerivative(double position) {
      if (this.coeffA == null) {
         throw new IllegalStateException("Must call setNodes first.");
      } else if (position > 1.0) {
         return null;
      } else {
         position *= this.scaling;
         int index = (int)Math.floor(position);
         Vector3 a = this.coeffA[index];
         Vector3 b = this.coeffB[index];
         Vector3 c = this.coeffC[index];
         return a.multiply(1.5 * position - 3.0 * index)
            .add(b)
            .multiply(2.0 * position)
            .add(a.multiply(1.5 * index).subtract(b).multiply(2.0 * index))
            .add(c)
            .multiply(this.scaling);
      }
   }

   @Override
   public double arcLength(double positionA, double positionB) {
      if (this.coeffA == null) {
         throw new IllegalStateException("Must call setNodes first.");
      } else if (positionA > positionB) {
         return this.arcLength(positionB, positionA);
      } else {
         positionA *= this.scaling;
         positionB *= this.scaling;
         int indexA = (int)Math.floor(positionA);
         double remainderA = positionA - indexA;
         int indexB = (int)Math.floor(positionB);
         double remainderB = positionB - indexB;
         return this.arcLengthRecursive(indexA, remainderA, indexB, remainderB);
      }
   }

   private double arcLengthRecursive(int indexLeft, double remainderLeft, int indexRight, double remainderRight) {
      switch (indexRight - indexLeft) {
         case 0:
            return this.arcLengthRecursive(indexLeft, remainderLeft, remainderRight);
         case 1:
            return this.arcLengthRecursive(indexLeft, remainderLeft, 1.0) + this.arcLengthRecursive(indexRight, 0.0, remainderRight);
         default:
            return this.arcLengthRecursive(indexLeft, remainderLeft, indexRight - 1, 1.0) + this.arcLengthRecursive(indexRight, 0.0, remainderRight);
      }
   }

   private double arcLengthRecursive(int index, double remainderLeft, double remainderRight) {
      Vector3 a = this.coeffA[index].multiply(3.0);
      Vector3 b = this.coeffB[index].multiply(2.0);
      Vector3 c = this.coeffC[index];
      int nPoints = 8;
      double accum = a.multiply(remainderLeft).add(b).multiply(remainderLeft).add(c).length() / 2.0;

      for (int i = 1; i < 7; i++) {
         double t = i / 8.0;
         t = (remainderRight - remainderLeft) * t + remainderLeft;
         accum += a.multiply(t).add(b).multiply(t).add(c).length();
      }

      accum += a.multiply(remainderRight).add(b).multiply(remainderRight).add(c).length() / 2.0;
      return accum * (remainderRight - remainderLeft) / 8.0;
   }

   @Override
   public int getSegment(double position) {
      if (this.coeffA == null) {
         throw new IllegalStateException("Must call setNodes first.");
      } else if (position > 1.0) {
         return Integer.MAX_VALUE;
      } else {
         position *= this.scaling;
         return (int)Math.floor(position);
      }
   }
}
