package github.nighter.smartspawner.libs.mariadb;

import github.nighter.smartspawner.libs.mariadb.client.result.Result;
import github.nighter.smartspawner.libs.mariadb.client.util.ClosableLock;
import github.nighter.smartspawner.libs.mariadb.codec.Parameter;
import github.nighter.smartspawner.libs.mariadb.util.ParameterList;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class BaseCallableStatement extends ServerPreparedStatement implements CallableStatement {
   protected final String databaseName;
   protected final String procedureName;
   protected final Set<Integer> outputParameters = new HashSet<>();
   protected CallableParameterMetaData parameterMetaData = null;
   private Result outputResult = null;

   protected BaseCallableStatement(
      String sql,
      Connection con,
      ClosableLock lock,
      String databaseName,
      String procedureName,
      int resultSetType,
      int resultSetConcurrency,
      int defaultFetchSize
   ) throws SQLException {
      super(sql, con, lock, 1, resultSetType, resultSetConcurrency, defaultFetchSize);
      this.databaseName = databaseName;
      this.procedureName = procedureName;
   }

   public abstract boolean isFunction();

   protected void outputResultFromRes(int i) throws SQLException {
      this.outputResult = (Result)this.results.remove(this.results.size() - i);
      this.outputResult.next();
   }

   @Override
   public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
      this.checkIndex(parameterIndex);
      this.outputParameters.add(parameterIndex);
      if (!this.parameters.containsKey(parameterIndex - 1)) {
         this.parameters.set(parameterIndex - 1, Parameter.NULL_PARAMETER);
      }
   }

   private void checkIndex(int index) throws SQLException {
      if (index <= 0
         || this.prepareResult != null && index > this.prepareResult.getParameters().length + (this.isFunction() ? 1 : 0)
         || this.prepareResult == null && this.parameterMetaData != null && index > this.parameterMetaData.getParameterCount()) {
         throw this.exceptionFactory().create(String.format("wrong parameter index %s", index));
      }
   }

   @Override
   public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
      this.registerOutParameter(parameterIndex, sqlType);
   }

   @Override
   public void clearParameters() throws SQLException {
      this.checkNotClosed();
      this.parameters = new ParameterList();
      this.outputParameters.stream().forEach(index -> this.parameters.set(index - 1, Parameter.NULL_PARAMETER));
   }

   @Override
   public boolean wasNull() throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.wasNull();
   }

   private int idxToOutIdx(int idx) throws SQLException {
      int outputIndex = 1;
      if (idx < 1) {
         throw this.exceptionFactory().create(String.format("wrong index %s", idx));
      } else if (!this.outputParameters.contains(idx)) {
         throw this.exceptionFactory().create(String.format("index %s not declared as output", idx));
      } else {
         for (int i = 1; i < idx; i++) {
            if (this.outputParameters.contains(i)) {
               outputIndex++;
            }
         }

         return outputIndex;
      }
   }

   protected void checkOutputResult() throws SQLException {
      if (this.outputResult == null) {
         throw this.exceptionFactory().create("No output result");
      }
   }

   @Override
   public String getString(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getString(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public boolean getBoolean(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getBoolean(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public byte getByte(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getByte(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public short getShort(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getShort(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public int getInt(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getInt(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public long getLong(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getLong(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public float getFloat(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getFloat(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public double getDouble(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getDouble(this.idxToOutIdx(parameterIndex));
   }

   @Deprecated
   @Override
   public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getBigDecimal(this.idxToOutIdx(parameterIndex), scale);
   }

   @Override
   public byte[] getBytes(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getBytes(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public Date getDate(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getDate(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public Time getTime(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getTime(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public Timestamp getTimestamp(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getTimestamp(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public Object getObject(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getObject(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getBigDecimal(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getObject(this.idxToOutIdx(parameterIndex), map);
   }

   @Override
   public Ref getRef(int parameterIndex) throws SQLException {
      throw this.exceptionFactory().notSupported("Method ResultSet.getRef not supported");
   }

   @Override
   public Blob getBlob(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getBlob(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public Clob getClob(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getClob(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public Array getArray(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      throw this.exceptionFactory().notSupported("Method ResultSet.getArray not supported");
   }

   @Override
   public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getDate(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getTime(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getTimestamp(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
      this.registerOutParameter(parameterIndex, sqlType);
   }

   @Override
   public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
      this.checkNotClosed();
      this.registerOutParameter(this.nameToIndex(parameterName), sqlType);
   }

   private int nameToIndex(String parameterName) throws SQLException {
      if (parameterName == null) {
         throw this.exceptionFactory().create("parameter name cannot be null");
      } else {
         if (this.parameterMetaData == null) {
            this.getParameterMetaData();
         }

         int count = this.parameterMetaData.getParameterCount();

         for (int i = 1; i <= count; i++) {
            String name = this.parameterMetaData.getParameterName(i);
            if (name != null && name.equalsIgnoreCase(parameterName)) {
               return i;
            }
         }

         throw this.exceptionFactory().create(String.format("parameter name %s not found", parameterName));
      }
   }

   @Override
   public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
      this.registerOutParameter(parameterName, sqlType);
   }

   @Override
   public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
      this.registerOutParameter(parameterName, sqlType);
   }

   @Override
   public URL getURL(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getURL(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public void setURL(String parameterName, URL val) throws SQLException {
      this.setURL(this.nameToIndex(parameterName), val);
   }

   @Override
   public void setNull(String parameterName, int sqlType) throws SQLException {
      this.setNull(this.nameToIndex(parameterName), sqlType);
   }

   @Override
   public void setBoolean(String parameterName, boolean x) throws SQLException {
      this.setBoolean(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setByte(String parameterName, byte x) throws SQLException {
      this.setByte(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setShort(String parameterName, short x) throws SQLException {
      this.setShort(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setInt(String parameterName, int x) throws SQLException {
      this.setInt(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setLong(String parameterName, long x) throws SQLException {
      this.setLong(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setFloat(String parameterName, float x) throws SQLException {
      this.setFloat(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setDouble(String parameterName, double x) throws SQLException {
      this.setDouble(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
      this.setBigDecimal(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setString(String parameterName, String x) throws SQLException {
      this.setString(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setBytes(String parameterName, byte[] x) throws SQLException {
      this.setBytes(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setDate(String parameterName, Date x) throws SQLException {
      this.setDate(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setTime(String parameterName, Time x) throws SQLException {
      this.setTime(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
      this.setTimestamp(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
      this.setAsciiStream(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
      this.setBinaryStream(this.nameToIndex(parameterName), x, length);
   }

   @Override
   public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
      this.setObject(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
      this.setObject(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setObject(String parameterName, Object x) throws SQLException {
      this.setObject(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
      this.setCharacterStream(this.nameToIndex(parameterName), reader, length);
   }

   @Override
   public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
      this.setDate(this.nameToIndex(parameterName), x, cal);
   }

   @Override
   public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
      this.setTime(this.nameToIndex(parameterName), x, cal);
   }

   @Override
   public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
      this.setTimestamp(this.nameToIndex(parameterName), x, cal);
   }

   @Override
   public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
      this.setNull(this.nameToIndex(parameterName), sqlType, typeName);
   }

   @Override
   public String getString(String parameterName) throws SQLException {
      return this.outputResult.getString(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public boolean getBoolean(String parameterName) throws SQLException {
      return this.outputResult.getBoolean(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public byte getByte(String parameterName) throws SQLException {
      return this.outputResult.getByte(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public short getShort(String parameterName) throws SQLException {
      return this.outputResult.getShort(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public int getInt(String parameterName) throws SQLException {
      return this.outputResult.getInt(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public long getLong(String parameterName) throws SQLException {
      return this.outputResult.getLong(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public float getFloat(String parameterName) throws SQLException {
      return this.outputResult.getFloat(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public double getDouble(String parameterName) throws SQLException {
      return this.outputResult.getDouble(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public byte[] getBytes(String parameterName) throws SQLException {
      return this.outputResult.getBytes(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public Date getDate(String parameterName) throws SQLException {
      return this.outputResult.getDate(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public Time getTime(String parameterName) throws SQLException {
      return this.outputResult.getTime(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public Timestamp getTimestamp(String parameterName) throws SQLException {
      return this.outputResult.getTimestamp(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public Object getObject(String parameterName) throws SQLException {
      return this.outputResult.getObject(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public BigDecimal getBigDecimal(String parameterName) throws SQLException {
      return this.outputResult.getBigDecimal(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
      return this.outputResult.getObject(this.idxToOutIdx(this.nameToIndex(parameterName)), map);
   }

   @Override
   public Ref getRef(String parameterName) throws SQLException {
      throw this.exceptionFactory().notSupported("Method ResultSet.getRef not supported");
   }

   @Override
   public Blob getBlob(String parameterName) throws SQLException {
      return this.outputResult.getBlob(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public Clob getClob(String parameterName) throws SQLException {
      return this.outputResult.getClob(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public Array getArray(String parameterName) throws SQLException {
      throw this.exceptionFactory().notSupported("Method ResultSet.getArray not supported");
   }

   @Override
   public Date getDate(String parameterName, Calendar cal) throws SQLException {
      return this.outputResult.getDate(this.idxToOutIdx(this.nameToIndex(parameterName)), cal);
   }

   @Override
   public Time getTime(String parameterName, Calendar cal) throws SQLException {
      return this.outputResult.getTime(this.idxToOutIdx(this.nameToIndex(parameterName)), cal);
   }

   @Override
   public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
      return this.outputResult.getTimestamp(this.idxToOutIdx(this.nameToIndex(parameterName)), cal);
   }

   @Override
   public URL getURL(String parameterName) throws SQLException {
      return this.outputResult.getURL(this.idxToOutIdx(this.nameToIndex(parameterName)));
   }

   @Override
   public RowId getRowId(int parameterIndex) throws SQLException {
      throw this.exceptionFactory().notSupported("RowId are not supported");
   }

   @Override
   public RowId getRowId(String parameterName) throws SQLException {
      throw this.exceptionFactory().notSupported("RowId are not supported");
   }

   @Override
   public void setRowId(String parameterName, RowId x) throws SQLException {
      throw this.exceptionFactory().notSupported("RowId parameter are not supported");
   }

   @Override
   public void setNString(String parameterName, String value) throws SQLException {
      this.setNString(this.nameToIndex(parameterName), value);
   }

   @Override
   public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
      this.setNCharacterStream(this.nameToIndex(parameterName), value, length);
   }

   @Override
   public void setNClob(String parameterName, NClob value) throws SQLException {
      this.setNClob(this.nameToIndex(parameterName), value);
   }

   @Override
   public void setClob(String parameterName, Reader reader, long length) throws SQLException {
      this.setClob(this.nameToIndex(parameterName), reader, length);
   }

   @Override
   public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
      this.setBlob(this.nameToIndex(parameterName), inputStream, length);
   }

   @Override
   public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
      this.setNClob(this.nameToIndex(parameterName), reader, length);
   }

   @Override
   public NClob getNClob(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getNClob(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public NClob getNClob(String parameterName) throws SQLException {
      return this.getNClob(this.nameToIndex(parameterName));
   }

   @Override
   public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
      throw this.exceptionFactory().notSupported("SQLXML parameter are not supported");
   }

   @Override
   public SQLXML getSQLXML(int parameterIndex) throws SQLException {
      throw this.exceptionFactory().notSupported("SQLXML are not supported");
   }

   @Override
   public SQLXML getSQLXML(String parameterName) throws SQLException {
      throw this.exceptionFactory().notSupported("SQLXML are not supported");
   }

   @Override
   public String getNString(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getNString(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public String getNString(String parameterName) throws SQLException {
      return this.getNString(this.nameToIndex(parameterName));
   }

   @Override
   public Reader getNCharacterStream(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getNCharacterStream(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public Reader getNCharacterStream(String parameterName) throws SQLException {
      return this.getNCharacterStream(this.nameToIndex(parameterName));
   }

   @Override
   public Reader getCharacterStream(int parameterIndex) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getCharacterStream(this.idxToOutIdx(parameterIndex));
   }

   @Override
   public Reader getCharacterStream(String parameterName) throws SQLException {
      return this.getNCharacterStream(this.nameToIndex(parameterName));
   }

   @Override
   public void setBlob(String parameterName, Blob x) throws SQLException {
      this.setBlob(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setClob(String parameterName, Clob x) throws SQLException {
      this.setClob(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
      this.setAsciiStream(this.nameToIndex(parameterName), x, length);
   }

   @Override
   public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
      this.setBinaryStream(this.nameToIndex(parameterName), x, length);
   }

   @Override
   public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
      this.setCharacterStream(this.nameToIndex(parameterName), reader, length);
   }

   @Override
   public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
      this.setAsciiStream(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
      this.setBinaryStream(this.nameToIndex(parameterName), x);
   }

   @Override
   public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
      this.setCharacterStream(this.nameToIndex(parameterName), reader);
   }

   @Override
   public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
      this.setNCharacterStream(this.nameToIndex(parameterName), value);
   }

   @Override
   public void setClob(String parameterName, Reader reader) throws SQLException {
      this.setClob(this.nameToIndex(parameterName), reader);
   }

   @Override
   public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
      this.setBlob(this.nameToIndex(parameterName), inputStream);
   }

   @Override
   public void setNClob(String parameterName, Reader reader) throws SQLException {
      this.setNClob(this.nameToIndex(parameterName), reader);
   }

   @Override
   public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
      this.checkNotClosed();
      this.checkOutputResult();
      return this.outputResult.getObject(this.idxToOutIdx(parameterIndex), type);
   }

   @Override
   public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
      return this.getObject(this.nameToIndex(parameterName), type);
   }

   @Override
   public void setObject(String parameterName, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
      this.setObject(this.nameToIndex(parameterName), x, targetSqlType, scaleOrLength);
   }

   @Override
   public void setObject(String parameterName, Object x, SQLType targetSqlType) throws SQLException {
      this.setObject(this.nameToIndex(parameterName), x, targetSqlType);
   }

   @Override
   public void registerOutParameter(int parameterIndex, SQLType sqlType) throws SQLException {
      this.registerOutParameter(parameterIndex, 0);
   }

   @Override
   public void registerOutParameter(int parameterIndex, SQLType sqlType, int scale) throws SQLException {
      this.registerOutParameter(parameterIndex, sqlType);
   }

   @Override
   public void registerOutParameter(int parameterIndex, SQLType sqlType, String typeName) throws SQLException {
      this.registerOutParameter(parameterIndex, sqlType);
   }

   @Override
   public void registerOutParameter(String parameterName, SQLType sqlType) throws SQLException {
      this.registerOutParameter(this.nameToIndex(parameterName), sqlType);
   }

   @Override
   public void registerOutParameter(String parameterName, SQLType sqlType, int scale) throws SQLException {
      this.registerOutParameter(this.nameToIndex(parameterName), sqlType);
   }

   @Override
   public void registerOutParameter(String parameterName, SQLType sqlType, String typeName) throws SQLException {
      this.registerOutParameter(this.nameToIndex(parameterName), sqlType);
   }

   public CallableParameterMetaData getParameterMetaData() throws SQLException {
      if (this.parameterMetaData == null) {
         HostAddress host = this.con.getClient().getHostAddress();
         HostAddress.MysqlProcStatus status = host.getMysqlProcStatus();
         if (status != HostAddress.MysqlProcStatus.UNUSABLE) {
            try {
               this.parameterMetaData = this.fetchViaMysqlProc();
               if (status == HostAddress.MysqlProcStatus.UNKNOWN) {
                  host.setMysqlProcStatus(HostAddress.MysqlProcStatus.USABLE);
               }

               return this.parameterMetaData;
            } catch (SQLException var5) {
               int code = var5.getErrorCode();
               if (code != 1142 && code != 1146 && code != 1044) {
                  throw var5;
               }
            }

            host.setMysqlProcStatus(HostAddress.MysqlProcStatus.UNUSABLE);
         }

         this.parameterMetaData = this.fetchViaInformationSchema();
      }

      return this.parameterMetaData;
   }

   private CallableParameterMetaData fetchViaMysqlProc() throws SQLException {
      PreparedStatement prep = new ClientPreparedStatement(
         "SELECT param_list, `returns` FROM mysql.proc WHERE name = ? AND db = ?", this.con, this.lock, 2, 1003, 1007, 0
      );

      CallableParameterMetaData var11;
      try {
         prep.setString(1, this.procedureName);
         prep.setString(2, this.databaseName);
         ResultSet rs = prep.executeQuery();

         try {
            List<CallableParameterMetaData.ParamInfo> list = new ArrayList<>();
            if (rs.next()) {
               String paramList = rs.getString(1);
               if (this.isFunction()) {
                  String returns = rs.getString(2);
                  CallableParameterMetaData.ParamInfo ret = new CallableParameterMetaData.ParamInfo();
                  fillTypeFields(ret, returns);
                  list.add(ret);
               }

               if (paramList != null && !paramList.trim().isEmpty()) {
                  parseMysqlProcParamList(paramList, list);
               }
            }

            var11 = new CallableParameterMetaData(list, this.isFunction());
         } catch (Throwable var9) {
            if (rs != null) {
               try {
                  rs.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (rs != null) {
            rs.close();
         }
      } catch (Throwable var10) {
         try {
            prep.close();
         } catch (Throwable var7) {
            var10.addSuppressed(var7);
         }

         throw var10;
      }

      prep.close();
      return var11;
   }

   private CallableParameterMetaData fetchViaInformationSchema() throws SQLException {
      String sql = "SELECT PARAMETER_NAME, PARAMETER_MODE, DATA_TYPE, DTD_IDENTIFIER, NUMERIC_PRECISION, NUMERIC_SCALE, CHARACTER_MAXIMUM_LENGTH FROM information_schema.PARAMETERS WHERE SPECIFIC_NAME = ? AND SPECIFIC_SCHEMA = ? ORDER BY ORDINAL_POSITION";
      boolean serverPrep = this.con.getContext().getConf().cachePrepStmts() && this.con.getContext().getConf().useServerPrepStmts();
      PreparedStatement prep = (PreparedStatement)(serverPrep
         ? new ServerPreparedStatement(sql, this.con, this.lock, 2, 1003, 1007, 0)
         : new ClientPreparedStatement(sql, this.con, this.lock, 2, 1003, 1007, 0));

      CallableParameterMetaData var5;
      try {
         prep.setString(1, this.procedureName);
         prep.setString(2, this.databaseName);
         ResultSet rs = prep.executeQuery();

         try {
            var5 = new CallableParameterMetaData(rs, this.isFunction());
         } catch (Throwable var9) {
            if (rs != null) {
               try {
                  rs.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (rs != null) {
            rs.close();
         }
      } catch (Throwable var10) {
         if (prep != null) {
            try {
               prep.close();
            } catch (Throwable var7) {
               var10.addSuppressed(var7);
            }
         }

         throw var10;
      }

      if (prep != null) {
         prep.close();
      }

      return var5;
   }

   private static void parseMysqlProcParamList(String list, List<CallableParameterMetaData.ParamInfo> out) {
      int depth = 0;
      char strQuote = 0;
      boolean back = false;
      int start = 0;

      for (int i = 0; i < list.length(); i++) {
         char c = list.charAt(i);
         if (strQuote != 0) {
            if (c == strQuote && list.charAt(i - 1) != '\\') {
               strQuote = 0;
            }
         } else if (back) {
            if (c == '`') {
               back = false;
            }
         } else if (c == '\'' || c == '"') {
            strQuote = c;
         } else if (c == '`') {
            back = true;
         } else if (c == '(') {
            depth++;
         } else if (c == ')') {
            depth--;
         } else if (c == ',' && depth == 0) {
            addParsedParam(list.substring(start, i), out);
            start = i + 1;
         }
      }

      addParsedParam(list.substring(start), out);
   }

   private static void addParsedParam(String segment, List<CallableParameterMetaData.ParamInfo> out) {
      String s = segment.trim();
      if (!s.isEmpty()) {
         CallableParameterMetaData.ParamInfo p = new CallableParameterMetaData.ParamInfo();
         int pos = 0;
         String upper = s.toUpperCase(Locale.ROOT);
         if (upper.startsWith("INOUT") && s.length() > 5 && Character.isWhitespace(s.charAt(5))) {
            p.parameterMode = "INOUT";
            pos = 5;
         } else if (upper.startsWith("OUT") && s.length() > 3 && Character.isWhitespace(s.charAt(3))) {
            p.parameterMode = "OUT";
            pos = 3;
         } else if (upper.startsWith("IN") && s.length() > 2 && Character.isWhitespace(s.charAt(2))) {
            p.parameterMode = "IN";
            pos = 2;
         } else {
            p.parameterMode = "IN";
         }

         while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
            pos++;
         }

         if (pos < s.length() && s.charAt(pos) == '`') {
            int end = pos + 1;
            StringBuilder name = new StringBuilder();

            while (end < s.length()) {
               char c = s.charAt(end);
               if (c == '`') {
                  if (end + 1 >= s.length() || s.charAt(end + 1) != '`') {
                     break;
                  }

                  name.append('`');
                  end += 2;
               } else {
                  name.append(c);
                  end++;
               }
            }

            p.parameterName = name.toString();
            pos = end + 1;
         } else {
            int end = pos;

            while (end < s.length() && !Character.isWhitespace(s.charAt(end))) {
               end++;
            }

            p.parameterName = s.substring(pos, end);
            pos = end;
         }

         while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
            pos++;
         }

         fillTypeFields(p, s.substring(pos));
         out.add(p);
      }
   }

   private static void fillTypeFields(CallableParameterMetaData.ParamInfo p, String typeExpr) {
      if (typeExpr != null) {
         String t = typeExpr.trim();
         if (!t.isEmpty()) {
            p.dtdIdentifier = t.toUpperCase(Locale.ROOT);
            int e = 0;

            while (e < t.length() && !Character.isWhitespace(t.charAt(e)) && t.charAt(e) != '(') {
               e++;
            }

            p.dataType = t.substring(0, e).toUpperCase(Locale.ROOT);
            String op = p.dataType;
            switch (op) {
               case "BOOLEAN":
               case "BOOL":
                  p.dataType = "TINYINT";
                  p.dtdIdentifier = "TINYINT(1)";
                  break;
               case "INTEGER":
                  p.dataType = "INT";
                  break;
               case "NUMERIC":
               case "DEC":
               case "FIXED":
                  p.dataType = "DECIMAL";
            }

            int op = e;

            while (op < t.length() && Character.isWhitespace(t.charAt(op))) {
               op++;
            }

            if (op < t.length() && t.charAt(op) == '(') {
               int depth = 1;
               int closeIdx = -1;

               for (int i = op + 1; i < t.length(); i++) {
                  char c = t.charAt(i);
                  if (c == '(') {
                     depth++;
                  } else if (c == ')') {
                     if (--depth == 0) {
                        closeIdx = i;
                        break;
                     }
                  }
               }

               if (closeIdx > op) {
                  String inner = t.substring(op + 1, closeIdx).trim();
                  int comma = inner.indexOf(44);

                  try {
                     if (comma < 0) {
                        int n = Integer.parseInt(inner);
                        if (isNumericType(p.dataType)) {
                           p.numericPrecision = n;
                        } else {
                           p.characterMaxLength = n;
                        }
                     } else {
                        p.numericPrecision = Integer.parseInt(inner.substring(0, comma).trim());
                        p.numericScale = Integer.parseInt(inner.substring(comma + 1).trim());
                     }
                  } catch (NumberFormatException var10) {
                  }
               }
            }

            if (p.numericPrecision == 0) {
               boolean unsigned = p.dtdIdentifier != null && p.dtdIdentifier.contains(" UNSIGNED");
               p.numericPrecision = defaultNumericPrecision(p.dataType, unsigned);
            }
         }
      }
   }

   private static int defaultNumericPrecision(String dt, boolean unsigned) {
      if (dt == null) {
         return 0;
      } else {
         switch (dt) {
            case "TINYINT":
               return 3;
            case "SMALLINT":
               return 5;
            case "MEDIUMINT":
               return unsigned ? 8 : 7;
            case "INT":
               return 10;
            case "BIGINT":
               return unsigned ? 20 : 19;
            case "FLOAT":
               return 12;
            case "DOUBLE":
            case "REAL":
               return 22;
            default:
               return 0;
         }
      }
   }

   private static boolean isNumericType(String dt) {
      if (dt == null) {
         return false;
      } else {
         switch (dt) {
            case "BIT":
            case "TINYINT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "INT":
            case "INTEGER":
            case "BIGINT":
            case "DECIMAL":
            case "NUMERIC":
            case "FLOAT":
            case "DOUBLE":
            case "REAL":
            case "YEAR":
               return true;
            default:
               return false;
         }
      }
   }
}
