package github.nighter.smartspawner.libs.mariadb.type;

import java.util.Arrays;
import java.util.Objects;

public class LineString implements Geometry {
   private final Point[] points;
   private final boolean open;

   public LineString(Point[] points, boolean open) {
      this.points = points;
      this.open = open;
   }

   public Point[] getPoints() {
      return this.points;
   }

   public boolean isOpen() {
      return this.open;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("LINESTRING(");
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
         return o != null && o instanceof LineString ? this.open == ((LineString)o).isOpen() && this.toString().equals(o.toString()) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = Objects.hash(this.open);
      return 31 * result + Arrays.hashCode((Object[])this.points);
   }
}
