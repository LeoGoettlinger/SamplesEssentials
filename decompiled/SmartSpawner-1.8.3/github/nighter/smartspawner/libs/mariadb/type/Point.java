package github.nighter.smartspawner.libs.mariadb.type;

import java.util.Objects;

public class Point implements Geometry {
   private final double x;
   private final double y;

   public Point(double x, double y) {
      this.x = x;
      this.y = y;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   @Override
   public String toString() {
      return "POINT(" + this.x + " " + this.y + ")";
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o != null && o instanceof Point ? this.toString().equals(o.toString()) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y);
   }
}
