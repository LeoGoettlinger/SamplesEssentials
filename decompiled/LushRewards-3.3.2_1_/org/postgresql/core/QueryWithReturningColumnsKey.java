package org.postgresql.core;

import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.Nullable;

class QueryWithReturningColumnsKey extends BaseQueryKey {
   public final String[] columnNames;
   private int size;

   QueryWithReturningColumnsKey(String sql, boolean isParameterized, boolean escapeProcessing, @Nullable String[] columnNames) {
      super(sql, isParameterized, escapeProcessing);
      if (columnNames == null) {
         columnNames = new String[]{"*"};
      }

      this.columnNames = columnNames;
   }

   @Override
   public long getSize() {
      int size = this.size;
      if (size != 0) {
         return size;
      } else {
         size = (int)super.getSize();
         if (this.columnNames != null) {
            size = (int)(size + 16L);

            for (String columnName : this.columnNames) {
               size = (int)(size + columnName.length() * 2L);
            }
         }

         this.size = size;
         return size;
      }
   }

   @Override
   public String toString() {
      return "QueryWithReturningColumnsKey{sql='"
         + this.sql
         + '\''
         + ", isParameterized="
         + this.isParameterized
         + ", escapeProcessing="
         + this.escapeProcessing
         + ", columnNames="
         + Arrays.toString((Object[])this.columnNames)
         + '}';
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o == null || this.getClass() != o.getClass()) {
         return false;
      } else if (!super.equals(o)) {
         return false;
      } else {
         QueryWithReturningColumnsKey that = (QueryWithReturningColumnsKey)o;
         return Arrays.equals((Object[])this.columnNames, (Object[])that.columnNames);
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      return 31 * result + Arrays.hashCode((Object[])this.columnNames);
   }
}
