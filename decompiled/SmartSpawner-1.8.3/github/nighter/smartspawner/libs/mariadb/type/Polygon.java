package github.nighter.smartspawner.libs.mariadb.type;

import java.util.Arrays;

public class Polygon implements Geometry {
   private final LineString[] lines;

   public Polygon(LineString[] lines) {
      this.lines = lines;
   }

   public LineString[] getLines() {
      return this.lines;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("POLYGON(");
      int indexLine = 0;

      for (LineString ls : this.lines) {
         if (indexLine++ > 0) {
            sb.append(",");
         }

         sb.append("(");
         int index = 0;

         for (Point pt : ls.getPoints()) {
            if (index++ > 0) {
               sb.append(",");
            }

            sb.append(pt.getX()).append(" ").append(pt.getY());
         }

         sb.append(")");
      }

      sb.append(")");
      return sb.toString();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o != null && o instanceof Polygon ? this.toString().equals(o.toString()) : false;
      }
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode((Object[])this.lines);
   }
}
