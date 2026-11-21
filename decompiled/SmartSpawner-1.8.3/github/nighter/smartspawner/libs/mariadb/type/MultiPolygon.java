package github.nighter.smartspawner.libs.mariadb.type;

import java.util.Arrays;

public class MultiPolygon implements Geometry {
   private final Polygon[] polygons;

   public MultiPolygon(Polygon[] polygons) {
      this.polygons = polygons;
   }

   public Polygon[] getPolygons() {
      return this.polygons;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("MULTIPOLYGON(");
      int indexpoly = 0;

      for (Polygon poly : this.polygons) {
         if (indexpoly++ > 0) {
            sb.append(",");
         }

         sb.append("(");
         int indexLine = 0;

         for (LineString ls : poly.getLines()) {
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
      }

      sb.append(")");
      return sb.toString();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o != null && o instanceof MultiPolygon ? this.toString().equals(o.toString()) : false;
      }
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode((Object[])this.polygons);
   }
}
