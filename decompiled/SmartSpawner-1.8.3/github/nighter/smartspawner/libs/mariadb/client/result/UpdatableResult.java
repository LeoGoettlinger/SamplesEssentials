package github.nighter.smartspawner.libs.mariadb.client.result;

import github.nighter.smartspawner.libs.mariadb.BasePreparedStatement;
import github.nighter.smartspawner.libs.mariadb.Connection;
import github.nighter.smartspawner.libs.mariadb.Statement;
import github.nighter.smartspawner.libs.mariadb.client.Column;
import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.result.rowdecoder.BinaryRowDecoder;
import github.nighter.smartspawner.libs.mariadb.client.socket.Reader;
import github.nighter.smartspawner.libs.mariadb.codec.Parameter;
import github.nighter.smartspawner.libs.mariadb.plugin.Codec;
import github.nighter.smartspawner.libs.mariadb.plugin.array.FloatArray;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.BigDecimalCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.BlobCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.BooleanCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.ByteArrayCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.ByteCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.ClobCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.DateCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.DoubleCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.FloatArrayCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.FloatCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.IntCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.LongCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.ReaderCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.ShortCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.StreamCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.StringCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.TimeCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.TimestampCodec;
import github.nighter.smartspawner.libs.mariadb.util.ParameterList;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdatableResult extends CompleteResult {
   private static final int STATE_STANDARD = 0;
   private static final int STATE_UPDATE = 1;
   private static final int STATE_UPDATED = 2;
   private static final int STATE_INSERT = 3;
   private static final int STATE_INSERTED = 4;
   private String database;
   private String table;
   private boolean canInsert;
   private boolean canUpdate;
   private String sqlStateError = "HY000";
   private boolean isAutoincrementPk;
   private int savedRowPointer;
   private String changeError;
   private int state = 0;
   private ParameterList parameters;
   private String[] primaryCols;

   public UpdatableResult(
      Statement stmt,
      boolean binaryProtocol,
      long maxRows,
      ColumnDecoder[] metadataList,
      Reader reader,
      Context context,
      int resultSetType,
      boolean closeOnCompletion,
      boolean traceEnable
   ) throws IOException, SQLException {
      super(stmt, binaryProtocol, maxRows, metadataList, reader, context, resultSetType, closeOnCompletion, traceEnable, false);
      this.checkIfUpdatable();
      this.parameters = new ParameterList(metadataList.length);
   }

   private void checkIfUpdatable() throws SQLException {
      this.isAutoincrementPk = false;
      this.canInsert = true;
      this.canUpdate = true;
      this.database = null;
      this.table = null;

      for (Column columnDefinition : this.metadataList) {
         if (columnDefinition.getTable().isEmpty()) {
            this.cannotUpdateInsertRow("The result-set contains fields without without any database/table information");
            this.sqlStateError = "0A000";
            return;
         }

         if (this.database != null && !this.database.equals(columnDefinition.getSchema())) {
            this.cannotUpdateInsertRow("The result-set contains more than one database");
            this.sqlStateError = "0A000";
            return;
         }

         this.database = columnDefinition.getSchema();
         if (this.table != null && !this.table.equals(columnDefinition.getTable())) {
            this.cannotUpdateInsertRow("The result-set contains fields on different tables");
            this.sqlStateError = "0A000";
            return;
         }

         this.table = columnDefinition.getTable();
      }

      if (this.database != null && !this.database.isEmpty() && this.table != null) {
         for (Column col : this.metadataList) {
            if (col.isPrimaryKey()) {
               this.isAutoincrementPk = col.isAutoIncrement();
               if (this.isAutoincrementPk) {
                  this.primaryCols = new String[]{col.getColumnName()};
                  return;
               }
            }
         }

         ResultSet rs = this.statement
            .getConnection()
            .createStatement()
            .executeQuery("SHOW COLUMNS FROM " + quoteIdentifier(this.database) + "." + quoteIdentifier(this.table));
         List<String> primaryColumns = new ArrayList<>();

         while (rs.next()) {
            if ("PRI".equals(rs.getString("Key"))) {
               primaryColumns.add(rs.getString("Field"));
               boolean keyPresent = false;

               for (Column colx : this.metadataList) {
                  if (rs.getString("Field").equals(colx.getColumnName())) {
                     keyPresent = true;
                  }
               }

               boolean canBeNull = "YES".equals(rs.getString("Null"));
               boolean hasDefault = rs.getString("Default") != null;
               boolean generated = rs.getString("Extra") != null && !rs.getString("Extra").isEmpty();
               this.isAutoincrementPk = rs.getString("Extra") != null && rs.getString("Extra").contains("auto_increment");
               if (!keyPresent && !canBeNull && !hasDefault && !generated) {
                  this.canInsert = false;
                  this.changeError = String.format("primary field `%s` is not present in query", rs.getString("Field"));
               }

               if (!keyPresent) {
                  this.canUpdate = false;
                  this.changeError = String.format("Cannot update rows, since primary field %s is not present in query", rs.getString("Field"));
               }
            }
         }

         if (primaryColumns.isEmpty()) {
            this.canUpdate = false;
            this.changeError = "Cannot update rows, since no primary field is present in query";
         } else {
            this.primaryCols = primaryColumns.toArray(new String[0]);
         }
      } else {
         this.cannotUpdateInsertRow("The result-set contains fields without without any database/table information");
         this.sqlStateError = "0A000";
      }
   }

   private void cannotUpdateInsertRow(String reason) {
      this.changeError = reason;
      this.canUpdate = false;
      this.canInsert = false;
   }

   private void checkUpdatable(int position) throws SQLException {
      if (position > 0 && position <= this.metadataList.length) {
         if (this.state == 0 || this.state == 2) {
            this.state = 1;
         }

         if (this.state == 1) {
            if (this.rowPointer <= -1) {
               throw new SQLDataException("Current position is before the first row", "22023");
            }

            if (this.rowPointer >= this.dataSize) {
               throw new SQLDataException("Current position is after the last row", "22023");
            }

            if (!this.canUpdate) {
               throw this.exceptionFactory.create("ResultSet cannot be updated. " + this.changeError, this.sqlStateError);
            }
         }
      } else {
         throw this.exceptionFactory.create("No such column: " + position, "22023");
      }
   }

   @Override
   public boolean rowUpdated() {
      return this.state == 2;
   }

   @Override
   public boolean rowInserted() {
      return this.state == 4;
   }

   @Override
   public boolean rowDeleted() {
      return false;
   }

   @Override
   public void updateNull(int columnIndex) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, Parameter.NULL_PARAMETER);
   }

   @Override
   public void updateBoolean(int columnIndex, boolean x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(BooleanCodec.INSTANCE, x));
   }

   @Override
   public void updateByte(int columnIndex, byte x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(ByteCodec.INSTANCE, x));
   }

   @Override
   public void updateShort(int columnIndex, short x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(ShortCodec.INSTANCE, x));
   }

   @Override
   public void updateInt(int columnIndex, int x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(IntCodec.INSTANCE, x));
   }

   @Override
   public void updateLong(int columnIndex, long x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(LongCodec.INSTANCE, x));
   }

   @Override
   public void updateFloat(int columnIndex, float x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(FloatCodec.INSTANCE, x));
   }

   @Override
   public void updateDouble(int columnIndex, double x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(DoubleCodec.INSTANCE, x));
   }

   @Override
   public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(BigDecimalCodec.INSTANCE, x));
   }

   @Override
   public void updateString(int columnIndex, String x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(StringCodec.INSTANCE, x));
   }

   @Override
   public void updateBytes(int columnIndex, byte[] x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(ByteArrayCodec.INSTANCE, x));
   }

   @Override
   public void updateDate(int columnIndex, Date x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(DateCodec.INSTANCE, x));
   }

   @Override
   public void updateTime(int columnIndex, Time x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(TimeCodec.INSTANCE, x));
   }

   @Override
   public void updateArray(int columnIndex, Array x) throws SQLException {
      this.checkUpdatable(columnIndex);
      if (x instanceof FloatArray) {
         this.parameters.set(columnIndex - 1, new Parameter<>(FloatArrayCodec.INSTANCE, (float[])x.getArray()));
      } else {
         throw this.exceptionFactory.notSupported(String.format("this type of Array parameter %s is not supported", x.getClass()));
      }
   }

   @Override
   public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(TimestampCodec.INSTANCE, x));
   }

   @Override
   public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(StreamCodec.INSTANCE, x, (long)length));
   }

   @Override
   public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(StreamCodec.INSTANCE, x, (long)length));
   }

   @Override
   public void updateCharacterStream(int columnIndex, java.io.Reader x, int length) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(ReaderCodec.INSTANCE, x, (long)length));
   }

   @Override
   public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
      this.updateInternalObject(columnIndex, x, (long)scaleOrLength);
   }

   @Override
   public void updateObject(int columnIndex, Object x) throws SQLException {
      this.updateInternalObject(columnIndex, x, null);
   }

   @Override
   public void updateNull(String columnLabel) throws SQLException {
      this.updateNull(this.findColumn(columnLabel));
   }

   @Override
   public void updateBoolean(String columnLabel, boolean x) throws SQLException {
      this.updateBoolean(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateByte(String columnLabel, byte x) throws SQLException {
      this.updateByte(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateShort(String columnLabel, short x) throws SQLException {
      this.updateShort(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateInt(String columnLabel, int x) throws SQLException {
      this.updateInt(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateLong(String columnLabel, long x) throws SQLException {
      this.updateLong(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateFloat(String columnLabel, float x) throws SQLException {
      this.updateFloat(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateDouble(String columnLabel, double x) throws SQLException {
      this.updateDouble(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
      this.updateBigDecimal(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateString(String columnLabel, String x) throws SQLException {
      this.updateString(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateBytes(String columnLabel, byte[] x) throws SQLException {
      this.updateBytes(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateDate(String columnLabel, Date x) throws SQLException {
      this.updateDate(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateTime(String columnLabel, Time x) throws SQLException {
      this.updateTime(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
      this.updateTimestamp(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
      this.updateAsciiStream(this.findColumn(columnLabel), x, length);
   }

   @Override
   public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
      this.updateBinaryStream(this.findColumn(columnLabel), x, length);
   }

   @Override
   public void updateCharacterStream(String columnLabel, java.io.Reader reader, int length) throws SQLException {
      this.updateCharacterStream(this.findColumn(columnLabel), reader, length);
   }

   @Override
   public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
      this.updateObject(this.findColumn(columnLabel), x, scaleOrLength);
   }

   @Override
   public void updateObject(String columnLabel, Object x) throws SQLException {
      this.updateObject(this.findColumn(columnLabel), x);
   }

   @Override
   public void insertRow() throws SQLException {
      if (this.state == 3 || this.state == 4) {
         String insertSql = this.buildInsertQuery();
         PreparedStatement insertPreparedStatement = ((Connection)this.statement.getConnection())
            .prepareInternal(insertSql, 1, 1003, 1007, this.rowDecoder instanceof BinaryRowDecoder);

         try {
            int paramPos = 0;

            for (int pos = 0; pos < this.metadataList.length; pos++) {
               Column colInfo = this.metadataList[pos];
               github.nighter.smartspawner.libs.mariadb.client.util.Parameter param = this.parameters.size() > pos ? this.parameters.get(pos) : null;
               if (param != null) {
                  ((BasePreparedStatement)insertPreparedStatement).setParameter(paramPos++, param);
               } else if (!Arrays.asList(this.primaryCols).contains(colInfo.getColumnName()) && !colInfo.hasDefault()) {
                  ((BasePreparedStatement)insertPreparedStatement).setParameter(paramPos++, Parameter.NULL_PARAMETER);
               }
            }

            insertPreparedStatement.execute();
            if (this.context.getVersion().isMariaDBServer() && this.context.getVersion().versionGreaterOrEqual(10, 5, 1)) {
               ResultSet insertRs = insertPreparedStatement.getResultSet();
               if (insertRs.next()) {
                  byte[] rowByte = ((Result)insertRs).getCurrentRowData();
                  this.addRowData(rowByte);
               }
            } else if (this.isAutoincrementPk) {
               ResultSet rsKey = insertPreparedStatement.getGeneratedKeys();
               if (rsKey.next()) {
                  PreparedStatement refreshPreparedStatement = this.prepareRefreshStmt();

                  try {
                     refreshPreparedStatement.setObject(1, rsKey.getObject(1));
                     Result rs = (Result)refreshPreparedStatement.executeQuery();
                     if (rs.next()) {
                        this.addRowData(rs.getCurrentRowData());
                     }
                  } catch (Throwable var10) {
                     if (refreshPreparedStatement != null) {
                        try {
                           refreshPreparedStatement.close();
                        } catch (Throwable var9) {
                           var10.addSuppressed(var9);
                        }
                     }

                     throw var10;
                  }

                  if (refreshPreparedStatement != null) {
                     refreshPreparedStatement.close();
                  }
               }
            } else {
               this.addRowData(this.refreshRawData());
            }
         } catch (Throwable var11) {
            if (insertPreparedStatement != null) {
               try {
                  insertPreparedStatement.close();
               } catch (Throwable var8) {
                  var11.addSuppressed(var8);
               }
            }

            throw var11;
         }

         if (insertPreparedStatement != null) {
            insertPreparedStatement.close();
         }

         this.parameters = new ParameterList(this.parameters.size());
         this.state = 4;
      }
   }

   private static String quoteIdentifier(String identifier) {
      return "`" + identifier.replace("`", "``") + "`";
   }

   private String buildInsertQuery() throws SQLException {
      StringBuilder insertSql = new StringBuilder("INSERT " + quoteIdentifier(this.database) + "." + quoteIdentifier(this.table) + " ( ");
      StringBuilder valueClause = new StringBuilder();
      StringBuilder returningClause = new StringBuilder();
      boolean firstParam = true;

      for (int pos = 0; pos < this.metadataList.length; pos++) {
         Column colInfo = this.metadataList[pos];
         if (pos != 0) {
            returningClause.append(", ");
         }

         returningClause.append(quoteIdentifier(colInfo.getColumnName()));
         github.nighter.smartspawner.libs.mariadb.client.util.Parameter param = this.parameters.size() > pos ? this.parameters.get(pos) : null;
         if (param != null) {
            if (!firstParam) {
               insertSql.append(",");
               valueClause.append(", ");
            }

            insertSql.append(quoteIdentifier(colInfo.getColumnName()));
            valueClause.append("?");
            firstParam = false;
         } else if (Arrays.asList(this.primaryCols).contains(colInfo.getColumnName())) {
            boolean isAutoIncrement = colInfo.isAutoIncrement() || this.primaryCols.length == 1 && this.isAutoincrementPk;
            if (!isAutoIncrement && !colInfo.hasDefault()) {
               throw this.exceptionFactory.create(String.format("Cannot call insertRow() not setting value for primary key %s", colInfo.getColumnName()));
            }

            if (!isAutoIncrement && (!this.context.getVersion().isMariaDBServer() || !this.context.getVersion().versionGreaterOrEqual(10, 5, 1))) {
               throw this.exceptionFactory
                  .create(
                     String.format(
                        "Cannot call insertRow() not setting value for primary key %s with default value before server 10.5", colInfo.getColumnName()
                     )
                  );
            }
         } else if (!colInfo.hasDefault()) {
            if (!firstParam) {
               insertSql.append(",");
               valueClause.append(", ");
            }

            firstParam = false;
            insertSql.append(quoteIdentifier(colInfo.getColumnName()));
            valueClause.append("?");
         }
      }

      insertSql.append(") VALUES (").append((CharSequence)valueClause).append(")");
      if (this.context.getVersion().isMariaDBServer() && this.context.getVersion().versionGreaterOrEqual(10, 5, 1)) {
         insertSql.append(" RETURNING ").append((CharSequence)returningClause);
      }

      return insertSql.toString();
   }

   private String refreshStmt() {
      StringBuilder selectSql = new StringBuilder("SELECT ");
      StringBuilder whereClause = new StringBuilder(" WHERE ");
      boolean firstPrimary = true;

      for (int pos = 0; pos < this.metadataList.length; pos++) {
         Column colInfo = this.metadataList[pos];
         if (pos != 0) {
            selectSql.append(",");
         }

         selectSql.append(quoteIdentifier(colInfo.getColumnName()));
         if (Arrays.asList(this.primaryCols).contains(colInfo.getColumnName())) {
            if (!firstPrimary) {
               whereClause.append("AND ");
            }

            firstPrimary = false;
            whereClause.append(quoteIdentifier(colInfo.getColumnName())).append(" = ? ");
         }
      }

      selectSql.append(" FROM ").append(quoteIdentifier(this.database)).append(".").append(quoteIdentifier(this.table)).append((CharSequence)whereClause);
      return selectSql.toString();
   }

   private PreparedStatement prepareRefreshStmt() throws SQLException {
      return ((Connection)this.statement.getConnection()).prepareInternal(this.refreshStmt(), 1, 1003, 1007, this.rowDecoder instanceof BinaryRowDecoder);
   }

   private byte[] refreshRawData() throws SQLException {
      int fieldsPrimaryIndex = 0;
      PreparedStatement refreshPreparedStatement = this.prepareRefreshStmt();

      byte[] var9;
      try {
         for (int pos = 0; pos < this.metadataList.length; pos++) {
            Column colInfo = this.metadataList[pos];
            if (Arrays.asList(this.primaryCols).contains(colInfo.getColumnName())) {
               if (this.state != 0 && this.parameters.size() > pos && this.parameters.get(pos) != null) {
                  github.nighter.smartspawner.libs.mariadb.client.util.Parameter value = this.parameters.get(pos);
                  ((BasePreparedStatement)refreshPreparedStatement).setParameter(fieldsPrimaryIndex++, value);
               } else {
                  refreshPreparedStatement.setObject(++fieldsPrimaryIndex, this.getObject(pos + 1));
               }
            }
         }

         Result rs = (Result)refreshPreparedStatement.executeQuery();
         rs.next();
         var9 = rs.getCurrentRowData();
      } catch (Throwable var7) {
         if (refreshPreparedStatement != null) {
            try {
               refreshPreparedStatement.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (refreshPreparedStatement != null) {
         refreshPreparedStatement.close();
      }

      return var9;
   }

   private String updateQuery() {
      StringBuilder updateSql = new StringBuilder("UPDATE " + quoteIdentifier(this.database) + "." + quoteIdentifier(this.table) + " SET ");
      StringBuilder whereClause = new StringBuilder(" WHERE ");
      boolean firstUpdate = true;

      for (int pos = 0; pos < this.primaryCols.length; pos++) {
         String key = this.primaryCols[pos];
         if (pos != 0) {
            whereClause.append("AND ");
         }

         whereClause.append(quoteIdentifier(key)).append(" = ? ");
      }

      for (int pos = 0; pos < this.metadataList.length; pos++) {
         Column colInfo = this.metadataList[pos];
         if (this.parameters.size() > pos && this.parameters.get(pos) != null) {
            if (!firstUpdate) {
               updateSql.append(",");
            }

            firstUpdate = false;
            updateSql.append(quoteIdentifier(colInfo.getColumnName())).append(" = ? ");
         }
      }

      return firstUpdate ? null : updateSql.append((CharSequence)whereClause).toString();
   }

   @Override
   public void updateRow() throws SQLException {
      if (this.state == 3) {
         throw this.exceptionFactory.create("Cannot call updateRow() when inserting a new row");
      } else if (this.rowPointer < 0) {
         throw this.exceptionFactory.create("Current position is before the first row", "22023");
      } else if (this.rowPointer >= this.dataSize) {
         throw this.exceptionFactory.create("Current position is after the last row", "22023");
      } else {
         if (this.state == 1 || this.state == 2) {
            String updateQuery = this.updateQuery();
            if (updateQuery != null) {
               PreparedStatement preparedStatement = ((Connection)this.statement.getConnection())
                  .prepareInternal(updateQuery, 1, 1003, 1007, this.rowDecoder instanceof BinaryRowDecoder);

               try {
                  int fieldsIndex = 0;

                  for (int pos = 0; pos < this.metadataList.length; pos++) {
                     if (this.parameters.size() > pos) {
                        github.nighter.smartspawner.libs.mariadb.client.util.Parameter param = this.parameters.get(pos);
                        if (param != null) {
                           ((BasePreparedStatement)preparedStatement).setParameter(fieldsIndex++, param);
                        }
                     }
                  }

                  for (int posx = 0; posx < this.metadataList.length; posx++) {
                     Column colInfo = this.metadataList[posx];
                     if (Arrays.asList(this.primaryCols).contains(colInfo.getColumnName())) {
                        preparedStatement.setObject(++fieldsIndex, this.getObject(posx + 1));
                     }
                  }

                  preparedStatement.execute();
               } catch (Throwable var7) {
                  if (preparedStatement != null) {
                     try {
                        preparedStatement.close();
                     } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                     }
                  }

                  throw var7;
               }

               if (preparedStatement != null) {
                  preparedStatement.close();
               }

               this.refreshRow();
            }

            this.parameters = new ParameterList(this.parameters.size());
            this.state = 2;
         }
      }
   }

   @Override
   public void deleteRow() throws SQLException {
      if (this.state == 3) {
         throw this.exceptionFactory.create("Cannot call deleteRow() when inserting a new row");
      } else if (!this.canUpdate) {
         throw this.exceptionFactory.create("ResultSet cannot be updated. " + this.changeError, this.sqlStateError);
      } else if (this.rowPointer < 0) {
         throw new SQLDataException("Current position is before the first row", "22023");
      } else if (this.rowPointer >= this.dataSize) {
         throw new SQLDataException("Current position is after the last row", "22023");
      } else {
         StringBuilder deleteSql = new StringBuilder("DELETE FROM " + quoteIdentifier(this.database) + "." + quoteIdentifier(this.table) + " WHERE ");
         boolean firstPrimary = true;

         for (Column colInfo : this.metadataList) {
            if (Arrays.asList(this.primaryCols).contains(colInfo.getColumnName())) {
               if (!firstPrimary) {
                  deleteSql.append("AND ");
               }

               firstPrimary = false;
               deleteSql.append(quoteIdentifier(colInfo.getColumnName())).append(" = ? ");
            }
         }

         PreparedStatement deletePreparedStatement = ((Connection)this.statement.getConnection()).prepareInternal(deleteSql.toString(), 1, 1003, 1007, false);

         try {
            int fieldsPrimaryIndex = 1;

            for (int pos = 0; pos < this.metadataList.length; pos++) {
               Column colInfox = this.metadataList[pos];
               if (Arrays.asList(this.primaryCols).contains(colInfox.getColumnName())) {
                  deletePreparedStatement.setObject(fieldsPrimaryIndex++, this.getObject(pos + 1));
               }
            }

            deletePreparedStatement.executeUpdate();
            System.arraycopy(this.data, this.rowPointer + 1, this.data, this.rowPointer, this.dataSize - 1 - this.rowPointer);
            this.data[this.dataSize - 1] = null;
            this.dataSize--;
            this.previous();
         } catch (Throwable var8) {
            if (deletePreparedStatement != null) {
               try {
                  deletePreparedStatement.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (deletePreparedStatement != null) {
            deletePreparedStatement.close();
         }
      }
   }

   @Override
   public void refreshRow() throws SQLException {
      if (this.state == 3) {
         throw this.exceptionFactory.create("Cannot call refreshRow() when inserting a new row");
      } else if (this.rowPointer < 0) {
         throw this.exceptionFactory.create("Current position is before the first row", "22023");
      } else if (this.rowPointer >= this.data.length) {
         throw this.exceptionFactory.create("Current position is after the last row", "22023");
      } else {
         if (this.canUpdate) {
            this.updateRowData(this.refreshRawData());
         }
      }
   }

   @Override
   public void cancelRowUpdates() {
      this.parameters = new ParameterList(this.parameters.size());
      this.state = 0;
   }

   @Override
   public void moveToInsertRow() throws SQLException {
      if (!this.canInsert) {
         throw this.exceptionFactory.create("No row can be inserted. " + this.changeError, this.sqlStateError);
      } else {
         this.parameters = new ParameterList(this.parameters.size());
         this.state = 3;
         this.savedRowPointer = this.rowPointer;
      }
   }

   @Override
   public void moveToCurrentRow() {
      this.state = 0;
      this.resetToRowPointer();
   }

   @Override
   public void updateBlob(int columnIndex, Blob x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(BlobCodec.INSTANCE, x));
   }

   @Override
   public void updateBlob(String columnLabel, Blob x) throws SQLException {
      this.updateBlob(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateClob(int columnIndex, Clob x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(ClobCodec.INSTANCE, x));
   }

   @Override
   public void updateClob(String columnLabel, Clob x) throws SQLException {
      this.updateClob(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateNString(int columnIndex, String nString) throws SQLException {
      this.updateString(columnIndex, nString);
   }

   @Override
   public void updateNString(String columnLabel, String nString) throws SQLException {
      this.updateString(columnLabel, nString);
   }

   @Override
   public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
      this.updateClob(columnIndex, nClob);
   }

   @Override
   public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
      this.updateClob(columnLabel, nClob);
   }

   @Override
   public void updateNCharacterStream(int columnIndex, java.io.Reader x, long length) throws SQLException {
      this.updateCharacterStream(columnIndex, x, length);
   }

   @Override
   public void updateNCharacterStream(String columnLabel, java.io.Reader reader, long length) throws SQLException {
      this.updateCharacterStream(columnLabel, reader, length);
   }

   @Override
   public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(StreamCodec.INSTANCE, x, length));
   }

   @Override
   public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(StreamCodec.INSTANCE, x, length));
   }

   @Override
   public void updateCharacterStream(int columnIndex, java.io.Reader x, long length) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(ReaderCodec.INSTANCE, x, length));
   }

   @Override
   public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
      this.updateAsciiStream(this.findColumn(columnLabel), x, length);
   }

   @Override
   public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
      this.updateBinaryStream(this.findColumn(columnLabel), x, length);
   }

   @Override
   public void updateCharacterStream(String columnLabel, java.io.Reader reader, long length) throws SQLException {
      this.updateCharacterStream(this.findColumn(columnLabel), reader, length);
   }

   @Override
   public void updateBlob(int columnIndex, InputStream x, long length) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(StreamCodec.INSTANCE, x, length));
   }

   @Override
   public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
      this.updateBlob(this.findColumn(columnLabel), inputStream, length);
   }

   @Override
   public void updateClob(int columnIndex, java.io.Reader x, long length) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(ReaderCodec.INSTANCE, x, length));
   }

   @Override
   public void updateClob(String columnLabel, java.io.Reader reader, long length) throws SQLException {
      this.updateClob(this.findColumn(columnLabel), reader, length);
   }

   @Override
   public void updateNClob(int columnIndex, java.io.Reader reader, long length) throws SQLException {
      this.updateClob(columnIndex, reader, length);
   }

   @Override
   public void updateNClob(String columnLabel, java.io.Reader reader, long length) throws SQLException {
      this.updateClob(columnLabel, reader, length);
   }

   @Override
   public void updateNCharacterStream(int columnIndex, java.io.Reader x) throws SQLException {
      this.updateCharacterStream(columnIndex, x);
   }

   @Override
   public void updateNCharacterStream(String columnLabel, java.io.Reader reader) throws SQLException {
      this.updateCharacterStream(columnLabel, reader);
   }

   @Override
   public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(StreamCodec.INSTANCE, x));
   }

   @Override
   public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(StreamCodec.INSTANCE, x));
   }

   @Override
   public void updateCharacterStream(int columnIndex, java.io.Reader x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(ReaderCodec.INSTANCE, x));
   }

   @Override
   public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
      this.updateAsciiStream(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
      this.updateBinaryStream(this.findColumn(columnLabel), x);
   }

   @Override
   public void updateCharacterStream(String columnLabel, java.io.Reader reader) throws SQLException {
      this.updateCharacterStream(this.findColumn(columnLabel), reader);
   }

   @Override
   public void updateBlob(int columnIndex, InputStream x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(StreamCodec.INSTANCE, x));
   }

   @Override
   public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
      this.updateBlob(this.findColumn(columnLabel), inputStream);
   }

   @Override
   public void updateClob(int columnIndex, java.io.Reader x) throws SQLException {
      this.checkUpdatable(columnIndex);
      this.parameters.set(columnIndex - 1, new Parameter<>(ReaderCodec.INSTANCE, x));
   }

   @Override
   public void updateClob(String columnLabel, java.io.Reader reader) throws SQLException {
      this.updateClob(this.findColumn(columnLabel), reader);
   }

   @Override
   public void updateNClob(int columnIndex, java.io.Reader reader) throws SQLException {
      this.updateClob(columnIndex, reader);
   }

   @Override
   public void updateNClob(String columnLabel, java.io.Reader reader) throws SQLException {
      this.updateClob(columnLabel, reader);
   }

   @Override
   public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
      this.updateInternalObject(columnIndex, x, (long)scaleOrLength);
   }

   private void updateInternalObject(int columnIndex, Object x, Long scaleOrLength) throws SQLException {
      this.checkUpdatable(columnIndex);
      if (x == null) {
         this.parameters.set(columnIndex - 1, Parameter.NULL_PARAMETER);
      } else {
         for (Codec<?> codec : this.context.getConf().codecs()) {
            if (codec.canEncode(x)) {
               Parameter p = new Parameter<>(codec, x, scaleOrLength);
               this.parameters.set(columnIndex - 1, p);
               return;
            }
         }

         throw new SQLException(String.format("Type %s not supported type", x.getClass().getName()));
      }
   }

   @Override
   public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
      this.updateObject(this.findColumn(columnLabel), x, targetSqlType, scaleOrLength);
   }

   @Override
   public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
      this.updateInternalObject(columnIndex, x, null);
   }

   @Override
   public void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SQLException {
      this.updateObject(this.findColumn(columnLabel), x, targetSqlType);
   }

   @Override
   public int getConcurrency() {
      return 1008;
   }

   private void resetToRowPointer() {
      this.rowPointer = this.savedRowPointer;
      if (this.rowPointer != -1 && this.rowPointer < this.dataSize - 1) {
         this.setRow(this.data[this.rowPointer]);
      } else {
         this.setNullRowBuf();
      }

      this.savedRowPointer = -1;
   }

   @Override
   public void beforeFirst() throws SQLException {
      if (this.state == 3) {
         this.resetToRowPointer();
      }

      this.state = 0;
      super.beforeFirst();
   }

   @Override
   public boolean first() throws SQLException {
      if (this.state == 3) {
         this.resetToRowPointer();
      }

      this.state = 0;
      return super.first();
   }

   @Override
   public boolean last() throws SQLException {
      if (this.state == 3) {
         this.resetToRowPointer();
      }

      this.state = 0;
      return super.last();
   }

   @Override
   public void afterLast() throws SQLException {
      if (this.state == 3) {
         this.resetToRowPointer();
      }

      this.state = 0;
      super.afterLast();
   }

   @Override
   public boolean absolute(int row) throws SQLException {
      if (this.state == 3) {
         this.resetToRowPointer();
      }

      this.state = 0;
      return super.absolute(row);
   }

   @Override
   public boolean relative(int rows) throws SQLException {
      if (this.state == 3) {
         this.resetToRowPointer();
      }

      this.state = 0;
      return super.relative(rows);
   }

   @Override
   public boolean next() throws SQLException {
      if (this.state == 3) {
         this.resetToRowPointer();
      }

      this.state = 0;
      return super.next();
   }

   @Override
   public boolean previous() throws SQLException {
      if (this.state == 3) {
         this.resetToRowPointer();
      }

      this.state = 0;
      return super.previous();
   }
}
