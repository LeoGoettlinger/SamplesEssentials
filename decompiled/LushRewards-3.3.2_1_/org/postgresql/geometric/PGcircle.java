package org.postgresql.geometric;

import java.io.Serializable;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.GT;
import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class PGcircle extends PGobject implements Serializable, Cloneable {
   @Nullable
   public PGpoint center;
   public double radius;

   public PGcircle(double x, double y, double r) {
      this(new PGpoint(x, y), r);
   }

   public PGcircle(PGpoint c, double r) {
      this();
      this.center = c;
      this.radius = r;
   }

   public PGcircle(String s) throws SQLException {
      this();
      this.setValue(s);
   }

   public PGcircle() {
      this.type = "circle";
   }

   @Override
   public void setValue(@Nullable String s) throws SQLException {
      if (s == null) {
         this.center = null;
      } else {
         PGtokenizer t = new PGtokenizer(PGtokenizer.removeAngle(s), ',');
         if (t.getSize() != 2) {
            throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, s), PSQLState.DATA_TYPE_MISMATCH);
         } else {
            try {
               this.center = new PGpoint(t.getToken(0));
               this.radius = Double.parseDouble(t.getToken(1));
            } catch (NumberFormatException var4) {
               throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, s), PSQLState.DATA_TYPE_MISMATCH, var4);
            }
         }
      }
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (!(obj instanceof PGcircle)) {
         return false;
      } else {
         PGcircle p = (PGcircle)obj;
         PGpoint center = this.center;
         PGpoint pCenter = p.center;
         if (center == null) {
            return pCenter == null;
         } else {
            return pCenter == null ? false : p.radius == this.radius && equals(pCenter, center);
         }
      }
   }

   @Override
   public int hashCode() {
      if (this.center == null) {
         return 0;
      } else {
         long bits = Double.doubleToLongBits(this.radius);
         int v = (int)(bits ^ bits >>> 32);
         return v * 31 + this.center.hashCode();
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      PGcircle newPGcircle = (PGcircle)super.clone();
      if (newPGcircle.center != null) {
         newPGcircle.center = (PGpoint)newPGcircle.center.clone();
      }

      return newPGcircle;
   }

   @Nullable
   @Override
   public String getValue() {
      return this.center == null ? null : "<" + this.center + "," + this.radius + ">";
   }
}
