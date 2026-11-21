package org.postgresql.util;

import java.io.Serializable;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.internal.Nullness;

public class PGobject implements Serializable, Cloneable {
   @Nullable
   protected String type;
   @Nullable
   protected String value;

   public final void setType(String type) {
      this.type = type;
   }

   public void setValue(@Nullable String value) throws SQLException {
      this.value = value;
   }

   public final String getType() {
      return Nullness.castNonNull(this.type, "PGobject#type is uninitialized. Please call setType(String)");
   }

   @Nullable
   public String getValue() {
      return this.value;
   }

   public boolean isNull() {
      return this.getValue() == null;
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj instanceof PGobject) {
         Object otherValue = ((PGobject)obj).getValue();
         return otherValue == null ? this.getValue() == null : otherValue.equals(this.getValue());
      } else {
         return false;
      }
   }

   @Override
   public Object clone() throws CloneNotSupportedException {
      return super.clone();
   }

   @Override
   public String toString() {
      return this.getValue();
   }

   @Override
   public int hashCode() {
      String value = this.getValue();
      return value != null ? value.hashCode() : 0;
   }

   protected static boolean equals(@Nullable Object a, @Nullable Object b) {
      return a == b || a != null && a.equals(b);
   }
}
