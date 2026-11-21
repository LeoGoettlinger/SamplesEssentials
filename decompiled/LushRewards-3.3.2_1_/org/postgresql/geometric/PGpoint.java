package org.postgresql.geometric;

import java.awt.Point;
import java.io.Serializable;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.ByteConverter;
import org.postgresql.util.GT;
import org.postgresql.util.PGBinaryObject;
import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

public class PGpoint extends PGobject implements PGBinaryObject, Serializable, Cloneable {
   public double x;
   public double y;
   public boolean isNull;

   public PGpoint(double x, double y) {
      this();
      this.x = x;
      this.y = y;
   }

   public PGpoint(String value) throws SQLException {
      this();
      this.setValue(value);
   }

   public PGpoint() {
      this.type = "point";
   }

   @Override
   public void setValue(@Nullable String s) throws SQLException {
      this.isNull = s == null;
      if (s != null) {
         PGtokenizer t = new PGtokenizer(PGtokenizer.removePara(s), ',');

         try {
            this.x = Double.parseDouble(t.getToken(0));
            this.y = Double.parseDouble(t.getToken(1));
         } catch (NumberFormatException var4) {
            throw new PSQLException(GT.tr("Conversion to type {0} failed: {1}.", this.type, s), PSQLState.DATA_TYPE_MISMATCH, var4);
         }
      }
   }

   @Override
   public void setByteValue(byte[] b, int offset) {
      this.isNull = false;
      this.x = ByteConverter.float8(b, offset);
      this.y = ByteConverter.float8(b, offset + 8);
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (!(obj instanceof PGpoint)) {
         return false;
      } else {
         PGpoint p = (PGpoint)obj;
         if (this.isNull) {
            return p.isNull;
         } else {
            return p.isNull ? false : this.x == p.x && this.y == p.y;
         }
      }
   }

   @Override
   public int hashCode() {
      if (this.isNull) {
         return 0;
      } else {
         long v1 = Double.doubleToLongBits(this.x);
         long v2 = Double.doubleToLongBits(this.y);
         return (int)(v1 ^ v2 ^ v1 >>> 32 ^ v2 >>> 32);
      }
   }

   @Nullable
   @Override
   public String getValue() {
      return this.isNull ? null : "(" + this.x + "," + this.y + ")";
   }

   @Override
   public int lengthInBytes() {
      return this.isNull ? 0 : 16;
   }

   @Override
   public void toBytes(byte[] b, int offset) {
      if (!this.isNull) {
         ByteConverter.float8(b, offset, this.x);
         ByteConverter.float8(b, offset + 8, this.y);
      }
   }

   public void translate(int x, int y) {
      this.translate((double)x, (double)y);
   }

   public void translate(double x, double y) {
      this.isNull = false;
      this.x += x;
      this.y += y;
   }

   public void move(int x, int y) {
      this.setLocation(x, y);
   }

   public void move(double x, double y) {
      this.isNull = false;
      this.x = x;
      this.y = y;
   }

   public void setLocation(int x, int y) {
      this.move((double)x, (double)y);
   }

   @Deprecated
   public void setLocation(Point p) {
      this.setLocation(p.x, p.y);
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }
}
