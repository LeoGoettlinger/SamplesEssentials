package org.jdbi.v3.core.result;

import java.lang.reflect.Type;
import java.util.List;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.meta.Alpha;
import org.jdbi.v3.meta.Beta;

public abstract class RowView {
   public <T> T getRow(Class<T> rowType) {
      return rowType.cast(this.getRow(rowType));
   }

   @Alpha
   public <T> T getRow(Class<T> rowType, String prefix) {
      return rowType.cast(this.getRow(rowType, prefix));
   }

   public <T> T getRow(GenericType<T> rowType) {
      return (T)this.getRow(rowType.getType());
   }

   @Alpha
   public <T> T getRow(GenericType<T> rowType, String prefix) {
      return (T)this.getRow(rowType.getType(), prefix);
   }

   public abstract Object getRow(Type type);

   @Alpha
   public Object getRow(Type type, String prefix) {
      throw new UnsupportedOperationException("getRow by prefix is not supported by " + this.getClass().getName());
   }

   @Beta
   public List<String> getColumnNames() {
      throw new UnsupportedOperationException("getColumnNames is not supported by " + this.getClass().getName());
   }

   @Beta
   public <T> T getRow(RowMapper<T> mapper) {
      throw new UnsupportedOperationException("getRow with a mapper is not supported by " + this.getClass().getName());
   }

   @Beta
   public Object getColumn(String column) {
      throw new UnsupportedOperationException("getColumn without a type is not supported by " + this.getClass().getName());
   }

   @Beta
   public Object getColumn(int column) {
      throw new UnsupportedOperationException("getColumn without a type is not supported by " + this.getClass().getName());
   }

   public <T> T getColumn(String column, Class<T> type) {
      return type.cast(this.getColumn(column, type));
   }

   public <T> T getColumn(int column, Class<T> type) {
      return type.cast(this.getColumn(column, type));
   }

   public <T> T getColumn(String column, GenericType<T> type) {
      return (T)this.getColumn(column, type.getType());
   }

   public abstract <T> T getColumn(int column, QualifiedType<T> type);

   public abstract <T> T getColumn(String column, QualifiedType<T> type);

   public <T> T getColumn(int column, GenericType<T> type) {
      return (T)this.getColumn(column, type.getType());
   }

   public Object getColumn(String column, Type type) {
      return this.getColumn(column, QualifiedType.of(type));
   }

   public Object getColumn(int column, Type type) {
      return this.getColumn(column, QualifiedType.of(type));
   }
}
