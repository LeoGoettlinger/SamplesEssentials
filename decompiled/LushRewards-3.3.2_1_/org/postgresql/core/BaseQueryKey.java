package org.postgresql.core;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.CanEstimateSize;

class BaseQueryKey implements CanEstimateSize {
   public final String sql;
   public final boolean isParameterized;
   public final boolean escapeProcessing;

   BaseQueryKey(String sql, boolean isParameterized, boolean escapeProcessing) {
      this.sql = sql;
      this.isParameterized = isParameterized;
      this.escapeProcessing = escapeProcessing;
   }

   @Override
   public String toString() {
      return "BaseQueryKey{sql='" + this.sql + '\'' + ", isParameterized=" + this.isParameterized + ", escapeProcessing=" + this.escapeProcessing + '}';
   }

   @Override
   public long getSize() {
      return this.sql == null ? 16L : 16L + this.sql.length() * 2L;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BaseQueryKey that = (BaseQueryKey)o;
         if (this.isParameterized != that.isParameterized) {
            return false;
         } else if (this.escapeProcessing != that.escapeProcessing) {
            return false;
         } else {
            return this.sql != null ? this.sql.equals(that.sql) : that.sql == null;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.sql != null ? this.sql.hashCode() : 0;
      result = 31 * result + (this.isParameterized ? 1 : 0);
      return 31 * result + (this.escapeProcessing ? 1 : 0);
   }
}
