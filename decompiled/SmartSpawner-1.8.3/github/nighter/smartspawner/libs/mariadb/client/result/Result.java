package github.nighter.smartspawner.libs.mariadb.client.result;

import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.client.Column;
import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Completion;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.result.rowdecoder.BinaryRowDecoder;
import github.nighter.smartspawner.libs.mariadb.client.result.rowdecoder.RowDecoder;
import github.nighter.smartspawner.libs.mariadb.client.result.rowdecoder.TextRowDecoder;
import github.nighter.smartspawner.libs.mariadb.client.socket.Reader;
import github.nighter.smartspawner.libs.mariadb.client.util.ClosableLock;
import github.nighter.smartspawner.libs.mariadb.client.util.MutableInt;
import github.nighter.smartspawner.libs.mariadb.export.ExceptionFactory;
import github.nighter.smartspawner.libs.mariadb.message.server.ErrorPacket;
import github.nighter.smartspawner.libs.mariadb.plugin.Codec;
import github.nighter.smartspawner.libs.mariadb.plugin.array.FloatArray;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.BigDecimalCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.BigIntegerCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.BlobCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.ByteArrayCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.ClobCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.FloatArrayCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.ReaderCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.StreamCodec;
import github.nighter.smartspawner.libs.mariadb.plugin.codec.StringCodec;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.SQLType;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class Result implements ResultSet, Completion {
   public static final int NULL_LENGTH = -1;
   private static final BinaryRowDecoder BINARY_ROW_DECODER = new BinaryRowDecoder();
   private static final TextRowDecoder TEXT_ROW_DECODER = new TextRowDecoder();
   protected final int resultSetType;
   protected final ExceptionFactory exceptionFactory;
   protected final Reader reader;
   protected final Context context;
   protected final ColumnDecoder[] metadataList;
   protected final RowDecoder rowDecoder;
   protected final ReadableByteBuf rowBuf = new ReadableByteBuf(null, 0);
   protected final boolean traceEnable;
   private final int maxIndex;
   private final MutableInt fieldLength = new MutableInt(0);
   private final boolean forceAlias;
   private final byte[] nullBitmap;
   protected int dataSize = 0;
   protected byte[][] data;
   protected MutableInt fieldIndex = new MutableInt();
   protected boolean loaded;
   protected boolean outputParameter;
   protected int rowPointer = -1;
   protected boolean closed;
   protected Statement statement;
   protected long maxRows;
   private boolean closeOnCompletion;
   private Map<String, Integer> mapper = null;
   private int fetchSize;

   public Result(
      github.nighter.smartspawner.libs.mariadb.Statement stmt,
      boolean binaryProtocol,
      long maxRows,
      ColumnDecoder[] metadataList,
      Reader reader,
      Context context,
      int resultSetType,
      boolean closeOnCompletion,
      boolean traceEnable,
      boolean forceAlias,
      int fetchSize
   ) {
      this.maxRows = maxRows;
      this.statement = stmt;
      this.closeOnCompletion = closeOnCompletion;
      this.metadataList = metadataList;
      this.maxIndex = this.metadataList.length;
      this.reader = reader;
      this.exceptionFactory = context.getExceptionFactory();
      this.context = context;
      this.resultSetType = resultSetType;
      this.traceEnable = traceEnable;
      this.forceAlias = forceAlias;
      this.fetchSize = fetchSize;
      if (binaryProtocol) {
         this.rowDecoder = BINARY_ROW_DECODER;
         this.nullBitmap = new byte[(this.maxIndex + 9) / 8];
      } else {
         this.rowDecoder = TEXT_ROW_DECODER;
         this.nullBitmap = null;
      }
   }

   protected Result(ColumnDecoder[] metadataList, Result prev) {
      this.maxRows = prev.maxRows;
      this.statement = prev.statement;
      this.closeOnCompletion = prev.closeOnCompletion;
      this.metadataList = metadataList;
      this.maxIndex = metadataList.length;
      this.reader = prev.reader;
      this.exceptionFactory = prev.exceptionFactory;
      this.context = prev.context;
      this.resultSetType = prev.resultSetType;
      this.traceEnable = prev.traceEnable;
      this.forceAlias = true;
      this.rowDecoder = prev.rowDecoder;
      this.nullBitmap = prev.nullBitmap;
      this.data = prev.data;
      this.dataSize = prev.dataSize;
      this.fetchSize = prev.fetchSize;
      this.loaded = prev.loaded;
      this.outputParameter = prev.outputParameter;
   }

   public Result(ColumnDecoder[] metadataList, byte[][] data, Context context, int resultSetType) {
      this.metadataList = metadataList;
      this.maxIndex = this.metadataList.length;
      this.reader = null;
      this.loaded = true;
      this.exceptionFactory = context.getExceptionFactory();
      this.context = context;
      this.data = data;
      this.dataSize = data.length;
      this.statement = null;
      this.resultSetType = resultSetType;
      this.closeOnCompletion = false;
      this.traceEnable = false;
      this.rowDecoder = TEXT_ROW_DECODER;
      this.nullBitmap = null;
      this.forceAlias = false;
   }

   protected boolean readNext(byte[] buf) throws IOException, SQLException {
      switch (buf[0]) {
         case -2:
            if (this.context.isEofDeprecated() && buf.length < 16777215 || !this.context.isEofDeprecated() && buf.length < 8) {
               ReadableByteBuf readBuf = this.reader.readableBufFromArray(buf);
               readBuf.skip();
               int serverStatus;
               int warnings;
               if (!this.context.isEofDeprecated()) {
                  warnings = readBuf.readUnsignedShort();
                  serverStatus = readBuf.readUnsignedShort();
               } else {
                  readBuf.readLongLengthEncodedNotNull();
                  readBuf.readLongLengthEncodedNotNull();
                  serverStatus = readBuf.readUnsignedShort();
                  warnings = readBuf.readUnsignedShort();
               }

               this.outputParameter = (serverStatus & 4096) != 0;
               if ((serverStatus & 8) == 0) {
                  this.setBulkResult();
               }

               this.context.setServerStatus(serverStatus);
               this.context.setWarning(warnings);
               this.loaded = true;
               return false;
            }
         default:
            if (this.dataSize + 1 > this.data.length) {
               this.growDataArray();
            }

            this.data[this.dataSize++] = buf;
            return true;
         case -1:
            this.loaded = true;
            ErrorPacket errorPacket = new ErrorPacket(this.reader.readableBufFromArray(buf), this.context);
            throw this.exceptionFactory.create(errorPacket.getMessage(), errorPacket.getSqlState(), errorPacket.getErrorCode());
      }
   }

   public abstract void setBulkResult();

   public void closeOnCompletion() throws SQLException {
      this.closeOnCompletion = true;
   }

   protected void skipRemaining() throws IOException, SQLException {
      while (true) {
         ReadableByteBuf buf = this.reader.readReusablePacket(this.traceEnable);
         switch (buf.getUnsignedByte()) {
            case 254:
               if (this.context.isEofDeprecated() && buf.readableBytes() < 16777215 || !this.context.isEofDeprecated() && buf.readableBytes() < 8) {
                  buf.skip();
                  int serverStatus;
                  int warnings;
                  if (!this.context.isEofDeprecated()) {
                     warnings = buf.readUnsignedShort();
                     serverStatus = buf.readUnsignedShort();
                  } else {
                     buf.readLongLengthEncodedNotNull();
                     buf.readLongLengthEncodedNotNull();
                     serverStatus = buf.readUnsignedShort();
                     warnings = buf.readUnsignedShort();
                  }

                  this.outputParameter = (serverStatus & 4096) != 0;
                  this.context.setServerStatus(serverStatus);
                  this.context.setWarning(warnings);
                  this.loaded = true;
                  return;
               }
               break;
            case 255:
               this.loaded = true;
               ErrorPacket errorPacket = new ErrorPacket(buf, this.context);
               throw this.exceptionFactory.create(errorPacket.getMessage(), errorPacket.getSqlState(), errorPacket.getErrorCode());
         }
      }
   }

   private void growDataArray() {
      int newCapacity = Math.max(10, this.data.length << 1);
      byte[][] newData = new byte[newCapacity][];
      System.arraycopy(this.data, 0, newData, 0, this.data.length);
      this.data = newData;
   }

   @Override
   public abstract boolean next() throws SQLException;

   public abstract boolean streaming();

   public abstract boolean isBulkResult();

   public abstract void fetchRemaining() throws SQLException;

   public boolean loaded() {
      return this.loaded;
   }

   public boolean isOutputParameter() {
      return this.outputParameter;
   }

   @Override
   public void close() throws SQLException {
      if (!this.loaded) {
         try {
            this.skipRemaining();
         } catch (SQLTimeoutException var2) {
         } catch (IOException var3) {
            throw this.exceptionFactory.create("Error while streaming resultSet data", "08000", var3);
         }
      }

      this.closed = true;
      if (this.closeOnCompletion && (this.context.getServerStatus() & 8) == 0) {
         this.statement.close();
      }
   }

   public void closeFromStmtClose(ClosableLock lock) throws SQLException {
      ClosableLock ignore = lock.closeableLock();

      try {
         this.fetchRemaining();
         this.closed = true;
      } catch (Throwable var6) {
         if (ignore != null) {
            try {
               ignore.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (ignore != null) {
         ignore.close();
      }
   }

   public void abort() {
      this.closed = true;
   }

   protected byte[] getCurrentRowData() {
      return this.data[0];
   }

   protected void addRowData(byte[] buf) {
      if (this.dataSize + 1 > this.data.length) {
         this.growDataArray();
      }

      this.data[this.dataSize++] = buf;
   }

   protected void updateRowData(byte[] rawData) {
      this.data[this.rowPointer] = rawData;
      if (rawData == null) {
         this.setNullRowBuf();
      } else {
         this.setRow(rawData);
         this.fieldIndex.set(-1);
      }
   }

   private void checkIndex(int index) throws SQLException {
      if (index < 1 || index > this.maxIndex) {
         throw new SQLException(String.format("Wrong index position. Is %s but must be in 1-%s range", index, this.maxIndex));
      } else if (this.rowBuf.buf == null) {
         throw new SQLDataException("wrong row position", "22023");
      }
   }

   @Override
   public boolean wasNull() {
      return this.rowDecoder.wasNull(this.nullBitmap, this.fieldIndex, this.fieldLength);
   }

   @Override
   public String getString(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decodeString(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength, this.context);
   }

   @Override
   public boolean getBoolean(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1 ? false : this.rowDecoder.decodeBoolean(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength);
   }

   @Override
   public byte getByte(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1 ? 0 : this.rowDecoder.decodeByte(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength);
   }

   @Override
   public short getShort(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1 ? 0 : this.rowDecoder.decodeShort(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength);
   }

   @Override
   public int getInt(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1 ? 0 : this.rowDecoder.decodeInt(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength);
   }

   @Override
   public long getLong(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1 ? 0L : this.rowDecoder.decodeLong(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength);
   }

   public BigInteger getBigInteger(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decode(BigIntegerCodec.INSTANCE, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
   }

   public BigInteger getBigInteger(String columnLabel) throws SQLException {
      return this.getBigInteger(this.findColumn(columnLabel));
   }

   @Override
   public float getFloat(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1 ? 0.0F : this.rowDecoder.decodeFloat(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength);
   }

   @Override
   public double getDouble(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1 ? 0.0 : this.rowDecoder.decodeDouble(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength);
   }

   @Deprecated
   @Override
   public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      if (this.fieldLength.get() == -1) {
         return null;
      } else {
         BigDecimal d = this.rowDecoder.decode(BigDecimalCodec.INSTANCE, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
         return d == null ? null : d.setScale(scale, RoundingMode.HALF_DOWN);
      }
   }

   @Override
   public byte[] getBytes(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decode(ByteArrayCodec.INSTANCE, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
   }

   @Override
   public Date getDate(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decodeDate(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength, null, this.context);
   }

   @Override
   public Time getTime(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decodeTime(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength, null, this.context);
   }

   @Override
   public Timestamp getTimestamp(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decodeTimestamp(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength, null, this.context);
   }

   @Override
   public InputStream getAsciiStream(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decode(StreamCodec.INSTANCE, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
   }

   @Deprecated
   @Override
   public InputStream getUnicodeStream(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decode(StreamCodec.INSTANCE, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
   }

   @Override
   public InputStream getBinaryStream(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decode(StreamCodec.INSTANCE, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
   }

   @Override
   public String getString(String columnLabel) throws SQLException {
      return this.getString(this.findColumn(columnLabel));
   }

   @Override
   public boolean getBoolean(String columnLabel) throws SQLException {
      return this.getBoolean(this.findColumn(columnLabel));
   }

   @Override
   public byte getByte(String columnLabel) throws SQLException {
      return this.getByte(this.findColumn(columnLabel));
   }

   @Override
   public short getShort(String columnLabel) throws SQLException {
      return this.getShort(this.findColumn(columnLabel));
   }

   @Override
   public int getInt(String columnLabel) throws SQLException {
      return this.getInt(this.findColumn(columnLabel));
   }

   @Override
   public long getLong(String columnLabel) throws SQLException {
      return this.getLong(this.findColumn(columnLabel));
   }

   @Override
   public float getFloat(String columnLabel) throws SQLException {
      return this.getFloat(this.findColumn(columnLabel));
   }

   @Override
   public double getDouble(String columnLabel) throws SQLException {
      return this.getDouble(this.findColumn(columnLabel));
   }

   @Deprecated
   @Override
   public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
      return this.getBigDecimal(this.findColumn(columnLabel), scale);
   }

   @Override
   public byte[] getBytes(String columnLabel) throws SQLException {
      return this.getBytes(this.findColumn(columnLabel));
   }

   @Override
   public Date getDate(String columnLabel) throws SQLException {
      return this.getDate(this.findColumn(columnLabel));
   }

   @Override
   public Time getTime(String columnLabel) throws SQLException {
      return this.getTime(this.findColumn(columnLabel));
   }

   @Override
   public Timestamp getTimestamp(String columnLabel) throws SQLException {
      return this.getTimestamp(this.findColumn(columnLabel));
   }

   @Override
   public InputStream getAsciiStream(String columnLabel) throws SQLException {
      return this.getAsciiStream(this.findColumn(columnLabel));
   }

   @Deprecated
   @Override
   public InputStream getUnicodeStream(String columnLabel) throws SQLException {
      return this.getUnicodeStream(this.findColumn(columnLabel));
   }

   @Override
   public InputStream getBinaryStream(String columnLabel) throws SQLException {
      return this.getBinaryStream(this.findColumn(columnLabel));
   }

   @Override
   public SQLWarning getWarnings() throws SQLException {
      return this.statement == null ? null : this.statement.getWarnings();
   }

   @Override
   public void clearWarnings() throws SQLException {
      if (this.statement != null) {
         this.statement.clearWarnings();
      }
   }

   @Override
   public String getCursorName() throws SQLException {
      throw this.exceptionFactory.notSupported("Cursors are not supported");
   }

   public ResultSetMetaData getMetaData() {
      return new ResultSetMetaData(this.exceptionFactory, this.metadataList, this.context.getConf(), this.forceAlias);
   }

   @Override
   public Object getObject(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.defaultDecode(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength, this.context);
   }

   @Override
   public Object getObject(String columnLabel) throws SQLException {
      return this.getObject(this.findColumn(columnLabel));
   }

   @Override
   public java.io.Reader getCharacterStream(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decode(ReaderCodec.INSTANCE, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
   }

   @Override
   public java.io.Reader getCharacterStream(String columnLabel) throws SQLException {
      return this.getCharacterStream(this.findColumn(columnLabel));
   }

   @Override
   public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decode(BigDecimalCodec.INSTANCE, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
   }

   @Override
   public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
      return this.getBigDecimal(this.findColumn(columnLabel));
   }

   protected void checkClose() throws SQLException {
      if (this.closed) {
         throw this.exceptionFactory.create("Operation not permit on a closed resultSet", "HY000");
      }
   }

   protected void checkNotForwardOnly() throws SQLException {
      if (this.resultSetType == 1003) {
         throw this.exceptionFactory.create("Operation not permit on TYPE_FORWARD_ONLY resultSet", "HY000");
      }
   }

   @Override
   public boolean isBeforeFirst() throws SQLException {
      this.checkClose();
      return this.rowPointer == -1 && this.dataSize > 0;
   }

   @Override
   public abstract boolean isAfterLast() throws SQLException;

   @Override
   public abstract boolean isFirst() throws SQLException;

   @Override
   public abstract boolean isLast() throws SQLException;

   @Override
   public abstract void beforeFirst() throws SQLException;

   @Override
   public abstract void afterLast() throws SQLException;

   @Override
   public abstract boolean first() throws SQLException;

   @Override
   public abstract boolean last() throws SQLException;

   @Override
   public abstract int getRow() throws SQLException;

   public void setRow(byte[] row) {
      this.rowBuf.buf(row, row.length, 0);
      this.fieldIndex.set(-1);
   }

   @Override
   public abstract boolean absolute(int var1) throws SQLException;

   @Override
   public abstract boolean relative(int var1) throws SQLException;

   @Override
   public abstract boolean previous() throws SQLException;

   @Override
   public int getFetchDirection() {
      return 1002;
   }

   @Override
   public void setFetchDirection(int direction) throws SQLException {
      if (direction == 1001) {
         throw this.exceptionFactory.create("Invalid operation. Allowed direction are ResultSet.FETCH_FORWARD and ResultSet.FETCH_UNKNOWN");
      }
   }

   @Override
   public int getType() {
      return this.resultSetType;
   }

   @Override
   public int getConcurrency() {
      return 1007;
   }

   @Override
   public boolean rowUpdated() throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public boolean rowInserted() throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public boolean rowDeleted() throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateNull(int columnIndex) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBoolean(int columnIndex, boolean x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateByte(int columnIndex, byte x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateShort(int columnIndex, short x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateInt(int columnIndex, int x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateLong(int columnIndex, long x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateFloat(int columnIndex, float x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateDouble(int columnIndex, double x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateString(int columnIndex, String x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBytes(int columnIndex, byte[] x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateDate(int columnIndex, Date x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateTime(int columnIndex, Time x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateCharacterStream(int columnIndex, java.io.Reader x, int length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateObject(int columnIndex, Object x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateNull(String columnLabel) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBoolean(String columnLabel, boolean x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateByte(String columnLabel, byte x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateShort(String columnLabel, short x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateInt(String columnLabel, int x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateLong(String columnLabel, long x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateFloat(String columnLabel, float x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateDouble(String columnLabel, double x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateString(String columnLabel, String x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBytes(String columnLabel, byte[] x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateDate(String columnLabel, Date x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateTime(String columnLabel, Time x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateCharacterStream(String columnLabel, java.io.Reader reader, int length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateObject(String columnLabel, Object x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void insertRow() throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateRow() throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void deleteRow() throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void refreshRow() throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void cancelRowUpdates() throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void moveToInsertRow() throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void moveToCurrentRow() throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public Statement getStatement() {
      return this.statement;
   }

   public void setStatement(Statement stmt) {
      this.statement = stmt;
   }

   @Override
   public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
      if (map != null && !map.isEmpty()) {
         throw this.exceptionFactory.notSupported("Method ResultSet.getObject(int columnIndex, Map<String, Class<?>> map) not supported for non empty map");
      } else {
         return this.getObject(columnIndex);
      }
   }

   @Override
   public Ref getRef(int columnIndex) throws SQLException {
      throw this.exceptionFactory.notSupported("Method ResultSet.getRef not supported");
   }

   @Override
   public Blob getBlob(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decode(BlobCodec.INSTANCE, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
   }

   @Override
   public Clob getClob(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decode(ClobCodec.INSTANCE, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
   }

   @Override
   public Array getArray(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      if (this.fieldLength.get() == -1) {
         return null;
      } else {
         float[] val = this.rowDecoder.decode(FloatArrayCodec.INSTANCE, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
         return new FloatArray(val, this.context);
      }
   }

   @Override
   public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
      if (map != null && !map.isEmpty()) {
         throw this.exceptionFactory.notSupported("Method ResultSet.getObject(String columnLabel, Map<String, Class<?>> map) not supported");
      } else {
         return this.getObject(columnLabel);
      }
   }

   @Override
   public Ref getRef(String columnLabel) throws SQLException {
      throw this.exceptionFactory.notSupported("Method ResultSet.getRef not supported");
   }

   @Override
   public Blob getBlob(String columnLabel) throws SQLException {
      return this.getBlob(this.findColumn(columnLabel));
   }

   @Override
   public Clob getClob(String columnLabel) throws SQLException {
      return this.getClob(this.findColumn(columnLabel));
   }

   @Override
   public Array getArray(String columnLabel) throws SQLException {
      return this.getArray(this.findColumn(columnLabel));
   }

   @Override
   public Date getDate(int columnIndex, Calendar cal) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decodeDate(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength, cal, this.context);
   }

   @Override
   public Date getDate(String columnLabel, Calendar cal) throws SQLException {
      return this.getDate(this.findColumn(columnLabel), cal);
   }

   @Override
   public Time getTime(int columnIndex, Calendar cal) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decodeTime(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength, cal, this.context);
   }

   @Override
   public Time getTime(String columnLabel, Calendar cal) throws SQLException {
      return this.getTime(this.findColumn(columnLabel), cal);
   }

   @Override
   public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decodeTimestamp(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength, cal, this.context);
   }

   @Override
   public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
      return this.getTimestamp(this.findColumn(columnLabel), cal);
   }

   @Override
   public URL getURL(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      if (this.fieldLength.get() == -1) {
         return null;
      } else {
         String s = this.rowDecoder.decode(StringCodec.INSTANCE, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
         if (s == null) {
            return null;
         } else {
            try {
               return new URI(s).toURL();
            } catch (Exception var4) {
               throw this.exceptionFactory.create(String.format("Could not parse '%s' as URL", s));
            }
         }
      }
   }

   @Override
   public URL getURL(String columnLabel) throws SQLException {
      return this.getURL(this.findColumn(columnLabel));
   }

   @Override
   public void updateRef(int columnIndex, Ref x) throws SQLException {
      throw this.exceptionFactory.notSupported("Method ResultSet.updateRef not supported");
   }

   @Override
   public void updateRef(String columnLabel, Ref x) throws SQLException {
      throw this.exceptionFactory.notSupported("Method ResultSet.updateRef not supported");
   }

   @Override
   public void updateBlob(int columnIndex, Blob x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBlob(String columnLabel, Blob x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateClob(int columnIndex, Clob x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateClob(String columnLabel, Clob x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateArray(int columnIndex, Array x) throws SQLException {
      throw this.exceptionFactory.notSupported("Array are not supported");
   }

   @Override
   public void updateArray(String columnLabel, Array x) throws SQLException {
      this.updateArray(this.findColumn(columnLabel), x);
   }

   @Override
   public RowId getRowId(int columnIndex) throws SQLException {
      throw this.exceptionFactory.notSupported("RowId are not supported");
   }

   @Override
   public RowId getRowId(String columnLabel) throws SQLException {
      throw this.exceptionFactory.notSupported("RowId are not supported");
   }

   @Override
   public void updateRowId(int columnIndex, RowId x) throws SQLException {
      throw this.exceptionFactory.notSupported("RowId are not supported");
   }

   @Override
   public void updateRowId(String columnLabel, RowId x) throws SQLException {
      throw this.exceptionFactory.notSupported("RowId are not supported");
   }

   @Override
   public int getHoldability() {
      return 1;
   }

   @Override
   public boolean isClosed() {
      return this.closed;
   }

   @Override
   public void updateNString(int columnIndex, String nString) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateNString(String columnLabel, String nString) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public NClob getNClob(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : (NClob)this.rowDecoder.decode(ClobCodec.INSTANCE, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
   }

   @Override
   public NClob getNClob(String columnLabel) throws SQLException {
      return this.getNClob(this.findColumn(columnLabel));
   }

   @Override
   public SQLXML getSQLXML(int columnIndex) throws SQLException {
      throw this.exceptionFactory.notSupported("Method ResultSet.getSQLXML not supported");
   }

   @Override
   public SQLXML getSQLXML(String columnLabel) throws SQLException {
      throw this.exceptionFactory.notSupported("Method ResultSet.getSQLXML not supported");
   }

   @Override
   public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
      throw this.exceptionFactory.notSupported("Method ResultSet.updateSQLXML not supported");
   }

   @Override
   public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
      throw this.exceptionFactory.notSupported("Method ResultSet.updateSQLXML not supported");
   }

   @Override
   public String getNString(int columnIndex) throws SQLException {
      return this.getString(columnIndex);
   }

   @Override
   public String getNString(String columnLabel) throws SQLException {
      return this.getString(columnLabel);
   }

   @Override
   public java.io.Reader getNCharacterStream(int columnIndex) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      return this.fieldLength.get() == -1
         ? null
         : this.rowDecoder.decode(ReaderCodec.INSTANCE, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
   }

   @Override
   public java.io.Reader getNCharacterStream(String columnLabel) throws SQLException {
      return this.getNCharacterStream(this.findColumn(columnLabel));
   }

   @Override
   public void updateNCharacterStream(int columnIndex, java.io.Reader x, long length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateNCharacterStream(String columnLabel, java.io.Reader reader, long length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateCharacterStream(int columnIndex, java.io.Reader x, long length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateCharacterStream(String columnLabel, java.io.Reader reader, long length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateClob(int columnIndex, java.io.Reader reader, long length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateClob(String columnLabel, java.io.Reader reader, long length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateNClob(int columnIndex, java.io.Reader reader, long length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateNClob(String columnLabel, java.io.Reader reader, long length) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateNCharacterStream(int columnIndex, java.io.Reader x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateNCharacterStream(String columnLabel, java.io.Reader reader) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateCharacterStream(int columnIndex, java.io.Reader x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateCharacterStream(String columnLabel, java.io.Reader reader) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateClob(int columnIndex, java.io.Reader reader) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateClob(String columnLabel, java.io.Reader reader) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateNClob(int columnIndex, java.io.Reader reader) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateNClob(String columnLabel, java.io.Reader reader) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
      this.checkIndex(columnIndex);
      this.fieldLength.set(this.rowDecoder.setPosition(columnIndex - 1, this.fieldIndex, this.maxIndex, this.rowBuf, this.nullBitmap, this.metadataList));
      if (this.wasNull()) {
         if (type.isPrimitive()) {
            throw new SQLException(String.format("Cannot return null for primitive %s", type.getName()));
         } else {
            return null;
         }
      } else {
         ColumnDecoder column = this.metadataList[columnIndex - 1];
         if (!Object.class.equals(type) && type != null) {
            Configuration conf = this.context.getConf();

            for (Codec<?> codec : conf.codecs()) {
               if (codec.canDecode(column, type)) {
                  return this.rowDecoder.decode((Codec<T>)codec, null, this.rowBuf, this.fieldLength, this.metadataList, columnIndex - 1, this.context);
               }
            }

            this.rowBuf.skip(this.fieldLength.get());
            throw new SQLException(String.format("Type %s not supported type for %s type", type, column.getType().name()));
         } else {
            return (T)this.rowDecoder.defaultDecode(this.metadataList, columnIndex - 1, this.rowBuf, this.fieldLength, this.context);
         }
      }
   }

   @Override
   public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
      return this.getObject(this.findColumn(columnLabel), type);
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

   @Override
   public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   @Override
   public void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SQLException {
      throw this.exceptionFactory.notSupported("Not supported when using CONCUR_READ_ONLY concurrency");
   }

   protected void setNullRowBuf() {
      this.rowBuf.buf(null, 0, 0);
   }

   @Override
   public int findColumn(String label) throws SQLException {
      if (label == null) {
         throw new SQLException("null is not a valid label value");
      } else {
         if (this.mapper == null) {
            this.mapper = new HashMap<>((int)(this.maxIndex * 2.5));

            for (int i = 0; i < this.maxIndex; i++) {
               Column ci = this.metadataList[i];
               String columnAlias = ci.getColumnAlias();
               if (columnAlias != null) {
                  columnAlias = columnAlias.toLowerCase(Locale.ROOT);
                  this.mapper.putIfAbsent(columnAlias, i + 1);
                  String tableAlias = ci.getTableAlias();
                  String tableLabel = tableAlias != null ? tableAlias : ci.getTable();
                  this.mapper.putIfAbsent(tableLabel.toLowerCase(Locale.ROOT) + "." + columnAlias, i + 1);
               }
            }
         }

         Integer ind = this.mapper.get(label.toLowerCase(Locale.ROOT));
         if (ind == null) {
            String keys = Arrays.toString(this.mapper.keySet().toArray(new String[0]));
            throw new SQLException(String.format("Unknown label '%s'. Possible value %s", label, keys));
         } else {
            return ind;
         }
      }
   }

   @Override
   public int getFetchSize() throws SQLException {
      return this.fetchSize;
   }

   @Override
   public void setFetchSize(int fetchSize) throws SQLException {
      if (fetchSize < 0) {
         throw this.exceptionFactory.create(String.format("invalid fetch size %s", fetchSize));
      } else {
         this.fetchSize = fetchSize;
      }
   }
}
