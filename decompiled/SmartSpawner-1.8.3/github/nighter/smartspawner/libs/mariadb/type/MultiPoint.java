package github.nighter.smartspawner.libs.mariadb.type;

import java.util.Arrays;

public class MultiPoint implements Geometry {
   private final Point[] points;

   public MultiPoint(Point[] points) {
      this.points = points;
   }

   public Point[] getPoints() {
      return this.points;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("MULTIPOINT(");
      int index = 0;

      for (Point pt : this.points) {
         if (index++ > 0) {
            sb.append(",");
         }

         sb.append(pt.getX()).append(" ").append(pt.getY());
      }

      sb.append(")");
      return sb.toString();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o != null && o instanceof MultiPoint ? this.toString().equals(o.toString()) : false;
      }
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode((Object[])this.points);
   }
}
