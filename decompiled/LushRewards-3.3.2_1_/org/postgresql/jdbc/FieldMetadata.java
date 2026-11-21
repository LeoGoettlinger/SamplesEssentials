package org.postgresql.jdbc;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.CanEstimateSize;

public class FieldMetadata implements CanEstimateSize {
   final String columnName;
   final String tableName;
   final String schemaName;
   final int nullable;
   final boolean autoIncrement;

   public FieldMetadata(String columnName) {
      this(columnName, "", "", 2, false);
   }

   FieldMetadata(String columnName, String tableName, String schemaName, int nullable, boolean autoIncrement) {
      this.columnName = columnName;
      this.tableName = tableName;
      this.schemaName = schemaName;
      this.nullable = nullable;
      this.autoIncrement = autoIncrement;
   }

   @Override
   public long getSize() {
      return this.columnName.length() * 2 + this.tableName.length() * 2 + this.schemaName.length() * 2 + 4L + 1L;
   }

   @Override
   public String toString() {
      return "FieldMetadata{columnName='"
         + this.columnName
         + '\''
         + ", tableName='"
         + this.tableName
         + '\''
         + ", schemaName='"
         + this.schemaName
         + '\''
         + ", nullable="
         + this.nullable
         + ", autoIncrement="
         + this.autoIncrement
         + '}';
   }

   public static class Key {
      final int tableOid;
      final int positionInTable;

      Key(int tableOid, int positionInTable) {
         this.positionInTable = positionInTable;
         this.tableOid = tableOid;
      }

      @Override
      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            FieldMetadata.Key key = (FieldMetadata.Key)o;
            return this.tableOid != key.tableOid ? false : this.positionInTable == key.positionInTable;
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.tableOid;
         return 31 * result + this.positionInTable;
      }

      @Override
      public String toString() {
         return "Key{tableOid=" + this.tableOid + ", positionInTable=" + this.positionInTable + '}';
      }
   }
}
