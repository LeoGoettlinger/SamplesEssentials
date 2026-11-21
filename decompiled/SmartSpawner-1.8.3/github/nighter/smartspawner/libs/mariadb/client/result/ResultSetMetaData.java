package github.nighter.smartspawner.libs.mariadb.client.result;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.client.Column;
import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.export.ExceptionFactory;
import github.nighter.smartspawner.libs.mariadb.util.constants.CatalogTerm;
import java.sql.SQLException;

public class ResultSetMetaData implements java.sql.ResultSetMetaData {
   private final ExceptionFactory exceptionFactory;
   private final ColumnDecoder[] fieldPackets;
   private final Configuration conf;
   private final boolean forceAlias;

   public ResultSetMetaData(ExceptionFactory exceptionFactory, ColumnDecoder[] fieldPackets, Configuration conf, boolean forceAlias) {
      this.exceptionFactory = exceptionFactory;
      this.fieldPackets = fieldPackets;
      this.conf = conf;
      this.forceAlias = forceAlias;
   }

   @Override
   public int getColumnCount() {
      return this.fieldPackets.length;
   }

   @Override
   public boolean isAutoIncrement(int column) throws SQLException {
      return (this.getColumn(column).getFlags() & 512) != 0;
   }

   @Override
   public boolean isCaseSensitive(int column) {
      return true;
   }

   @Override
   public boolean isSearchable(int column) {
      return true;
   }

   @Override
   public boolean isCurrency(int column) {
      return false;
   }

   @Override
   public int isNullable(int column) throws SQLException {
      return (this.getColumn(column).getFlags() & 1) == 0 ? 1 : 0;
   }

   @Override
   public boolean isSigned(int column) throws SQLException {
      return this.getColumn(column).isSigned();
   }

   @Override
   public int getColumnDisplaySize(int column) throws SQLException {
      return this.getColumn(column).getDisplaySize();
   }

   @Override
   public String getColumnLabel(int column) throws SQLException {
      return this.getColumn(column).getColumnAlias();
   }

   @Override
   public String getColumnName(int idx) throws SQLException {
      Column column = this.getColumn(idx);
      String columnName = column.getColumnName();
      return !"".equals(columnName) && !this.forceAlias ? columnName : column.getColumnAlias();
   }

   @Override
   public String getCatalogName(int column) throws SQLException {
      return this.conf.useCatalogTerm() == CatalogTerm.UseSchema ? this.getColumn(column).getCatalog() : this.getColumn(column).getSchema();
   }

   @Override
   public int getPrecision(int column) throws SQLException {
      return this.getColumn(column).getPrecision();
   }

   @Override
   public int getScale(int index) throws SQLException {
      return this.getColumn(index).getDecimals();
   }

   @Override
   public String getTableName(int column) throws SQLException {
      if (this.forceAlias) {
         return this.getColumn(column).getTableAlias();
      } else {
         return this.conf.blankTableNameMeta() ? "" : this.getColumn(column).getTable();
      }
   }

   @Override
   public String getSchemaName(int column) throws SQLException {
      return this.conf.useCatalogTerm() == CatalogTerm.UseSchema ? this.getColumn(column).getSchema() : "";
   }

   @Override
   public int getColumnType(int column) throws SQLException {
      return this.getColumn(column).getColumnType(this.conf);
   }

   @Override
   public String getColumnTypeName(int index) throws SQLException {
      return this.getColumn(index).getColumnTypeName(this.conf);
   }

   @Override
   public boolean isReadOnly(int column) throws SQLException {
      Column ci = this.getColumn(column);
      return ci.getColumnName().isEmpty();
   }

   @Override
   public boolean isWritable(int column) throws SQLException {
      return !this.isReadOnly(column);
   }

   @Override
   public boolean isDefinitelyWritable(int column) throws SQLException {
      return !this.isReadOnly(column);
   }

   @Override
   public String getColumnClassName(int column) throws SQLException {
      return this.getColumn(column).defaultClassname(this.conf);
   }

   private ColumnDecoder getColumn(int column) throws SQLException {
      if (column >= 1 && column <= this.fieldPackets.length) {
         return this.fieldPackets[column - 1];
      } else {
         throw this.exceptionFactory.create(String.format("wrong column index %s", column));
      }
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      if (this.isWrapperFor(iface)) {
         return iface.cast(this);
      } else {
         throw new SQLException("The receiver is not a wrapper for " + iface.getName());
      }
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) {
      return iface.isInstance(this);
   }
}
