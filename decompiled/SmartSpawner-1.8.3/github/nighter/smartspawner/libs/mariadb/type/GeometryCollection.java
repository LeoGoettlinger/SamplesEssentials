package github.nighter.smartspawner.libs.mariadb.type;

import java.util.Arrays;

public class GeometryCollection implements Geometry {
   private final Geometry[] geometries;

   public GeometryCollection(Geometry[] geometries) {
      this.geometries = geometries;
   }

   public Geometry[] getGeometries() {
      return this.geometries;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("GEOMETRYCOLLECTION(");
      int indexpoly = 0;

      for (Geometry geo : this.geometries) {
         if (indexpoly++ > 0) {
            sb.append(",");
         }

         sb.append(geo.toString());
      }

      sb.append(")");
      return sb.toString();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o != null && o instanceof GeometryCollection ? this.toString().equals(o.toString()) : false;
      }
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode((Object[])this.geometries);
   }
}
