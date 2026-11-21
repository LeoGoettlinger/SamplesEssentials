package github.nighter.smartspawner.libs.mariadb.client.context;

import github.nighter.smartspawner.libs.mariadb.BasePreparedStatement;
import github.nighter.smartspawner.libs.mariadb.Configuration;
import github.nighter.smartspawner.libs.mariadb.HostAddress;
import github.nighter.smartspawner.libs.mariadb.client.ColumnDecoder;
import github.nighter.smartspawner.libs.mariadb.client.Context;
import github.nighter.smartspawner.libs.mariadb.client.PrepareCache;
import github.nighter.smartspawner.libs.mariadb.client.ReadableByteBuf;
import github.nighter.smartspawner.libs.mariadb.client.ServerVersion;
import github.nighter.smartspawner.libs.mariadb.export.ExceptionFactory;
import github.nighter.smartspawner.libs.mariadb.export.Prepare;
import github.nighter.smartspawner.libs.mariadb.message.server.InitialHandshakePacket;
import java.sql.SQLNonTransientConnectionException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.function.Function;

public class BaseContext implements Context {
   private final long serverCapabilities;
   private final long clientCapabilities;
   private final byte[] seed;
   private final ServerVersion version;
   private final boolean eofDeprecated;
   private final boolean skipMeta;
   private final Function<ReadableByteBuf, ColumnDecoder> columnDecoderFunction;
   private final Configuration conf;
   private final ExceptionFactory exceptionFactory;
   private final boolean canUseTransactionIsolation;
   private final Boolean loopbackAddress;
   private final PrepareCache prepareCache;
   private final HostAddress hostAddress;
   protected int serverStatus;
   private Long autoIncrement;
   private String maxscaleVersion = null;
   private long threadId;
   private String charset;
   private boolean initialized = false;
   private final Runnable connectionCloser;
   private String database;
   private Integer transactionIsolationLevel;
   private int warning;
   private int stateFlag = 0;
   private boolean serverReadOnly = false;
   private String redirectUrl = null;
   private TimeZone connectionTimeZone = null;

   public BaseContext(
      HostAddress hostAddress,
      InitialHandshakePacket handshake,
      long clientCapabilities,
      Configuration conf,
      ExceptionFactory exceptionFactory,
      PrepareCache prepareCache,
      Boolean loopbackAddress,
      Runnable connectionCloser
   ) {
      this.connectionCloser = connectionCloser;
      this.hostAddress = hostAddress;
      this.threadId = handshake.getThreadId();
      this.seed = handshake.getSeed();
      this.serverCapabilities = handshake.getCapabilities();
      this.serverStatus = handshake.getServerStatus();
      this.version = handshake.getVersion();
      this.clientCapabilities = clientCapabilities;
      this.eofDeprecated = this.hasClientCapability(16777216L);
      this.skipMeta = this.hasClientCapability(68719476736L);
      this.columnDecoderFunction = this.hasClientCapability(34359738368L) ? ColumnDecoder::decode : ColumnDecoder::decodeStd;
      this.conf = conf;
      this.database = conf.database();
      this.exceptionFactory = exceptionFactory;
      this.prepareCache = prepareCache;
      this.canUseTransactionIsolation = this.version.isMariaDBServer() && this.version.versionGreaterOrEqual(11, 1, 1)
         || !this.version.isMariaDBServer()
            && (
               this.version.getMajorVersion() >= 8 && this.version.versionGreaterOrEqual(8, 0, 3)
                  || this.version.getMajorVersion() < 8 && this.version.versionGreaterOrEqual(5, 7, 20)
            );
      this.loopbackAddress = loopbackAddress;
   }

   @Override
   public Boolean isLoopbackAddress() {
      return this.loopbackAddress;
   }

   @Override
   public long getThreadId() {
      return this.threadId;
   }

   @Override
   public void setThreadId(long connectionId) {
      this.threadId = connectionId;
   }

   @Override
   public byte[] getSeed() {
      return this.seed;
   }

   @Override
   public boolean hasServerCapability(long flag) {
      return (this.serverCapabilities & flag) > 0L;
   }

   @Override
   public boolean hasClientCapability(long flag) {
      return (this.clientCapabilities & flag) > 0L;
   }

   @Override
   public boolean permitPipeline() {
      return !this.conf.disablePipeline() && (this.clientCapabilities & 17179869184L) > 0L;
   }

