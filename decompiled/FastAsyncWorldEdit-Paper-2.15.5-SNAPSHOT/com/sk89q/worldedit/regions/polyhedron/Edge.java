package com.sk89q.worldedit.regions.polyhedron;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.Vector3;

public class Edge {
   private final Vector3 start;
   private final Vector3 end;

   public Edge(Vector3 start, Vector3 end) {
      Preconditions.checkNotNull(start);
      Preconditions.checkNotNull(end);
      this.start = start;
      this.end = end;
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof Edge otherEdge)) {
         return false;
      } else {
         return this.start == otherEdge.end && this.end == otherEdge.start ? true : this.end == otherEdge.end && this.start == otherEdge.start;
      }
   }

   @Override
   public int hashCode() {
      return this.start.hashCode() ^ this.end.hashCode();
   }

   @Override
   public String toString() {
      return "(" + this.start + "," + this.end + ")";
   }

   public Triangle createTriangle(Vector3 vertex) {
      Preconditions.checkNotNull(vertex);
      return new Triangle(this.start, this.end, vertex);
   }

   public Triangle createTriangle2(Vector3 vertex) {
      Preconditions.checkNotNull(vertex);
      return new Triangle(this.start, vertex, this.end);
   }
}