   @Override
   public int getServerStatus() {
      return this.serverStatus;
   }

   @Override
   public void setServerStatus(int serverStatus) {
      this.serverStatus = serverStatus;
   }

   @Override
   public String getDatabase() {
      return this.database;
   }

   @Override
   public void setMaxscaleVersion(String maxscaleVersion) {
      this.maxscaleVersion = maxscaleVersion;
   }

   @Override
   public void setDatabase(String database) {
      this.database = database;
   }

   @Override
   public String getMaxscaleVersion() {
      return this.maxscaleVersion;
   }

   @Override
   public ServerVersion getVersion() {
      return this.version;
   }

   @Override
   public boolean isEofDeprecated() {
      return this.eofDeprecated;
   }

   @Override
   public Function<ReadableByteBuf, ColumnDecoder> getColumnDecoderFunction() {
      return this.columnDecoderFunction;
   }

   @Override
   public boolean canSkipMeta() {
      return this.skipMeta;
   }

   @Override
   public int getWarning() {
      return this.warning;
   }

   @Override
   public void setWarning(int warning) {
      this.warning = warning;
   }

   @Override
   public ExceptionFactory getExceptionFactory() {
      return this.exceptionFactory;
   }

   @Override
   public Configuration getConf() {
      return this.conf;
   }

   @Override
   public Integer getTransactionIsolationLevel() {
      return this.transactionIsolationLevel;
   }

   @Override
   public void setTransactionIsolationLevel(Integer transactionIsolationLevel) {
      this.transactionIsolationLevel = transactionIsolationLevel;
   }

   @Override
   public Prepare getPrepareCacheCmd(String sql, BasePreparedStatement preparedStatement) {
      return this.prepareCache.get(this.database + "|" + sql, preparedStatement);
   }

   @Override
   public Prepare putPrepareCacheCmd(String sql, Prepare result, BasePreparedStatement preparedStatement) {
      return this.prepareCache.put(this.database + "|" + sql, result, preparedStatement);
   }

   @Override
   public void resetPrepareCache() {
      if (this.prepareCache != null) {
         this.prepareCache.reset();
      }
   }

   @Override
   public int getStateFlag() {
      return this.stateFlag;
   }

   @Override
   public void resetStateFlag() {
      this.stateFlag = 0;
   }

   @Override
   public void addStateFlag(int state) {
      this.stateFlag |= state;
   }

   @Override
   public boolean isServerReadOnly() {
      return this.serverReadOnly;
   }

   @Override
   public void setServerReadOnly(boolean serverReadOnly) {
      this.serverReadOnly = serverReadOnly;
   }

   @Override
   public void setTreadsConnected(long threadsConnected) {
      if (this.hostAddress != null) {
         this.hostAddress.setThreadsConnected(threadsConnected);
      }
   }

   @Override
   public Long getAutoIncrement() {
      return this.autoIncrement;
   }

   @Override
   public void setAutoIncrement(long autoIncrement) {
      this.autoIncrement = autoIncrement;
   }

   @Override
   public String getCharset() {
      return this.charset;
   }

   @Override
   public void setCharset(String charset) throws SQLNonTransientConnectionException {
      if (this.initialized && charset != null && !charset.startsWith("utf8")) {
         this.connectionCloser.run();
         throw new SQLNonTransientConnectionException(
            String.format("Connection character set was changed to '%s'. Only utf8 / utf8mb3 / utf8mb4 are supported. The connection has been closed.", charset),
            "08000"
         );
      } else {
         this.charset = charset;
      }
   }

   @Override
   public void setInitialized() {
      this.initialized = true;
   }

   @Override
   public String getRedirectUrl() {
      return this.redirectUrl;
   }

   @Override
   public boolean canUseTransactionIsolation() {
      return this.canUseTransactionIsolation;
   }

   @Override
   public void setRedirectUrl(String redirectUrl) {
      this.redirectUrl = redirectUrl;
   }

   @Override
   public TimeZone getConnectionTimeZone() {
      return this.connectionTimeZone;
   }

   @Override
   public void setConnectionTimeZone(TimeZone connectionTimeZone) {
      this.connectionTimeZone = connectionTimeZone;
   }

   @Override
   public Calendar getDefaultCalendar() {
      return this.conf.preserveInstants() ? Calendar.getInstance(this.connectionTimeZone) : Calendar.getInstance();
   }
}
